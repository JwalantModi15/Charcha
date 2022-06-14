package com.techinnovator.jmcharcha.requests;

public class RequestsModel {

    private String userId;
    private String img;
    private String userName;

    public RequestsModel(String userId, String img, String userName) {
        this.userId = userId;
        this.img = img;
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
