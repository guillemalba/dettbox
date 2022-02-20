package com.glasswork.dettbox.model;

public class User {

    public String email, password, birth, groupName;

    public User () {}

    public User (String email, String password, String birth, String groupName) {
        this.email = email;
        this.password = password;
        this.birth = birth;
        this.groupName = groupName;
    }
}
