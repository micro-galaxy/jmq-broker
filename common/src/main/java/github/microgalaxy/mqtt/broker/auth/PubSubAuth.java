package github.microgalaxy.mqtt.broker.auth;

import io.netty.handler.codec.mqtt.MqttMessageType;

import java.io.Serializable;

/**
 * 订阅认证信息
 *
 * @author Microgalaxy （https://github.com/micro-galaxy）
 */
public final class PubSubAuth implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MqttMessageType type;
    private final String clientId;
    private final String ipAddress;
    private final String protocols;
    private final String topic;

    public PubSubAuth(MqttMessageType type, String clientId, String ipAddress, String protocols, String topic) {
        this.type = type;
        this.clientId = clientId;
        this.ipAddress = ipAddress;
        this.protocols = protocols;
        this.topic = topic;
    }

    public MqttMessageType getType() {
        return type;
    }

    public String getClientId() {
        return clientId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getProtocols() {
        return protocols;
    }

    public String getTopic() {
        return topic;
    }
}
