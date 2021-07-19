package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.client.ISessionStore;
import github.microgalaxy.mqtt.broker.client.ISubscribeStore;
import github.microgalaxy.mqtt.broker.client.Session;
import github.microgalaxy.mqtt.broker.client.Subscribe;
import github.microgalaxy.mqtt.broker.message.IMessagePacketId;
import github.microgalaxy.mqtt.broker.message.RetainMessage;
import github.microgalaxy.mqtt.broker.nettyex.MqttConnectReturnCodeEx;
import github.microgalaxy.mqtt.broker.message.IDupRetainMessage;
import github.microgalaxy.mqtt.broker.util.TopicUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * subscribe topic
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttSubscribe<T extends MessageHandleType.Subscribe, M extends MqttSubscribeMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private ISessionStore sessionServer;
    @Autowired
    private ISubscribeStore subscribeStoreServer;
    @Autowired
    private IDupRetainMessage retainMessageServer;
    @Autowired
    private IMessagePacketId messagePacketIdServer;

    /**
     * subscribe topic message
     * and sends publish message with the retain tag
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
            if (TopicUtils.validTopic(topic.topicName())) {
                if (session.getMqttProtocolVersion().protocolLevel() >= MqttVersion.MQTT_5.protocolLevel())
                    reasonCodes.add(msg.fixedHeader().qosLevel().value());
                Subscribe subscribe = new Subscribe(session.getClientId(), topic.topicName(), topic.qualityOfService());
                subscribeStoreServer.put(topic.topicName(), subscribe);
                if (log.isDebugEnabled())
                    log.debug("SUBSCRIBE - Client subscribe message arrives: clientId:{}, topic:{}, qos:{}",
                            session.getClientId(), topic.topicName(), topic.qualityOfService().value());
            } else {
                if (session.getMqttProtocolVersion().protocolLevel() < MqttVersion.MQTT_5.protocolLevel()) {
                    MqttMessage disconnectMessage = MqttMessageBuilders.disconnect()
                            .reasonCode((byte) MqttConnectReturnCodeEx.SUBSCRIBE_REFUSED_NOT_SUPPORT_TOPIC.value())
                            .build();
                    channel.writeAndFlush(disconnectMessage);
                    channel.close();
                    return;
                }
                reasonCodes.add(MqttConnectReturnCodeEx.SUBSCRIBE_REFUSED_NOT_SUPPORT_TOPIC.value());
            }
        }
        if (session.getMqttProtocolVersion().protocolLevel() >= MqttVersion.MQTT_5.protocolLevel()) {
            MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                    new MqttSubAckPayload(reasonCodes));
            channel.writeAndFlush(subAckMessage);
        }

        //send retain message
        subTopics.forEach(s -> sendRetainMessage(channel, s.topicName(), s.qualityOfService()));
    }


    private void sendRetainMessage(Channel channel, String topicName, MqttQoS qos) {
        List<RetainMessage> retainMessages = retainMessageServer.match(topicName);
        retainMessages.forEach(m -> {
            MqttQoS targetQos = MqttQoS.valueOf(Math.min(m.getQos().value(), qos.value()));
            int messageId = MqttQoS.AT_MOST_ONCE == targetQos ? 0 : messagePacketIdServer.nextMessageId();
            MqttPublishMessage publishMessage = MqttMessageBuilders.publish()
                    .topicName(m.getTopic())
                    .messageId(messageId)
                    .qos(targetQos)
                    .retained(false)
                    .payload(Unpooled.buffer().writeBytes(m.getPayload()))
                    .build();
            channel.writeAndFlush(publishMessage);
            if (log.isDebugEnabled())
                log.debug("PUBLISH - Send retain message: clientId:{}, topic:{}, qos:{}, messageId:{}",
                        channel.attr(AttributeKey.valueOf("clientId")).get(), m.getTopic(), targetQos.value(), messageId);
        });
    }
}
