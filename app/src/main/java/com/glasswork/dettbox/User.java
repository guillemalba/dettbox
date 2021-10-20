package com.glasswork.dettbox;

public class User {

    public String email, password, birth;
    public boolean led_boolean;

    public User () {}

    public User (String email, String password, String birth, boolean led_boolean) {
        this.email = email;
        this.password = password;
        this.birth = birth;
        this.led_boolean = led_boolean;
    }
}
