package com.inacap.echameunamano.providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeofireProvider {

    private DatabaseReference dataBse;
    private GeoFire geofire;

    public  GeofireProvider (){
        dataBse = FirebaseDatabase.getInstance().getReference().child("Operadores_activos");
        geofire = new GeoFire(dataBse);
    }

    public void guardaUbicacion(String id, LatLng latLgn){
        geofire.setLocation(id, new GeoLocation(latLgn.latitude, latLgn.longitude));
    }
    public void borraUbicacion(String id){
        geofire.removeLocation(id);
    }
    //Método para obtener conductores cercanos
    public GeoQuery obtieneOperadores(LatLng latLng){
        GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 5);
        geoQuery.removeAllListeners();
        return geoQuery;
    }
}
