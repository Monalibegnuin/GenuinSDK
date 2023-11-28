package com.begnuine.library.core.interfaces;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface RetrofitInterface {
    @POST("{module}")
    Call<ResponseBody> doRequestPost(@Path(value = "module", encoded = true) String module, @Body RequestBody requestBody);

    @POST("{module}")
    Call<ResponseBody> doRequestPostWithHeader(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String module, @Body RequestBody requestBody);

    @POST("{module}")
    Call<ResponseBody> doRequestPostWithTempHeader(@Header("x-temp-auth-token") String token, @Path(value = "module", encoded = true) String module, @Body RequestBody requestBody);

    @PUT("{module}")
    Call<ResponseBody> doRequestPutWithHeader(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String module, @Body RequestBody requestBody);

    @PATCH("{module}")
    Call<ResponseBody> doRequestPatchWithHeader(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String module, @Body RequestBody requestBody);

    @GET("{module}")
    Call<ResponseBody> doRequestGetWithHeader(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path);

    @GET("{module}")
    Call<ResponseBody> doRequestGet(@Path(value = "module", encoded = true) String path);

    @GET("{module}")
    Call<ResponseBody> doRequestGetWithQuery(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path, @Query("last_chat_id") String chatId);

    @GET("{module}")
    Call<ResponseBody> doRequestGetShareURL(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path, @Query("type") String type);

    @GET("{module}")
    Call<ResponseBody> doRequestGetConversationDetails(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path, @Query("chat_id") String chatId);

    @GET("{module}")
    Call<ResponseBody> doSearchRequest(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path, @Query("text") String text);

    @GET("{module}")
    Call<ResponseBody> doRequestGetData(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path, @QueryMap(encoded = true) Map<String, Object> options);

    @GET("{module}")
    Call<ResponseBody> doRequestGetDataWithoutHeader(@Path(value = "module", encoded = true) String path, @QueryMap(encoded = true) Map<String, Object> options);

    @DELETE("{module}")
    Call<ResponseBody> doRequestDelete(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path);

    @DELETE("{module}")
    Call<ResponseBody> doRequestDeleteWithQuery(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path, @Query("chat_id") String chatId);

    @DELETE("{module}")
    Call<ResponseBody> doRequestDeleteCommunity(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path, @Query("community_id") String communityId);

    @DELETE("{module}")
    Call<ResponseBody> doRequestDeleteAllRecentSearches(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path, @Query("delete_all") String deleteAll);

    @DELETE("{module}")
    Call<ResponseBody> doRequestDeleteRecentSearch(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path, @Query("id") String id);

    @DELETE("{module}")
    Call<ResponseBody> doRequestDeleteComment(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path, @Query("comment_id") String commentId);

    @DELETE("{module}")
    Call<ResponseBody> doRequestDeleteVideo(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String path, @Query("chat_id") String chatId, @Query("conversation_id") String videoId);

    @Multipart
    @POST("{module}")
    Call<ResponseBody> postVideoThumbnail(@Header("x-auth-token") String token, @Path(value = "module", encoded = true) String module, @PartMap() Map<String, RequestBody> partMap
            , @Part MultipartBody.Part video_thumbnail, @Part MultipartBody.Part video_share_image);

    @PUT
    Call<ResponseBody> uploadFile(@Url String path, @Body RequestBody body);

}
