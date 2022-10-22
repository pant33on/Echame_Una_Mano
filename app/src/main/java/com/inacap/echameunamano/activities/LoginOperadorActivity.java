package com.inacap.echameunamano.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.cliente.TipoServicioActivity;
import com.inacap.echameunamano.activities.operador.MapaOperadorActivity;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.OperadorProvider;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class LoginOperadorActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etContraseña;
    private Button btnIngresar;
    private CircleImageView btnVolver;
    private FirebaseAuth auth;
    private AlertDialog dialogo;
    private SharedPreferences preferencias;
    private AuthProvider authProvider;
    private OperadorProvider operadorProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_operador);
        //MyToolbar.show(this, "Login de usuario", true);
        authProvider = new AuthProvider();
        operadorProvider = new OperadorProvider();

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
        dialogo = new SpotsDialog(LoginOperadorActivity.this, R.style.Custom);
        preferencias = getApplicationContext().getSharedPreferences("tipoUsuario", MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();

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
            dialogo.show();
            auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                String usuario = preferencias.getString("usuario","");
                                //AQUÍ HACER CONSULTA EN BDD SI EL TIPO CORRESPONDE CON EL SHARED PREFERENCES
                                if(authProvider.existeSesion()){
                                    operadorProvider.getOperador(authProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                String tipo = snapshot.child("tipo").getValue().toString();
                                                if(tipo.equals(usuario)){
                                                    Intent intent = new Intent(LoginOperadorActivity.this, MapaOperadorActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }else{
                                                authProvider.logout();
                                                Toast.makeText(LoginOperadorActivity.this, "TIPO DE USUARIO INCORRECTO, VUELVA A LA PANTALLA DE SELECCIÓN", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                                }
                            }else{
                                Toast.makeText(LoginOperadorActivity.this, "Correo electrónico o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                            }
                            dialogo.hide();
                        }
                    });
        }else{
            Toast.makeText(this, "La contraseña y correo electrónico son obligatorios", Toast.LENGTH_SHORT).show();
        }
    }
}