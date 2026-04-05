package com.example.carcarego.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.carcarego.R;
import com.example.carcarego.databinding.ItemVehicleSelectionBinding;
import com.example.carcarego.model.Car;
import java.util.List;

public class VehicleSelectionAdapter extends RecyclerView.Adapter<VehicleSelectionAdapter.ViewHolder> {

    private List<Car> carList;
    private int selectedPosition = -1;
    private OnCarSelectedListener listener;

    public interface OnCarSelectedListener {
        void onCarSelected(Car car);
    }

    public VehicleSelectionAdapter(List<Car> carList, OnCarSelectedListener listener) {
        this.carList = carList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVehicleSelectionBinding binding = ItemVehicleSelectionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.binding.tvVehicleModel.setText(car.getBrandModel());
        holder.binding.tvVehiclePlate.setText(car.getLicensePlate());

        if (selectedPosition == position) {
            holder.binding.cvVehicle.setStrokeWidth(4);
            holder.binding.cvVehicle.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.blue_accent));
            holder.binding.cvVehicle.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.dark_blue_variant));
        } else {
            holder.binding.cvVehicle.setStrokeWidth(0);
            holder.binding.cvVehicle.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_bg_color));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
            listener.onCarSelected(car);
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemVehicleSelectionBinding binding;
        public ViewHolder(ItemVehicleSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}