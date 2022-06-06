package com.inacap.echameunamano.providers;

import com.inacap.echameunamano.modelos.FCMBody;
import com.inacap.echameunamano.modelos.FCMResuesta;
import com.inacap.echameunamano.retrofit.IFCMApi;
import com.inacap.echameunamano.retrofit.RetrofitCliente;

import retrofit2.Call;

public class NotificacionProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificacionProvider() {
    }

    public Call<FCMResuesta> enviaNotificacion(FCMBody body){
        return RetrofitCliente.getClienteObjeto(url).create(IFCMApi.class).send(body);
    }
}
