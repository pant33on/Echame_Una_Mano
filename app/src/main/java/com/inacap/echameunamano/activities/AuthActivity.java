package com.inacap.echameunamano.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.cliente.RegisterActivity;
import com.inacap.echameunamano.activities.operador.RegOperadorActivity;
import com.inacap.echameunamano.includes.MyToolbar;

public class AuthActivity extends AppCompatActivity {

    private Button btnIrLogin;
    private Button btnIrRegistro;
    private SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        MyToolbar.show(this, "Seleccionar opción", true);

        preferencias = getApplicationContext().getSharedPreferences("tipoUsuario", MODE_PRIVATE);
        btnIrLogin = findViewById(R.id.btnIrLogin);
        btnIrLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irALogin();
            }
        });
        btnIrRegistro = findViewById(R.id.btnIrRegistro);
        btnIrRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irARegister();
            }
        });
    }

    public void irALogin() {
        String usuario = preferencias.getString("usuario","");
        if(usuario.equals("cliente")){
            Intent intent = new Intent(AuthActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        if(usuario.equals("operador")){
            Intent intent = new Intent(AuthActivity.this, LoginOperadorActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void irARegister() {
        String tipoUsuario = preferencias.getString("usuario", "");
        if(tipoUsuario.equals("cliente")){
            Intent intent = new Intent(AuthActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(AuthActivity.this, RegOperadorActivity.class);
            startActivity(intent);
            finish();
        }
    }
}