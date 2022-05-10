package com.glasswork.dettbox.model;

public class UserRanking {

    public String id, name, position, time, totalTaskMinutes;

    public UserRanking () {}

    public UserRanking (String id, String name, String position, String time, String totalTaskMinutes) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.time = time;
        this.totalTaskMinutes = totalTaskMinutes;
    }

    public UserRanking(String name, String position, String time) {
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

    public String getTotalTaskMinutes() {
        return totalTaskMinutes;
    }

    public void setTotalTaskMinutes(String totalTaskMinutes) {
        this.totalTaskMinutes = totalTaskMinutes;
    }
}
