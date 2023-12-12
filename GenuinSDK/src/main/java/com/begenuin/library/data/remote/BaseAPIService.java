package com.begenuin.library.data.remote;

import static com.begenuin.library.common.Utility.showLog;
import static com.begenuin.library.common.Utility.showLogException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;

import com.begenuin.library.R;
import com.begenuin.library.common.Constants;
import com.begenuin.library.common.Utility;
import com.begenuin.library.SDKInitiate;
import com.begenuin.library.core.interfaces.ResponseListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/* * ****************************************************************************
 * Author: Genuin
 *
 * Created: 1/24/2017
 * Purpose: For the Management of the complete request of the webservice from internet checking to response sending to respective listener.
 *
 * Change Log:
 * ===========
 * Name                          Change Date            Purpose
 * Vishal Nirmal               1/24/2017              Created.
 * ***************************************************************************** */
public class BaseAPIService {
    private static final String TAG_EXCEPTION = "TAG_EXCEPTION";
    private final Context context;
    private final ResponseListener responseListener; /*Custom interface for listening response success or failure.*/
    private final boolean isShowProgress; /*This variable is for deciding dynamically that you want to show dialog or not.*/
    public static ProgressDialog pd;
    private String reqModule;
    private boolean reqHeader = false;
    private Context reqContext;
    private String reqType, reqChatId;
    private Map<String, Object> reqMap;
    private RequestBody reqBody;
    private boolean isMultiPart = false;
    private final long startMillis;
    // For MultiPart
    private MultipartBody.Part tempMultipartBody;
    private HashMap<String, RequestBody> tempMapRequestBody;
    private MultipartBody.Part tempMultipartOptionalBody;

    private Call<ResponseBody> apiCall;

    /**
     * This is the constructor of the class
     *
     * @param context          of the class
     * @param module           name of the service
     * @param requestBody      request body of data
     * @param responseListener is the listener of response
     * @param isShowProgress   decides whether show progress bar or not
     */
    public BaseAPIService(final Context context, String module, RequestBody requestBody, boolean isHeader,
                          ResponseListener responseListener, String apiMethodType, boolean isShowProgress) {
        this.context = context;
        this.responseListener = responseListener;
        this.isShowProgress = isShowProgress;
        startMillis = System.currentTimeMillis();
        if (Utility.isNetworkAvailable(this.context)) {
            if (this.isShowProgress) {
                showProgressDialog(this.context);
            }
            showLog("URL", module);
            processRequest(module, requestBody, isHeader, context, apiMethodType);
        } else {
            responseListener.onFailure(Constants.NO_NETWORK);
        }
    }

    public BaseAPIService(final Context context, String module, boolean isHeader, String chatId, ResponseListener responseListener, String apiMethodType, boolean isShowProgress) {
        this.context = context;
        this.responseListener = responseListener;
        this.isShowProgress = isShowProgress;
        startMillis = System.currentTimeMillis();
        if (Utility.isNetworkAvailable(this.context)) {
            if (this.isShowProgress) {
                showProgressDialog(this.context);
            }
            showLog("URL", module);
            processGetORDeleteRequest(module, chatId, new HashMap<>(), isHeader, context, apiMethodType);
        } else {
            responseListener.onFailure(Constants.NO_NETWORK);
        }
    }

    public BaseAPIService(final Context context, String module, boolean isHeader, String chatId, Map<String, Object> map, ResponseListener responseListener, String apiMethodType, boolean isShowProgress) {
        this.context = context;
        this.responseListener = responseListener;
        this.isShowProgress = isShowProgress;
        startMillis = System.currentTimeMillis();
        if (Utility.isNetworkAvailable(this.context)) {
            if (this.isShowProgress) {
                showProgressDialog(this.context);
            }
            showLog("URL", module);
            processGetORDeleteRequest(module, chatId, map, isHeader, context, apiMethodType);
        } else {
            responseListener.onFailure(Constants.NO_NETWORK);
        }
    }

    public BaseAPIService(final Context context, String path, String searchText, ResponseListener responseListener, boolean isShowProgress) {
        this.context = context;
        this.responseListener = responseListener;
        this.isShowProgress = isShowProgress;
        startMillis = System.currentTimeMillis();
        if (Utility.isNetworkAvailable(this.context)) {
            showLog("URL", path);
            //processSearchRequest(path, searchText);
        } else {
            responseListener.onFailure(Constants.NO_NETWORK);
        }
    }

