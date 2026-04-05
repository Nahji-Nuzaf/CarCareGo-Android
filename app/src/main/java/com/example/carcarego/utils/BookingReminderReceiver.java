package com.example.carcarego.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BookingReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.showBookingNotification(
                context,
                "Complete Your Purchase!",
                "Don't miss out! Finish your payment now to secure your products."
        );
    }
}