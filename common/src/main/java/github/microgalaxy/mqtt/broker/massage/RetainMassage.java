package github.microgalaxy.mqtt.broker.massage;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.io.Serializable;

/**
 * 保留消息
 * <p>
 * 服务端收到 Retain 标志为 1 的 PUBLISH 报文时，会将该报文视为保留消息，除了被正常转发以外，
 * 保留消息会被存储在服务端，每个主题下只能存在一份保留消息，因此如果已经存在相同主题的保留消息，则该保留消息被替换。
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public final class RetainMassage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String topic;

    private final MqttQoS qos;

    private final String payload;

    public RetainMassage(String topic, MqttQoS qos, String payload) {
        this.topic = topic;
        this.qos = qos;
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public MqttQoS getQos() {
        return qos;
    }

    public String getPayload() {
        return payload;
    }
}
