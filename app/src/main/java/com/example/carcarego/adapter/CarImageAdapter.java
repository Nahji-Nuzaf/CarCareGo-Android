package com.example.carcarego.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.carcarego.R;
import java.util.List;

public class CarImageAdapter extends RecyclerView.Adapter<CarImageAdapter.ViewHolder> {

    private List<String> images;

    public CarImageAdapter(List<String> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String base64 = images.get(position);
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        holder.ivThumbnail.setImageBitmap(decodedByte);

        holder.btnRemove.setOnClickListener(v -> {
            images.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() { return images.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        View btnRemove;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            btnRemove = itemView.findViewById(R.id.btnRemoveImage);
        }
    }
}