package com.example.carcarego.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class BatteryLevelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        if (level <= 5) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Battery Warning")
                    .setMessage("Battery is at " + level + "%. Finish your booking!")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}