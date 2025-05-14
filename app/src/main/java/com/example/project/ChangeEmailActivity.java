package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChangeEmailActivity extends AppCompatActivity {

    private static final String TAG = "ChangeEmailActivity";
    private EditText currentEmailEditText, passwordEditText, newEmailEditText, confirmNewEmailEditText;
    private Button changeEmailButton;
    private ImageButton returnButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        currentEmailEditText = findViewById(R.id.currentemailedittext);
        passwordEditText = findViewById(R.id.passwordedittext);
        newEmailEditText = findViewById(R.id.newemailedittext);
        confirmNewEmailEditText = findViewById(R.id.confirmnewemailedittext);
        changeEmailButton = findViewById(R.id.changeemailButton);
        returnButton = findViewById(R.id.back);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        user = mAuth.getCurrentUser();

        returnButton.setOnClickListener(v -> finish());
        changeEmailButton.setOnClickListener(v -> validateAndSendVerification());
    }

    private void validateAndSendVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentEmail = currentEmailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String newEmail = newEmailEditText.getText().toString().trim();
        String confirmNewEmail = confirmNewEmailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newEmail)) {
            newEmailEditText.setError("New email is required");
            newEmailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            newEmailEditText.setError("Please enter a valid email address");
            newEmailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmNewEmail)) {
            confirmNewEmailEditText.setError("Please confirm your new email");
            confirmNewEmailEditText.requestFocus();
            return;
        }

        if (!newEmail.equals(confirmNewEmail)) {
            confirmNewEmailEditText.setError("New email and confirmation do not match");
            confirmNewEmailEditText.requestFocus();
            return;
        }

        if (newEmail.equals(currentEmail)) {
            newEmailEditText.setError("New email must be different from current email");
            newEmailEditText.requestFocus();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {

                    savePendingEmailUpdate(user.getUid(), newEmail);

                    sendVerificationEmail(user, newEmail);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Authentication failed", e);
                    Toast.makeText(ChangeEmailActivity.this,
                            "Authentication failed. Please check your password.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void savePendingEmailUpdate(String userId, String newEmail) {
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("pendingEmail", newEmail);
        updates.put("verificationTimestamp", System.currentTimeMillis());

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pending email information saved to Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save pending email status", e);
                    Toast.makeText(ChangeEmailActivity.this,
                            "Failed to save pending email status: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void sendVerificationEmail(FirebaseUser user, String newEmail) {
        Log.d(TAG, "Sending verification email to: " + newEmail);

        user.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Verification email sent successfully to " + newEmail);

                    showVerificationSentDialog(newEmail);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send verification email", e);

                    showVerificationFailedDialog(user, newEmail, e.getMessage());
                });
    }

    private void showVerificationSentDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Verification Email Sent")
                .setMessage("A verification email has been sent to " + email +
                        ". Please check your inbox (including spam folder) and follow the link to verify your new email address." +
                        "\n\nAfter verification, you can sign in with your new email.")
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(ChangeEmailActivity.this, MainActivity.class);
                    mAuth.signOut();
                    startActivity(intent);
                })
                .setCancelable(false)
                .show();
    }

    private void showVerificationFailedDialog(FirebaseUser user, String email, String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle("Verification Failed")
                .setMessage("Failed to send verification email: " + errorMessage)
                .setPositiveButton("Retry", (dialog, which) -> {
                    sendVerificationEmail(user, email);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    cancelPendingEmailUpdate(user.getUid());
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void cancelPendingEmailUpdate(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("pendingEmail", null);
        updates.put("verificationTimestamp", null);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pending email update canceled");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to cancel pending email update", e);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPendingEmailVerification();
    }

    private void checkPendingEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        DocumentReference userRef = db.collection("users").document(user.getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String pendingEmail = documentSnapshot.getString("pendingEmail");

                if (pendingEmail != null) {
                    if (pendingEmail.equals(user.getEmail())) {
                        completeEmailUpdate(user.getUid(), pendingEmail);
                        Toast.makeText(this, "Email successfully updated to: " + pendingEmail,
                                Toast.LENGTH_LONG).show();
                    } else {
                        long timestamp = documentSnapshot.getLong("verificationTimestamp");
                        if (System.currentTimeMillis() - timestamp > 24 * 60 * 60 * 1000) {
                            cancelPendingEmailUpdate(user.getUid());
                        }
                    }
                }
            }
        });
    }

    private void completeEmailUpdate(String userId, String newEmail) {
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("pendingEmail", null);
        updates.put("verificationTimestamp", null);
        updates.put("email", newEmail);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Email update completed in Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to complete email update in Firestore", e);
                });
    }
}