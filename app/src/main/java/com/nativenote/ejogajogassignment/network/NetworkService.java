package com.nativenote.ejogajogassignment.network;



import com.nativenote.ejogajogassignment.network.model.PlaceDetails;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface NetworkService {

    @GET("reverse?format=jsonv2")
    Observable<Response<PlaceDetails>> getPlaceData(
            @Query("lat") double lat,
            @Query("lon") double lon
    );
}
