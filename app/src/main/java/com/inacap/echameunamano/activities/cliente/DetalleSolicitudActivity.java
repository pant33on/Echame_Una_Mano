package com.inacap.echameunamano.activities.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.includes.MyToolbar;
import com.inacap.echameunamano.modelos.AjusteValor;
import com.inacap.echameunamano.providers.AjusteValorProvider;
import com.inacap.echameunamano.providers.GoogleApiProvider;
import com.inacap.echameunamano.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
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

    private String tiempoFinal;
    private String distanciaFinal;
    private double distanciaDouble;
    private double tiempoDouble;
    private double valorTotal;

    private SharedPreferences preferencias;
    private String tipoServicio = "";
    private GoogleApiProvider googleApiProvider;
    private AjusteValorProvider ajusteValorProvider;

    private List<LatLng> polyLineLista;
    private PolylineOptions polylineOpciones;
    private TextView tvOrigen;
    private TextView tvDestino;
    private TextView tvTiempo;
    private TextView tvDistancia;
    private TextView tvValor;
    private TextView tvServicio;
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
        ajusteValorProvider = new AjusteValorProvider();

        //TRAER DEL SHARED PREFERENCES EL TIPO DE SERVICIO
        preferencias = getApplicationContext().getSharedPreferences("tipoServicio", MODE_PRIVATE);

        tvOrigen = findViewById(R.id.tvOrigen);
        tvDestino = findViewById(R.id.tvDestino);
        tvTiempo = findViewById(R.id.tvTiempo);
        tvDistancia = findViewById(R.id.tvDistancia);
        tvValor = findViewById(R.id.tvValor);
        tvServicio = findViewById(R.id.tvServicio);
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
        intent.putExtra("tiempo",tiempoFinal);
        intent.putExtra("distancia",distanciaFinal);
        intent.putExtra("valor",valorTotal);
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
                    tiempoFinal = duration.getString("text");
                    distanciaFinal = distance.getString("text");

                    String[] distanciaConvert = distanciaFinal.split(" ");
                    distanciaDouble = Double.parseDouble(distanciaConvert[0]);
                    distanciaFinal = String.valueOf(distanciaDouble);

                    String[] tiempoConvert = tiempoFinal.split(" ");
                    tiempoDouble = Double.parseDouble(tiempoConvert[0]);

                    //Dar formato a tiempo
                    DecimalFormat formatea = new DecimalFormat("#.#");
                    String tiempoF = formatea.format(tiempoDouble);
                    tiempoFinal = tiempoF;

                    calculaPrecio(distanciaDouble);
                    tvTiempo.setText(tiempoFinal+" mins");
                    tvDistancia.setText(distanciaFinal+" km");

                } catch (Exception e){
                    Log.d("TAG_","Error: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void calculaPrecio(Double distanciaDouble) {
        ajusteValorProvider.getAjusteValor().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    AjusteValor ajusteValor = snapshot.getValue(AjusteValor.class);
                    double valorDistancia = distanciaDouble * ajusteValor.getKm();
                    double valorGrua = ajusteValor.getSer_grua();
                    double valorBateria = ajusteValor.getSer_bateria();
                    double valorNeumatico = ajusteValor.getSer_neumatico();

                    tipoServicio = preferencias.getString("servicio", "");
                    String tipo = "";
                    if(tipoServicio.equals("servicio_grua")){
                        valorTotal = valorDistancia + valorGrua;
                        tipo = "Grúa";
                    }else if(tipoServicio.equals("servicio_bateria")){
                        valorTotal = valorBateria;
                        tipo = "Batería";
                    }else if(tipoServicio.equals("servicio_neumatico")){
                        valorTotal = valorNeumatico;
                        tipo = "Neumático";
                    }
                    DecimalFormat formatea = new DecimalFormat("###,###.##");
                    String totalF = formatea.format(valorTotal);
                    String valorFinal = totalF.replace(",", ".");
                    tvValor.setText("$ "+valorFinal);
                    tvServicio.setText(tipo);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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