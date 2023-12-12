package com.begenuin.library.views;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.begenuin.library.R;
import com.begenuin.library.common.Utility;
import com.begenuin.library.common.customViews.CustomTextView;
import com.begenuin.library.core.enums.ExploreVideoType;
import com.begenuin.library.core.enums.PeekSource;
import com.begenuin.library.data.model.ConversationModel;
import com.begenuin.library.data.model.DiscoverModel;
import com.begenuin.library.data.viewmodel.ExploreViewModel;
import com.begenuin.library.peekandpop.PeekAndPop;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

//LongPressRecyclerActionInterface
public class PublicVideoAdapterNew extends RecyclerView.Adapter<PublicVideoAdapterNew.ViewHolder> implements PeekAndPop.OnGeneralActionListener {

    private final ArrayList<ExploreViewModel> list;
    private final Activity context;
    private ItemClickListener mClickListener;
    private final boolean isMyProfile;
    PeekAndPop peekAndPop;
    PeekViewAdapter peekViewAdapter;
    PeekSource peekSource;

    public PublicVideoAdapterNew(Activity context, ArrayList<ExploreViewModel> list, boolean isMyProfile, PeekSource peekSource) {
        this.list = list;
        this.context = context;
        this.isMyProfile = isMyProfile;
        peekAndPop = new PeekAndPop.Builder(context)
                .peekLayout(R.layout.peek_view)
                .build();
       // peekAndPop.setOnGeneralActionListener(this);
        peekViewAdapter = new PeekViewAdapter(peekAndPop, context);
        this.peekSource = peekSource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_my_profile_video_list_new, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final ExploreViewModel exploreViewModel = list.get(i);
        if (exploreViewModel.type == ExploreVideoType.PUBLIC_VIDEO) {
            /*
                llBottom layout is no longer valid as per design, so has been dropped.
                View count for public videos has been moved to llTop.
            */

            //viewHolder.llBottom.setVisibility(View.VISIBLE);
            viewHolder.llTop.setVisibility(View.VISIBLE);
            viewHolder.llRTName.setVisibility(View.GONE);
            viewHolder.tvRTName.setVisibility(View.GONE);
            viewHolder.ivUnlistedRT.setVisibility(View.GONE);
            viewHolder.ivRoundTableIcon.setVisibility(View.GONE);
            DiscoverModel publicVideoVO = (DiscoverModel) exploreViewModel.getObj();
            viewHolder.tvNoOfRTViews.setText(Utility.formatNumber(publicVideoVO.getNoOfViews()));
            //viewHolder.txtReplies.setText(Utility.formatNumber(publicVideoVO.getNoOfConversation()));
            //viewHolder.txtWatched.setText(Utility.formatNumber(publicVideoVO.getNoOfViews()));

            if (publicVideoVO.getIsFlag() == 1) {
                viewHolder.ivReport.setImageResource(R.drawable.ic_retry);
                viewHolder.llReport.setVisibility(View.VISIBLE);
            } else if (publicVideoVO.getSettings() != null && !publicVideoVO.getSettings().getDiscoverable()) {
                viewHolder.ivReport.setImageResource(R.drawable.ic_icon_link);
                viewHolder.llReport.setVisibility(View.VISIBLE);
            } else {
                viewHolder.llReport.setVisibility(View.GONE);
            }

            viewHolder.ivThumb.setTag(i);
            viewHolder.llReport.setTag(i);

            if (!TextUtils.isEmpty(publicVideoVO.getVideoThumbnail())) {
                viewHolder.ivThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
                viewHolder.ivThumb.setPadding(0, 0, 0, 0);
                viewHolder.ivThumb.setBackgroundColor(context.getResources().getColor(R.color.colorWhite, null));
                if (!TextUtils.isEmpty(publicVideoVO.getImagePath())) {
                    Glide.with(context).load(publicVideoVO.getImagePath()).placeholder(R.color.color_E7E7E7).error(R.drawable.ic_no_preivew).into(viewHolder.ivThumb);
                } else if (!TextUtils.isEmpty(publicVideoVO.getVideoThumbnailLarge()) && TextUtils.isEmpty(publicVideoVO.getLocalVideoPath())) {
                    Glide.with(context).load(publicVideoVO.getVideoThumbnailLarge()).placeholder(R.color.color_E7E7E7).error(R.drawable.ic_no_preivew).into(viewHolder.ivThumb);
                } else {
                    Glide.with(context).load(publicVideoVO.getVideoThumbnail()).placeholder(R.color.color_E7E7E7).error(R.drawable.ic_no_preivew).into(viewHolder.ivThumb);
                }
            } else {
                viewHolder.ivThumb.setImageDrawable(null);
                viewHolder.ivThumb.setBackgroundColor(context.getResources().getColor(R.color.conv_dummy_1, null));
            }

            if (publicVideoVO.getImageUploadStatus() == 2 && publicVideoVO.getVideoUploadStatus() == 2 && publicVideoVO.getApiStatus() == 1) {
                viewHolder.rawLlRetry.setVisibility(View.GONE);
                viewHolder.ivProgressBar.setVisibility(View.GONE);
                setImageGrayScale(false, viewHolder.ivThumb);
                peekAndPop.addLongClickView(viewHolder.itemView, i);
            } else {
                peekAndPop.addLongClickView(viewHolder.itemView, i, false);
                if (publicVideoVO.getVideoId().equalsIgnoreCase("-1")) {
                    viewHolder.ivProgressBar.setVisibility(View.GONE);
                    viewHolder.rawLlRetry.setVisibility(View.GONE);
                } else {
                    setImageGrayScale(true, viewHolder.ivThumb);
                    if (publicVideoVO.isRetry()) {
                        viewHolder.ivProgressBar.setVisibility(View.GONE);
                        viewHolder.rawLlRetry.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.ivProgressBar.setVisibility(View.VISIBLE);
                        viewHolder.rawLlRetry.setVisibility(View.GONE);
                        if (publicVideoVO.getCompressionStatus() == 0 || publicVideoVO.getUploadProgress() == 0 || publicVideoVO.getUploadProgress() == 100) {
                            viewHolder.ivProgressBar.setIndeterminate(true);
                        } else {
                            viewHolder.ivProgressBar.setIndeterminate(false);
                            viewHolder.ivProgressBar.setProgress(publicVideoVO.getUploadProgress(), true);
                        }
                    }
                }
            }
        } else {
            /*
                llBottom dropped. Parent layout of tvRTName and ivUnlistedRT named llRTName.
                Its visibility toggled as per video type (public video/rt video)
            */

            peekAndPop.addLongClickView(viewHolder.itemView, i);
            ConversationModel conversationModel = (ConversationModel) exploreViewModel.getObj();
            //viewHolder.llBottom.setVisibility(View.GONE);
            viewHolder.llRTName.setVisibility(View.VISIBLE);
            viewHolder.llReport.setVisibility(View.GONE);
            viewHolder.rawLlRetry.setVisibility(View.GONE);
            viewHolder.ivProgressBar.setVisibility(View.GONE);
            viewHolder.tvRTName.setVisibility(View.VISIBLE);
            viewHolder.ivRoundTableIcon.setVisibility(View.GONE);
            viewHolder.llTop.setVisibility(View.VISIBLE);

            viewHolder.tvRTName.setText(conversationModel.getGroup().getName());
            viewHolder.tvNoOfRTViews.setText(conversationModel.getConvViews());
            if (!TextUtils.isEmpty(conversationModel.getImageURL())) {
                Glide.with(context).load(conversationModel.getImageURL()).placeholder(R.color.color_E7E7E7).into(viewHolder.ivThumb);
            }

            if (conversationModel.getSettings() != null && !conversationModel.getSettings().getDiscoverable()) {
                viewHolder.ivUnlistedRT.setVisibility(View.VISIBLE);
            } else {
                viewHolder.ivUnlistedRT.setVisibility(View.GONE);
            }
        }
    }

