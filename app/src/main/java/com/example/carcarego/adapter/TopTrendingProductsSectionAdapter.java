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
import com.example.carcarego.model.Product;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class TopTrendingProductsSectionAdapter extends RecyclerView.Adapter<TopTrendingProductsSectionAdapter.ViewHolder> {

    private List<Product> products;
    private OnListingItemClickListener listener;
    private int selectedPosition = 0;

    public TopTrendingProductsSectionAdapter(List<Product> products, OnListingItemClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TopTrendingProductsSectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_recycler, parent, false);
        return new ViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull ListingAdapter.ViewHolder holder, int position) {
//        Product product = products.get(position);
//        holder.productName.setText(product.getTitle());
//        holder.productPrice.setText((int) product.getPrice());
//        Glide.with(holder.itemView.getContext())
//                .load(product.getImages().get(0))
//                .centerCrop()
//                .into(holder.productImage);
//
//        holder.itemView.setOnClickListener(view -> {
//            if(listener != null){
//                listener.onListingItemClick(product);
//            }
//        });
//    }

    @Override
    public void onBindViewHolder(@NonNull TopTrendingProductsSectionAdapter.ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.productName.setText(product.getTitle());

        holder.productPrice.setText("LKR " + String.valueOf((int) product.getPrice()));

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImages().get(0))
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image) // Show while loading
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onListingItemClick(product);
            }
        });

        holder.productCard.setOnClickListener(v -> {
            if (listener != null) listener.onListingItemClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView productPrice;
        ImageView productImage;
        MaterialCardView productCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.itemProductName);
            productImage = itemView.findViewById(R.id.itemProductImg);
            productCard = (MaterialCardView) itemView.findViewById(R.id.itemProductCard);
            productPrice = itemView.findViewById(R.id.itemProductPrice);
        }
    }

    public interface OnListingItemClickListener{
        void onListingItemClick(Product product);
    }

}