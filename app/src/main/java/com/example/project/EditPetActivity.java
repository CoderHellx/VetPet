package com.example.project;

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

public class EditPetActivity extends AppCompatActivity {

    private ImageView imagePet;
    private EditText  inputName, inputBirthday, inputInfo;
    private Spinner inputType, inputGender;
    private Button buttonSave, buttonCancel, buttonDelete;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;
    private String ownerId;
    private Pet pet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pet);

        // Link views
        imagePet = findViewById(R.id.imagePet);

        inputName = findViewById(R.id.inputName);
        inputBirthday = findViewById(R.id.inputBirthday);
        inputInfo = findViewById(R.id.inputInfo);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonDelete = findViewById(R.id.buttonDelete);
        inputType = findViewById(R.id.inputType);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.pet_type_options, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputType.setAdapter(typeAdapter);

        inputGender = findViewById(R.id.inputGender);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputGender.setAdapter(genderAdapter);


        // Get Firebase + intent
        db = FirebaseFirestore.getInstance();
        ownerId = Utils.currentUser.userId;
        pet = (Pet) getIntent().getSerializableExtra("pet");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving changes...");
        progressDialog.setCancelable(false);


        if (pet == null) {
            Toast.makeText(this, "Pet data missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        if (pet != null) {
            int typePosition = typeAdapter.getPosition(pet.getSpecies());
            inputType.setSelection(typePosition);

            int genderPosition = genderAdapter.getPosition(pet.getGender());
            inputGender.setSelection(genderPosition);

            inputName.setText(pet.getName());
            inputBirthday.setText(pet.getBirthday());
            inputInfo.setText(pet.getAdditionalInfo());
            Glide.with(this)
                    .load(pet.getImageUrl())
                    .placeholder(R.drawable.circle_background)
                    .circleCrop()
                    .into(imagePet);

            imagePet.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Pet Image"), PICK_IMAGE_REQUEST);
            });


        }

        buttonCancel.setOnClickListener(v -> finish());

        buttonDelete.setOnClickListener(v -> {
            db.collection("pets").document(pet.getId()).delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Pet deleted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EditPetActivity.this, HomepageActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
        });

        buttonSave.setOnClickListener(v -> {
            pet.setSpecies(inputType.getSelectedItem().toString());
            pet.setGender(inputGender.getSelectedItem().toString());
            pet.setName(inputName.getText().toString());
            pet.setBirthday(inputBirthday.getText().toString());
            pet.setAdditionalInfo(inputInfo.getText().toString());

            progressDialog.show();

            if (selectedImageUri != null) {
                StorageReference storageRef = FirebaseStorage.getInstance()
                        .getReference("pets/" + ownerId + "/" + pet.getId() + ".jpg");

                storageRef.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            pet.setImageUrl(uri.toString());

                            db.collection("pets").document(pet.getId()).set(pet)
                                    .addOnSuccessListener(unused -> {
                                        progressDialog.dismiss(); 
                                        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                        }))
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                db.collection("pets").document(pet.getId()).set(pet)
                        .addOnSuccessListener(unused -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Failed to save changes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).circleCrop().into(imagePet);
        }
    }

}
