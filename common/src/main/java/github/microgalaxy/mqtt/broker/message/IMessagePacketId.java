package github.microgalaxy.mqtt.broker.message;

import io.netty.handler.codec.mqtt.MqttVersion;

/**
 * Mqtt消息ID接口
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface IMessagePacketId {
    /**
     * 获取全局唯一消息ID
     *
     * @param mqttVersion
     * @return
     */
    int nextMessageId(MqttVersion mqttVersion);

    /**
     * 释放消息ID
     *
     * @param messageId
     */
    void releaseMessageId(int messageId);
}
