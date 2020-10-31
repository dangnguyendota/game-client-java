package com.gmail.dangnguyendota.client;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;

import org.apache.http.NameValuePair;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface Session {
    @CheckForNull
    @CheckReturnValue
    String authToken();

    void setListener(SessionListener listener);

    ListenableFuture<Boolean> connectStage();

    ListenableFuture<Boolean> connectActor(@Nonnull String address, @Nonnull String gameId, @Nonnull String roomId);

    void disconnectStage();

    void disconnectActor();

    ListenableFuture<Void> searchRoom(@Nonnull String gameId);

    ListenableFuture<Void> cancelSearching();

    ListenableFuture<Void> joinRoom(@Nonnull UUID serviceId, @Nonnull String gameId, @Nonnull UUID roomId);

    ListenableFuture<Actor> queryRoom();

    void sendRoomData(@Nonnull String gameId, @Nonnull String roomId, @Nonnull byte[] data);
}
