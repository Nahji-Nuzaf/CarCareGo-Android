package com.example.carcarego.model;

public class ServiceItem {
    private String id, name, image, category, badge, duration;
    private double price, rating;
    private int reviewCount;

    public ServiceItem() {}

    public String getName() { return name; }
    public String getImage() { return image; }
    public String getBadge() { return badge; }
    public String getDuration() { return duration; }
    public double getPrice() { return price; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
}