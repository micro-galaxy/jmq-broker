package github.microgalaxy.mqtt.broker.store;

import github.microgalaxy.mqtt.broker.massage.DupPubRelMassage;
import github.microgalaxy.mqtt.broker.massage.DupPublishMassage;

import java.util.List;

/**
 * qos2 PUBREL重发消息存储
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface IDupPubRelMassage {
    /**
     * 消息存储
     *
     * @param clientId
     * @param dupPubRelMassage
     */
    void put(String clientId, DupPubRelMassage dupPubRelMassage);

    /**
     * 获取消息集合
     *
     * @param clientId
     * @return
     */
    List<DupPubRelMassage> get(String clientId);

    /**
     * 移除指定消息
     *
     * @param clientId
     * @param massageId
     */
    void remove(String clientId, int massageId);

    /**
     * 移除整个客户端消息
     *
     * @param clientId
     */
    void removeClient(String clientId);
}
