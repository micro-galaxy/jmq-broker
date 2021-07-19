package github.microgalaxy.mqtt.broker.internal;


import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * ignite内部消息
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public final class InternalMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nullable
    private String clientId;

    private String topic;

    private MqttQoS qos;

    private byte[] payload;

    private boolean retain;

    private boolean dup;

    @Nullable
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public MqttQoS getQos() {
        return qos;
    }

    public void setQos(MqttQoS qos) {
        this.qos = qos;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public boolean isDup() {
        return dup;
    }

    public void setDup(boolean dup) {
        this.dup = dup;
    }
}
