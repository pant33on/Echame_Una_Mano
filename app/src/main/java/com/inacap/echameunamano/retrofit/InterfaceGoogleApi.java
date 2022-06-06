package com.inacap.echameunamano.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface InterfaceGoogleApi {

    @GET
    Call<String> getDirecciones(@Url String url);
}
