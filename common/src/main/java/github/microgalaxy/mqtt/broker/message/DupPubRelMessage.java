package github.microgalaxy.mqtt.broker.message;

import java.io.Serializable;

/**
 * PubRel消息qos2质量保障
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public final class DupPubRelMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String clientId;

    private final int messageId;

    public DupPubRelMessage(String clientId, int messageId) {
        this.clientId = clientId;
        this.messageId = messageId;
    }

    public String getClientId() {
        return clientId;
    }

    public int getMessageId() {
        return messageId;
    }

}
