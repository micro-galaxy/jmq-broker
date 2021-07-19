package github.microgalaxy.mqtt.broker.message;

import java.util.List;

/**
 * 保留消息存储(Retain消息)
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface IDupRetainMessage {
    /**
     * 消息存储
     *
     * @param topic
     * @param retainMessage
     */
    void put(String topic, RetainMessage retainMessage);

    /**
     * 获取消息
     *
     * @param topic
     * @return
     */
    RetainMessage get(String topic);

    /**
     * 获取消息集合
     *
     * @param topicFilter
     * @return
     */
    List<RetainMessage> match(String topicFilter);

    /**
     * 移除指定消息
     *
     * @param topic
     */
    void remove(String topic);
}
