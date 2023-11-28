package com.begnuine.library.data.remote;

import com.begenuine.feedscreensdk.common.Constants;
import com.begnuine.library.core.interfaces.RetrofitInterface;

import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/* * ****************************************************************************
 * Author: Genuin
 *
 * Created: 1/12/2017
 * Purpose: For retrofit builder that will be used in call webservices.
 *
 * Change Log:
 * ===========
 * Name                          Change Date            Purpose
 * Vishal Nirmal               1/12/2017              Created.
 * ***************************************************************************** */
public class RetrofitBuilder {

    public static OkHttpClient.Builder httpClient;
    public static Dispatcher dispatcher;

    public RetrofitBuilder() {
        /*No need to define here anything*/
    }

    /**
     * Method to get webservice
     *
     * @return the instance of RetrofitInterface
     */
    public static RetrofitInterface getWebService() {
        return getBuilder().build().create(RetrofitInterface.class);
    }

    public static RetrofitInterface getV4WebService() {
        return getV4Builder().build().create(RetrofitInterface.class);
    }

//    public static RetrofitInterface getSearchWebService() {
//        return getSearchBuilder().build().create(RetrofitInterface.class);
//    }

    public static RetrofitInterface getDeepLinkWebService() {
        return getDeepLinkBuilder().build().create(RetrofitInterface.class);
    }

    public static RetrofitInterface getUploadWebService(String baseURL, ProgressInterceptor.ProgressListener progressListener){
        return getUploadFileBuilder(baseURL, progressListener).build().create(RetrofitInterface.class);
    }

    public static OkHttpClient.Builder getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder();
            //httpClient.addInterceptor(new DatadogInterceptor());
            //httpClient.eventListenerFactory(new DatadogEventListener.Factory());
            httpClient.readTimeout(90, TimeUnit.SECONDS);
            httpClient.connectTimeout(90, TimeUnit.SECONDS);
            httpClient.writeTimeout(90, TimeUnit.SECONDS);
            dispatcher = new Dispatcher();
            httpClient.dispatcher(dispatcher);
        }
        return httpClient;
    }

    public static void allRequestsCancel() {
        if (dispatcher != null) {
            dispatcher.cancelAll();
        }
    }


    /**
     * Method to get retofit builder object.
     *
     * @return object of the RetrofitBuilder
     */
    public static Retrofit.Builder getBuilder() {
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(Constants.BASE_URL);
        builder.addConverterFactory(ScalarsConverterFactory.create());
        builder.addConverterFactory(GsonConverterFactory.create());
        builder.client(getHttpClient().build());
        return builder;
    }

    public static Retrofit.Builder getV4Builder() {
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(Constants.BASE_URL_v4);
        builder.addConverterFactory(ScalarsConverterFactory.create());
        builder.addConverterFactory(GsonConverterFactory.create());
        builder.client(getHttpClient().build());
        return builder;
    }

//    public static Retrofit.Builder getSearchBuilder() {
//        Retrofit.Builder builder = new Retrofit.Builder();
//        builder.baseUrl(Constants.SEARCH_BASE_URL);
//        builder.addConverterFactory(GsonConverterFactory.create());
//        builder.client(getHttpClient().build());
//        return builder;
//    }

    public static Retrofit.Builder getDeepLinkBuilder() {
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(Constants.BASE_URL);
        builder.addConverterFactory(ScalarsConverterFactory.create());
        builder.addConverterFactory(GsonConverterFactory.create());
        builder.client(getHttpClient().build());
        return builder;
    }

    public static Retrofit.Builder getUploadFileBuilder(String baseURL, ProgressInterceptor.ProgressListener progressListener){
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(baseURL);
        builder.addConverterFactory(ScalarsConverterFactory.create());
        builder.addConverterFactory(GsonConverterFactory.create());
        builder.client(getUploadFileHTTPClient(progressListener).build());
        return builder;
    }

    public static OkHttpClient.Builder getUploadFileHTTPClient(ProgressInterceptor.ProgressListener progressListener){
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new ProgressInterceptor(progressListener));
        httpClient.readTimeout(900, TimeUnit.SECONDS);
        httpClient.connectTimeout(900, TimeUnit.SECONDS);
        httpClient.writeTimeout(900, TimeUnit.SECONDS);
        return httpClient;
    }
}