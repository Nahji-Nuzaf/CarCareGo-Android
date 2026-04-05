package com.example.carcarego.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class Detailer implements Serializable {
    @Exclude
    private String id;
    private String name, providerName, image, serviceType, city, address, phone, description;
    private double rating;
    private int reviewCount;


    private Double latitude;
    private Double longitude;

    public Detailer() {}


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public String getProviderName() { return providerName; }
    public String getImage() { return image; }
    public String getServiceType() { return serviceType; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getDescription() { return description; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}