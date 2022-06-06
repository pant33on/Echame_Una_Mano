package com.inacap.echameunamano.retrofit;

import com.inacap.echameunamano.modelos.FCMBody;
import com.inacap.echameunamano.modelos.FCMResuesta;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAArHDof6s:APA91bE0K-L8H4tG0dJ4gYdosS1NdKw21YduURjsk96H4xVHUSBwNj7MeEvW28tLYfKdO-faBZUOuZPUxacicF47EnRLMDQpflx9wEIC0F51sw3Jib57RE3Bb3FHRO1wCoqMUQ9ilnJf"
    })
    @POST("fcm/send")
    Call<FCMResuesta> send(@Body FCMBody body);

}
