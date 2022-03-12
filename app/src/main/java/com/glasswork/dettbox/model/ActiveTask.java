package com.glasswork.dettbox.model;

public class ActiveTask {
    private String id;
    private String title;
    private String description;
    private String member1;
    private String member2;
    private String time;
    private String verifiedCount;

    public ActiveTask(String id, String title, String description, String member1, String member2, String time, String verifiedCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.member1 = member1;
        this.member2 = member2;
        this.time = time;
        this.verifiedCount = verifiedCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMember1() {
        return member1;
    }

    public void setMember1(String member1) {
        this.member1 = member1;
    }

    public String getMember2() {
        return member2;
    }

    public void setMember2(String member2) {
        this.member2 = member2;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVerifiedCount() {
        return verifiedCount;
    }

    public void setVerifiedCount(String verifiedCount) {
        this.verifiedCount = verifiedCount;
    }
}
