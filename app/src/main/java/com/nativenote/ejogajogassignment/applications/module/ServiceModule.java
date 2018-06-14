package com.nativenote.ejogajogassignment.applications.module;

import android.content.Context;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nativenote.ejogajogassignment.BuildConfig;
import com.nativenote.ejogajogassignment.applications.ApplicationContext;
import com.nativenote.ejogajogassignment.applications.ApplicationScope;
import com.nativenote.ejogajogassignment.network.DateTimeConverter;
import com.nativenote.ejogajogassignment.network.NetworkService;
import com.nativenote.ejogajogassignment.network.ServiceFactory;
import com.nativenote.ejogajogassignment.network.ToStringConverterFactory;
import com.nativenote.ejogajogassignment.network.error.ErrorHandlingExecutorCallAdapterFactory;

import org.joda.time.DateTime;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module(includes = NetworkModule.class)
public class ServiceModule {

    @Provides
    @ApplicationScope
    public Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
//                .registerTypeAdapter(new TypeToken<RealmList<Long>>() {
//                }.getType(), new LongRealmConverter())
//                .registerTypeAdapter(new TypeToken<RealmList<ProductImage>>() {
//                }.getType(), new ProductImageRealmConverter())
//                .registerTypeAdapter(new TypeToken<RealmList<String>>() {
//                }.getType(), new StringRealmConverter())
                .serializeNulls()
                .setLenient()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(DateTime.class, new DateTimeConverter());
        return gsonBuilder.create();
    }

    @Provides
    @ApplicationScope
    public Retrofit.Builder retrofit(OkHttpClient okHttpClient, Gson gson) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(new ToStringConverterFactory())
                .addCallAdapterFactory(new ErrorHandlingExecutorCallAdapterFactory(new ErrorHandlingExecutorCallAdapterFactory.MainThreadExecutor()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .baseUrl(BuildConfig.APIBASEURL);
    }

    @Provides
    @ApplicationScope
    public NetworkService networkService(Retrofit.Builder retrofit) {
        return retrofit.build().create(NetworkService.class);
    }

    @Provides
    @ApplicationScope
    @SuppressWarnings("unused")
    public ServiceFactory providesService(NetworkService networkService) {
        return new ServiceFactory(networkService);
    }

}
