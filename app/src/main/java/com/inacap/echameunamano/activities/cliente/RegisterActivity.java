package com.inacap.echameunamano.activities.cliente;

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
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.LoginActivity;
import com.inacap.echameunamano.activities.operador.MapaOperadorActivity;
import com.inacap.echameunamano.activities.operador.RegOperadorActivity;
import com.inacap.echameunamano.includes.MyToolbar;
import com.inacap.echameunamano.modelos.Cliente;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.ClienteProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {
    AuthProvider authProvider;
    ClienteProvider clienteProvider;

    //Vistas
    Button btnRegistrar;
    TextInputEditText etNombre;
    TextInputEditText etEmail;
    TextInputEditText etContraseña;
    AlertDialog dialogo;

    private SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        MyToolbar.show(this, "Registro de usuario", true);

        btnRegistrar = findViewById(R.id.btnRegistrar);
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etContraseña = findViewById(R.id.etContraseña);
        //Dialogo loquillo
        dialogo = new SpotsDialog(RegisterActivity.this, R.style.Custom);
        authProvider = new AuthProvider();
        clienteProvider = new ClienteProvider();

        preferencias = getApplicationContext().getSharedPreferences("tipoUsuario", MODE_PRIVATE);

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
        String pass = etContraseña.getText().toString();

        //Traer el tipo de usuario para registrarlo
        String tipo = preferencias.getString("usuario", "");

        if(!nombre.isEmpty() && !email.isEmpty() && !pass.isEmpty()){
            if(esUnNombreValido(nombre)){
                if(esUnMailValido(email)){
                    if(esUnPassValido(pass)){
                        dialogo.show();
                        registra(nombre, email, pass, tipo);
                    }else{
                        etContraseña.setError("Ingrese una contraseña válida");
                    }
                }else{
                    etEmail.setError("Ingrese un correo válido");
                }
            }else{
                etNombre.setError("Ingrese un nómbre válido");
            }
        }else{
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_LONG).show();
        }
    }

    void registra(String nombre, String email, String pass, String tipo) {
        authProvider.register(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                dialogo.hide();
                if(task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Cliente cliente = new Cliente(id, nombre, email, tipo);
                    crear(cliente);

                    Toast.makeText(RegisterActivity.this, "REGISTRO EXITOSO", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void crear(Cliente cliente){
        clienteProvider.crear(cliente).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(RegisterActivity.this, TipoServicioActivity.class);
                    //Con esta función aseguramos que al apretar volver no pueda volver a pantalla de registro
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(RegisterActivity.this, "No se pudo crear el cliente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Función para validar nombre de usuario
    public static boolean esUnNombreValido(String nombre)
    {
        // Validar patrón de nombre
        String regex = "^[a-zA-ZÀ-ÿ\\u00f1\\u00d1]+(\\s*[a-zA-ZÀ-ÿ\\u00f1\\u00d1]*)*[a-zA-ZÀ-ÿ\\u00f1\\u00d1]+$";
        Pattern p = Pattern.compile(regex);

        //validar que no esté vacío
        if (nombre == null) {
            return false;
        }
        // Se utiliza el método matcher de la clase Pattern para validar que el nombre mantenga el patrón requerido
        Matcher m = p.matcher(nombre);
        return m.matches();
    }

    // Función para validar mail
    public static boolean esUnMailValido(String mail)
    {
        // Validar patrón de mail
        String regex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        Pattern p = Pattern.compile(regex);

        //validar que no esté vacío
        if (mail == null) {
            return false;
        }
        // Se utiliza el método matcher de la clase Pattern para validar que el nombre mantenga el patrón requerido
        Matcher m = p.matcher(mail);
        return m.matches();
    }

    // Función para validar pass
    public static boolean esUnPassValido(String pass)
    {
        // Validar patrón de password
        String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])(?!.*\\s).{8,20}$";
        Pattern p = Pattern.compile(regex);

        //validar que no esté vacío
        if (pass == null) {
            return false;
        }
        // Se utiliza el método matcher de la clase Pattern para validar que el nombre mantenga el patrón requerido
        Matcher m = p.matcher(pass);
        return m.matches();
    }
}