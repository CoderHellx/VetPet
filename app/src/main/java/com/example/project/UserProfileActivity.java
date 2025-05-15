package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import okhttp3.*;

public class UserProfileActivity extends AppCompatActivity {

    private EditText nameEditText, surnameEditText, emailEditText, passwordEditText;
    private Button saveChangesBtn, logoutBtn, deleteProfileBtn;
    private Spinner countrySpinner, citySpinner;
    private ImageButton changePasswordBtn, changeEmailBtn, returnBtn;
    private TextView nameSurnameText, rankText;
    private String currentCountry;
    private String currentCity;
    private FirebaseAuth mAuth;
    private DocumentReference currentUserDoc;
    private String currentUserId;
    private List<String> countryList = new ArrayList<>();
    FirebaseUser user;
    private Map<String, List<String>> citiesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initializeViews();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {
            currentUserId = user.getUid();
            currentUserDoc = FirebaseFirestore.getInstance().collection("users").document(currentUserId);
            loadUserProfile();
        }

        setListeners();
        loadCountriesFromApi();
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.nameedittext);
        surnameEditText = findViewById(R.id.surnameedittext);
        emailEditText = findViewById(R.id.emailedittext);
        passwordEditText = findViewById(R.id.passwordedittext);
        saveChangesBtn = findViewById(R.id.saveChangesButton);
        logoutBtn = findViewById(R.id.logoutButton);
        deleteProfileBtn = findViewById(R.id.deleteProfileButton);
        changeEmailBtn = findViewById(R.id.changeemailbutton);
        changePasswordBtn = findViewById(R.id.changepasswordbutton);
        countrySpinner = findViewById(R.id.countries);
        citySpinner = findViewById(R.id.cities);
        returnBtn = findViewById(R.id.back);
        nameSurnameText = findViewById(R.id.namesurname);
        rankText = findViewById(R.id.rank);
    }

    private void setListeners() {
        returnBtn.setOnClickListener(v -> finish());

        changeEmailBtn.setOnClickListener(v -> startActivity(new Intent(this, ChangeEmailActivity.class)));

        changePasswordBtn.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        saveChangesBtn.setOnClickListener(v -> saveChanges());

        logoutBtn.setOnClickListener(v -> showConfirmationDialog("Logout", "Do you want to logout?", () -> {
            mAuth.signOut();
            Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
        }));

        deleteProfileBtn.setOnClickListener(v -> showConfirmationDialog("Delete Account", "Warning! Deleting your account cannot be undone.", this::deleteAccount));

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = countryList.get(position);
                loadCitiesFromApi(selectedCountry, cityList -> {
                    citiesMap.put(selectedCountry, cityList);
                    ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(UserProfileActivity.this, android.R.layout.simple_spinner_item, cityList);
                    cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    citySpinner.setAdapter(cityAdapter);
                    if (currentCity != null && cityList.contains(currentCity)) {
                        int cityIndex = cityList.indexOf(currentCity);
                        citySpinner.setSelection(cityIndex);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadUserProfile() {
        currentUserDoc.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                String name = document.getString("name");
                String surname = document.getString("surname");
                String password = document.getString("password");
                currentCountry = document.getString("country");
                currentCity = document.getString("city");
                Double rank = document.getDouble("averageRating");

                nameSurnameText.setText(name + " " + surname);
                rankText.setText(String.format("%.1f", rank));
                currentUserDoc.update("email", user.getEmail());

                nameEditText.setText(name);
                surnameEditText.setText(surname);
                emailEditText.setText(user.getEmail());
                passwordEditText.setText(password);
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Failed to fetch user data: " + e.getMessage()));
    }

    private void saveChanges() {
        String name = nameEditText.getText().toString().trim();
        String surname = surnameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String country = (String) countrySpinner.getSelectedItem();
        String city = (String) citySpinner.getSelectedItem();

        if (name.isEmpty() || surname.isEmpty()) {
            Toast.makeText(this, "A field cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUserDoc.update("name", name, "surname", surname, "email", user.getEmail(), "password", password, "country", country, "city", city)
                .addOnSuccessListener(aVoid -> {
                    nameSurnameText.setText(name + " " + surname);
                    Toast.makeText(this, "Your profile has been updated", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    currentUserDoc.delete();
                    Toast.makeText(this, "Your profile has been deleted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                } else {
                    Log.e("DeleteUser", "Auth deletion failed: " + Objects.requireNonNull(task.getException()).getMessage());
                }
            });
        }
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> onConfirm.run())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void loadCountriesFromApi() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://countriesnow.space/api/v0.1/countries/positions").build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonBody = response.body().string();
                        JSONArray dataArray = new JSONObject(jsonBody).getJSONArray("data");
                        countryList.clear();
                        for (int i = 0; i < dataArray.length(); i++) {
                            countryList.add(dataArray.getJSONObject(i).getString("name"));
                        }
                        new Handler(Looper.getMainLooper()).post(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(UserProfileActivity.this, android.R.layout.simple_spinner_item, countryList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            countrySpinner.setAdapter(adapter);

                            if (currentCountry != null) {
                                int countryIndex = countryList.indexOf(currentCountry);
                                if (countryIndex >= 0) {
                                    countrySpinner.setSelection(countryIndex);
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void loadCitiesFromApi(String country, Consumer<List<String>> callback) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("country", country);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("https://countriesnow.space/api/v0.1/countries/cities")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonBody = response.body().string();
                        JSONArray data = new JSONObject(jsonBody).getJSONArray("data");
                        List<String> cities = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            cities.add(data.getString(i));
                        }
                        new Handler(Looper.getMainLooper()).post(() -> callback.accept(cities));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
