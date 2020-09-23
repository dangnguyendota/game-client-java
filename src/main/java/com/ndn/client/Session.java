package com.ndn.client;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import org.apache.http.NameValuePair;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface Session {
    UUID id();

    String username();

    String displayName();

    String avatarCode();

    Object attribute(String key);

    String authToken();

    Date authExpiredTime();

    boolean authIsExpired();

    Date createTime();

    Date updateTime();

    void setListener(SessionListener listener);

    JsonObject post(@Nonnull String path, @Nonnull List<NameValuePair> params) throws IOException;

    ListenableFuture<Boolean> register(@Nonnull String username, @Nonnull String password, @Nonnull String displayName);

    ListenableFuture<Boolean> login(String username, String password);

    ListenableFuture<Void> searchRoom(@Nonnull String gameId);

    ListenableFuture<Void> cancelSearching();

    ListenableFuture<Void> joinRoom(@Nonnull UUID serviceId, @Nonnull String gameId, @Nonnull UUID roomId);

    ListenableFuture<Actor> queryRoom();

    ListenableFuture<Boolean> connectStage();

    ListenableFuture<Boolean> connectActor(@Nonnull String address, @Nonnull String gameId, @Nonnull String roomId);

    void disconnect();
}
