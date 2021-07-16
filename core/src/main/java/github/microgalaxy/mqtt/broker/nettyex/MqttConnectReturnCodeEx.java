package github.microgalaxy.mqtt.broker.nettyex;

/**
 * mqtt v5 原因代码扩展（遵循mqtt v5 标准）
 *
 * @author Microgalaxy
 */
public enum MqttConnectReturnCodeEx {
    /**
     * 0xA1
     * 不支持订阅标识符,服务器不支持订阅标识符；不接受订阅。
     */
    SUBSCRIBE_REFUSED_NOT_SUPPORT_TOPIC(0xA1);

    private final int value;
    MqttConnectReturnCodeEx(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
