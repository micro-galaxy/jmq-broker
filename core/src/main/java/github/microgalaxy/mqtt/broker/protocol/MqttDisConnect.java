package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.client.ISessionStore;
import github.microgalaxy.mqtt.broker.client.ISubscribeStore;
import github.microgalaxy.mqtt.broker.client.Session;
import github.microgalaxy.mqtt.broker.store.IDupPubRelMassage;
import github.microgalaxy.mqtt.broker.store.IDupPublishMassage;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttVersion;
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
        return (T) T.DISCONNECT;
    }

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
            subscribeServer.removeClient(clientId);
            dupPublishMassageServer.removeClient(clientId);
            dupPubRelMassageServer.removeClient(clientId);
        }
        sessionServer.remove(clientId);
        if (session.getMqttProtocolVersion().protocolLevel() >= MqttVersion.MQTT_5.protocolLevel()) {
            MqttMessage disconnectAckMessage = MqttMessageBuilders.disconnect().build();
            channel.writeAndFlush(disconnectAckMessage);
        }
        channel.close();
        log.info("DISCONNECT - Client disconnected: clientId:{}, clearSession:{}", clientId, session.isCleanSession());
    }

}
