package github.microgalaxy.mqtt.broker.message;

/**
 * Mqtt消息ID接口
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface IMessagePacketId {
    /**
     * 获取全局唯一消息ID
     *
     * @return
     */
    int nextMessageId();

    /**
     * 释放消息ID
     *
     * @param messageId
     */
    void releaseMessageId(int messageId);
}
