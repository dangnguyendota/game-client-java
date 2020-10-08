package com.gmail.dangnguyendota.example;

import com.gmail.dangnguyendota.client.Error;
import com.gmail.dangnguyendota.client.SessionListener;
import java.util.Date;
import static com.gmail.dangnguyendota.example.Colors.*;

public class SimpleListener implements SessionListener {
    @Override
    public void onStageConnected() {
        System.out.println(ANSI_BLUE + "stage connected" + ANSI_RESET);
    }

    @Override
    public void onStageDisconnected() {
        System.out.println(ANSI_RED + "stage disconnected" + ANSI_RESET);
    }

    @Override
    public void onStageError(Error e) {
        System.out.println(ANSI_RED + "stage error code: " + e.getCode() + ", message: " + e.getMessage() + ANSI_RESET);

    }

    @Override
    public void onHttpError(int code, String error) {
        System.out.println(ANSI_RED + "http error code: " + code + ", message: " + error + ANSI_RESET);
    }

    @Override
    public void onActorConnected() {
        System.out.println(ANSI_BLUE + "actor connected" + ANSI_RESET);
    }

    @Override
    public void onActorDisconnected() {
        System.out.println(ANSI_RED + "actor disconnected" + ANSI_RESET);
    }

    @Override
    public void onActorError(Error e) {
        System.out.println(ANSI_RED + "actor error code: " + e.getCode() + " message: " + e.getMessage() + ANSI_RESET);
    }

    @Override
    public void onException(Exception e) {
        System.out.println(ANSI_RED + "exception " + e.getMessage() + ANSI_RESET);

    }

    @Override
    public void onJoinedRoom(String serviceId, String gameId, String roomId, String address, Date createTime) {
        System.out.println(ANSI_BLUE + "joined actor service serviceId: " + serviceId + " gameId: " + gameId + " roomId: " + roomId +
                " address: " + address + " create time: " +  createTime + ANSI_RESET);
    }

    @Override
    public void playerJoined(String id, String name, String avatar) {
        System.out.println(ANSI_BLUE + "player joined room, id: " + id + ", name: " + name + ", avatar: " + avatar + ANSI_RESET);
    }

    @Override
    public void playerLeft(String id, String name, String avatar) {
        System.out.println(ANSI_BLUE + "player left room, id: " + id + ", name: " + name + ", avatar: " + avatar + ANSI_RESET);
    }

    @Override
    public void onRoomMessaged(String gameId, String roomId, byte[] data, long time) {
        System.out.println("messaged gameId: " + gameId + " roomId: " + roomId + " time: " + time);
    }

    @Override
    public void onRelayedMessaged(String gameId, String roomId, String senderId, byte[] data, long time) {

    }

    @Override
    public void onRoomClosed(String gameId, String roomId) {

    }
}
