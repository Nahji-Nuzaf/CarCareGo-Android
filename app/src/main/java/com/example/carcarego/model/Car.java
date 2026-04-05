package com.example.carcarego.model;

import java.util.List;

public class Car {
    private String id;
    private String brandModel;
    private String licensePlate;
    private String vehicleType;
    private List<String> carImages;

    public Car() {}

    public Car(String id, String brandModel, String licensePlate, String vehicleType, List<String> carImages) {
        this.id = id;
        this.brandModel = brandModel;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.carImages = carImages;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBrandModel() { return brandModel; }
    public void setBrandModel(String brandModel) { this.brandModel = brandModel; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public List<String> getCarImages() { return carImages; }
    public void setCarImages(List<String> carImages) { this.carImages = carImages; }
}