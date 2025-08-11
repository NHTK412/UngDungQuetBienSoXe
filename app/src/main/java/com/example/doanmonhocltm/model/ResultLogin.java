package com.example.doanmonhocltm.model;


import java.util.List;

public class ResultLogin {
    private String token;
    private String type;
    private String id;
    private String username;
    private List<String> roles;


    private String email;

    public ResultLogin(String token, String type, String id, String username, List<String> roles, String email) {
        this.token = token;
        this.type = type;
        this.id = id;
        this.username = username;
        this.roles = roles;

        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
