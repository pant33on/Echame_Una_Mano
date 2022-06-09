package com.inacap.echameunamano.activities.operador;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.R;

import com.inacap.echameunamano.activities.cliente.SolicitudOperadorActivity;
import com.inacap.echameunamano.modelos.ClienteTransaccion;
import com.inacap.echameunamano.modelos.FCMBody;
import com.inacap.echameunamano.modelos.FCMResuesta;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.ClienteProvider;
import com.inacap.echameunamano.providers.ClienteTransaccionProvider;
import com.inacap.echameunamano.providers.GeofireProvider;
import com.inacap.echameunamano.providers.GoogleApiProvider;
import com.inacap.echameunamano.providers.NotificacionProvider;
import com.inacap.echameunamano.providers.TokenProvider;
import com.inacap.echameunamano.utils.DecodePoints;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaOperadorTransaccionActivity extends AppCompatActivity implements OnMapReadyCallback {
    //region Variables
    private AuthProvider authProvider;
    private GoogleMap mapa;
    private SupportMapFragment mapaFragment;
    private LocationRequest ubicacionRequest;
    private FusedLocationProviderClient ubicacionFused;
    private Marker marcador;
    private LatLng latLngActual;
    private GeofireProvider geofireProvider;
    private TokenProvider tokenProvider;
    private Button btnIniciarViaje;
    private Button btnFinalizarViaje;
    private NotificacionProvider notificacionProvider;


    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private String extraIdCliente;
    private ClienteProvider clienteProvider;
    private ClienteTransaccionProvider clienteTransaccionProvider;
    private TextView tvNombreClienteTransaccion;
    private TextView tvEmailClienteTransaccion;
    private TextView tvOrigenTransaccion;
    private TextView tvDestinoTransaccion;
    private boolean esPrimeraConexion = true;
    private boolean estaCerca = false;

    private LatLng origenLatLng;
    private LatLng destinoLatLng;
    private GoogleApiProvider googleApiProvider;
    private List<LatLng> polyLineLista;
    private PolylineOptions polylineOpciones;
    //endregion

    private LocationCallback ubicacionCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location ubicacion : locationResult.getLocations())
                if (getApplicationContext() != null) {
                    latLngActual = new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude());
                    if (marcador != null){
                        marcador.remove();
                    }
                    marcador = mapa.addMarker(new MarkerOptions().position(
                                            new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude())
                                    )
                                    .title("Tu posición")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_grua))
                    );

                    //Obtengo localización en tiempo real
                    mapa.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));
                    actualizaUbicacion();

                    if (esPrimeraConexion) {
                        esPrimeraConexion = false;
                        getClienteTransaccion();
                    }
                }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_operador_transaccion);
        authProvider = new AuthProvider();
        mapaFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapaFragment.getMapAsync(this);
        ubicacionFused = LocationServices.getFusedLocationProviderClient(this);
        geofireProvider = new GeofireProvider("Operadores_ocupados");
        tokenProvider = new TokenProvider();

        clienteProvider = new ClienteProvider();
        clienteTransaccionProvider = new ClienteTransaccionProvider();

        tvNombreClienteTransaccion = findViewById(R.id.tvNombreClienteTransaccion);
        tvEmailClienteTransaccion = findViewById(R.id.tvEmailClienteTransaccion);
        tvOrigenTransaccion = findViewById(R.id.tvOrigenTransaccion);
        tvDestinoTransaccion = findViewById(R.id.tvDestinoTransaccion);
        btnIniciarViaje = findViewById(R.id.btnIniciarViaje);
        btnFinalizarViaje = findViewById(R.id.btnFinalizarViaje);
        notificacionProvider = new NotificacionProvider();

        googleApiProvider = new GoogleApiProvider(MapaOperadorTransaccionActivity.this);
        extraIdCliente = getIntent().getStringExtra("idCliente");
        getCliente();

        //btnIniciarViaje.setEnabled(false);
        btnIniciarViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(estaCerca){
                    iniciarViaje();
                    //
                    estaCerca = false;
                }else{
                    Toast.makeText(MapaOperadorTransaccionActivity.this, "Debes estar más cerca del cliente para Iniciar Viaje", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnFinalizarViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalizarViaje();
            }
            //ACA AGREGAR VERIFICACIÓN QUE ESTÁ LLEGANDO A PUNTO DE DESTINO
            //ACA AGREGAR VERIFICACIÓN QUE ESTÁ LLEGANDO A PUNTO DE DESTINO


        });
    }

    private void finalizarViaje() {
        clienteTransaccionProvider.actualizaEstado(extraIdCliente, "finalizado");
        clienteTransaccionProvider.actualizaIdHistorial(extraIdCliente);
        enviaNotificacion("finalizado");
        if(ubicacionFused != null){
            ubicacionFused.removeLocationUpdates(ubicacionCallback);
        }
        geofireProvider.borraUbicacion(authProvider.getId());
        Intent intent = new Intent(MapaOperadorTransaccionActivity.this, CalificacionAlClienteActivity.class);
        intent.putExtra("idCliente", extraIdCliente);
        startActivity(intent);
        finish();
    }

    private void iniciarViaje() {
        clienteTransaccionProvider.actualizaEstado(extraIdCliente, "iniciado");
        btnIniciarViaje.setVisibility(View.GONE);
        btnFinalizarViaje.setVisibility(View.VISIBLE);

        //Elimino marcador y ruta trazada previamente
        mapa.clear();
        mapa.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_azul)));
        trazaRuta(destinoLatLng);
        enviaNotificacion("iniciado");
    }

    private double getDistanciaEntre(LatLng clienteLatLng, LatLng operadorLatLng){
        float distancia = 0;
        Location clienteUbicacion = new Location("");
        Location operadorUbicacion = new Location("");
        clienteUbicacion.setLatitude(clienteLatLng.latitude);
        clienteUbicacion.setLongitude(clienteLatLng.longitude);
        operadorUbicacion.setLatitude(operadorLatLng.latitude);
        operadorUbicacion.setLongitude(operadorLatLng.longitude);
        //SUPER función para traer la distancia (valor double) entre 2 posiciones LatLng
        distancia = operadorUbicacion.distanceTo(clienteUbicacion);
        return distancia;

    }

    private void getClienteTransaccion() {
        clienteTransaccionProvider.getClienteTransaccion(extraIdCliente).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String destino = snapshot.child("destino").getValue().toString();
                    String origen = snapshot.child("origen").getValue().toString();
                    double destinoLat = Double.parseDouble(snapshot.child("destinoLat").getValue().toString());
                    double destinoLng = Double.parseDouble(snapshot.child("destinoLng").getValue().toString());
                    double origenLat = Double.parseDouble(snapshot.child("origenLat").getValue().toString());
                    double origenLng = Double.parseDouble(snapshot.child("origenLng").getValue().toString());
                    destinoLatLng = new LatLng(destinoLat, destinoLng);
                    origenLatLng = new LatLng(origenLat, origenLng);
                    tvOrigenTransaccion .setText("Recoger en: " + origen);
                    tvDestinoTransaccion.setText("Destino: " + destino);

                    //agregar icono al punto de origen de cliente
                    mapa.addMarker(new MarkerOptions().position(origenLatLng).title("Recoger aquí").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_rojo)));

                    trazaRuta(origenLatLng);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void trazaRuta(LatLng latLng){
        googleApiProvider.getDirecciones(latLngActual, latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject ruta = jsonArray.getJSONObject(0);
                    JSONObject polylines = ruta.getJSONObject("overview_polyline");
                    String puntos = polylines.getString("points");

                    //Las polyLines son para ver el trazado de la ruta
                    polyLineLista = DecodePoints.decodePoly(puntos);
                    polylineOpciones = new PolylineOptions();
                    polylineOpciones.color(Color.DKGRAY);
                    polylineOpciones.width(10f);
                    polylineOpciones.startCap(new SquareCap());
                    polylineOpciones.jointType(JointType.ROUND);
                    polylineOpciones.addAll(polyLineLista);
                    mapa.addPolyline(polylineOpciones);

                    JSONArray legs = ruta.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanciaText = distance.getString("text");
                    String duracionText = duration.getString("text");

                } catch (Exception e){
                    Log.d("TAG_","Error: " + e.getMessage());
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    private void getCliente() {
        clienteProvider.getCliente(extraIdCliente).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombre = snapshot.child("nombre").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    tvNombreClienteTransaccion.setText("Nombre cliente: " + nombre);
                    tvEmailClienteTransaccion.setText("Email cliente: " + email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //AQUI DEFINO DISTANCIA MÍNIMA PARA QUE OPERADOR PUEDA ACEPTAR VIAJE
    //AQUI DEFINO DISTANCIA MÍNIMA PARA QUE OPERADOR PUEDA ACEPTAR VIAJE
    private void actualizaUbicacion() {
        if(authProvider.existeSesion() && latLngActual != null){
            geofireProvider.guardaUbicacion(authProvider.getId(), latLngActual);
            if(!estaCerca){
                if(origenLatLng != null && latLngActual != null){
                    double distancia = getDistanciaEntre(origenLatLng, latLngActual); //retorna valor en metros =)
                    if(distancia <= 200){
                        //btnIniciarViaje.setEnabled(true);
                        estaCerca = true;
                        Toast.makeText(this, "Estás llegando a la ubicación del cliente", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa.getUiSettings().setZoomControlsEnabled(true);

        ubicacionRequest = new LocationRequest();
        ubicacionRequest.setInterval(1000);
        ubicacionRequest.setFastestInterval(1000);
        ubicacionRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        ubicacionRequest.setSmallestDisplacement(5);

        ubicacionDeInicio();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if(gpsActivado()){
                        ubicacionFused.requestLocationUpdates(ubicacionRequest, ubicacionCallback, Looper.myLooper());
                        mapa.setMyLocationEnabled(false);
                    }else{
                        alertaNoGPS();
                    }
                } else {
                    revisaPermisos();
                }
            } else {
                revisaPermisos();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActivado()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            ubicacionFused.requestLocationUpdates(ubicacionRequest, ubicacionCallback, Looper.myLooper());
            mapa.setMyLocationEnabled(false);
        }else{
            alertaNoGPS();
        }
    }

    private void alertaNoGPS(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicación para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    private boolean gpsActivado(){
        boolean gpsActivo = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            gpsActivo = true;
        }
        return gpsActivo;
    }

    private void desconectar() {
        if(ubicacionFused != null){
            ubicacionFused.removeLocationUpdates(ubicacionCallback);
            if(authProvider.existeSesion()){
                geofireProvider.borraUbicacion(authProvider.getId());
            }
        }else{
            Toast.makeText(this, "No se puede desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void ubicacionDeInicio(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if(gpsActivado()){
                    ubicacionFused.requestLocationUpdates(ubicacionRequest, ubicacionCallback, Looper.myLooper());
                    mapa.setMyLocationEnabled(false);
                }else{
                    alertaNoGPS();
                }
            }else{
                revisaPermisos();
            }
        }else{
            if(gpsActivado()){
                ubicacionFused.requestLocationUpdates(ubicacionRequest, ubicacionCallback, Looper.myLooper());
                mapa.setMyLocationEnabled(false);
            }else{
                alertaNoGPS();
            }

        }
    }

    private void revisaPermisos(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicación requiere de los permisos de ubicación para poder utilizarse")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapaOperadorTransaccionActivity.this
                                        ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                                        ,LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }else{
                ActivityCompat.requestPermissions(MapaOperadorTransaccionActivity.this
                        ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                        ,LOCATION_REQUEST_CODE);
            }
        }
    }

    private void enviaNotificacion(final String estado) {
        tokenProvider.getToken(extraIdCliente).addListenerForSingleValueEvent(new ValueEventListener() {
            //El datasnapshot devuelve del nodo Token: idUsuario + Token
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //Con este método obtengo token del usuario.
                    String token = snapshot.child("token").getValue().toString();
                    Map<String, String> mapa = new HashMap<>();
                    mapa.put("titulo", "ESTADO DE TU VIAJE");
                    mapa.put("contenido",
                            "El estado de tu viaje es: " + estado);
                    FCMBody fcmBody = new FCMBody(token, "high", "4500s", mapa);
                    notificacionProvider.enviaNotificacion(fcmBody).enqueue(new Callback<FCMResuesta>() {
                        @Override
                        public void onResponse(Call<FCMResuesta> call, Response<FCMResuesta> response) {
                            //si llegó respuesta desde el servidor
                            if(response.body() != null){
                                if (response.body().getSuccess() != 1){
                                    Toast.makeText(MapaOperadorTransaccionActivity.this, "No se pudo enviar la notificación", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(MapaOperadorTransaccionActivity.this, "PROBLEMAS MAYORES", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResuesta> call, Throwable t) {
                            Log.d("TAG_", "Error" + t.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(MapaOperadorTransaccionActivity.this, "NO SE PUEDE HACER SNAPSHOT", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}