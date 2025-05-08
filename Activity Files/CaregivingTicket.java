package com.example.vetpet;

import java.util.Date;

public class CaregivingTicket {

    public Pet pet;
    public String details;
    public String startingDate;
    public String startingTimeHour;
    public String startingTimeMinute;
    public String endingDate;
    public String endingTimeHour;
    public String endingTimeMinute;
    public String ticketId;
    public String ownerId;
    public String petId;
    public String specie;
    public String city;
    public Date createdAt;
    public int isApproved;
    public String caregivingUserId;

    public CaregivingTicket(){
        this.createdAt = new Date();
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public String getPetId() {
        return petId;
    }

    public void setSpecie(String specie) {
        this.specie = specie;
    }

    public String getSpecie() {
        return specie;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public Pet getPet() {
        return pet;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public void setStartingDate(String startingDate, String startingTimeHour, String startingTimeMinute){
        this.startingDate = startingDate;
        this.startingTimeHour = startingTimeHour;
        this.startingTimeMinute = startingTimeMinute;
    }

    public void setEndingDate(String endingDate, String endingTimeHour, String endingTimeMinute){
        this.endingDate = endingDate;
        this.endingTimeHour = endingTimeHour;
        this.endingTimeMinute = endingTimeMinute;
    }

    public String getStartingDate() {
        return startingDate;
    }

    public String getStartingTimeHour() {
        return startingTimeHour;
    }

    public String getStartingTimeMinute() {
        return startingTimeMinute;
    }

    public String getEndingDate() {
        return endingDate;
    }

    public String getEndingTimeHour() {
        return endingTimeHour;
    }

    public String getEndingTimeMinute() {
        return endingTimeMinute;
    }

    public int getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(int isApproved) {
        this.isApproved = isApproved;
    }

    public String getCaregivingUserId() {
        return caregivingUserId;
    }

    public void setCaregivingUserId(String caregivingUserId) {
        this.caregivingUserId = caregivingUserId;
    }
}
