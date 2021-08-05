package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.handler.MqttException;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
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
public class MqttMsgProtocolFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttMsgProtocolFactory.class);
    private static final Map<MqttMessageType, AbstractMqttMsgProtocol> MQTT_MSG_PROTOCOL_POOL = new ConcurrentHashMap();

    private MqttMsgProtocolFactory() {
    }

    static void registerMsgHandle(MqttMessageType type, AbstractMqttMsgProtocol process) {
        MQTT_MSG_PROTOCOL_POOL.put(type, process);
    }

    public static void processMsg(Channel channel, MqttMessage msg) {
        AbstractMqttMsgProtocol process = MQTT_MSG_PROTOCOL_POOL.get(msg.fixedHeader().messageType());
        if (!ObjectUtils.isEmpty(process)) {
            try {
                process.onMqttMsg(channel, process.getMessageType().cast(msg));
            } catch (MqttException e) {
                LOGGER.info("The Mqtt message handler error, message:{}", msg.toString(), e);
                process.onHandlerError(channel, process.getMessageType().cast(msg), e);
            } catch (Exception e) {
                LOGGER.error("The Mqtt message handler error, message:{}", msg.toString(), e);
                MqttMessage disconnectMessage = MqttMessageBuilders.disconnect()
                        .reasonCode(MqttConnectReturnCode.CONNECTION_REFUSED_MALFORMED_PACKET.byteValue())
                        .build();
                channel.writeAndFlush(disconnectMessage);
                channel.close();
            }

        } else {
            LOGGER.warn("The Mqtt message handler not implemented, message:{}", msg.toString());
        }
    }

    public static <T> T getHandler(MqttMessageType type, Class<T> clazz) {
        AbstractMqttMsgProtocol msgHandler = MQTT_MSG_PROTOCOL_POOL.get(type);
        if(ObjectUtils.isEmpty(msgHandler))
            throw new IllegalArgumentException("No implement of the target type found");
        return clazz.cast(MQTT_MSG_PROTOCOL_POOL.get(type));
    }
}
