package com.glasswork.dettbox.model;

public class UserRanking {

    public String id, name, position, time;

    public UserRanking () {}

    public UserRanking (String id, String name, String position, String time) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
