package com.inacap.echameunamano.activities.cliente;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.MainActivity;
import com.inacap.echameunamano.activities.operador.MapaOperadorActivity;
import com.inacap.echameunamano.includes.MyToolbar;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.GeofireProvider;

import java.util.ArrayList;
import java.util.List;

public class MapaClienteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private AuthProvider authProvider;
    private GoogleMap mapa;
    private SupportMapFragment mapaFragment;
    private LocationRequest ubicacionRequest;
    private FusedLocationProviderClient ubicacionFused;
    private Marker marcador;
    private GeofireProvider geofireProvider;
    private LatLng latLngActual;
    private List<Marker> listaOperadores = new ArrayList<>();
    private boolean esPrimeraConexion = true;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private LocationCallback ubicacionCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location ubicacion : locationResult.getLocations())
                if (getApplicationContext() != null) {
                    if (marcador != null){
                        marcador.remove();
                    }

                    latLngActual = new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude());

                    marcador = mapa.addMarker(new MarkerOptions().position(
                            new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude())
                            )
                                    .title("Tu posición")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_mi_ubicacion2))
                    );
                    //Obtengo localización en tiempo real
                    mapa.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude()))
                                    .zoom(15f)
                                    .build()
                    ));
                    //Con esto me aseguro que método obtieneOperadores se ejecute una sola vez.
                    if(esPrimeraConexion){
                        esPrimeraConexion = false;
                        obtieneOperadores();
                    }
                }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_cliente);
        MyToolbar.show(this, "Cliente", false);
        authProvider = new AuthProvider();
        mapaFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapaFragment.getMapAsync(this);
        ubicacionFused = LocationServices.getFusedLocationProviderClient(this);
        geofireProvider = new GeofireProvider();

    }

    void logout() {
        authProvider.logout();
        Intent intent = new Intent(MapaClienteActivity.this, MainActivity.class);
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

    private void ubicacionDeInicio(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if(gpsActivado()){
                    ubicacionFused.requestLocationUpdates(ubicacionRequest, ubicacionCallback, Looper.myLooper());
                }else{
                    alertaNoGPS();
                }
            }else{
                revisaPermisos();
            }
        }else{
            if(gpsActivado()){
                ubicacionFused.requestLocationUpdates(ubicacionRequest, ubicacionCallback, Looper.myLooper());
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
                                ActivityCompat.requestPermissions(MapaClienteActivity.this
                                        ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                                        ,LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }else{
                ActivityCompat.requestPermissions(MapaClienteActivity.this
                        ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                        ,LOCATION_REQUEST_CODE);
            }
        }
    }

    //Método para obtener operadores cercanos utiliza método de clase GeofireProvider
    private void obtieneOperadores(){
       geofireProvider.obtieneOperadores(latLngActual).addGeoQueryEventListener(new GeoQueryEventListener() {
           @Override
           public void onKeyEntered(String key, GeoLocation location) {
               //Añadiremos marcadores de conductores conectados
               for (Marker marcador: listaOperadores){
                   if(marcador.getTag() != null){
                       if(marcador.getTag().equals(key)){
                           return;
                       }
                   }
               }
               LatLng posicionOperador = new LatLng(location.latitude,location.longitude);
               Marker marcador = mapa.addMarker(new MarkerOptions()
                       .position(posicionOperador)
                       .title("Operador disponible")
                       .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_grua))
               );
               marcador.setTag(key);
               listaOperadores.add(marcador);
           }
           //Remover operadores de la lista
           @Override
           public void onKeyExited(String key) {
               for (Marker marcador: listaOperadores){
                   if(marcador.getTag() != null){
                       if(marcador.getTag().equals(key)){
                           marcador.remove();
                           listaOperadores.remove(marcador);
                           return;
                       }
                   }
               }
           }
           @Override
           public void onKeyMoved(String key, GeoLocation location) {
                //Actualiza posición de cada operador
               for (Marker marcador: listaOperadores){
                   if(marcador.getTag() != null){
                       if(marcador.getTag().equals(key)){
                           marcador.setPosition(new LatLng(location.latitude,location.longitude));
                       }
                   }
               }
           }
           @Override
           public void onGeoQueryReady() {
           }

           @Override
           public void onGeoQueryError(DatabaseError error) {
           }
       });
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
        return super.onOptionsItemSelected(item);
    }
}