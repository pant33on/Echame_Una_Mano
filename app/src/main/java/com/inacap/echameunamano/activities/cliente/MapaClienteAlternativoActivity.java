package com.inacap.echameunamano.activities.cliente;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.MainActivity;
import com.inacap.echameunamano.includes.MyToolbar;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.ClienteTransaccionProvider;
import com.inacap.echameunamano.providers.GeofireProvider;
import com.inacap.echameunamano.providers.HistorialProvider;
import com.inacap.echameunamano.providers.TokenProvider;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapaClienteAlternativoActivity extends AppCompatActivity implements OnMapReadyCallback {

    //region Variables
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
    private AutocompleteSupportFragment autoCompletar;
    private AutocompleteSupportFragment autoCompletarDestino;
    private CameraPosition camaraPosicion;

    private PlacesClient lugares;
    private String nombreOrigen;
    private LatLng origenLatLng;
    private String nombreDestino;
    private LatLng destinoLatLng;
    private GoogleMap.OnCameraIdleListener camaraListener;
    private Button btnBuscarServicio;
    private TokenProvider tokenProvider;
    private ClienteTransaccionProvider clienteTransaccionProvider;

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

                    //Obtengo localización en tiempo real
                    if(camaraPosicion == null){
                        mapa.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude()))
                                        .zoom(15f)
                                        .build()
                        ));
                    }

                    //Con esto me aseguro que método obtieneOperadores se ejecute una sola vez.
                    if (esPrimeraConexion) {
                        esPrimeraConexion = false;
                        obtieneOperadores();
                        limitaBusqueda();
                    }
                }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_cliente_alternativo);
        MyToolbar.show(this, "Cliente", false);
        authProvider = new AuthProvider();
        historialProvider = new HistorialProvider();

        mapaFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapaFragment.getMapAsync(this);
        ubicacionFused = LocationServices.getFusedLocationProviderClient(this);
        geofireProvider = new GeofireProvider("Operadores_activos");

        btnBuscarServicio = findViewById(R.id.btnBuscarServicio);
        tokenProvider = new TokenProvider();
        clienteTransaccionProvider = new ClienteTransaccionProvider();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }
        lugares = Places.createClient(this);
        setPuntoOrigen();
        setPuntoDestino();
        onCameraMove();
        eliminarClienteTransaccion();

        btnBuscarServicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solicitarOperador();
            }
        });
        generaToken();
        llenaDashboard();
        //revisaPermisos();
    }

    private void eliminarClienteTransaccion() {
        clienteTransaccionProvider.eliminar(authProvider.getId());
    }

    //AÑADIR TIPO DE SERVICIO SELECCIONADO POR USUARIO
    //AÑADIR TIPO DE SERVICIO SELECCIONADO POR USUARIO
    private void solicitarOperador() {
        destinoLatLng = origenLatLng;
        nombreDestino = nombreOrigen;
        if(origenLatLng != null && destinoLatLng != null){

            Intent intent = new Intent(MapaClienteAlternativoActivity.this, DetalleSolicitudAlternativaActivity.class);
            intent.putExtra("origen_lat", origenLatLng.latitude);
            intent.putExtra("origen_lng", origenLatLng.longitude);
            intent.putExtra("destino_lat", destinoLatLng.latitude);
            intent.putExtra("destino_lng", destinoLatLng.longitude);
            intent.putExtra("nombre_origen", nombreOrigen);
            intent.putExtra("nombre_destino", nombreDestino);

            try {
                startActivity(intent);
                finish();
            }catch (Error E){
                Log.d("TAG_", "error : "+ E.getMessage().toString());
            }
        }else{
            Toast.makeText(this, "Debe seleccionar el lugar de origen y destino", Toast.LENGTH_SHORT).show();
        }
    }

    //Este metodo ajusta parámetros de búsqueda pero no está funcionando bien el sphericalUtil porque me muestra ubicaciones más lejos
    private void limitaBusqueda(){
        LatLng ladoNorte = SphericalUtil.computeOffset(latLngActual, 5000,0);
        LatLng ladoSur = SphericalUtil.computeOffset(latLngActual, 5000,180);
        autoCompletar.setCountry("CL");
        autoCompletar.setLocationBias(RectangularBounds.newInstance(ladoSur,ladoNorte));
        autoCompletarDestino.setCountry("CL");
        autoCompletarDestino.setLocationBias(RectangularBounds.newInstance(ladoSur,ladoNorte));
    }

    //Es para setear dirección en base a possicion de cámara.
    private void onCameraMove(){
        camaraListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder = new Geocoder(MapaClienteAlternativoActivity.this);
                    origenLatLng = mapa.getCameraPosition().target;
                    List<Address> listaDirecciones = geocoder.getFromLocation(origenLatLng.latitude, origenLatLng.longitude, 1);
                    String comuna = listaDirecciones.get(0).getLocality();
                    String numero = listaDirecciones.get(0).getFeatureName();
                    String ciudad = listaDirecciones.get(0).getAdminArea();
                    String region = listaDirecciones.get(0).getSubAdminArea();
                    String pais = listaDirecciones.get(0).getCountryName();
                    String codPost = listaDirecciones.get(0).getPostalCode();
                    String direccion = listaDirecciones.get(0).getAddressLine(0);

                    //Funciones para limpiar lo que trae el getAddressLine y dejar solo la dirección.
                    List<String> laDire = new ArrayList<String>(Arrays.asList(direccion.split(",")));
                    laDire.subList(1, laDire.size()).clear();
                    String dire = "";
                    for(String s : laDire){
                        dire = s;
                    }

                    nombreOrigen = dire+", "+comuna;
                    autoCompletar.setText(dire+", "+comuna);
                    nombreDestino = nombreOrigen;
                    autoCompletarDestino.setText(dire+", "+comuna);
                } catch (Exception e){
                    Log.e("TAG_","Excepción: " + e.getMessage());
                }
            }
        };
    }

    private void setPuntoOrigen() {
        autoCompletar = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAtocompleteOrigen);
        autoCompletar.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        autoCompletar.setHint("Punto de origen");
        autoCompletar.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                nombreOrigen = place.getName();
                origenLatLng = place.getLatLng();

                Log.d("PLACE", "Nombre: " + nombreOrigen);
                Log.d("PLACE", "Lat: " + origenLatLng.latitude);
                Log.d("PLACE", "Long: " + origenLatLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MapaClienteAlternativoActivity.this, "Algo ocurrió", Toast.LENGTH_SHORT).show();
                Log.d("TAG_", "error: " + status.toString());
            }
        });
    }

    private void setPuntoDestino() {
        autoCompletarDestino = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAtocompleteDestino);
        autoCompletarDestino.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        autoCompletarDestino.setHint("Destino");
        autoCompletarDestino.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                nombreDestino = nombreOrigen;
                destinoLatLng = origenLatLng;
                Log.d("PLACE", "Nombre: " + nombreDestino);
                Log.d("PLACE", "Lat: " + destinoLatLng.latitude);
                Log.d("PLACE", "Long: " + destinoLatLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MapaClienteAlternativoActivity.this, "Algo ocurrió", Toast.LENGTH_SHORT).show();
                Log.d("TAG_", "error: " + status.toString());
            }
        });
    }

    void logout() {
        authProvider.logout();
        Intent intent = new Intent(MapaClienteAlternativoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mapa.setMyLocationEnabled(true);
        mapa.setOnCameraIdleListener(camaraListener);

        //ubicacionRequest = LocationRequest.create();
        ubicacionRequest = new LocationRequest();
        ubicacionRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        ubicacionRequest.setInterval(5000);
        ubicacionRequest.setFastestInterval(1000);
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
                        mapa.setMyLocationEnabled(true);
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
            mapa.setMyLocationEnabled(true);
        }else if(requestCode == SETTINGS_REQUEST_CODE && gpsActivado()){
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
                    mapa.setMyLocationEnabled(true);
                }else{
                    alertaNoGPS();
                }
            }else{
                revisaPermisos();
            }
        }else{
            if(gpsActivado()){
                ubicacionFused.requestLocationUpdates(ubicacionRequest, ubicacionCallback, Looper.myLooper());
                mapa.setMyLocationEnabled(true);
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
                                ActivityCompat.requestPermissions(MapaClienteAlternativoActivity.this
                                        ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                                        ,LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }else{
                ActivityCompat.requestPermissions(MapaClienteAlternativoActivity.this
                        ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                        ,LOCATION_REQUEST_CODE);
            }
        }
    }
    //AQUI DEBO FILTRAR OPERADORES QUE OFREZCAN SERVICIO BUSCADO
    //AQUI DEBO FILTRAR OPERADORES QUE OFREZCAN SERVICIO BUSCADO
    //Método para obtener operadores cercanos utiliza método de clase GeofireProvider
    private void obtieneOperadores(){
        geofireProvider.obtieneOperadores(latLngActual, 20).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //Añadiremos marcadores de conductores conectados
                for (Marker marcador: listaOperadores){
                    if(marcador.getTag() != null){
                        //AQUI DEBO FILTRAR OPERADORES QUE OFREZCAN SERVICIO BUSCADO
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
            //AQUI DEBO FILTRAR OPERADORES QUE OFREZCAN SERVICIO BUSCADO
            //AQUI DEBO FILTRAR OPERADORES QUE OFREZCAN SERVICIO BUSCADO
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                //Actualiza posición de cada operador
                for (Marker marcador: listaOperadores){
                    if(marcador.getTag() != null){
                        //AQUI DEBO FILTRAR OPERADORES QUE OFREZCAN SERVICIO BUSCADO
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
        if(item.getItemId() == R.id.action_update){
            Intent intent = new Intent(MapaClienteAlternativoActivity.this, ActualizaPerfilActivity.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.action_historial){
            Intent intent = new Intent(MapaClienteAlternativoActivity.this, HistorialClienteActivity.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.action_dashboard){
            Intent intent = new Intent(MapaClienteAlternativoActivity.this, DashboardClienteActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    void generaToken(){
        tokenProvider.creaToken(authProvider.getId());
    }

    public void llenaDashboard(){
        idLoco = authProvider.getId();
        historialProvider.getCantServicios(idLoco);
    }
}