package github.microgalaxy.mqtt.broker.client;

import java.io.Serializable;

/**
 * 客户端订阅信息
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
final class Subscribe implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String clientId;

    private final String topic;

    private final int qos;

    public Subscribe(String clientId, String topic, int qos) {
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

    public int getQos() {
        return qos;
    }
}
