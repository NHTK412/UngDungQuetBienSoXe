package com.example.doanmonhocltm.model;

public class UserLocationRequest {
    private String  accountId;

    private Double latitude;

    private Double longitude;

    public UserLocationRequest(String accountId, Double latitude, Double longitude) {
        this.accountId = accountId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
