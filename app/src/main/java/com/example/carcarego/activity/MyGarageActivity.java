package com.example.carcarego.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carcarego.R;
import com.example.carcarego.adapter.GarageAdapter;
import com.example.carcarego.databinding.ActivityMyGarageBinding; // Ensure this matches your layout name
import com.example.carcarego.model.Car;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyGarageActivity extends AppCompatActivity {
    private ActivityMyGarageBinding binding;
    private List<Car> carList = new ArrayList<>();
    private GarageAdapter adapter;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyGarageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        adapter = new GarageAdapter(carList);
        binding.rvGarageList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGarageList.setAdapter(adapter);

        binding.fabAddNewCar.setOnClickListener(v -> startActivity(new Intent(this, AddCarActivity.class)));
        binding.btnBackGarage.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGarageData();
    }

    private void loadGarageData() {
        db.collection("users").document(uid).collection("garage")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    carList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Car car = doc.toObject(Car.class);
                        if (car != null) carList.add(car);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}