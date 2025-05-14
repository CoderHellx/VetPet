package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    FirebaseUser user;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();

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
