package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

public class AdoptionDetails extends AppCompatActivity {

    TextView petName, petType, petAge, petSex, petDetails, username, email;

    ImageView userImg, petImg;

    String userId, userImgUrl, ticketId;
    Button apply;

    FirebaseDatabaseManager db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adoption_details);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        db = new FirebaseDatabaseManager();

        petName = findViewById(R.id.pet_name);
        petType = findViewById(R.id.pet_type);
        petAge = findViewById(R.id.pet_age);
        petSex = findViewById(R.id.pet_gender);
        petDetails = findViewById(R.id.pet_details);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);

        apply = findViewById(R.id.apply);

        petImg = findViewById(R.id.pet_image);
        userImg = findViewById(R.id.user_image);

        userId = getIntent().getStringExtra("ownerId");
        userImgUrl = getIntent().getStringExtra("user_img");

        ticketId = getIntent().getStringExtra("ticketId");

        String applicantId = "123";//Will probably change //User.getId();

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.makeApply(ticketId, applicantId, userId, "adoption");

                Toast.makeText(AdoptionDetails.this, "Your application has been forwarded.", Toast.LENGTH_SHORT).show();

                finish();
            }
        });

        petName.setText(getIntent().getStringExtra("name"));
        petType.setText(getIntent().getStringExtra("type"));
        petAge.setText(getIntent().getStringExtra("age"));
        petSex.setText(getIntent().getStringExtra("sex"));
        petDetails.setText(getIntent().getStringExtra("details"));
        username.setText(getIntent().getStringExtra("username"));
        email.setText(getIntent().getStringExtra("email"));

        Glide.with(this)
                .load(getIntent().getStringExtra("pet_img"))
                .placeholder(R.drawable.circle_shape)
                .circleCrop()
                .into(petImg);

        Glide.with(this)
                .load(getIntent().getStringExtra("user_img"))
                .placeholder(R.drawable.circle_shape)
                .circleCrop()
                .into(userImg);
    }
}