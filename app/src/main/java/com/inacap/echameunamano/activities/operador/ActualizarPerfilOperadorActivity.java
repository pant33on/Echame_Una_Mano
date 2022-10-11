package com.inacap.echameunamano.activities.operador;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.cliente.ActualizaPerfilActivity;
import com.inacap.echameunamano.includes.MyToolbar;
import com.inacap.echameunamano.modelos.Operador;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.ImagenProvider;
import com.inacap.echameunamano.providers.OperadorProvider;
import com.inacap.echameunamano.utils.FileUtil;
import com.squareup.picasso.Picasso;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class ActualizarPerfilOperadorActivity extends AppCompatActivity {

    private ImageView imageViewPerfil;
    private Button btnActualizaPerfil;
    private TextInputEditText etNombre;
    private TextInputEditText etMarcaVehiculo;
    private TextInputEditText etPatente;
    private AutoCompleteTextView acGrua;
    private AutoCompleteTextView acBateria;
    private AutoCompleteTextView acNeumatico;

    private String nombre;
    private String marcaVehiculo;
    private String patente;
    private String grua ="";
    private String bateria ="";
    private String neumatico ="";
    private String servicio_grua ="";
    private String servicio_bateria ="";
    private String servicio_neumatico ="";


    String[] item = {"Activo","Inactivo"};
    ArrayAdapter<String> adapterItem;

    private OperadorProvider operadorProvider;
    private AuthProvider authProvider;
    private File imageFile;
    private String urlImagen;
    private final int GALLERY_REQUEST = 1;
    private ProgressDialog progressDialog;
    private ImagenProvider imagenProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_perfil_operador);
        MyToolbar.show(this, "Actualizar perfil", true);

        imageViewPerfil = findViewById(R.id.imageViewPerfil);
        btnActualizaPerfil = findViewById(R.id.btnActualizaPerfil);
        etNombre = findViewById(R.id.etNombre);
        etMarcaVehiculo = findViewById(R.id.etMarcaVehiculo);
        etPatente = findViewById(R.id.etPatente);
        acGrua = findViewById(R.id.acGrua);
        acBateria = findViewById(R.id.acBateria);
        acNeumatico = findViewById(R.id.acNeumatico);

        //Método autocomplete (select)
        adapterItem = new ArrayAdapter<String>(this,R.layout.lista_items,item);

        operadorProvider = new OperadorProvider();
        authProvider = new AuthProvider();
        imagenProvider = new ImagenProvider("operador_imagenes");

        progressDialog = new ProgressDialog(this);

        getOperadorInfo();

        imageViewPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirGaleria();
            }
        });

        btnActualizaPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actualizaPerfil();
            }
        });
    }

    //Método AutoComplete
    private void iniciaAc(){
        acGrua.setAdapter(adapterItem);
        acGrua.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                grua = parent.getItemAtPosition(i).toString();
            }
        });
        acBateria.setAdapter(adapterItem);
        acBateria.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                bateria = parent.getItemAtPosition(i).toString();
            }
        });
        acNeumatico.setAdapter(adapterItem);
        acNeumatico.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                neumatico = parent.getItemAtPosition(i).toString();
            }
        });
    }

    private void abrirGaleria() {
        Intent galeriaIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galeriaIntent.setType("image/*");
        startActivityForResult(galeriaIntent, GALLERY_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            try {
                imageFile = FileUtil.from(this, data.getData());
                imageViewPerfil.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
            }catch (Exception e){
                Log.d("ERROR", "Mensaje: "+e.getMessage());
            }
        }
    }

    private void getOperadorInfo(){
        operadorProvider.getOperador(authProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombre = snapshot.child("nombre").getValue().toString();
                    String marcaVehiculo = snapshot.child("marcaVehiculo").getValue().toString();
                    String patente = snapshot.child("patente").getValue().toString();
                    servicio_grua = snapshot.child("servicio_grua").getValue().toString();
                    servicio_bateria = snapshot.child("servicio_bateria").getValue().toString();
                    servicio_neumatico = snapshot.child("servicio_neumatico").getValue().toString();
                    String imagen = "";
                    if(snapshot.hasChild("imagen")){
                        imagen = snapshot.child("imagen").getValue().toString();
                        Picasso.with(ActualizarPerfilOperadorActivity.this).load(imagen).into(imageViewPerfil);
                    }

                    etNombre.setText(nombre);
                    etMarcaVehiculo.setText(marcaVehiculo);
                    etPatente.setText(patente);
                    acGrua.setText(servicio_grua);
                    acBateria.setText(servicio_bateria);
                    acNeumatico.setText(servicio_neumatico);
                    iniciaAc();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void actualizaPerfil() {
        nombre = etNombre.getText().toString();
        marcaVehiculo = etMarcaVehiculo.getText().toString();
        patente = etPatente.getText().toString();

        if (!nombre.equals("") && !marcaVehiculo.equals("") && !patente.equals("")){
            progressDialog.setMessage("Espere un momento...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            guardarImagen();
        }else{
            Toast.makeText(this, "No pueden quedar campos vacíos", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarImagen() {
        imagenProvider.guardaImagen(ActualizarPerfilOperadorActivity.this, imageFile, authProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    imagenProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imagen = uri.toString();
                            Operador operador = new Operador();
                            operador.setNombre(nombre);
                            operador.setMarcaVehiculo(marcaVehiculo);
                            operador.setPatente(patente);
                            if(grua.isEmpty()){
                                operador.setGrua(servicio_grua);
                            }else{
                                operador.setGrua(grua);
                            }
                            if(bateria.isEmpty()){
                                operador.setBateria(servicio_bateria);
                            }else{
                                operador.setBateria(bateria);
                            }
                            if(neumatico.isEmpty()){
                                operador.setNeumatico(servicio_neumatico);
                            }else{
                                operador.setNeumatico(neumatico);
                            }

                            operador.setImagen(imagen);

                            operador.setId(authProvider.getId());

                            operadorProvider.actualizar(operador).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ActualizarPerfilOperadorActivity.this, "Su información se actualizó correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }else{
                    Toast.makeText(ActualizarPerfilOperadorActivity.this, "Hubo un problema al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}