package com.gmail.dangnguyendota.client;

public class Config {
    String apiVersion;
    String apiAddress;
    String stageAddress;

    public Config withApiVersion(String version) {
        this.apiVersion = version;
        return this;
    }

    public Config withApiAddress(String address) {
        this.apiAddress = address;
        return this;
    }

    public Config withGameServerAddress(String address) {
        this.stageAddress = address;
        return this;
    }

    public String loginPath() {
        return apiAddress + "/api/" + apiVersion + "/user/login";
    }

    public String registerPath() {
        return apiAddress + "/api/" + apiVersion + "/user/register";
    }

    public String gameServerPath(String token) {
        return this.stageAddress + "?token=" + token;
    }
}
