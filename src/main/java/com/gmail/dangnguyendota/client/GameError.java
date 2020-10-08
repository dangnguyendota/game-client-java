package com.gmail.dangnguyendota.client;

public class GameError implements Error {
    protected int code;
    protected String message;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
