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
import com.example.carcarego.model.CartItem;
import com.example.carcarego.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {

    private List<CartItem> itemList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public OrderDetailAdapter(List<CartItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This uses your custom receipt-style layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_orders, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = itemList.get(position);

        // 1. Set quantity immediately from the order data
        holder.tvQty.setText(String.format(Locale.US, "%02d", item.getQuantity()));

        // 2. Fetch missing details from "products" using productId "p9", etc.
        db.collection("products").whereEqualTo("productId", item.getProductId()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Product product = queryDocumentSnapshots.getDocuments().get(0).toObject(Product.class);
                        if (product != null) {
                            holder.tvName.setText(product.getTitle());
                            holder.tvPrice.setText(String.format(Locale.US, "LKR %,.0f", product.getPrice()));

                            if (product.getImages() != null && !product.getImages().isEmpty()) {
                                Glide.with(holder.itemView.getContext())
                                        .load(product.getImages().get(0))
                                        .centerCrop()
                                        .into(holder.ivImage);
                            }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQty;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartProductName);
            tvPrice = itemView.findViewById(R.id.tvCartProductPrice);
            tvQty = itemView.findViewById(R.id.tvCartQuantity);
            ivImage = itemView.findViewById(R.id.ivCartProductImage);
        }
    }
}