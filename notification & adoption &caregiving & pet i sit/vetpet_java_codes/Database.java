package com.example.vetpet;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Database {

    FirebaseFirestore database;

    public Database () {
        database = FirebaseFirestore.getInstance();

        CollectionReference usersRef = database.collection("messages");
        usersRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                return;
            }

            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                switch (dc.getType()) {
                    case ADDED:
                        System.out.println("Eklendi: " + dc.getDocument().getData());
                        break;
                    case MODIFIED:
                        System.out.println("Güncellendi: " + dc.getDocument().getData());
                        break;
                    case REMOVED:
                        System.out.println("Silindi: " + dc.getDocument().getData());
                        break;
                }
            }
        });
    }

    public Task<Void> createDoc (String collectionName, String name) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);

        return database.collection(collectionName).document().set(data);
    }

    public Task<Void> deleteDoc (String collectionName, String docId) {
        DocumentReference ref = database.collection(collectionName).document(docId);
        return ref.delete();
    }

    public Task<Void> updateDoc (String collectionName, String docId, String name) {
        DocumentReference ref = database.collection(collectionName).document(docId);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);

        return ref.update(data);
    }
}
