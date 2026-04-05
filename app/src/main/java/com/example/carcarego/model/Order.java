package com.example.carcarego.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Exclude
    private String orderId;

    private String userId;
    private List<CartItem> items;
    private double totalAmount;
    private double subtotal;
    private double shippingFee;
    private String status;


    private Timestamp orderDate;

    private ShippingAddress shippingAddress;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShippingAddress {
        private String address1;
        private String address2;
        private String city;
        private String name;
        private String phone;
    }
}