    public void updateVideoProgress(ViewHolder viewHolder, int progress) {
        if (progress == 100) {
            viewHolder.ivProgressBar.setIndeterminate(true);
        } else {
            viewHolder.ivProgressBar.setIndeterminate(false);
            viewHolder.ivProgressBar.setProgress(progress, true);
        }
    }

    /**
     * You can set the image to b/w with this method. Works fine with the
     * opacity.
     *
     * @param greyScale true if the gray scale should be activated.
     */
    public void setImageGrayScale(boolean greyScale, ImageView imageView) {
        if (greyScale) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            imageView.setColorFilter(new ColorMatrixColorFilter(matrix));
        } else {
            imageView.setColorFilter(null);
        }
    }

//    public void adjustPosAfterDelete() {
//        if (peekAndPop != null) {
//            peekAndPop.holdAndReleaseViews.clear();
//            for (int i = 0; i < list.size(); i++) {
//
//            }
//        }
//    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onPeek(View longClickView, int position) {
        mClickListener.onPeek();
        ExploreViewModel exploreViewModel = list.get(position);
        DiscoverModel publicVideoVO = new DiscoverModel();
        longClickView.getParent().requestDisallowInterceptTouchEvent(true);
        peekViewAdapter.setVideoPosition(position);
        //peekViewAdapter.setReplyCommentInterface(this);
        peekViewAdapter.setExploreViewModel(exploreViewModel);
        String videoType = "";
        if (exploreViewModel.type == ExploreVideoType.RT) {
            videoType = "RT";
        } else if (exploreViewModel.type == ExploreVideoType.PUBLIC_VIDEO) {
            videoType = "Public";
            publicVideoVO = (DiscoverModel) exploreViewModel.getObj();
        }
        boolean isFlagged = publicVideoVO.getIsFlag() == 1;
        peekViewAdapter.setRecyclerOptionsData(peekSource, videoType, isMyProfile, isFlagged);
        peekViewAdapter.initViews();
        peekViewAdapter.playVideo();
    }

    @Override
    public void onPop(View longClickView, int position) {
        peekViewAdapter.getPlayer().stop();
        peekViewAdapter.getPlayer().clearMediaItems();
        mClickListener.onPop();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivThumb;
        ImageView ivReport, ivUnlistedRT;
        LinearLayout llReport;
        LinearLayout rawLlRetry;
        LinearLayout llBottom;
        LinearLayout llTop;
        LinearLayout llRTName;
        ImageView ivRoundTableIcon;
        CustomTextView tvRTName, tvNoOfRTViews;
        CircularProgressIndicator ivProgressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            ivReport = itemView.findViewById(R.id.ivReport);
            llReport = itemView.findViewById(R.id.llReport);
            ivThumb = itemView.findViewById(R.id.my_profile_list_item_iv_Thumb);
            //txtReplies = itemView.findViewById(R.id.my_profile_list_item_txt_reply);
            //txtWatched = itemView.findViewById(R.id.my_profile_list_item_txt_watched);
            llRTName = itemView.findViewById(R.id.llRTName);
            rawLlRetry = itemView.findViewById(R.id.rawLlRetry);
            ivProgressBar = itemView.findViewById(R.id.ivProgressBar);
            //llBottom = itemView.findViewById(R.id.llBottom);
            tvRTName = itemView.findViewById(R.id.tvRTName);
            ivRoundTableIcon = itemView.findViewById(R.id.ivRoundTableIcon);
            tvNoOfRTViews = itemView.findViewById(R.id.tvNoOfRTViews);
            llTop = itemView.findViewById(R.id.llTop);
            ivUnlistedRT = itemView.findViewById(R.id.ivUnlistedRT);
            llReport.setOnClickListener(view -> {
                if (getBindingAdapterPosition() == -1 || list.size() == 0) {
                    return;
                }
                DiscoverModel discoverModel = (DiscoverModel) list.get(getBindingAdapterPosition()).getObj();
//                if (discoverModel.getIsFlag() == 1) {
//                    openBottomSheetDialogForFlagVideo(getBindingAdapterPosition());
//                }
            });
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getBindingAdapterPosition() == -1 || list.size() == 0) {
                return;
            }
            //mClickListener.onItemClick(view, getBindingAdapterPosition());
        }
    }

    private String getContentId(int pos) {
        return list.get(pos).getFeedId();
    }

    private String getContentCategory(int pos) {
        return list.get(pos).type.getValue();
    }

