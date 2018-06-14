package com.nativenote.ejogajogassignment.network;


import android.util.Log;

import com.nativenote.ejogajogassignment.network.model.PlaceDetails;


import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ServiceFactory {
    NetworkService networkService;

    public ServiceFactory(NetworkService networkService) {
        this.networkService = networkService;
    }

    public Subscription getPlaceDetails(double lat, double lon, final PlaceCallback callback) {
        return networkService.getPlaceData(lat, lon)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> Observable.error(throwable))
                .subscribe(new Subscriber<Response<PlaceDetails>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Response<PlaceDetails> response) {
                        if (response.code() == 200)
                            callback.onSuccess(response.body());
                        else
                            callback.onError("Error");
                    }
                });
    }

    public interface PlaceCallback {
        void onSuccess(PlaceDetails geoRoute);

        void onError(String errorMessage);
    }
}
