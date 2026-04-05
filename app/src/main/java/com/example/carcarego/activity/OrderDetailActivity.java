package com.example.carcarego.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.carcarego.adapter.OrderDetailAdapter;
import com.example.carcarego.databinding.ActivityOrderDetailBinding;
import com.example.carcarego.model.Order;
import com.google.gson.Gson;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private ActivityOrderDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Receive the Order object passed from the list
        String orderJson = getIntent().getStringExtra("ORDER_DATA");

        if (orderJson != null) {
            Order order = new Gson().fromJson(orderJson, Order.class);
            if (order != null) {
                setupUI(order);
            }
        }

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupUI(Order order) {
        // Header
        if (order.getOrderId() != null) {
            binding.tvDetailOrderId.setText("Order #" + order.getOrderId().substring(0, 8).toUpperCase());
        }

        // Shipping Info (Maps to your ShippingAddress inner class)
        if (order.getShippingAddress() != null) {
            String addressText = order.getShippingAddress().getName() + "\n" +
                    order.getShippingAddress().getAddress1() + ", " +
                    order.getShippingAddress().getCity();
            binding.tvDetailAddress.setText(addressText);
        }

        // Summary
        binding.tvDetailTotal.setText(String.format(Locale.US, "LKR %,.2f", order.getTotalAmount()));

        // Setup the Specialized Detail Adapter
        OrderDetailAdapter adapter = new OrderDetailAdapter(order.getItems());
        binding.rvOrderProduct.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrderProduct.setAdapter(adapter);
    }
}