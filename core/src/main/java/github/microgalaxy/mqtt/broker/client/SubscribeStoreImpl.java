package github.microgalaxy.mqtt.broker.client;

import github.microgalaxy.mqtt.broker.util.TopicUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class SubscribeStoreImpl implements ISubscribeStore {
    private final Map<String, Map<String, Subscribe>> clientSubscribeCatch = new ConcurrentHashMap<>();

    @Override
    public void put(String topic, Subscribe subscribe) {
        Map<String, Subscribe> subscribeMap = clientSubscribeCatch.containsKey(topic) ?
                clientSubscribeCatch.get(topic) : new ConcurrentHashMap<>();
        subscribeMap.put(subscribe.getClientId(), subscribe);
        clientSubscribeCatch.put(topic, subscribeMap);
    }

    @Override
    public void remove(String topic, String clientId) {
        if (!clientSubscribeCatch.containsKey(topic)) return;
        Map<String, Subscribe> subscribeMap = clientSubscribeCatch.get(topic);
        if (!subscribeMap.containsKey(clientId)) return;
        subscribeMap.remove(clientId);
        if (CollectionUtils.isEmpty(subscribeMap)) {
            clientSubscribeCatch.remove(topic);
        } else {
            clientSubscribeCatch.put(topic, subscribeMap);
        }
    }

    @Override
    public void removeClient(String clientId) {
        clientSubscribeCatch.forEach((key, subscribeMap) -> {
            if (!subscribeMap.containsKey(clientId)) return;
            subscribeMap.remove(clientId);
            if (CollectionUtils.isEmpty(subscribeMap)) {
                subscribeMap.remove(key);
            } else {
                clientSubscribeCatch.put(key, subscribeMap);
            }
        });
    }

    @Override
    public List<Subscribe> matchTopic(String topicFilter) {
        return clientSubscribeCatch.entrySet().stream()
                .filter(v -> TopicUtils.matchingTopic(topicFilter,v.getKey()))
                .map(v -> v.getValue().values())
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    }
}
