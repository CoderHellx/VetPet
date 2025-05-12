package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputStreamReader is = null;
        try {
            is = new InputStreamReader(getAssets().open("world_cities.csv"));
        } catch (IOException e) {
            //throw new RuntimeException(e);
            System.out.println("File not found!!!");
        }

        Button signInBtn = findViewById(R.id.signInBtn);
        Button signUpBtn = findViewById(R.id.signUpBtn);

        signInBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        signUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
