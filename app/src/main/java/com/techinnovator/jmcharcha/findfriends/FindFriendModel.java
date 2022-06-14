package com.techinnovator.jmcharcha.findfriends;

public class FindFriendModel {
    private String img;
    private String name;
    private String userId;
    private boolean requestStatus;

    public FindFriendModel(String img, String name, String userId, boolean requestStatus) {
        this.img = img;
        this.name = name;
        this.userId = userId;
        this.requestStatus = requestStatus;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(boolean requestStatus) {
        this.requestStatus = requestStatus;
    }
}

