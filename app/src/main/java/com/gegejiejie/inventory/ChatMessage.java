package com.gegejiejie.inventory;

import java.util.Date;

public class ChatMessage {
    private long id;
    private boolean isMe;
    private String message;
    private int userId;
    private String dateTime;
    private static long incremental_id = 1;
    public ChatMessage(boolean bIsMe, String tMessage, int idUserId)
    {
        isMe = bIsMe;
        message = tMessage;
        userId = idUserId;
        id = incremental_id++;
        dateTime = new Date().toString();
    }
    public ChatMessage() {

    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public boolean getIsme() {
        return isMe;
    }
    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public long getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDate() {
        return dateTime;
    }

    public void setDate(String dateTime) {
        this.dateTime = dateTime;
    }
}
