package com.example.project;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.CaregiverRatingAdapter;
import com.example.project.CaregivingTicket;
import com.example.project.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

        Button buttonBack = findViewById(R.id.back);
        buttonBack.setOnClickListener(v -> finish()); // this goes back to the previous activity


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
                        CaregivingTicket ticket = new CaregivingTicket();

                        ticket.setTicketId(doc.getString("ticketId"));

                        Pet pet = new Pet();
                        pet.setName(doc.getString("petName"));
                        ticket.setPet(pet);

                        db.collection("applications").whereEqualTo("ticketId",ticket.getTicketId()).whereEqualTo("type","caregiving").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    ticket.setCaregivingUserId(doc.getString("applicantId"));

                                    ticketsToRate.add(ticket);
                                }
                                adapter = new CaregiverRatingAdapter(RatingListActivity.this, ticketsToRate);
                                recyclerView.setAdapter(adapter);
                            }
                        });
                    }
                });
    }

    private boolean hasCaregivingEnded(CaregivingTicket ticket) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date endDate = sdf.parse(ticket.getEndingDate() + " " + ticket.getEndingTimeHour() + ":" + ticket.getEndingTimeMinute());
            return new Date().after(endDate);
        } catch (ParseException e) {
            return false;
        }
    }
}
