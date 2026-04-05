package com.example.carcarego.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.carcarego.databinding.ActivityAddAddressBinding;
import com.example.carcarego.UIUtils; // Our new Snackbar tool
import com.example.carcarego.model.Address;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddAddressActivity extends AppCompatActivity {

    private ActivityAddAddressBinding binding;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        loadExistingAddress();


        binding.btnSaveAddress.setOnClickListener(v -> {
            validateAndSave();
        });

        binding.btnBackAddAddress.setOnClickListener(v -> finish());

    }

    private void validateAndSave() {
        String line1 = binding.etAddressName.getText().toString().trim();
        String line2 = binding.etAddressLine02.getText().toString().trim(); // Optional
        String city = binding.etCity.getText().toString().trim();
        String postal = binding.etPostalCode.getText().toString().trim();

        if (line1.isEmpty()) {
            UIUtils.showCustomSnackbar(binding.getRoot(), "Please enter Address Line 01", false);
            return;
        }

        if (city.isEmpty()) {
            UIUtils.showCustomSnackbar(binding.getRoot(), "Please enter your City", false);
            return;
        }

        if (postal.isEmpty() || postal.length() < 5) {
            UIUtils.showCustomSnackbar(binding.getRoot(), "Please enter a valid Postal Code", false);
            return;
        }

        Address newAddress = new Address(null, line1, line2, city, postal, false);

        saveAddressToFirestore(newAddress);
    }

    private void saveAddressToFirestore(Address address) {
        db.collection("users")
                .document(uid)
                .collection("addresses")
                .add(address)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();
                    documentReference.update("id", generatedId);

                    UIUtils.showCustomSnackbar(binding.getRoot(), "Address Saved Successfully!", true);

                    binding.btnSaveAddress.postDelayed(this::finish, 2000);
                })
                .addOnFailureListener(e -> {
                    UIUtils.showCustomSnackbar(binding.getRoot(), "Error: " + e.getMessage(), false);
                });
    }

    private void loadExistingAddress() {
        db.collection("users")
                .document(uid)
                .collection("addresses")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Address address = queryDocumentSnapshots.getDocuments().get(0).toObject(Address.class);

                        if (address != null) {
                            binding.etAddressName.setText(address.getAddressLine1());
                            binding.etAddressLine02.setText(address.getAddressLine2());
                            binding.etCity.setText(address.getCity());
                            binding.etPostalCode.setText(address.getPostalCode());

                            binding.btnSaveAddress.setText("Update Address");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    UIUtils.showCustomSnackbar(binding.getRoot(), "Failed to load saved address", false);
                });
    }
}