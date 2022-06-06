package com.inacap.echameunamano.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.inacap.echameunamano.modelos.ClienteTransaccion;
import com.inacap.echameunamano.modelos.Historial;

import java.util.HashMap;
import java.util.Map;

public class HistorialProvider {
    private DatabaseReference database;

    public HistorialProvider() {
        database = FirebaseDatabase.getInstance().getReference().child("Historial");
    }

    public Task<Void> crear(Historial historial){
        return database.child(historial.getIdHistorial()).setValue(historial);
    }

    public Task<Void> actualizaCalificacionCliente(String idHistorial, float calificacionCliente){
        Map<String, Object> map = new HashMap<>();
        map.put("calificacionCliente", calificacionCliente);
        return database.child(idHistorial).updateChildren(map);
    }

    public Task<Void> actualizaCalificacionOperador(String idHistorial, float calificacionOperador){
        Map<String, Object> map = new HashMap<>();
        map.put("calificacionOperador", calificacionOperador);
        return database.child(idHistorial).updateChildren(map);
    }

    public DatabaseReference getHistorial(String idHistorial){
        return database.child(idHistorial);
    }

}
