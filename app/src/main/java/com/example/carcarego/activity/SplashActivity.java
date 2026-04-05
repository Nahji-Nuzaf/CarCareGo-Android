package com.example.carcarego.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carcarego.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            View progressBar = findViewById(R.id.splashProgressBar);
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        }, 1000);

        new Handler(Looper.getMainLooper()).postDelayed(this::routeUser, 3000);
    }

    private void routeUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences preferences = getSharedPreferences("CarCareGoPrefs", MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean("isFirstTime", true);

        Intent intent;

        if (currentUser != null) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else if (!isFirstTime) {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}