package com.inacap.echameunamano.providers;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.inacap.echameunamano.R;
import com.inacap.echameunamano.retrofit.InterfaceGoogleApi;
import com.inacap.echameunamano.retrofit.RetrofitCliente;

import retrofit2.Call;

public class GoogleApiProvider {
    private Context context;

    public GoogleApiProvider(Context context){
        this.context = context;
    }

    public Call<String> getDirecciones(LatLng origenLatLng, LatLng destinoLatLng){
        String baseUrl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                     + "origin=" + origenLatLng.latitude + "," + origenLatLng.longitude + "&"
                     + "destination=" + destinoLatLng.latitude + "," + destinoLatLng.longitude + "&"
                     + "key=" + context.getResources().getString(R.string.google_maps_key);
        return RetrofitCliente.getCliente(baseUrl).create(InterfaceGoogleApi.class).getDirecciones(baseUrl + query);
    }
}
