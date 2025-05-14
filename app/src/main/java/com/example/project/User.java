package com.example.project;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class User {
    String userId, name, surname, email, country, city;
    String password = "123456";
    ArrayList<Pet> pets;
    ArrayList<Pet> petsIPetSit;

    double averageRating = 0;
    int totalRatings = 0;

    FirebaseAuth auth;
    FirebaseUser user;

    FirebaseDatabaseManager db;

    public User(String userId, String name, String surname, String email, String country){ //add password and change the User constructors that are there!!!!!
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        this.userId = userId;
        this.name = name;
        this.surname = surname;
        this.email = email;
        pets = new ArrayList<>();
        petsIPetSit = new ArrayList<>();
        this.country = country;
    }
    public void addPetToPets(Pet pet){
        pets.add(pet);
    }
    public void addPetToPetsIPetSit(Pet pet){
        petsIPetSit.add(pet);
    }
    public String getUserId(){return userId;}

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public interface PetsCallback {
        void onPetsFetched(ArrayList<Pet> pets);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void getPets(PetsCallback callback) {
        if (pets == null) {
            pets = new ArrayList<>();

            db.fetchPets(user.getUid()).addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    Pet pet = new Pet();
                    pet.setAge(doc.getLong("age").intValue());
                    pet.setName(doc.getString("name"));
                    pet.setGender(doc.getString("gender"));
                    pet.setSpecies(doc.getString("species"));
                    pets.add(pet);
                }
                callback.onPetsFetched(pets);
            });
        } else {
            callback.onPetsFetched(pets);
        }
    }


    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public ArrayList<Pet> getPetsIPetSit(){return petsIPetSit;}
    public void setUserId(String userId){this.userId = userId;}
    public String getName(){return this.name;}
    public String getPassword(){return this.password;}
    public String getSurname(){return this.surname;}
    public String getEmail(){return this.email;}
    public void setName(String name){this.name = name;}
    public void setSurname(String surname){this.surname = surname;}
    public void setEmail(String email){this.email = email;}

    public double getAverageRating(){
        return this.averageRating;
    }

    public void setAverageRating(double averageRating){
        this.averageRating = averageRating;
    }

    public int getTotalRatings(){
        return this.totalRatings;
    }

    public void setTotalRatings(int totalRatings){
        this.totalRatings = totalRatings;
    }
}
