package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.client.ISessionStore;
import github.microgalaxy.mqtt.broker.client.ISubscribeStore;
import github.microgalaxy.mqtt.broker.client.Session;
import github.microgalaxy.mqtt.broker.client.Subscribe;
import github.microgalaxy.mqtt.broker.handler.MqttException;
import github.microgalaxy.mqtt.broker.message.DupPublishMessage;
import github.microgalaxy.mqtt.broker.message.IMessagePacketId;
import github.microgalaxy.mqtt.broker.message.RetainMessage;
import github.microgalaxy.mqtt.broker.message.IDupPublishMessage;
import github.microgalaxy.mqtt.broker.message.IDupRetainMessage;
import io.netty.handler.codec.mqtt.MqttQoS;

import github.microgalaxy.mqtt.broker.internal.IInternalCommunication;
import github.microgalaxy.mqtt.broker.internal.InternalMessage;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * 发布消息
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttPublish<T extends MqttMessageType, M extends MqttPublishMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private ISessionStore sessionStoreServer;
    @Autowired
    private IMessagePacketId messagePacketIdServer;
    @Autowired
    private ISubscribeStore subscribeStoreServer;
    @Autowired
    private IInternalCommunication internalCommunicationServer;
    @Autowired
    private IDupPublishMessage dupPublishMessageServer;
    @Autowired
    private IDupRetainMessage dupRetainMessageServer;

    /**
     * 发布消息
     *
     * @param channel
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {
        String topic = msg.variableHeader().topicName();
        MqttQoS mqttQoS = msg.fixedHeader().qosLevel();
        byte[] messageBytes = new byte[msg.payload().readableBytes()];
        msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
        MqttPublishMessage publishMessage = MqttMessageBuilders.publish()
                .topicName(topic)
                .messageId(msg.variableHeader().packetId())
                .qos(mqttQoS)
                .payload(msg.payload())
                .retained(false)
                .build();
        InternalMessage internalMessage = new InternalMessage();
        internalMessage.setTopic(publishMessage.variableHeader().topicName());
        internalMessage.setQos(publishMessage.fixedHeader().qosLevel());
        internalMessage.setPayload(messageBytes);
        internalMessage.setRetain(false);
        internalMessage.setDup(false);

        if (MqttQoS.AT_LEAST_ONCE == mqttQoS) {
            sendPubAckMessage(channel, msg.variableHeader().packetId());
        }
        if (MqttQoS.EXACTLY_ONCE == mqttQoS) {
            sendPubRecMessage(channel, msg.variableHeader().packetId());
        }

        internalCommunicationServer.sendInternalMessage(internalMessage);
        sendPublishMessage(publishMessage, true);
        if (msg.fixedHeader().isRetain()) {
            if (messageBytes.length == 0) {
                dupRetainMessageServer.remove(topic);
            } else {
                RetainMessage retainMessage = new RetainMessage(topic, mqttQoS, messageBytes);
                dupRetainMessageServer.put(topic, retainMessage);
            }
        }
    }

    @Override
    public MqttMessageType getHandleType() {
        return T.PUBLISH;
    }

    @Override
    public void onHandlerError(Channel channel, M msg, MqttException ex) {
        MqttQoS mqttQoS = msg.fixedHeader().qosLevel();
        MqttMessage mqttErrorMessage = null;
        if (MqttQoS.AT_LEAST_ONCE == mqttQoS) {
            mqttErrorMessage = MqttMessageBuilders.pubAck()
                    .packetId(msg.variableHeader().packetId())
                    .reasonCode((byte) ex.getReasonCode())
                    .build();
        }
        if (MqttQoS.EXACTLY_ONCE == mqttQoS) {
            mqttErrorMessage = MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttPubReplyMessageVariableHeader(msg.variableHeader().packetId(), (byte) ex.getReasonCode(), MqttProperties.NO_PROPERTIES),
                    null);
        }
        channel.writeAndFlush(mqttErrorMessage);
        if (ex.isDisConnect()) {
            channel.close();
            log.info(ex.getMessage());
        }
    }

    private void sendPubAckMessage(Channel channel, int packetId) {
        MqttMessage pubAckMessage = MqttMessageBuilders.pubAck()
                .packetId(packetId)
                .build();
        channel.writeAndFlush(pubAckMessage);
        if (log.isDebugEnabled())
            log.debug("SEND - Send pubAck packet: clientId:{}, messageId:{}",
                    channel.attr(AttributeKey.valueOf("clientId")).get(), packetId);
    }

    private void sendPubRecMessage(Channel channel, int packetId) {
        MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(packetId), null);
        channel.writeAndFlush(pubRecMessage);
        if (log.isDebugEnabled())
            log.debug("SEND - Send pubRec packet: clientId:{}, messageId:{}",
                    channel.attr(AttributeKey.valueOf("clientId")).get(), packetId);
    }

    public void sendPublishMessage(MqttPublishMessage publishMessage, boolean needDup) {
        List<Subscribe> subscribes = subscribeStoreServer.matchTopic(publishMessage.variableHeader().topicName());
        subscribes.forEach(s -> {
            Session session = sessionStoreServer.get(s.getClientId());
            if (ObjectUtils.isEmpty(session)) return;
            MqttQoS targetQos = MqttQoS.valueOf(Math.min(s.getQos().value(), publishMessage.fixedHeader().qosLevel().value()));
            int messageId = MqttQoS.AT_MOST_ONCE == targetQos ? 0 :
                    messagePacketIdServer.nextMessageId((MqttVersion) session.getChannel().attr(AttributeKey.valueOf("mqttVersion")).get());
            MqttPublishMessage message = MqttMessageBuilders.publish()
                    .topicName(publishMessage.variableHeader().topicName())
                    .messageId(messageId)
                    .qos(targetQos)
                    .retained(publishMessage.fixedHeader().isRetain())
                    .payload(publishMessage.payload())
                    .build();
            if (targetQos.value() > MqttQoS.AT_MOST_ONCE.value() && needDup) {
                byte[] messageBytes = new byte[publishMessage.payload().readableBytes()];
                publishMessage.payload().getBytes(publishMessage.payload().readableBytes(), messageBytes);
                DupPublishMessage dupPublishMessage = new DupPublishMessage(s.getClientId(), s.getTopic(), targetQos, messageId, messageBytes);
                dupPublishMessageServer.put(s.getClientId(), dupPublishMessage);
            }
            session.getChannel().writeAndFlush(message);
            if (log.isDebugEnabled())
                log.debug("PUBLISH - Publish a message: clientId:{}, topic:{}, qos:{}, messageId:{}",
                        s.getClientId(), s.getTopic(), targetQos.value(), messageId);
        });
    }
}
