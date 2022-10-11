package com.inacap.echameunamano.activities.operador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.cliente.DetalleHistorialClienteActivity;
import com.inacap.echameunamano.modelos.Historial;
import com.inacap.echameunamano.providers.ClienteProvider;
import com.inacap.echameunamano.providers.HistorialProvider;
import com.inacap.echameunamano.providers.OperadorProvider;
import com.squareup.picasso.Picasso;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetalleHistorialOperadorActivity extends AppCompatActivity {

    private TextView tvNombreDetalle;
    private CircleImageView imagenDetalle;
    private TextView tvServicioDetalle;
    private TextView tvValorDetalle;
    private TextView tvOrigenDetalle;
    private TextView tvDestinoDetalle;
    private TextView tvDistanciaDetalle;
    private RatingBar calificacionDetalle;
    private String extraId;
    private HistorialProvider historialProvider;
    private ClienteProvider clienteProvider;
    private CircleImageView btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_historial_operador);

        tvNombreDetalle = findViewById(R.id.tvNombreDetalle);
        imagenDetalle = findViewById(R.id.imagenDetalle);

        tvServicioDetalle = findViewById(R.id.tvServicioDetalle);
        tvValorDetalle = findViewById(R.id.tvValorDetalle);
        tvOrigenDetalle = findViewById(R.id.tvOrigenDetalle);
        tvDestinoDetalle = findViewById(R.id.tvDestinoDetalle);
        tvDistanciaDetalle = findViewById(R.id.tvDistanciaDetalle);
        calificacionDetalle = findViewById(R.id.calificacionDetalle);
        extraId = getIntent().getStringExtra("idHistorial");
        historialProvider = new HistorialProvider();
        clienteProvider = new ClienteProvider();
        btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getHistorial();
    }

    private void getHistorial() {
        historialProvider.getHistorial(extraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //Instancio un modelo historial con todos los datos del snapshot
                    Historial historial = snapshot.getValue(Historial.class);

                    String servicio = String.valueOf(historial.getTipoServicio());
                    if(servicio.equals("servicio_grua")){
                        tvServicioDetalle.setText("Servicio: Servicio de grúa");
                    }else if(servicio.equals("servicio_bateria")){
                        tvServicioDetalle.setText("Servicio: Servicio de batería");
                    }else if(servicio.equals("servicio_neumatico")){
                        tvServicioDetalle.setText("Servicio: Servicio de neumático");
                    }
                    //Falta agregar cálculo de tarifa
                    tvValorDetalle.setText("Valor: $10.000");
                    tvOrigenDetalle.setText("Origen: "+historial.getOrigen());
                    tvDestinoDetalle.setText("Destino: "+historial.getDestino());
                    tvDistanciaDetalle.setText("Distancia: "+historial.getKm());

                    if(snapshot.hasChild("calificacionOperador")){
                        calificacionDetalle.setRating((float) historial.getCalificacionOperador());
                    }

                    //Función para traer nombre e imagen de operador a partir de su Id
                    clienteProvider.getCliente(historial.getIdCliente()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override//
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String nombre = snapshot.child("nombre").getValue().toString();
                                tvNombreDetalle.setText(nombre.toUpperCase());
                                if(snapshot.hasChild("imagen")){
                                    String imagen = snapshot.child("imagen").getValue().toString();
                                    Picasso.with(DetalleHistorialOperadorActivity.this).load(imagen).into(imagenDetalle);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}