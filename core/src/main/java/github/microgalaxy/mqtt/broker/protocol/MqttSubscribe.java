package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.client.ISessionStore;
import github.microgalaxy.mqtt.broker.client.ISubscribeStore;
import github.microgalaxy.mqtt.broker.client.Session;
import github.microgalaxy.mqtt.broker.client.Subscribe;
import github.microgalaxy.mqtt.broker.config.BrokerConstant;
import github.microgalaxy.mqtt.broker.massage.IMassagePacketId;
import github.microgalaxy.mqtt.broker.massage.RetainMassage;
import github.microgalaxy.mqtt.broker.nettyex.MqttConnectReturnCodeEx;
import github.microgalaxy.mqtt.broker.store.IDupRetainMassage;
import github.microgalaxy.mqtt.broker.util.TopicUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * subscribe topic
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttSubscribe<T extends MqttMessageType, M extends MqttSubscribeMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private ISessionStore sessionServer;
    @Autowired
    private ISubscribeStore subscribeStoreServer;
    @Autowired
    private IDupRetainMassage retainMassageServer;
    @Autowired
    private IMassagePacketId massagePacketIdServer;

    @Override
    protected T getType() {
        return (T) T.SUBSCRIBE;
    }

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
                            session.getClientId(), topic.topicName(), topic.qualityOfService());
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
        List<RetainMassage> retainMassages = retainMassageServer.match(topicName);
        retainMassages.forEach(m -> {
            MqttQoS targetQos = MqttQoS.valueOf(Math.min(m.getQos().value(), qos.value()));
            int messageId = MqttQoS.AT_MOST_ONCE == targetQos ? 0 : massagePacketIdServer.nextMassageId();
            MqttPublishMessage publishMessage = MqttMessageBuilders.publish()
                    .topicName(m.getTopic())
                    .messageId(messageId)
                    .qos(targetQos)
                    .retained(false)
                    .payload(m.getPayload())
                    .build();
            channel.writeAndFlush(publishMessage);
            if (log.isDebugEnabled())
                log.debug("PUBLISH - Send retain message: clientId:{}, topic:{}, qos:{}, messageId:{}",
                        channel.attr(AttributeKey.valueOf("clientId")).get(), m.getTopic(), targetQos, messageId);
        });
    }
}
