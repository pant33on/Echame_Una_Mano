package com.inacap.echameunamano.activities.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.includes.MyToolbar;
import com.inacap.echameunamano.providers.GoogleApiProvider;
import com.inacap.echameunamano.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleSolicitudActivity extends AppCompatActivity implements OnMapReadyCallback{
    private GoogleMap mapa;
    private SupportMapFragment mapaFragment;
    private double extraOrigenLat;
    private double extraOrigenLng;
    private double extraDestinoLat;
    private double extraDestinoLng;
    private LatLng origenLatLng;
    private LatLng destinoLatLng;
    private String extraNombreOrigen;
    private String extraNombreDestino;

    private GoogleApiProvider googleApiProvider;
    private List<LatLng> polyLineLista;
    private PolylineOptions polylineOpciones;
    private TextView tvOrigen;
    private TextView tvDestino;
    private TextView tvTiempo;
    private TextView tvDistancia;
    private Button btnSolicitarServicio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_solicitud);
        MyToolbar.show(this, "Datos de servicio", true);

        mapaFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapaFragment.getMapAsync(this);

        extraOrigenLat = getIntent().getDoubleExtra("origen_lat", 0);
        extraOrigenLng = getIntent().getDoubleExtra("origen_lng", 0);
        extraDestinoLat = getIntent().getDoubleExtra("destino_lat", 0);
        extraDestinoLng = getIntent().getDoubleExtra("destino_lng", 0);
        extraNombreOrigen = getIntent().getStringExtra("nombre_origen");
        extraNombreDestino = getIntent().getStringExtra("nombre_destino");

        origenLatLng = new LatLng(extraOrigenLat, extraOrigenLng);
        destinoLatLng = new LatLng(extraDestinoLat, extraDestinoLng);
        googleApiProvider = new GoogleApiProvider(DetalleSolicitudActivity.this);
        tvOrigen = findViewById(R.id.tvOrigen);
        tvDestino = findViewById(R.id.tvDestino);
        tvTiempo = findViewById(R.id.tvTiempo);
        tvDistancia = findViewById(R.id.tvDistancia);
        tvOrigen.setText(extraNombreOrigen);
        tvDestino.setText(extraNombreDestino);
        btnSolicitarServicio = findViewById(R.id.btnSolicitarServicio);

        btnSolicitarServicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irASolicitudOperador();
            }
        });
    }

    //AQUI ENVIAR MEDIANTE PUTEXTRA SELECCION DE SERVICIO
    private void irASolicitudOperador() {
        Intent intent = new Intent(DetalleSolicitudActivity.this, SolicitudOperadorActivity.class);
        intent.putExtra("origen_lat",origenLatLng.latitude);
        intent.putExtra("origen_lng",origenLatLng.longitude);
        intent.putExtra("origen",extraNombreOrigen);
        intent.putExtra("destino",extraNombreDestino);
        intent.putExtra("destino_lat",destinoLatLng.latitude);
        intent.putExtra("destino_lng",destinoLatLng.longitude);
        startActivity(intent);
        finish();
    }

    private void trazaRuta(){
        googleApiProvider.getDirecciones(origenLatLng, destinoLatLng).enqueue(new Callback<String>() {
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

                    tvTiempo.setText(duracionText);
                    tvDistancia.setText(distanciaText);

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

        mapa.addMarker(new MarkerOptions().position(origenLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_rojo)));
        mapa.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_azul)));

        mapa.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(origenLatLng)
                        .zoom(14f)
                        .build()
        ));
        trazaRuta();
    }
}