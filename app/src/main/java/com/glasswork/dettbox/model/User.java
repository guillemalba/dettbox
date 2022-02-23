package com.glasswork.dettbox.model;

public class User {

    public String name, surname, email, password, birth, groupName;

    public User () {}

    public User (String name, String surname, String email, String password, String birth, String groupName) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.birth = birth;
        this.groupName = groupName;
    }
}
