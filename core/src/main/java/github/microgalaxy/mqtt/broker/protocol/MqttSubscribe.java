package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.client.ISessionStore;
import github.microgalaxy.mqtt.broker.client.ISubscribeStore;
import github.microgalaxy.mqtt.broker.client.Session;
import github.microgalaxy.mqtt.broker.client.Subscribe;
import github.microgalaxy.mqtt.broker.config.BrokerProperties;
import github.microgalaxy.mqtt.broker.handler.MqttException;
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
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * subscribe topic
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttSubscribe<T extends MqttMessageType, M extends MqttSubscribeMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private BrokerProperties brokerProperties;
    @Autowired
    private ISessionStore sessionServer;
    @Autowired
    private ISubscribeStore subscribeStoreServer;
    @Autowired
    private IDupRetainMessage dupRetainMessageServer;
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
        List<Integer> reasonCodes = subTopics.stream()
                .filter(t -> !TopicUtils.validTopic(t.topicName()))
                .map(t -> MqttConnectReturnCodeEx.SUBSCRIBE_REFUSED_NOT_SUPPORT_TOPIC.value())
                .collect(Collectors.toList());
        MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                new MqttSubAckPayload(reasonCodes));
        channel.writeAndFlush(subAckMessage);
        if (!CollectionUtils.isEmpty(reasonCodes)) return;

        //store subscribe
        long now = System.currentTimeMillis();
        subTopics.forEach(t -> {
            Subscribe subscribe = new Subscribe(session.getClientId(), t.topicName(),
                    t.qualityOfService(),brokerProperties.getBrokerId(),now);
            if(subscribeStoreServer.repeatSubscribe(subscribe.getClientId(),subscribe.getTopic()))
                throw new MqttException(MqttVersion.MQTT_5 == session.getMqttProtocolVersion() ?
                        (int) MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE.byteValue() :
                        (int) MqttConnectReturnCode.CONNECTION_REFUSED_TOPIC_NAME_INVALID.byteValue(),
                        false, "SUBSCRIBE - Repeat subscribe,reason: Client subscribe already exists");

            subscribeStoreServer.put(t.topicName(), subscribe);
            if (log.isDebugEnabled())
                log.debug("SUBSCRIBE - Client subscribe topic: clientId:{}, topic:{}", session.getClientId(), t);
        });
        //send retain message
        subTopics.forEach(s -> sendRetainMessage(channel, s.topicName(), s.qualityOfService()));
    }

    @Override
    public MqttMessageType getHandleType() {
        return T.SUBSCRIBE;
    }

    @Override
    public void onHandlerError(Channel channel, M msg, MqttException ex) {
        MqttSubAckMessage subAckErrorMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                new MqttSubAckPayload(ex.getReasonCode()));
        channel.writeAndFlush(subAckErrorMessage);
        if (ex.isDisConnect()) {
            channel.close();
            log.info(ex.getMessage());
        }
    }

    private void sendRetainMessage(Channel channel, String subscribeTopic, MqttQoS qos) {
        List<RetainMessage> retainMessages = dupRetainMessageServer.match(subscribeTopic);
        retainMessages.forEach(m -> {
            MqttQoS targetQos = MqttQoS.valueOf(Math.min(m.getQos().value(), qos.value()));
            int messageId = MqttQoS.AT_MOST_ONCE == targetQos ? 0 :
                    messagePacketIdServer.nextMessageId((MqttVersion) channel.attr(AttributeKey.valueOf("mqttVersion")).get());
            MqttPublishMessage publishMessage = MqttMessageBuilders.publish()
                    .topicName(m.getTopic())
                    .messageId(messageId)
                    .qos(targetQos)
                    .retained(false)
                    .payload(Unpooled.buffer().writeBytes(m.getPayload()))
                    .build();
            channel.writeAndFlush(publishMessage);
            if (log.isDebugEnabled())
                log.debug("==> PUBLISH - Send retain message: clientId:{}, topic:{}, qos:{}, messageId:{}",
                        channel.attr(AttributeKey.valueOf("clientId")).get(), m.getTopic(), targetQos.value(), messageId);
        });
    }
}
