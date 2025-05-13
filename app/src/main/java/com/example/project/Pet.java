package com.example.project;

import java.io.Serializable;

public class Pet implements Serializable {
    private String id;
    private String ownerId;
    private String name;
    private String species;
    private String birthday;
    private String gender;
    private String additionalInfo;
    private String photoUrl;
    private int age;

    public Pet() {
    } // Required by Firebase
    public Pet(String id, String ownerId, String name, String type, String birthday,
               String gender, String additionalInfo, String photoUrl) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.species = type;
        this.birthday = birthday;
        this.gender = gender;
        this.additionalInfo = additionalInfo;
        this.photoUrl = photoUrl;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getImageUrl() {
        return photoUrl;
    }

    public void setImageUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getAge(){
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
