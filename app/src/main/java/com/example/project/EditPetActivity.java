package com.example.project;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditPetActivity extends AppCompatActivity {

    private ImageView imagePet;
    private EditText  inputName, inputBirthday, inputInfo;
    private Spinner inputType, inputGender;
    private Button buttonSave, buttonCancel, buttonDelete;

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
        ownerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pet = (Pet) getIntent().getSerializableExtra("pet");

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

        }

        buttonCancel.setOnClickListener(v -> finish());

        buttonDelete.setOnClickListener(v -> {
            db.collection("pets").document(pet.getId()).delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Pet deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        });

        buttonSave.setOnClickListener(v -> {
            pet.setSpecies(inputType.getSelectedItem().toString());
            pet.setGender(inputGender.getSelectedItem().toString());

            pet.setName(inputName.getText().toString());
            pet.setBirthday(inputBirthday.getText().toString());
            pet.setAdditionalInfo(inputInfo.getText().toString());

            db.collection("pets").document(pet.getId()).set(pet)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        });
    }
}
