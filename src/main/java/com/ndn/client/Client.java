package com.ndn.client;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ndn.event.actor.ActorPacket;
import com.ndn.event.stage.StagePacket;
import com.neovisionaries.ws.client.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Session {
    public static Session createSession(Config config) {
        return new Client(config);
    }

    private final Config config;
    private HttpClient client;
    private PlayerInfo info;
    private SessionListener listener;
    private WebSocket stageWs;
    private WebSocket actorWs;
    private PacketManager packetManager;
    private ExecutorService queue;

    public Client(Config config) {
        this.config = config;
        this.client = HttpClients.createDefault();
        packetManager = new PacketManager();
        queue = Executors.newSingleThreadExecutor();

    }

    @Override
    public void setListener(SessionListener listener) {
        this.listener = listener;
    }


    @Override
    public JsonObject post(@Nonnull String path, @Nonnull List<NameValuePair> params) throws IOException {
        HttpPost post = new HttpPost(path);
        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        try (InputStream stream = entity.getContent()) {
            JsonObject res = JsonParser.parseString(IOUtils.toString(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                this.listener.onHttpError(res.get("code").getAsInt(), res.get("error").getAsString());
            } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return res.get("response").getAsJsonObject();
            }
        }

        return null;
    }

    @Override
    public UUID id() {
        return info.id;
    }

    @Override
    public String username() {
        return info.username;
    }

    @Override
    public String displayName() {
        return info.displayName;
    }

    @Override
    public String avatarCode() {
        return info.avatar;
    }

    @Override
    public Object attribute(String key) {
        return info.attributes.get(key);
    }

    @Override
    public String authToken() {
        return info.token;
    }

    @Override
    public Date authExpiredTime() {
        return info.tokenExpiredTime;
    }

    @Override
    public boolean authIsExpired() {
        return this.authExpiredTime().getTime() - new Date().getTime() < 0L;
    }

    @Override
    public Date createTime() {
        return info.createdAt;
    }

    @Override
    public Date updateTime() {
        return info.updatedAt;
    }

    @Override
    public ListenableFuture<Boolean> login(String username, String password) {
        try {
            List<NameValuePair> params = new ArrayList<>(2);
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            JsonObject object = this.post(config.loginPath(), params);
            if (object != null) {
                info = new PlayerInfo();
                info.id = UUID.fromString(object.get("id").getAsString());
                info.username = object.get("username").getAsString();
                info.displayName = object.get("display_name").getAsString();
                info.avatar = object.get("avatar").getAsString();
                info.createdAt = new Date(object.get("created_at").getAsLong() * 1000L);
                info.updatedAt = new Date(object.get("updated_at").getAsLong() * 1000L);
                info.token = object.get("token").getAsString();
                info.tokenExpiredTime = new Date(object.get("expired").getAsLong() * 1000L);
                return Futures.immediateFuture(true);
            }
        } catch (Exception e) {
            listener.onException(e);
        }
        return Futures.immediateFuture(false);
    }

    @Override
    public ListenableFuture<Boolean> register(@Nonnull String username, @Nonnull String password, @Nonnull String displayName) {
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("display_name", displayName));
            JsonObject object = this.post(config.registerPath(), params);
            return Futures.immediateFuture(object != null);
        } catch (Exception e) {
            listener.onException(e);
        }
        return Futures.immediateFuture(false);
    }

    @Override
    public ListenableFuture<Boolean> connectStage() {
        try {
            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);
            stageWs = factory.createSocket(this.config.gameServerPath(this.info.token));
            WebSocketAdapter adapter = new WebSocketAdapter() {
                @Override
                public void onBinaryMessage(WebSocket websocket, byte[] binary) {
                    Client.this.onStageBinaryMessage(binary);
                }

                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                    Client.this.listener.onStageConnected();
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
                    Client.this.listener.onStageDisconnected();
                }
            };
            stageWs.addListener(adapter);
            stageWs.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);
            stageWs.connect();
            return Futures.immediateFuture(true);
        } catch (Exception e) {
            listener.onException(e);
        }
        return Futures.immediateFuture(false);
    }

    @Override
    public ListenableFuture<Boolean> connectActor(@Nonnull String address, @Nonnull String gameId, @Nonnull String roomId) {
        try {
            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);
            actorWs = factory.createSocket("ws://" + address + "/ws?token=" + authToken() + "&game_id=" + gameId + "&room_id=" + roomId);
            WebSocketAdapter adapter = new WebSocketAdapter() {
                @Override
                public void onBinaryMessage(WebSocket websocket, byte[] binary) {
                    Client.this.onActorBinaryMessage(binary);
                }

                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                    Client.this.listener.onActorConnected();
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
                    Client.this.listener.onActorDisconnected();
                }
            };
            actorWs.addListener(adapter);
            actorWs.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);
            actorWs.connect();
            return Futures.immediateFuture(true);
        } catch (Exception e) {
            listener.onException(e);
        }
        return Futures.immediateFuture(false);
    }

    @Override
    public ListenableFuture<Void> searchRoom(@Nonnull String gameId) {
        if (stageWs == null || !stageWs.isOpen()) {
            return Futures.immediateFailedFuture(new Exception("websocket is not connected"));
        }
        StagePacket.SearchRoom searchRoom = StagePacket.SearchRoom.newBuilder().
                setGameId(gameId).build();
        return sendPacket(StagePacket.Packet.newBuilder().
                setSearchRoom(searchRoom));
    }

    @Override
    public ListenableFuture<Void> cancelSearching() {
        if (stageWs == null || !stageWs.isOpen()) {
            return Futures.immediateFailedFuture(new Exception("websocket is not connected"));
        }
        StagePacket.CancelSearchRoom cancelSearchRoom = StagePacket.CancelSearchRoom.newBuilder().build();
        return sendPacket(StagePacket.Packet.newBuilder().setCancelSearchRoom(cancelSearchRoom));
    }

    @Override
    public ListenableFuture<Void> joinRoom(@Nonnull UUID serviceId, @Nonnull String gameId, @Nonnull UUID roomId) {
        if (stageWs == null || !stageWs.isOpen()) {
            return Futures.immediateFailedFuture(new Exception("websocket is not connected"));
        }
        StagePacket.JoinCreatedRoom joinCreatedRoom = StagePacket.JoinCreatedRoom.newBuilder().
                setServiceId(serviceId.toString()).setGameId(gameId).setRoomId(roomId.toString()).build();
        return sendPacket(StagePacket.Packet.newBuilder().setJoinCreatedRoom(joinCreatedRoom));
    }

    @Override
    public ListenableFuture<Actor> queryRoom() {
        if (stageWs == null || !stageWs.isOpen()) {
            return Futures.immediateFailedFuture(new Exception("websocket is not connected"));
        }
        StagePacket.QueryRoom queryRoom = StagePacket.QueryRoom.newBuilder().build();
        return sendPacket(StagePacket.Packet.newBuilder().setQueryRoom(queryRoom));
    }

    @Override
    public synchronized void disconnect() {
        packetManager.clear();
        stageWs.disconnect();
    }

    private void onStageBinaryMessage(byte[] binary) {
        try {
            final StagePacket.Packet packet = StagePacket.Packet.parseFrom(binary);
            if (packet.getPacketId() == null || "".equals(packet.getPacketId())) {
                // server message
                queue.execute(() -> {
                    if (packet.hasError()) {
                        listener.onStageError(new Error(packet.getError().getCode(), packet.getError().getMessage()));
                    } else {
                        if (packet.hasActor()) {
                            Actor actor = new Actor(packet.getActor());
                            listener.onJoinedRoom(actor.serviceId.toString(), actor.gameId, actor.roomId,
                                    actor.actorAddress + ":" + actor.actorPort, actor.createTime);
                        }
                    }
                });
            } else {
                // server response
                final SettableFuture future = packetManager.remove(packet.getPacketId());
                if (future == null) {
                    return;
                }

                if (packet.hasError()) {
                    future.setException(new GameException(ErrorCode.fromCode(packet.getError().getCode()),
                            packet.getError().getMessage()));
                } else {
                    if(packet.hasActor()) {
                        future.set(new Actor(packet.getActor()));
                    } else {
                        future.set(null);
                    }
                }
            }
        } catch (Exception e) {
            this.listener.onException(e);
        }
    }

    private void onActorBinaryMessage(byte[] binary) {
        try {
            final ActorPacket.Packet packet = ActorPacket.Packet.parseFrom(binary);
            queue.execute(()->{
                if(packet.hasRoomMessage()) {
                    ActorPacket.RoomMessage roomMessage = packet.getRoomMessage();
                    listener.onMessaged(roomMessage.getGameId(), roomMessage.getRoomId(), roomMessage.getData().toByteArray(), new Date(roomMessage.getTime() * 1000L));
                } else if(packet.hasJoinLeave()) {
                    ActorPacket.JoinLeave joinLeave = packet.getJoinLeave();
                    if(joinLeave.getJoinsList() != null) {
                        // joined actor room
                        for(ActorPacket.Presence presence : joinLeave.getJoinsList()) {
                            listener.playerJoined(presence.getId(), presence.getDisplayName(), presence.getAvatar());
                        }
                    }
                    if(joinLeave.getLeavesList() != null) {
                        // left actor room
                        for(ActorPacket.Presence presence : joinLeave.getLeavesList()) {
                            listener.playerJoined(presence.getId(), presence.getDisplayName(), presence.getAvatar());
                        }
                    }
                }
            });
        } catch (Exception e) {
            this.listener.onException(e);
        }
    }

    private <T> ListenableFuture<T> sendPacket(@Nonnull StagePacket.Packet.Builder builder) {
        String id = UUID.randomUUID().toString();
        SettableFuture<T> future = SettableFuture.create();
        this.packetManager.set(id, future);
        builder.setPacketId(id);
        stageWs.sendBinary(builder.build().toByteArray());
        return future;
    }
}