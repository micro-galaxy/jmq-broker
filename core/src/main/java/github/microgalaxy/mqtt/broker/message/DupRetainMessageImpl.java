package github.microgalaxy.mqtt.broker.message;

import github.microgalaxy.mqtt.broker.util.TopicUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class DupRetainMessageImpl implements IDupRetainMessage {
    private final Map<String, RetainMessage> retainMessageCache = new ConcurrentHashMap<>();

    @Override
    public void put(String topic, RetainMessage retainMessage) {
        retainMessageCache.put(topic, retainMessage);
    }

    @Override
    public RetainMessage get(String topic) {
        return retainMessageCache.get(topic);
    }

    @Override
    public List<RetainMessage> match(String subscribeTopic) {
        return retainMessageCache.entrySet().stream()
                .filter(v -> TopicUtils.matchingTopic(subscribeTopic,v.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public void remove(String topic) {
        retainMessageCache.remove(topic);
    }
}
