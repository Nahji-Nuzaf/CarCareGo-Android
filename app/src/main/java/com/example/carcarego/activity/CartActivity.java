package com.example.carcarego.activity;

import android.content.Intent;
import android.net.Uri; // Added for Telephony
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carcarego.adapter.CartAdapter;
import com.example.carcarego.databinding.ActivityCartBinding;
import com.example.carcarego.model.CartItem;
import com.example.carcarego.model.Product;
import com.example.carcarego.utils.BookingReminderReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding binding;
    private List<CartItem> cartItems;
    private CartAdapter adapter;
    private FirebaseFirestore db;
    private String uid;

    private double finalTotalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            uid = auth.getCurrentUser().getUid();
            loadCartItems();
        } else {
            Toast.makeText(this, "Please login to view cart", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.btnCheckout.setOnClickListener(v -> {
            if (cartItems != null && !cartItems.isEmpty()) {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putExtra("TOTAL_PAYMENT", finalTotalAmount);
                startActivity(intent);

                scheduleBookingReminder();
            } else {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            }
        });

        if (binding.btnCartBack != null) {
            binding.btnCartBack.setOnLongClickListener(v -> {
                makeSupportCall();
                return true;
            });
        }

        binding.btnEmptyShopNow.setOnClickListener(v -> finish());
        binding.btnCartBack.setOnClickListener(v -> finish());
    }

    private void makeSupportCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:0771234567")); // Replace with actual support number
        startActivity(intent);
    }

    private void loadCartItems() {
        db.collection("users").document(uid).collection("cart")
                .get()
                .addOnSuccessListener(qds -> {
                    cartItems = new ArrayList<>();
                    if (!qds.isEmpty()) {
                        for (DocumentSnapshot ds : qds.getDocuments()) {
                            CartItem item = ds.toObject(CartItem.class);
                            if (item != null) {
                                item.setDocumentId(ds.getId());
                                cartItems.add(item);
                            }
                        }
                        setupRecyclerView();
                        checkEmptyState();
                    } else {
                        updateTotalUI(0, 0);
                        checkEmptyState();
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading cart", e));
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(cartItems);
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(adapter);

        adapter.setOnQuantityChangeListener(cartItem -> {
            if (cartItem.getDocumentId() != null) {
                db.collection("users").document(uid)
                        .collection("cart")
                        .document(cartItem.getDocumentId())
                        .update("quantity", cartItem.getQuantity())
                        .addOnSuccessListener(aVoid -> updateTotal());
            }
        });

        adapter.setOnRemoveListener(position -> {
            String docId = cartItems.get(position).getDocumentId();
            db.collection("users").document(uid)
                    .collection("cart")
                    .document(docId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        cartItems.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, cartItems.size());
                        checkEmptyState();
                        updateTotal();
                        Toast.makeText(this, "Removed from cart", Toast.LENGTH_SHORT).show();
                    });
        });

        updateTotal();
    }

    private void updateTotal() {
        if (cartItems == null || cartItems.isEmpty()) {
            updateTotalUI(0, 0);
            return;
        }

        List<String> productIds = new ArrayList<>();
        for (CartItem item : cartItems) {
            productIds.add(item.getProductId());
        }

        db.collection("products")
                .whereIn("productId", productIds)
                .get()
                .addOnSuccessListener(qds -> {
                    double totalAmount = 0;
                    int totalItemsCount = 0;

                    Map<String, Double> priceMap = new HashMap<>();
                    for (DocumentSnapshot ds : qds.getDocuments()) {
                        Product p = ds.toObject(Product.class);
                        if (p != null) {
                            priceMap.put(p.getProductId(), p.getPrice());
                        }
                    }

                    for (CartItem item : cartItems) {
                        Double price = priceMap.get(item.getProductId());
                        if (price != null) {
                            totalAmount += (price * item.getQuantity());
                            totalItemsCount += item.getQuantity();
                        }
                    }

                    updateTotalUI(totalAmount, totalItemsCount);
                });
    }

    private void updateTotalUI(double total, int count) {
        finalTotalAmount = total;
        binding.tvCartTotal.setText(String.format(Locale.US, "LKR %,.2f", total));
        binding.tvCartItemToatl.setText(String.valueOf(count));
    }

    private void checkEmptyState() {
        if (cartItems == null || cartItems.isEmpty()) {
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.rvCartItems.setVisibility(View.GONE);
            binding.layoutCheckoutBottom.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyCart.setVisibility(View.GONE);
            binding.rvCartItems.setVisibility(View.VISIBLE);
            binding.layoutCheckoutBottom.setVisibility(View.VISIBLE);
        }
    }

    private void scheduleBookingReminder() {
        Intent intent = new Intent(this, BookingReminderReceiver.class);

        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                this,
                0,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE | android.app.PendingIntent.FLAG_UPDATE_CURRENT
        );

        android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(android.content.Context.ALARM_SERVICE);

        long triggerTime = System.currentTimeMillis() + (20 * 1000);

        if (alarmManager != null) {
            alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Log.d("ALARM_DEBUG", "Multitasking: Abandonment timer scheduled.");
        }
    }
}