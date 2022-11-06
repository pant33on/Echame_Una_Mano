package com.inacap.echameunamano.activities.operador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.cliente.RegisterActivity;
import com.inacap.echameunamano.includes.MyToolbar;
import com.inacap.echameunamano.modelos.Operador;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.OperadorProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dmax.dialog.SpotsDialog;

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
    AlertDialog dialogo;

    String[] item = {"Activo","Inactivo"};
    ArrayAdapter<String> adapterItem;

    AutoCompleteTextView acGrua;
    AutoCompleteTextView acBateria;
    AutoCompleteTextView acNeumatico;
    String grua ="";
    String bateria ="";
    String neumatico ="";

    private SharedPreferences preferencias;

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
        acGrua = findViewById(R.id.acGrua);
        acBateria = findViewById(R.id.acBateria);
        acNeumatico = findViewById(R.id.acNeumatico);

        //Métodos autocomplete (select)
        adapterItem = new ArrayAdapter<String>(this,R.layout.lista_items,item);
        iniciaAc();

        //Dialogo loquillo
        dialogo = new SpotsDialog(RegOperadorActivity.this, R.style.Custom);
        authProvider = new AuthProvider();
        operadorProvider = new OperadorProvider();

        preferencias = getApplicationContext().getSharedPreferences("tipoUsuario", MODE_PRIVATE);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRegistra();
            }
        });
    }
    private void iniciaAc(){
        acGrua.setAdapter(adapterItem);
        acGrua.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                grua = parent.getItemAtPosition(i).toString();
            }
        });
        acBateria.setAdapter(adapterItem);
        acBateria.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                bateria = parent.getItemAtPosition(i).toString();
            }
        });
        acNeumatico.setAdapter(adapterItem);
        acNeumatico.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                neumatico = parent.getItemAtPosition(i).toString();
            }
        });
    }

    private void clickRegistra() {
        String nombre = etNombre.getText().toString();
        String email = etEmail.getText().toString();
        String marca = etMarcaVehiculo.getText().toString();
        String patente = etPatente.getText().toString();
        String pass = etContraseña.getText().toString();

        //Traer el tipo de usuario para registrarlo
        String tipo = preferencias.getString("usuario", "");

        if(!nombre.isEmpty() && !email.isEmpty() && !pass.isEmpty() && !marca.isEmpty() && !patente.isEmpty()
                && !grua.isEmpty() && !bateria.isEmpty() && !neumatico.isEmpty()){
            if(esUnNombreValido(nombre)){
                if(esUnMailValido(email)){
                    if(esUnPassValido(pass)){
                        dialogo.show();
                        registra(nombre, email, marca, patente, pass, grua, bateria, neumatico, tipo);
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
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    void registra(String nombre, String email, String marca, String patente, String pass, String grua, String bateria, String neumatico, String tipo) {
        authProvider.register(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                dialogo.hide();
                if(task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Operador operador = new Operador(id, nombre, email, marca, patente, grua, bateria, neumatico, tipo);
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
                    finish();
                }else{
                    Toast.makeText(RegOperadorActivity.this, "No se pudo crear el operador", Toast.LENGTH_SHORT).show();
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