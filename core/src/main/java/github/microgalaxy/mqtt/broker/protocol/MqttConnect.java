package github.microgalaxy.mqtt.broker.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.springframework.stereotype.Component;

/**
 * 发起连接
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttConnect<T extends MqttMessageType, M extends MqttConnectMessage> extends AbstractMqttMsgProtocol<T, M> {

    /**
     * 获取消息类型
     *
     * @return
     */
    @Override
    protected T getType() {
        return (T) T.CONNECT;
    }

    /**
     * 发起连接消息
     *
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {

    }

}
