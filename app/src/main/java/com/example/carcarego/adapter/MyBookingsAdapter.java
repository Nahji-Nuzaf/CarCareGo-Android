package com.example.carcarego.adapter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carcarego.R;
import com.example.carcarego.databinding.ItemMyBookingsCardBinding;
import com.example.carcarego.model.MyBookingService; // Ensure this is your new Model

import java.util.List;

public class MyBookingsAdapter extends RecyclerView.Adapter<MyBookingsAdapter.ViewHolder> {

    private List<MyBookingService> bookings;
    private Context context;

    public MyBookingsAdapter(List<MyBookingService> bookings, Context context) {
        this.bookings = bookings;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyBookingsCardBinding binding = ItemMyBookingsCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyBookingService booking = bookings.get(position);

        holder.binding.tvServiceName.setText(booking.getServiceName());
        holder.binding.tvPrice.setText("LKR " + booking.getServicePrice());
        holder.binding.tvVehicleInfo.setText(booking.getVehicleModel() + " • " + booking.getVehiclePlate());
        holder.binding.tvDate.setText(booking.getBookingDate() + " @ " + booking.getBookingTime());

        String status = booking.getStatus();
        holder.binding.tvStatus.setText(status);

        if ("EXPIRED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            holder.binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#EF4444")); // Modern Red
            holder.binding.tvStatus.setText("CANCELLED/EXPIRED");
            holder.itemView.setAlpha(0.8f);
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            holder.binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981")); // Emerald Green
            holder.itemView.setAlpha(1.0f);
        } else {
            holder.binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#3B82F6")); // Electric Blue
            holder.itemView.setAlpha(1.0f);
        }

        if ("COMPLETED".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            holder.binding.btnDirections.setVisibility(View.GONE);
            holder.binding.btnTrackLive.setVisibility(View.GONE);
        } else {
            if ("STATION".equalsIgnoreCase(booking.getServiceType())) {
                holder.binding.btnDirections.setVisibility(View.VISIBLE);
                holder.binding.btnTrackLive.setVisibility(View.GONE);

                holder.binding.btnDirections.setOnClickListener(v -> {
                    if (booking.getLatitude() != null && booking.getLongitude() != null) {

                        android.content.Intent intent = new android.content.Intent(context, com.example.carcarego.activity.DirectionsActivity.class);

                        intent.putExtra("dest_lat", booking.getLatitude());
                        intent.putExtra("dest_lng", booking.getLongitude());
                        intent.putExtra("station_name", booking.getDetailerName());

                        context.startActivity(intent);

                        sendNotification("Navigation Started", "Heading to " + booking.getDetailerName());

                    } else {
                        android.widget.Toast.makeText(context, "Location data unavailable", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                holder.binding.btnDirections.setVisibility(View.GONE);
                holder.binding.btnTrackLive.setVisibility(View.VISIBLE);

                holder.binding.btnTrackLive.setOnClickListener(v -> {
                    android.widget.Toast.makeText(context, "Tracking Detailer...", android.widget.Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }


    private void openGoogleMapsDirections(double lat, double lng) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        }
    }

//    private void playClickSound() {
//
//        try {
//            MediaPlayer mp = MediaPlayer.create(context, R.raw.click_sound);
//            if (mp != null) {
//                mp.setOnCompletionListener(MediaPlayer::release);
//                mp.start();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "carcarego_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Booking Alerts", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.outline_directions_car_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemMyBookingsCardBinding binding;
        public ViewHolder(ItemMyBookingsCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}