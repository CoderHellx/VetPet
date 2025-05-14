package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Handler;
import android.os.Looper;
import java.util.function.Consumer;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private FirebaseDatabaseManager db = new FirebaseDatabaseManager();
    private EditText emailET, passET;
    private ProgressBar progressBar;

    private Spinner spinnerCountry;
    private Spinner spinnerCity;

    private List<String> countryList = new ArrayList<>();
    private Map<String, List<String>> citiesMap = new HashMap<>();

    private EditText nameET, surnameET;

    String selectedCountry;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        spinnerCountry = findViewById(R.id.countrySpinner);
        spinnerCity    = findViewById(R.id.citySpinner);

        loadCountriesFromApi();

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                countryList
        );
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(countryAdapter);

        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedCountry = countryList.get(pos);

                loadCitiesFromApi(selectedCountry, cityList -> {
                    ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                            SignUpActivity.this,
                            android.R.layout.simple_spinner_item,
                            cityList
                    );
                    cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCity.setAdapter(cityAdapter);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        mAuth = FirebaseAuth.getInstance();

        emailET = findViewById(R.id.emailEditText);
        passET  = findViewById(R.id.passwordEditText);
        progressBar = findViewById(R.id.progressBar);

        nameET = findViewById(R.id.nameEditText);
        surnameET = findViewById(R.id.surnameEditText);


        Button signUpBtn = findViewById(R.id.signUpButton);
        signUpBtn.setOnClickListener(v -> attemptSignUp());
    }


    private void loadCountriesFromApi() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://countriesnow.space/api/v0.1/countries/positions")
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
                        String body = response.body().string();
                        JSONObject json = new JSONObject(body);
                        JSONArray dataArray = json.getJSONArray("data");

                        countryList.clear();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject countryObj = dataArray.getJSONObject(i);
                            String country = countryObj.getString("name");
                            countryList.add(country);
                        }

                        new Handler(Looper.getMainLooper()).post(() -> {
                            ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(
                                    SignUpActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    countryList
                            );
                            countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCountry.setAdapter(countryAdapter);
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

        RequestBody body = RequestBody.create(
                MediaType.get("application/json"),
                json.toString()
        );

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
                        String resBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(resBody);
                        JSONArray citiesArray = jsonResponse.getJSONArray("data");

                        List<String> cityList = new ArrayList<>();
                        for (int i = 0; i < citiesArray.length(); i++) {
                            cityList.add(citiesArray.getString(i));
                        }

                        new Handler(Looper.getMainLooper()).post(() -> callback.accept(cityList));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private void attemptSignUp() {
        String email = emailET.getText().toString().trim();
        String pass  = passET.getText().toString().trim();

        String selectedCountry = spinnerCountry.getSelectedItem() != null
                ? spinnerCountry.getSelectedItem().toString()
                : "Unknown";

        String selectedCity = spinnerCity.getSelectedItem() != null
                ? spinnerCity.getSelectedItem().toString()
                : "Unknown";

        if (TextUtils.isEmpty(email)) {
            emailET.setError("Email can not be empty");
            return;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            passET.setError("The password needs to be at least 6 characters long");
            return;
        }

        progressBar.setVisibility(android.view.View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            //userID ve password e bakılacak
                            User u = new User(user.getUid(), nameET.getText().toString(), surnameET.getText().toString(), emailET.getText().toString(), selectedCountry);
                            u.setPassword(pass);
                            u.setCity(selectedCity);
                            db.saveUser(u)
                                    .addOnSuccessListener(unused -> {
                                        Log.d("SAVE_USER", "User saved successfully!");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("SAVE_USER", "Failed to save user: ", e);
                                    });



                            user.sendEmailVerification()
                                    .addOnCompleteListener(this, verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            Toast.makeText(this,
                                                    "Verification email has been sent: " + user.getEmail(),
                                                    Toast.LENGTH_LONG).show();
                                            finish();
                                        } else {
                                            Toast.makeText(this,
                                                    "Verification email could not be sent: " +
                                                            verifyTask.getException().getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                            Log.e("EMAIL_VERIFICATION", "Failed: ", verifyTask.getException());
                                        }
                                    });
                        } else {
                            Log.e("SIGNUP_FLOW", "FirebaseUser is null after successful task.");
                            Toast.makeText(this, "Unexpected error: user is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("FIREBASE_AUTH", "Sign-up failed: ", task.getException());
                        Toast.makeText(this,
                                "Sign up failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                        Log.e("FIREBASE_AUTH", "Sign up failed: ", task.getException());

                    }

                });
    }
}

