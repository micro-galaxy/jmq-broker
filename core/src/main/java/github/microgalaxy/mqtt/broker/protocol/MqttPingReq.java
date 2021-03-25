package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.protocol.AbstractMqttMsgProtocol;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.springframework.stereotype.Component;

/**
 * PING请求
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttPingReq<T extends MqttMessageType, M extends MqttMessage> extends AbstractMqttMsgProtocol<T, M> {

    /**
     * 获取消息类型
     *
     * @return
     */
    @Override
    protected T getType() {
        return (T) T.PINGREQ;
    }

    /**
     * PING请求消息
     *
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {

    }

}