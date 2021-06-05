package com.teamhns.hnsmobile.Model;

import android.graphics.drawable.Drawable;

public class Barogo {
    private String name;
    private String url;
    private Drawable image;

    public Barogo(String name, String url, Drawable image) {
        this.name = name;
        this.url = url;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }
}
