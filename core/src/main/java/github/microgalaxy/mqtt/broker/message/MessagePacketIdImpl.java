package github.microgalaxy.mqtt.broker.message;

import github.microgalaxy.mqtt.broker.handler.MqttException;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttVersion;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

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

    //TODO 提高消息并发，不使用递归
    @Override
    public synchronized int nextMessageId(MqttVersion mqttVersion) {
        if (messageIdCatch.size() >= MAX_PACKET_ID)
            throw new MqttException(
                    mqttVersion.protocolLevel() >= MqttVersion.MQTT_5.protocolLevel() ?
                            (int) MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_BUSY.byteValue() :
                            (int) MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE.byteValue(),
                    "No message packet ids available");
        if (ObjectUtils.isEmpty(messageIdCatch.get(currentPacketId))) {
            messageIdCatch.put(currentPacketId, currentPacketId);
            return currentPacketId;
        }
        currentPacketId++;
        if (currentPacketId > MAX_PACKET_ID) currentPacketId = MIN_PACKET_ID;
        return nextMessageId(mqttVersion);
    }

    @Override
    public synchronized void releaseMessageId(int messageId) {
        messageIdCatch.remove(messageId);
    }
}
