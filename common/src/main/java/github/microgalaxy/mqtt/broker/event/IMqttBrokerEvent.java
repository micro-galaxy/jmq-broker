package github.microgalaxy.mqtt.broker.event;

import io.netty.handler.codec.mqtt.MqttVersion;

import java.util.Date;

/**
 * Mqtt 消息事件
 * <p>
 * 客户端连接成功、客户端断开连接、消息流入、消息流出
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface IMqttBrokerEvent {

    /**
     * 客户端连接成功事件
     *
     * @param clientId
     * @param username
     * @param keepAlive
     * @param ip
     * @param protocolVersion
     * @param connectedTime
     */
    void onConnected(String clientId, String username, int keepAlive,
                     String ip, MqttVersion protocolVersion, Date connectedTime);

    /**
     * 客户端断开连接事件
     *
     * @param clientId
     * @param username
     * @param disConnectedTime
     */
    void onDisConnected(String clientId, String username, Date disConnectedTime);

    /**
     * 客户端消息抵达事件
     *
     * @param clientId
     * @param username
     * @param topic
     * @param qos
     * @param retain
     * @param payload
     * @param arriveTime
     */
    void onMessageArrived(String clientId, String username, String topic,
                          int qos, boolean retain, byte[] payload, Date arriveTime);

    /**
     * 消息发送事件
     *
     * @param clientId
     * @param username
     * @param targetClientId
     * @param targetClientName
     * @param topic
     * @param qos
     * @param retain
     * @param payload
     * @param arriveTime
     */
    void onMessageDelivered(String clientId, String username, String topic, String targetClientId,
                            String targetClientName, int qos, boolean retain, byte[] payload, Date arriveTime);
}
