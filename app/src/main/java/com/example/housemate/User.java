package com.example.housemate;

public class User {

    //Elementos del objeto User, deben tener el mismo nombre que en la colección de la BD.
    private String email;
    private String img_name;
    private String name;
    private String password;
    private String username;

    /**
     * Constructor por defecto de la clase User.
     */
    public User(){



    }

    /**
     *
     * Método encargado de obtener el email del usuario.
     *
     * @return
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * Método encargado de asignar un valor al email del usuario.
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * Método encargado de obtener el nombre de la imagen del usuario.
     *
     * @return
     */
    public String getImg_name() {
        return img_name;
    }

    /**
     *
     * Método encargado de asignar un valor al nombre del usuario.
     *
     * @param img_name
     */
    public void setImg_name(String img_name) {
        this.img_name = img_name;
    }

    /**
     *
     * Método encargado de obtener el nombre del usuario.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * Método encargado de asignar un valor al nombre del usuario.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * Método encargado de obtener la contraseña del usuario.
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * Método encargado de asignar un valor a la contraseña del usuario.
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     * Método encargado de obtener el nombre de usuario del usuario.
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * Método encargado de asignar un valor al nombre de usuario del usuario.
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
