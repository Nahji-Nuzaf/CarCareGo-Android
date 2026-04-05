package com.example.carcarego.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.carcarego.R;
import com.example.carcarego.databinding.ActivityMainBinding;
import com.example.carcarego.fragments.BookingFragment;
import com.example.carcarego.fragments.ExploreFragment;
import com.example.carcarego.fragments.HomeFragment;
import com.example.carcarego.fragments.MessageFragment;
import com.example.carcarego.fragments.ProfileFragment;
import com.example.carcarego.fragments.SettingsFragment;
import com.example.carcarego.fragments.ShopFragment;
import com.example.carcarego.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.Manifest;
import android.os.Build;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        drawerLayout = binding.drawerLayout;
        navigationView = binding.navigationView;
        bottomNavigationView = binding.bottomNavigation;
        ImageButton btnMenu = binding.btnMenuDrawer;

        checkInternetConnection();

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        binding.etHomeSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = binding.etHomeSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                routeSearchQuery(query);
                hideKeyboard();
            }
            return true;
        });

        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedName = prefs.getString("user_name", "User"); // "User" is the default if empty

        binding.tvUserNameHome.setText(savedName);

        updateUserInfo();
    }

    private void updateUserInfo() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(ds -> {
                        if (ds.exists()) {
                            User user = ds.toObject(User.class);
                            binding.tvUserNameHome.setText(user.getName());
                        }
                    });

            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.side_nav_login).setVisible(false);
            menu.findItem(R.id.side_nav_cart).setVisible(true);
            menu.findItem(R.id.side_nav_wishlist).setVisible(true);
            menu.findItem(R.id.side_nav_orders).setVisible(true);
            menu.findItem(R.id.side_nav_chat).setVisible(true);
            menu.findItem(R.id.side_nav_logout).setVisible(true);
        } else {
            binding.tvUserNameHome.setText("Guest User");
        }
    }

    private void routeSearchQuery(String query) {
        List<String> serviceKeywords = Arrays.asList("wash", "detail", "service", "polish", "wax", "clean");
        boolean isServiceSearch = false;
        String lowerQuery = query.toLowerCase();

        for (String key : serviceKeywords) {
            if (lowerQuery.contains(key)) {
                isServiceSearch = true;
                break;
            }
        }

        if (isServiceSearch) {
            bottomNavigationView.setSelectedItemId(R.id.bottom_nav_explore);
            new Handler(Looper.getMainLooper()).postDelayed(() -> sendSearchToFragment("EXPLORE_QUERY", query), 200);
        } else {
            bottomNavigationView.setSelectedItemId(R.id.bottom_nav_shop);
            new Handler(Looper.getMainLooper()).postDelayed(() -> sendSearchToFragment("SHOP_QUERY", query), 200);
        }
    }

    private void sendSearchToFragment(String key, String query) {
        Bundle bundle = new Bundle();
        bundle.putString("search_query", query);
        getSupportFragmentManager().setFragmentResult(key, bundle);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        Menu navMenu = navigationView.getMenu();
        Menu bottomNavMenu = bottomNavigationView.getMenu();
        for (int i = 0; i < navMenu.size(); i++) navMenu.getItem(i).setChecked(false);
        for (int i = 0; i < bottomNavMenu.size(); i++) bottomNavMenu.getItem(i).setChecked(false);

        if (itemId == R.id.side_nav_wishlist) {
            startActivity(new Intent(MainActivity.this, WishlistActivity.class));
        } else if (itemId == R.id.side_nav_cart) {
            startActivity(new Intent(MainActivity.this, CartActivity.class));
        } else if (itemId == R.id.side_nav_orders) {
            startActivity(new Intent(MainActivity.this, MyOrdersActivity.class));
        } else if (itemId == R.id.side_nav_chat) {
            loadFragment(new MessageFragment());
        } else if (itemId == R.id.side_nav_settings) {
            loadFragment(new SettingsFragment());
        } else if (itemId == R.id.side_nav_login) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else if (itemId == R.id.side_nav_logout) {
            firebaseAuth.signOut();
            loadFragment(new HomeFragment());
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.side_nav_menu);
            binding.tvUserNameHome.setText("Guest User");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else if (itemId == R.id.bottom_nav_home) {
            loadFragment(new HomeFragment());
        } else if (itemId == R.id.bottom_nav_explore) {
            loadFragment(new ExploreFragment());
        } else if (itemId == R.id.bottom_nav_booking) {
            loadFragment(new BookingFragment());
        } else if (itemId == R.id.bottom_nav_shop) {
            loadFragment(new ShopFragment());
        } else if (itemId == R.id.bottom_nav_profile) {
            if (firebaseAuth.getCurrentUser() == null) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            } else {
                loadFragment(new ProfileFragment());
            }
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
            showOfflineDialog();
        }
    }

    private void showOfflineDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("No Internet Connection")
                .setMessage("CarCareGo requires an active connection. Please check your settings.")
                .setPositiveButton("Retry", (dialog, which) -> checkInternetConnection())
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}