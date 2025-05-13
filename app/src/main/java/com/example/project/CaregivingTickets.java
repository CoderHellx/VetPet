package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CaregivingTickets extends AppCompatActivity {

    RecyclerView tickets;
    TicketsAdapter adapter;
    FirebaseDatabaseManager db;
    ArrayList<CaregivingTicket> ticketsArrayList = new ArrayList<>();
    ArrayList<CaregivingTicket> originalList = new ArrayList<>();

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
    EditText startingDateEditText, endingDateEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiving_tickets);

        type = getIntent().getStringExtra("type");

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
        startingDateEditText = findViewById(R.id.sd);
        endingDateEditText = findViewById(R.id.ed);

        ntf = findViewById(R.id.ntf);
        ntf.setVisibility(View.GONE);

        adapter = new TicketsAdapter(ticketsArrayList);
        tickets.setAdapter(adapter);
        tickets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        db = new FirebaseDatabaseManager();

        if (type.equalsIgnoreCase("other")) {
            db.fetchOtherCaregivingTickets().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    if (!(doc.getLong("apply") == 0)) continue;
                    addTicketFromDoc(doc);
                }
                adapter.notifyDataSetChanged();
            });
        } else {
            db.fetchCaregivingTicketsBySpecies(type.toLowerCase()).addOnSuccessListener(filteredDocuments -> {
                for (DocumentSnapshot doc : filteredDocuments) {
                    if (!(doc.getLong("apply") == 0)) continue;
                    addTicketFromDoc(doc);
                }
                adapter.notifyDataSetChanged();
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

        startingDateEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(android.text.Editable editable) {
                applyAllFilters();
            }
        });

        endingDateEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(android.text.Editable editable) {
                applyAllFilters();
            }
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

        String startingDate = doc.getString("startingDate");
        String startingHour = doc.getString("startingTimeHour");
        String startingMinute = doc.getString("startingTimeMinute");

        String endingDate = doc.getString("endingDate");
        String endingHour = doc.getString("endingTimeHour");
        String endingMinute = doc.getString("endingTimeMinute");

        CaregivingTicket ticket = new CaregivingTicket();
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
        ticket.setStartingDate(startingDate, startingHour, startingMinute);
        ticket.setEndingDate(endingDate, endingHour, endingMinute);

        ticketsArrayList.add(ticket);
        originalList.add(ticket);

        if(ticketsArrayList.size() == 0) {
            ntf.setVisibility(View.VISIBLE);
        }
    }

    interface OnFilterSelected {
        void onSelected(String value);
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


    public void applyAllFilters() {
        ticketsArrayList.clear();

        String startDateStr = startingDateEditText.getText().toString().trim();
        String endDateStr = endingDateEditText.getText().toString().trim();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Date startDate = null;
        Date endDate = null;

        try {
            if (!startDateStr.isEmpty()) {
                startDate = sdf.parse(startDateStr);
            }
            if (!endDateStr.isEmpty()) {
                endDate = sdf.parse(endDateStr);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (CaregivingTicket ticket : originalList) {
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

            boolean dateMatch = true;
            try {
                Date ticketDate = sdf.parse(ticket.getStartingDate());
                if (startDate != null && ticketDate.before(startDate)) {
                    dateMatch = false;
                }
                if (endDate != null && ticketDate.after(endDate)) {
                    dateMatch = false;
                }
            } catch (ParseException e) {
                dateMatch = false;
            }

            if (ageMatch && sexMatch && cityMatch && dateMatch) {
                ticketsArrayList.add(ticket);
            }
        }

        adapter.notifyDataSetChanged();
    }

    class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.ViewHolder> {

        ArrayList<CaregivingTicket> ticketsArray;

        public TicketsAdapter(ArrayList<CaregivingTicket> ticketsArray) {
            this.ticketsArray = ticketsArray;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_caregiving_ticket, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CaregivingTicket t = ticketsArray.get(position);

            holder.name.setText("Name: " + t.getPet().getName());
            holder.sex.setText("Gender: " + t.getPet().getGender());

            holder.ed.setText(t.getEndingDate() + " " + t.getEndingTimeHour() + ":" + t.getEndingTimeMinute());
            holder.sd.setText(t.getStartingDate() + " " + t.getStartingTimeHour() + ":" + t.getStartingTimeMinute());

            holder.city.setText(t.getCity());

            Glide.with(getApplicationContext())
                    .load(t.getPet().getImageUrl())
                    .placeholder(R.drawable.circle_shape)
                    .circleCrop()
                    .into(holder.pet_image);

            holder.ticket.setOnClickListener(view -> {
                db.fetchUserByUid(t.ownerId).addOnSuccessListener(new OnSuccessListener<User>() {
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
            TextView name, sex, city, ed, sd;
            ImageView pet_image;
            LinearLayout ticket;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                sex = itemView.findViewById(R.id.sex);
                city = itemView.findViewById(R.id.city);
                pet_image = itemView.findViewById(R.id.pet_image);
                ticket = itemView.findViewById(R.id.ticket);
                ed = itemView.findViewById(R.id.ed);
                sd = itemView.findViewById(R.id.sd);
            }
        }
    }
}
