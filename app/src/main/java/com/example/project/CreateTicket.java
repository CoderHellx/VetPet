package com.example.project;
import com.bumptech.glide.Glide;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CreateTicket extends AppCompatActivity {
    private LinearLayout llPets;
    private EditText etDetails;
    private Button btnCreate;
    private FirebaseDatabaseManager dbManager;
    private FirebaseAuth auth;
    private String selectedPetId, selectedSpecies, selectedPetName, selectedPetGender, selectedCity;
    private Pet selectedPet;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_adoption_ticket);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        llPets = findViewById(R.id.llPets);
        etDetails = findViewById(R.id.additionalInfo);
        btnCreate = findViewById(R.id.createTicket);

        dbManager = new FirebaseDatabaseManager();
        auth = FirebaseAuth.getInstance();

        fetchPets(auth.getCurrentUser().getUid());

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTicket();
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

    private void createTicket() {
        if (selectedPetId == null) {
            Toast.makeText(this, "Please select a pet", Toast.LENGTH_SHORT).show();
            return;
        }

        final String detailsInput = etDetails.getText().toString().trim();
        final String details = detailsInput.isEmpty() ? "" : detailsInput;

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        dbManager.fetchUserByUid(firebaseUser.getUid())
                .addOnSuccessListener(user -> {
                    String userCity = user.getCity(); // ✅ fetched city

                    Ticket ticket = new Ticket(
                            user.getUserId(),
                            selectedPetId,
                            selectedSpecies,
                            details,
                            userCity
                    );

                    ticket.setPet(selectedPet);

                    dbManager.saveTicket(ticket)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Ticket created!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}