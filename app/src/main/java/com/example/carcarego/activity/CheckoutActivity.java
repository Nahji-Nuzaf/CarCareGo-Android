package com.example.carcarego.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.example.carcarego.utils.BookingReminderReceiver;
import com.example.carcarego.utils.NotificationHelper;
import com.google.firebase.firestore.WriteBatch;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carcarego.UIUtils;
import com.example.carcarego.databinding.ActivityCheckoutBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class CheckoutActivity extends AppCompatActivity {

    private ActivityCheckoutBinding binding;
    private FirebaseFirestore db;
    private String uid;

    private double subtotal = 0.0;
    private double shippingFee = 0.0;
    private double taxAmount = 0.0;
    private double finalTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        subtotal = getIntent().getDoubleExtra("TOTAL_PAYMENT", 0.0);

        loadUserData();

        binding.cbSameAsShipping.setChecked(true);
        binding.layoutBillingDetails.setVisibility(View.GONE);

        binding.cbSameAsShipping.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.layoutBillingDetails.setVisibility(View.GONE);
            } else {
                binding.layoutBillingDetails.setVisibility(View.VISIBLE);
                // Smooth scroll to billing fields
//                binding.nsvCheckout.post(() ->
//                        binding.nsvCheckout.smoothScrollTo(0, binding.layoutBillingDetails.getTop())
//                );
            }
        });

        binding.etCheckoutCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculateTotals();
            }
        });

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnProceed.setOnClickListener(v -> startPayHerePayment());

        calculateTotals();
    }

    private final ActivityResultLauncher<Intent> payHereLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                        PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                        if (response != null && response.isSuccess()) {
                            // Payment Success - Save to Firestore
                            saveOrder();
                        } else {
                            String errorMsg = (response != null && response.getData() != null) ? response.getData().getMessage() : "Payment Failed";
                            UIUtils.showCustomSnackbar(binding.getRoot(), errorMsg, false);
                        }
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    UIUtils.showCustomSnackbar(binding.getRoot(), "Payment Canceled", false);
                }
            }
    );

    private void calculateTotals() {
        String city = binding.etCheckoutCity.getText().toString().trim().toLowerCase();

        if (city.isEmpty()) {
            shippingFee = 0.0;
        } else if (city.equals("colombo")) {
            shippingFee = 400.0;
        } else {
            shippingFee = 300.0;
        }

        taxAmount = subtotal * 0.03;

        finalTotal = subtotal + shippingFee + taxAmount;

        binding.tvSummarySubtotal.setText(String.format(Locale.US, "LKR %,.2f", subtotal));
        binding.tvSummaryShipping.setText(shippingFee > 0 ? String.format(Locale.US, "LKR %,.2f", shippingFee) : "FREE");
        binding.tvSummaryTax.setText(String.format(Locale.US, "LKR %,.2f", taxAmount));
        binding.tvSummaryTotal.setText(String.format(Locale.US, "LKR %,.2f", finalTotal));
    }

    private void loadUserData() {
        db.collection("users").document(uid).get().addOnSuccessListener(ds -> {
            if (ds.exists()) {
                binding.etCheckoutName.setText(ds.getString("name"));
                binding.etCheckoutEmail.setText(ds.getString("email"));
                binding.etCheckoutPhone.setText(ds.getString("mobileNumber"));
            }
        });

        db.collection("users").document(uid).collection("addresses")
                .limit(1)
                .get()
                .addOnSuccessListener(qds -> {
                    if (!qds.isEmpty()) {
                        DocumentSnapshot ad = qds.getDocuments().get(0);
                        binding.etCheckoutAddress1.setText(ad.getString("addressLine1"));
                        binding.etCheckoutAddress2.setText(ad.getString("addressLine2"));
                        binding.etCheckoutCity.setText(ad.getString("city"));
                        binding.etCheckoutPostal.setText(ad.getString("postalCode"));
                        calculateTotals();
                    }
                });
    }

    private void startPayHerePayment() {
        String name = binding.etCheckoutName.getText().toString().trim();
        String email = binding.etCheckoutEmail.getText().toString().trim();
        String phone = binding.etCheckoutPhone.getText().toString().trim();
        String address1 = binding.etCheckoutAddress1.getText().toString().trim();
        String city = binding.etCheckoutCity.getText().toString().trim();
        String zip = binding.etCheckoutPostal.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address1.isEmpty() || city.isEmpty() || zip.isEmpty()) {
            UIUtils.showCustomSnackbar(binding.getRoot(), "Please complete all Shipping Information fields", false);
            return;
        }

        if (!binding.cbSameAsShipping.isChecked()) {
            if (binding.etBillingName.getText().toString().trim().isEmpty() ||
                    binding.etBillingAddress1.getText().toString().trim().isEmpty() ||
                    binding.etBillingCity.getText().toString().trim().isEmpty()) {
                UIUtils.showCustomSnackbar(binding.getRoot(), "Please complete all Billing Information fields", false);
                return;
            }
        }

        InitRequest req = new InitRequest();
        req.setSandBox(true);
        req.setMerchantId("1224184");
        req.setMerchantSecret("MzUzNTg1MzkyMTI5ODM2NTU1MjEzMjcwNzc1ODIzMzcxMzIwNzA4NA==");
        req.setCurrency("LKR");
        req.setAmount(finalTotal);
        req.setOrderId("CCG-" + System.currentTimeMillis());
        req.setItemsDescription("Car Care Go Service");

        req.getCustomer().setFirstName(name);
        req.getCustomer().setLastName("");
        req.getCustomer().setEmail(email);
        req.getCustomer().setPhone(phone);
        req.getCustomer().getAddress().setAddress(address1);
        req.getCustomer().getAddress().setCity(city);
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        payHereLauncher.launch(intent);
    }

