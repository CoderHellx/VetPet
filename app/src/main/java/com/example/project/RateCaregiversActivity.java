package com.example.project;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RateCaregiversActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private Button submitButton;

    private CaregivingTicket caregivingTicket; // Assume passed from intent or adapter
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_caregiver); // your layout file

        ratingBar = findViewById(R.id.ratingBar);
        submitButton = findViewById(R.id.submitButton);

        ImageButton buttonBack = findViewById(R.id.backFromRating);
        buttonBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Retrieve caregivingTicket from intent (you must pass it from adapter)
        caregivingTicket = (CaregivingTicket) getIntent().getSerializableExtra("ticket");

        submitButton.setOnClickListener(v -> {
            if (!caregivingTicket.hasCaregivingEnded(RateCaregiversActivity.this)) {
                Toast.makeText(this, "You cannot rate before the caregiving period ends.", Toast.LENGTH_SHORT).show();
                return;
            }
            submitRating();
        });
    }

    private void submitRating() {
        int ratingValue = (int) ratingBar.getRating();

        Rating rating = new Rating(
                currentUserId,
                caregivingTicket.getTicketId(),
                caregivingTicket.getPetId(),
                ratingValue
        );

        String caregiverUserId = caregivingTicket.getCaregivingUserId();

        // Save rating under caregiver's ratings subcollection
        db.collection("users")
                .document(caregiverUserId)
                .collection("ratings")
                .document(caregivingTicket.getTicketId())
                .set(rating)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Rating submitted!", Toast.LENGTH_SHORT).show();

                    // Mark ticket as rated
                    db.collection("caregivingTickets")
                            .document(caregivingTicket.getTicketId())
                            .update("isRated", true);

                    // Update caregiver's average rating
                    updateCaregiverAverage(caregiverUserId, ratingValue);
                })
                .addOnFailureListener(e -> {
                    Log.e("Rating", "Failed to submit rating", e);
                    Toast.makeText(this, "Failed to submit", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCaregiverAverage(String caregiverUserId, int newRating) {
        DocumentReference caregiverRef = db.collection("users").document(caregiverUserId);

        caregiverRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Double currentAverage = snapshot.getDouble("averageRating");
                Long totalRatings = snapshot.getLong("totalRatings");

                if (currentAverage == null) currentAverage = 0.0;
                if (totalRatings == null) totalRatings = 0L;

                double newAverage = ((currentAverage * totalRatings) + newRating) / (totalRatings + 1);

                caregiverRef.update("averageRating", newAverage, "totalRatings", totalRatings + 1);
            }
        });
    }


}

