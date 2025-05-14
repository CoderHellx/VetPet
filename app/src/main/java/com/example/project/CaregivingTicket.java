package com.example.project;

import android.content.Context;
import android.widget.Toast;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CaregivingTicket implements Serializable {

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

    private boolean isRated;

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

    public boolean isRated() {
        return isRated;
    }

    public void setRated(boolean rated){
        this.isRated = rated;
    }

    public boolean hasCaregivingEnded(Context context) {
        if (endingDate == null || endingTimeHour == null || endingTimeMinute == null) {
            // Toast.makeText(context, "Missing data: " +
            // "\nendingDate: " + endingDate +
            //"\nendingTimeHour: " + endingTimeHour +
            // "\nendingTimeMinute: " + endingTimeMinute,
            //Toast.LENGTH_LONG).show();
            return false;
        }

        try {
            String[] dateParts = endingDate.split("/");
            if (dateParts.length != 3) {
                //Toast.makeText(context, "Incorrect date format: " + endingDate, Toast.LENGTH_LONG).show();
                return false;
            }

            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1;
            int year = Integer.parseInt(dateParts[2]);

            int hour = Integer.parseInt(endingTimeHour);
            int minute = Integer.parseInt(endingTimeMinute);

            Calendar endTime = Calendar.getInstance();
            endTime.set(year, month, day, hour, minute, 0);
            endTime.set(Calendar.MILLISECOND, 0);

            Calendar now = Calendar.getInstance();

            String debug = "Now: " + now.getTime() + "\nEnd: " + endTime.getTime();
            //Toast.makeText(context, debug, Toast.LENGTH_LONG).show();

            return now.after(endTime);
        } catch (Exception e) {
            //Toast.makeText(context, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }
    }








}
