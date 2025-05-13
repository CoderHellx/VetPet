package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CaregiverRatingAdapter extends RecyclerView.Adapter<CaregiverRatingAdapter.TicketViewHolder> {

    private List<CaregivingTicket> ticketList;
    private Context context;

    public CaregiverRatingAdapter(Context context, List<CaregivingTicket> ticketList) {
        this.context = context;
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket_rating, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        CaregivingTicket ticket = ticketList.get(position);

        FirebaseFirestore.getInstance().collection("users")
                .document(ticket.getCaregivingUserId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    String caregiverName = snapshot.getString("name");
                    if (caregiverName != null && !caregiverName.isEmpty()) {
                        holder.textCaregiverName.setText(caregiverName);
                    } else {
                        holder.textCaregiverName.setText("Unknown Caregiver");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.textCaregiverName.setText("Unknown Caregiver");
                });

        holder.textPetName.setText("Pet: " + ticket.getPet().getName());

        holder.buttonRate.setOnClickListener(v -> {
            Intent intent = new Intent(context, RateCaregiversActivity.class);
            intent.putExtra("ticket", ticket);
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView textCaregiverName, textPetName;
        Button buttonRate;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            textCaregiverName = itemView.findViewById(R.id.textCaregiverName);
            textPetName = itemView.findViewById(R.id.textPetName);
            buttonRate = itemView.findViewById(R.id.buttonRate);
        }
    }
}
