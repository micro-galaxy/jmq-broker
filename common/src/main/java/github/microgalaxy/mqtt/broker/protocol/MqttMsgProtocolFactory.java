package github.microgalaxy.mqtt.broker.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mqtt消息协议工厂
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public  class MqttMsgProtocolFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttMsgProtocolFactory.class);
    private static final Map<MqttMessageType, IMqttMsgProtocol> MQTT_MSG_PROTOCOL_POOL = new ConcurrentHashMap();

    private MqttMsgProtocolFactory() {
    }

    static void registerMsgHandle(MqttMessageType type, IMqttMsgProtocol process) {
        MQTT_MSG_PROTOCOL_POOL.put(type, process);
    }

    public static void processMsg(Channel channel, MqttMessage msg) {
        IMqttMsgProtocol process = MQTT_MSG_PROTOCOL_POOL.get(msg.fixedHeader().messageType());
        if (!ObjectUtils.isEmpty(process)) {
            process.onMqttMsg(channel, msg);
        }
    }
}
