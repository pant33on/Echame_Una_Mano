package com.inacap.echameunamano.activities.operador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.cliente.HistorialClienteActivity;
import com.inacap.echameunamano.adapters.HistorialClienteAdapter;
import com.inacap.echameunamano.adapters.HistorialOperadorAdapter;
import com.inacap.echameunamano.includes.MyToolbar;
import com.inacap.echameunamano.modelos.Historial;
import com.inacap.echameunamano.providers.AuthProvider;

import android.os.Bundle;

public class HistorialOperadorActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistorialOperadorAdapter adaptador;
    private AuthProvider authProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_operador);
        MyToolbar.show(this, "Historial", true);

        recyclerView = findViewById(R.id.rvHistorial);
        //Se requiere un LinearLayout para mostrar la información
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        authProvider = new AuthProvider();
        //Consulta base de datos
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("Historial")
                .orderByChild("idOperador")
                .equalTo(authProvider.getId());

        //Obtener opciones que me pide el constructor HistorialAdapter
        FirebaseRecyclerOptions<Historial> opciones = new FirebaseRecyclerOptions.Builder<Historial>()
                .setQuery(query, Historial.class)
                .build();
        //Instancia adaptador
        adaptador = new HistorialOperadorAdapter(opciones, HistorialOperadorActivity.this);

        recyclerView.setAdapter(adaptador);
        adaptador.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adaptador.stopListening();
    }
}