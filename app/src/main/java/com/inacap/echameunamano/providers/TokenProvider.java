package com.inacap.echameunamano.providers;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.inacap.echameunamano.modelos.Token;

public class TokenProvider {
    DatabaseReference database;

    public TokenProvider() {
        database = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }
    /*public void creaToken(String idUsuario){
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Token token = new Token(s);
                database.child(idUsuario).setValue(token);
            }
        });
    }*/
    public void creaToken(String idUsuario){
        if(idUsuario == null) return;
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Token token = new Token(s);
                database.child(idUsuario).setValue(token);
            }
        });
    }

    public DatabaseReference getToken(String idUsuario){

        return database.child(idUsuario);
    }
}
