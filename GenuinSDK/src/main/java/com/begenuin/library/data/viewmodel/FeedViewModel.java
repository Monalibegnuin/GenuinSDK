package com.begenuin.library.data.viewmodel;

import static com.begenuin.library.common.Utility.isNetworkAvailable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import com.begenuine.feedscreensdk.common.Constants;
import com.begenuin.library.common.Utility;
import com.begenuin.library.SDKInitiate;
import com.begenuin.library.core.enums.ExploreVideoType;
import com.begenuin.library.core.enums.FeedViewType;
import com.begenuin.library.core.interfaces.FeedCommunityListInterface;
import com.begenuin.library.core.interfaces.FeedViewModelListener;
import com.begenuin.library.core.interfaces.ResponseListener;
import com.begenuin.library.data.model.CommunityModel;
import com.begenuin.library.data.model.ConversationModel;
import com.begenuin.library.data.model.DiscoverModel;
import com.begenuin.library.data.remote.BaseAPIService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FeedViewModel {

    private static FeedViewModel mInstance;
    private String apiResponse, apiMyLoopResponse, apiMySubscriptionResponse, apiCommunityVideosResponse;
    private String searchText = "";
    private String pageSession = "";
    private String searchPageSession = "";
    private int page = 1;
    private int searchPage = 1;
    private boolean isEndOfSearch = false;
    private long startMillis, endMillis;
    private long searchStartMillis;
    public long expireMillis;
    public long expireSearchMillis;
    private int searchLastPos = 0;
    private int lastPos = 0;
    private FeedViewModelListener feedForYouListener, feedMyLoopListener, feedSubscriberListener, feedCommunityVideosListener;
    private FeedCommunityListInterface feedCommunityListInterface;
    private BaseAPIService feedService, feedMyLoopService, feedMySubscriptionService, feedCommunityVideosService, feedCommunityListService;
    private long startLaunchMillis;
    private boolean isEventLogged;
    private boolean isDataDogLogged;
    private String deepLinkVideoId = "";
    public String currentlyLoadedCommunity = "";

    private final ArrayList<ExploreViewModel> masterDiscoverArr = new ArrayList<>();
    private final ArrayList<ExploreViewModel> masterSearchDiscoverArr = new ArrayList<>();
    private final ArrayList<ExploreViewModel> masterMyLoopArr = new ArrayList<>();
    public final ArrayList<ExploreViewModel> masterMySubscriptionsArr = new ArrayList<>();
    public final ArrayList<ExploreViewModel> masterCommunityVideosArr = new ArrayList<>();
    public final ArrayList<CommunityModel> masterCommunitiesList = new ArrayList<>();

    private final ArrayList<String> masterDiscoverIds = new ArrayList<>();
    private final ArrayList<String> tempDiscoverIds = new ArrayList<>();

    public enum APIStatus {
        NONE,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    public APIStatus statusForYou = APIStatus.NONE;
    public APIStatus statusForMyLoops = APIStatus.NONE;
    public APIStatus statusForSubscriptions = APIStatus.NONE;
    public APIStatus statusForCommunityVideos = APIStatus.NONE;

    public ArrayList<ExploreViewModel> getMasterMySubscriptionsArr() {
        return masterMySubscriptionsArr;
    }


    public ArrayList<ExploreViewModel> getMasterDiscoverArr(int pos) {
        if (pos == 0) {
            return masterMyLoopArr;
        } else if (pos == 1) {
            return masterMySubscriptionsArr;
        } else if (pos == 2) {
            return masterDiscoverArr;
        } else {
            return masterCommunityVideosArr;
        }
    }

    public String getDeepLinkVideoId() {
        return deepLinkVideoId;
    }

    public void setDeepLinkVideoId(String deepLinkVideoId) {
        this.deepLinkVideoId = deepLinkVideoId;
    }

    public int getPage() {
        return page;
    }

    public boolean isEndOfSearch() {
        return isEndOfSearch;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public int getSearchLastPos() {
        return searchLastPos;
    }

    public void setSearchLastPos(int searchLastPos) {
        this.searchLastPos = searchLastPos;
    }

    public int getLastPos() {
        return lastPos;
    }

    public void setLastPos(int lastPos) {
        this.lastPos = lastPos;
    }

    public long getStartLaunchMillis() {
        return startLaunchMillis;
    }

    public void setStartLaunchMillis(long startLaunchMillis) {
        this.startLaunchMillis = startLaunchMillis;
    }

    public boolean isEventLogged() {
        return isEventLogged;
    }

    public void setEventLogged(boolean eventLogged) {
        isEventLogged = eventLogged;
    }

    public boolean isDataDogLogged() {
        return isDataDogLogged;
    }

    public void setDataDogLogged(boolean dataDogLogged) {
        isDataDogLogged = dataDogLogged;
    }

    public static FeedViewModel getInstance() {
        if (mInstance == null) {
            mInstance = new FeedViewModel();
        }
        return mInstance;
    }

    public void setFeedForYouListener(FeedViewModelListener feedViewModelListener) {
        feedForYouListener = feedViewModelListener;
    }

    public void setFeedMyLoopListener(FeedViewModelListener feedViewModelListener) {
        feedMyLoopListener = feedViewModelListener;
    }

    public void setFeedSubscriberListener(FeedViewModelListener feedViewModelListener) {
        feedSubscriberListener = feedViewModelListener;
    }

    public void setFeedCommunityVideoListener(FeedViewModelListener feedViewModelListener) {
        feedCommunityVideosListener = feedViewModelListener;
    }

    public void setFeedCommunityListInterface(FeedCommunityListInterface feedCommunityListInterface) {
        this.feedCommunityListInterface = feedCommunityListInterface;
    }

    public void initializeFeedData(Context context) {
        feedForYouVideos(context, true, false);
        feedCommunityList(context);
    }

    public void clearFeedDataListeners() {
        feedForYouListener = null;
        feedSubscriberListener = null;
        feedMyLoopListener = null;
        feedCommunityVideosListener = null;
    }

    public String getResponse(int pos) {
        if (pos == 0) {
            return apiMyLoopResponse;
        } else if (pos == 1) {
            return apiMySubscriptionResponse;
        } else if (pos == 2) {
            return apiResponse;
        } else {
            return apiCommunityVideosResponse;
        }
    }

    public void setEmptyResponse(int pos) {
        if (pos == 0) {
            apiMyLoopResponse = "";
        } else if (pos == 1) {
            apiMySubscriptionResponse = "";
        } else {
            apiResponse = "";
        }
    }

    public boolean isSearch() {
        return !TextUtils.isEmpty(searchText);
    }

    public void resetCurrentFeed(int pos) {
        if (pos == FeedViewType.MY_LOOPS.getValue()) {
            apiMyLoopResponse = "";
            masterMyLoopArr.clear();
            statusForMyLoops = APIStatus.NONE;
            if (feedMyLoopService != null) {
                feedMyLoopService.cancelCall();
            }
        } else if (pos == FeedViewType.SUBSCRIPTIONS.getValue()) {
            apiMySubscriptionResponse = "";
            masterMySubscriptionsArr.clear();
            statusForSubscriptions = APIStatus.NONE;
            if (feedMySubscriptionService != null) {
                feedMySubscriptionService.cancelCall();
            }
        } else if (pos == FeedViewType.FOR_YOU.getValue()) {
            page = 1;
            pageSession = "";
            apiResponse = "";
            deepLinkVideoId = "";
            masterDiscoverArr.clear();
            statusForYou = APIStatus.NONE;
            masterDiscoverIds.clear();
            if (feedService != null) {
                feedService.cancelCall();
            }
        } else if (pos == FeedViewType.COMMUNITY.getValue()) {
            apiCommunityVideosResponse = "";
            masterCommunityVideosArr.clear();
            statusForCommunityVideos = APIStatus.NONE;
            if (feedCommunityVideosService != null) {
                feedCommunityVideosService.cancelCall();
            }
        }
    }

    public void reset() {
        isEndOfSearch = false;
        page = 1;
        searchPage = 1;
        pageSession = "";
        apiResponse = "";
        apiMyLoopResponse = "";
        apiMySubscriptionResponse = "";
        apiCommunityVideosResponse = "";
        searchPageSession = "";
        searchText = "";
        masterDiscoverArr.clear();
        masterMyLoopArr.clear();
        masterMySubscriptionsArr.clear();
        masterCommunitiesList.clear();
        masterCommunityVideosArr.clear();
        statusForYou = APIStatus.NONE;
        statusForMyLoops = APIStatus.NONE;
        statusForSubscriptions = APIStatus.NONE;
        statusForCommunityVideos = APIStatus.NONE;
        lastPos = 0;
        searchLastPos = 0;
        deepLinkVideoId = "";
        masterDiscoverIds.clear();
        if (feedService != null) {
            feedService.cancelCall();
        }
        if (feedMyLoopService != null) {
            feedMyLoopService.cancelCall();
        }
        if (feedMySubscriptionService != null) {
            feedMySubscriptionService.cancelCall();
        }
        if (feedCommunityVideosService != null) {
            feedCommunityVideosService.cancelCall();
        }
        if (feedCommunityListService != null) {
            feedCommunityListService.cancelCall();
        }
    }

    public void managementForRefreshFeed() {
        isEndOfSearch = false;
        deepLinkVideoId = "";
        statusForYou = APIStatus.COMPLETED;
        apiResponse = "";
        if (TextUtils.isEmpty(searchText)) {
            lastPos = 0;
            page = 1;
            pageSession = "";
            if (masterDiscoverArr.size() > 10) {
                masterDiscoverArr.removeAll(masterDiscoverArr.subList(0, masterDiscoverArr.size() - 10));
            }
        } else {
            searchLastPos = 0;
            searchPage = 1;
            searchPageSession = "";
            if (masterSearchDiscoverArr.size() > 10) {
                masterSearchDiscoverArr.removeAll(masterSearchDiscoverArr.subList(0, masterSearchDiscoverArr.size() - 10));
            }
        }
        if (feedService != null) {
            feedService.cancelCall();
        }
    }

    public void feedCommunityList(Context context) {
        //String userId = Utility.getLoggedInUserId(context);
        String userId = SDKInitiate.INSTANCE.getUserId();
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        feedCommunityListService = new BaseAPIService(
                context,
                Constants.GET_COMMUNITIES,
                true,
                "",
                map,
                new ResponseListener() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject dataJson = jsonObject.getJSONObject("data");
                            if (dataJson.has("communities")) {
                                JSONArray  jsonArray = dataJson.getJSONArray("communities");
                                if (jsonArray.length() > 0) {
                                    Gson gson = new Gson();
                                    Type communityListData = new TypeToken<ArrayList<CommunityModel>>() {}.getType();
                                    ArrayList<CommunityModel> tempCommunityList = gson.fromJson(jsonArray.toString(), communityListData);
                                    masterCommunitiesList.addAll(tempCommunityList);
                                    if (feedCommunityListInterface != null) {
                                        feedCommunityListInterface.onCommunityListLoaded(masterCommunitiesList);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String error) {

                    }
                },
                "GET_DATA",
                false
        );
    }

    public void addCommunityToFeed(CommunityModel communityModel) {
        masterCommunitiesList.add(0, communityModel);
        if (feedCommunityListInterface != null) {
            feedCommunityListInterface.onCommunityListLoaded(masterCommunitiesList);
        }
    }

    public void removeCommunityFromFeed(String communityId) {
        masterCommunitiesList.removeIf(communityModel -> Objects.equals(communityModel.getCommunityId(), communityId));
        if (feedCommunityListInterface != null) {
            feedCommunityListInterface.onCommunityListLoaded(masterCommunitiesList);
        }
    }

    public void feedCommunityVideos(Context context, boolean isNewDiscover, String communityId) {
        if (!isNetworkAvailable(context)) {
            statusForCommunityVideos = APIStatus.FAILED;
            return;
        }

        statusForCommunityVideos = APIStatus.IN_PROGRESS;

        if (isNewDiscover) {
            if (masterCommunityVideosArr.size() > 0) {
                if (feedCommunityVideosListener != null) {
                    statusForCommunityVideos = APIStatus.COMPLETED;
                    feedCommunityVideosListener.onFeedCacheDataLoaded(masterCommunityVideosArr);
                    return;
                } else {
                    masterCommunityVideosArr.clear();
                }
            }
        }

        if (feedCommunityVideosService != null) {
            feedCommunityVideosService.cancelCall();
        }

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("community_id", communityId);
            if (masterCommunityVideosArr.size() > 0) {
                ExploreViewModel exploreViewModel = masterCommunityVideosArr.get(masterCommunityVideosArr.size() - 1);
                map.put("last_video_id", exploreViewModel.getConvId());
            }

            feedCommunityVideosService = new BaseAPIService(context, Constants.MY_COMMUNITY_VIDEOS, true, "", map, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    statusForCommunityVideos = APIStatus.COMPLETED;
                    if (feedCommunityVideosService == null) {
                        apiCommunityVideosResponse = response;
                    }
                    try {
                        currentlyLoadedCommunity = communityId;
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);

                        if (dataJson.has("feeds")) {
                            JSONArray jsonArray = dataJson.getJSONArray("feeds");
                            if (jsonArray.length() > 0) {
                                Gson gson = new Gson();
                                ArrayList<ExploreViewModel> homeTempList = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String feedType = jsonObject.optString("feed_type", "");
                                    JSONObject jFeedObj = jsonObject.optJSONObject("feed");
                                    if (feedType.equalsIgnoreCase(ExploreVideoType.RT.getValue())) {
                                        ExploreViewModel exploreViewModel = new ExploreViewModel();
                                        exploreViewModel.type = ExploreVideoType.RT;
                                        assert jFeedObj != null;
                                        ConversationModel conversationModel = gson.fromJson(jFeedObj.toString(), ConversationModel.class);
                                        exploreViewModel.modelInterface = conversationModel;
                                        if (conversationModel.getChats() != null && conversationModel.getChats().size() > 0) {
                                            homeTempList.add(exploreViewModel);
                                        }
                                    }
                                }
                                if (homeTempList.size() > 0) {
                                    masterCommunityVideosArr.addAll(homeTempList);
                                    if (feedCommunityVideosListener != null) {
                                        feedCommunityVideosListener.onFeedDataLoaded(homeTempList, isNewDiscover);
                                    }
                                } else {
                                    if (feedCommunityVideosListener != null) {
                                        feedCommunityVideosListener.onEmptyFeedData(isNewDiscover);
                                    }
                                }
                            } else {
                                if (feedCommunityVideosListener != null) {
                                    feedCommunityVideosListener.onEmptyFeedData(isNewDiscover);
                                }
                            }
                        } else {
                            if (feedCommunityVideosListener != null) {
                                feedCommunityVideosListener.onEmptyFeedData(isNewDiscover);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    statusForCommunityVideos = APIStatus.FAILED;
                    if (feedCommunityVideosListener != null) {
                        feedCommunityVideosListener.onFeedDataFailure(error, false);
                    }
                }
            }, "GET_DATA", false);
        } catch (Exception e) {
            Utility.showLogException(e);
        }
    }

    public void feedForMyLoopsVideos(Context context, boolean isNewDiscover) {

        if (!isNetworkAvailable(context)) {
            statusForMyLoops = APIStatus.FAILED;
            return;
        }

        statusForMyLoops = APIStatus.IN_PROGRESS;

        if (isNewDiscover) {
            if (masterMyLoopArr.size() > 0) {
                if (feedMyLoopListener != null) {
                    statusForMyLoops = APIStatus.COMPLETED;
                    feedMyLoopListener.onFeedCacheDataLoaded(masterMyLoopArr);
                    return;
                } else {
                    masterMyLoopArr.clear();
                }
            }
        }

        if (feedMyLoopService != null) {
            feedMyLoopService.cancelCall();
        }

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("video_types[]", "member");
            if (masterMyLoopArr.size() > 0) {
                ExploreViewModel exploreViewModel = masterMyLoopArr.get(masterMyLoopArr.size() - 1);
                map.put("last_video_id", exploreViewModel.getConvId());
            }

            feedMyLoopService = new BaseAPIService(context, Constants.MY_LOOP_VIDEOS, true, "", map, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    statusForMyLoops = APIStatus.COMPLETED;
                    if (feedMyLoopListener == null) {
                        apiMyLoopResponse = response;
                    }
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);

                        if (dataJson.has("feeds")) {
                            JSONArray jsonArray = dataJson.getJSONArray("feeds");
                            if (jsonArray.length() > 0) {
                                Gson gson = new Gson();
                                ArrayList<ExploreViewModel> homeTempList = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String feedType = jsonObject.optString("feed_type", "");
                                    JSONObject jFeedObj = jsonObject.optJSONObject("feed");
                                    if (feedType.equalsIgnoreCase(ExploreVideoType.RT.getValue())) {
                                        ExploreViewModel exploreViewModel = new ExploreViewModel();
                                        exploreViewModel.type = ExploreVideoType.RT;
                                        assert jFeedObj != null;
                                        ConversationModel conversationModel = gson.fromJson(jFeedObj.toString(), ConversationModel.class);
                                        exploreViewModel.modelInterface = conversationModel;
                                        if (conversationModel.getChats() != null && conversationModel.getChats().size() > 0) {
                                            homeTempList.add(exploreViewModel);
                                        }
                                    }
                                }
                                if (homeTempList.size() > 0) {
                                    masterMyLoopArr.addAll(homeTempList);
                                    if (feedMyLoopListener != null) {
                                        feedMyLoopListener.onFeedDataLoaded(homeTempList, isNewDiscover);
                                    }
                                } else {
                                    if (feedMyLoopListener != null) {
                                        feedMyLoopListener.onEmptyFeedData(isNewDiscover);
                                    }
                                }
                            } else {
                                if (feedMyLoopListener != null) {
                                    feedMyLoopListener.onEmptyFeedData(isNewDiscover);
                                }
                            }
                        } else {
                            if (feedMyLoopListener != null) {
                                feedMyLoopListener.onEmptyFeedData(isNewDiscover);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    statusForMyLoops = APIStatus.FAILED;
                    if (feedMyLoopListener != null) {
                        feedMyLoopListener.onFeedDataFailure(error, false);
                    }
                }
            }, "GET_DATA", false);
        } catch (Exception e) {
            Utility.showLogException(e);
        }
    }

    public void feedForMySubscriptionsVideos(Context context, boolean isNewDiscover) {

        if (!isNetworkAvailable(context)) {
            statusForSubscriptions = APIStatus.FAILED;
            return;
        }

        statusForSubscriptions = APIStatus.IN_PROGRESS;

        if (isNewDiscover) {
            if (masterMySubscriptionsArr.size() > 0) {
                if (feedSubscriberListener != null) {
                    statusForSubscriptions = APIStatus.COMPLETED;
                    feedSubscriberListener.onFeedCacheDataLoaded(masterMySubscriptionsArr);
                    return;
                } else {
                    masterMySubscriptionsArr.clear();
                }
            }
        }

        if (feedMySubscriptionService != null) {
            feedMySubscriptionService.cancelCall();
        }

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("video_types[]", "subscriber");
            if (masterMySubscriptionsArr.size() > 0) {
                ExploreViewModel exploreViewModel = masterMySubscriptionsArr.get(masterMySubscriptionsArr.size() - 1);
                map.put("last_video_id", exploreViewModel.getConvId());
            }

            feedMySubscriptionService = new BaseAPIService(context, Constants.MY_LOOP_VIDEOS, true, "", map, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    statusForSubscriptions = APIStatus.COMPLETED;
                    if (feedSubscriberListener == null) {
                        apiMySubscriptionResponse = response;
                    }
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);

                        if (dataJson.has("feeds")) {
                            JSONArray jsonArray = dataJson.getJSONArray("feeds");
                            if (jsonArray.length() > 0) {
                                Gson gson = new Gson();
                                ArrayList<ExploreViewModel> homeTempList = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String feedType = jsonObject.optString("feed_type", "");
                                    JSONObject jFeedObj = jsonObject.optJSONObject("feed");
                                    if (feedType.equalsIgnoreCase(ExploreVideoType.RT.getValue())) {
                                        ExploreViewModel exploreViewModel = new ExploreViewModel();
                                        exploreViewModel.type = ExploreVideoType.RT;
                                        assert jFeedObj != null;
                                        ConversationModel conversationModel = gson.fromJson(jFeedObj.toString(), ConversationModel.class);
                                        exploreViewModel.modelInterface = conversationModel;
                                        if (conversationModel.getChats() != null && conversationModel.getChats().size() > 0) {
                                            homeTempList.add(exploreViewModel);
                                        }
                                    }
                                }
                                if (homeTempList.size() > 0) {
                                    masterMySubscriptionsArr.addAll(homeTempList);
                                    if (feedSubscriberListener != null) {
                                        feedSubscriberListener.onFeedDataLoaded(homeTempList, isNewDiscover);
                                    }
                                } else {
                                    if (feedSubscriberListener != null) {
                                        feedSubscriberListener.onEmptyFeedData(isNewDiscover);
                                    }
                                }
                            } else {
                                if (feedSubscriberListener != null) {
                                    feedSubscriberListener.onEmptyFeedData(isNewDiscover);
                                }
                            }
                        } else {
                            if (feedSubscriberListener != null) {
                                feedSubscriberListener.onEmptyFeedData(isNewDiscover);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    statusForSubscriptions = APIStatus.FAILED;
                    if (feedSubscriberListener != null) {
                        feedSubscriberListener.onFeedDataFailure(error, false);
                    }
                }
            }, "GET_DATA", false);
        } catch (Exception e) {
            Utility.showLogException(e);
        }
    }

    public void feedForYouVideos(Context context, boolean isNewDiscover, boolean isShowProgress) {

        if (!isNetworkAvailable(context)) {
            statusForYou = APIStatus.FAILED;
            return;
        }

        statusForYou = APIStatus.IN_PROGRESS;

        if (isNewDiscover) {
            isEndOfSearch = false;
            searchPage = 1;
            searchPageSession = "";
            searchLastPos = 0;
            if (masterDiscoverArr.size() > 0) {
                if (feedForYouListener != null) {
                    statusForYou = APIStatus.COMPLETED;
                    feedForYouListener.onFeedCacheDataLoaded(masterDiscoverArr);
                    return;
                } else {
                    page = 1;
                    pageSession = "";
                    masterDiscoverArr.clear();
                }
            }
        }

        if (page == 1) {
            startMillis = System.currentTimeMillis();
        }

        if (feedService != null) {
            feedService.cancelCall();
        }

        try {
            //TODO: Need to add userId
            //String userId = SharedPrefUtils.getStringPreference(context, Constants.PREF_USER);
            String userId = SDKInitiate.INSTANCE.getUserId();
           // String oldPageSession = SharedPrefUtils.getStringPreference(context, Constants.PREF_OLD_PAGE_SESSION);
            String oldPageSession = "";
            @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            Map<String, Object> map = new HashMap<>();
            map.put("device_id", deviceId);
            if (page > 1) {
                ExploreViewModel exploreViewModel = masterDiscoverArr.get(masterDiscoverArr.size() - 1);
                map.put("last_video_type", exploreViewModel.type.getValue());
                map.put("last_video_id", exploreViewModel.getConvId());
                if (exploreViewModel.type == ExploreVideoType.RT) {
                    map.put("last_video_parent_id", exploreViewModel.getFeedId());
                }
            }

            if (!TextUtils.isEmpty(pageSession)) {
                map.put("page_session", pageSession);
            } else if (!TextUtils.isEmpty(oldPageSession)) {
                map.put("old_page_session", oldPageSession);
            }

            if (!deepLinkVideoId.equalsIgnoreCase("")) {
                map.put("discover_video_id", deepLinkVideoId);
            }
            feedService = new BaseAPIService(context, Constants.HOME, true, "", map, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    statusForYou = APIStatus.COMPLETED;
                    deepLinkVideoId = "";
                    if (feedForYouListener == null) {
                        apiResponse = response;
                    }
                    try {
                        if (page == 1) {
                            endMillis = System.currentTimeMillis();
//                            HashMap<String, Object> map = new HashMap<String, Object>() {{
//                                put("exe_time", endMillis - startMillis);
//                                put("user_id", userId);
//                                put("device_id", deviceId);
//                            }};
//                            GenuInApplication.getInstance().sendEventLogs(Constants.DISCOVER_API_TIME, map);
                        }
                        page++;
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);

                        if (dataJson.has("page_session")) {
                            pageSession = dataJson.optString("page_session", "");
                            //SharedPrefUtils.setStringPreference(context, Constants.PREF_OLD_PAGE_SESSION, pageSession);
                        }

                        if (dataJson.has("expiry")) {
                            expireMillis = dataJson.optLong("expiry", 0);
                        }

                         if (dataJson.has("feeds")) {
                            JSONArray jsonArray = dataJson.getJSONArray("feeds");
                            if (jsonArray.length() > 0) {
                                Gson gson = new Gson();
                                ArrayList<ExploreViewModel> homeTempList = new ArrayList<>();
                                tempDiscoverIds.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String feedType = jsonObject.optString("feed_type", "");
                                    JSONObject jFeedObj = jsonObject.optJSONObject("feed");
                                    if (feedType.equalsIgnoreCase(ExploreVideoType.PUBLIC_VIDEO.getValue())) {
                                        ExploreViewModel exploreViewModel = new ExploreViewModel();
                                        exploreViewModel.type = ExploreVideoType.PUBLIC_VIDEO;
                                        assert jFeedObj != null;
                                        DiscoverModel discoverModel = gson.fromJson(jFeedObj.toString(), DiscoverModel.class);
                                        exploreViewModel.modelInterface = discoverModel;
                                        homeTempList.add(exploreViewModel);
                                        tempDiscoverIds.add(exploreViewModel.getConvId());
                                    } else if (feedType.equalsIgnoreCase(ExploreVideoType.RT.getValue())) {
                                        ExploreViewModel exploreViewModel = new ExploreViewModel();
                                        exploreViewModel.type = ExploreVideoType.RT;
                                        assert jFeedObj != null;
                                        ConversationModel conversationModel = gson.fromJson(jFeedObj.toString(), ConversationModel.class);
                                        exploreViewModel.modelInterface = conversationModel;
                                        if (conversationModel.getChats() != null && conversationModel.getChats().size() > 0) {
                                            homeTempList.add(exploreViewModel);
                                            tempDiscoverIds.add(exploreViewModel.getConvId());
                                        }
                                    }
                                }
                                if (homeTempList.size() > 0) {
                                    //TODO : Log event not added
//                                    logDuplicateIds(oldPageSession);
//                                    logDuplicateIdsFromExisting(oldPageSession);
                                    masterDiscoverArr.addAll(homeTempList);
                                    if (!pageSession.equalsIgnoreCase(oldPageSession)) {
                                        masterDiscoverIds.clear();
                                    }
                                    masterDiscoverIds.addAll(tempDiscoverIds);
                                    if (feedForYouListener != null) {
                                        feedForYouListener.onFeedDataLoaded(homeTempList, isNewDiscover);
                                    }
                                } else {
                                    if (feedForYouListener != null) {
                                        feedForYouListener.onEmptyFeedData(isNewDiscover);
                                    }
                                }
                            } else {
                                if (feedForYouListener != null) {
                                    feedForYouListener.onEmptyFeedData(isNewDiscover);
                                }
                            }
                        } else {
                            if (feedForYouListener != null) {
                                feedForYouListener.onEmptyFeedData(isNewDiscover);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    statusForYou = APIStatus.FAILED;
                    if (feedForYouListener != null) {
                        feedForYouListener.onFeedDataFailure(error, false);
                    }
                }
            }, "GET_DATA", false);
        } catch (Exception e) {
            Utility.showLogException(e);
        }
    }

    public void searchVideos(Context context, boolean isNewDiscover, boolean isShowProgress) {

        if (!isNetworkAvailable(context)) {
            statusForYou = APIStatus.FAILED;
            return;
        }
        searchStartMillis = System.currentTimeMillis();
        statusForYou = APIStatus.IN_PROGRESS;

        if (isNewDiscover) {
            isEndOfSearch = false;
            searchPage = 1;
            searchPageSession = "";
            searchLastPos = 0;
        }

        if (feedService != null) {
            feedService.cancelCall();
        }

        Map<String, Object> map = new HashMap<>();
        try {
            //TODO: Need to add user id
//            String userId = SharedPrefUtils.getStringPreference(context, Constants.PREF_USER);
//            String oldPageSession = SharedPrefUtils.getStringPreference(context, Constants.PREF_OLD_SEARCH_PAGE_SESSION);
            String userId = SDKInitiate.INSTANCE.getUserId(), oldPageSession = "";
            @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            map.put("device_id", deviceId);
            map.put("query_string", searchText.trim());
            if (searchPage > 1) {
                ExploreViewModel exploreViewModel = masterSearchDiscoverArr.get(masterSearchDiscoverArr.size() - 1);
                map.put("last_video_type", exploreViewModel.type.getValue());
                map.put("last_video_id", exploreViewModel.getConvId());
                if (exploreViewModel.type == ExploreVideoType.RT) {
                    map.put("last_video_parent_id", exploreViewModel.getFeedId());
                }
            } else {
                masterSearchDiscoverArr.clear();
            }

            if (!TextUtils.isEmpty(searchPageSession)) {
                map.put("page_session", searchPageSession);
            } else if (!TextUtils.isEmpty(oldPageSession)) {
                map.put("old_page_session", oldPageSession);
            }
            Utility.showLog("Discover api", map.toString());

            feedService = new BaseAPIService(context, Constants.SEARCH_API, true, "", map, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    statusForYou = APIStatus.COMPLETED;
                    if (feedForYouListener == null) {
                        deepLinkVideoId = "";
                        apiResponse = response;
                    }
                    try {
//                        if (searchPage == 1) {
//                            if (Constants.START_MILLIS_SEARCH > 0) {
//                                Constants.END_MILLIS_SEARCH = System.currentTimeMillis();
//                                HashMap<String, Object> map = new HashMap<String, Object>() {{
//                                    put("exe_time", Constants.END_MILLIS_SEARCH - Constants.START_MILLIS_SEARCH);
//                                    put("user_id", userId);
//                                    put("device_id", deviceId);
//                                }};
//                                GenuInApplication.getInstance().sendEventLogs(Constants.SEARCH_WITH_DISCOVER_API, map);
//                            }
//                        }
//
//                        HashMap<String, Object> map = new HashMap<String, Object>() {{
//                            put("latency", System.currentTimeMillis() - searchStartMillis);
//                            put(Constants.KEY_KEYWORD_SEARCHED, searchText);
//                        }};
//                        GenuInApplication.getInstance().sendEventLogs(Constants.KEYWORD_SEARCHED, map);

                        searchPage++;
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);
                        if (dataJson.has("page_session")) {
                            searchPageSession = dataJson.optString("page_session", "");
                            //SharedPrefUtils.setStringPreference(context, Constants.PREF_OLD_SEARCH_PAGE_SESSION, searchPageSession);
                        }

                        if (dataJson.has("expiry")) {
                            expireSearchMillis = dataJson.optLong("expiry", 0);
                        }

                        if (dataJson.has("end_of_search")) {
                            isEndOfSearch = dataJson.getBoolean("end_of_search");
                        } else {
                            isEndOfSearch = false;
                        }

                        if (dataJson.has("feeds")) {
                            JSONArray jsonArray = dataJson.getJSONArray("feeds");
                            if (jsonArray.length() > 0) {
                                Gson gson = new Gson();
                                ArrayList<ExploreViewModel> homeTempList = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String feedType = jsonObject.optString("feed_type", "");
                                    JSONObject jFeedObj = jsonObject.optJSONObject("feed");
                                    if (feedType.equalsIgnoreCase(ExploreVideoType.PUBLIC_VIDEO.getValue())) {
                                        ExploreViewModel exploreViewModel = new ExploreViewModel();
                                        exploreViewModel.type = ExploreVideoType.PUBLIC_VIDEO;
                                        assert jFeedObj != null;
                                        DiscoverModel discoverModel = gson.fromJson(jFeedObj.toString(), DiscoverModel.class);
                                        exploreViewModel.modelInterface = discoverModel;
                                        homeTempList.add(exploreViewModel);
                                    } else if (feedType.equalsIgnoreCase(ExploreVideoType.RT.getValue())) {
                                        ExploreViewModel exploreViewModel = new ExploreViewModel();
                                        exploreViewModel.type = ExploreVideoType.RT;
                                        assert jFeedObj != null;
                                        ConversationModel conversationModel = gson.fromJson(jFeedObj.toString(), ConversationModel.class);
                                        exploreViewModel.modelInterface = conversationModel;
                                        if (conversationModel.getChats() != null && conversationModel.getChats().size() > 0) {
                                            homeTempList.add(exploreViewModel);
                                        }
                                    }
                                }
                                if (homeTempList.size() > 0) {
                                    masterSearchDiscoverArr.addAll(homeTempList);
                                    if (feedForYouListener != null) {
                                        feedForYouListener.onFeedDataLoaded(homeTempList, isNewDiscover);
                                    }
                                } else {
                                    if (feedForYouListener != null) {
                                        feedForYouListener.onEmptyFeedData(isNewDiscover);
                                    }
                                }
                            } else {
                                if (feedForYouListener != null) {
                                    feedForYouListener.onEmptyFeedData(isNewDiscover);
                                }
                            }
                        } else {
                            if (feedForYouListener != null) {
                                feedForYouListener.onEmptyFeedData(isNewDiscover);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    statusForYou = APIStatus.FAILED;
                    if (feedForYouListener != null) {
                        feedForYouListener.onFeedDataFailure(error, true);
                    }
                }
            }, "GET_DATA", isShowProgress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
