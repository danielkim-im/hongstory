package com.teamhns.hnsmobile.Model;

public class User {

    private String uid;
    private String name;
    private String pimg;
    private String bio;

    public User(String uid, String name, String pimg, String bio) {
        this.uid = uid;
        this.name = name;
        this.pimg = pimg;
        this.bio = bio;
    }

    public User() {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPimg() {
        return pimg;
    }

    public void setPimg(String pimg) {
        this.pimg = pimg;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
