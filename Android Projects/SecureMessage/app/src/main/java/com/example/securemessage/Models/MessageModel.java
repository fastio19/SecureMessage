package com.example.securemessage.Models;

public class MessageModel {
    String message;
    String uId;
    Long timeStamp;
    String key;
    public MessageModel(){
    }

    public MessageModel(String message, String uId, String key) {
        this.message = message;
        this.uId = uId;
        this.key = key;
    }

    public MessageModel(String message, String uId, Long timeStamp,String key) {
        this.message = message;
        this.uId = uId;
        this.timeStamp = timeStamp;
        this.key = key;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
