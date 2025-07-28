package com.example.restaurant_food_app;

public class MenuItem {
    private String id;
    private String dishName;
    private double price;
    private String imageUrl;
    private boolean isAvailable;

    public MenuItem(String id, String dishName, double price, String imageUrl, boolean isAvailable) {
        this.id = id;
        this.dishName = dishName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getDishName() { return dishName; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public boolean isAvailable() { return isAvailable; }
}