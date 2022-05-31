package com.inacap.echameunamano.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;
import com.inacap.echameunamano.R;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class NotificationHelper extends ContextWrapper {
    private static final String CHANNEL_ID = "com.inacap.echameunamano";
    private static final String CHANNEL_NAME = "EchameUnaMano";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            crearCanales();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private  void crearCanales(){
        NotificationChannel notificationChannel = new
                NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
                );
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        //notificationChannel.setLightColor(android.R.color.darker_gray);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager(){
        if(manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
    //Configuración para Oreo o superior
    @RequiresApi(api = Build.VERSION_CODES.O)
    public  Notification.Builder getNotificationOreo (String titulo,
            String contenido, PendingIntent intent, Uri sonidoUri){
        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setAutoCancel(true)
                .setSound(sonidoUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_auto);
    }
    //Configuración para versiones inferiores a Oreo
    public  NotificationCompat.Builder getNotificationLecagy (String titulo,
            String contenido, PendingIntent intent, Uri sonidoUri){
        return new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setAutoCancel(true)
                .setSound(sonidoUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_auto);
    }
}
