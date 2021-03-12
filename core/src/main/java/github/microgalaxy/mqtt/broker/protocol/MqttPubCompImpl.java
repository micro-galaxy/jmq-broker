package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.protocol.AbstractMqttMsgProtocol;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.springframework.stereotype.Component;

/**
 * QoS2消息完成
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttPubCompImpl<T extends MqttMessageType, M extends MqttMessage> extends AbstractMqttMsgProtocol<T, M> {

    @Override
    protected T getType() {
        return (T) T.PUBCOMP;
    }

    /**
     * QoS2消息完成消息
     *
     * @param channel
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {

    }
}
