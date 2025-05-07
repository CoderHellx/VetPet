package com.example.vetpet;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.IgnoreExtraProperties;
@IgnoreExtraProperties
public class Pet{
    private String id;

    private String ownerId;

    private String name;
    private String gender;

    private String species;

    private String imageUrl;

    private int age;

    public Pet() { }

    public Pet(String petId, String ownerId, String name, String species,
               String imageUrl, String gender, int age) {
        this.id = petId;
        this.ownerId = ownerId;
        this.name = name;
        this.species = species;
        this.imageUrl = imageUrl;
        this.gender = gender;
        this.age = age;
    }


    public String getId()              { return id; }
    public void setId(String id)       { this.id = id; }

    public String getOwnerId()         { return ownerId; }
    public void setOwnerId(String o)   { this.ownerId = o; }

    public String getName()            { return name; }
    public void setName(String n)      { this.name = n; }

    public String getSpecies()         { return species; }
    public String getGender()         { return gender; }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setSpecies(String s)   { this.species = s; }

    public String getImageUrl()        { return imageUrl; }
    public void setImageUrl(String u)  { this.imageUrl = u; }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
