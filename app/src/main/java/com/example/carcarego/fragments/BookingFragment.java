package com.example.carcarego.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carcarego.adapter.MyBookingsAdapter;
import com.example.carcarego.databinding.FragmentBookingBinding;
import com.example.carcarego.model.MyBookingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookingFragment extends Fragment {

    private FragmentBookingBinding binding;
    private MyBookingsAdapter upcomingAdapter;
    private MyBookingsAdapter pastAdapter;
    private List<MyBookingService> upcomingList = new ArrayList<>();
    private List<MyBookingService> pastList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookingBinding.inflate(inflater, container, false);

        setupRecyclerViews();
        loadUserBookings();

        return binding.getRoot();
    }

    private void setupRecyclerViews() {
        upcomingAdapter = new MyBookingsAdapter(upcomingList, getContext());
        binding.rvUpcomingBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUpcomingBookings.setAdapter(upcomingAdapter);

        pastAdapter = new MyBookingsAdapter(pastList, getContext());
        binding.rvPastBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPastBookings.setAdapter(pastAdapter);
    }

    private void loadUserBookings() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.util.Date today = calendar.getTime();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());

        FirebaseFirestore.getInstance().collection("ServiceBookings")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    upcomingList.clear();
                    pastList.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        MyBookingService booking = doc.toObject(MyBookingService.class);
                        booking.setBookingId(doc.getId());

                        try {
                            java.util.Date bDate = sdf.parse(booking.getBookingDate());
                            String status = booking.getStatus();

                            if ("COMPLETED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
                                pastList.add(booking);
                            }
                            else if (bDate != null && bDate.before(today) && "PENDING".equalsIgnoreCase(status)) {
                                booking.setStatus("EXPIRED");

                                pastList.add(booking);
                            }

                            else {
                                upcomingList.add(booking);
                            }

                        } catch (Exception e) {
                            upcomingList.add(booking);
                        }
                    }

                    upcomingAdapter.notifyDataSetChanged();
                    pastAdapter.notifyDataSetChanged();
                });
    }

    private void toggleEmptyState() {
        if (upcomingList.isEmpty() && pastList.isEmpty()) {
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}