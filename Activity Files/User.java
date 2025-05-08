package com.example.vetpet;

import java.util.ArrayList;

public class User {
    String userId, name, surname, email, country, city;
    ArrayList<Pet> pets;
    ArrayList<Pet> petsIPetSit;
    public User(String userId, String name, String surname, String email){
        this.userId = userId;
        this.name = name;
        this.surname = surname;
        this.email = email;
        pets = new ArrayList<>();
        petsIPetSit = new ArrayList<>();
    }
    public void addPetToPets(Pet pet){
        pets.add(pet);
    }
    public void addPetToPetsIPetSit(Pet pet){
        petsIPetSit.add(pet);
    }
}
