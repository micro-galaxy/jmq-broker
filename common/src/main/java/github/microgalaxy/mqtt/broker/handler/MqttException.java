package github.microgalaxy.mqtt.broker.handler;

/**
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public class MqttException extends RuntimeException {
    private Integer reasonCode;
    private boolean disConnect;

    public MqttException(String message, Throwable cause) {
        super(message, cause);
    }

    public MqttException(Integer reasonCode, String message) {
        super(message, null);
        this.reasonCode = reasonCode;
    }

    public MqttException(Integer reasonCode, boolean disConnect, String message, Throwable cause) {
        super(message, cause);
        this.reasonCode = reasonCode;
        this.disConnect = disConnect;
    }

    public MqttException(Integer reasonCode, boolean disConnect, String message) {
        super(message, null);
        this.reasonCode = reasonCode;
        this.disConnect = disConnect;
    }

    public int getReasonCode() {
        return reasonCode;
    }

    public boolean isDisConnect() {
        return disConnect;
    }
}
