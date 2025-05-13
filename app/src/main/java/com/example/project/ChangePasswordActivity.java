package com.example.project;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText currentPasswordedittext,newPasswordedittext,confirmNewPasswordedittext;
    Button button;
    DocumentReference currentUser;
    ImageButton returnbutton;
    String currentuserId, password;
    FirebaseAuth mAuth;
    DocumentReference currentuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        button = findViewById(R.id.changepasswordButton);
        currentPasswordedittext = findViewById(R.id.currentpasswordedittext);
        newPasswordedittext = findViewById(R.id.newpasswordedittext);
        confirmNewPasswordedittext = findViewById(R.id.confirmnewpasswordedittext);
        returnbutton = findViewById(R.id.back);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        currentuserId = user.getUid();
        currentuser = FirebaseFirestore.getInstance().collection("users").document(
                currentuserId);

        currentUser.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        password = documentSnapshot.getString("password");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "User data fetch failed: " + e.getMessage());
                });

        returnbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (password.equals(currentPasswordedittext.getText().toString().trim())){
                    if (newPasswordedittext.getText().toString().trim().equals(confirmNewPasswordedittext.getText().toString().trim())){
                        if(newPasswordedittext.getText().toString().trim().length()<6){
                            Toast.makeText(getApplicationContext(), "Password needs to be at least 6 characters long", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            currentUser.update("password",newPasswordedittext.getText().toString().trim());
                            Toast.makeText(getApplicationContext(), "Your password has been updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "New password and confirmation do not match.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Current password is incorrect.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}