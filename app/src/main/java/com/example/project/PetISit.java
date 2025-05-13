package com.example.project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class PetISit extends AppCompatActivity {

    RecyclerView petsRecyclerView;
    FirebaseDatabaseManager db;
    PetsAdapter petsAdapter;
    ArrayList<Map<String, Object>> petsArray = new ArrayList<>();

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_i_sit);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        db = new FirebaseDatabaseManager();
        petsRecyclerView = findViewById(R.id.pets);
        petsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        auth = FirebaseAuth.getInstance();

        String userId = auth.getCurrentUser().getUid();

        db.getApprovedCaregivingTicketsByUser(userId).addOnSuccessListener(documents -> {
            if (documents != null && !documents.isEmpty()) {
                for (DocumentSnapshot document : documents) {
                    Map<String, Object> petData = document.getData();
                    if (petData != null) {
                        petsArray.add(petData);
                    }
                }
                petsAdapter = new PetsAdapter(petsArray);
                petsRecyclerView.setAdapter(petsAdapter);
            } else {
                Toast.makeText(PetISit.this, "No approved caregiving applications found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(PetISit.this, "Error fetching data.", Toast.LENGTH_SHORT).show();
        });
    }

    class PetsAdapter extends RecyclerView.Adapter<PetsAdapter.PetsViewHolder> {

        ArrayList<Map<String, Object>> petsArray;

        public PetsAdapter(ArrayList<Map<String, Object>> petsArray) {
            this.petsArray = petsArray;
        }

        @NonNull
        @Override
        public PetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_caregiving_ticket, parent, false);
            return new PetsViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull PetsViewHolder holder, int position) {
            Map<String, Object> petData = petsArray.get(position);

            String petName = (String) petData.get("petName");
            String petType = (String) petData.get("petGender");

            String petImageUrl = (String) petData.get("petImageUrl");

            String endingDate = (String) petData.get("endingDate");
            String endingTimeHour = (String) petData.get("endingTimeHour");
            String endingTimeMinute = (String) petData.get("endingTimeMinute");

            String startingDate = (String) petData.get("startingDate");
            String startingTimeHour = (String) petData.get("startingTimeHour");
            String startingTimeMinute = (String) petData.get("startingTimeMinute");

            holder.petName.setText(petName != null ? petName : "Unknown Pet Name");
            holder.sex.setText(petType != null ? petType : "Unknown Pet Gender");

            holder.ed.setText(endingDate + " " + endingTimeHour + ":" + endingTimeMinute);
            holder.sd.setText(startingDate + " " + startingTimeHour + ":" + startingTimeMinute);

            Glide.with(getApplicationContext())
                    .load(petImageUrl)
                    .placeholder(R.drawable.circle_shape)
                    .circleCrop()
                    .into(holder.petImage);
        }

        @Override
        public int getItemCount() {
            return petsArray.size();
        }

        class PetsViewHolder extends RecyclerView.ViewHolder {

            TextView petName, sex, sd, ed, city;
            ImageView petImage;

            public PetsViewHolder(@NonNull View itemView) {
                super(itemView);
                petName = itemView.findViewById(R.id.name);
                sex = itemView.findViewById(R.id.sex);
                sd = itemView.findViewById(R.id.sd);
                ed = itemView.findViewById(R.id.ed);
                city = itemView.findViewById(R.id.city);

                city.setVisibility(View.GONE);

                petImage = itemView.findViewById(R.id.pet_image);
            }
        }
    }
}
