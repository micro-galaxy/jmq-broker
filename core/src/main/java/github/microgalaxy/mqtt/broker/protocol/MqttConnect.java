package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.auth.LoginAuth;
import github.microgalaxy.mqtt.broker.auth.LoginAuthInterface;
import github.microgalaxy.mqtt.broker.client.ISessionStore;
import github.microgalaxy.mqtt.broker.client.ISubscribeStore;
import github.microgalaxy.mqtt.broker.client.Session;
import github.microgalaxy.mqtt.broker.config.BrokerProperties;
import github.microgalaxy.mqtt.broker.handler.MqttException;
import github.microgalaxy.mqtt.broker.message.IDupPubRelMessage;
import github.microgalaxy.mqtt.broker.message.IDupPublishMessage;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;

/**
 * 发起连接
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttConnect<T extends MqttMessageType, M extends MqttConnectMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired(required = false)
    private LoginAuthInterface authServer;
    @Autowired
    private ISessionStore sessionServer;
    @Autowired
    private ISubscribeStore subscribeStoreServer;
    @Autowired
    private IDupPublishMessage dupPublishMessageServer;
    @Autowired
    private IDupPubRelMessage dupPubRelMessageServer;
    @Autowired
    private BrokerProperties brokerProperties;

    /**
     * 发起连接消息
     *
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {
        //处理编码异常、客户端id异常
        validMsgFormat(channel, msg);
        //客户端认证
        loginAuth(channel, msg);
        //登录
        singleLogin(channel, msg);
        //处理心跳消息
        heartbeat(channel, msg);
        //处理qos1，qos2未完成的消息
        processDupMsg(channel, msg);
    }

    @Override
    public MqttMessageType getHandleType() {
        return T.CONNECT;
    }


    @Override
    public void onHandlerError(Channel channel, M msg, MqttException ex) {
        MqttConnAckMessage connAckMessage = MqttMessageBuilders.connAck()
                .returnCode(MqttConnectReturnCode.valueOf((byte) ex.getReasonCode()))
                .sessionPresent(false)
                .build();
        channel.writeAndFlush(connAckMessage);
        if (ex.isDisConnect()) {
            channel.close();
            log.info(ex.getMessage());
        }
    }

    private void validMsgFormat(Channel channel, M msg) {
        //decode fail
        if (msg.decoderResult().isFailure()) {
            Throwable cause = msg.decoderResult().cause();
            int reasonCode = MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE.byteValue();
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                reasonCode = MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION.byteValue();
            } else if (cause instanceof MqttIdentifierRejectedException) {
                reasonCode = MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED.byteValue();
            }
            throw new MqttException(reasonCode, true, "Connect closed,reason: Mqtt packet format error");
        }
        if (StringUtils.isEmpty(msg.payload().clientIdentifier())) {
            MqttVersion mqttVersion = MqttVersion.fromProtocolNameAndLevel(msg.variableHeader().name(), (byte) msg.variableHeader().version());
            MqttConnectReturnCode reasonCode = MqttVersion.MQTT_5 == mqttVersion ? MqttConnectReturnCode.CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID
                    : MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
            throw new MqttException((int) reasonCode.byteValue(), true, "Connect closed,reason: Mqtt clientId is empty");
        }
    }

    private void loginAuth(Channel channel, M msg) {
        MqttVersion mqttVersion = MqttVersion.fromProtocolNameAndLevel(msg.variableHeader().name(), (byte) msg.variableHeader().version());
        MqttConnectPayload payload = msg.payload();
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        LoginAuth loginMode = new LoginAuth(payload.clientIdentifier(), payload.userName(), payload.password(),
                socketAddress.getHostString(), mqttVersion.name(), socketAddress.getPort());
        if(ObjectUtils.isEmpty(authServer)) return;
        boolean authOk = authServer.loginAuth(loginMode);
        if (!authOk)
            throw new MqttException(MqttVersion.MQTT_5 == mqttVersion ?
                    (int) MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD.byteValue() :
                    (int) MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD.byteValue(),
                    true, "Connect closed,reason: Bad username or password");

    }

    private void singleLogin(Channel channel, M msg) {
        MqttVersion mqttVersion = MqttVersion.fromProtocolNameAndLevel(msg.variableHeader().name(), (byte) msg.variableHeader().version());
        String clientId = msg.payload().clientIdentifier();
        Session previousSession = sessionServer.get(clientId);
        if (!ObjectUtils.isEmpty(previousSession)) {
            if (previousSession.isCleanSession()) {
                sessionServer.remove(clientId);
                subscribeStoreServer.removeClient(clientId);
                dupPublishMessageServer.removeClient(clientId);
                dupPubRelMessageServer.removeClient(clientId);
            }else {
                subscribeStoreServer.upNode(clientId,brokerProperties.getBrokerId());
            }
            previousSession.getChannel().close();
        }
        //will message
        MqttPublishMessage willMessage = null;
        if (msg.variableHeader().isWillFlag()) {
            willMessage = MqttMessageBuilders.publish().topicName(msg.payload().willTopic())
                    .messageId(0)
                    .qos(MqttQoS.valueOf(msg.variableHeader().willQos()))
                    .payload(Unpooled.buffer().writeBytes(msg.payload().willMessageInBytes()))
                    .retained(msg.variableHeader().isWillRetain())
                    .build();
        }
        channel.attr(AttributeKey.valueOf("clientId")).set(msg.payload().clientIdentifier());
        channel.attr(AttributeKey.valueOf("mqttVersion")).set(mqttVersion);
        Session curSession = new Session(clientId, msg.payload().userName(), channel,
                msg.variableHeader().isCleanSession(), willMessage, mqttVersion);
        sessionServer.put(clientId, curSession);

        MqttConnAckMessage connAckMessage = MqttMessageBuilders.connAck().returnCode(MqttConnectReturnCode.CONNECTION_ACCEPTED)
                .sessionPresent(!msg.variableHeader().isCleanSession())
                .build();
        channel.writeAndFlush(connAckMessage);
        log.info("CONNECT - Client connected: clientId:{}, mqttVersion: {}, clearSession:{}", clientId, mqttVersion, msg.variableHeader().isCleanSession());
    }


    private void heartbeat(Channel channel, M msg) {
        if (msg.variableHeader().keepAliveTimeSeconds() > 0) {
            if (channel.pipeline().names().contains("idleStateHandler"))
                channel.pipeline().remove("idleStateHandler");
            channel.pipeline().addFirst("idleStateHandler", new IdleStateHandler(0, 0, Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f)));
        }
    }


    private void processDupMsg(Channel channel, M msg) {
        if (msg.variableHeader().isCleanSession())
            return;
        MqttConnectPayload payload = msg.payload();
        dupPublishMessageServer.get(payload.clientIdentifier())
                .forEach(m -> {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, true, m.getQos(), false, 0),
                            new MqttPublishVariableHeader(m.getTopic(), m.getMessageId()), m.getPayload());
                    channel.writeAndFlush(publishMessage);
                });
        dupPubRelMessageServer.get(payload.clientIdentifier())
                .forEach(m -> {
                    MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBREL, true, MqttQoS.AT_MOST_ONCE, false, 0),
                            MqttMessageIdVariableHeader.from(m.getMessageId()), null);
                    channel.writeAndFlush(pubRelMessage);
                });

    }

}
