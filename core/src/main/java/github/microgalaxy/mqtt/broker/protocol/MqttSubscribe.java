package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.client.ISessionStore;
import github.microgalaxy.mqtt.broker.client.Session;
import github.microgalaxy.mqtt.broker.nettyex.MqttConnectReturnCodeEx;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 订阅主题
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttSubscribe<T extends MqttMessageType, M extends MqttSubscribeMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private ISessionStore sessionServer;

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
        Session session = sessionServer.get((String) channel.attr(AttributeKey.valueOf("clientId")).get());
        List<MqttTopicSubscription> subTopics = msg.payload().topicSubscriptions();
        List<Integer> reasonCodes = new ArrayList<>(subTopics.size());
        for (MqttTopicSubscription topic : subTopics) {
            if (!validTopic(topic)) continue;
            if (MqttVersion.MQTT_5 == session.getMqttProtocolVersion()) {
                reasonCodes.add(Integer.parseInt(MqttConnectReturnCodeEx.SUBSCRIBE_REFUSED_NOT_SUPPORT_TOPIC.byteValue() + "", 16));
            } else {
                MqttMessage disconnectMessage = MqttMessageBuilders.disconnect()
                        .reasonCode(MqttConnectReturnCodeEx.SUBSCRIBE_REFUSED_NOT_SUPPORT_TOPIC.byteValue())
                        .build();
                channel.writeAndFlush(disconnectMessage);
                channel.close();
                break;
            }
        }
        if (MqttVersion.MQTT_5 == session.getMqttProtocolVersion() && !CollectionUtils.isEmpty(reasonCodes)) {
            MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                    new MqttSubAckPayload(reasonCodes));
            channel.writeAndFlush(subAckMessage);
        }
    }

    private boolean validTopic(MqttTopicSubscription topic) {
        topic.topicName();
    }
}
