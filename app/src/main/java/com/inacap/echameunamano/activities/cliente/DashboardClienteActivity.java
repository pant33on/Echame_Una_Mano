package com.inacap.echameunamano.activities.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.MainActivity;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.ClienteProvider;
import com.inacap.echameunamano.providers.HistorialProvider;
import com.squareup.picasso.Picasso;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardClienteActivity extends AppCompatActivity {

    private TextView tvCantGrua;
    private TextView tvCantBateria;
    private TextView tvCantNeumatico;
    private TextView tvNombreDetalle;
    private CircleImageView btnVolver;
    private CircleImageView imagenDetalle;

    private AuthProvider authProvider;
    private ClienteProvider clienteProvider;
    private HistorialProvider historialProvider;
    private int cantGrua = 0;
    private int cantBateria = 0;
    private int cantNeumatico = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_cliente);

        tvCantGrua = findViewById(R.id.tvCantGrua);
        tvCantBateria = findViewById(R.id.tvCantBateria);
        tvCantNeumatico = findViewById(R.id.tvCantNeumatico);
        tvNombreDetalle = findViewById(R.id.tvNombreDetalle);

        authProvider = new AuthProvider();
        historialProvider = new HistorialProvider();
        clienteProvider = new ClienteProvider();

        imagenDetalle = findViewById(R.id.imagenDetalle);
        btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getDashboardInfo();
    }

    private void getDashboardInfo() {
        cantGrua = MainActivity.contador.getNumeroGrua();
        cantBateria = MainActivity.contador.getNumeroBateria();
        cantNeumatico = MainActivity.contador.getNumeroNeumatico();

        tvCantGrua.setText("Servicios de grúa: "+cantGrua);
        tvCantBateria.setText("Servicios de batería: "+cantBateria);
        tvCantNeumatico.setText("Servicios de neumático: "+cantNeumatico);

        //Función para traer nombre e imagen de operador a partir de su Id
        clienteProvider.getCliente(authProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override//
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombre = snapshot.child("nombre").getValue().toString();
                    tvNombreDetalle.setText(nombre.toUpperCase());

                    if(snapshot.hasChild("imagen")){
                        String imagen = snapshot.child("imagen").getValue().toString();
                        Picasso.with(DashboardClienteActivity.this).load(imagen).into(imagenDetalle);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}