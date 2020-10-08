package com.gmail.dangnguyendota.example;

import com.gmail.dangnguyendota.client.Client;
import com.gmail.dangnguyendota.client.Config;
import com.gmail.dangnguyendota.client.Session;
import com.gmail.dangnguyendota.client.SessionListener;

import java.util.Date;
import java.util.Scanner;
import java.util.UUID;

public class ExampleClient {
    private Scanner scanner;
    private String username, password;
    private Session session;
    private String actorId, actorAddr, gameId, roomId;

    public ExampleClient(Config config) {
        scanner = new Scanner(System.in);

        session = Client.createSession(config);
        SessionListener listener = new SimpleListener() {
            private UUID serviceId;
            private String gameId;
            @Override
            public void onJoinedRoom(String serviceId, String gameId, String roomId, String address, Date createTime) {
                super.onJoinedRoom(serviceId, gameId, roomId, address, createTime);
                ExampleClient.this.actorId = serviceId;
                ExampleClient.this.roomId = roomId;
                ExampleClient.this.gameId = gameId;
                ExampleClient.this.actorAddr = address;
            }
        };
        session.setListener(listener);
    }

    public void start() {
        Loop:
        while (true) {
            try {
                System.out.println("ACTIONS");
                System.out.println("1. login");
                System.out.println("2. search room");
                System.out.println("3. join actor");
                System.out.println("4. send data");
                System.out.print("choose action: ");
                String chosen = scanner.nextLine();
                switch (chosen) {
                    case "1":
                        login();
                        break;
                    case "2":
                        search();
                        break;
                    case "3":
                        join();
                        break;
                    case "4":
                        sendData();
                        break;
                    default:
                        break Loop;
                }
            } catch (Exception e) {
                System.out.print(e.getMessage());
            }
        }
    }

    public void login() throws Exception {
        System.out.print("username? ");
        username = scanner.nextLine();
        System.out.print("password? ");
        password = scanner.nextLine();
        session.login(username, password).get();
        session.connectStage().get();
    }

    public void search() throws Exception {
        System.out.print("search for game id? ");
        String gameId = scanner.nextLine();
        session.searchRoom(gameId).get();
    }

    public void join() throws Exception {
        session.connectActor(actorAddr, gameId, roomId).get();
    }

    public void sendData() {

    }
}
