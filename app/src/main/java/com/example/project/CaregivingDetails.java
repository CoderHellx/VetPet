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

public class CaregivingDetails extends AppCompatActivity {

    TextView petName, petType, petAge, petSex, petDetails, username, email;

    ImageView userImg, petImg;

    String userId, userImgUrl, ticketId;

    Button apply;

    FirebaseDatabaseManager db;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiving_details);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        db = new FirebaseDatabaseManager();

        email = findViewById(R.id.email);
        petName = findViewById(R.id.pet_name);
        petType = findViewById(R.id.pet_type);
        petAge = findViewById(R.id.pet_age);
        petSex = findViewById(R.id.pet_gender);
        petDetails = findViewById(R.id.pet_details);
        username = findViewById(R.id.username);

        auth = FirebaseAuth.getInstance();

        apply = findViewById(R.id.apply);

        petImg = findViewById(R.id.pet_image);
        userImg = findViewById(R.id.user_image);

        userId = getIntent().getStringExtra("ownerId");
        userImgUrl = getIntent().getStringExtra("user_img");

        petName.setText(getIntent().getStringExtra("name"));
        petType.setText(getIntent().getStringExtra("type"));
        petAge.setText(getIntent().getStringExtra("age"));
        petSex.setText(getIntent().getStringExtra("sex"));
        petDetails.setText(getIntent().getStringExtra("details"));
        username.setText(getIntent().getStringExtra("username"));
        email.setText(getIntent().getStringExtra("email"));

        ticketId = getIntent().getStringExtra("ticketId");//Not sure

        String applicantId = auth.getCurrentUser().getUid();

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.makeApply(ticketId, applicantId, userId, "caregiving"); //I am not sure about the type

                Toast.makeText(CaregivingDetails.this, "Your application has been forwarded.", Toast.LENGTH_SHORT).show();

                finish();
            }
        });
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