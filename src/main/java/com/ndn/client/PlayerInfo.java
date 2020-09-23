package com.ndn.client;

import com.google.gson.Gson;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class PlayerInfo {
    public UUID id;
    public String username, displayName, avatar;
    public Date createdAt, updatedAt, tokenExpiredTime;
    public String token;
    public Map<String, Object> attributes;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}