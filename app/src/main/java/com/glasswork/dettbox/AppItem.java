package com.glasswork.dettbox;

public class AppItem {
    private String appName;
    private String appTimeUsed;

    public AppItem(String appName, String appTimeUsed) {
        this.appName = appName;
        this.appTimeUsed = appTimeUsed;
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
