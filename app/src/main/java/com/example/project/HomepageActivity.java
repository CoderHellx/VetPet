package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomepageActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout llPets;
    private Pet selectedPet;
    private String selectedPetId;
    private static final int ADD_PET_REQUEST = 1002;

    private void displayPets(List<Pet> pets) {

        for (int i = 0; i < pets.size(); i++) {
            View petView = getLayoutInflater().inflate(R.layout.item_pet, llPets, false);
            ImageView imgPet = petView.findViewById(R.id.imgPet);


            Pet pet = pets.get(i);

            Glide.with(this)
                    .load(pet.getImageUrl())
                    .placeholder(R.drawable.circle_shape)
                    .circleCrop()
                    .into(imgPet);

            petView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(), PetDetailActivity.class);
                    // i.setClass(getApplicationContext(), className.class);
                    // startActivity(i);
                    i.putExtra("pet", pet);
                    startActivity(i);
                }
            });

            llPets.addView(petView);
        }
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        llPets = findViewById(R.id.llPets);
        db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchPets(currentUserId);

        findViewById(R.id.adoption).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), Adoption.class);
                startActivity(i);
            }
        });

        findViewById(R.id.caregiving).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), Caregiving.class);
                startActivity(i);
            }
        });

        findViewById(R.id.petsIPetSit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), PetISit.class);
                startActivity(i);
            }
        });

        findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), UserProfileActivity.class);
                startActivity(i);
            }
        });


        findViewById(R.id.AddingPetButton).setOnClickListener(view -> {
            Intent intent = new Intent(HomepageActivity.this, AddPetActivity.class);
            startActivityForResult (intent, ADD_PET_REQUEST);
        });

        findViewById(R.id.RankCaregivers).setOnClickListener(view -> {
            Intent intent = new Intent(HomepageActivity.this, RatingListActivity.class);
            startActivity(intent);
        });


        findViewById(R.id.notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), Notify.class);
                startActivity(i);
            }
        });

        Utils.getCurrentUser(new Utils.CurrentUserCallback() {
            @Override
            public void onUserReady(User user) {
                user.getPets(new User.PetsCallback() {
                    @Override
                    public void onPetsFetched(ArrayList<Pet> pets) {
                        if(!pets.isEmpty()) {
                            displayPets(pets);
                        }
                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PET_REQUEST && resultCode == RESULT_OK) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            fetchPets(currentUserId);
        }
    }
}
