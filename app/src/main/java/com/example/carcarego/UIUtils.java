package com.example.carcarego; // Make sure this matches your folder structure

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.carcarego.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class UIUtils {

    public static void showCustomSnackbar(View view, String message, boolean isSuccess) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View snackView = snackbar.getView();

        snackView.setBackgroundResource(isSuccess ? R.drawable.snackbar_success_bg : R.drawable.snackbar_error_bg);

        ViewGroup.LayoutParams params = snackView.getLayoutParams();
        if (params instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams fParams = (FrameLayout.LayoutParams) params;
            fParams.gravity = android.view.Gravity.TOP;
            fParams.setMargins(60, 100, 60, 0);
            snackView.setLayoutParams(fParams);
        } else if (params instanceof androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) {
            androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams cParams =
                    (androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) params;
            cParams.gravity = android.view.Gravity.TOP;
            cParams.setMargins(60, 100, 60, 0);
            snackView.setLayoutParams(cParams);
        }

        android.widget.TextView tv = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (tv != null) {
            int iconRes = isSuccess ? R.drawable.baseline_check_circle_24 : R.drawable.outline_error_24;
            tv.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
            tv.setCompoundDrawablePadding(28);
            tv.setTextColor(isSuccess ? android.graphics.Color.parseColor("#1E40AF") : android.graphics.Color.WHITE);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        snackbar.setAnimationMode(com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE);

        snackbar.show();
    }
}