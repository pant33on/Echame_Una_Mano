package com.inacap.echameunamano.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.inacap.echameunamano.activities.operador.MapaOperadorTransaccionActivity;
import com.inacap.echameunamano.providers.AuthProvider;
import com.inacap.echameunamano.providers.ClienteTransaccionProvider;
import com.inacap.echameunamano.providers.GeofireProvider;

public class AceptarReceiver extends BroadcastReceiver {
    private ClienteTransaccionProvider clienteTransaccionProvider;
    private GeofireProvider geofireProvider;
    private AuthProvider authProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        authProvider = new AuthProvider();
        geofireProvider = new GeofireProvider("Operadores_activos");
        geofireProvider.borraUbicacion(authProvider.getId());

        String idCliente = intent.getExtras().getString("idCliente");
        clienteTransaccionProvider = new ClienteTransaccionProvider();
        clienteTransaccionProvider.actualizaEstado(idCliente, "aceptado");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(context, MapaOperadorTransaccionActivity.class);
        //Con el FLAG puedo hacer que me abra la actividad aun estando fuera de la aplicación
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idCliente", idCliente);
        context.startActivity(intent1);
    }
}
