package com.example.vetpet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ViewCaregivingTickets extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_caregiving_tickets);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.btnCat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CaregivingTickets.class);
                intent.putExtra("type", "cat");
                startActivity(intent);
            }
        });

        findViewById(R.id.btnDog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CaregivingTickets.class);
                intent.putExtra("type", "dog");
                startActivity(intent);
            }
        });

        findViewById(R.id.btnBird).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CaregivingTickets.class);
                intent.putExtra("type", "bird");
                startActivity(intent);
            }
        });

        findViewById(R.id.btnRabbit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CaregivingTickets.class);
                intent.putExtra("type", "rabbit");
                startActivity(intent);
            }
        });

        findViewById(R.id.btnHamster).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CaregivingTickets.class);
                intent.putExtra("type", "hamster");
                startActivity(intent);
            }
        });

        findViewById(R.id.btnFish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CaregivingTickets.class);
                intent.putExtra("type", "fish");
                startActivity(intent);
            }
        });

        findViewById(R.id.btnTurtle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CaregivingTickets.class);
                intent.putExtra("type", "turtle");
                startActivity(intent);
            }
        });

        findViewById(R.id.btnOther).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CaregivingTickets.class);
                intent.putExtra("type", "other");
                startActivity(intent);
            }
        });
    }
}