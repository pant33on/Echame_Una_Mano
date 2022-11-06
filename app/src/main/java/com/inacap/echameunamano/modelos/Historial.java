package com.inacap.echameunamano.modelos;

public class Historial {

    String idHistorial;
    String idCliente;
    String idOperador;
    String tipoServicio;
    String destino;
    String origen;
    String tiempo;
    String km;
    String estado;
    double origenLat;
    double origenLng;
    double destinoLat;
    double destinoLng;
    double calificacionCliente;
    double calificacionOperador;
    Long fecha;
    double valor;

    public Historial() {
    }

    public Historial(String idHistorial, String idCliente, String idOperador, String tipoServicio,
                     String destino, String origen, String tiempo, String km, String estado, double origenLat,
                     double origenLng, double destinoLat, double destinoLng, double valor) {
        this.idHistorial = idHistorial;
        this.idCliente = idCliente;
        this.idOperador = idOperador;
        this.tipoServicio = tipoServicio;
        this.destino = destino;
        this.origen = origen;
        this.tiempo = tiempo;
        this.km = km;
        this.estado = estado;
        this.origenLat = origenLat;
        this.origenLng = origenLng;
        this.destinoLat = destinoLat;
        this.destinoLng = destinoLng;
        this.valor = valor;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public Long getFecha() {
        return fecha;
    }

    public void setFecha(Long fecha) {
        this.fecha = fecha;
    }

    public String getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(String idHistorial) {
        this.idHistorial = idHistorial;
    }

    public double getCalificacionCliente() {
        return calificacionCliente;
    }

    public void setCalificacionCliente(double calificacionCliente) {
        this.calificacionCliente = calificacionCliente;
    }

    public double getCalificacionOperador() {
        return calificacionOperador;
    }

    public void setCalificacionOperador(double calificacionOperador) {
        this.calificacionOperador = calificacionOperador;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdOperador() {
        return idOperador;
    }

    public void setIdOperador(String idOperador) {
        this.idOperador = idOperador;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getOrigenLat() {
        return origenLat;
    }

    public void setOrigenLat(double origenLat) {
        this.origenLat = origenLat;
    }

    public double getOrigenLng() {
        return origenLng;
    }

    public void setOrigenLng(double origenLng) {
        this.origenLng = origenLng;
    }

    public double getDestinoLat() {
        return destinoLat;
    }

    public void setDestinoLat(double destinoLat) {
        this.destinoLat = destinoLat;
    }

    public double getDestinoLng() {
        return destinoLng;
    }

    public void setDestinoLng(double destinoLng) {
        this.destinoLng = destinoLng;
    }
}
