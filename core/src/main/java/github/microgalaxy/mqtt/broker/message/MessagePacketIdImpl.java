package github.microgalaxy.mqtt.broker.message;

import io.netty.handler.codec.mqtt.MqttVersion;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MessagePacketIdImpl implements IMessagePacketId {
    private final int MIN_PACKET_ID = 1;
    private final int MAX_PACKET_ID = 1 << 16 - 1;
    private final Map<Integer, Integer> messageIdCatch = new ConcurrentHashMap<>(MAX_PACKET_ID);
    private int currentPacketId = MIN_PACKET_ID;

    @Override
    public synchronized int nextMessageId(MqttVersion mqttVersion) {
        for (; ; ) {
            if (!messageIdCatch.containsKey(currentPacketId)) {
                messageIdCatch.put(currentPacketId, currentPacketId);
                return currentPacketId;
            }
            currentPacketId++;
            if (currentPacketId > MAX_PACKET_ID) currentPacketId = MIN_PACKET_ID;
        }
    }

    @Override
    public synchronized void releaseMessageId(int messageId) {
        messageIdCatch.remove(messageId);
    }
}
