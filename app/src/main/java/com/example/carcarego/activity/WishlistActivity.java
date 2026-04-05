package com.example.carcarego.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carcarego.adapter.WishlistAdapter;
import com.example.carcarego.databinding.ActivityWishlistBinding;
import com.example.carcarego.model.WishlistItem;
import com.example.carcarego.utils.ShakeDetector; // Make sure this helper class exists
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private ActivityWishlistBinding binding;
    private List<WishlistItem> wishlistItems;
    private WishlistAdapter adapter;
    private FirebaseFirestore db;
    private String uid;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWishlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mShakeDetector = new ShakeDetector();

            mShakeDetector.setOnShakeListener(count -> {
                if (wishlistItems != null && !wishlistItems.isEmpty()) {
                    showClearWishlistDialog();
                }
            });
        }

        if (auth.getCurrentUser() != null) {
            uid = auth.getCurrentUser().getUid();
            loadWishlist();
        } else {
            Toast.makeText(this, "Please login to view wishlist", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.btnWishBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager != null && mAccelerometer != null) {
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mShakeDetector);
        }
        super.onPause();
    }

    private void showClearWishlistDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Shake Detected!")
                .setMessage("Would you like to clear your entire wishlist?")
                .setPositiveButton("Clear All", (dialog, which) -> clearEntireWishlist())
                .setNegativeButton("Keep Items", null)
                .show();
    }

    private void clearEntireWishlist() {
        WriteBatch batch = db.batch();

        for (WishlistItem item : wishlistItems) {
            batch.delete(db.collection("users").document(uid)
                    .collection("wishlist").document(item.getDocumentId()));
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            wishlistItems.clear();
            adapter.notifyDataSetChanged();
            checkEmptyState();
            Toast.makeText(this, "Wishlist cleared!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to clear", Toast.LENGTH_SHORT).show());
    }

    private void loadWishlist() {
        db.collection("users").document(uid).collection("wishlist")
                .get()
                .addOnSuccessListener(qds -> {
                    wishlistItems = new ArrayList<>();
                    if (!qds.isEmpty()) {
                        for (DocumentSnapshot ds : qds.getDocuments()) {
                            WishlistItem item = ds.toObject(WishlistItem.class);
                            if (item != null) {
                                item.setDocumentId(ds.getId());
                                wishlistItems.add(item);
                            }
                        }
                        setupRecyclerView();
                    }
                    checkEmptyState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupRecyclerView() {
        adapter = new WishlistAdapter(wishlistItems, new WishlistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(WishlistItem item) {
                Intent intent = new Intent(WishlistActivity.this, ProductDetailActivity.class);
                intent.putExtra("productId", item.getProductId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(WishlistItem item, int position) {
                db.collection("users").document(uid).collection("wishlist")
                        .document(item.getDocumentId()).delete()
                        .addOnSuccessListener(aVoid -> {
                            wishlistItems.remove(position);
                            adapter.notifyItemRemoved(position);
                            adapter.notifyItemRangeChanged(position, wishlistItems.size());
                            checkEmptyState();
                        });
            }
        });

        binding.rvWishlistItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvWishlistItems.setAdapter(adapter);
    }

    private void checkEmptyState() {
        if (wishlistItems == null || wishlistItems.isEmpty()) {
            binding.layoutEmptyWishlist.setVisibility(View.VISIBLE);
            binding.rvWishlistItems.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyWishlist.setVisibility(View.GONE);
            binding.rvWishlistItems.setVisibility(View.VISIBLE);
        }
    }
}