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
    public static User currentUser;
    private FirebaseAuth mAuth;
    private EditText emailET, passET;
    private ProgressBar progressBar;
    private TextView forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        findViewById(R.id.back).setOnClickListener(view -> finish());

        emailET = findViewById(R.id.emailEditText);
        passET  = findViewById(R.id.passwordEditText);
        Button signBtn = findViewById(R.id.signInButton);
        progressBar = findViewById(R.id.progressBar);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        mAuth = FirebaseAuth.getInstance();

        forgotPasswordText.setOnClickListener(v -> showResetPasswordDialog());

        signBtn.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = emailET.getText().toString().trim();
        String pass  = passET.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailET.setError("Email can not be empty");
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            passET.setError("Password can not be empty");
            return;
        }
        if (pass.length() < 6) {
            passET.setError("Password needs to be at least 6 characters long");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) {
                            Toast.makeText(this, "Unexpected error: User is null", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String uid = firebaseUser.getUid();

                        FirebaseDatabaseManager dbManager = new FirebaseDatabaseManager();
                        dbManager.fetchUserByUid(uid).addOnSuccessListener(user -> {
                            currentUser = user;

                            Toast.makeText(this, "Welcome: " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(SignInActivity.this, HomepageActivity.class);
                            intent.putExtra("currentUserName", user.getName()); // Optional
                            startActivity(intent);
                            finish();

                        }).addOnFailureListener(e -> {
                            Toast.makeText(SignInActivity.this, "User data not found: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

                    } else {
                        Toast.makeText(this,
                                "Sign in failed: " +
                                        task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
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
                Toast.makeText(this, "Email can not be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Email has been sent to you", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Email could not be sent: " +
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}
