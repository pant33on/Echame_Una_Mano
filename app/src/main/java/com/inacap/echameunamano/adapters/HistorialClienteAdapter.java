package com.inacap.echameunamano.adapters;

import android.content.Context;
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
        holder.tvOrigen.setText(historial.getOrigen());
        holder.tvDestino.setText(historial.getDestino());
        holder.tvCalificacion.setText(String.valueOf(historial.getCalificacionOperador()));
        operadorProvider.getOperador(historial.getIdOperador()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
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
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_historial, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvNombre;
        private TextView tvOrigen;
        private TextView tvDestino;
        private TextView tvCalificacion;
        private ImageView imageHistorial;

        public  ViewHolder(View view){
            super(view);
            tvNombre = view.findViewById(R.id.tvNombre);
            tvOrigen = view.findViewById(R.id.tvOrigen);
            tvDestino = view.findViewById(R.id.tvDestino);
            tvCalificacion = view.findViewById(R.id.tvCalificacion);
            imageHistorial = view.findViewById(R.id.imageHistorial);
        }
    }

}
