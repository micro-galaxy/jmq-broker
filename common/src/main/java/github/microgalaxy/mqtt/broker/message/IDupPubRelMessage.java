package github.microgalaxy.mqtt.broker.message;

import java.util.List;

/**
 * qos2 PUBREL重发消息存储
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface IDupPubRelMessage {
    /**
     * 消息存储
     *
     * @param clientId
     * @param dupPubRelMessage
     */
    void put(String clientId, DupPubRelMessage dupPubRelMessage);

    /**
     * 获取消息集合
     *
     * @param clientId
     * @return
     */
    List<DupPubRelMessage> get(String clientId);

    /**
     * 移除指定消息
     *
     * @param clientId
     * @param messageId
     */
    void remove(String clientId, int messageId);

    /**
     * 移除整个客户端消息
     *
     * @param clientId
     */
    void removeClient(String clientId);
}
