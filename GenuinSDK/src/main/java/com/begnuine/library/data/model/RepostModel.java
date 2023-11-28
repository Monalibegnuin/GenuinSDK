package com.begnuine.library.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RepostModel implements Serializable {
    @SerializedName("owner")
    @Expose
    private MembersModel owner;
    @SerializedName("is_deleted")
    @Expose
    private boolean isDeleted = false;

    public MembersModel getOwner() {
        return owner;
    }

    public void setOwner(MembersModel owner) {
        this.owner = owner;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
