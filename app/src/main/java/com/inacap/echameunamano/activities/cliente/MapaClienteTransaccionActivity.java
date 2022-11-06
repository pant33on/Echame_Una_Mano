package com.inacap.echameunamano.activities.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.operador.MapaOperadorTransaccionActivity;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.ClienteTransaccionProvider;
import com.inacap.echameunamano.providers.GeofireProvider;
import com.inacap.echameunamano.providers.GoogleApiProvider;
import com.inacap.echameunamano.providers.OperadorProvider;
import com.inacap.echameunamano.providers.TokenProvider;
import com.inacap.echameunamano.utils.DecodePoints;
import com.squareup.picasso.Picasso;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaClienteTransaccionActivity extends AppCompatActivity implements OnMapReadyCallback {
    //region Variables
    private AuthProvider authProvider;
    private GoogleMap mapa;
    private SupportMapFragment mapaFragment;
    private Marker marcadorOperador;
    private GeofireProvider geofireProvider;
    private CameraPosition camaraPosicion;

    private PlacesClient lugares;
    private String nombreOrigen;
    private LatLng origenLatLng;
    private String nombreDestino;
    private LatLng destinoLatLng;
    private LatLng operadorLatLng;
    private boolean esPrimeraConexion = true;

    private TokenProvider tokenProvider;
    private ClienteTransaccionProvider clienteTransaccionProvider;
    private OperadorProvider operadorProvider;
    private ValueEventListener listener;
    private ValueEventListener listenerEstado;
    private String idOp;

    private GoogleApiProvider googleApiProvider;
    private List<LatLng> polyLineLista;
    private PolylineOptions polylineOpciones;

    private TextView tvNombreOperadorTransaccion;
    private TextView tvEmailOperadorTransaccion;
    private TextView tvOrigenTransaccion;
    private TextView tvDestinoTransaccion;
    private TextView tvEstadoTransaccion;
    private ImageView imageViewTransaccion;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_cliente_transaccion);

        authProvider = new AuthProvider();
        mapaFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapaFragment.getMapAsync(this);
        geofireProvider = new GeofireProvider("Operadores_ocupados");

        tokenProvider = new TokenProvider();
        clienteTransaccionProvider = new ClienteTransaccionProvider();
        googleApiProvider = new GoogleApiProvider(MapaClienteTransaccionActivity.this);
        operadorProvider = new OperadorProvider();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        tvNombreOperadorTransaccion = findViewById(R.id.tvNombreOperadorTransaccion);
        tvEmailOperadorTransaccion = findViewById(R.id.tvEmailOperadorTransaccion);
        tvOrigenTransaccion = findViewById(R.id.tvOrigenTransaccion);
        tvDestinoTransaccion = findViewById(R.id.tvDestinoTransaccion);
        tvEstadoTransaccion = findViewById(R.id.tvEstadoTransaccion);
        imageViewTransaccion = findViewById(R.id.imageViewTransaccion);

        getEstado();
        getClienteTransaccion();
    }

    private void getEstado() {
        listenerEstado = clienteTransaccionProvider.getEstado(authProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String estado = snapshot.getValue().toString();
                    tvEstadoTransaccion.setText("Estado: " + estado);

                    if(estado.equals("iniciado")){
                        iniciaTransaccion();
                    }else if(estado.equals("finalizado")){
                        finalizaTransaccion();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void finalizaTransaccion() {
        Intent intent = new Intent(MapaClienteTransaccionActivity.this, CalificacionAlOperadorActivity.class);
        startActivity(intent);
        finish();
    }

    private void iniciaTransaccion() {
        mapa.clear();
        mapa.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_azul)));
        trazaRuta(destinoLatLng);
    }

    private void getClienteTransaccion() {
        clienteTransaccionProvider.getClienteTransaccion(authProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String destino = snapshot.child("destino").getValue().toString();
                    String origen = snapshot.child("origen").getValue().toString();
                    String idOperador = snapshot.child("idOperador").getValue().toString();
                    idOp =idOperador;
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
                    getOperador(idOperador);
                    getUbicacionOperador(idOperador);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getOperador(String idOperador) {
        operadorProvider.getOperador(idOperador).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombre = snapshot.child("nombre").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    String imagen = "";
                    if(snapshot.hasChild("imagen")){
                        imagen = snapshot.child("imagen").getValue().toString();
                        Picasso.with(MapaClienteTransaccionActivity.this).load(imagen).into(imageViewTransaccion);
                    }
                    tvNombreOperadorTransaccion.setText("Nombre operador: " + nombre);
                    tvEmailOperadorTransaccion.setText("Email operador: " + email);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getUbicacionOperador(String idOperador) {
        listener = geofireProvider.getUbicacionOperador(idOperador).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    double lat = Double.parseDouble(snapshot.child("0").getValue().toString());
                    double lng = Double.parseDouble(snapshot.child("1").getValue().toString());
                    operadorLatLng = new LatLng(lat, lng);

                    if(marcadorOperador != null){
                        marcadorOperador.remove();
                    }
                    marcadorOperador = mapa.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title("Tu operador")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_grua)));
                    //Para trazar ruta solamente la primera vez
                    if(esPrimeraConexion){
                        esPrimeraConexion = false;
                        mapa.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(operadorLatLng)
                                        .zoom(14f)
                                        .build()
                        ));
                        trazaRuta(origenLatLng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void trazaRuta(LatLng latLng){
        googleApiProvider.getDirecciones(operadorLatLng, latLng).enqueue(new Callback<String>() {
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

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Servicio en proceso", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(listener != null){
                geofireProvider.getUbicacionOperador(idOp).removeEventListener(listener);
            }
            if(listenerEstado != null){
                clienteTransaccionProvider.getEstado(authProvider.getId()).removeEventListener(listenerEstado);
            }
        }catch (Exception E){
            Log.d("TAG_", E.getMessage());
        }
    }
}