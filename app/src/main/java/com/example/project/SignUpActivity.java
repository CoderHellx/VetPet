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

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailET, passET;
    private ProgressBar progressBar;

    private Spinner spinnerCountry;
    private Spinner spinnerCity;

    private List<String> countryList = new ArrayList<>();
    private Map<String, List<String>> citiesMap = new HashMap<>();


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

        loadCitiesFromCSV();

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
                String selectedCountry = countryList.get(pos);
                List<String> cityList = citiesMap.get(selectedCountry);
                if (cityList == null) cityList = new ArrayList<>();

                ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                        SignUpActivity.this,
                        android.R.layout.simple_spinner_item,
                        cityList
                );
                cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(cityAdapter);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        mAuth = FirebaseAuth.getInstance();

        emailET = findViewById(R.id.emailEditText);
        passET  = findViewById(R.id.passwordEditText);
        progressBar = findViewById(R.id.progressBar);

        Button signUpBtn = findViewById(R.id.signUpButton);
        signUpBtn.setOnClickListener(v -> attemptSignUp());
    }

    private void loadCitiesFromCSV() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open("world_cities.csv"))
        )) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                String city    = parts[0].trim();
                String country = parts[1].trim();

                if (!citiesMap.containsKey(country)) {
                    citiesMap.put(country, new ArrayList<>());
                    countryList.add(country);
                }
                citiesMap.get(country).add(city);
            }

            Log.d("SPINNER_DEBUG", "Loaded countries: " + countryList.size());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void attemptSignUp() {
        String email = emailET.getText().toString().trim();
        String pass  = passET.getText().toString().trim();

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
                        user.sendEmailVerification()
                                .addOnCompleteListener(this, verifyTask -> {
                                    if (verifyTask.isSuccessful()) {
                                        Toast.makeText(this,
                                                "Verification email has been sent: " + user.getEmail(),
                                                Toast.LENGTH_LONG).show();
                                        // ➋ Sign-In sayfasına dön
                                        finish();
                                    } else {
                                        Toast.makeText(this,
                                                "Verification email could not be sent: " +
                                                        verifyTask.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this,
                                "Sign up failed: " +
                                        task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}

