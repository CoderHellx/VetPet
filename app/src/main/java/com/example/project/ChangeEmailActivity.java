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

public class ChangeEmailActivity extends AppCompatActivity {

    EditText currentemailedittext,newemailedittext,confirmNewemailedittext;
    Button button;
    DocumentReference currentUser;
    ImageButton returnbutton;
    String currentuserId, email ;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        currentemailedittext = findViewById(R.id.currentemailedittext);
        newemailedittext = findViewById(R.id.newemailedittext);
        confirmNewemailedittext = findViewById(R.id.confirmnewemailedittext);
        button = findViewById(R.id.changeemailButton);
        returnbutton = findViewById(R.id.back);

        currentuserId = getIntent().getStringExtra("id");
        currentUser = FirebaseFirestore.getInstance().collection("users").document(
                currentuserId);

        returnbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        currentUser.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        email = documentSnapshot.getString("email");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "User data fetch failed: " + e.getMessage());
                });

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (email.equals(currentemailedittext.getText().toString().trim())){
                    if (newemailedittext.getText().toString().trim().equals(confirmNewemailedittext.getText().toString().trim())){
                        if(newemailedittext.getText().toString().trim().length()==0){
                            Toast.makeText(getApplicationContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            currentUser.update("email",newemailedittext.getText().toString().trim());
                            Toast.makeText(getApplicationContext(), "Your email has been updated", Toast.LENGTH_SHORT).show();
                            finish();
                            verifyemail();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "New email and confirmation do not match.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Current email is incorrect.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verifyemail() {
        FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, verifyTask -> {
                    if (verifyTask.isSuccessful()) {
                        Toast.makeText(this,
                                "Verification email has been sent: " + user.getEmail(),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Verification email could not be sent: " +
                                        verifyTask.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

}