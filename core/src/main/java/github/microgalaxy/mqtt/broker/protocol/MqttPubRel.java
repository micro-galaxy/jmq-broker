package github.microgalaxy.mqtt.broker.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Component;

/**
 * QoS2消息释放
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttPubRel<T extends MqttMessageType, M extends MqttMessage> extends AbstractMqttMsgProtocol<T, M> {

    /**
     * QoS2消息释放消息
     *
     * @param channel
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        int messageId = ((MqttMessageIdVariableHeader) msg.variableHeader()).messageId();
        MqttMessage pubCompMqttMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        log.debug("<== PUBREL - PubRel request arrives: clientId:{}, messageId:{}",clientId, messageId);
        channel.writeAndFlush(pubCompMqttMessage);
    }

    @Override
    public MqttMessageType getHandleType() {
        return T.PUBREL;
    }
}
