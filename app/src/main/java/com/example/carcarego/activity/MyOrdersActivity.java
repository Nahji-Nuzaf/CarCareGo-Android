package com.example.carcarego.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.carcarego.adapter.OrderAdapter;
import com.example.carcarego.databinding.ActivityMyOrdersBinding;
import com.example.carcarego.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity {

    private ActivityMyOrdersBinding binding;
    private OrderAdapter adapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        if (uid != null) {
            setupRecyclerView();
            loadOrders();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        adapter = new OrderAdapter(orderList);
        binding.rvMyOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMyOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);

        // Fetching from 'bookings' collection
        db.collection("bookings")
                .whereEqualTo("userId", uid)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    orderList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Order order = doc.toObject(Order.class);
                            if (order != null) {
                                // CRITICAL: Firestore Document ID is the Order ID
                                order.setOrderId(doc.getId());
                                orderList.add(order);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        binding.layoutEmptyOrders.setVisibility(View.GONE);
                    } else {
                        binding.layoutEmptyOrders.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e("FIRESTORE_ERROR", e.getMessage());
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}