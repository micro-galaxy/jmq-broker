package github.microgalaxy.mqtt.broker.client;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class SessionStoreImpl implements ISessionStore {
    private final Map<String, Session> clientSessionCache = new ConcurrentHashMap<>();

    @Override
    public void put(String clientId, Session session) {
        clientSessionCache.put(clientId, session);
    }

    @Override
    public Session get(String clientId) {
        return clientSessionCache.get(clientId);
    }

    @Override
    public void remove(String clientId) {
        clientSessionCache.remove(clientId);
    }
}
