package com.example.carcarego.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carcarego.R;
import com.example.carcarego.model.CartItem;
import com.example.carcarego.model.Product;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> cartItems;
    private OnQuantityChangeListener changeListener;
    private OnRemoveListener removeListener;

    public CartAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.changeListener = listener;
    }

    public void setOnRemoveListener(OnRemoveListener listener) {
        this.removeListener = listener;
    }

    @NonNull
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, int position) {
//        CartItem cartItem = cartItems.get(position);
//
//
//        holder.productName.setText(cartItem.getTitle());
//        holder.productPrice.setText(String.format(Locale.US, "LKR %,.2f", cartItem.getPrice()));
//        holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
//
//
//        Glide.with(holder.itemView.getContext())
//                .load(cartItem.getImage())
//                .centerCrop()
//                .placeholder(R.drawable.placeholder_image)
//                .into(holder.productImage);
//
//
//        holder.btnPlus.setOnClickListener(v -> {
//
//            cartItem.setQuantity(cartItem.getQuantity() + 1);
//            notifyItemChanged(holder.getAbsoluteAdapterPosition());
//            if (changeListener != null) {
//                changeListener.onChanged(cartItem);
//            }
//        });
//
//
//        holder.btnMinus.setOnClickListener(v -> {
//            if (cartItem.getQuantity() > 1) {
//                cartItem.setQuantity(cartItem.getQuantity() - 1);
//                notifyItemChanged(holder.getAbsoluteAdapterPosition());
//                if (changeListener != null) {
//                    changeListener.onChanged(cartItem);
//                }
//            }
//        });
//
//
//        holder.btnRemove.setOnClickListener(v -> {
//            int currentPos = holder.getAbsoluteAdapterPosition();
//            if (currentPos != RecyclerView.NO_POSITION && removeListener != null) {
//                removeListener.onRemoved(currentPos);
//            }
//        });
//    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("products").whereEqualTo("productId", cartItem.getProductId())
                .get()
                .addOnSuccessListener(qds -> {
                    if (!qds.isEmpty()) {
                        Product product = qds.getDocuments().get(0).toObject(Product.class);
                        if (product != null) {
                            holder.productName.setText(product.getTitle());
                            holder.productPrice.setText(String.format(Locale.US, "LKR %,.2f", product.getPrice()));
                            holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));

                            Glide.with(holder.itemView.getContext())
                                    .load(product.getImages().get(0))
                                    .centerCrop()
                                    .into(holder.productImage);


                            holder.btnPlus.setOnClickListener(v -> {
                                if (cartItem.getQuantity() < product.getStockCount()) {
                                    int newQty = cartItem.getQuantity() + 1;
                                    cartItem.setQuantity(newQty);


                                    String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                                    if (uid != null && cartItem.getDocumentId() != null) {
                                        db.collection("users").document(uid)
                                                .collection("cart").document(cartItem.getDocumentId())
                                                .update("quantity", newQty);
                                    }

                                    notifyItemChanged(holder.getAbsoluteAdapterPosition());
                                    if (changeListener != null) changeListener.onChanged(cartItem);
                                }
                            });
                        }
                    }
                });


        holder.btnMinus.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                int newQty = cartItem.getQuantity() - 1;
                cartItem.setQuantity(newQty);


                String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                if (uid != null && cartItem.getDocumentId() != null) {
                    db.collection("users").document(uid)
                            .collection("cart").document(cartItem.getDocumentId())
                            .update("quantity", newQty);
                }

                notifyItemChanged(holder.getAbsoluteAdapterPosition());
                if (changeListener != null) changeListener.onChanged(cartItem);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) removeListener.onRemoved(holder.getAbsoluteAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity;
        ImageView productImage;
        ImageButton btnPlus, btnMinus, btnRemove;
        MaterialCardView productCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.tvCartProductName);
            productImage = itemView.findViewById(R.id.ivCartProductImage);
            productCard = itemView.findViewById(R.id.productCard);
            productPrice = itemView.findViewById(R.id.tvCartProductPrice);
            productQuantity = itemView.findViewById(R.id.tvCartQuantity);
            btnPlus = itemView.findViewById(R.id.btnCartPlus);
            btnMinus = itemView.findViewById(R.id.btnCartMinus);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
        }
    }


    public interface OnQuantityChangeListener {
        void onChanged(CartItem cartItem);
    }

    public interface OnRemoveListener {
        void onRemoved(int position);
    }
}