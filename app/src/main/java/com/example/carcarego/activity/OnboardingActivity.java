package com.example.carcarego.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.viewpager2.widget.ViewPager2;

import com.example.carcarego.R;
import com.example.carcarego.adapter.OnboardingAdapter;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        TextView btnSkip = findViewById(R.id.btnSkip);
        MaterialButton btnNext = findViewById(R.id.btnNext);
        ConstraintLayout layoutBottom = findViewById(R.id.layoutBottom);

        OnboardingAdapter adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(layoutBottom);

                if (position == 2) { // Last Page
                    btnSkip.setVisibility(View.GONE);
                    btnNext.setText("Get Started");

                    constraintSet.clear(R.id.btnNext, ConstraintSet.START);
                    constraintSet.connect(R.id.btnNext, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                    constraintSet.connect(R.id.btnNext, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                } else {
                    btnSkip.setVisibility(View.VISIBLE);
                    btnNext.setText("Continue");

                    constraintSet.clear(R.id.btnNext, ConstraintSet.START);
                    constraintSet.clear(R.id.btnNext, ConstraintSet.END);
                    constraintSet.connect(R.id.btnNext, ConstraintSet.START, R.id.btnSkip, ConstraintSet.END, 32);
                    constraintSet.connect(R.id.btnNext, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                }
                constraintSet.applyTo(layoutBottom);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 2) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                markOnboardingComplete();
            }
        });

        btnSkip.setOnClickListener(v -> markOnboardingComplete());
    }

    private void markOnboardingComplete() {
        SharedPreferences preferences = getSharedPreferences("CarCareGoPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstTime", false);
        editor.apply();

        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}