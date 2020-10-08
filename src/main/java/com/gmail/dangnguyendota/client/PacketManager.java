package com.gmail.dangnguyendota.client;

import com.google.common.util.concurrent.SettableFuture;

import java.util.HashMap;
import java.util.Map;

public class PacketManager {

    private Map<String, SettableFuture<?>> collection;

    public PacketManager() {
        collection = new HashMap<>();
    }

    public void set(String id, SettableFuture<?> future) {
        collection.put(id, future);
    }

    public SettableFuture<?> remove(String id) {
        return collection.remove(id);
    }

    public void clear() {
        collection.clear();
    }
}