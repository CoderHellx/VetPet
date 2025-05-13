package com.example.project;

import android.app.Activity;
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

import java.util.UUID;

public class AddPetActivity extends AppCompatActivity {

    private ImageView petImageView;
    private EditText inputName, inputBirthday, inputInfo;
    private Spinner inputGender, inputType;
    private Button addButton, cancelButton;
    private Uri selectedImageUri;

    private static final int PICK_IMAGE_REQUEST = 1001;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        // Link UI
        petImageView = findViewById(R.id.imagePet);
        inputName = findViewById(R.id.inputName);
        inputType = findViewById(R.id.inputType); // now it's a Spinner

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.pet_type_options, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputType.setAdapter(typeAdapter);

        inputBirthday = findViewById(R.id.inputBirthday);
        inputInfo = findViewById(R.id.inputInfo);
        inputGender = findViewById(R.id.inputGender);
        addButton = findViewById(R.id.buttonAddPet);
        cancelButton = findViewById(R.id.buttonCancel); // <-- add this button in XML

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Set up gender dropdown (Spinner)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputGender.setAdapter(adapter);

        // Image selection from gallery
        petImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Pet Picture"), PICK_IMAGE_REQUEST);
        });

        // Cancel button behavior
        cancelButton.setOnClickListener(v -> finish());

        // Add pet
        addButton.setOnClickListener(v -> {

                uploadImageAndSavePet();

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(petImageView);
        }

    }

    private void uploadImageAndSavePet() {
        String ownerId = SignInActivity.currentUser.userId;
        String petId = UUID.randomUUID().toString();

        if (selectedImageUri == null) {

            // No image selected, use default image URL or empty string
            String defaultImageUrl = ""; // or provide a default hosted image URL
            Pet pet = new Pet(
                    petId,
                    ownerId,
                    inputName.getText().toString(),
                    inputType.getSelectedItem().toString(),
                    inputBirthday.getText().toString(),
                    inputGender.getSelectedItem().toString(),
                    inputInfo.getText().toString(),
                    defaultImageUrl
            );

            db.collection("pets")
                    .document(petId)
                    .set(pet)
                    .addOnSuccessListener(aVoid -> {
                        if (SignInActivity.currentUser != null) {
                            SignInActivity.currentUser.addPetToPets(pet);
                        }

                        Toast.makeText(AddPetActivity.this, "Pet added successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(AddPetActivity.this, "Failed to add pet: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );


        } else {
            // Image selected, proceed with upload
            StorageReference ref = storage.getReference("pets/" + ownerId + "/" + petId + ".jpg");

            ref.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                Pet pet = new Pet(
                                        petId,
                                        ownerId,
                                        inputName.getText().toString(),
                                        inputType.getSelectedItem().toString(),
                                        inputBirthday.getText().toString(),
                                        inputGender.getSelectedItem().toString(),
                                        inputInfo.getText().toString(),
                                        uri.toString()
                                );

                                db.collection("pets")
                                        .document(petId)
                                        .set(pet)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(AddPetActivity.this, "Pet added successfully!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(AddPetActivity.this, "Failed to add pet: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            }).addOnFailureListener(e -> {
                                Toast.makeText(AddPetActivity.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            })
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(AddPetActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }
    }


}
