package com.inacap.echameunamano.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.inacap.echameunamano.channel.NotificationHelper;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        RemoteMessage.Notification notification = message.getNotification();
        Map<String, String> data = message.getData();
        String title = data.get("titulo");
        String body = data.get("contenido");

        if(title != null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                muestraNotificacionOreo(title, body);
            }else{
                muestraNotificacionLegacy(title, body);
            }
        }
    }

    private void muestraNotificacionLegacy(String titulo, String contenido) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext()
                ,0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationLecagy(titulo, contenido, intent, sonido);
        notificationHelper.getManager().notify(1, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void muestraNotificacionOreo(String titulo, String contenido) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext()
                ,0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotificationOreo(titulo, contenido, intent, sonido);
        notificationHelper.getManager().notify(1, builder.build());
    }
}
