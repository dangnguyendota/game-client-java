package com.gmail.dangnguyendota.client;

import com.gmail.dangnguyendota.event.actor.ActorPacket;
import com.gmail.dangnguyendota.event.stage.StagePacket;
import com.google.protobuf.ByteString;
import com.neovisionaries.ws.client.*;
import javax.annotation.Nonnull;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Session {
    private SessionListener listener;
    private WebSocket stageWs;
    private WebSocket actorWs;
    private final ExecutorService queue;
    private final String socketAddr, token, node;

    public Client(String socketAddr, String node, String token) {
        queue = Executors.newSingleThreadExecutor();
        this.socketAddr = socketAddr;
        this.token = token;
        this.node = node;
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
    public boolean connectStage() {
        if (stageOpened()) {
            listener.onException(new Exception("stage is already opened"));
            return false;
        }

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
            return true;
        } catch (Exception e) {
            listener.onException(e);
        }
        return false;
    }

    @Override
    public boolean connectActor(@Nonnull String address, @Nonnull String gameId, @Nonnull String roomId) {
        if (actorOpened()) {
            listener.onException(new Exception("actor is already opened"));
            return false;
        }
        try {
            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);
            actorWs = factory.createSocket("ws://" + address + "/ws?token=" + authToken() + "&game_id=" + gameId + "&room_id=" + roomId + "&node=" + node);
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
            return true;
        } catch (Exception e) {
            listener.onException(e);
        }
        return false;
    }

    @Override
    public synchronized void disconnectStage() {
        if(!stageOpened()) return;
        stageWs.disconnect();
    }

    @Override
    public synchronized void disconnectActor() {
        if (!actorOpened()) return;
        actorWs.disconnect();
    }


    @Override
    public void loadConfig() {
        if (!stageOpened()) return;
        StagePacket.LoadUserConfig packet = StagePacket.LoadUserConfig.newBuilder().build();
        stageWs.sendBinary(StagePacket.Packet.newBuilder().setLoadUserConfig(packet).build().toByteArray());
    }

    @Override
    public void search(@Nonnull String gameId) {
        if (!stageOpened()) return;
        StagePacket.Search packet = StagePacket.Search.newBuilder().setGameId(gameId).build();
        stageWs.sendBinary(StagePacket.Packet.newBuilder().setSearch(packet).build().toByteArray());
    }

    @Override
    public void cancel() {
        if (!stageOpened()) return;

        StagePacket.Cancel packet = StagePacket.Cancel.newBuilder().build();
        stageWs.sendBinary(StagePacket.Packet.newBuilder().setCancel(packet).build().toByteArray());
    }

    @Override
    public void sendRoomData(@Nonnull String gameId, @Nonnull String roomId, @Nonnull byte[] data) {
        if (!actorOpened()) return;

        ActorPacket.RoomMessage roomMessage = ActorPacket.RoomMessage.newBuilder().
                setGameId(gameId).setRoomId(roomId).setTime(System.currentTimeMillis() / 1000L).
                setData(ByteString.copyFrom(data)).build();
        ActorPacket.Packet packet = ActorPacket.Packet.newBuilder().setRoomMessage(roomMessage).build();
        actorWs.sendBinary(packet.toByteArray());
    }

    @Override
    public void leave(@Nonnull String gameId, @Nonnull String roomId) {
        if(!actorOpened()) return;
        ActorPacket.LeaveRoom packet = ActorPacket.LeaveRoom.newBuilder().setGameId(gameId).setRoomId(roomId).build();
        actorWs.sendBinary(ActorPacket.Packet.newBuilder().setLeaveRoom(packet).build().toByteArray());
    }

    private void onStageBinaryMessage(byte[] binary) {
        try {
            final StagePacket.Packet packet = StagePacket.Packet.parseFrom(binary);
            queue.execute(() -> {
                if (packet.hasError()) {
                    listener.onStageError(packet.getError().getCode(), packet.getError().getMessage());
                } else if(packet.hasUserConfig()) {
                    StagePacket.UserConfig config = packet.getUserConfig();
                    listener.onStageData(config);
                } else if(packet.hasSearchResult()) {
                    StagePacket.SearchResult result = packet.getSearchResult();
                    listener.onSearchResult(result.getOk());
                } else if(packet.hasCancelResult()) {
                    StagePacket.CancelResult result = packet.getCancelResult();
                    listener.onCancelResult(result.getOk());
                } else if(packet.hasRoom()) {
                    StagePacket.Room room = packet.getRoom();
                    listener.onCreatedRoom(room.getServiceId(), room.getGameId(), room.getRoomId(), room.getAddr() + ":" + room.getPort(), room.getState(), room.getPlayersList());
                }
            });
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
                } else if (packet.hasClosedSession()) {
                    ActorPacket.ClosedSession closedSession = packet.getClosedSession();
                    listener.onKicked(closedSession.getGameId(), closedSession.getRoomId());
                }
            });
        } catch (Exception e) {
            this.listener.onException(e);
        }
    }

    private boolean stageOpened() {
        return stageWs != null && stageWs.isOpen();
    }

    private boolean actorOpened() {
        return actorWs != null && actorWs.isOpen();
    }
}
