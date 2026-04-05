package com.example.carcarego.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carcarego.R;
import com.example.carcarego.adapter.ExploreAdapter;
import com.example.carcarego.databinding.FragmentExploreBinding;
import com.example.carcarego.model.Detailer;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private ExploreAdapter adapter;
    private List<Detailer> allDetailers;
    private List<Detailer> filteredList;
    private FirebaseFirestore db;
    private String currentFilter = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        setupFilters();
        loadDetailers();
    }

    private void setupRecyclerView() {
        allDetailers = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ExploreAdapter(filteredList);
        binding.rvExploreDetailers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvExploreDetailers.setAdapter(adapter);
    }

    private void loadDetailers() {
        db.collection("detailers").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allDetailers.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Detailer detailer = doc.toObject(Detailer.class);
                if (detailer != null) {
                    detailer.setId(doc.getId());
                    allDetailers.add(detailer);
                }
            }
            applyFiltersAndSearch();
        }).addOnFailureListener(e -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilters() {
        binding.chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipMobile) {
                currentFilter = "MOBILE";
            } else if (checkedId == R.id.chipStation) {
                currentFilter = "STATION";
            } else {
                currentFilter = "All";
            }
            applyFiltersAndSearch();
        });
    }

//    private void setupSearch() {
//        if (binding.etSearch != null) {
//            binding.etSearch.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    applyFiltersAndSearch();
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {}
//            });
//        }
//    }


    private void applyFiltersAndSearch() {
        String query = "";
//        if (binding.etSearch != null) {
//            query = binding.etSearch.getText().toString().toLowerCase().trim();
//        }

        filteredList.clear();
        for (Detailer d : allDetailers) {
            boolean matchesFilter = currentFilter.equals("All") || d.getServiceType().equalsIgnoreCase(currentFilter);
            boolean matchesSearch = d.getName().toLowerCase().contains(query) ||
                    d.getCity().toLowerCase().contains(query);

            if (matchesFilter && matchesSearch) {
                filteredList.add(d);
            }
        }
        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            // binding.tvNoResults.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}