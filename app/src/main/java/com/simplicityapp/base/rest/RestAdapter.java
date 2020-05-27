package com.simplicityapp.base.rest;

import com.google.gson.GsonBuilder;
import com.simplicityapp.base.config.AppConfig;
import com.simplicityapp.base.connection.JsonAPI;
import com.simplicityapp.BuildConfig;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Retrofit;

public class RestAdapter {

    public static JsonAPI createAPI() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? Level.BODY : Level.NONE);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .cache(null)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.getWebURL())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .client(okHttpClient)
                .build();

        return retrofit.create(JsonAPI.class);
    }

    /**
     * createShortAPI use only for GCM registration
     * use 2 second only to connect and register
     */
    public static JsonAPI createShortAPI() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? Level.BODY : Level.NONE);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .cache(null)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.getWebURL())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .client(okHttpClient)
                .build();

        return retrofit.create(JsonAPI.class);
    }
}
