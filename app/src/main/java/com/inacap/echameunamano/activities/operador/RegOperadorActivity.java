package com.inacap.echameunamano.activities.operador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.includes.MyToolbar;
import com.inacap.echameunamano.modelos.Operador;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.OperadorProvider;

public class RegOperadorActivity extends AppCompatActivity {


    AuthProvider authProvider;
    OperadorProvider operadorProvider;

    //Vistas
    Button btnRegistrar;
    TextInputEditText etNombre;
    TextInputEditText etEmail;
    TextInputEditText etMarcaVehiculo;
    TextInputEditText etPatente;
    TextInputEditText etContraseña;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_operador);
        MyToolbar.show(this, "Registro de conductor", true);

        btnRegistrar = findViewById(R.id.btnRegistrar);
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etMarcaVehiculo = findViewById(R.id.etMarcaVehiculo);
        etPatente = findViewById(R.id.etPatente);
        etContraseña = findViewById(R.id.etContraseña);

        authProvider = new AuthProvider();
        operadorProvider = new OperadorProvider();

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRegistra();
            }
        });
    }
    private void clickRegistra() {
        String nombre = etNombre.getText().toString();
        String email = etEmail.getText().toString();
        String marca = etMarcaVehiculo.getText().toString();
        String patente = etPatente.getText().toString();
        String pass = etContraseña.getText().toString();

        if(!nombre.isEmpty() && !email.isEmpty() && !pass.isEmpty() && !marca.isEmpty() && !patente.isEmpty()){
            if(pass.length()>=6){
                registra(nombre, email, marca, patente, pass);
            }else{
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    void registra(String nombre, String email, String marca, String patente, String pass) {
        authProvider.register(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Operador operador = new Operador(id, nombre, email, marca, patente);
                    crear(operador);
                }
            }
        });
    }

    void crear(Operador operador){
        operadorProvider.crear(operador).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //Toast.makeText(RegOperadorActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegOperadorActivity.this, MapaOperadorActivity.class);

                    //Con esta función aseguramos que al apretar volver no pueda volver a pantalla de registro
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else{
                    Toast.makeText(RegOperadorActivity.this, "No se pudo crear el operador", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}