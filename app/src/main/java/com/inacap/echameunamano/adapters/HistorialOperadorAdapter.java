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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.cliente.DetalleHistorialClienteActivity;
import com.inacap.echameunamano.activities.operador.DetalleHistorialOperadorActivity;
import com.inacap.echameunamano.modelos.Historial;
import com.inacap.echameunamano.providers.ClienteProvider;
import com.inacap.echameunamano.providers.OperadorProvider;
import com.squareup.picasso.Picasso;

public class HistorialOperadorAdapter extends FirebaseRecyclerAdapter<Historial, HistorialOperadorAdapter.ViewHolder> {

    private ClienteProvider clienteProvider;
    private Context miContext;

    public HistorialOperadorAdapter(FirebaseRecyclerOptions<Historial> opciones, Context context){
        super(opciones);
        clienteProvider = new ClienteProvider();
        miContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull HistorialOperadorAdapter.ViewHolder holder, int position, @NonNull Historial historial) {
        //Otra forma de obtener id:
        //String idHistorial = historial.getIdHistorial();
        String idHistorial = getRef(position).getKey();

        holder.tvOrigen.setText(historial.getOrigen());
        holder.tvDestino.setText(historial.getDestino());
        holder.tvCalificacion.setText(String.valueOf(historial.getCalificacionCliente()));
        String servicio = String.valueOf(historial.getTipoServicio());
        if(servicio.equals("servicio_grua")){
            holder.tvServicio.setText("Servicio de grúa");
        }else if(servicio.equals("servicio_bateria")){
            holder.tvServicio.setText("Servicio de batería");
        }else if(servicio.equals("servicio_neumatico")){
            holder.tvServicio.setText("Servicio de neumático");
        }

        clienteProvider.getCliente(historial.getIdCliente()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override//
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombre = snapshot.child("nombre").getValue().toString();
                    if(snapshot.hasChild("imagen")){
                        String imagen = snapshot.child("imagen").getValue().toString();
                        Picasso.with(miContext).load(imagen).into(holder.imageHistorial);
                    }
                    holder.tvNombre.setText(nombre);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        holder.miView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(miContext, DetalleHistorialOperadorActivity.class);
                intent.putExtra("idHistorial", idHistorial);
                miContext.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public HistorialOperadorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_historial, parent, false);
        return new HistorialOperadorAdapter.ViewHolder(view);
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
