package dev.mcmt.core.comms;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public final class MessageBus {
    private final Map<String, ConcurrentLinkedQueue<Object>> channels = new ConcurrentHashMap<>();

    public void send(String channel, Object message) {
        channels.computeIfAbsent(channel, c -> new ConcurrentLinkedQueue<>()).add(message);
    }

    public List<Object> drain(String channel) {
        var q = channels.get(channel);
        if (q == null) return List.of();
        Object m;
        var out = new java.util.ArrayList<Object>(q.size());
        while ((m = q.poll()) != null) out.add(m);
        return out;
    }

    public Map<String, List<Object>> drainAll() {
        return channels.entrySet().stream().collect(Collectors.toUnmodifiableMap(
            Map.Entry::getKey,
            e -> drain(e.getKey())
        ));
    }
}
