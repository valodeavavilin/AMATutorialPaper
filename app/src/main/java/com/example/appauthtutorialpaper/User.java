package com.example.appauthtutorialpaper;

public class User {
    public String firstName, lastName, email, phone, password, profileImageUrl;

    public User() {} // Constructor gol necesar pentru Firebase

    public User(String firstName, String lastName, String email, String phone, String password, String profileImageUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
    }
}
