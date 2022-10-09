package com.inacap.echameunamano.providers;

import android.content.Context;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inacap.echameunamano.utils.CompressorBitmapImage;

import java.io.File;

public class ImagenProvider {
    private StorageReference miStorage;

    public ImagenProvider(String ref){
        miStorage = FirebaseStorage.getInstance().getReference().child(ref);
    }
    public UploadTask guardaImagen(Context context, File imagen, String idUsuario){
        byte[] imageByte = CompressorBitmapImage.getImage(context, imagen.getPath(), 500, 500);
        final StorageReference storage = miStorage.child(idUsuario + ".jpg");
        miStorage = storage;
        UploadTask uploadTask = storage.putBytes(imageByte);
        return uploadTask;
    }
    public StorageReference getStorage(){
        return miStorage;
    }

}
