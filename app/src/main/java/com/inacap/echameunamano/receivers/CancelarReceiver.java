package com.inacap.echameunamano.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.inacap.echameunamano.providers.ClienteTransaccionProvider;

public class CancelarReceiver extends BroadcastReceiver {

    private ClienteTransaccionProvider clienteTransaccionProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        String idCliente = intent.getExtras().getString("idCliente");
        clienteTransaccionProvider = new ClienteTransaccionProvider();
        clienteTransaccionProvider.actualizaEstado(idCliente, "cancelado");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);
    }
}
