package com.example.project;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RatingListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CaregiverRatingAdapter adapter;
    private List<CaregivingTicket> ticketsToRate = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_list);

        ImageButton buttonBack = findViewById(R.id.backFromRating);
        buttonBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewTickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadTickets();
    }

    private void loadTickets() {
        db.collection("caregiving_tickets")
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Boolean isRated = doc.getBoolean("isRated");
                        if (isRated != null && isRated) {
                            continue;
                        }

                        CaregivingTicket ticket = new CaregivingTicket();
                        ticket.setTicketId(doc.getString("ticketId"));

                        Pet pet = new Pet();
                        pet.setName(doc.getString("petName"));
                        ticket.setPet(pet);

                        ticket.setEndingDate(
                                doc.getString("endingDate"),
                                doc.getString("endingTimeHour"),
                                doc.getString("endingTimeMinute")
                        );

                        ticket.setStartingDate(
                                doc.getString("startingDate"),
                                doc.getString("startingTimeHour"),
                                doc.getString("startingTimeMinute")
                        );

                        ticket.setRated(isRated != null && isRated);

                        db.collection("applications")
                                .whereEqualTo("ticketId", ticket.getTicketId())
                                .whereEqualTo("type", "caregiving")
                                .get()
                                .addOnSuccessListener((QuerySnapshot apps) -> {
                                    for (QueryDocumentSnapshot appDoc : apps) {
                                        ticket.setCaregivingUserId(appDoc.getString("applicantId"));
                                        ticketsToRate.add(ticket);
                                    }

                                    if (adapter == null) {
                                        adapter = new CaregiverRatingAdapter(RatingListActivity.this, ticketsToRate);
                                        recyclerView.setAdapter(adapter);
                                    } else {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load tickets", Toast.LENGTH_SHORT).show();
                });
    }

}
