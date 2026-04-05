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

public class ServicePackageAdapter extends RecyclerView.Adapter<ServicePackageAdapter.ViewHolder> {
    private List<BookingService> list;
    private int selectedPos = -1;
    private OnServiceSelectedListener listener;

    public interface OnServiceSelectedListener { void onSelected(BookingService service); }

    public ServicePackageAdapter(List<BookingService> list, OnServiceSelectedListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_service_selection, p, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int p) {
        BookingService s = list.get(p);
        h.name.setText(s.getName());
        h.price.setText(String.format("LKR %,.0f", s.getPrice()));
        h.duration.setText(s.getDuration());
        h.radio.setChecked(p == selectedPos);

        View.OnClickListener click = v -> {
            selectedPos = h.getBindingAdapterPosition();
            notifyDataSetChanged();
            listener.onSelected(s);
        };
        h.itemView.setOnClickListener(click);
        h.radio.setOnClickListener(click);
    }

    @Override public int getItemCount() { return list.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, duration;
        RadioButton radio;
        public ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tvServiceName);
            price = v.findViewById(R.id.tvServicePrice);
            duration = v.findViewById(R.id.tvServiceDuration);
            radio = v.findViewById(R.id.rbService);
        }
    }
}