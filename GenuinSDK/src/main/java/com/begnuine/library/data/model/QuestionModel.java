package com.begnuine.library.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class QuestionModel implements Serializable {

    @SerializedName("question_id")
    @Expose
    private String questionId;
    @SerializedName("question")
    @Expose
    private String question;
    @SerializedName("date")
    @Expose
    private Long date;
    @SerializedName("share_url")
    @Expose
    private String shareURL;
    @SerializedName("owner")
    @Expose
    private MembersModel owner;

    public String getShareURL() {
        return shareURL;
    }

    public void setShareURL(String shareURL) {
        this.shareURL = shareURL;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public MembersModel getOwner() {
        return owner;
    }

    public void setOwner(MembersModel owner) {
        this.owner = owner;
    }
}
