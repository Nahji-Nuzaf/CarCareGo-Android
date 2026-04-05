package com.example.carcarego.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carcarego.R;
import com.example.carcarego.model.Category;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categories;
    private int selectedPosition = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }

    private OnCategoryClickListener listener;

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.categoryName.setText(category.getCategoryName());

        int sapphireBlue = Color.parseColor("#1E88E5");
        int defaultWhite = Color.parseColor("#FFFFFF");

        if (selectedPosition == position) {
            holder.categoryName.setTextColor(sapphireBlue);
            holder.cardCategory.setStrokeColor(sapphireBlue);
            holder.cardCategory.setStrokeWidth(4);
        } else {
            holder.categoryName.setTextColor(defaultWhite);
            holder.cardCategory.setStrokeWidth(0);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onCategoryClick(category.getCategoryName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        MaterialCardView cardCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.tvCategoryName);
            cardCategory = (MaterialCardView) itemView.findViewById(R.id.cardCategory);
        }
    }
}