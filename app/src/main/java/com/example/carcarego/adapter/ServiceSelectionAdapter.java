package com.example.carcarego.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.carcarego.R;
import com.example.carcarego.model.BookingService;
import java.util.List;
import java.util.Locale;

public class ServiceSelectionAdapter extends RecyclerView.Adapter<ServiceSelectionAdapter.ViewHolder> {

    private List<BookingService> serviceList;
    private int selectedPosition = -1;

    public ServiceSelectionAdapter(List<BookingService> serviceList) {
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingService service = serviceList.get(position);

        holder.tvName.setText(service.getName());
        holder.tvPrice.setText(String.format(Locale.US, "LKR %,.0f", service.getPrice()));
        holder.tvDuration.setText(service.getDuration());

        holder.rbService.setChecked(position == selectedPosition);

        View.OnClickListener clickListener = v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                selectedPosition = currentPos;
                notifyDataSetChanged();
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.rbService.setOnClickListener(clickListener);
    }

    public BookingService getSelectedService() {
        if (selectedPosition != -1 && selectedPosition < serviceList.size()) {
            return serviceList.get(selectedPosition);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvDuration;
        RadioButton rbService;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            tvDuration = itemView.findViewById(R.id.tvServiceDuration);
            rbService = itemView.findViewById(R.id.rbService);
        }
    }
}