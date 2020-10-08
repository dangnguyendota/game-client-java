package com.gmail.dangnguyendota.client;

import com.gmail.event.actor.ActorPacket;
import com.gmail.event.stage.StagePacket;

public interface Error {

    int getCode();

    String getMessage();

    static Error create(StagePacket.Error e) {
        return create(e.getCode(), e.getMessage());
    }

    static Error create(ActorPacket.Error e) {
        return create(e.getCode(), e.getMessage());
    }

    static Error create(int code, String message) {
        GameError err = new GameError();
        err.code = code;
        err.message = message;
        return err;
    }
}
