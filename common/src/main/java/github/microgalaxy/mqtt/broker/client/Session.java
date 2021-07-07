package github.microgalaxy.mqtt.broker.client;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttVersion;

import java.io.Serializable;

/**
 * mqtt客户端Session
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public final class Session implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String clientId;
    private final String username;
    private final Channel channel;
    private final boolean cleanSession;
    private final MqttPublishMessage willMassage;
    private final int mqttProtocolVersion;

    public Session(String clientId, String username, Channel channel, boolean cleanSession, MqttPublishMessage willMassage, int mqttProtocolVersion) {
        this.clientId = clientId;
        this.username = username;
        this.channel = channel;
        this.cleanSession = cleanSession;
        this.willMassage = willMassage;
        this.mqttProtocolVersion = mqttProtocolVersion;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username;
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public MqttPublishMessage getWillMassage() {
        return willMassage;
    }

    public int getMqttProtocolVersion() {
        return mqttProtocolVersion;
    }
}
