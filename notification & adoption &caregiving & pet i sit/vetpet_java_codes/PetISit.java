package com.example.vetpet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class PetISit extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_i_sit);
    }

    class PetsAdapter extends RecyclerView.Adapter<PetsAdapter.PetsViewHolder> {

        ArrayList<Pet> petsArray = new ArrayList<>();

        public PetsAdapter(ArrayList<Pet> petsArray) {
            this.petsArray = petsArray;
        }

        @NonNull
        @Override
        public PetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PetsViewHolder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_pet_sit, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PetsViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return petsArray.size();
        }

        class PetsViewHolder extends RecyclerView.ViewHolder {

            public PetsViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}