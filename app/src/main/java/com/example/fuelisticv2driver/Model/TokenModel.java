package com.example.fuelisticv2driver.Model;

public class TokenModel {
    private String phone, token;
    private boolean sellerToken, driverToken;


    public TokenModel() {

    }

    public TokenModel(String phone, String token, boolean sellerToken, boolean driverToken) {
        this.phone = phone;
        this.token = token;
        this.sellerToken = sellerToken;
        this.driverToken = driverToken;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSellerToken() {
        return sellerToken;
    }

    public void setSellerToken(boolean sellerToken) {
        this.sellerToken = sellerToken;
    }

    public boolean isDriverToken() {
        return driverToken;
    }

    public void setDriverToken(boolean driverToken) {
        this.driverToken = driverToken;
    }
}
