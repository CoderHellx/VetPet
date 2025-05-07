package com.example.vetpet;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDatabaseManager {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Void> saveTicket(Ticket ticket) {
        DocumentReference ref = db.collection("tickets").document();
        ticket.setTicketId(ref.getId());

        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("ticketId", ticket.getTicketId());
        ticketData.put("ownerId", ticket.getOwnerId());
        ticketData.put("petId", ticket.getTicketPetId());
        ticketData.put("species", ticket.getSpecie().toLowerCase());
        ticketData.put("isAccepted",false);
        ticketData.put("details", ticket.getDetails());
        ticketData.put("city", ticket.getCity());
        ticketData.put("petName", ticket.getPet().getName());
        ticketData.put("petGender", ticket.getPet().getGender());
        ticketData.put("petImageUrl", ticket.getPet().getImageUrl());
        ticketData.put("petAge", ticket.getPet().getAge());
        ticketData.put("createdAt", ticket.getCreatedAt());

        return ref.set(ticketData);
    }

    public Task<Void> saveCaregivingTicket(CaregivingTicket ticket) {
        DocumentReference ref = db.collection("caregiving_tickets").document();
        ticket.setTicketId(ref.getId());

        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("ticketId", ticket.getTicketId());
        ticketData.put("ownerId", ticket.getOwnerId());
        ticketData.put("petId", ticket.getPetId());
        ticketData.put("species", ticket.getSpecie().toLowerCase());
        ticketData.put("isAccepted",false);
        ticketData.put("details", ticket.getDetails());
        ticketData.put("city", ticket.getCity());
        ticketData.put("petName", ticket.getPet().getName());
        ticketData.put("petGender", ticket.getPet().getGender());
        ticketData.put("petImageUrl", ticket.getPet().getImageUrl());
        ticketData.put("petAge", ticket.getPet().getAge());
        ticketData.put("startingDate", ticket.getStartingDate());
        ticketData.put("startingTimeHour", ticket.getStartingTimeHour());
        ticketData.put("startingTimeMinute", ticket.getStartingTimeMinute());
        ticketData.put("endingDate", ticket.getEndingDate());
        ticketData.put("endingTimeHour", ticket.getEndingTimeHour());
        ticketData.put("endingTimeMinute", ticket.getEndingTimeMinute());
        ticketData.put("createdAt", ticket.getCreatedAt());

        return ref.set(ticketData);
    }

    public Task<Void> saveTestPet(Pet pet) {
        return db.collection("pets").document(pet.getId()).set(pet);
    }


    public Task<QuerySnapshot> fetchPets(String ownerId) {
        return db.collection("pets")
                .whereEqualTo("ownerId", ownerId)
                .get();
    }

    public Task<QuerySnapshot> fetchTickets() {
        Query query = db.collection("tickets");

        return query.get();
    }

    public Task<QuerySnapshot> fetchOtherTickets() {
        List<String> excludedSpecies = Arrays.asList("cat", "dog", "rabbit", "bird", "hamster", "fish", "turtle");

        Query query = db.collection("tickets")
                .whereNotIn("species", excludedSpecies)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        return query.orderBy("createdAt", Query.Direction.DESCENDING).get();
    }

    public Task<QuerySnapshot> fetchOtherCaregivingTickets() {
        List<String> excludedSpecies = Arrays.asList("cat", "dog", "rabbit", "bird", "hamster", "fish", "turtle");

        Query query = db.collection("caregiving_tickets")
                .whereNotIn("species", excludedSpecies)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        return query.orderBy("createdAt", Query.Direction.DESCENDING).get();
    }

    public Task<QuerySnapshot> fetchTicketsBySpecies(String species) {
        Query query = db.collection("tickets");

        if (species != null && !species.isEmpty() && !species.equalsIgnoreCase("All")) {
            species = species.toLowerCase();
            query = query.whereEqualTo("species", species);
        }

        return query.get();
    }

    public Task<QuerySnapshot> fetchCaregivingTicketsBySpecies(String species) {
        Query query = db.collection("caregiving_tickets");

        if (species != null && !species.isEmpty() && !species.equalsIgnoreCase("All")) {
            species = species.toLowerCase();
            query = query.whereEqualTo("species", species);
        }

        return query.get();
    }

    public Task<Void> makeApply(String ticketId, String applicantId, String ownerId, String type) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference appRef = db.collection("applications").document();
        String applicationId = appRef.getId();

        Map<String, Object> applyData = new HashMap<>();
        applyData.put("applicationId", appRef.getId());
        applyData.put("ticketId", ticketId);
        applyData.put("applicantId", applicantId);
        applyData.put("type", type);
        applyData.put("status", "pending");
        applyData.put("timestamp", FieldValue.serverTimestamp());

        DocumentReference notifyOwnerRef = db.collection("notifications").document();
        Map<String, Object> notifyOwnerData = new HashMap<>();
        notifyOwnerData.put("notificationId", notifyOwnerRef.getId());
        notifyOwnerData.put("userId", ownerId);
        notifyOwnerData.put("type", "application");
        notifyOwnerData.put("message", "A user has applied for your ticket.");
        notifyOwnerData.put("applicationId", applicationId);
        notifyOwnerData.put("isRead", false);
        notifyOwnerData.put("timestamp", FieldValue.serverTimestamp());

        DocumentReference notifyApplicantRef = db.collection("notifications").document();
        Map<String, Object> notifyApplicantData = new HashMap<>();
        notifyApplicantData.put("notificationId", notifyApplicantRef.getId());
        notifyApplicantData.put("userId", applicantId);
        notifyApplicantData.put("type", "application_r");
        notifyApplicantData.put("message", "Your application has been submitted, awaiting approval.");
        notifyApplicantData.put("applicationId", applicationId);
        notifyApplicantData.put("isRead", false);
        notifyApplicantData.put("timestamp", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();
        batch.set(appRef, applyData);
        batch.set(notifyOwnerRef, notifyOwnerData);
        batch.set(notifyApplicantRef, notifyApplicantData);

        return batch.commit();
    }

    public Task<List<NotificationModel>> fetchNotifications(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        return db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .continueWith(task -> {
                    List<NotificationModel> notifications = new ArrayList<>();

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String message = doc.getString("message");
                            String appId = doc.getString("applicationId");
                            boolean isRead = Boolean.TRUE.equals(doc.getBoolean("isRead"));
                            Timestamp timestamp = doc.getTimestamp("timestamp");
                            String type = doc.getString("type");
                            String userIda = doc.getString("userId");

                            notifications.add(new NotificationModel(message, appId, isRead, timestamp, type, userIda));
                        }
                    }

                    Log.e("as", notifications.toString());

                    return notifications;
                });
    }

    public Task<Void> setTicket(String applyId, boolean isApprove, String userId) {
        TaskCompletionSource<Void> taskSource = new TaskCompletionSource<>();

        db.collection("applications")
                .whereEqualTo("applicationId", applyId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        String ticketId = doc.getString("ticketId");
                        String type = doc.getString("type");

                        Log.e("asd", doc.toString());

                        HashMap<String, Object> data = new HashMap<>();
                        data.put("isApproved", isApprove);

                        String collectionPath = "";

                        if (type.equalsIgnoreCase("adoption")) {
                            if (isApprove) {
                                data.put("adoptionUserId", userId);
                            }
                            collectionPath = "tickets";
                        } else {
                            if (isApprove) {
                                data.put("caregivingUserId", userId);
                            }
                            collectionPath = "caregiving_tickets";
                        }

                        db.collection(collectionPath)
                                .document(ticketId)
                                .set(data, SetOptions.merge())
                                .addOnSuccessListener(unused -> taskSource.setResult(null))
                                .addOnFailureListener(taskSource::setException);

                    } else {
                        taskSource.setException(new Exception("Başvuru bulunamadı."));
                    }
                })
                .addOnFailureListener(taskSource::setException);

        return taskSource.getTask();
    }

    public Task<Boolean> isApplicationApproved(String applyId) {
        TaskCompletionSource<Boolean> taskSource = new TaskCompletionSource<>();

        db.collection("applications").document(applyId).get()
                .addOnSuccessListener(applicationSnapshot -> {
                    if (applicationSnapshot.exists()) {
                        String ticketId = applicationSnapshot.getString("ticketId");
                        String type = applicationSnapshot.getString("type");

                        if (ticketId == null || type == null) {
                            taskSource.setResult(null);
                            return;
                        }

                        String ticketCollection = type.equalsIgnoreCase("adoption") ? "tickets" : "caregiving_tickets";

                        db.collection(ticketCollection).document(ticketId).get()
                                .addOnSuccessListener(ticketSnapshot -> {
                                    if (ticketSnapshot.exists()) {
                                        Boolean isApproved = ticketSnapshot.getBoolean("isApproved");
                                        taskSource.setResult(isApproved);
                                    } else {
                                        taskSource.setResult(null);
                                    }
                                })
                                .addOnFailureListener(e -> taskSource.setResult(null));
                    } else {
                        taskSource.setResult(null);
                    }
                })
                .addOnFailureListener(e -> taskSource.setResult(null));

        return taskSource.getTask();
    }


    public Task<Map<String, Object>> getTicketDetailsFromApplyId(String applyId) {
        TaskCompletionSource<Map<String, Object>> taskSource = new TaskCompletionSource<>();

        db.collection("applications").document(applyId).get()
                .addOnSuccessListener(appSnap -> {
                    if (!appSnap.exists()) {
                        taskSource.setResult(null);
                        return;
                    }

                    String ticketId = appSnap.getString("ticketId");
                    String type = appSnap.getString("type");

                    if (ticketId == null || type == null) {
                        taskSource.setResult(null);
                        return;
                    }

                    String col = type.equalsIgnoreCase("adoption") ? "tickets" : "caregiving_tickets";

                    db.collection(col).document(ticketId).get()
                            .addOnSuccessListener(ticketSnap -> {
                                if (!ticketSnap.exists()) {
                                    taskSource.setResult(null);
                                    return;
                                }

                                Map<String, Object> data = ticketSnap.getData();
                                if (data != null) {
                                    data.put("ticketId", ticketSnap.getId());
                                }
                                data.put("appType", appSnap.getString("type"));
                                taskSource.setResult(data);
                            })
                            .addOnFailureListener(e -> taskSource.setResult(null));
                })
                .addOnFailureListener(e -> taskSource.setResult(null));

        return taskSource.getTask();
    }
}