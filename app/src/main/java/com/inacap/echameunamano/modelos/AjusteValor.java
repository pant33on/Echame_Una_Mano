package com.inacap.echameunamano.modelos;

public class AjusteValor {
    double km;
    double ser_grua;
    double ser_bateria;
    double ser_neumatico;

    public AjusteValor(double km, double ser_grua, double ser_bateria, double ser_neumatico) {
        this.km = km;
        this.ser_grua = ser_grua;
        this.ser_bateria = ser_bateria;
        this.ser_neumatico = ser_neumatico;
    }

    public AjusteValor() {
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }

    public double getSer_grua() {
        return ser_grua;
    }

    public void setSer_grua(double ser_grua) {
        this.ser_grua = ser_grua;
    }

    public double getSer_bateria() {
        return ser_bateria;
    }

    public void setSer_bateria(double ser_bateria) {
        this.ser_bateria = ser_bateria;
    }

    public double getSer_neumatico() {
        return ser_neumatico;
    }

    public void setSer_neumatico(double ser_neumatico) {
        this.ser_neumatico = ser_neumatico;
    }
}
