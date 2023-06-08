package com.example.housemate;

import java.util.ArrayList;

/**
 * Clase habitación que permitirá almacenar y manejar información sobre una habitación obtenida de la BD.
 */
public class Room {

    //Elementos del objeto Room, deben tener el mismo nombre que en la colección de la BD.
    private String address;
    private String city;
    private String description;
    private String price;
    private String publisherId;
    private String title;
    private String image;
    private String booked;
    private String postId;
    private String bookedBy;
    private String maxDays;
    private String bookedDays;

    /**
     * Constructor por defecto del objeto Room.
     */
    public Room(){



    }

    /**
     *
     * Método encargado de obtener información relativo a la ciudad de la publicación.
     *
     * @return
     */
    public String getCity() {
        return city;
    }

    /**
     *
     * Método encargado de asignar un valor a la variable de la ciudad.
     *
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     *
     * Método encargado de obtener información relativo a la dirección de la publicación.
     *
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     *
     * Método encargado de asignar un valor a la variable de la dirección.
     *
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     *
     * Método encargado de obtener información relativo al título del post.
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * Método encargado de asignar un valor a la variable del título del post.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * Método encargado de obtener información relativo a la descripción de la habitación.
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * Método encargado de asignar un valor a la variable de la descripción de la habitación.
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * Método encargado de obtener información relativo al precio de la habitación.
     *
     * @return
     */
    public String getPrice() {
        return price;
    }

    /**
     *
     * Método encargado de asignar un valor a la variable del precio.
     *
     * @param price
     */
    public void setPrice(String price) {
        this.price = price;
    }

    /**
     *
     * Método encargado de obtener información relativo al ID de la persona que ha publicado la habitación.
     *
     * @return
     */
    public String getPublisherId() {
        return publisherId;
    }

    /**
     *
     * Método encargado de asignar un valor a la variable de la ID de la persona que publica la habitación.
     *
     * @param publisherId
     */
    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    /**
     *
     * Método encargado de obtener información relativo a el URL de la imagen asociada a la habitación.
     *
     * @return
     */
    public String getImage() {
        return image;
    }

    /**
     *
     * Método encargado de asignar un valor a la variable de la URL de la imagen de la habitación.
     *
     * @param img
     */
    public void setImage(String img) {
        this.image = img;
    }

    /**
     *
     * Método encargado de obtener información relativo a saber si la habitación está reservada o no.
     *
     * @return
     */
    public String getBooked() {
        return booked;
    }

    /**
     *
     * Método encargado de asignar un valor a la variable para saber si está reservada o no.
     *
     * @param booked
     */
    public void setBooked(String booked) {
        this.booked = booked;
    }

    /**
     *
     * Método encargado de obtener información relativo al id del post.
     *
     * @return
     */
    public String getPostId() {
        return postId;
    }

    /**
     *
     * Método encargado de asignar un valor a la variable de la ID de la habitación.
     *
     * @param postId
     */
    public void setPostId(String postId) {
        this.postId = postId;
    }

    /**
     *
     * Método encargado de obtener información relativo a la persona que ha reservado la habitación
     *
     * @return
     */
    public String getBookedBy() {
        return bookedBy;
    }

    /**
     *
     * Método encargado de asignar un valor a la variable de la persona que ha reservado la habitación.
     *
     * @param bookedBy
     */
    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }

    /**
     *
     * Método encargado de obtener información relativo a el máximo de días disponible.
     *
     * @return
     */
    public String getMaxDays() {
        return maxDays;
    }

    /**
     *
     * Método encargado de asignar un valor a la variable de máximos días disponible.
     *
     * @param maxDays
     */
    public void setMaxDays(String maxDays) {
        this.maxDays = maxDays;
    }

    /**
     *
     * Método encargado de obtener información relativo a el número de dias reservada.
     *
     * @return
     */
    public String getBookedDays() {
        return bookedDays;
    }

    /**
     *
     * Método encargado de asignar un valor a la variable de días reservado.
     *
     * @param bookedDays
     */
    public void setBookedDays(String bookedDays) {
        this.bookedDays = bookedDays;
    }
}
