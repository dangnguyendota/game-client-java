package com.gmail.dangnguyendota.client;

import java.util.Date;

public interface SessionListener {
    void onHttpError(final int code, final String error);

    void onStageConnected();

    void onStageDisconnected();

    void onStageError(final Error e);

    void onActorConnected();

    void onActorDisconnected();

    void onActorError(final Error e);

    void onException(final Exception e);

    void onJoinedRoom(final String serviceId, final String gameId, final String roomId, final String address, final Date createTime);

    void playerJoined(final String id, final String name, final String avatar);

    void playerLeft(final String id, final String name, final String avatar);

    void onRoomMessaged(final String gameId, final String roomId, final byte[] data, final long time);

    void onRelayedMessaged(final String gameId, final String roomId, final String senderId, final byte[] data, final long time);

    void onRoomClosed(final String gameId, final String roomId);
}
