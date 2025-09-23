package org.calendar.context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventContext {
    private final Map<String, Map<String, Object>> context;

    public EventContext() {
        this.context = new ConcurrentHashMap<>();
    }

    public void put(String key, String eventId, Object value) throws Exception {
        if (this.eventContextExist(key, eventId)) throw new Exception("Un context existe déjà");
        this.context.computeIfAbsent(key, k -> new HashMap<>()).put(eventId, value);
    }

    public Object get(String key, String eventId) throws Exception {
        if (!this.eventContextExist(key, eventId)) throw new Exception("Le context n'existe pas");
        return this.context.get(key).get(eventId);
    }

    public void clear(String key, String eventId) throws Exception {
        if (!this.eventContextExist(key, eventId)) throw new Exception("Le context n'existe pas");
        this.context.get(key).remove(eventId);
    }

    public boolean eventContextExist(String key, String eventId) {
        return this.context.containsKey(key) && this.context.get(key).containsKey(eventId);
    }
}
