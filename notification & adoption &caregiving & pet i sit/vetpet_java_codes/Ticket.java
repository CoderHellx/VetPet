package com.example.vetpet;

import java.util.Date;

public class Ticket {
    private String ticketId;
    private String ownerId;
    private String petId;
    private Pet pet;
    private String specie;
    private boolean isAccepted = false;
    private String details;
    private String city;
    private Date createdAt;
    public int isApproved;
    public String adoptionUserId;

    public Ticket() { }

    public Ticket(String ownerId, String petId, String specie,String details,  String city) {
        this.ownerId = ownerId;
        this.petId = petId;
        this.specie = specie;
        this.details = details;
        this.city = city;
        this.createdAt = new Date();
    }

    public void closeTicket() {
        this.isAccepted = true;
    }
    public String getTicketId(){
        return ticketId;
    }
    public String getOwnerId(){
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getTicketPetId(){
        return petId;
    }
    public String getSpecie(){
        return specie;
    }
    public String getDetails(){
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCity(){
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Date getCreatedAt(){
        return createdAt;
    }
    public Pet getPet(){
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public void setTicketId(String id) {
        ticketId = id;
    }

    public void setIsApproved(int isApproved) {
        this.isApproved = isApproved;
    }

    public int getIsApproved() {
        return isApproved;
    }

    public void setAdoptionUserId(String adoptionUserId) {
        this.adoptionUserId = adoptionUserId;
    }

    public String getAdoptionUserId() {
        return adoptionUserId;
    }
}