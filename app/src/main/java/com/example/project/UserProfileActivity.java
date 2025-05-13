package com.example.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    EditText nameedittext, surnameedittext, emailedittext, passwordedittext;
    Button savechanges, logout, deleteprofile;
    Spinner countryspinner, cityspinner;
    ImageButton changePassword, changeEmail, returnbutton;
    String currentuserId,country,city;
    TextView namesurnameText, rankText;
    DocumentReference currentuser;
    ArrayAdapter<String> cityAdapter;
    ArrayAdapter<String> countryAdapter;
    FirebaseAuth mAuth;
    private List<String> countryList = new ArrayList<>();
    private Map<String, List<String>> citiesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        namesurnameText = findViewById(R.id.namesurname);
        rankText = findViewById(R.id.rank);
        nameedittext = findViewById(R.id.nameedittext);
        surnameedittext = findViewById(R.id.surnameedittext);
        emailedittext = findViewById(R.id.emailedittext);
        passwordedittext = findViewById(R.id.passwordedittext);
        savechanges = findViewById((R.id.saveChangesButton));
        logout = findViewById((R.id.logoutButton));
        deleteprofile = findViewById((R.id.deleteProfileButton));
        changeEmail = findViewById(R.id.changeemailbutton);
        changePassword = findViewById(R.id.changepasswordbutton);
        countryspinner = findViewById(R.id.countries);
        cityspinner = findViewById(R.id.cities);
        returnbutton = findViewById(R.id.back);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        currentuserId = user.getUid();
        currentuser = FirebaseFirestore.getInstance().collection("users").document(
                currentuserId);

        returnbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, ChangeEmailActivity.class);
                intent.putExtra("id", currentuserId);
                startActivity(intent);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
                intent.putExtra("id", currentuserId);
                startActivity(intent);
            }
        });

        currentuser.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String surname = documentSnapshot.getString("surname");
                        String email = documentSnapshot.getString("email");
                        String password = documentSnapshot.getString("password");
                        Double rank = documentSnapshot.getDouble("ranking");
                        country = documentSnapshot.getString("country");
                        city = documentSnapshot.getString("city");
                        namesurnameText.setText(name + " " + surname);
                        rankText.setText("" + rank);
                        nameedittext.setText(name);
                        surnameedittext.setText(surname);
                        emailedittext.setText(email);
                        passwordedittext.setText(password);
                        int countryPosition = countryAdapter.getPosition(country);
                        countryspinner.setSelection(countryPosition);

                        List<String> cityList = citiesMap.get(country);
                        if (cityList != null) {
                            cityAdapter = new ArrayAdapter<>(
                                    UserProfileActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    cityList
                            );
                            cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            cityspinner.setAdapter(cityAdapter);

                            int cityPosition = cityAdapter.getPosition(city);
                            cityspinner.setSelection(cityPosition);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "User data fetch failed: " + e.getMessage());
                });

        savechanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newname = nameedittext.getText().toString().trim();
                String newsurname = surnameedittext.getText().toString().trim();
                String newemail = emailedittext.getText().toString().trim();
                String newpassword = passwordedittext.getText().toString().trim();
                String country = "";
                String city = "";
                if (countryspinner.getSelectedItem() != null) {
                    country = countryspinner.getSelectedItem().toString();
                }
                if (cityspinner.getSelectedItem() != null) {
                    city = cityspinner.getSelectedItem().toString();
                }
                if(newname.length()==0 || newsurname.length()==0){
                    Toast.makeText(getApplicationContext(), "A field cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    currentuser.update("name", newname, "surname", newsurname,
                            "email", newemail, "password", newpassword,"country",country,"city",city);
                    namesurnameText.setText(newname + " " + newsurname);
                    Toast.makeText(getApplicationContext(), "Your profile has been updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder warning = new AlertDialog.Builder(UserProfileActivity.this);
                warning.setTitle("Logout");
                warning.setMessage("Do you want to logout?");
                warning.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Logging out", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent();
                        i.setClass(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                    }
                });
                warning.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = warning.create();
                alertDialog.show();
            }
        });

        deleteprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder warning = new AlertDialog.Builder(UserProfileActivity.this);
                warning.setTitle("Delete Account");
                warning.setMessage("Warning! Deleting your account cannot be undone.");
                warning.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Your profile has been deleted", Toast.LENGTH_SHORT).show();
                        currentuser.delete();
                        Intent i = new Intent();
                        i.setClass(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                    }
                });
                warning.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = warning.create();
                alertDialog.show();
            }
        });

        loadCitiesFromCSV();

        countryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                countryList
        );
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countryspinner.setAdapter(countryAdapter);

        countryspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstLoad = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedCountry = countryList.get(pos);
                List<String> cityList = citiesMap.get(selectedCountry);
                if (cityList == null) cityList = new ArrayList<>();

                cityAdapter = new ArrayAdapter<>(
                        UserProfileActivity.this,
                        android.R.layout.simple_spinner_item,
                        cityList
                );
                cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cityspinner.setAdapter(cityAdapter);
                if (firstLoad && country != null && city != null) {
                    int cityposition = cityAdapter.getPosition(city);
                    cityspinner.setSelection(cityposition);
                    firstLoad = false;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
                String city = parts[0].trim();
                String country = parts[1].trim();

                if (!citiesMap.containsKey(country)) {
                    citiesMap.put(country, new ArrayList<>());
                    countryList.add(country);
                }
                citiesMap.get(country).add(city);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}