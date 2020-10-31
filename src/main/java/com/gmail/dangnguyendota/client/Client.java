package com.gmail.dangnguyendota.client;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;
import com.neovisionaries.ws.client.*;
import dangnguyendota.event.actor.ActorPacket;
import dangnguyendota.event.stage.StagePacket;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Session {
    private SessionListener listener;
    private WebSocket stageWs;
    private WebSocket actorWs;
    private final PacketManager packetManager;
    private final ExecutorService queue;
    private final String socketAddr, token;

    public Client(String socketAddr, String token) {
        packetManager = new PacketManager();
        queue = Executors.newSingleThreadExecutor();
        this.socketAddr = socketAddr;
        this.token = token;
    }

    @Override
    public void setListener(SessionListener listener) {
        this.listener = listener;
    }

    @Override
    public String authToken() {
        return token;
    }

    @Override
    public ListenableFuture<Boolean> connectStage() {
        try {
            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);
            stageWs = factory.createSocket(socketAddr + "?token=" + token);
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
    public void sendRoomData(@Nonnull String gameId, @Nonnull String roomId, @Nonnull byte[] data) {
        if (actorWs == null || !actorWs.isOpen()) {
            listener.onException(new Exception("actor is not connected"));
            return;
        }

        ActorPacket.RoomMessage roomMessage = ActorPacket.RoomMessage.newBuilder().
                setGameId(gameId).setRoomId(roomId).setTime(System.currentTimeMillis() / 1000L).
                setData(ByteString.copyFrom(data)).build();
        ActorPacket.Packet packet = ActorPacket.Packet.newBuilder().setRoomMessage(roomMessage).build();
        actorWs.sendBinary(packet.toByteArray());
    }

    @Override
    public synchronized void disconnectStage() {
        packetManager.clear();
        if (stageWs == null || !stageWs.isOpen()) {
            return;
        }
        stageWs.disconnect();
    }

    @Override
    public synchronized void disconnectActor() {
        if (actorWs == null || !actorWs.isOpen()) {
            return;
        }
        actorWs.disconnect();
    }

    private void onStageBinaryMessage(byte[] binary) {
        try {
            final StagePacket.Packet packet = StagePacket.Packet.parseFrom(binary);
            if (packet.getPacketId() == null || "".equals(packet.getPacketId())) {
                // server message
                queue.execute(() -> {
                    if (packet.hasError()) {
                        listener.onStageError(packet.getError().getCode(), packet.getError().getMessage());
                    } else {
                        if (packet.hasActor()) {
                            Actor actor = new Actor(packet.getActor());
                            listener.onJoinedRoom(actor.serviceId, actor.gameId, actor.roomId,
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
                    future.setException(new GameException(ErrorCode.Unknown,
                            packet.getError().getMessage()));
                } else {
                    if (packet.hasActor()) {
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
            queue.execute(() -> {
                if (packet.hasMessages()) {
                    ActorPacket.RealtimeMessages messages = packet.getMessages();
                    List<ActorPacket.RealtimeMessage> list = messages.getMessagesList();
                    for (ActorPacket.RealtimeMessage message : list) {
                        listener.onRelayedMessage(messages.getGameId(), messages.getRoomId(), message.getPresenceId(),
                                message.getData().toByteArray(), message.getCreateTime());
                    }
                } else if (packet.hasRoomMessage()) {
                    ActorPacket.RoomMessage roomMessage = packet.getRoomMessage();
                    listener.onRoomMessage(roomMessage.getGameId(), roomMessage.getRoomId(), roomMessage.getData().toByteArray(), roomMessage.getTime());
                } else if (packet.hasJoinLeave()) {
                    ActorPacket.JoinLeave joinLeave = packet.getJoinLeave();
                    if (joinLeave.getJoinsList() != null) {
                        // joined actor room
                        for (ActorPacket.Presence presence : joinLeave.getJoinsList()) {
                            listener.playerJoined(presence.getId(), presence.getDisplayName(), presence.getAvatar());
                        }
                    }
                    if (joinLeave.getLeavesList() != null) {
                        // left actor room
                        for (ActorPacket.Presence presence : joinLeave.getLeavesList()) {
                            listener.playerLeft(presence.getId(), presence.getDisplayName(), presence.getAvatar());
                        }
                    }
                } else if (packet.hasClosedRoom()) {
                    listener.onRoomClosed(packet.getClosedRoom().getGameId(), packet.getClosedRoom().getRoomId());
                } else if (packet.hasError()) {
                    listener.onActorError(packet.getError().getCode(), packet.getError().getMessage());
                } else if (packet.hasRoomError()) {
                    ActorPacket.RoomError error = packet.getRoomError();
                    listener.onRoomError(error.getGameId(), error.getRoomId(), error.getCode(), error.getMessage());
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
