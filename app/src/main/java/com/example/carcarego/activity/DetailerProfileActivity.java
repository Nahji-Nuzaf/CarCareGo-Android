package com.example.carcarego.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.carcarego.R;
import com.example.carcarego.adapter.ExploreAdapter;
import com.example.carcarego.databinding.ActivityDetailerProfileBinding;
import com.example.carcarego.model.Detailer;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class DetailerProfileActivity extends AppCompatActivity {
    private ActivityDetailerProfileBinding binding;
    private Detailer detailer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String json = getIntent().getStringExtra("DETAILER_DATA");
        if (json != null) {
            detailer = new Gson().fromJson(json, Detailer.class);
            setupUI();
            loadRecommendedDetailers();
        } else {
            Toast.makeText(this, "Error: Data not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(this, ServiceBookingActivity.class);
            intent.putExtra("DETAILER_DATA", detailer); // Make sure Detailer implements Serializable
            startActivity(intent);
        });
    }

    private void setupUI() {
        binding.tvProfileName.setText(detailer.getName());
        binding.tvProfileLocation.setText("📍 " + detailer.getCity() + ", Sri Lanka");
        binding.tvDescription.setText(detailer.getDescription());
        binding.tvProviderName.setText(detailer.getProviderName());

        Glide.with(this)
                .load(detailer.getImage())
                .placeholder(R.drawable.placeholder_image)
                .into(binding.ivHeader);

        binding.btnCallProvider.setOnClickListener(v -> makeCall());
        binding.btnCall.setOnClickListener(v -> makeCall());

        binding.btnMessageProvider.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + detailer.getPhone()));
            intent.putExtra("sms_body", "Hi " + detailer.getName() + ", I'm interested in your car wash service.");
            startActivity(intent);
        });

        binding.btnViewOnMap.setOnClickListener(v -> {
            String mapUri = "geo:0,0?q=" + Uri.encode(detailer.getAddress() + ", " + detailer.getCity());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUri));
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                String webUri = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(detailer.getAddress());
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUri)));
            }
        });

        binding.btnBook.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Booking Sheet...", Toast.LENGTH_SHORT).show();
        });
    }

    private void makeCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + detailer.getPhone()));
        startActivity(intent);
    }

    private void loadRecommendedDetailers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String currentDetailerId = detailer.getId();

        db.collection("detailers")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Detailer> recommendedList = new ArrayList<>();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Detailer d = doc.toObject(Detailer.class);
                            if (d != null) {
                                d.setId(doc.getId());

                                if (!d.getId().equals(currentDetailerId)) {
                                    recommendedList.add(d);
                                }
                            }
                        }

                        LinearLayoutManager layoutManager = new LinearLayoutManager(
                                DetailerProfileActivity.this,
                                LinearLayoutManager.HORIZONTAL,
                                false
                        );

                        binding.layoutRecommendedDetailers.itemSectionContainer.setLayoutManager(layoutManager);
                        binding.layoutRecommendedDetailers.itemSectionTitle.setText("Recommended for You");

                        ExploreAdapter adapter = new ExploreAdapter(recommendedList);
                        binding.layoutRecommendedDetailers.itemSectionContainer.setAdapter(adapter);

                    } else {
                        binding.layoutRecommendedDetailers.getRoot().setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.layoutRecommendedDetailers.getRoot().setVisibility(View.GONE);
                });
    }



}