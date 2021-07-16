package github.microgalaxy.mqtt.broker.client;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.io.Serializable;

/**
 * 客户端订阅信息
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public final class Subscribe implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String clientId;

    private final String topic;

    private final MqttQoS qos;

    public Subscribe(String clientId, String topic, MqttQoS qos) {
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
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
}
