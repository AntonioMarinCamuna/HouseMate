package com.example.housemate;

import java.util.ArrayList;

public class Room {

    private String address;
    private String title;
    private String description;
    private String image;
    private double price;
    private String roomId;
    private String publisherId;

    private ArrayList<String> imageGallery;

    public Room(){



    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ArrayList<String> getImageGallery() {
        return imageGallery;
    }

    public void setImageGallery(ArrayList<String> imageGallery) {
        this.imageGallery = imageGallery;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }
}
