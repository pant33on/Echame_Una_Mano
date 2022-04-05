package com.inacap.echameunamano.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.cliente.MapaClienteActivity;
import com.inacap.echameunamano.activities.cliente.RegisterActivity;
import com.inacap.echameunamano.activities.operador.MapaOperadorActivity;
import com.inacap.echameunamano.activities.operador.RegOperadorActivity;
import com.inacap.echameunamano.includes.MyToolbar;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText etEmail;
    TextInputEditText etContraseña;
    Button btnIngresar;

    FirebaseAuth auth;
    DatabaseReference dataBase;
    AlertDialog dialogo; //Investigar para poner el progress dialog bar

    SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MyToolbar.show(this, "Login de usuario", true);

        etEmail = findViewById(R.id.etEmail);
        etContraseña = findViewById(R.id.etContraseña);
        btnIngresar = findViewById(R.id.btnIngresar);

        //dialogo = new SpotsDialog...
        preferencias = getApplicationContext().getSharedPreferences("tipoUsuario", MODE_PRIVATE);

        auth = FirebaseAuth.getInstance();
        dataBase = FirebaseDatabase.getInstance().getReference();

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {
        String email = etEmail.getText().toString();
        String pass = etContraseña.getText().toString();

        if(!email.isEmpty() && !pass.isEmpty()){
            if(pass.length() >= 6){
                auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    String usuario = preferencias.getString("usuario","");
                                    if(usuario.equals("cliente")){
                                        Intent intent = new Intent(LoginActivity.this, MapaClienteActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }else{
                                        Intent intent = new Intent(LoginActivity.this, MapaOperadorActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                }else{
                                    Toast.makeText(LoginActivity.this, "Correo electrónico o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }else{
                Toast.makeText(this, "La contraseña debe tener más de 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "La contraseña y correo electrónico son obligatorios", Toast.LENGTH_SHORT).show();
        }
    }
}