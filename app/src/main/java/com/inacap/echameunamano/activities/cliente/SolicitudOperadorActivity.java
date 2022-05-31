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
import com.inacap.echameunamano.modelos.ClienteTransaccion;
import com.inacap.echameunamano.modelos.FCMBody;
import com.inacap.echameunamano.modelos.FCMResuesta;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.ClienteProvider;
import com.inacap.echameunamano.providers.ClienteTransaccionProvider;
import com.inacap.echameunamano.providers.ElDato;
import com.inacap.echameunamano.providers.GeofireProvider;
import com.inacap.echameunamano.providers.GoogleApiProvider;
import com.inacap.echameunamano.providers.NotificacionProvider;
import com.inacap.echameunamano.providers.OperadorProvider;
import com.inacap.echameunamano.providers.TokenProvider;
import com.inacap.echameunamano.utils.DecodePoints;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolicitudOperadorActivity extends AppCompatActivity {
    private LottieAnimationView animacion;
    private TextView tvBuscando;
    private Button btnCancelarViaje;
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
    private LatLng origenLatLng;
    private LatLng destinoLatLng;
    private double radio = 0.1;
    private boolean operadorEncontrado = false;
    private String idOperadorEncontrado = "";
    private LatLng operadorEncontradoLatLng;
    private NotificacionProvider notificacionProvider;
    private TokenProvider tokenProvider;

    private ClienteTransaccionProvider clienteTransaccionProvider;
    private AuthProvider authProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitud_operador);

        animacion = findViewById(R.id.animacion);
        tvBuscando = findViewById(R.id.tvBuscando);
        btnCancelarViaje = findViewById(R.id.btnCancelarViaje);
        animacion.playAnimation();
        geofireProvider = new GeofireProvider();

        //TRAER DEL SHARED PREFERENCES EL TIPO DE SERVICIO
        preferencias = getApplicationContext().getSharedPreferences("tipoServicio", MODE_PRIVATE);

        extraNombreOrigen = getIntent().getStringExtra("origen");
        extraNombreDestino = getIntent().getStringExtra("destino");
        extraOrigenLat = getIntent().getDoubleExtra("origen_lat",0);
        extraOrigenLng = getIntent().getDoubleExtra("origen_lng",0);
        extraDestinoLat = getIntent().getDoubleExtra("destino_lat",0);
        extraDestinoLng = getIntent().getDoubleExtra("destino_lng",0);
        origenLatLng = new LatLng(extraOrigenLat, extraOrigenLng);
        destinoLatLng = new LatLng(extraDestinoLat, extraDestinoLng);

        obtenOperadorCercano();
        notificacionProvider = new NotificacionProvider();
        tokenProvider = new TokenProvider();
        clienteTransaccionProvider = new ClienteTransaccionProvider();
        authProvider = new AuthProvider();
        googleApiProvider = new GoogleApiProvider(SolicitudOperadorActivity.this);

    }

    private void obtenOperadorCercano(){
    geofireProvider.obtieneOperadores(origenLatLng, radio).addGeoQueryEventListener(new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            if(!operadorEncontrado) {
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
                radio = radio + 0.1f;
                //No encontró ningún conductor
                if(radio > 20){
                    tvBuscando.setText("No se encontró un operador");
                    Toast.makeText(SolicitudOperadorActivity.this, "No se encontró un operador", Toast.LENGTH_SHORT).show();
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
        referencia.child(tipoServicio).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    ElDato.dato = String.valueOf(task.getResult().getValue());
                    if(ElDato.dato.equals("Activo")){
                        //ElDato.dato = String.join(",", ElDato.respuesta);
                        crearClienteTransaccion();
                        tvBuscando.setText("Se ha encontrado un operador \nEsperando respuesta\n" + ElDato.dato);
                        //enviaNotificacion();
                    }else{
                        operadorEncontrado = false;
                        radio = radio + 0.1f;
                        //No encontró ningún conductor
                        if(radio > 20){
                            tvBuscando.setText("No se encontró un operador");
                            Toast.makeText(SolicitudOperadorActivity.this, "No se encontró un operador", Toast.LENGTH_SHORT).show();
                            return;
                        }else {
                            obtenOperadorCercano();
                        }
                    }
                }else{
                    ElDato.dato = "Hubo un problema con la task";
                    Log.d("TAG_","Error: " + task.getException().getMessage());
                }
            }
        });

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

    //Método para enviar notificacion
    private void enviaNotificacion(final String tiempo, final String km) {
        tokenProvider.getToken(idOperadorEncontrado).addListenerForSingleValueEvent(new ValueEventListener() {
            //El datasnapshot devuelve del nodo Token: idUsuario + Token
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //Con este método obtengo token del usuario.
                    String token = snapshot.child("token").getValue().toString();
                    Map<String, String> mapa = new HashMap<>();
                    mapa.put("titulo", "SOLICITUD DE SERVICIO A " + tiempo + "DE TU POSICIÓN");
                    mapa.put("contenido", "Un cliente está solicitando un servicio a una distancia de " + km);
                    FCMBody fcmBody = new FCMBody(token, "high", mapa);
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
                                            tiempo,
                                            km,
                                            "creado",
                                            origenLatLng.latitude,
                                            origenLatLng.longitude,
                                            destinoLatLng.latitude,
                                            destinoLatLng.longitude
                                    );
                                    clienteTransaccionProvider.crear(clienteTransaccion).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(SolicitudOperadorActivity.this, "La petición se creó correctamente", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    //Toast.makeText(SolicitudOperadorActivity.this, "La notificación se ha enviado correctamente", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SolicitudOperadorActivity.this, "NO SE PUEDE HACER SNAPSHOT", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}