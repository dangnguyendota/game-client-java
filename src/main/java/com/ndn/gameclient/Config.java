package com.ndn.gameclient;

public class Config {
    String apiVersion;
    String apiAddress;
    String gsAddress;

    public Config withApiVersion(String version) {
        this.apiVersion = version;
        return this;
    }

    public Config withApiAddress(String address) {
        this.apiAddress = address;
        return this;
    }

    public Config withGameServerAddress(String address) {
        this.gsAddress = address;
        return this;
    }

    public String loginPath() {
        return apiAddress + "/api/" + apiVersion + "/user/login";
    }

    public String registerPath() {
        return apiAddress + "/api/" + apiVersion + "/user/register";
    }

    public String gameServerPath(String token) {
        return this.gsAddress + "?token=" + token;
    }
}