    public BaseAPIService(final Context context, String baseURL, String path, RequestBody requestBody, ResponseListener responseListener, ProgressInterceptor.ProgressListener progressListener) {
        this.context = context;
        this.responseListener = responseListener;
        this.isShowProgress = false;
        startMillis = System.currentTimeMillis();
        if (Utility.isNetworkAvailable(this.context)) {
            apiCall = RetrofitBuilder.getUploadWebService(baseURL, progressListener).uploadFile(path, requestBody);
            apiCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        try {
                            String res = response.body().string();
                            responseListener.onSuccess(res);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        responseListener.onFailure("Error");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    responseListener.onFailure("Error");
                }
            });
        } else {
            responseListener.onFailure(Constants.NO_NETWORK);
        }

    }

    public static boolean isShowingProgressDialog() {
        try {
            return pd != null && pd.isShowing();
        } catch (Exception e) {
            showLogException(e);
            return false;
        }
    }

    public void cancelCall() {
        if (apiCall != null) {
            apiCall.cancel();
            apiCall = null;
        }
    }

//    private void processSearchRequest(String path, String searchText) {
//        //String token = SharedPrefUtils.getStringPreference(context, Constants.PREF_XAT);
//        String token = "";
//        apiCall = RetrofitBuilder.getSearchWebService().doSearchRequest(token, path, searchText);
//        apiCall.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
//                try {
//                    if (response.code() == 200) {
//                        String res = response.body().string();
//                        responseListener.onSuccess(res);
//                        showLog("Response:", res);
//                    } else {
//                        JSONObject jsonObject;
//                        try {
//                            String responseBody = response.errorBody().string();
//                            jsonObject = new JSONObject(responseBody);
//                            String userMessage = jsonObject.optString("message", "");
//                            String code = jsonObject.optString("code", "");
//                            if (code.equalsIgnoreCase(Constants.VIDEO_ALREADY_DELETED_CODE)) {
//                                responseListener.onFailure(code);
//                            }
//                            if (!TextUtils.isEmpty(userMessage)) {
//                                Utility.showToast(context, userMessage);
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                } catch (Exception e) {
//                    showLogException(e);
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
//                try {
//                    showLog(TAG_EXCEPTION, t.getMessage());
//                    if (responseListener != null) {
//                        responseListener.onFailure(t.getMessage());
//                    }
//                } catch (Exception e) {
//                    showLogException(e);
//                }
//            }
//        });
//    }

    private void processMultiPartRequest(final String module, final MultipartBody.Part multipartBody, final HashMap<String, RequestBody> requestBody, final MultipartBody.Part multipartOptionalBody) {
        String token = SDKInitiate.INSTANCE.getAuthToken();
        //token = SharedPrefUtils.getStringPreference(context, Constants.PREF_XAT);
        showLog("token", token);
        apiCall = RetrofitBuilder.getWebService().postVideoThumbnail(token, module, requestBody, multipartBody, multipartOptionalBody);
        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200) {
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        String res = response.body().string();
                        responseListener.onSuccess(res);
                        showLog("Response:", res);
                    } else if (response.code() == 401) {
//                        if (module.equals(Constants.REFRESH_TOKEN)) {
//                            //Force Logout
//                            dismissProgressDialog();
//                            RefreshTokenManager.getInstance().status = RefreshTokenManager.RefreshTokenAPIStatus.FAILED;
//                            EventBus.getDefault().post(new APICallRefreshEvent());
//                            //Note: Here removing whole cache directory so explicitly not removing qrCode image folder
//                            clearDataAndRedirect();
//                        } else {
//                            reqContext = context;
//                            reqModule = module;
//                            isMultiPart = true;
//                            tempMapRequestBody = requestBody;
//                            tempMultipartBody = multipartBody;
//                            tempMultipartOptionalBody = multipartOptionalBody;
//                            if (RefreshTokenManager.getInstance().status != RefreshTokenManager.RefreshTokenAPIStatus.IN_PROGRESS && RefreshTokenManager.getInstance().status != RefreshTokenManager.RefreshTokenAPIStatus.COMPLETED) {
//                                RefreshTokenManager.getInstance().callRefreshTokenAPI(context);
//                            }
//                            EventBus.getDefault().register(BaseAPIService.this);
//                        }
                    } else {
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(response.errorBody().string());
                            String userMessage = jsonObject.getString("message");
                            Utility.showToast(context, userMessage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    showLogException(e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgressDialog();
                //showToast(context, context.getResources().getString(R.string.something_went_wrong_server));
                try {
                    showLog(TAG_EXCEPTION, t.getMessage());
                    responseListener.onFailure(t.getMessage());
                } catch (Exception e) {
                    showLogException(e);
                }
            }
        });
    }

    /**
     * Show progress dialog with message.
     *
     * @param context of the class
     */
    public static void showProgressDialog(Context context) {
        try {
            if (context != null) {
                if (!((Activity) context).isFinishing() && !isShowingProgressDialog()) {
                    pd = ProgressDialog.show(context, null, null, true, false);
                    pd.setContentView(R.layout.progress_bar);
                    pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    pd.show();
                }
            }
        } catch (Exception e) {
            showLogException(e);
        }
    }

    /**
     * Method is used to process the request by service name and request body sent to it
     *
     * @param module      Name of the service
     * @param requestBody body of the request
     */
    private void processRequest(final String module, final RequestBody requestBody, final boolean isHeader, final Context context, final String apiMethodType) {
        String token = SDKInitiate.INSTANCE.getAuthToken();
        showLog("token", token);
        switch (apiMethodType) {
            case "POST":
                if (isHeader) {
                    if (module.equalsIgnoreCase(Constants.SEARCH_API)) {
                        //apiCall = RetrofitBuilder.getSearchWebService().doRequestPostWithHeader(token, module, requestBody);
                    } else if (module.equalsIgnoreCase(Constants.COMPLETE_PROFILE)) {
                        //String tempToken = SharedPrefUtils.getStringPreference(context, Constants.PREF_TEMP_XAT);
                        String tempToken = SDKInitiate.INSTANCE.getAuthToken();
                        apiCall = RetrofitBuilder.getWebService().doRequestPostWithTempHeader(tempToken, module, requestBody);
                    } else {
                        apiCall = RetrofitBuilder.getWebService().doRequestPostWithHeader(token, module, requestBody);
                    }
                } else {
                    if (module.equalsIgnoreCase(Constants.SEARCH_API)) {
                       // apiCall = RetrofitBuilder.getSearchWebService().doRequestPost(module, requestBody);
                    } else {
                        apiCall = RetrofitBuilder.getWebService().doRequestPost(module, requestBody);
                    }
                }
                break;
            case "GET":
               // if (isLoggedIn()) {
                    apiCall = RetrofitBuilder.getWebService().doRequestGetWithHeader(token, module);
//                } else {
//                    apiCall = RetrofitBuilder.getWebService().doRequestGet(module);
//                }
                break;
            case "DELETE":
                apiCall = RetrofitBuilder.getWebService().doRequestDelete(token, module);
                break;
            case "PUT":
                apiCall = RetrofitBuilder.getWebService().doRequestPutWithHeader(token, module, requestBody);
                break;
            case "PATCH":
                apiCall = RetrofitBuilder.getWebService().doRequestPatchWithHeader(token, module, requestBody);
                break;
        }

        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                try {
                    Request request = response.raw().request();
                    String requestJsonStr = "";
                    if (requestBody != null) {
                        requestJsonStr = Utility.bodyToString(requestBody);
                    }
                    if (response.code() == 200) {
                        //sendAPISuccessLogs(request.url().toString());
//                        if (module.equalsIgnoreCase(Constants.VERIFY_OTP) || module.equalsIgnoreCase(Constants.VERIFY_NEW_MOBILE) || module.equalsIgnoreCase(Constants.COMPLETE_PROFILE) || module.equalsIgnoreCase(Constants.REFRESH_TOKEN)) {
//                            try {
//                                Headers headerList = response.headers();
//                                String tempToken = headerList.get(Constants.HEADER_TEMP_TOKEN);
//                                if (tempToken != null) {
//                                    SharedPrefUtils.setStringPreference(context, Constants.PREF_TEMP_XAT, tempToken);
//                                } else {
//                                    String token = headerList.get(Constants.HEADER_TOKEN);
//                                    SharedPrefUtils.setStringPreference(context, Constants.PREF_XAT, token);
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        if (module.equalsIgnoreCase(Constants.VIDEO_UPLOAD + Constants.AW_CREDENTIAL)) {
//                            try {
//                                SharedPrefUtils.setLongPreference(context, Constants.PREF_REFRESH_TIME, System.currentTimeMillis());
//                                Headers headerList = response.headers();
//                                SharedPrefUtils.setStringPreference(context, Constants.PREF_AWA, headerList.get("accessKeyId"));
//                                SharedPrefUtils.setStringPreference(context, Constants.PREF_AWS, headerList.get("secretAccessKey"));
//                                SharedPrefUtils.setStringPreference(context, Constants.PREF_AWT, headerList.get("sessionToken"));
//                                SharedPrefUtils.setStringPreference(context, Constants.PREF_AWB, headerList.get("bucketName"));
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        } else {
//                            if (isShowProgress) {
//                                dismissProgressDialog();
//                            }
//                        }
                        String res = response.body().string();
                        if (responseListener != null) {
                            dismissProgressDialog();
                            responseListener.onSuccess(res);
                        }
                        showLog("Response:", res);
                    } else if (response.code() == 404) {
                        String responseBody = response.errorBody().string();
                        //sendAPIFailureLogs(request.url().toString(), requestJsonStr, responseBody);
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        if (module.equalsIgnoreCase(Constants.GET_PROFILE)) {
                            if (responseListener != null) {
                                responseListener.onFailure("404");
                            }
                        }
//                        } else if (module.equalsIgnoreCase(Constants.SEND_REPLY)) {
//                            if (responseListener != null) {
//                                responseListener.onFailure("404");
//                            }
//                        } else if (module.equalsIgnoreCase(Constants.EMBED)) {
//                            if (responseListener != null) {
//                                responseListener.onFailure(responseBody);
//                            }
//                        } else if (module.equalsIgnoreCase(Constants.ADD_UPDATE_CUSTOM_QUESTION)) {
//                            if (responseListener != null) {
//                                responseListener.onFailure("404");
//                            }
//                        }
                    } else if (response.code() == 401) {
                        String responseBody = response.errorBody().string();
                        //sendAPIFailureLogs(request.url().toString(), requestJsonStr, responseBody);
                        if (module.equals(Constants.GET_PROFILE)) {
                            // Force Logout
                            dismissProgressDialog();
                            showLog("token", "Force Logout");
                            //RefreshTokenManager.getInstance().status = RefreshTokenManager.RefreshTokenAPIStatus.FAILED;
                            //EventBus.getDefault().post(new APICallRefreshEvent());
                            //Note: Here removing whole cache directory so explicitly not removing qrCode image folder
                            //clearDataAndRedirect();
                        } else if (module.equals(Constants.COMPLETE_PROFILE)) {
                            dismissProgressDialog();
                            responseListener.onFailure(responseBody);
                        } else {
                            reqContext = context;
                            reqHeader = isHeader;
                            reqModule = module;
                            reqType = apiMethodType;
                            reqBody = requestBody;
//                            showLog("RefreshToken", RefreshTokenManager.getInstance().status.name());
//                            if (RefreshTokenManager.getInstance().status != RefreshTokenManager.RefreshTokenAPIStatus.IN_PROGRESS && RefreshTokenManager.getInstance().status != RefreshTokenManager.RefreshTokenAPIStatus.COMPLETED) {
//                                RefreshTokenManager.getInstance().callRefreshTokenAPI(context);
//                            }
                            //EventBus.getDefault().register(BaseAPIService.this);
                        }
                    } else if (response.code() == 429) {
                        String responseBody = response.errorBody().string();
                        //sendAPIFailureLogs(request.url().toString(), requestJsonStr, responseBody);
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        if (module.equalsIgnoreCase(Constants.HOME) || module.equalsIgnoreCase(Constants.SEARCH_API)) {
                            if (responseListener != null) {
                                responseListener.onFailure("429");
                            }
                        }
                    } else {
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        JSONObject jsonObject;
                        try {
                            String responseBody = response.errorBody().string();
                            //sendAPIFailureLogs(request.url().toString(), requestJsonStr, responseBody);
                            jsonObject = new JSONObject(responseBody);
                            String userMessage = jsonObject.optString("message", "");
                            String code = jsonObject.optString("code", "");
//                            if (module.equalsIgnoreCase(Constants.CAN_INVITE_ALL)) {
//                                if (responseListener != null) {
//                                    responseListener.onFailure(code);
//                                }
//                            } else if (module.equalsIgnoreCase(Constants.EMBED)) {
//                                if (responseListener != null) {
//                                    responseListener.onFailure(responseBody);
//                                }
//                            } else if (module.equalsIgnoreCase(Constants.INVITE_ALL)) {
//                                if (code.equalsIgnoreCase(Constants.CODE_5160)) {
//                                    responseListener.onFailure(responseBody);
//                                } else {
//                                    responseListener.onFailure(code);
//                                }
//                            } else if (code.equalsIgnoreCase(Constants.VIDEO_ALREADY_DELETED_CODE)) {
//                                if (responseListener != null) {
//                                    responseListener.onFailure(code);
//                                }
//                            } else if (code.equalsIgnoreCase(Constants.DELETED_ACCOUNT_CODE)) {
//                                if (responseListener != null) {
//                                    responseListener.onFailure(code);
//                                }
//                            } else if (module.contains(Constants.SAVE_VIDEO) || module.contains(Constants.UNSAVE_VIDEO) || module.contains(Constants.SYNC_CONTACTS)) {
//                                if (responseListener != null) {
//                                    responseListener.onFailure(code);
//                                }
//                            } else if (module.equalsIgnoreCase(Constants.UPDATE_ENTITIES)) {
//                                if (responseListener != null) {
//                                    responseListener.onFailure(code);
//                                }
//                            } else if (code.equalsIgnoreCase(Constants.CODE_5061) || code.equalsIgnoreCase(Constants.CODE_5026) || code.equalsIgnoreCase(Constants.CODE_5082) ||
//                                    code.equalsIgnoreCase(Constants.CODE_5090) || code.equalsIgnoreCase(Constants.CODE_5095) || code.equalsIgnoreCase(Constants.CODE_5096) ||
//                                    code.equalsIgnoreCase(Constants.CODE_5097) || code.equalsIgnoreCase(Constants.CODE_5025) || code.equalsIgnoreCase(Constants.CODE_5057) ||
//                                    code.equalsIgnoreCase(Constants.CODE_5044) || code.equalsIgnoreCase(Constants.CODE_5094) || code.equalsIgnoreCase(Constants.CODE_5059) ||
//                                    code.equalsIgnoreCase(Constants.CODE_5154) || code.equalsIgnoreCase(Constants.CODE_5093) || code.equalsIgnoreCase(Constants.CODE_5060) ||
//                                    code.equalsIgnoreCase(Constants.CODE_5178) || code.equalsIgnoreCase(Constants.CODE_5175) || code.equalsIgnoreCase(Constants.CODE_5156) ||
//                                    code.equalsIgnoreCase(Constants.CODE_5198) || code.equalsIgnoreCase(Constants.CODE_5216) || code.equalsIgnoreCase(Constants.CODE_5101)) {
//                                if (responseListener != null) {
//                                    if (module.contains(Constants.CHECK_VIDEO) && (code.equalsIgnoreCase(Constants.CODE_5095) || code.equalsIgnoreCase(Constants.CODE_5096))) {
//                                        responseListener.onFailure(code + userMessage);
//                                    } else if (module.equalsIgnoreCase(Constants.VALIDATE_NEW_MOBILE) && code.equalsIgnoreCase(Constants.CODE_5094)) {
//                                        responseListener.onFailure(code + userMessage);
//                                    } else if (module.contains(Constants.SEND_REPLY) && code.equalsIgnoreCase(Constants.CODE_5057)) {
//                                        responseListener.onFailure(responseBody);
//                                    } else if (module.contains(Constants.SEND_REACTION) && code.equalsIgnoreCase(Constants.CODE_5061)) {
//                                        responseListener.onFailure(responseBody);
//                                    } else if (module.equalsIgnoreCase(Constants.CREATE_PUBLIC_VIDEO) && code.equalsIgnoreCase(Constants.CODE_5156)) {
//                                        responseListener.onFailure(responseBody);
//                                    } else if (module.equalsIgnoreCase(Constants.CREATE_COMMENT) && code.equalsIgnoreCase(Constants.CODE_5175)) {
//                                        responseListener.onFailure(responseBody);
//                                    } else if (module.equals(Constants.COMPLETE_PROFILE) || module.equals(Constants.UPDATE_USER_PROFILE)) {
//                                        responseListener.onFailure(responseBody);
//                                    } else if (module.equalsIgnoreCase(Constants.CREATE_COMMUNITY) && code.equalsIgnoreCase(Constants.CODE_5216)) {
//                                        responseListener.onFailure(responseBody);
//                                    } else {
//                                        responseListener.onFailure(userMessage);
//                                    }
//                                }
//                            } else if (!TextUtils.isEmpty(userMessage) && responseListener != null) {
//                                if (!module.contains(Constants.LOOP_MESSAGE_PIN) && !module.contains(Constants.SUBSCRIBE_RT) && !module.contains(Constants.READ) && !module.contains(Constants.STORE_UTM_DATA) && !module.contains(Constants.GET_UPLOAD_URL) && !module.contains(Constants.VERIFY_OTP) && !module.contains(Constants.VIEW_VIDEO) && !module.contains(Constants.SAVE_VIDEO) && !module.contains(Constants.UNSAVE_VIDEO) && !module.contains(Constants.EDIT_GROUP)) {
//                                    Utility.showToast(context, userMessage);
//                                }
//
//                                responseListener.onFailure(userMessage);
//
//                                if (module.contains(Constants.VALIDATE_USER) || module.contains(Constants.USER_LOGIN)) {
//
//                                    long start = SharedPrefUtils.getLongPreference(context, "login_duration");
//                                    long duration = System.currentTimeMillis() - start;
//                                    HashMap<String, Object> map;
//                                    map = new HashMap<String, Object>() {{
//                                        put("latency", duration);
//                                    }};
//                                   // GenuInApplication.getInstance().sendEventLogs(Constants.SIGN_IN_FAILED, map);
//                                } else if (module.contains(Constants.COMPLETE_PROFILE)) {
//                                    String token = SharedPrefUtils.getStringPreference(context, Constants.PREF_XAT);
//                                    if (TextUtils.isEmpty(token)) {
//                                        long start = SharedPrefUtils.getLongPreference(context, "login_duration");
//                                        long duration = System.currentTimeMillis() - start;
//                                        HashMap<String, Object> map;
//                                        map = new HashMap<String, Object>() {{
//                                            put("latency", duration);
//                                        }};
//                                       // GenuInApplication.getInstance().sendEventLogs(Constants.SIGN_UP_FAILED, map);
//                                    }
//                                }
//                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    Utility.showLogException(e);
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                dismissProgressDialog();
                try {
                    showLog(TAG_EXCEPTION, t.getMessage());
//                    if (module.contains(Constants.CAN_INVITE_ALL) || module.contains(Constants.INVITE_ALL)) {
//                        if (responseListener != null) {
//                            responseListener.onFailure("");
//                        }
//                    } else if (module.contains(Constants.CHECK_VIDEO)) {
//                        Utility.showToast(context, Constants.SOMETHING_WENT_WRONG_MSG);
//                    } else if (module.contains(Constants.CREATE_COMMUNITY)) {
//                        if (responseListener != null) {
//                            responseListener.onFailure("error");
//                        }
//                    } else if (!module.contains(Constants.READ) && !module.contains(Constants.VIEW_VIDEO) && !module.contains(Constants.SHARE_COUNT) && !module.contains(Constants.CLICK_COUNT)) {
//                        if (responseListener != null) {
//                            responseListener.onFailure(t.getMessage());
//                        }
//                    }
//                    String requestJsonStr = "";
//                    if (requestBody != null) {
//                        requestJsonStr = Utility.bodyToString(requestBody);
//                    }
//                    sendAPIFailureLogs(call.request().url().toString(), requestJsonStr, t.getMessage());
                } catch (Exception e) {
                    Utility.showLogException(e);
                }
            }
        });
    }

//    public boolean isLoggedIn() {
//        return SharedPrefUtils.getBoolPreference(context, Constants.PREF_LOGIN);
//    }

    private void processGetORDeleteRequest(final String module, final String chatId, final Map<String, Object> map, final boolean isHeader, final Context context, final String apiMethodType) {
        //String token = SharedPrefUtils.getStringPreference(context, Constants.PREF_XAT);
        String token = SDKInitiate.INSTANCE.getAuthToken();
        showLog("token", token);
        if (apiMethodType.equalsIgnoreCase("DELETE_COMMENT")) {
            apiCall = RetrofitBuilder.getWebService().doRequestDeleteComment(token, module, chatId);
        } else if (apiMethodType.equalsIgnoreCase("GET_DATA")) {
            if (TextUtils.isEmpty(token)) {
                if (module.equalsIgnoreCase(Constants.SEARCH_API)) {
                    //apiCall = RetrofitBuilder.getSearchWebService().doRequestGetDataWithoutHeader(module, map);
                } else {
                    apiCall = RetrofitBuilder.getWebService().doRequestGetDataWithoutHeader(module, map);
                }
            } else {
                if (module.equalsIgnoreCase(Constants.SEARCH_API)) {
                    //apiCall = RetrofitBuilder.getSearchWebService().doRequestGetData(token, module, map);
                } else if (module.equalsIgnoreCase(Constants.LOOP_RECENT_UPDATES)) {
                    apiCall = RetrofitBuilder.getV4WebService().doRequestGetData(token, module, map);
                } else {
                    apiCall = RetrofitBuilder.getWebService().doRequestGetData(token, module, map);
                }
            }
        } else if (apiMethodType.equalsIgnoreCase("DELETE_WITH_QUERY")) {
            if (map.containsKey("videoId")) {
                apiCall = RetrofitBuilder.getWebService().doRequestDeleteVideo(token, module, chatId, map.get("videoId").toString());
            } else if(map.containsKey("community_id")) {
                apiCall = RetrofitBuilder.getWebService().doRequestDeleteCommunity(token, module, map.get("community_id").toString());
            } else if (module.equalsIgnoreCase(Constants.DELETE_SEARCH_RECENTS)) {
                if (map.containsKey("delete_all")) {
                    apiCall = RetrofitBuilder.getWebService().doRequestDeleteAllRecentSearches(token, module, (String) map.getOrDefault("delete_all", "true"));
                } else {
                    apiCall = RetrofitBuilder.getWebService().doRequestDeleteRecentSearch(token, module, map.getOrDefault("id", "").toString());
                }
            } else {
                apiCall = RetrofitBuilder.getWebService().doRequestDeleteWithQuery(token, module, chatId);
            }
        } else if (apiMethodType.equalsIgnoreCase("GET_CONV_DETAILS")) {
            apiCall = RetrofitBuilder.getWebService().doRequestGetConversationDetails(token, module, chatId);
        } else if (apiMethodType.equalsIgnoreCase("GET_SHARE_URL")) {
            apiCall = RetrofitBuilder.getWebService().doRequestGetShareURL(token, module, chatId);
        } else {
            if (TextUtils.isEmpty(chatId)) {
                apiCall = RetrofitBuilder.getWebService().doRequestGetWithHeader(token, module);
            } else {
                apiCall = RetrofitBuilder.getWebService().doRequestGetWithQuery(token, module, chatId);
            }
        }
        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                try {
                    Request request = response.raw().request();
                    if (response.code() == 200) {
                        if (isShowProgress) {
                            showLog("Tag", module);
                            dismissProgressDialog();
                        }
                        String res = response.body().string();
                        if (responseListener != null) {
                            responseListener.onSuccess(res);
                        }
                        showLog("Response:", res);
                    } else if (response.code() == 412) {
                        String responseBody = response.errorBody().string();
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        if (responseListener != null) {
                            responseListener.onFailure("412");
                        }
                    } else if (response.code() == 429) {
                        String responseBody = response.errorBody().string();
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        if (responseListener != null) {
                            responseListener.onFailure("429");
                        }
                    } else if (response.code() == 404 || response.code() == 403) {

                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        JSONObject jsonObject;
                        try {
                            String responseBody = response.errorBody().string();
                            jsonObject = new JSONObject(responseBody);
                            String userMessage = jsonObject.optString("message", "");
                            String code = jsonObject.optString("code", "");
                            if (responseListener != null) {
                                if(module.contains(Constants.GET_COMMUNITY)){
                                    responseListener.onFailure("404");
                                }else {
                                    responseListener.onFailure(userMessage);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.code() == 401) {
                        String responseBody = response.errorBody().string();
//                        sendAPIFailureLogs(request.url().toString(), "", responseBody);
//                        if (module.equals(Constants.REFRESH_TOKEN)) {
//                            //Force Logout
//                            dismissProgressDialog();
//                            RefreshTokenManager.getInstance().status = RefreshTokenManager.RefreshTokenAPIStatus.FAILED;
//                            EventBus.getDefault().post(new APICallRefreshEvent());
//                            //Note: Here removing whole cache directory so explicitly not removing qrCode image folder
//                            clearDataAndRedirect();
//                        } else {
//                            reqContext = context;
//                            reqHeader = isHeader;
//                            reqModule = module;
//                            reqType = apiMethodType;
//                            reqChatId = chatId;
//                            reqMap = map;
//                            if (RefreshTokenManager.getInstance().status != RefreshTokenManager.RefreshTokenAPIStatus.IN_PROGRESS && RefreshTokenManager.getInstance().status != RefreshTokenManager.RefreshTokenAPIStatus.COMPLETED) {
//                                RefreshTokenManager.getInstance().callRefreshTokenAPI(context);
//                            }
//                            EventBus.getDefault().register(BaseAPIService.this);
//                        }
                    } else {
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        JSONObject jsonObject;
                        try {
                            String responseBody = response.errorBody().string();
                            jsonObject = new JSONObject(responseBody);
                            String userMessage = jsonObject.optString("message", "");
                            String code = jsonObject.optString("code", "");
//                            if (module.equalsIgnoreCase(Constants.QR_CODE)) {
//                                if (responseListener != null) {
//                                    responseListener.onFailure(userMessage);
//                                }
//                            } else {
//                                if (responseListener != null) {
//                                    responseListener.onFailure("");
//                                }
//                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    showLogException(e);
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                if(isShowProgress) {
                    dismissProgressDialog();
                }
                try {
                    showLog(TAG_EXCEPTION, t.getMessage());
                    if (responseListener != null) {
                        responseListener.onFailure("");
                    }
                } catch (Exception e) {
                    showLogException(e);
                }
            }
        });
    }

    private void processDeepLinkRequest(final String module, final Map<String, Object> map, final boolean isHeader, final Context context, final String apiMethodType) {
        //String token = SharedPrefUtils.getStringPreference(context, Constants.PREF_XAT);
        String token = SDKInitiate.INSTANCE.getAuthToken();
        showLog("token", token);
        if (map.size() == 0) {
            apiCall = RetrofitBuilder.getDeepLinkWebService().doRequestGet(module);
        } else {
            apiCall = RetrofitBuilder.getDeepLinkWebService().doRequestGetData(token, module, map);
        }
        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                try {
                    if (response.code() == 200) {
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        String res = response.body().string();
                        if (responseListener != null) {
                            responseListener.onSuccess(res);
                        }
                        showLog("Response:", res);
                    } else if (response.code() == 412) {
                        String responseBody = response.errorBody().string();
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        if (responseListener != null) {
                            responseListener.onFailure("412");
                        }
                    } else if (response.code() == 429) {
                        String responseBody = response.errorBody().string();
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        if (responseListener != null) {
                            responseListener.onFailure("429");
                        }
                    } else if (response.code() == 404 || response.code() == 403) {
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        JSONObject jsonObject;
                        try {
                            String responseBody = response.errorBody().string();
                            jsonObject = new JSONObject(responseBody);
                            String userMessage = jsonObject.optString("message", "");
                            String code = jsonObject.optString("code", "");
                            if (responseListener != null) {
                                responseListener.onFailure(userMessage);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.code() == 401) {
//                        String responseBody = response.errorBody().string();
//                        sendAPIFailureLogs(apiCall.request().url().toString(), "", responseBody);
//                        if (module.equals(Constants.REFRESH_TOKEN)) {
//                            //Force Logout
//                            dismissProgressDialog();
//                            RefreshTokenManager.getInstance().status = RefreshTokenManager.RefreshTokenAPIStatus.FAILED;
//                            EventBus.getDefault().post(new APICallRefreshEvent());
//                            //Note: Here removing whole cache directory so explicitly not removing qrCode image folder
//                            clearDataAndRedirect();
//                        } else {
//                            reqContext = context;
//                            reqHeader = isHeader;
//                            reqModule = module;
//                            reqType = apiMethodType;
//                            reqMap = map;
//                            if (RefreshTokenManager.getInstance().status != RefreshTokenManager.RefreshTokenAPIStatus.IN_PROGRESS && RefreshTokenManager.getInstance().status != RefreshTokenManager.RefreshTokenAPIStatus.COMPLETED) {
//                                RefreshTokenManager.getInstance().callRefreshTokenAPI(context);
//                            }
//                            EventBus.getDefault().register(BaseAPIService.this);
//                        }
                    } else {
                        if (isShowProgress) {
                            dismissProgressDialog();
                        }
                        JSONObject jsonObject;
                        try {
                            String responseBody = response.errorBody().string();
                            jsonObject = new JSONObject(responseBody);
                            String userMessage = jsonObject.optString("message", "");
                            String code = jsonObject.optString("code", "");
//                            if (module.equalsIgnoreCase(Constants.QR_CODE)) {
//                                if (responseListener != null) {
//                                    responseListener.onFailure(userMessage);
//                                }
//                            } else {
//                                if (responseListener != null) {
//                                    responseListener.onFailure("");
//                                }
//                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    showLogException(e);
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                dismissProgressDialog();
                try {
                    showLog(TAG_EXCEPTION, t.getMessage());
                    if (responseListener != null) {
                        responseListener.onFailure("");
                    }
                } catch (Exception e) {
                    showLogException(e);
                }
            }
        });
    }

    /*Dismiss progress dialog.*/
    public static void dismissProgressDialog() {
        try {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        } catch (Exception e) {
            showLogException(e);
        }
    }

    private long mLastClickTime = 0;
}