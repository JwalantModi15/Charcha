package com.techinnovator.jmcharcha.chats;

public class MessageModel {
    private String from;
    private String message;
    private String messageId;
    private long time;
    private String type;

    public MessageModel(){

    }

    public MessageModel(String from, String message, String messageId, long time, String type) {
        this.from = from;
        this.message = message;
        this.messageId = messageId;
        this.time = time;
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
