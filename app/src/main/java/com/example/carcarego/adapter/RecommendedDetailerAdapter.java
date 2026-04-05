package com.example.carcarego.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carcarego.model.Detailer;

import java.util.List;
import java.util.function.Consumer;

public class RecommendedDetailerAdapter extends RecyclerView.Adapter<RecommendedDetailerAdapter.ViewHolder> {
    private List<Detailer> list;
    private Consumer<Detailer> listener;

    public RecommendedDetailerAdapter(List<Detailer> list, Consumer<Detailer> listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecommendedDetailerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendedDetailerAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
