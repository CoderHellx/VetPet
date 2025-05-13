package com.example.project;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText currentPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private Button changePasswordButton;
    private ImageButton returnButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        changePasswordButton = findViewById(R.id.changepasswordButton);
        currentPasswordEditText = findViewById(R.id.currentpasswordedittext);
        newPasswordEditText = findViewById(R.id.newpasswordedittext);
        confirmNewPasswordEditText = findViewById(R.id.confirmnewpasswordedittext);
        returnButton = findViewById(R.id.back);

        mAuth = FirebaseAuth.getInstance();
        returnButton.setOnClickListener(v -> finish());

        changePasswordButton.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword)) {
            currentPasswordEditText.setError("Please enter your current password");
            currentPasswordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError("Please enter a new password");
            newPasswordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmNewPassword)) {
            confirmNewPasswordEditText.setError("Please confirm your new password");
            confirmNewPasswordEditText.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            newPasswordEditText.setError("Password needs to be at least 6 characters long");
            newPasswordEditText.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            confirmNewPasswordEditText.setError("New password and confirmation do not match");
            confirmNewPasswordEditText.requestFocus();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(ChangePasswordActivity.this,
                                        "Your password has been updated", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ChangePasswordActivity.this,
                                        "Failed to update password: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Current password is incorrect",
                            Toast.LENGTH_SHORT).show();
                });
    }
}