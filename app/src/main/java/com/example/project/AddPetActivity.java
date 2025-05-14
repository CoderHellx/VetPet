package com.example.project;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.UUID;

public class AddPetActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;
    private ImageView petImageView;
    private EditText inputName, inputBirthday, inputInfo;
    private Spinner inputGender, inputType;
    private Button addButton, cancelButton;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding pet...");
        progressDialog.setCancelable(false);


        // Link UI
        petImageView = findViewById(R.id.imagePet);
        inputName = findViewById(R.id.inputName);
        inputBirthday = findViewById(R.id.inputBirthday);
        inputInfo = findViewById(R.id.inputInfo);
        inputGender = findViewById(R.id.inputGender);
        inputType = findViewById(R.id.inputType);
        addButton = findViewById(R.id.buttonAddPet);
        cancelButton = findViewById(R.id.buttonCancel);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Populate gender spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputGender.setAdapter(genderAdapter);

        // Populate type spinner
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.pet_type_options, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputType.setAdapter(typeAdapter);

        // Image picker
        petImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Pet Picture"), PICK_IMAGE_REQUEST);
        });

        // Cancel
        cancelButton.setOnClickListener(v -> finish());

        // Add pet
        addButton.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String birthday = inputBirthday.getText().toString().trim();
            String info = inputInfo.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter the pet's name.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (birthday.isEmpty()) {
                Toast.makeText(this, "Please enter the pet's birthday.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Simple age calc
            int birthYear;
            try {
                String[] parts = birthday.split("/");
                birthYear = Integer.parseInt(parts[2]);
            } catch (Exception e) {
                Toast.makeText(this, "Birthday must be in format dd/MM/yyyy.", Toast.LENGTH_SHORT).show();
                return;
            }

            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int age = currentYear - birthYear;

            progressDialog.show();

            uploadImageAndSavePet(name, birthday, info, age);
        });
    }

    private void uploadImageAndSavePet(String name, String birthday, String info, int age) {
        String ownerId = Utils.currentUser.userId;
        String petId = UUID.randomUUID().toString();

        if (selectedImageUri == null) {
            // No image selected, proceed with default or empty image URL
            savePetToFirestore(name, birthday, info, "", ownerId, petId, age);
        } else {
            // Upload image to Firebase Storage
            StorageReference ref = storage.getReference("pets/" + ownerId + "/" + petId + ".jpg");
            ref.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        savePetToFirestore(name, birthday, info, uri.toString(), ownerId, petId, age);
                    }).addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_LONG).show()))
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private void savePetToFirestore(String name, String birthday, String info, String imageUrl,
                                    String ownerId, String petId, int age) {

        Pet pet = new Pet(
                petId,
                ownerId,
                name,
                inputType.getSelectedItem().toString(),
                birthday,
                inputGender.getSelectedItem().toString(),
                info,
                imageUrl
        );

        pet.setAge(age);

        db.collection("pets")
                .document(petId)
                .set(pet)
                .addOnSuccessListener(aVoid -> {
                    if (Utils.currentUser != null) {
                        Utils.currentUser.addPetToPets(pet);
                    }
                    Toast.makeText(this, "Pet added successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add pet: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).circleCrop().into(petImageView);
        }
    }
}
