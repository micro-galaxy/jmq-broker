package github.microgalaxy.mqtt.broker.message;

import java.util.List;

/**
 * qos1,2 PUBLISH重发消息存储
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface IDupPublishMessage {
    /**
     * 消息存储
     *
     * @param clientId
     * @param dupPublishMessage
     */
    void put(String clientId, DupPublishMessage dupPublishMessage);

    /**
     * 获取消息集合
     *
     * @param clientId
     * @return
     */
    List<DupPublishMessage> get(String clientId);

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
