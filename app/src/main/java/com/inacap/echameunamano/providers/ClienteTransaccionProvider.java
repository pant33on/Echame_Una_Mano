package com.inacap.echameunamano.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.inacap.echameunamano.modelos.ClienteTransaccion;

import java.util.HashMap;
import java.util.Map;

public class ClienteTransaccionProvider {
    private DatabaseReference database;

    public ClienteTransaccionProvider() {
        database = FirebaseDatabase.getInstance().getReference().child("ClienteTransaccion");
    }

    public Task<Void> crear(ClienteTransaccion clienteTransaccion){
        return database.child(clienteTransaccion.getIdCliente()).setValue(clienteTransaccion);
    }

    public Task<Void> actualizaEstado(String idClienteTransaccion, String estado){
        Map<String, Object> map = new HashMap<>();
        map.put("estado", estado);
        return database.child(idClienteTransaccion).updateChildren(map);
    }

    public Task<Void> actualizaIdHistorial(String idClienteTransaccion){
        //Generar Id único
        //Generar Id único
        String idUnico = database.push().getKey();
        Map<String, Object> map = new HashMap<>();
        map.put("idHistorial", idUnico);
        return database.child(idClienteTransaccion).updateChildren(map);
    }

    public DatabaseReference getEstado(String idClienteTransaccion){
        return database.child(idClienteTransaccion).child("estado");
    }

    public DatabaseReference getClienteTransaccion(String idClienteTransaccion){
        return database.child(idClienteTransaccion);
    }

    public Query getTransaccionPorOperador(String idOperador){
        return database.orderByChild("idOperador").equalTo(idOperador);
    }

    public Task<Void> eliminar(String idClienteTransaccion){
        return database.child(idClienteTransaccion).removeValue();
    }
}
