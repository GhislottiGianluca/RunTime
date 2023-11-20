package com.example.runtime.firestore.models;

public class User {
    private String username;
    private String password;
    private String uuid;

    public User(String username, String password, String uuid) {
        this.username = username;
        this.password = password;
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUuid() {
        return uuid;
    }

}
