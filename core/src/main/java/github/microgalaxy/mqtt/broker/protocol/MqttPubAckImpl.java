package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.protocol.AbstractMqttMsgProtocol;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.springframework.stereotype.Component;

/**
 * 发布回执
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttPubAckImpl<T extends MqttMessageType, M extends MqttMessage> extends AbstractMqttMsgProtocol<T, M> {

    @Override
    protected T getType() {
        return (T) T.PUBACK;
    }

    /**
     * 发布回执消息
     *
     * @param channel
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {

    }
}
