package github.microgalaxy.mqtt.broker.message;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.io.Serializable;

/**
 * publish消息qos1，2质量保障
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public final class DupPublishMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String clientId;

    private final String topic;

    private final MqttQoS qos;

    private final int messageId;

    private final byte[] payload;

    public DupPublishMessage(String clientId, String topic, MqttQoS qos, int messageId, byte[] payload) {
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
        this.messageId = messageId;
        this.payload = payload;
    }

    public String getClientId() {
        return clientId;
    }

    public String getTopic() {
        return topic;
    }

    public MqttQoS getQos() {
        return qos;
    }

    public int getMessageId() {
        return messageId;
    }

    public byte[] getPayload() {
        return payload;
    }
}
