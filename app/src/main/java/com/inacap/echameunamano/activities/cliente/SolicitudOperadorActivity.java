package com.inacap.echameunamano.activities.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.modelos.AjusteValor;
import com.inacap.echameunamano.modelos.ClienteTransaccion;
import com.inacap.echameunamano.modelos.FCMBody;
import com.inacap.echameunamano.modelos.FCMResuesta;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.ClienteTransaccionProvider;
import com.inacap.echameunamano.providers.GeofireProvider;
import com.inacap.echameunamano.providers.GoogleApiProvider;
import com.inacap.echameunamano.providers.NotificacionProvider;
import com.inacap.echameunamano.providers.OperadorProvider;
import com.inacap.echameunamano.providers.TokenProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolicitudOperadorActivity extends AppCompatActivity {
    //region Variables
    private LottieAnimationView animacion;
    private TextView tvBuscando;
    private Button btnCancelarViaje;
    private Button btnVolver;
    private GeofireProvider geofireProvider;

    private DatabaseReference referencia;
    //AQUI TENGO QUE PONER EL PUTEXTRA DE TIPO DE SERVICIO ELEGIDO
    private SharedPreferences preferencias;
    private String tipoServicio = "";
    private GoogleApiProvider googleApiProvider;

    private String extraNombreOrigen;
    private String extraNombreDestino;
    private double extraOrigenLat;
    private double extraOrigenLng;
    private double extraDestinoLat;
    private double extraDestinoLng;
    private String tiempoTotal;
    private String distanciaTotal;
    private double extraValor;

    private LatLng origenLatLng;
    private LatLng destinoLatLng;

    private double radio = 0.1;
    private boolean operadorEncontrado = false;
    private String idOperadorEncontrado = "";
    private LatLng operadorEncontradoLatLng;
    private NotificacionProvider notificacionProvider;
    private TokenProvider tokenProvider;
    private boolean noHay = false;


    private ClienteTransaccionProvider clienteTransaccionProvider;
    private AuthProvider authProvider;
    private ValueEventListener listener;

    private ArrayList <String> operadoresQueNo = new ArrayList<>();
    private Handler handler = new Handler();
    private int tiempo = 0;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(tiempo < 35){
                tiempo++;
                handler.postDelayed(runnable, 1000);
            }else{
                if(idOperadorEncontrado != null){
                    if(!idOperadorEncontrado.equals("")){
                        repiteSolicitud();
                    }
                }
                handler.removeCallbacks(runnable);
            }
        }
    };

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitud_operador);

        animacion = findViewById(R.id.animacion);
        tvBuscando = findViewById(R.id.tvBuscando);
        btnCancelarViaje = findViewById(R.id.btnCancelarViaje);
        btnVolver = findViewById(R.id.btnVolver);
        animacion.playAnimation();
        geofireProvider = new GeofireProvider("Operadores_activos");

        //TRAER DEL SHARED PREFERENCES EL TIPO DE SERVICIO
        preferencias = getApplicationContext().getSharedPreferences("tipoServicio", MODE_PRIVATE);

        extraNombreOrigen = getIntent().getStringExtra("origen");
        extraNombreDestino = getIntent().getStringExtra("destino");
        extraOrigenLat = getIntent().getDoubleExtra("origen_lat",0);
        extraOrigenLng = getIntent().getDoubleExtra("origen_lng",0);
        extraDestinoLat = getIntent().getDoubleExtra("destino_lat",0);
        extraDestinoLng = getIntent().getDoubleExtra("destino_lng",0);
        tiempoTotal = getIntent().getStringExtra("tiempo");
        distanciaTotal = getIntent().getStringExtra("distancia");
        extraValor = getIntent().getDoubleExtra("valor",0);

        origenLatLng = new LatLng(extraOrigenLat, extraOrigenLng);
        destinoLatLng = new LatLng(extraDestinoLat, extraDestinoLng);

        obtenOperadorCercano();
        notificacionProvider = new NotificacionProvider();
        tokenProvider = new TokenProvider();
        clienteTransaccionProvider = new ClienteTransaccionProvider();
        authProvider = new AuthProvider();
        googleApiProvider = new GoogleApiProvider(SolicitudOperadorActivity.this);

        btnCancelarViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelarPeticion();
            }
        });
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //cancelarPeticion();
                finish();
            }
        });
    }

    private void cancelarPeticion() {
        clienteTransaccionProvider.getClienteTransaccion(authProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    clienteTransaccionProvider.eliminar(authProvider.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            enviaNotificacionCancelar();
                        }
                    });
                }else{
                    Intent intent = new Intent(SolicitudOperadorActivity.this, TipoServicioActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private boolean rechazoSolicitud(String idOperador){
        for (String id: operadoresQueNo) {
            if(id.equals(idOperador)){
                return true;
            }
        }
        return false;
    }

    private void revisaEstado() {
        listener = clienteTransaccionProvider.getEstado(authProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String estado = snapshot.getValue().toString();
                    if(estado.equals("aceptado")){
                        Intent intent = new Intent(SolicitudOperadorActivity.this, MapaClienteTransaccionActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }else if(estado.equals("cancelado")){
                        Toast.makeText(SolicitudOperadorActivity.this, "El operador no aceptó el servicio", Toast.LENGTH_SHORT).show();
                        //COSA PARA VALIDAR
                        //COSA PARA VALIDAR
                        repiteSolicitud();

                        /*Intent intent = new Intent(SolicitudOperadorActivity.this, MapaClienteActivity.class);
                        startActivity(intent);
                        finish();*/
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void repiteSolicitud() {
        operadoresQueNo.add(idOperadorEncontrado);
        operadorEncontrado = false;
        idOperadorEncontrado = "";
        radio = 0.1;
        obtenOperadorCercano();
    }

    private void obtenOperadorCercano(){
        geofireProvider.obtieneOperadores(origenLatLng, radio).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!operadorEncontrado && !rechazoSolicitud(key)) {
                    idOperadorEncontrado = key;
                    operadorEncontrado = true;
                    operadorEncontradoLatLng = new LatLng(location.latitude, location.longitude);
                    filtraPorServicio();
                }
            }
            @Override
            public void onKeyExited(String key) {
            }
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }
            @Override
            public void onGeoQueryReady() {
                //Ingresa cuando termina búsqueda de operador en radio de 0.1 km
                if (!operadorEncontrado) {
                    radio = radio + 0.1;
                    //No encontró ningún conductor
                    if(radio > 20){
                        noHay = true;
                        tvBuscando.setText("No se encontró un operador");
                        btnCancelarViaje.setVisibility(View.GONE);
                        btnVolver.setVisibility(View.VISIBLE);
                        return;
                    }else {
                        obtenOperadorCercano();
                    }
                }
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    //Método para accionar solo si el operador presta servicio
    private void filtraPorServicio(){
        referencia = FirebaseDatabase.getInstance().getReference().child("Usuarios").child("Operadores").child(idOperadorEncontrado);
        tipoServicio = preferencias.getString("servicio", "");
        referencia.child(tipoServicio).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String estadoServicio = snapshot.getValue().toString();
                    if(estadoServicio.equals("Activo")){
                        crearClienteTransaccion();
                        tvBuscando.setText("Se ha encontrado un operador \nEsperando respuesta");
                        //enviaNotificacion();
                    }else{
                        repiteSolicitud();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        /*referencia.child(tipoServicio).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    String estadoServicio = String.valueOf(task.getResult().getValue());
                    if(estadoServicio.equals("Activo")){
                        crearClienteTransaccion();
                        tvBuscando.setText("Se ha encontrado un operador \nEsperando respuesta");
                        //enviaNotificacion();
                    }else{
                        repiteSolicitud();
                        //operadorEncontrado = false;
                        //radio = radio + 0.1f;
                        //No encontró ningún conductor
                        //if(radio > 20){
                            noHay = true;
                            tvBuscando.setText("No se encontró un operador");
                            btnCancelarViaje.setVisibility(View.GONE);
                            btnVolver.setVisibility(View.VISIBLE);
                            return;
                        }else {
                            obtenOperadorCercano();
                        }
                    }
                }else{
                    Log.d("TAG_","Error: " + task.getException().getMessage());
                }
            }
        });*/
    }

    //cliente transaccion metodo
    private void crearClienteTransaccion(){
        googleApiProvider.getDirecciones(origenLatLng, operadorEncontradoLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject ruta = jsonArray.getJSONObject(0);
                    JSONObject polylines = ruta.getJSONObject("overview_polyline");
                    String puntos = polylines.getString("points");
                    JSONArray legs = ruta.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanciaText = distance.getString("text");
                    String duracionText = duration.getString("text");
                    //Ahora aquí se ejectua método enviar notificación
                    enviaNotificacion(duracionText, distanciaText);
                } catch (Exception e){
                    Log.d("TAG_","Error: " + e.getMessage());
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    //NOTIFICACION CANCELAR NOTIFICACION CANCELAR
    //NOTIFICACION CANCELAR NOTIFICACION CANCELAR
    private void enviaNotificacionCancelar(){
        if(idOperadorEncontrado != null){
            tokenProvider.getToken(idOperadorEncontrado).addListenerForSingleValueEvent(new ValueEventListener() {
                //El datasnapshot devuelve del nodo Token: idUsuario + Token
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        if(snapshot.hasChild("token")){
                            //Con este método obtengo token del usuario.
                            String token = snapshot.child("token").getValue().toString();
                            Map<String, String> mapa = new HashMap<>();
                            mapa.put("titulo", "VIAJE CANCELADO");
                            mapa.put("contenido", "El cliente canceló la solicitud"
                            );
                            FCMBody fcmBody = new FCMBody(token, "high", "4500s", mapa);
                            notificacionProvider.enviaNotificacion(fcmBody).enqueue(new Callback<FCMResuesta>() {
                                @Override
                                public void onResponse(Call<FCMResuesta> call, Response<FCMResuesta> response) {
                                    //si llegó respuesta desde el servidor
                                    if(response.body() != null){
                                        if (response.body().getSuccess() == 1){
                                            Toast.makeText(SolicitudOperadorActivity.this, "La solicitud se canceló correctamente", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(SolicitudOperadorActivity.this, TipoServicioActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }else{
                                            Toast.makeText(SolicitudOperadorActivity.this, "No se pudo enviar la notificación", Toast.LENGTH_SHORT).show();
                                        }
                                    }else {
                                        Toast.makeText(SolicitudOperadorActivity.this, "PROBLEMAS MAYORES", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<FCMResuesta> call, Throwable t) {
                                    Log.d("TAG_", "Error" + t.getMessage());
                                }
                            });
                        }else{
                            Toast.makeText(SolicitudOperadorActivity.this, "La solicitud se canceló correctamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SolicitudOperadorActivity.this, TipoServicioActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    }else{
                        Toast.makeText(SolicitudOperadorActivity.this, "NO SE PUEDE HACER SNAPSHOT", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }else{
            Toast.makeText(SolicitudOperadorActivity.this, "La solicitud se canceló correctamente", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SolicitudOperadorActivity.this, TipoServicioActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //MÉTODO ENVIAR NOTIFICACION ENVIAR NOTIFICACION
    //MÉTODO ENVIAR NOTIFICACION ENVIAR NOTIFICACION
    private void enviaNotificacion(final String min, final String km) {
        tokenProvider.getToken(idOperadorEncontrado).addListenerForSingleValueEvent(new ValueEventListener() {
            //El datasnapshot devuelve del nodo Token: idUsuario + Token
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("token")){
                        //Con este método obtengo token del usuario.
                        String token = snapshot.child("token").getValue().toString();
                        Map<String, String> mapa = new HashMap<>();
                        mapa.put("titulo", "SOLICITUD DE SERVICIO A " + min + " DE TU POSICIÓN");
                        mapa.put("contenido",
                                "Un cliente está solicitando un servicio a una distancia de " + km + "\n" +
                                        "Recoger en: " + extraNombreOrigen + "\n" +
                                        "Destino: " + extraNombreDestino);

                        //Enviar datos necesarios a MyFireMessaginBaseService para que este los envíe a NotificationActivity
                        //Enviar datos necesarios a MyFireMessaginBaseService para que este los envíe a NotificationActivity
                        mapa.put("idCliente", authProvider.getId());
                        mapa.put("origen", extraNombreOrigen);
                        mapa.put("destino", extraNombreDestino);
                        mapa.put("tiempo", min);
                        mapa.put("distancia", km);

                        DecimalFormat formatea = new DecimalFormat("###,###.##");
                        String totalF = formatea.format(extraValor);
                        String valorFinal = totalF.replace(",", ".");
                        mapa.put("valor", String.valueOf(valorFinal));

                        FCMBody fcmBody = new FCMBody(token, "high", "4500s", mapa);
                        notificacionProvider.enviaNotificacion(fcmBody).enqueue(new Callback<FCMResuesta>() {
                            @Override
                            public void onResponse(Call<FCMResuesta> call, Response<FCMResuesta> response) {
                                //si llegó respuesta desde el servidor
                                if(response.body() != null){
                                    if (response.body().getSuccess() == 1){
                                        ClienteTransaccion clienteTransaccion = new ClienteTransaccion(
                                                authProvider.getId(),
                                                idOperadorEncontrado,
                                                preferencias.getString("servicio", ""),
                                                extraNombreDestino,
                                                extraNombreOrigen,
                                                tiempoTotal,
                                                distanciaTotal,
                                                "creado",
                                                origenLatLng.latitude,
                                                origenLatLng.longitude,
                                                destinoLatLng.latitude,
                                                destinoLatLng.longitude,
                                                extraValor
                                        );
                                        clienteTransaccionProvider.crear(clienteTransaccion).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                revisaEstado();
                                            }
                                        });
                                    }else{
                                        Toast.makeText(SolicitudOperadorActivity.this, "No se pudo enviar la notificación", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    Toast.makeText(SolicitudOperadorActivity.this, "PROBLEMAS MAYORES", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<FCMResuesta> call, Throwable t) {
                                Log.d("TAG_", "Error" + t.getMessage());
                            }
                        });
                    }
                }else{
                    Toast.makeText(SolicitudOperadorActivity.this, "NO SE PUEDE HACER SNAPSHOT", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(noHay){
            super.onBackPressed();
        }else{
            Toast.makeText(this, "Si desea salir debe cancelar la solicitud", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if(listener != null){
                clienteTransaccionProvider.getEstado(authProvider.getId()).removeEventListener(listener);
            }
            super.onDestroy();
        }catch (Exception E){
            Log.d("TAG_", E.getMessage());
        }
    }
}