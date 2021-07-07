package github.microgalaxy.mqtt.broker.client;

/**
 * 客户端session服务接口
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface ISessionStore {
    /**
     * 存储session
     *
     * @param clientId
     * @param session
     */
    void put(String clientId, Session session);

    /**
     * 获取session
     *
     * @param clientId
     * @return
     */
    Session get(String clientId);

    /**
     * 移除session
     *
     * @param clientId
     */
    void remove(String clientId);
}
