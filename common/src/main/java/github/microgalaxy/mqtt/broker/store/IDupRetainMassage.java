package github.microgalaxy.mqtt.broker.store;

import github.microgalaxy.mqtt.broker.massage.RetainMassage;

import java.util.List;

/**
 * 保留消息存储(Retain消息)
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface IDupRetainMassage {
    /**
     * 消息存储
     *
     * @param topic
     * @param retainMassage
     */
    void put(String topic, RetainMassage retainMassage);

    /**
     * 获取消息
     *
     * @param topic
     * @return
     */
    RetainMassage get(String topic);

    /**
     * 获取消息集合
     *
     * @param topicFilter
     * @return
     */
    List<RetainMassage> match(String topicFilter);

    /**
     * 移除指定消息
     *
     * @param topic
     */
    void remove(String topic);
}
