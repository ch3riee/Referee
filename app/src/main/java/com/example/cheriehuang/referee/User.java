package com.example.cheriehuang.referee;

/**
 * Created by cheriehuang on 5/23/17.
 */

public class User {

    public String name;
    public String email;
    public String username;
    public String registeredDate;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getUsername()
    {
        return this.username;
    }

    public User(String username, String name, String email, String registered) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.registeredDate = registered;
    }

}
