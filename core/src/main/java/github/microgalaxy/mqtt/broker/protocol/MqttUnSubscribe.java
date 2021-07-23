package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.client.ISubscribeStore;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * unsubscribe
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttUnSubscribe<T extends MqttMessageType, M extends MqttUnsubscribeMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private ISubscribeStore subscribeStoreServer;

    /**
     * unsubscribe message
     *
     * @param channel
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {
        List<String> topics = msg.payload().topics();
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        topics.forEach(t -> {
            subscribeStoreServer.remove(t, clientId);
            if (log.isDebugEnabled())
                log.debug("UNSUBSCRIBE - Client unsubscribe message arrives: clientId:{}, topic:{}", clientId, t);
        });
        MqttUnsubAckMessage unsubAckMessage = MqttMessageBuilders.unsubAck()
                .packetId(msg.variableHeader().messageId())
                .build();
        channel.writeAndFlush(unsubAckMessage);
    }

    @Override
    public MqttMessageType getHandleType() {
        return T.UNSUBSCRIBE;
    }
}
