package com.gmail.dangnguyendota.client;

import com.gmail.event.stage.StagePacket;

import java.util.Date;

public class Actor {
    public String serviceId;
    public String gameId;
    public String roomId;
    public String actorAddress;
    public long actorPort;
    public Date createTime;
    
    Actor(StagePacket.Actor actor) {
        this.serviceId = actor.getServiceId();
        this.gameId = actor.getGameId();
        this.roomId = actor.getRoomId();
        this.actorAddress = actor.getActorAddress();
        this.actorPort = actor.getActorPort();
        this.createTime = new Date(actor.getCreateTime() * 1000L);
    }
}
