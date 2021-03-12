package github.microgalaxy.mqtt.broker.massage;

/**
 * Mqtt消息ID接口
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface IMassagePacketId {
    /**
     * 获取全局唯一消息ID
     *
     * @return
     */
    int nextMassageId();

    /**
     * 释放消息ID
     *
     * @param massageId
     */
    void releaseMassageId(int massageId);
}
