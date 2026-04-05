package com.example.carcarego.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.carcarego.R;
import com.example.carcarego.activity.DetailerProfileActivity;
import com.example.carcarego.model.Detailer;
import com.google.gson.Gson;

import java.util.List;
import java.util.Locale;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ViewHolder> {

    private List<Detailer> detailerList;
    private Context context;

    public ExploreAdapter(List<Detailer> detailerList) {
        this.detailerList = detailerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_detailer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Detailer detailer = detailerList.get(position);

        holder.tvDetailerName.setText(detailer.getName());
        holder.tvLocation.setText("📍 " + detailer.getCity());
//        holder.tvRating.setText(String.format(Locale.US, "⭐ %.1f", detailer.getRating()));
//        holder.tvReviewCount.setText(String.format(Locale.US, "(%d+ reviews)", detailer.getReviewCount()));

        if ("MOBILE".equalsIgnoreCase(detailer.getServiceType())) {
            holder.tvServiceTypeBadge.setText("MOBILE SERVICE");
            holder.tvServiceTypeBadge.setBackgroundResource(R.drawable.bg_badge_mobile);
            holder.tvServiceTypeBadge.setTextColor(context.getColor(android.R.color.holo_blue_light));
        } else {
            holder.tvServiceTypeBadge.setText("STATIONARY SHOP");
            holder.tvServiceTypeBadge.setBackgroundResource(R.drawable.bg_badge_mobile);
            holder.tvServiceTypeBadge.setTextColor(context.getColor(android.R.color.white));
        }

        Glide.with(context)
                .load(detailer.getImage())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.ivDetailer);

        holder.itemView.setOnClickListener(v -> {
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailerProfileActivity.class);

            Gson gson = new Gson();
            String detailerJson = gson.toJson(detailer);

            intent.putExtra("DETAILER_DATA", detailerJson);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return detailerList != null ? detailerList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDetailer;
        TextView tvDetailerName, tvServiceTypeBadge, tvLocation, tvRating, tvReviewCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDetailer = itemView.findViewById(R.id.ivDetailer);
            tvDetailerName = itemView.findViewById(R.id.tvDetailerName);
            tvServiceTypeBadge = itemView.findViewById(R.id.tvServiceTypeBadge);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviewCount = itemView.findViewById(R.id.tvReviewCount);
        }
    }
}