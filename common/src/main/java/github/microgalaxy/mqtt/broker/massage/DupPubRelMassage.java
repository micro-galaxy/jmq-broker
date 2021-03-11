package github.microgalaxy.mqtt.broker.massage;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.io.Serializable;

/**
 * PubRel消息qos2质量保障
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public final class DupPubRelMassage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String clientId;

    private final String topic;

    private final MqttQoS qos;

    private final int massageId;

    private final byte[] massagePayload;

    public DupPubRelMassage(String clientId, String topic, MqttQoS qos, int massageId, byte[] massagePayload) {
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
        this.massageId = massageId;
        this.massagePayload = massagePayload;
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

    public int getMassageId() {
        return massageId;
    }

    public byte[] getMassagePayload() {
        return massagePayload;
    }
}
