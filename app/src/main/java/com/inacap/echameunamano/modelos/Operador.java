package com.inacap.echameunamano.modelos;

public class Operador {
    String id;
    String nombre;
    String email;
    String marcaVehiculo;
    String patente;

    public Operador() {
    }

    public Operador(String id, String nombre, String email, String marcaVehiculo, String patente) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.marcaVehiculo = marcaVehiculo;
        this.patente = patente;
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

    public String getMarcaVehiculo() {
        return marcaVehiculo;
    }

    public void setMarcaVehiculo(String marcaVehiculo) {
        this.marcaVehiculo = marcaVehiculo;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }
}
