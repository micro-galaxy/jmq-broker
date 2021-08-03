package github.microgalaxy.mqtt.broker.client;

import github.microgalaxy.mqtt.broker.config.BrokerConstant;
import github.microgalaxy.mqtt.broker.util.TopicUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class SubscribeStoreImpl implements ISubscribeStore {
    private final Map<String, Map<String, Subscribe>> clientSubscribeCatch = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Subscribe>> clientShareSubscribeCatch = new ConcurrentHashMap<>();

    @Override
    public void put(String topic, Subscribe subscribe) {
        Map<String, Map<String, Subscribe>> topicSubscribeMap =
                topic.startsWith(BrokerConstant.ShareSubscribe.SUBSCRIBE_SHARE_PREFIX) ?
                        clientShareSubscribeCatch : clientSubscribeCatch;
        Map<String, Subscribe> subscribeMap = topicSubscribeMap.containsKey(topic) ?
                topicSubscribeMap.get(topic) : new ConcurrentHashMap<>();
        subscribeMap.put(subscribe.getClientId(), subscribe);
        topicSubscribeMap.put(topic, subscribeMap);
    }

    @Override
    public void remove(String topic, String clientId) {
        Map<String, Map<String, Subscribe>> topicSubscribeMap =
                topic.startsWith(BrokerConstant.ShareSubscribe.SUBSCRIBE_SHARE_PREFIX) ?
                        clientShareSubscribeCatch : clientSubscribeCatch;
        if (!topicSubscribeMap.containsKey(topic)) return;
        Map<String, Subscribe> subscribeMap = topicSubscribeMap.get(topic);
        if (!subscribeMap.containsKey(clientId)) return;
        subscribeMap.remove(clientId);
        if (CollectionUtils.isEmpty(subscribeMap)) {
            topicSubscribeMap.remove(topic);
        } else {
            topicSubscribeMap.put(topic, subscribeMap);
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
        clientShareSubscribeCatch.forEach((key, subscribeMap) -> {
            if (!subscribeMap.containsKey(clientId)) return;
            subscribeMap.remove(clientId);
            if (CollectionUtils.isEmpty(subscribeMap)) {
                subscribeMap.remove(key);
            } else {
                clientShareSubscribeCatch.put(key, subscribeMap);
            }
        });
    }

    @Override
    public Collection<Subscribe> matchTopic(String publishTopic) {
        return clientSubscribeCatch.entrySet().stream()
                .filter(v -> TopicUtils.matchingTopic(v.getKey(), publishTopic))
                .map(v -> v.getValue().values())
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    }

    @Override
    public Collection<Subscribe> matchShareTopic(String publishTopic) {
        return clientShareSubscribeCatch.entrySet().stream()
                .filter(v -> TopicUtils.matchingShareTopic(v.getKey(), publishTopic))
                .map(v -> v.getValue().values())
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    }

    @Override
    public void upNode(String clientId, String brokerId) {
        clientSubscribeCatch.forEach((key, value) -> {
            Subscribe subscribe = value.get(clientId);
            if (!ObjectUtils.isEmpty(subscribe)) subscribe.setJmqId(brokerId);
        });
        clientShareSubscribeCatch.forEach((key, value) -> {
            Subscribe subscribe = value.get(clientId);
            if (!ObjectUtils.isEmpty(subscribe)) subscribe.setJmqId(brokerId);
        });
    }

    @Override
    public boolean repeatSubscribe(String clientId, String topic) {
        Object subscribe = Optional.ofNullable(clientSubscribeCatch.get(topic)).orElse(Collections.EMPTY_MAP).get(clientId);
        Object shareSubscribe = Optional.ofNullable(clientShareSubscribeCatch.get(topic)).orElse(Collections.EMPTY_MAP).get(clientId);
        return !ObjectUtils.isEmpty(subscribe) || !ObjectUtils.isEmpty(shareSubscribe);
    }
}
