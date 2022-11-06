package com.inacap.echameunamano.providers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AjusteValorProvider {
    DatabaseReference database;

    public AjusteValorProvider() {
        database = FirebaseDatabase.getInstance().getReference().child("Ajustes_valor");
    }

    public DatabaseReference getAjusteValor(){
        return database;
    }
}
