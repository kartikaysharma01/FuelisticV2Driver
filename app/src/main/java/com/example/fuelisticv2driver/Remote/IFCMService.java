package com.example.fuelisticv2driver.Remote;

import com.example.fuelisticv2driver.Model.FCMResponse;
import com.example.fuelisticv2driver.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA2xulrrY:APA91bH_WpkBn3rYojNofGeeUmNGiWPIIBiCk6CJqF0_OBd9XcheuZaSG6hxfq_oeAwrOKao2tN2GzUalRvjOGA_xx8jCIbIbNwrJe9YlUS0sjGUcmh3JrfITLTDMaiDxUAQc-9rnu5f"

    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
