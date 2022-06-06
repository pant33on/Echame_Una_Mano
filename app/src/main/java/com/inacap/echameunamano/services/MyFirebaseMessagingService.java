package com.inacap.echameunamano.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import com.inacap.echameunamano.R;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.inacap.echameunamano.activities.operador.NotificacionActivity;
import com.inacap.echameunamano.channel.NotificationHelper;
import com.inacap.echameunamano.receivers.AceptarReceiver;
import com.inacap.echameunamano.receivers.CancelarReceiver;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final int NOTIFICATION_CODE = 100;

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

        String idCliente = data.get("idCliente");
        String origen = data.get("origen");
        String destino = data.get("destino");
        String tiempo = data.get("tiempo");
        String distancia = data.get("distancia");

        if(title != null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                if(title.contains(("SOLICITUD DE SERVICIO"))) {
                    muestraNotificacionOreoAction(title, body, idCliente);
                    muestraNotificacionActivity(idCliente, origen, destino, tiempo, distancia);
                }else if(title.contains(("VIAJE CANCELADO"))){
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(2);
                    muestraNotificacionOreo(title, body);
                }else{
                    muestraNotificacionOreo(title, body);
                }
            }else{
                if(title.contains(("SOLICITUD DE SERVICIO"))){
                    muestraNotificacionLegacyAction(title, body, idCliente);
                    muestraNotificacionActivity(idCliente, origen, destino, tiempo, distancia);
                }else if(title.contains(("VIAJE CANCELADO"))) {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(2);
                    muestraNotificacionLegacy(title, body);
                }else{
                    muestraNotificacionLegacy(title, body);
                }
            }
        }
    }

    private void muestraNotificacionActivity(String idCliente, String origen, String destino, String tiempo, String distancia) {
        //Para activar telefono aunque esté bloqueado
        PowerManager pm = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
        boolean estaPantallaEncendida = pm.isScreenOn();
        if(!estaPantallaEncendida){
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                     PowerManager.PARTIAL_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.ON_AFTER_RELEASE,
                            "AppName:MyLock"
            );
            wakeLock.acquire(10000);
        }
        Intent intent = new Intent(getBaseContext(), NotificacionActivity.class);
        intent.putExtra("idCliente", idCliente);
        intent.putExtra("origen", origen);
        intent.putExtra("destino", destino);
        intent.putExtra("tiempo", tiempo);
        intent.putExtra("distancia", distancia);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
    //Muestra notificacion Oreo + Actions
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void muestraNotificacionOreoAction(String titulo, String contenido, String idCliente) {
        //ACEPTAR ACCION
        Intent aceptarIntent = new Intent(this, AceptarReceiver.class);
        aceptarIntent.putExtra("idCliente", idCliente);
        PendingIntent aceptarPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, aceptarIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action aceptarAccion = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                aceptarPendingIntent
        ).build();

        //CANCELAR ACCION
        Intent cancelarIntent = new Intent(this, CancelarReceiver.class);
        cancelarIntent.putExtra("idCliente", idCliente);
        PendingIntent cancelarPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelarIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action cancelarAccion = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelarPendingIntent
        ).build();
        //lala
        Uri sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotificationOreoAction(titulo, contenido, sonido, aceptarAccion, cancelarAccion);
        notificationHelper.getManager().notify(2, builder.build());
    }

    //Muestra notificacion Legacy + Actions
    private void muestraNotificacionLegacyAction(String titulo, String contenido, String idCliente) {

        //ACEPTAR ACCION
        Intent aceptarIntent = new Intent(this, AceptarReceiver.class);
        aceptarIntent.putExtra("idCliente", idCliente);
        PendingIntent aceptarPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, aceptarIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action aceptarAccion = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                aceptarPendingIntent
        ).build();

        //CANCELAR ACCION
        Intent cancelarIntent = new Intent(this, CancelarReceiver.class);
        aceptarIntent.putExtra("idCliente", idCliente);
        PendingIntent cancelarPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelarIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action cancelarAccion = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelarPendingIntent
        ).build();

        Uri sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationLecagyAction(titulo, contenido, sonido,aceptarAccion, cancelarAccion);
        notificationHelper.getManager().notify(2, builder.build());
    }
}
