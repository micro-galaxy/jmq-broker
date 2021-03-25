package github.microgalaxy.mqtt.broker.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import org.springframework.stereotype.Component;

/**
 * 订阅主题
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttSubscribe<T extends MqttMessageType, M extends MqttSubscribeMessage> extends AbstractMqttMsgProtocol<T, M> {

    @Override
    protected T getType() {
        return (T) T.SUBSCRIBE;
    }

    /**
     * 订阅主题消息
     *
     * @param channel
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {

    }
}
