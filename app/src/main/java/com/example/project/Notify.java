package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class Notify extends AppCompatActivity {

    FirebaseDatabaseManager db;
    ArrayList<NotificationModel> notifyArray = new ArrayList<>();
    NotifyAdapter adapter;
    RecyclerView recyclerView;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        auth = FirebaseAuth.getInstance();

        //Back
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        db = new FirebaseDatabaseManager();
        recyclerView = findViewById(R.id.recycler_view_notify);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String userId = auth.getCurrentUser().getUid();

        db.fetchNotifications(userId).addOnSuccessListener(notifications -> {
            notifyArray.clear();
            notifyArray.addAll(notifications);
            adapter = new NotifyAdapter(notifyArray);
            recyclerView.setAdapter(adapter);
        });
    }

    class NotifyAdapter extends RecyclerView.Adapter<NotifyAdapter.NotifyViewHolder> {

        ArrayList<NotificationModel> notifys;

        public NotifyAdapter(ArrayList<NotificationModel> notifys) {
            this.notifys = notifys;
        }

        @NonNull
        @Override
        public NotifyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notify, parent, false);
            return new NotifyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotifyViewHolder holder, int position) {
            NotificationModel notification = notifys.get(position);

            holder.message.setText(notification.message);
            holder.status.setText(notification.isRead ? "Okundu" : "Yeni");

            holder.username.setText(notification.userId);

            String userId = auth.getCurrentUser().getUid();

            holder.accept.setOnClickListener(v -> {
                db.setTicket(notification.applicationId, true, userId);
                Toast.makeText(Notify.this, "Approved.", Toast.LENGTH_SHORT).show();
                db.deleteNotification(notification.applicationId);
                finish();
            });

            holder.reject.setOnClickListener(v -> {
                db.setTicket(notification.applicationId, false, userId);
                Toast.makeText(Notify.this, "Rejected.", Toast.LENGTH_SHORT).show();
                db.deleteNotification(notification.applicationId);
                finish();
            });

            if(notification.type.equalsIgnoreCase("application")) {
                holder.app.setVisibility(View.VISIBLE);
                holder.ticket.setVisibility(View.GONE);

                db.isApplicationApproved(notification.applicationId)
                        .addOnSuccessListener(isApproved -> {
                            if(isApproved == null) return;

                            if (isApproved) {
                                holder.reject.setVisibility(View.GONE);
                                holder.accept.setVisibility(View.GONE);

                                holder.status.setText("You Approved.");
                            } else {
                                holder.reject.setVisibility(View.GONE);
                                holder.accept.setVisibility(View.GONE);

                                holder.status.setText("You Rejected.");
                            }
                        });

                Glide.with(getApplicationContext())
                        .load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQD-atDw_q1EzrHxgTLQY7cNz1xpi0rcQFjHA&s")
                        .placeholder(R.drawable.circle_shape)
                        .circleCrop()
                        .into(holder.userIcon);
            } else {
                holder.app.setVisibility(View.GONE);
                holder.ticket.setVisibility(View.VISIBLE);

                db.getTicketDetailsFromApplyId(notification.applicationId)
                        .addOnSuccessListener(data -> {
                            if (data != null) {
                                String petName = (String) data.get("petName");
                                String sex = (String) data.get("gender");
                                String petImg = (String) data.get("petImageUrl");
                                String type = (String) data.get("appType");
                                Boolean isApproved = (Boolean) data.get("isApproved");
                                String endingDate = "";
                                String startingDate = "";

                                holder.pet_name.setText(petName);
                                holder.sex.setText(sex);

                                if(!(type.equals("adoption"))) {
                                    endingDate = (String) data.get("endingDate");
                                    startingDate = (String) data.get("startingDate");

                                    holder.ed.setText(endingDate);
                                    holder.sd.setText(startingDate);
                                } else {
                                    holder.ed.setVisibility(View.GONE);

                                    if(isApproved != null && isApproved) {
                                        holder.sd.setText("Approved");
                                    } else {
                                        if(isApproved == null) {
                                            holder.sd.setText("Waiting");
                                        } else {
                                            holder.sd.setText("Rejected");
                                        }
                                    }
                                }

                                Glide.with(getApplicationContext())
                                        .load(petImg)
                                        .placeholder(R.drawable.circle_shape)
                                        .circleCrop()
                                        .into(holder.petImg);
                            }
                        });
            }
        }

        @Override
        public int getItemCount() {
            return notifys.size();
        }

        class NotifyViewHolder extends RecyclerView.ViewHolder {

            TextView message, status, username, pet_name, sex, ed, sd;
            Button accept, reject;
            ImageView userIcon, petImg;
            LinearLayout app, ticket;

            public NotifyViewHolder(@NonNull View itemView) {
                super(itemView);
                message = itemView.findViewById(R.id.notification_message);
                status = itemView.findViewById(R.id.rank);
                accept = itemView.findViewById(R.id.accept);
                reject = itemView.findViewById(R.id.reject);
                userIcon = itemView.findViewById(R.id.user_icon);
                app = itemView.findViewById(R.id.app);
                ticket = itemView.findViewById(R.id.ticket);
                username = itemView.findViewById(R.id.username);
                pet_name = itemView.findViewById(R.id.name);
                sd = itemView.findViewById(R.id.sd);
                ed = itemView.findViewById(R.id.ed);
                sex = itemView.findViewById(R.id.sex);
                petImg = itemView.findViewById(R.id.pet_image);
            }
        }
    }
}
