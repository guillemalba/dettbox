package com.glasswork.dettbox.model;

public class User {

    public String name, email, password, groupName, backgroundColor;

    public User () {}

    public User (String name, String email, String password, String groupName) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.groupName = groupName;
    }

    public User(String name, String email, String password, String groupName, String backgroundColor) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.groupName = groupName;
        this.backgroundColor = backgroundColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
