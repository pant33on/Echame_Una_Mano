package com.inacap.echameunamano.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.inacap.echameunamano.modelos.Cliente;
import com.inacap.echameunamano.modelos.Operador;

import java.util.HashMap;
import java.util.Map;

public class OperadorProvider {
    DatabaseReference database;

    public OperadorProvider(){
        database = FirebaseDatabase.getInstance().getReference().child("Usuarios").child("Operadores");
    }
    public Task<Void> crear(Operador operador){
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", operador.getNombre());
        map.put("email", operador.getEmail());
        map.put("marcaVehiculo", operador.getMarcaVehiculo());
        map.put("patente", operador.getPatente());
        return database.child(operador.getId()).setValue(map);
    }
}
