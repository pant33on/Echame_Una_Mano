package com.inacap.echameunamano.providers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.activities.MainActivity;
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

    //Agrega el número total de servicios de cada tipo al Singleton
    public void getCantServicios(String idLoco){
        MainActivity.contador.resetNumeros();
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds: snapshot.getChildren()){
                        String idTemp = ds.child("idCliente").getValue().toString();
                        if(idTemp.equals(idLoco)){
                            String tipo = ds.child("tipoServicio").getValue().toString();
                            if(tipo.equals("servicio_grua")){
                                MainActivity.contador.contar("grua");
                            }else if(tipo.equals("servicio_bateria")){
                                MainActivity.contador.contar("bateria");
                            }else if(tipo.equals("servicio_neumatico")){
                                MainActivity.contador.contar("neumatico");
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //Agrega el número total de servicios de cada tipo al Singleton
    public void getCantServiciosOperador(String idLoco){
        MainActivity.contador.resetNumeros();
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds: snapshot.getChildren()){
                        String idTemp = ds.child("idOperador").getValue().toString();
                        if(idTemp.equals(idLoco)){
                            String tipo = ds.child("tipoServicio").getValue().toString();
                            if(tipo.equals("servicio_grua")){
                                MainActivity.contador.contar("grua");
                            }else if(tipo.equals("servicio_bateria")){
                                MainActivity.contador.contar("bateria");
                            }else if(tipo.equals("servicio_neumatico")){
                                MainActivity.contador.contar("neumatico");
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}
