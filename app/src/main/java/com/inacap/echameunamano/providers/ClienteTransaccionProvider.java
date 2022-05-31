package com.inacap.echameunamano.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.inacap.echameunamano.modelos.ClienteTransaccion;

public class ClienteTransaccionProvider {
    private DatabaseReference database;

    public ClienteTransaccionProvider() {
        database = FirebaseDatabase.getInstance().getReference().child("ClienteTransaccion");
    }

    public Task<Void> crear(ClienteTransaccion clienteTransaccion){
        return database.child(clienteTransaccion.getIdCliente()).setValue(clienteTransaccion);
    }
}
