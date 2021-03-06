package com.inacap.echameunamano.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.inacap.echameunamano.modelos.Cliente;

import java.util.HashMap;
import java.util.Map;

public class ClienteProvider {
    DatabaseReference database;

    public ClienteProvider(){
        database = FirebaseDatabase.getInstance().getReference().child("Usuarios").child("Clientes");
    }
    public Task<Void> crear(Cliente cliente){
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", cliente.getNombre());
        map.put("email", cliente.getEmail());
        return database.child(cliente.getId()).setValue(map);
    }
}
