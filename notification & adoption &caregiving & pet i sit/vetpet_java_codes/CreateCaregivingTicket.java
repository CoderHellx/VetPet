package com.example.vetpet;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.post_caregiver_ticket);

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

        setupPetSelection();

        postRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CaregivingTicket ticket = new CaregivingTicket();

                ticket.setPet(selectedPet);
                ticket.setOwnerId("123");
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

    private void setupPetSelection() {
        FirebaseUser user = auth.getCurrentUser();
        // if (user == null) return;

        String uuid = "123"; // user.getUuid();

        // Test petleri
        List<Pet> testPets = new ArrayList<>();
        testPets.add(new Pet("pet1", uuid, "Köpük", "Dog",
                "https://cdn.pixabay.com/photo/2016/12/13/05/15/puppy-1903313_640.jpg", "Male", 1));
        testPets.add(new Pet("pet2", uuid, "Lucky", "Cat",
                "https://cdn.pixabay.com/photo/2017/02/20/18/03/cat-2083492_640.jpg", "Female", 2));
        testPets.add(new Pet("pet3", uuid, "Tarçın", "Rabbit",
                "https://cdn.pixabay.com/photo/2017/04/02/22/36/easter-2197043_640.jpg", "Male", 3));

        displayPets(testPets);
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
