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
import com.inacap.echameunamano.activities.cliente.TipoServicioActivity;
import com.inacap.echameunamano.activities.operador.MapaOperadorActivity;
import com.inacap.echameunamano.activities.operador.RegOperadorActivity;
import com.inacap.echameunamano.includes.MyToolbar;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etContraseña;
    private Button btnIngresar;
    private CircleImageView btnVolver;
    private FirebaseAuth auth;
    private DatabaseReference dataBase;
    private AlertDialog dialogo;
    private SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //MyToolbar.show(this, "Login de usuario", true);

        etEmail = findViewById(R.id.etEmail);
        etContraseña = findViewById(R.id.etContraseña);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Dialogo loquillo
        dialogo = new SpotsDialog(LoginActivity.this, R.style.Custom);

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
                dialogo.show();
                auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    String usuario = preferencias.getString("usuario","");
                                    if(usuario.equals("cliente")){
                                        Intent intent = new Intent(LoginActivity.this, TipoServicioActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        Intent intent = new Intent(LoginActivity.this, MapaOperadorActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }else{
                                    Toast.makeText(LoginActivity.this, "Correo electrónico o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                                }
                                dialogo.hide();
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