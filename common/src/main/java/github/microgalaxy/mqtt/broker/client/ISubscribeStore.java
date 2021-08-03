package github.microgalaxy.mqtt.broker.client;

import java.util.Collection;

/**
 * 客户端订阅服务接口
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface ISubscribeStore {
    /**
     * 订阅客户端存储
     *
     * @param topic
     * @param subscribe
     */
    void put(String topic, Subscribe subscribe);

    /**
     * 取消订阅
     *
     * @param topic
     * @param clientId
     */
    void remove(String topic, String clientId);

    /**
     * 取消client订阅
     *
     * @param clientId
     */
    void removeClient(String clientId);

    /**
     * topic匹配订阅客户端
     *
     * @param publishTopic
     * @return
     */
    Collection<Subscribe> matchTopic(String publishTopic);

    /**
     * 共享topic匹配订阅客户端
     *
     * @param publishTopic
     * @return
     */
    Collection<Subscribe> matchShareTopic(String publishTopic);

    /**
     * 更新client订阅所在的节点
     *
     * @param clientId
     * @param brokerId
     */
    void upNode(String clientId, String brokerId);


    /**
     * 客户端订阅已存在
     *
     * @param clientId
     * @param topic
     * @return
     */
    boolean repeatSubscribe(String clientId, String topic);
}
