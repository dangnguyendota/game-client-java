package com.ndn.gameclient;

import com.ndn.event.stage.StagePacket;

import java.util.Date;
import java.util.UUID;

public class Actor {
    public UUID serviceId;
    public String gameId;
    public UUID roomId;
    public String actorAddress;
    public long actorPort;
    public Date createTime;
    
    Actor(StagePacket.Actor actor) {
        this.serviceId = UUID.fromString(actor.getServiceId());
        this.gameId = actor.getGameId();
        this.roomId = UUID.fromString(actor.getRoomId());
        this.actorAddress = actor.getActorAddress();
        this.actorPort = actor.getActorPort();
        this.createTime = new Date(actor.getCreateTime() * 1000L);
    }
}
