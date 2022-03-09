package com.glasswork.dettbox.model;

public class ActiveTask {
    private String description;
    private String member1;
    private String member2;

    public ActiveTask(String description, String member1, String member2) {
        this.description = description;
        this.member1 = member1;
        this.member2 = member2;
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
}
