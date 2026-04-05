package com.example.carcarego.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carcarego.activity.ProductDetailActivity;
import com.example.carcarego.adapter.CategoryAdapter;
import com.example.carcarego.adapter.ListingAdapter;
import com.example.carcarego.databinding.FragmentShopBinding;
import com.example.carcarego.model.Category;
import com.example.carcarego.model.Product;
import com.example.carcarego.utils.DatabaseHelper;
import com.example.carcarego.utils.NetworkChangeReceiver;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment {

    private FragmentShopBinding binding;
    private ListingAdapter listingAdapter;
    private FirebaseFirestore db;
    private NetworkChangeReceiver networkReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("SHOP_QUERY", this, (requestKey, bundle) -> {
            String query = bundle.getString("search_query");
            if (query != null) {
                searchProductsInFirestore(query);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvShopProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        loadCategories();
        loadProducts("All");
    }

    private void searchProductsInFirestore(String query) {
        if (db == null) db = FirebaseFirestore.getInstance();

        if (getContext() != null) {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            dbHelper.addSearchQuery(query);
            Log.d("SQLITE_STORAGE", "Saved to SQLite: " + query);
        }

        db.collection("products")
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(qds -> {
                    if (binding == null || !isAdded()) return;

                    List<Product> filteredList = qds.toObjects(Product.class);
                    Log.d("SEARCH_DEBUG", "Found products: " + filteredList.size());

                    if (listingAdapter != null) {
                        listingAdapter.updateList(filteredList);
                    } else {
                        listingAdapter = new ListingAdapter(filteredList, this::openProductDetail);
                        binding.rvShopProducts.setAdapter(listingAdapter);
                    }
                })
                .addOnFailureListener(e -> Log.e("SEARCH_ERROR", "Firestore search failed: " + e.getMessage()));
    }

    private void loadCategories() {
        db.collection("categories").get().addOnCompleteListener(task -> {
            if (binding == null || !task.isSuccessful()) return;
            List<Category> categories = new ArrayList<>();
            categories.add(new Category("all_id", "All"));
            categories.addAll(task.getResult().toObjects(Category.class));
            CategoryAdapter adapter = new CategoryAdapter(categories, this::loadProducts);
            binding.rvCategories.setAdapter(adapter);
        });
    }

    private void loadProducts(String categoryName) {
        Query query;
        CollectionReference productsRef = db.collection("products");
        if (categoryName.equalsIgnoreCase("All")) {
            query = productsRef.orderBy("title", Query.Direction.ASCENDING);
        } else {
            query = productsRef.whereEqualTo("categoryId", categoryName).orderBy("title", Query.Direction.ASCENDING);
        }

        query.get().addOnSuccessListener(qds -> {
            if (binding == null) return;
            List<Product> products = qds.toObjects(Product.class);
            listingAdapter = new ListingAdapter(products, this::openProductDetail);
            binding.rvShopProducts.setAdapter(listingAdapter);
        });
    }

    private void openProductDetail(Product product) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("productId", product.getProductId());
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (networkReceiver == null) {
            networkReceiver = new NetworkChangeReceiver(() -> {
                if (isAdded()) {
                    loadProducts("All");
                    Toast.makeText(getContext(), "Connection Restored! Refreshing Shop...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireActivity().registerReceiver(networkReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (networkReceiver != null) {
                requireActivity().unregisterReceiver(networkReceiver);
            }
        } catch (IllegalArgumentException e) {
            Log.e("NETWORK_ERROR", "Receiver was already unregistered");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}