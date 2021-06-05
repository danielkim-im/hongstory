package com.teamhns.hnsmobile.Model;

import com.google.firebase.database.ServerValue;

public class QAPost {
    private String postKey;
    private String description;
    private String picture;
    private String hashtag;
    private String contentType;
    private Object timeStamp;
    private String userId;
    private String privacy;

    public QAPost(String description,
                  String picture, String hashtag,
                  String contentType,
                  String privacy,
                  String userId) {
        this.description = description;
        this.picture = picture;
        this.hashtag = hashtag;
        this.contentType = contentType;
        this.privacy = privacy;
        this.userId = userId;
        this.timeStamp = ServerValue.TIMESTAMP;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Object getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
