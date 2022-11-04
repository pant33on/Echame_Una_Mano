package com.inacap.echameunamano.activities.operador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.ClienteTransaccionProvider;
import com.inacap.echameunamano.providers.GeofireProvider;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.EventListener;

public class NotificacionActivity extends AppCompatActivity {
    private LottieAnimationView animacionNotificacion;
    private TextView tvNotificacionDestino;
    private TextView tvNotificacionOrigen;
    private TextView tvNotificacionTiempo;
    private TextView tvNotificacionDistancia;
    private Button btnNotificacionAceptar;
    private Button btnNotificacionCancelar;
    private TextView tvContador;

    private ClienteTransaccionProvider clienteTransaccionProvider;
    private GeofireProvider geofireProvider;
    private AuthProvider authProvider;
    private  String extraIdCliente;
    private  String extraOrigen;
    private  String extraDestino;
    private  String extraTiempo;
    private  String extraDistancia;
    private MediaPlayer mediaPlayer;
    private ValueEventListener listener;

    private int contador = 60;
    private Handler handler;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            contador -= 1;
            tvContador.setText(String.valueOf(contador));
            if(contador > 0){
                iniciaContador();
            }else{
                cancelarTransaccion();
            }
        }
    };

    private void iniciaContador() {
        handler = new Handler();
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacion);
        animacionNotificacion = findViewById(R.id.animacionNotificacion);
        animacionNotificacion.playAnimation();
        mediaPlayer = MediaPlayer.create(this, R.raw.ring);
        mediaPlayer.setLooping(true);
        clienteTransaccionProvider = new ClienteTransaccionProvider();

        tvNotificacionOrigen = findViewById(R.id.tvNotificacionOrigen);
        tvNotificacionDestino = findViewById(R.id.tvNotificacionDestino);
        tvNotificacionTiempo = findViewById(R.id.tvNotificacionTiempo);
        tvNotificacionDistancia = findViewById(R.id.tvNotificacionDistancia);
        btnNotificacionAceptar = findViewById(R.id.btnNotificacionAceptar);
        btnNotificacionCancelar = findViewById(R.id.btnNotificacionCancelar);
        tvContador = findViewById(R.id.tvContador);

        extraIdCliente = getIntent().getStringExtra("idCliente");
        extraOrigen = getIntent().getStringExtra("origen");
        extraDestino = getIntent().getStringExtra("destino");
        extraTiempo = getIntent().getStringExtra("tiempo");
        extraDistancia = getIntent().getStringExtra("distancia");

        tvNotificacionOrigen.setText(extraOrigen);
        tvNotificacionDestino.setText(extraDestino);
        tvNotificacionTiempo.setText(extraTiempo);
        tvNotificacionDistancia.setText(extraDistancia);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        btnNotificacionAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aceptarTransaccion();
            }
        });
        btnNotificacionCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               cancelarTransaccion();
            }
        });
        iniciaContador();
        revisaCancelacion();
    }

    private void cancelarTransaccion() {
        if(handler != null) handler.removeCallbacks(runnable);
        clienteTransaccionProvider.actualizaEstado(extraIdCliente, "cancelado");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent = new Intent(NotificacionActivity.this, MapaOperadorActivity.class);
        startActivity(intent);
        finish();
    }

    private void aceptarTransaccion() {
        if(handler != null) handler.removeCallbacks(runnable);
        authProvider = new AuthProvider();
        geofireProvider = new GeofireProvider("Operadores_activos");
        geofireProvider.borraUbicacion(authProvider.getId());

        clienteTransaccionProvider = new ClienteTransaccionProvider();
        clienteTransaccionProvider.actualizaEstado(extraIdCliente, "aceptado");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent = new Intent(NotificacionActivity.this, MapaOperadorTransaccionActivity.class);
        //REVISAR FLAGS REVISAR FLAGS REVISAR FLAGS
        //REVISAR FLAGS REVISAR FLAGS REVISAR FLAGS
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(Intent.ACTION_RUN);
        intent.putExtra("idCliente", extraIdCliente);
        startActivity(intent);
    }

    private void revisaCancelacion(){
        listener = clienteTransaccionProvider.getClienteTransaccion(extraIdCliente).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Toast.makeText(NotificacionActivity.this, "El cliente canceló la búsqueda", Toast.LENGTH_LONG).show();
                    if(handler != null) handler.removeCallbacks(runnable);
                    Intent intent = new Intent(NotificacionActivity.this, MapaOperadorActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onPause() {
        try {
            super.onPause();
            if(mediaPlayer != null){
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
            }
        }catch (Exception e){
            Log.d("TAG_", e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        try {
            super.onStop();
            if(mediaPlayer != null){
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.release();
                }
            }
        }catch (Exception e){
            Log.d("TAG_", e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            if(mediaPlayer != null){
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
            }
        }catch (Exception e){
            Log.d("TAG_", e.getMessage());
        }
    }

    /*@Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            if(handler != null) handler.removeCallbacks(runnable);
            if(mediaPlayer != null){
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
            }
            if(listener != null){
                clienteTransaccionProvider.getClienteTransaccion(extraIdCliente).removeEventListener(listener);
            }
        }catch (Exception e){
            Log.d("TAG_", e.getMessage());
        }
    }*/
}