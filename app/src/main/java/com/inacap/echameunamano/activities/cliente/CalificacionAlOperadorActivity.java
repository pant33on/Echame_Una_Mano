package com.inacap.echameunamano.activities.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.operador.CalificacionAlClienteActivity;
import com.inacap.echameunamano.activities.operador.MapaOperadorActivity;
import com.inacap.echameunamano.modelos.ClienteTransaccion;
import com.inacap.echameunamano.modelos.Historial;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.ClienteTransaccionProvider;
import com.inacap.echameunamano.providers.HistorialProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class CalificacionAlOperadorActivity extends AppCompatActivity {
    private LottieAnimationView animacionCalificacionOperador;
    private TextView tvOrigenCalificacion;
    private TextView tvDestinoCalificacion;
    private RatingBar ratingBar;
    private float calificacion = 0;
    private AuthProvider authProvider;

    private Button btnCalificarOperador;
    private ClienteTransaccionProvider clienteTransaccionProvider;
    private Historial historial;
    private HistorialProvider historialProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificacion_al_operador);
        animacionCalificacionOperador = findViewById(R.id.animacionCalificacionOperador);
        animacionCalificacionOperador.playAnimation();
        clienteTransaccionProvider = new ClienteTransaccionProvider();

        tvOrigenCalificacion = findViewById(R.id.tvOrigenCalificacion);
        tvDestinoCalificacion = findViewById(R.id.tvDestinoCalificacion);
        ratingBar = findViewById(R.id.ratingBarCalificacion);

        btnCalificarOperador = findViewById(R.id.btnCalificarOperador);
        historialProvider = new HistorialProvider();
        authProvider = new AuthProvider();

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float calif, boolean b) {
                calificacion = calif;
            }
        });
        btnCalificarOperador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calificar();
            }
        });
        getClienteTransaccion();
    }

    private void getClienteTransaccion(){
        clienteTransaccionProvider.getClienteTransaccion(authProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    ClienteTransaccion clienteTransaccion = snapshot.getValue(ClienteTransaccion.class);
                    tvOrigenCalificacion.setText(clienteTransaccion.getOrigen());
                    tvDestinoCalificacion.setText(clienteTransaccion.getDestino());
                    historial = new Historial(
                            clienteTransaccion.getIdHistorial(),
                            clienteTransaccion.getIdCliente(),
                            clienteTransaccion.getIdOperador(),
                            clienteTransaccion.getTipoServicio(),
                            clienteTransaccion.getDestino(),
                            clienteTransaccion.getOrigen(),
                            clienteTransaccion.getTiempo(),
                            clienteTransaccion.getKm(),
                            clienteTransaccion.getEstado(),
                            clienteTransaccion.getOrigenLat(),
                            clienteTransaccion.getOrigenLng(),
                            clienteTransaccion.getDestinoLat(),
                            clienteTransaccion.getDestinoLng()
                    );

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void calificar() {
        if(calificacion > 0){
            historial.setCalificacionOperador(calificacion);
            //Corregir a futuro para traer fecha y hora por separado en un campo tipo Date
            historial.setFecha(new Date().getTime());
            historialProvider.getHistorial(historial.getIdHistorial()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        historialProvider.actualizaCalificacionOperador(historial.getIdHistorial(), calificacion).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificacionAlOperadorActivity.this, "La calificación se ingresó correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificacionAlOperadorActivity.this, TipoServicioActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }else{
                        historialProvider.crear(historial).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificacionAlOperadorActivity.this, "La calificación se ingresó correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificacionAlOperadorActivity.this, TipoServicioActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }else{
            Toast.makeText(this, "Debe ingresar una calificación", Toast.LENGTH_SHORT).show();
        }
    }
}