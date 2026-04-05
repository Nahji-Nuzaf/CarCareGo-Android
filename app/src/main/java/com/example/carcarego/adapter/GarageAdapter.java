package com.example.carcarego.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carcarego.R;
import com.example.carcarego.model.Car;

import java.util.List;

public class GarageAdapter extends RecyclerView.Adapter<GarageAdapter.ViewHolder> {
    private List<Car> carList;

    public GarageAdapter(List<Car> carList) { this.carList = carList; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.tvName.setText(car.getBrandModel());
        holder.tvPlate.setText(car.getLicensePlate());
        holder.tvType.setText(car.getVehicleType());

        if (car.getCarImages() != null && !car.getCarImages().isEmpty()) {
            String base64 = car.getCarImages().get(0);
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.ivCar.setImageBitmap(decodedByte);
        }
    }

    @Override
    public int getItemCount() { return carList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPlate, tvType;
        ImageView ivCar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCarName);
            tvPlate = itemView.findViewById(R.id.tvLicensePlate);
            tvType = itemView.findViewById(R.id.tvType);
            ivCar = itemView.findViewById(R.id.ivCarImage);
        }
    }
}
