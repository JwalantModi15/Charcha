package com.techinnovator.jmcharcha.chats;

public class ChatListModel {
    private String userId;
    private String name;
    private String img;
    private String unReadCount;
    private String lastMessage;
    private String lastMessageTime;

    public ChatListModel(String userId, String name, String img, String unReadCount, String lastMessage, String lastMessageTime) {
        this.userId = userId;
        this.name = name;
        this.img = img;
        this.unReadCount = unReadCount;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(String unReadCount) {
        this.unReadCount = unReadCount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}
