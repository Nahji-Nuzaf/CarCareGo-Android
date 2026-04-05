package com.example.carcarego.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carcarego.R;
import com.example.carcarego.model.WishlistItem;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {
    private List<WishlistItem> wishlist;
    private OnItemClickListener listener;

    public WishlistAdapter(List<WishlistItem> wishlist, OnItemClickListener listener) {
        this.wishlist = wishlist;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wishlist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WishlistItem item = wishlist.get(position);
        holder.title.setText(item.getTitle());
        holder.price.setText(String.format("LKR %,.2f", item.getPrice()));

        Glide.with(holder.itemView.getContext()).load(item.getImage()).into(holder.image);

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item, position));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() { return wishlist.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, btnDelete;
        TextView title, price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivWishProductImage);
            title = itemView.findViewById(R.id.tvWishProductName);
            price = itemView.findViewById(R.id.tvWishProductPrice);
            btnDelete = itemView.findViewById(R.id.btnRemoveWish);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(WishlistItem item);
        void onDeleteClick(WishlistItem item, int position);
    }
}
