package github.microgalaxy.mqtt.broker.massage;


import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * ignite内部消息
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public final class InternalMassage implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nullable
    private String clientId;

    private String topic;

    private int qos;

    private byte[] payLoad;

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

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public byte[] getPayLoad() {
        return payLoad;
    }

    public void setPayLoad(byte[] payLoad) {
        this.payLoad = payLoad;
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
