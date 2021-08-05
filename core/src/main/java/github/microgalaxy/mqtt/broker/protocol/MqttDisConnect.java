package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.client.ISessionStore;
import github.microgalaxy.mqtt.broker.client.ISubscribeStore;
import github.microgalaxy.mqtt.broker.client.Session;
import github.microgalaxy.mqtt.broker.message.IDupPubRelMessage;
import github.microgalaxy.mqtt.broker.message.IDupPublishMessage;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 断开连接
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttDisConnect<T extends MqttMessageType, M extends MqttMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private ISessionStore sessionServer;
    @Autowired
    private ISubscribeStore subscribeStoreServer;
    @Autowired
    private IDupPublishMessage dupPublishMessageServer;
    @Autowired
    private IDupPubRelMessage dupPubRelMessageServer;

    /**
     * 断开连接消息
     *
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        Session session = sessionServer.get(clientId);
        if (session.isCleanSession()) {
            subscribeStoreServer.removeClient(clientId);
            dupPublishMessageServer.removeClient(clientId);
            dupPubRelMessageServer.removeClient(clientId);
        }
        sessionServer.remove(clientId);
        if (session.getMqttProtocolVersion().protocolLevel() >= MqttVersion.MQTT_5.protocolLevel()) {
            MqttMessage disconnectAckMessage = MqttMessageBuilders.disconnect().build();
            channel.writeAndFlush(disconnectAckMessage);
        }
        channel.close();
        log.info("DISCONNECT - Client disconnected: clientId:{}, clearSession:{}", clientId, session.isCleanSession());
    }

    @Override
    public MqttMessageType getHandleType() {
        return T.DISCONNECT;
    }


    public void cleanSession(Channel channel) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        subscribeStoreServer.removeClient(clientId);
        dupPublishMessageServer.removeClient(clientId);
        dupPubRelMessageServer.removeClient(clientId);
        sessionServer.remove(clientId);
    }
}
