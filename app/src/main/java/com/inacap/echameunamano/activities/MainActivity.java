package com.inacap.echameunamano.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.cliente.MapaClienteActivity;
import com.inacap.echameunamano.activities.cliente.TipoServicioActivity;
import com.inacap.echameunamano.activities.operador.MapaOperadorActivity;
import com.inacap.echameunamano.adapters.Contador;

public class MainActivity extends AppCompatActivity {

    private Button mButtonSoyCliente;
    private Button mButtonSoyOperador;
    private SharedPreferences preferencias;
    public static Contador contador = Contador.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferencias = getApplicationContext().getSharedPreferences("tipoUsuario", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        mButtonSoyCliente = findViewById(R.id.btnSoyCliente);
        mButtonSoyOperador = findViewById(R.id.btnSoyOperador);
        mButtonSoyCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("usuario", "cliente");
                editor.apply();
                irActivityAuth();
            }
        });
        mButtonSoyOperador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("usuario", "operador");
                editor.apply();
                irActivityAuth();
            }
        });
    }

    private void irActivityAuth() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String usuario = preferencias.getString("usuario","");
            if(usuario.equals("operador")){
                Intent intent = new Intent(MainActivity.this, MapaOperadorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(MainActivity.this, TipoServicioActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }
}