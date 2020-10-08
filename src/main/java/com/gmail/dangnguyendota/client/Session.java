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
    UUID id();

    @CheckForNull
    @CheckReturnValue
    String username();

    @CheckForNull
    @CheckReturnValue
    String displayName();

    @CheckForNull
    @CheckReturnValue
    String avatarCode();

    @CheckForNull
    @CheckReturnValue
    Object attribute(String key);

    @CheckForNull
    @CheckReturnValue
    String authToken();

    @CheckForNull
    @CheckReturnValue
    Date authExpiredTime();

    @CheckForNull
    @CheckReturnValue
    Boolean authIsExpired();

    @CheckForNull
    @CheckReturnValue
    Date createTime();

    @CheckForNull
    @CheckReturnValue
    Date updateTime();

    void setListener(SessionListener listener);

    JsonObject post(@Nonnull String path, @Nonnull List<NameValuePair> params) throws IOException;

    ListenableFuture<Boolean> register(@Nonnull String username, @Nonnull String password, @Nonnull String displayName);

    ListenableFuture<Boolean> login(@Nonnull String username, @Nonnull String password);

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
