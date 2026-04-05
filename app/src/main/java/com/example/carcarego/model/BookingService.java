package com.example.carcarego.model;

import com.google.firebase.firestore.Exclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingService implements Serializable {
    @Exclude
    private String serviceId;

    private String name;

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    private String description;
    private double price;
    private String duration;
    private String detailerId;
    private String category;

    public BookingService(String name, String description, double price, String duration, String detailerId, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.duration = duration;
        this.detailerId = detailerId;
        this.category = category;

    }
}