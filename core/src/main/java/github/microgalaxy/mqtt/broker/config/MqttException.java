package github.microgalaxy.mqtt.broker.config;

import io.netty.handler.codec.mqtt.MqttMessageType;

/**
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public class MqttException extends RuntimeException {
    private Integer reasonCode;
    private Boolean disConnect;
    private MqttMessageType mqttMessageType;

    public MqttException(String message, Throwable cause) {
        super(message, cause);
    }

    public MqttException(Integer reasonCode, String message) {
        super(message, null);
        this.reasonCode = reasonCode;
    }

    public MqttException(Integer reasonCode, Boolean disConnect, String message, Throwable cause) {
        super(message, cause);
        this.reasonCode = reasonCode;
        this.disConnect = disConnect;
    }

    public MqttException(Integer reasonCode, Boolean disConnect, MqttMessageType mqttMessageType, String message, Throwable cause) {
        super(message, cause);
        this.reasonCode = reasonCode;
        this.disConnect = disConnect;
        this.mqttMessageType = mqttMessageType;
    }

    public int getReasonCode() {
        return reasonCode;
    }

    public boolean isDisConnect() {
        return disConnect;
    }

    public MqttMessageType getMqttMessageType() {
        return mqttMessageType;
    }
}
