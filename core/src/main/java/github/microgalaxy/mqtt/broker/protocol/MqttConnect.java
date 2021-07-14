package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.auth.LoginAuth;
import github.microgalaxy.mqtt.broker.auth.LoginAuthInterface;
import github.microgalaxy.mqtt.broker.client.ISessionStore;
import github.microgalaxy.mqtt.broker.client.ISubscribeStore;
import github.microgalaxy.mqtt.broker.client.Session;
import github.microgalaxy.mqtt.broker.store.IDupPubRelMassage;
import github.microgalaxy.mqtt.broker.store.IDupPublishMassage;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * 发起连接
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttConnect<T extends MqttMessageType, M extends MqttConnectMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private LoginAuthInterface authServer;
    @Autowired
    private ISessionStore sessionServer;
    @Autowired
    private ISubscribeStore subscribeServer;
    @Autowired
    private IDupPublishMassage dupPublishMassageServer;
    @Autowired
    private IDupPubRelMassage dupPubRelMassageServer;

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
        //处理编码异常、客户端id异常
        boolean formatOk = validMsgFormat(channel, msg);
        if (!formatOk) {
            return;
        }
        //客户端认证
        boolean authOk = loginAuth(channel, msg);
        if (!authOk) {
            return;
        }

        //登录
        singleLogin(channel, msg);
        //处理心跳消息
        heartbeat(channel, msg);
        //处理qos1，qos2未完成的消息
        processDupMsg(channel, msg);
    }

    private void processDupMsg(Channel channel, M msg) {
        if (msg.variableHeader().isCleanSession())
            return;
        dupPublishMassageServer.get(msg.payload().clientIdentifier())
                .forEach(dupPublishMassage -> {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, true, dupPublishMassage.getQos(), false, 0),
                            new MqttPublishVariableHeader(dupPublishMassage.getTopic(), dupPublishMassage.getMassageId()),
                            dupPublishMassage.getPayload());
                    channel.writeAndFlush(publishMessage);
                });
        dupPubRelMassageServer.get(msg.payload().clientIdentifier())
                .forEach(dupPubRelMassage -> {
                    MqttMessage pubRelMassage = MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBREL, true, MqttQoS.AT_MOST_ONCE, false, 0),
                            MqttMessageIdVariableHeader.from(dupPubRelMassage.getMassageId()), null);
                    channel.writeAndFlush(pubRelMassage);
                });

    }

    private void heartbeat(Channel channel, M msg) {
        if (msg.variableHeader().keepAliveTimeSeconds() > 0) {
            if (channel.pipeline().names().contains("idleStateHandler"))
                channel.pipeline().remove("idleStateHandler");
            channel.pipeline().addFirst("idleStateHandler", new IdleStateHandler(0, 0, Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f)));
        }
    }

    private void singleLogin(Channel channel, M msg) {
        String clientId = msg.payload().clientIdentifier();
        Session previousSession = sessionServer.get(clientId);
        if (!ObjectUtils.isEmpty(previousSession)) {
            if (previousSession.isCleanSession()) {
                sessionServer.remove(clientId);
                subscribeServer.removeClient(clientId);
                dupPublishMassageServer.removeClient(clientId);
                dupPubRelMassageServer.removeClient(clientId);
            }
            previousSession.getChannel().close();
        }
//TODO will support mqtt v5
        //遗嘱消息
        MqttPublishMessage willMessage = null;
        if (msg.variableHeader().isWillFlag()) {
            willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(msg.variableHeader().willQos()), msg.variableHeader().isWillRetain(), 0),
                    new MqttPublishVariableHeader(msg.payload().willTopic(), 0), msg.payload().willMessage());
        }
        channel.attr(AttributeKey.valueOf("clientId")).set(msg.payload().clientIdentifier());
        Session curSession = new Session(clientId, msg.payload().userName(), channel,
                msg.variableHeader().isCleanSession(), willMessage, msg.variableHeader().version());
        sessionServer.put(clientId, curSession);

        MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, msg.variableHeader().isCleanSession()), null);
        channel.writeAndFlush(connAckMessage);
        log.info("CONNECT - Client connected: clientId:{}, clearSession:{}", clientId, msg.variableHeader().isCleanSession());
    }

    private boolean loginAuth(Channel channel, M msg) {
        MqttVersion mqttVersion = MqttVersion.fromProtocolNameAndLevel(msg.variableHeader().name(),(byte) msg.variableHeader().version());
        MqttConnectPayload payload = msg.payload();
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        LoginAuth loginMode = new LoginAuth(payload.clientIdentifier(), payload.userName(), payload.password(),
                socketAddress.getHostString(), mqttVersion.name(), socketAddress.getPort());
        boolean authOk = authServer.loginAuth(loginMode);
        if (!authOk) {
            if (log.isDebugEnabled())
                log.debug("Bad username or password:{}", msg.decoderResult().toString());
            MqttMessageBuilders.ConnAckBuilder connAckMessage = MqttMessageBuilders.connAck().returnCode(MqttVersion.MQTT_5 == mqttVersion ?
                    MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD : MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD)
            channel.writeAndFlush(connAckMessage);
            channel.close();
        }
        return authOk;
    }

    private boolean validMsgFormat(Channel channel, M msg) {
        boolean enbDebug = log.isDebugEnabled();
        //解码失败
        if (msg.decoderResult().isFailure()) {
            Throwable cause = msg.decoderResult().cause();
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                if (enbDebug)
                    log.debug("Unsupported versions of the mqtt protocol:{}", msg.decoderResult().toString());
                MqttMessageBuilders.ConnAckBuilder connAckMessage = MqttMessageBuilders.connAck()
                        .returnCode(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION)
                        .sessionPresent(false);
                channel.writeAndFlush(connAckMessage);
            } else if (cause instanceof MqttIdentifierRejectedException) {
                if (enbDebug)
                    log.debug("Request contains an invalid client identifier:{}", msg.decoderResult().toString());
                MqttMessageBuilders.ConnAckBuilder connAckMessage = MqttMessageBuilders.connAck()
                        .returnCode(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED)
                        .sessionPresent(false);
                channel.writeAndFlush(connAckMessage);
            }
            channel.close();
            return false;
        }
        if (StringUtils.isEmpty(msg.payload().clientIdentifier())) {
            if (enbDebug)
                log.debug("Request contains an invalid client identifier:{}", msg.decoderResult().toString());
            MqttVersion mqttVersion = MqttVersion.fromProtocolNameAndLevel(msg.variableHeader().name(), (byte) msg.variableHeader().version());
            MqttMessageBuilders.ConnAckBuilder connAckMessage = MqttMessageBuilders.connAck()
                    .returnCode(MqttVersion.MQTT_5 == mqttVersion ? MqttConnectReturnCode.CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID
                            : MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED)
                    .sessionPresent(false);
            channel.writeAndFlush(connAckMessage);
            channel.close();
            return false;
        }
        return true;
    }

}
