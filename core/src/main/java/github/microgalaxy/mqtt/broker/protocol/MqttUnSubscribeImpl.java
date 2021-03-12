package github.microgalaxy.mqtt.broker.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import org.springframework.stereotype.Component;

/**
 * 取消订阅
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttUnSubscribeImpl<T extends MqttMessageType, M extends MqttUnsubscribeMessage> extends AbstractMqttMsgProtocol<T, M> {

    @Override
    protected T getType() {
        return (T) T.UNSUBSCRIBE;
    }

    /**
     * 取消订阅消息
     *
     * @param channel
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {

    }
}
