package com.inacap.echameunamano.modelos;

public class Operador {
    String id;
    String nombre;
    String email;
    String marcaVehiculo;
    String patente;
    String grua;
    String bateria;
    String neumatico;
    String imagen = "";


    public Operador() {
    }

    public Operador(String id, String nombre, String email, String marcaVehiculo, String patente, String grua, String bateria, String neumatico) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.marcaVehiculo = marcaVehiculo;
        this.patente = patente;
        this.grua = grua;
        this.bateria = bateria;
        this.neumatico = neumatico;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getGrua() {
        return grua;
    }

    public void setGrua(String grua) {
        this.grua = grua;
    }

    public String getBateria() {
        return bateria;
    }

    public void setBateria(String bateria) {
        this.bateria = bateria;
    }

    public String getNeumatico() {
        return neumatico;
    }

    public void setNeumatico(String neumatico) {
        this.neumatico = neumatico;
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