//    private void saveOrder() {
//        Map<String, Object> order = new HashMap<>();
//        order.put("orderId", "CCG-" + System.currentTimeMillis());
//        order.put("userId", uid);
//        order.put("subtotal", subtotal);
//        order.put("shippingFee", shippingFee);
//        order.put("taxAmount", taxAmount);
//        order.put("totalAmount", finalTotal);
//        order.put("status", "PAID");
//        order.put("orderDate", Timestamp.now());
//
//        Map<String, Object> shipping = new HashMap<>();
//        shipping.put("name", binding.etCheckoutName.getText().toString().trim());
//        shipping.put("address1", binding.etCheckoutAddress1.getText().toString().trim());
//        shipping.put("address2", binding.etCheckoutAddress2.getText().toString().trim());
//        shipping.put("city", binding.etCheckoutCity.getText().toString().trim());
//        shipping.put("phone", binding.etCheckoutPhone.getText().toString().trim());
//        order.put("shippingAddress", shipping);
//
//        if (binding.cbSameAsShipping.isChecked()) {
//            order.put("billingAddress", shipping);
//        } else {
//            Map<String, Object> billing = new HashMap<>();
//            billing.put("name", binding.etBillingName.getText().toString().trim());
//            billing.put("address1", binding.etBillingAddress1.getText().toString().trim());
//            billing.put("city", binding.etBillingCity.getText().toString().trim());
//            order.put("billingAddress", billing);
//        }
//
//        db.collection("bookings")
//                .add(order)
//                .addOnSuccessListener(doc -> {
//                    UIUtils.showCustomSnackbar(binding.getRoot(), "Booking Confirmed!", true);
//                    // Clear cart would be a great next step here!
//                    binding.btnProceed.postDelayed(this::finish, 2000);
//                })
//                .addOnFailureListener(e -> {
//                    UIUtils.showCustomSnackbar(binding.getRoot(), "Error: " + e.getMessage(), false);
//                });
//    }