//    private void callForDeleteVideo(final int pos) {
//        try {
//            DiscoverModel discoverModel = (DiscoverModel) list.get(pos).getObj();
//            String videoId = discoverModel.getVideoId();
//            String module = Constants.DELETE + videoId;
//            JSONObject jsonObject = new JSONObject();
//            new BaseAPIService(context, module, Utility.getRequestBody(jsonObject.toString()), true, new ResponseListener() {
//                @Override
//                public void onSuccess(String response) {
//                    if (Utility.getDBHelper() != null) {
//                        Utility.getDBHelper().deletePublicVideoRecord(discoverModel.getVideoId());
//                    }
//                    list.remove(pos);
//                    notifyItemRemoved(pos);
//                }
//
//                @Override
//                public void onFailure(String error) {
//                    if (error.equalsIgnoreCase(Constants.VIDEO_ALREADY_DELETED_CODE)) {
//                        if (Utility.getDBHelper() != null) {
//                            Utility.getDBHelper().deletePublicVideoRecord(discoverModel.getVideoId());
//                        }
//                        list.remove(pos);
//                        notifyItemRemoved(pos);
//                    }
//                }
//            }, "POST", true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    Dialog mDeleteVideoDialog;
    BottomSheetDialog bottomSheetDialogFlaggedVideo;

