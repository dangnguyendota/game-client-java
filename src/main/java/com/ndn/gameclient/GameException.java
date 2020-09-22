package com.ndn.gameclient;

public class GameException extends Throwable {
    private final ErrorCode code;
    private final String message;
    public GameException(ErrorCode code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    public ErrorCode getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
