package com.example.vetpet;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class CaregivingDetails extends AppCompatActivity {

    TextView petName, petType, petAge, petSex, petDetails, username;

    ImageView userImg, petImg;

    String userId, userImgUrl;

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

        petName = findViewById(R.id.pet_name);
        petType = findViewById(R.id.pet_type);
        petAge = findViewById(R.id.pet_age);
        petSex = findViewById(R.id.pet_gender);
        petDetails = findViewById(R.id.pet_details);
        username = findViewById(R.id.username);

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