package com.example.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdoptionTickets extends AppCompatActivity {

    RecyclerView tickets;
    TicketsAdapter adapter;
    FirebaseDatabaseManager db;
    ArrayList<Ticket> ticketsArrayList = new ArrayList<>();
    ArrayList<Ticket> originalList = new ArrayList<>();

    String type;
    String selectedAge = "All";
    String selectedSex = "All";
    String selectedCity = "All";

    String[] ages = {"All", "0-1 Years", "1-7 Years", "+7 Years"};
    String[] sex = {"All", "Male", "Female"};

    List<String> cityList = new ArrayList<>();

    String country;

    LinearLayout ageDropdown, sexDropdown, cityDropdown;
    TextView ageDropdownText, sexDropdownText, cityDropdownText, ntf;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adoption_tickets);

        type = getIntent().getStringExtra("type");

        Utils.getCurrentUser(new Utils.CurrentUserCallback() {
            @Override
            public void onUserReady(User user) {

            }
        });

        Utils.getCurrentUser(new Utils.CurrentUserCallback() {
            @Override
            public void onUserReady(User user) {
                country = user.getCountry();

                cityList.add("All");

                Utils.getCitiesFromApi(country, getApplicationContext(), cities -> {
                    cityList.addAll(cities);
                    runOnUiThread(() -> {
                        setupFilter(cityDropdown, cityDropdownText, cityList, selected -> {
                            selectedCity = selected;
                            cityDropdownText.setText(selected);
                            applyAllFilters();
                        });
                    });
                });

            }
        });

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tickets = findViewById(R.id.tickets);
        ageDropdown = findViewById(R.id.age_dropdown);
        ageDropdownText = findViewById(R.id.age_dropdown_text);
        sexDropdown = findViewById(R.id.sex_dropdown);
        sexDropdownText = findViewById(R.id.sex_dropdown_text);
        cityDropdown = findViewById(R.id.city_dropdown);
        cityDropdownText = findViewById(R.id.city_dropdown_text);

        ntf = findViewById(R.id.ntf);
        ntf.setVisibility(View.GONE);

        adapter = new TicketsAdapter(ticketsArrayList);
        tickets.setAdapter(adapter);
        tickets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        db = new FirebaseDatabaseManager();

        if (type.equalsIgnoreCase("other")) {
            db.fetchOtherTickets().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        if (!(doc.getLong("apply") == 0)) continue;
                        addTicketFromDoc(doc);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        } else {
            db.fetchTicketsBySpecies(type.toLowerCase()).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        if (!(doc.getLong("apply") == 0)) continue;
                        addTicketFromDoc(doc);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        }

        setupFilter(ageDropdown, ageDropdownText, ages, selected -> {
            selectedAge = selected;
            ageDropdownText.setText(selected);
            applyAllFilters();
        });

        setupFilter(sexDropdown, sexDropdownText, sex, selected -> {
            selectedSex = selected;
            sexDropdownText.setText(selected);
            applyAllFilters();
        });
    }

    void addTicketFromDoc(DocumentSnapshot doc) {
        String petName = doc.getString("petName");
        String sex = doc.getString("petGender");
        String city = doc.getString("city");
        String species = doc.getString("species");
        String details = doc.getString("details");
        int age = doc.getLong("petAge").intValue();
        String imageUrl = doc.getString("petImageUrl");
        String ticketId = doc.getString("ticketId");
        String ownerId = doc.getString("ownerId");

        Ticket ticket = new Ticket();
        ticket.setTicketId(ticketId);
        Pet pet = new Pet();
        pet.setGender(sex);
        pet.setName(petName);
        pet.setAge(age);
        pet.setSpecies(species);
        pet.setImageUrl(imageUrl);
        ticket.setPet(pet);
        ticket.setCity(city);
        ticket.setDetails(details);
        ticket.setOwnerId(ownerId);

        ticketsArrayList.add(ticket);
        originalList.add(ticket);

        if(ticketsArrayList.size() == 0) {
            ntf.setVisibility(View.VISIBLE);
        }
    }

    interface OnFilterSelected {
        void onSelected(String value);
    }

    void setupFilter(LinearLayout dropdown, TextView textView, List<String> values, OnFilterSelected callback) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(Color.WHITE);

        LayoutInflater inflater = LayoutInflater.from(this);
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(container);

        PopupWindow popupWindow = new PopupWindow(scrollView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        for (String label : values) {
            View itemView = inflater.inflate(R.layout.filter_item, null);
            TextView itemText = itemView.findViewById(R.id.item_text);
            itemText.setText(label);

            itemView.setOnClickListener(v -> {
                callback.onSelected(label);
                popupWindow.dismiss();
            });

            container.addView(itemView);
        }

        popupWindow.setElevation(10f);
        dropdown.setOnClickListener(v -> popupWindow.showAsDropDown(dropdown));
    }

    void setupFilter(LinearLayout dropdown, TextView textView, String[] values, OnFilterSelected callback) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(Color.WHITE);

        LayoutInflater inflater = LayoutInflater.from(this);
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(container);

        PopupWindow popupWindow = new PopupWindow(scrollView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        for (String label : values) {
            View itemView = inflater.inflate(R.layout.filter_item, null);
            TextView itemText = itemView.findViewById(R.id.item_text);
            itemText.setText(label);

            itemView.setOnClickListener(v -> {
                callback.onSelected(label);
                popupWindow.dismiss();
            });

            container.addView(itemView);
        }

        popupWindow.setElevation(10f);
        dropdown.setOnClickListener(v -> popupWindow.showAsDropDown(dropdown));
    }

    public void applyAllFilters() {
        ticketsArrayList.clear();

        for (Ticket ticket : originalList) {
            int age = ticket.getPet().getAge();
            String gender = ticket.getPet().getGender();
            String ticketCity = ticket.getCity();

            boolean ageMatch;
            switch (selectedAge) {
                case "0-1 Years":
                    ageMatch = age <= 1;
                    break;
                case "1-7 Years":
                    ageMatch = age > 1 && age <= 7;
                    break;
                case "+7 Years":
                    ageMatch = age > 7;
                    break;
                default:
                    ageMatch = true;
                    break;
            }


            boolean sexMatch = selectedSex.equals("All") || selectedSex.equalsIgnoreCase(gender);
            boolean cityMatch = selectedCity.equals("All") || selectedCity.equalsIgnoreCase(ticketCity);

            if (ageMatch && sexMatch && cityMatch) {
                ticketsArrayList.add(ticket);
            }
        }

        adapter.notifyDataSetChanged();
    }

    class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.ViewHolder> {

        ArrayList<Ticket> ticketsArray;

        public TicketsAdapter(ArrayList<Ticket> ticketsArray) {
            this.ticketsArray = ticketsArray;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_ticket, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Ticket t = ticketsArray.get(position);

            holder.name.setText("Name: " + t.getPet().getName());
            holder.sex.setText("Gender: " + t.getPet().getGender());
            holder.city.setText(t.getCity());

            Glide.with(getApplicationContext())
                    .load(t.getPet().getImageUrl())
                    .placeholder(R.drawable.circle_shape)
                    .circleCrop()
                    .into(holder.pet_image);

            holder.ticket.setOnClickListener(view -> {
                db.fetchUserByUid(t.getOwnerId()).addOnSuccessListener(new OnSuccessListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        Intent i = new Intent(getApplicationContext(), CaregivingDetails.class);
                        i.putExtra("ticketId", t.getTicketId());
                        i.putExtra("type", t.getPet().getSpecies());
                        i.putExtra("name", t.getPet().getName());
                        i.putExtra("age", t.getPet().getAge() + "");
                        i.putExtra("sex", t.getPet().getGender());
                        i.putExtra("details", t.getDetails());
                        i.putExtra("ownerId", t.getOwnerId());
                        i.putExtra("pet_img", t.getPet().getImageUrl());
                        i.putExtra("username", user.getName() + " " + user.getSurname());
                        i.putExtra("email", user.getEmail());
                        i.putExtra("user_img", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQD-atDw_q1EzrHxgTLQY7cNz1xpi0rcQFjHA&s");
                        startActivity(i);
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return ticketsArray.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, sex, city;
            ImageView pet_image;
            LinearLayout ticket;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                sex = itemView.findViewById(R.id.sex);
                city = itemView.findViewById(R.id.city);
                pet_image = itemView.findViewById(R.id.pet_image);
                ticket = itemView.findViewById(R.id.ticket);
            }
        }
    }
}
