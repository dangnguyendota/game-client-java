package com.gmail.dangnguyendota.client;

import dangnguyendota.event.actor.ActorPacket;
import dangnguyendota.event.stage.StagePacket;

public class Error {
    protected int code;
    protected String message;

    public Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Error(StagePacket.Error e) {
        this.code = e.getCode();
        this.message = e.getMessage();
    }

    public Error(ActorPacket.Error e) {
        this.code = e.getCode();
        this.message = e.getMessage();
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
