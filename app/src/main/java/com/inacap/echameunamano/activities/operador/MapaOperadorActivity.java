package com.inacap.echameunamano.activities.operador;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.MainActivity;
import com.inacap.echameunamano.activities.cliente.ActualizaPerfilActivity;
import com.inacap.echameunamano.activities.cliente.DashboardClienteActivity;
import com.inacap.echameunamano.activities.cliente.HistorialClienteActivity;
import com.inacap.echameunamano.activities.cliente.MapaClienteActivity;
import com.inacap.echameunamano.includes.MyToolbar;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.GeofireProvider;
import com.inacap.echameunamano.providers.HistorialProvider;
import com.inacap.echameunamano.providers.TokenProvider;

public class MapaOperadorActivity extends AppCompatActivity implements OnMapReadyCallback {

    //region Variables
    private AuthProvider authProvider;
    private GoogleMap mapa;
    private SupportMapFragment mapaFragment;
    private LocationRequest ubicacionRequest;
    private FusedLocationProviderClient ubicacionFused;
    private Marker marcador;
    private Button btnConectarse;
    private boolean estaConectado = false;
    private LatLng latLngActual;
    private GeofireProvider geofireProvider;
    private TokenProvider tokenProvider;
    private ValueEventListener listener;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private String idLoco;
    private HistorialProvider historialProvider;
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
                }
        }
    };

    private void actualizaUbicacion() {
        if(authProvider.existeSesion() && latLngActual != null){
            geofireProvider.guardaUbicacion(authProvider.getId(), latLngActual);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_operador);
        MyToolbar.show(this, "Operador", false);
        historialProvider = new HistorialProvider();
        authProvider = new AuthProvider();
        mapaFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapaFragment.getMapAsync(this);
        ubicacionFused = LocationServices.getFusedLocationProviderClient(this);
        geofireProvider = new GeofireProvider("Operadores_activos");

        tokenProvider = new TokenProvider();

        btnConectarse = findViewById(R.id.btnConectarse);
        btnConectarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(estaConectado){
                    desconectar();
                }else{
                    ubicacionDeInicio();
                }
            }
        });
        generaToken();
        estaElOperadorOcupado();
        llenaDashboard();
    }

    public void llenaDashboard(){
        idLoco = authProvider.getId();
        historialProvider.getCantServiciosOperador(idLoco);
    }

    private void estaElOperadorOcupado() {
        listener = geofireProvider.estaElOperadorOcupado(authProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //dejar de escuchar la ubicación en tiempo real del "LocationCallBack"
                    desconectar();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void logout() {
        //desconectar();
        authProvider.logout();
        Intent intent = new Intent(MapaOperadorActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
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
            btnConectarse.setText("Conectarse");
            estaConectado = false;
            //Deja de obtener ubicación en tiempo real
            ubicacionFused.removeLocationUpdates(ubicacionCallback);
            if(authProvider.existeSesion()){
                //Elimina referencia a nodo de Operadores_activos
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
                    btnConectarse.setText("Desconectarse");
                    estaConectado = true;
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
                                ActivityCompat.requestPermissions(MapaOperadorActivity.this
                                        ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                                        ,LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }else{
                ActivityCompat.requestPermissions(MapaOperadorActivity.this
                        ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                        ,LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.operador_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.operador_action_logout){
            desconectar();
            logout();
        }
        if(item.getItemId() == R.id.action_update){
            Intent intent = new Intent(MapaOperadorActivity.this, ActualizarPerfilOperadorActivity.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.action_historial){
            Intent intent = new Intent(MapaOperadorActivity.this, HistorialOperadorActivity.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.action_dashboard){
            Intent intent = new Intent(MapaOperadorActivity.this, DashboardOperadorActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    void generaToken(){
        tokenProvider.creaToken(authProvider.getId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(listener != null){
                geofireProvider.estaElOperadorOcupado(authProvider.getId()).removeEventListener(listener);
            }
        }catch (Exception E){
            Log.d("TAG_", E.getMessage());
        }
    }

}