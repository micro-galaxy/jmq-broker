package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.client.ISessionStore;
import github.microgalaxy.mqtt.broker.client.ISubscribeStore;
import github.microgalaxy.mqtt.broker.client.Session;
import github.microgalaxy.mqtt.broker.client.Subscribe;
import github.microgalaxy.mqtt.broker.massage.DupPublishMassage;
import github.microgalaxy.mqtt.broker.massage.IMassagePacketId;
import github.microgalaxy.mqtt.broker.massage.RetainMassage;
import github.microgalaxy.mqtt.broker.store.IDupPublishMassage;
import github.microgalaxy.mqtt.broker.store.IDupRetainMassage;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.buffer.ByteBuf;

import github.microgalaxy.mqtt.broker.internal.IInternalCommunication;
import github.microgalaxy.mqtt.broker.internal.InternalMassage;
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
public class MqttPublish<T extends MessageHandleType.Publish, M extends MqttPublishMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private ISessionStore sessionStoreServer;
    @Autowired
    private IMassagePacketId massagePacketIdServer;
    @Autowired
    private ISubscribeStore subscribeStoreServer;
    @Autowired
    private IInternalCommunication internalCommunicationServer;
    @Autowired
    private IDupPublishMassage dupPublishMassageServer;
    @Autowired
    private IDupRetainMassage dupRetainMassageServer;

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
        msg.payload().getBytes(msg.payload().readableBytes(), messageBytes);
        MqttPublishMessage publishMessage = MqttMessageBuilders.publish()
                .topicName(topic)
                .messageId(msg.variableHeader().packetId())
                .qos(mqttQoS)
                .payload(msg.payload())
                .retained(false)
                .build();
        InternalMassage internalMassage = new InternalMassage();
        internalMassage.setTopic(publishMessage.variableHeader().topicName());
        internalMassage.setQos(publishMessage.fixedHeader().qosLevel());
        internalMassage.setPayload(messageBytes);
        internalMassage.setRetain(false);
        internalMassage.setDup(false);
        internalCommunicationServer.sendInternalMassage(internalMassage);
        sendPublishMessage(publishMessage, true);
        if (MqttQoS.AT_LEAST_ONCE == mqttQoS) {
            sendPubAckMessage(channel, msg.variableHeader().packetId());
        }
        if (MqttQoS.EXACTLY_ONCE == mqttQoS) {
            sendPubRecMessage(channel, msg.variableHeader().packetId());
        }

        if (msg.fixedHeader().isRetain()) {
            if (messageBytes.length == 0) {
                dupRetainMassageServer.remove(topic);
            } else {
                RetainMassage retainMassage = new RetainMassage(topic, mqttQoS, messageBytes);
                dupRetainMassageServer.put(topic, retainMassage);
            }
        }
    }


    private void sendPubAckMessage(Channel channel, int packetId) {
        MqttMessage pubAckMessage = MqttMessageBuilders.pubAck()
                .packetId(packetId)
                .build();
        channel.writeAndFlush(pubAckMessage);
    }

    private void sendPubRecMessage(Channel channel, int packetId) {
        MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(packetId), null);
        channel.writeAndFlush(pubRecMessage);
    }

    public void sendPublishMessage(MqttPublishMessage publishMessage, boolean needDup) {
        List<Subscribe> subscribes = subscribeStoreServer.matchTopic(publishMessage.variableHeader().topicName());
        subscribes.forEach(s -> {
            Session session = sessionStoreServer.get(s.getClientId());
            if (ObjectUtils.isEmpty(session)) return;

            int messageId = massagePacketIdServer.nextMassageId();
            MqttQoS targetQos = MqttQoS.valueOf(Math.min(s.getQos().value(), publishMessage.fixedHeader().qosLevel().value()));
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
                DupPublishMassage dupPublishMassage = new DupPublishMassage(s.getClientId(), s.getTopic(), targetQos, messageId, messageBytes);
                dupPublishMassageServer.put(s.getClientId(), dupPublishMassage);
            }
            session.getChannel().writeAndFlush(message);
            if (log.isDebugEnabled())
                log.debug("PUBLISH - Publish a message: clientId:{}, topic:{}, qos:{}, messageId:{}",
                        s.getClientId(), s.getTopic(), targetQos.value(), messageId);
        });
    }
}
