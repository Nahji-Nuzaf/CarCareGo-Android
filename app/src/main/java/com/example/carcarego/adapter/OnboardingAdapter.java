package com.example.carcarego.adapter; // Make sure this matches your new package name

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.carcarego.fragments.OnboardingFragment1;
import com.example.carcarego.fragments.OnboardingFragment2;
import com.example.carcarego.fragments.OnboardingFragment3;

public class OnboardingAdapter extends FragmentStateAdapter {
    public OnboardingAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new OnboardingFragment1();
            case 1:
                return new OnboardingFragment2();
            case 2:
                return new OnboardingFragment3();
            default:
                return new OnboardingFragment1();
        }
    }
    @Override
    public int getItemCount() {
        return 3;
    }
}