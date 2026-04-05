package com.example.carcarego.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.carcarego.R;
import com.example.carcarego.activity.ProductDetailActivity;
import com.example.carcarego.adapter.BannerAdapter;
import com.example.carcarego.adapter.ExploreAdapter;
import com.example.carcarego.adapter.TopTrendingProductsSectionAdapter;
import com.example.carcarego.databinding.FragmentHomeBinding;
import com.example.carcarego.model.Banner;
import com.example.carcarego.model.Detailer;
import com.example.carcarego.model.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;

    private BannerAdapter bannerAdapter;

    private final List<Banner> bannerList = new ArrayList<>();

    private final Handler sliderHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupBannerCarousel();

//        binding.cardBookNow.setOnClickListener(v -> {
//            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
//            if (bottomNav != null) {
//                bottomNav.setSelectedItemId(R.id.bottom_nav_explore);
//            }
//        });

        binding.cardBookNow.setOnClickListener(v -> {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.bottom_nav_explore);
            }

            com.example.carcarego.utils.NotificationHelper.showBookingNotification(
                    getContext(),
                    "Booking Initiated!",
                    "Finish selecting your detailer to confirm your wash."
            );
        });

        loadBannerData();
        loadPopularProducts();
        loadRecommendedDetailers();

        return binding.getRoot();
    }

    private void setupBannerCarousel() {
        bannerAdapter = new BannerAdapter(bannerList);
        binding.viewPagerBanner.setAdapter(bannerAdapter);
        binding.dotsIndicator.setViewPager2(binding.viewPagerBanner);
        setupBannerAutoSlider();
    }


    private void loadBannerData() {
        db.collection("banners")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding == null) return;
                    List<Banner> data = queryDocumentSnapshots.toObjects(Banner.class);
                    bannerList.clear();
                    bannerList.addAll(data);
                    if (bannerAdapter != null) bannerAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Banner load failed", e));
    }


    private void loadPopularProducts() {
        db.collection("products")
                .limit(10)
                .get()
                .addOnSuccessListener(qds -> {
                    if (binding == null || !isAdded()) return;
                    if (!qds.isEmpty()) {
                        List<Product> products = qds.toObjects(Product.class);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                        binding.homePopularProductSection.itemSectionContainer.setLayoutManager(layoutManager);

                        TopTrendingProductsSectionAdapter adapter = new TopTrendingProductsSectionAdapter(products, product -> {
                            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                            intent.putExtra("productId", product.getProductId());
                            startActivity(intent);
                        });

                        binding.homePopularProductSection.itemSectionTitle.setText("Top Trending Products");
                        binding.homePopularProductSection.itemSectionContainer.setAdapter(adapter);
                    }
                });
    }

    private void loadRecommendedDetailers() {
        db.collection("detailers")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding == null || !isAdded()) return;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Detailer> detailerList = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Detailer d = doc.toObject(Detailer.class);
                            if (d != null) {
                                d.setId(doc.getId());
                                detailerList.add(d);
                            }
                        }
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                        binding.layoutRecommendedDetailers.itemSectionContainer.setLayoutManager(layoutManager);
                        binding.layoutRecommendedDetailers.itemSectionTitle.setText("Recommended Detailers");

                        ExploreAdapter adapter = new ExploreAdapter(detailerList);
                        binding.layoutRecommendedDetailers.itemSectionContainer.setAdapter(adapter);
                    } else {
                        binding.layoutRecommendedDetailers.getRoot().setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding != null) binding.layoutRecommendedDetailers.getRoot().setVisibility(View.GONE);
                });
    }

    private void setupBannerAutoSlider() {
        binding.viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding != null && bannerAdapter != null && bannerAdapter.getItemCount() > 1) {
                int nextItem = binding.viewPagerBanner.getCurrentItem() + 1;
                if (nextItem >= bannerAdapter.getItemCount()) nextItem = 0;
                binding.viewPagerBanner.setCurrentItem(nextItem, true);
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bannerList.size() > 1) sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
        binding = null;
    }
}