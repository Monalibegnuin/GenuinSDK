package com.begenuin.library.data.viewmodel;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.begenuin.library.common.Constants;
import com.begenuin.library.common.Utility;
import com.begenuin.library.core.interfaces.QuestionViewModelListener;
import com.begenuin.library.core.interfaces.ResponseListener;
import com.begenuin.library.data.model.ConversationModel;
import com.begenuin.library.data.model.QuestionModel;
import com.begenuin.library.data.remote.BaseAPIService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionViewModel {

    private static QuestionViewModel mInstance;
    private BaseAPIService questionService;
    private int apiCallValue;
    private int loopQuestionsApiCallValue;
    private String deepLinkQuestionId = "";
    private String deepLinkLoopQuestionId = "";

    public final ArrayList<QuestionModel> masterQuestionsArr = new ArrayList<>();
    public final ArrayList<QuestionModel> loopQuestionsArr = new ArrayList<>();
    public ConversationModel conversation;

    public static QuestionViewModel getInstance() {
        if (mInstance == null) {
            mInstance = new QuestionViewModel();
        }
        return mInstance;
    }

    public enum QuestionsSyncStatus {
        NONE,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    public QuestionsSyncStatus status = QuestionsSyncStatus.NONE;
    public QuestionsSyncStatus loopQuestionSyncStatus = QuestionsSyncStatus.NONE;
    private QuestionViewModelListener listener;

    public String getDeepLinkQuestionId() {
        return deepLinkQuestionId;
    }

    public void setDeepLinkQuestionId(String deepLinkQuestionId) {
        this.deepLinkQuestionId = deepLinkQuestionId;
    }

    public String getDeepLinkLoopQuestionId() {
        return deepLinkLoopQuestionId;
    }

    public void setDeepLinkLoopQuestionId(String deepLinkLoopQuestionId) {
        this.deepLinkLoopQuestionId = deepLinkLoopQuestionId;
    }

    public void setListener(QuestionViewModelListener questionViewModelListener) {
        listener = questionViewModelListener;
    }

    public void reset() {
        masterQuestionsArr.clear();
        status = QuestionsSyncStatus.NONE;
        deepLinkQuestionId = "";
        if (questionService != null) {
            questionService.cancelCall();
        }
    }

    public void syncLoopQuestions(Context context, String chatId) {
        try {
            if (loopQuestionSyncStatus == QuestionsSyncStatus.IN_PROGRESS) {
                return;
            }
            if(questionService != null){
                questionService.cancelCall();
            }
            loopQuestionSyncStatus = QuestionsSyncStatus.IN_PROGRESS;
            Map<String, Object> map = new HashMap<>();
            map.put("id", chatId);
            map.put("association_type", 1);
            if(!TextUtils.isEmpty(deepLinkLoopQuestionId)){
                map.put("include_question_ids[]", deepLinkLoopQuestionId);
                deepLinkLoopQuestionId = "";
            }
            questionService = new BaseAPIService(
                    context,
                    Constants.SYNC_LOOP_QUESTIONS,
                    true,
                    "",
                    map,
                    new ResponseListener() {
                        @Override
                        public void onSuccess(String response) {
                            try {
                                Utility.showLog("TAGGG", response);
                                JSONObject object = new JSONObject(response);
                                JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);
                                conversation = new Gson().fromJson(dataJson.getJSONObject("roundtable").toString(), ConversationModel.class);
                                loopQuestionsApiCallValue = (int) (Math.ceil(conversation.getQuestions().size() * 0.3));
                                loopQuestionsArr.addAll(conversation.getQuestions());
                                listener.onQuestionsSyncSuccess(loopQuestionsArr, true);
                                loopQuestionSyncStatus = QuestionsSyncStatus.COMPLETED;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            listener.onQuestionsSyncFailure();
                        }
                    },
                    "GET_DATA",
                    false
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncQuestions(Context context, boolean isClearArray) {
        if (status == QuestionsSyncStatus.IN_PROGRESS) {
            return;
        }
        status = QuestionsSyncStatus.IN_PROGRESS;
        if (isClearArray) {
            masterQuestionsArr.clear();
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if (questionService != null) {
                questionService.cancelCall();
            }
            Map<String, Object> map = new HashMap<>();
            if (!TextUtils.isEmpty(deepLinkQuestionId)) {
                map.put("include_question_ids[]", deepLinkQuestionId);
            }
            questionService = new BaseAPIService(context, Constants.SYNC_QUESTIONS, true, "", map, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    Utility.showLog("Questions", response);
                    deepLinkQuestionId = "";
                    JSONObject object;
                    try {
                        object = new JSONObject(response);
                        JSONArray dataJson = object.getJSONArray(Constants.JSON_DATA);
                        Gson gson = new Gson();
                        Type questionListDataType = new TypeToken<ArrayList<QuestionModel>>() {
                        }.getType();
                        List<QuestionModel> questionsTempList = gson.fromJson(dataJson.toString(), questionListDataType);
//                        List<QuestionModel> temp = questionsTempList.subList(0, 10);
                        apiCallValue = (int) (questionsTempList.size() * 0.3);
                        masterQuestionsArr.addAll(questionsTempList);
                        status = QuestionsSyncStatus.COMPLETED;
                        if (listener != null) {
                            listener.onQuestionsSyncSuccess(masterQuestionsArr, isClearArray);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    deepLinkQuestionId = "";
                    status = QuestionsSyncStatus.FAILED;
                    if (listener != null) {
                        listener.onQuestionsSyncFailure();
                    }
                }
            }, "GET_DATA", false);
        });
    }

    public boolean shouldNextLoopApiCall(){
        return (loopQuestionSyncStatus != QuestionsSyncStatus.IN_PROGRESS && (loopQuestionsArr.size() - 1 <= loopQuestionsApiCallValue));
    }
    public QuestionModel goNextLoopQuestion(){
        if (loopQuestionsArr.size() > 0) {
            loopQuestionsArr.remove(0);
        }
        if (loopQuestionsArr.size() > 0) {
            return loopQuestionsArr.get(0);
        }
        return null;
    }

    public boolean shouldNextApiCall() {
        return (status != QuestionsSyncStatus.IN_PROGRESS && (masterQuestionsArr.size() <= apiCallValue));
    }

    public QuestionModel getNextQuestion() {
        if (masterQuestionsArr.size() > 0) {
            masterQuestionsArr.remove(0);
        }
        if (masterQuestionsArr.size() > 0) {
            return masterQuestionsArr.get(0);
        }
        return null;
    }
}