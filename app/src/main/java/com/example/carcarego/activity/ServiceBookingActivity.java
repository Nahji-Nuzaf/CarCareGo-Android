package com.example.carcarego.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carcarego.R;
import com.example.carcarego.adapter.ServicePackageAdapter;
import com.example.carcarego.adapter.VehicleSelectionAdapter;
import com.example.carcarego.databinding.ActivityServiceBookingBinding;
import com.example.carcarego.model.Car;
import com.example.carcarego.model.Detailer;
import com.example.carcarego.model.BookingService;
import com.example.carcarego.utils.NotificationHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ServiceBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityServiceBookingBinding binding;
    private Detailer detailer;
    private GoogleMap mMap;
    private LatLng selectedLocation;

    private String bookingDate = "";
    private String bookingTime = "";
    private Car selectedCar;
    private BookingService selectedService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiceBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        detailer = (Detailer) getIntent().getSerializableExtra("DETAILER_DATA");

        setupToolbar();
        setupMapLogic();
        loadUserVehicles();
        loadServicePackages();

        binding.btnSelectDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Service Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                binding.btnSelectDate.setText(datePicker.getHeaderText());
                this.bookingDate = datePicker.getHeaderText();
            });
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        binding.btnSelectTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Service Time")
                    .build();

            timePicker.addOnPositiveButtonClickListener(v1 -> {
                String time = String.format("%02d:%02d", timePicker.getHour(), timePicker.getMinute());
                binding.btnSelectTime.setText(time);
                this.bookingTime = time;
            });
            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });

        binding.btnConfirmBooking.setOnClickListener(v -> submitBooking());
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupMapLogic() {
        if ("MOBILE".equalsIgnoreCase(detailer.getServiceType())) {
            binding.layoutLocationPicker.setVisibility(View.VISIBLE);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapFragment);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        } else {
            binding.layoutLocationPicker.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng dehiwala = new LatLng(6.8511, 79.8640);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dehiwala, 15f));

        mMap.setOnCameraIdleListener(() -> {
            selectedLocation = mMap.getCameraPosition().target;
        });
    }

    private void loadUserVehicles() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("garage")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        binding.rvVehicleSelection.setVisibility(View.GONE);
                        Toast.makeText(this, "No vehicles found in your garage", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Car> carList = queryDocumentSnapshots.toObjects(Car.class);
                    VehicleSelectionAdapter adapter = new VehicleSelectionAdapter(carList, car -> {
                        this.selectedCar = car;
                    });
                    binding.rvVehicleSelection.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    binding.rvVehicleSelection.setAdapter(adapter);
                    binding.rvVehicleSelection.setVisibility(View.VISIBLE);
                });
    }

    private void loadServicePackages() {
        FirebaseFirestore.getInstance().collection("BookingServices")
                .whereEqualTo("detailerId", detailer.getId())
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<BookingService> services = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                        BookingService s = doc.toObject(BookingService.class);
                        if (s != null) {
                            s.setServiceId(doc.getId());
                            services.add(s);
                        }
                    }
                    ServicePackageAdapter adapter = new ServicePackageAdapter(services, service -> {
                        if (service != null) {
                            this.selectedService = service;
                            binding.tvSummaryServiceName.setText(service.getName());
                            binding.tvSummaryPrice.setText("LKR " + String.valueOf(service.getPrice()));
                        }
                    });
                    binding.rvServicePackages.setLayoutManager(new LinearLayoutManager(this));
                    binding.rvServicePackages.setAdapter(adapter);
                });
    }

    private void submitBooking() {
        if (selectedCar == null || selectedService == null || bookingDate.isEmpty() || bookingTime.isEmpty()) {
            Toast.makeText(this, "Please complete all selections", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("MOBILE".equalsIgnoreCase(detailer.getServiceType()) && selectedLocation == null) {
            Toast.makeText(this, "Please pin your location on the map", Toast.LENGTH_LONG).show();
            return;
        }

        java.util.Map<String, Object> booking = new java.util.HashMap<>();
        booking.put("userId", FirebaseAuth.getInstance().getUid());
        booking.put("vehicleModel", selectedCar.getBrandModel());
        booking.put("vehiclePlate", selectedCar.getLicensePlate());
        booking.put("serviceName", selectedService.getName());
        booking.put("servicePrice", selectedService.getPrice());
        booking.put("duration", selectedService.getDuration());
        booking.put("bookingDate", bookingDate);
        booking.put("bookingTime", bookingTime);
        booking.put("detailerId", detailer.getId());
        booking.put("detailerName", detailer.getName());
        booking.put("serviceType", detailer.getServiceType());

        if ("MOBILE".equalsIgnoreCase(detailer.getServiceType())) {
            if (selectedLocation != null) {
                booking.put("latitude", selectedLocation.latitude);
                booking.put("longitude", selectedLocation.longitude);
            }
        } else {
            if (detailer.getLatitude() != null && detailer.getLongitude() != null) {
                booking.put("latitude", detailer.getLatitude());
                booking.put("longitude", detailer.getLongitude());
            }
        }

        booking.put("instructions", binding.etInstructions.getText().toString().trim());
        booking.put("status", "PENDING");
        booking.put("timestamp", com.google.firebase.Timestamp.now());

        FirebaseFirestore.getInstance()
                .collection("ServiceBookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::showSuccessDialog, 2000);
                    NotificationHelper.showBookingNotification(
                            ServiceBookingActivity.this,
                            "Booking Sent!",
                            "Your service request is pending. The detailer will confirm shortly."
                    );
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showSuccessDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_booking_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        android.widget.Button btnDone = dialog.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        dialog.show();
    }
}