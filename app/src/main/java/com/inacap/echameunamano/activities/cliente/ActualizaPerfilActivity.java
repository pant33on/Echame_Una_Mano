package com.inacap.echameunamano.activities.cliente;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.activities.operador.ActualizarPerfilOperadorActivity;
import com.inacap.echameunamano.includes.MyToolbar;
import com.inacap.echameunamano.modelos.Cliente;
import com.inacap.echameunamano.modelos.Operador;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.ClienteProvider;
import com.inacap.echameunamano.providers.ImagenProvider;
import com.inacap.echameunamano.utils.CompressorBitmapImage;
import com.inacap.echameunamano.utils.FileUtil;
import com.squareup.picasso.Picasso;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActualizaPerfilActivity extends AppCompatActivity {
    private ImageView imageViewPerfil;
    private Button btnActualizaPerfil;
    private TextInputEditText etNombre;
    private ClienteProvider clienteProvider;
    private AuthProvider authProvider;
    private File imageFile;
    private String imagen = "";
    private final int GALLERY_REQUEST = 1;
    private ProgressDialog progressDialog;
    private String nombre;
    private CircleImageView btnVolver;

    private ImagenProvider imagenProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualiza_perfil);
        //MyToolbar.show(this, "Actualizar perfil", true);

        imageViewPerfil = findViewById(R.id.imageViewPerfil);
        btnActualizaPerfil = findViewById(R.id.btnActualizaPerfil);
        etNombre = findViewById(R.id.etNombre);
        clienteProvider = new ClienteProvider();
        authProvider = new AuthProvider();
        imagenProvider = new ImagenProvider("cliente_imagenes");
        btnVolver = findViewById(R.id.btnVolver);

        progressDialog = new ProgressDialog(this);
        getClienteInfo();
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
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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

    private void getClienteInfo(){
        clienteProvider.getCliente(authProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombre = snapshot.child("nombre").getValue().toString();
                    if(snapshot.hasChild("imagen")){
                        imagen = snapshot.child("imagen").getValue().toString();
                        Picasso.with(ActualizaPerfilActivity.this).load(imagen).into(imageViewPerfil);
                    }
                    etNombre.setText(nombre);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void actualizaPerfil() {
        nombre = etNombre.getText().toString();
        if (!nombre.equals("")){
            if(imageFile == null){
                progressDialog.setMessage("Espere un momento...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                guardarDatosSinImagen();
            }else{
                progressDialog.setMessage("Espere un momento...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                guardarDatosConImagen();
            }
        }else{
            Toast.makeText(this, "No pueden quedar campos vacíos", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarDatosConImagen() {
        imagenProvider.guardaImagen(ActualizaPerfilActivity.this, imageFile, authProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    imagenProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imagen = uri.toString();
                            Cliente cliente = new Cliente();
                            cliente.setImagen(imagen);
                            cliente.setNombre(nombre);
                            cliente.setId(authProvider.getId());
                            clienteProvider.actualizar(cliente).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ActualizaPerfilActivity.this, "Su información se actualizó correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }else{
                    Toast.makeText(ActualizaPerfilActivity.this, "Hubo un problema al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void guardarDatosSinImagen() {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setId(authProvider.getId());
        cliente.setImagen(imagen);
        clienteProvider.actualizar(cliente).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Toast.makeText(ActualizaPerfilActivity.this, "Su información se actualizó correctamente", Toast.LENGTH_SHORT).show();
            }
        });
    }
}