package com.example.vetpet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PetISit extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_i_sit);

        //Back
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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