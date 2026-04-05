package com.example.carcarego.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carcarego.R;
import com.example.carcarego.model.ServiceItem;

import java.util.List;
import java.util.Locale;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.ViewHolder> {

    private List<ServiceItem> list;
    private Context context;

    public RecommendedAdapter(Context context, List<ServiceItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommended_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceItem item = list.get(position);

        holder.name.setText(item.getName());
        holder.price.setText(String.format(Locale.US, "LKR %,.0f", item.getPrice()));
        holder.rating.setText(String.format(Locale.US, "%.1f (%d)", item.getRating(), item.getReviewCount()));
        holder.duration.setText(item.getDuration());

        if (item.getBadge() != null && !item.getBadge().isEmpty()) {
            holder.badge.setVisibility(View.VISIBLE);
            holder.badge.setText(item.getBadge());
        } else {
            holder.badge.setVisibility(View.GONE);
        }

        com.bumptech.glide.Glide.with(context)
                .load(item.getImage())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.image);
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, rating, duration, badge;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvServiceName);
            price = itemView.findViewById(R.id.tvServicePrice);
            image = itemView.findViewById(R.id.ivServiceImage);
        }
    }
}
