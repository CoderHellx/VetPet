package com.example.project;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

        FirebaseUser user = mAuth.getCurrentUser();

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
                    sendVerificationAndUpdateEmail(user, newEmail);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Authentication failed", e);
                    Toast.makeText(ChangeEmailActivity.this,
                            "Authentication failed. Please check your password.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void sendVerificationAndUpdateEmail(FirebaseUser user, String newEmail) {
        Log.d(TAG, "Attempting to send verification email to: " + newEmail);

        String verificationCode = generateVerificationCode();

        user.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firebase verification email sent successfully");

                    savePendingEmailUpdate(user.getUid(), newEmail, verificationCode, true);

                    Toast.makeText(ChangeEmailActivity.this,
                            "Verification email sent to " + newEmail + ". Please check your inbox (including spam folder) and follow the link to verify.",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase verification email failed", e);

                    // If Firebase verification fails, try custom verification logic
                    handleCustomVerification(user, newEmail, verificationCode);
                });
    }

    private void handleCustomVerification(FirebaseUser user, String newEmail, String verificationCode) {
        savePendingEmailUpdate(user.getUid(), newEmail, verificationCode, false);

        Toast.makeText(ChangeEmailActivity.this,
                "Firebase verification failed. Using backup method. Please check your app for verification instructions.",
                Toast.LENGTH_LONG).show();
        Log.d(TAG, "Custom verification code for " + newEmail + ": " + verificationCode);
        Toast.makeText(ChangeEmailActivity.this,
                "DEBUG: Verification code: " + verificationCode,
                Toast.LENGTH_LONG).show();
    }

    private String generateVerificationCode() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }

    private void savePendingEmailUpdate(String userId, String newEmail, String verificationCode, boolean usingFirebaseMethod) {
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("pendingEmail", newEmail);
        updates.put("verificationCode", verificationCode);
        updates.put("verificationTimestamp", System.currentTimeMillis());
        updates.put("usingFirebaseVerification", usingFirebaseMethod);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pending email information saved to Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save pending email status", e);
                    Toast.makeText(ChangeEmailActivity.this,
                            "Note: Failed to save pending email status: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    public static void syncFirestoreEmailIfChanged(@NonNull FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();



        DocumentReference userRef = db.collection("users").document(uid);
        Map<String, Object> update = new HashMap<>();
        update.put("email", user.getEmail());
        update.put("pendingEmail", null);
        update.put("verificationTimestamp", null);

        userRef.update(update)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Firestore email field synced with Firebase Auth"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update Firestore email field", e));
    }
}