package com.inacap.echameunamano.adapters;

public class Contador {

    private static Contador instance;
    private static int numeroGrua = 0;
    private static int numeroBateria = 0;
    private static int numeroNeumatico = 0;

    private Contador() {
    }
    public static Contador getInstance(){
        if(instance == null){
            instance = new Contador();
        }
        return instance;
    }
    public void contar(String servicio){
        if(servicio.equals("grua")){
            numeroGrua +=1;
        }else if(servicio.equals("bateria")){
            numeroBateria +=1;
        }else if(servicio.equals("neumatico")){
            numeroNeumatico+=1;
        }

    }
    public int getNumeroGrua(){
        return numeroGrua;
    }
    public int getNumeroBateria(){
        return numeroBateria;
    }
    public int getNumeroNeumatico(){
        return numeroNeumatico;
    }
    public void resetNumeros(){
        numeroGrua = 0;
        numeroBateria = 0;
        numeroNeumatico = 0;
    }
}
