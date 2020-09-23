package com.ndn.client;

import java.util.Date;
import java.util.UUID;

public interface SessionListener {
    void onHttpError(int code, String error);

    void onStageConnected();

    void onStageDisconnected();

    void onStageError(Error e);

    void onActorConnected();

    void onActorDisconnected();

    void onActorError(Error e);

    void onException(Exception e);

    void onJoinedRoom(String serviceId, String gameId, String roomId, String address, Date createTime);

    void playerJoined(String id, String name, String avatar);

    void playerLeft(String id, String name, String avatar);

    void onMessaged(String gameId, String roomId, byte[] data, Date time);
}
