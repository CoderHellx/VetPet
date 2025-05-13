package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CreateCaregivingTicket extends AppCompatActivity {

    EditText eHour, eMinute, eDate, sHour, sMinute, sDate, additionalInfo;
    private LinearLayout llPets;
    private FirebaseDatabaseManager dbManager;
    private FirebaseAuth auth;
    private String selectedPetId, selectedSpecies, selectedPetName, selectedPetGender;
    private Pet selectedPet;

    Button postRequest;

    FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.post_caregiver_ticket);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        dbManager = new FirebaseDatabaseManager();
        auth = FirebaseAuth.getInstance();

        llPets = findViewById(R.id.llPets);
        postRequest = findViewById(R.id.postRequest);
        additionalInfo = findViewById(R.id.AdditionalInfo);
        eHour = findViewById(R.id.eHour);
        eMinute = findViewById(R.id.eMin);
        eDate = findViewById(R.id.eDate);
        sHour = findViewById(R.id.sHour);
        sMinute = findViewById(R.id.sMin);
        sDate = findViewById(R.id.sDate);

        fetchPets(auth.getCurrentUser().getUid());


        postRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CaregivingTicket ticket = new CaregivingTicket();

                if (selectedPetId == null) {
                    Toast.makeText(view.getContext(), "Please select a pet", Toast.LENGTH_SHORT).show();
                    return;
                }

                String details = additionalInfo.getText().toString().trim();
                if (details.isEmpty()) {
                    details = "";
                }

                ticket.setPet(selectedPet);
                ticket.setOwnerId(auth.getCurrentUser().getUid()); //User.getId();
                ticket.setSpecie(selectedSpecies);
                ticket.setPetId(selectedPetId);
                ticket.setDetails(additionalInfo.getText().toString());
                ticket.setCity("Istanbul");
                ticket.setEndingDate(eDate.getText().toString(), eHour.getText().toString(), eMinute.getText().toString());
                ticket.setStartingDate(sDate.getText().toString(), sHour.getText().toString(), sMinute.getText().toString());

                dbManager.saveCaregivingTicket(ticket);

                Toast.makeText(CreateCaregivingTicket.this, "Success", Toast.LENGTH_SHORT).show();

                finish();
            }
        });
    }


    private void fetchPets(String ownerId) {
        db.collection("pets")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Pet> petList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Pet pet = doc.toObject(Pet.class);
                        if (pet != null) {
                            petList.add(pet);
                        }
                    }
                    displayPets(petList);
                })
                .addOnFailureListener(e -> {
                });
    }

    private void displayPets(List<Pet> pets) {
        for (int i = 0; i < pets.size(); i++) {
            View petView = getLayoutInflater().inflate(R.layout.item_pet, llPets, false);
            ImageView imgPet = petView.findViewById(R.id.imgPet);
            View border = petView.findViewById(R.id.viewBorder);

            Pet pet = pets.get(i);

            Glide.with(this)
                    .load(pet.getImageUrl())
                    .placeholder(R.drawable.circle_shape)
                    .circleCrop()
                    .into(imgPet);

            petView.setOnClickListener(v -> {
                for (int j = 0; j < llPets.getChildCount(); j++) {
                    View child = llPets.getChildAt(j);
                    View childBorder = child.findViewById(R.id.viewBorder);
                    if (childBorder != null) {
                        childBorder.setVisibility(View.GONE);
                    }
                }

                border.setVisibility(View.VISIBLE);

                selectedPetId = pet.getId();
                selectedSpecies = pet.getSpecies();
                selectedPetName = pet.getName();
                selectedPetGender = pet.getGender();
                selectedPet = pet;
            });

            llPets.addView(petView);
        }
    }
}
