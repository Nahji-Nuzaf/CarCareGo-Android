package com.example.carcarego.model;

import com.google.firebase.firestore.Exclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyBookingService implements Serializable {
    @Exclude
    private String bookingId;

    private String serviceName;
    private double servicePrice;
    private String serviceType;

    private String vehicleModel;
    private String vehiclePlate;
    private String userId;

    private String bookingDate;
    private String bookingTime;
    private String status;
    private String instructions;

    private String detailerId;
    private String detailerName;

    private Double latitude;
    private Double longitude;

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
}