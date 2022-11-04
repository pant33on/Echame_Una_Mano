package com.inacap.echameunamano.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.R;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.inacap.echameunamano.activities.MainActivity;
import com.inacap.echameunamano.activities.cliente.DetalleHistorialClienteActivity;
import com.inacap.echameunamano.modelos.Historial;
import com.inacap.echameunamano.providers.OperadorProvider;
import com.squareup.picasso.Picasso;

public class HistorialClienteAdapter extends FirebaseRecyclerAdapter<Historial, HistorialClienteAdapter.ViewHolder> {

    private OperadorProvider operadorProvider;
    private Context miContext;

    public HistorialClienteAdapter(FirebaseRecyclerOptions<Historial> opciones, Context context){
        super(opciones);
        operadorProvider = new OperadorProvider();
        miContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Historial historial) {
        //Otra forma de obtener id:
        //String idHistorial = historial.getIdHistorial();
        String idHistorial = getRef(position).getKey();

        holder.tvOrigen.setText(historial.getOrigen());
        holder.tvDestino.setText(historial.getDestino());
        holder.tvCalificacion.setText(String.valueOf(historial.getCalificacionOperador()));
        String servicio = String.valueOf(historial.getTipoServicio());
        if(servicio.equals("servicio_grua")){
            holder.tvServicio.setText("Servicio de grúa");
            //MainActivity.contador.contar("grua");
        }else if(servicio.equals("servicio_bateria")){
            holder.tvServicio.setText("Servicio de batería");
            //MainActivity.contador.contar("bateria");
        }else if(servicio.equals("servicio_neumatico")){
            holder.tvServicio.setText("Servicio de neumático");
            //MainActivity.contador.contar("neumatico");
        }



        //Función para traer nombre e imagen de operador a partir de su Id
        operadorProvider.getOperador(historial.getIdOperador()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override//
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombre = snapshot.child("nombre").getValue().toString();
                    holder.tvNombre.setText(nombre);
                    if(snapshot.hasChild("imagen")){
                        String imagen = snapshot.child("imagen").getValue().toString();
                        Picasso.with(miContext).load(imagen).into(holder.imageHistorial);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        holder.miView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(miContext, DetalleHistorialClienteActivity.class);
                intent.putExtra("idHistorial", idHistorial);
                miContext.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public HistorialClienteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_historial, parent, false);
        return new HistorialClienteAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvNombre;
        private TextView tvOrigen;
        private TextView tvDestino;
        private TextView tvCalificacion;
        private TextView tvServicio;
        private ImageView imageHistorial;
        private View miView;

        public  ViewHolder(View view){
            super(view);
            tvNombre = view.findViewById(R.id.tvNombre);
            tvOrigen = view.findViewById(R.id.tvOrigen);
            tvDestino = view.findViewById(R.id.tvDestino);
            tvCalificacion = view.findViewById(R.id.tvCalificacion);
            tvServicio = view.findViewById(R.id.tvServicio);
            imageHistorial = view.findViewById(R.id.imageHistorial);
            miView = view;
        }
    }

}
