package com.glasswork.dettbox.model;

import android.graphics.drawable.Drawable;

public class AppItem {
    private String appName;
    private String appTimeUsed;
    private Drawable drawableIcon;

    public AppItem(String appName, String appTimeUsed, Drawable drawableIcon) {
        this.appName = appName;
        this.appTimeUsed = appTimeUsed;
        this.drawableIcon = drawableIcon;
    }

    public Drawable getDrawableIcon() {
        return drawableIcon;
    }

    public void setDrawableIcon(Drawable drawableIcon) {
        this.drawableIcon = drawableIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppTimeUsed() {
        return appTimeUsed;
    }

    public void setAppTimeUsed(String appTimeUsed) {
        this.appTimeUsed = appTimeUsed;
    }
}
