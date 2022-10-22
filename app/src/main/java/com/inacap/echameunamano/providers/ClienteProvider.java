package com.inacap.echameunamano.providers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
        map.put("tipo", cliente.getTipo());
        return database.child(cliente.getId()).setValue(map);
    }

    public Task<Void> actualizar(Cliente cliente){
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", cliente.getNombre());
        map.put("imagen", cliente.getImagen());
        return database.child(cliente.getId()).updateChildren(map);
    }

    public DatabaseReference getCliente(String idCliente){
        return database.child(idCliente);
    }
}
