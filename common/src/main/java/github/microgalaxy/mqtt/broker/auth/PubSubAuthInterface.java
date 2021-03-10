package github.microgalaxy.mqtt.broker.auth;

/**
 * mqtt客户端发布、订阅认证服务接口
 *
 * @author Microgalaxy （https://github.com/micro-galaxy）
 */
@FunctionalInterface
public interface PubSubAuthInterface {

    /**
     * 发布、订阅认证
     *
     * @param pubSubAuth
     * @return
     */
    boolean pubSubAuth(PubSubAuth pubSubAuth);
}
