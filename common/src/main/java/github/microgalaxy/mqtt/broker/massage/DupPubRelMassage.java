package github.microgalaxy.mqtt.broker.massage;

import java.io.Serializable;

/**
 * PubRel消息qos2质量保障
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public final class DupPubRelMassage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String clientId;

    private final int massageId;

    public DupPubRelMassage(String clientId, int massageId) {
        this.clientId = clientId;
        this.massageId = massageId;
    }

    public String getClientId() {
        return clientId;
    }

    public int getMassageId() {
        return massageId;
    }

}
