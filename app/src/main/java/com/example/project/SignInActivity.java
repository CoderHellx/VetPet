package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.text.InputType;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailET, passET;
    private ProgressBar progressBar;
    private TextView forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Back button
        findViewById(R.id.back).setOnClickListener(view -> finish());

        // Initialize views
        emailET = findViewById(R.id.emailEditText);
        passET = findViewById(R.id.passwordEditText);
        Button signBtn = findViewById(R.id.signInButton);
        progressBar = findViewById(R.id.progressBar);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        mAuth = FirebaseAuth.getInstance();

        // Set click listeners
        forgotPasswordText.setOnClickListener(v -> showResetPasswordDialog());
        signBtn.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = emailET.getText().toString().trim();
        String pass = passET.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(email)) {
            emailET.setError("Email cannot be empty");
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            passET.setError("Password cannot be empty");
            return;
        }
        if (pass.length() < 6) {
            passET.setError("Password needs to be at least 6 characters long");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Firebase authentication
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            fetchUserData(firebaseUser.getUid());
                        }
                    } else {
                        Toast.makeText(SignInActivity.this,
                                "Sign in failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fetchUserData(String uid) {
        FirebaseDatabaseManager dbManager = new FirebaseDatabaseManager();
        dbManager.fetchUserByUid(uid)
                .addOnSuccessListener(user -> {
                    Utils.currentUser = user;
                    FirebaseUser firebaseuser = mAuth.getCurrentUser();
                    Toast.makeText(SignInActivity.this,
                            "Welcome: " + firebaseuser.getEmail(),
                            Toast.LENGTH_SHORT).show();

                    // Start HomepageActivity only once
                    Intent intent = new Intent(SignInActivity.this, HomepageActivity.class);
                    intent.putExtra("currentUserName", user.getName());
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignInActivity.this,
                            "User data not found: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText input = new EditText(this);
        input.setHint("Enter your email");
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Password reset email has been sent",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this,
                                    "Failed to send reset email: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}