//    private void saveOrder() {
//        // 1. Prepare Order Data
//        Map<String, Object> order = new HashMap<>();
//        String orderId = "CCG-" + System.currentTimeMillis();
//        order.put("orderId", orderId);
//        order.put("userId", uid);
//        order.put("subtotal", subtotal);
//        order.put("shippingFee", shippingFee);
//        order.put("taxAmount", taxAmount);
//        order.put("totalAmount", finalTotal);
//        order.put("status", "PAID");
//        order.put("orderDate", Timestamp.now());
//
//        // Addresses
//        Map<String, Object> shipping = new HashMap<>();
//        shipping.put("name", binding.etCheckoutName.getText().toString().trim());
//        shipping.put("address1", binding.etCheckoutAddress1.getText().toString().trim());
//        shipping.put("city", binding.etCheckoutCity.getText().toString().trim());
//        order.put("shippingAddress", shipping);
//        order.put("billingAddress", binding.cbSameAsShipping.isChecked() ? shipping : "Custom Billing Data");
//
//        // 2. Step A: Save the Booking
//        db.collection("bookings")
//                .document(orderId) // Using a specific ID makes it easier to track
//                .set(order)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d("Firestore", "Order Saved Successfully");
//
//                    // 3. Step B: Clear the Cart after saving the order
//                    clearUserCart();
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("Firestore", "Error saving order", e);
//                    UIUtils.showCustomSnackbar(binding.getRoot(), "Database Error: " + e.getMessage(), false);
//                });
//    }

    private void saveOrder() {
        db.collection("users").document(uid).collection("cart").get()
                .addOnSuccessListener(cartSnapshots -> {

                    List<Map<String, Object>> itemsList = new ArrayList<>();

                    for (DocumentSnapshot doc : cartSnapshots) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("productId", doc.getString("productId"));
                        item.put("quantity", doc.getLong("quantity"));
                        // item.put("unitPrice", doc.getDouble("price"));
                        itemsList.add(item);
                    }

                    Map<String, Object> order = new HashMap<>();
                    String orderId = "CCG-" + System.currentTimeMillis();

                    order.put("orderId", orderId);
                    order.put("userId", uid);
                    order.put("items", itemsList);
                    order.put("subtotal", subtotal);
                    order.put("shippingFee", shippingFee);
                    order.put("taxAmount", taxAmount);
                    order.put("totalAmount", finalTotal);
                    order.put("status", "PAID");
                    order.put("orderDate", Timestamp.now());

                    Map<String, Object> shipping = new HashMap<>();
                    shipping.put("name", binding.etCheckoutName.getText().toString().trim());
                    shipping.put("address1", binding.etCheckoutAddress1.getText().toString().trim());
                    shipping.put("address2", binding.etCheckoutAddress2.getText().toString().trim());
                    shipping.put("city", binding.etCheckoutCity.getText().toString().trim());
                    shipping.put("phone", binding.etCheckoutPhone.getText().toString().trim());
                    order.put("shippingAddress", shipping);

                    db.collection("bookings").document(orderId).set(order)
                            .addOnSuccessListener(aVoid -> {
                                clearUserCart();
                            })
                            .addOnFailureListener(e -> {
                                UIUtils.showCustomSnackbar(binding.getRoot(), "Failed to save order", false);
                            });
                })
                .addOnFailureListener(e -> {
                    UIUtils.showCustomSnackbar(binding.getRoot(), "Could not retrieve cart items", false);
                });
    }

    private void clearUserCart() {
        db.collection("users").document(uid).collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit().addOnSuccessListener(aVoid -> {
                        UIUtils.showCustomSnackbar(binding.getRoot(), "Booking Confirmed!", true);
                        NotificationHelper.showBookingNotification(
                                this,
                                "Payment Successful!",
                                "LKR " + finalTotal + " received. Your order is now being processed."
                        );
                        binding.btnProceed.postDelayed(() -> {
                            Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Closes CheckoutActivity
                        }, 2000);
                    });
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error clearing cart", e));
    }

    private void handlePaymentSuccess() {
        NotificationHelper.showBookingNotification(
                this,
                "Payment Successful!",
                "Your car wash has been scheduled. Check the 'Booking' tab for details."
        );

        cancelAbandonmentReminder();

        clearUserCart();

        Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void cancelAbandonmentReminder() {
        Intent intent = new Intent(this, BookingReminderReceiver.class);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                this,
                101,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE | android.app.PendingIntent.FLAG_NO_CREATE
        );

        android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            Log.d("MULTITASKING", "User paid! Abandonment timer cancelled.");
        }
    }
}