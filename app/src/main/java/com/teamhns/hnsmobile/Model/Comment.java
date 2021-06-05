package com.teamhns.hnsmobile.Model;

import com.google.firebase.database.ServerValue;

public class Comment {

    private String content, uid, commentKey, privacy;
    private Object timestamp;

    public Comment(String content, String privacy, String uid) {
        this.content = content;
        this.uid = uid;
        this.privacy = privacy;
        this.timestamp = ServerValue.TIMESTAMP;
    }
    public Comment() {

    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getCommentKey() {
        return commentKey;
    }

    public void setCommentKey(String commentKey) {
        this.commentKey = commentKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}

