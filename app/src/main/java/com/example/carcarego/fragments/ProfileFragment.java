package com.example.carcarego.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.carcarego.R;
import com.example.carcarego.activity.AddAddressActivity;
import com.example.carcarego.activity.EditProfileActivity;
import com.example.carcarego.activity.MyGarageActivity;
import com.example.carcarego.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            loadUserProfile(auth.getCurrentUser().getUid());
        }

        setupMenuClickListeners();

    }

    private void loadUserProfile(String uid) {
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String profilePicUrl = documentSnapshot.getString("profilePicUrl");

                binding.tvName.setText(name != null ? name : "User Name");
                binding.tvEmail.setText(email != null ? email : "No Email Provided");

                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                    try {
                        byte[] decodedString = android.util.Base64.decode(profilePicUrl, android.util.Base64.DEFAULT);

                        Glide.with(requireContext())
                                .asBitmap()
                                .load(decodedString)
                                .placeholder(R.drawable.account_circle_24px)
                                .error(R.drawable.account_circle_24px)
                                .into(binding.ivProfileImg);
                    } catch (Exception e) {
                        binding.ivProfileImg.setImageResource(R.drawable.account_circle_24px);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            if(isAdded()) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMenuClickListeners() {
        binding.cvEditYourProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        binding.cvManageAddress.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddAddressActivity.class);
            startActivity(intent);
        });

        binding.cvMyGarage.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyGarageActivity.class);
            startActivity(intent);
        });

        binding.cvSettings.setOnClickListener(v -> {
//            Intent intent = new Intent(getActivity(), SettingsFragment.class);
//            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}