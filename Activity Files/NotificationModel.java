package com.example.vetpet;

import com.google.firebase.Timestamp;

public class NotificationModel {
    public String message;
    public String applicationId;
    public boolean isRead;
    public Timestamp timestamp;
    public String type;
    public String userId;

    public NotificationModel(String message, String applicationId, boolean isRead, Timestamp timestamp, String type, String userId) {
        this.message = message;
        this.applicationId = applicationId;
        this.isRead = isRead;
        this.timestamp = timestamp;
        this.type = type;
        this.userId = userId;
    }
}
