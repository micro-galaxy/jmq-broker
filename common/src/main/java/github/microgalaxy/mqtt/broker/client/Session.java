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
final class Session implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String clientId;
    private final Channel channel;
    private final boolean cleanSession;
    private final MqttPublishMessage willMassage;
    private final MqttVersion mqttProtocolVersion;

    public Session(String clientId, Channel channel, boolean cleanSession, MqttPublishMessage willMassage, MqttVersion mqttProtocolVersion) {
        this.clientId = clientId;
        this.channel = channel;
        this.cleanSession = cleanSession;
        this.willMassage = willMassage;
        this.mqttProtocolVersion = mqttProtocolVersion;
    }

    public String getClientId() {
        return clientId;
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

    public MqttVersion getMqttProtocolVersion() {
        return mqttProtocolVersion;
    }
}
