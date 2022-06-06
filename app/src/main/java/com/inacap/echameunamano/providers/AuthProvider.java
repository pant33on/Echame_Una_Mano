package com.inacap.echameunamano.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class AuthProvider {
    FirebaseAuth auth;

    public AuthProvider(){
        auth = FirebaseAuth.getInstance();
    }

    public Task<AuthResult> register(String email, String pass){
        return auth.createUserWithEmailAndPassword(email, pass);
    }
    public Task<AuthResult> login(String email, String pass){
        return auth.signInWithEmailAndPassword(email, pass);
    }
    public  void logout(){
        auth.signOut();
    }
    public String getId(){
        return auth.getCurrentUser().getUid();
    }

    public boolean existeSesion(){
        boolean existe = false;
        if(auth.getCurrentUser() != null){
            existe = true;
        }
        return existe;
    }
}
