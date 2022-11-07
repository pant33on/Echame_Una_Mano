package com.inacap.echameunamano.activities.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.AuthActivity;
import com.inacap.echameunamano.activities.MainActivity;
import com.inacap.echameunamano.activities.operador.MapaOperadorActivity;
import com.inacap.echameunamano.includes.MyToolbar;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.HistorialProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TipoServicioActivity extends AppCompatActivity {

    private Button btnGrua;
    private Button btnBateria;
    private Button btnNeumatico;
    private SharedPreferences preferencias;

    private AuthProvider authProvider;
    private String idLoco;
    private HistorialProvider historialProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipo_servicio);
        MyToolbar.show(this, "Seleccionar opción", false);

        btnGrua = findViewById(R.id.btnGrua);
        btnBateria = findViewById(R.id.btnBateria);
        btnNeumatico = findViewById(R.id.btnNeumatico);
        authProvider = new AuthProvider();
        historialProvider = new HistorialProvider();

        preferencias = getApplicationContext().getSharedPreferences("tipoServicio", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();

        btnGrua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("servicio", "servicio_grua");
                editor.apply();
                irActivityGrua();
            }
        });
        btnBateria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("servicio", "servicio_bateria");
                editor.apply();
                irActivityBateria();
            }
        });
        btnNeumatico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("servicio", "servicio_neumatico");
                editor.apply();
                irActivityNeumatico();
            }
        });
    llenaDashboard();
    }

    private void irActivityGrua() {
        Intent intent = new Intent(TipoServicioActivity.this, MapaClienteActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void irActivityBateria() {
        Intent intent = new Intent(TipoServicioActivity.this, MapaClienteAlternativoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void irActivityNeumatico() {
        Intent intent = new Intent(TipoServicioActivity.this, MapaClienteAlternativoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /*@Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String usuario = preferencias.getString("servicio","");
            if(usuario.equals("servicio-grua")){
                Intent intent = new Intent(TipoServicioActivity.this, MapaClienteActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else if(usuario.equals("servicio-bateria")){

            }else if(usuario.equals("servicio-neumatico")){

            }
        }
    }*/
    void logout() {
        authProvider.logout();
        Intent intent = new Intent(TipoServicioActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cliente_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.cliente_action_logout){
            logout();
        }
        if(item.getItemId() == R.id.action_update){
            Intent intent = new Intent(TipoServicioActivity.this, ActualizaPerfilActivity.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.action_historial){
            Intent intent = new Intent(TipoServicioActivity.this, HistorialClienteActivity.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.action_dashboard){
            Intent intent = new Intent(TipoServicioActivity.this, DashboardClienteActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void llenaDashboard(){
        idLoco = authProvider.getId();
        historialProvider.getCantServicios(idLoco);
    }

}