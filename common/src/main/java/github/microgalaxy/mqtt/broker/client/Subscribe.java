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

    private String jmqId;

    private final Long subTimestamp;

    public Subscribe(String clientId, String topic, MqttQoS qos, String jmqId,Long subTimestamp) {
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
        this.jmqId = jmqId;
        this.subTimestamp = subTimestamp;
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

    public String getJmqId() {
        return jmqId;
    }

    public void setJmqId(String jmqId) {
        this.jmqId = jmqId;
    }

    public Long getSubTimestamp() {
        return subTimestamp;
    }
}
