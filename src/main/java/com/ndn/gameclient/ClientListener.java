package com.ndn.gameclient;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.UUID;

public interface ClientListener {
    void onConnected();

    void onDisconnected();

    void onHttpError(int code, String error);

    void onError(Error e);

    void onException(Exception e);

    void onJoinedRoom(UUID serviceId, String gameId, UUID roomId, String address, Date createTime);
}