//    private void showDeleteVideoDialog(final int pos) {
//        mDeleteVideoDialog = new Dialog(context);
//        mDeleteVideoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        mDeleteVideoDialog.setContentView(R.layout.common_simple_dialog);
//        mDeleteVideoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        mDeleteVideoDialog.show();
//
//        CustomTextView tvTitle = mDeleteVideoDialog.findViewById(R.id.dialog_title);
//        CustomTextView tvMsg = mDeleteVideoDialog.findViewById(R.id.dialog_message);
//        CustomTextView btnCancel = mDeleteVideoDialog.findViewById(R.id.dialog_btn_cancel);
//        CustomTextView btnYes = mDeleteVideoDialog.findViewById(R.id.dialog_btn_yes);
//
//        tvTitle.setText(context.getResources().getString(R.string.txt_warn_header));
//        tvMsg.setText(context.getResources().getString(R.string.txt_alert_delete_video1));
//        btnYes.setText(context.getResources().getString(R.string.txt_delete));
//        btnCancel.setOnClickListener(v -> mDeleteVideoDialog.dismiss());
//        btnYes.setOnClickListener(v -> {
//            mDeleteVideoDialog.dismiss();
//            if (Utility.isNetworkAvailable(context)) {
//                callForDeleteVideo(pos);
//            } else {
//                Utility.showToast(context, Constants.INTERNET_MSG);
//            }
//        });
//    }

//    public void openBottomSheetDialogForFlagVideo(int pos) {
//        View bottomSheetView = context.getLayoutInflater().inflate(R.layout.bottom_sheet_flag_video, null);
//        RelativeLayout rlUnlisted = bottomSheetView.findViewById(R.id.rlUnlisted);
//        TextView tvContactUs = bottomSheetView.findViewById(R.id.tvContactUs);
//
//        tvContactUs.setOnClickListener(v -> {
//            bottomSheetDialogFlaggedVideo.dismiss();
//            DiscoverModel discoverModel = (DiscoverModel) list.get(pos).getObj();
//            context.startActivity(new Intent(context, ContactUsActivityNew.class)
//                    .putExtra("video_id", discoverModel.getVideoId()));
//            context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//        });
//
//        rlUnlisted.setOnClickListener(v -> {
//            bottomSheetDialogFlaggedVideo.dismiss();
//        });
//
//        // Bottom sheet dialog
//        bottomSheetDialogFlaggedVideo = new BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme);
//        bottomSheetDialogFlaggedVideo.setContentView(bottomSheetView);
//        bottomSheetDialogFlaggedVideo.setCancelable(true);
////        bottomSheetDialogPrivacyOptions.setOnCancelListener(dialogInterface -> GenuInApplication.getInstance().sendSegmentLogs(Constants.CAMERA_QUICK_MENU_CLOSED, propertiesClose));
//        bottomSheetDialogFlaggedVideo.show();
//    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);

        void onReply(int position);

        void onComment(int position);

        void onPeek();

        void onPop();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
}