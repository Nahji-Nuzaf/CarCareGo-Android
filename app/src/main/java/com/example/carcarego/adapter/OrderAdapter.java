package com.example.carcarego.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.carcarego.R;
import com.example.carcarego.activity.OrderDetailActivity;
import com.example.carcarego.model.Order;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private final List<Order> orderList;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder holder, int position) {
        Order order = orderList.get(position);

        // 1. Order ID Formatting (Extract first 8 chars for a "Order #ABC1234" look)
        String displayId = "ORDER";
        if (order.getOrderId() != null) {
            displayId = order.getOrderId().length() >= 8
                    ? "Order #" + order.getOrderId().substring(0, 8).toUpperCase()
                    : "Order #" + order.getOrderId().toUpperCase();
        }
        holder.tvOrderId.setText(displayId);

        // 2. Date Formatting (Converting Firebase Timestamp to readable Date)
        if (order.getOrderDate() != null) {
            Date date = order.getOrderDate().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.tvOrderDate.setText(sdf.format(date));
        } else {
            holder.tvOrderDate.setText("N/A");
        }

        // 3. Status Badge Logic (Optional: Visual coloring)
        String status = order.getStatus() != null ? order.getStatus() : "PENDING";
        holder.tvOrderStatus.setText(status);

        if (status.equalsIgnoreCase("PAID")) {
            holder.tvOrderStatus.setTextColor(Color.parseColor("#4ADE80")); // Green
        } else {
            holder.tvOrderStatus.setTextColor(Color.parseColor("#FBBF24")); // Amber/Yellow
        }

        // 4. Currency Formatting (LKR)
        holder.tvOrderTotal.setText(String.format(Locale.US, "LKR %,.2f", order.getTotalAmount()));

        // 5. Navigation to Detail Activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OrderDetailActivity.class);

            // Serialize the full Order object (including nested CartItems and Address)
            Gson gson = new Gson();
            String myOrderJson = gson.toJson(order);

            intent.putExtra("ORDER_DATA", myOrderJson);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvOrderDate, tvOrderTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}