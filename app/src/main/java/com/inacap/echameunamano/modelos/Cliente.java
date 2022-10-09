package com.inacap.echameunamano.modelos;

public class Cliente {
    String id ="";
    String nombre = "";
    String email = "";
    String imagen = "";

    public Cliente(String id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    public Cliente() {
    }

    public Cliente(String id, String nombre, String email, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.imagen = imagen;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
