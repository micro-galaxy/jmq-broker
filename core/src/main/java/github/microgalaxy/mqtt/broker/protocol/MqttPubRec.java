package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.massage.DupPubRelMassage;
import github.microgalaxy.mqtt.broker.protocol.AbstractMqttMsgProtocol;
import github.microgalaxy.mqtt.broker.store.IDupPubRelMassage;
import github.microgalaxy.mqtt.broker.store.IDupPublishMassage;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * QoS2消息回执
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttPubRec<T extends MqttMessageType, M extends MqttMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private IDupPublishMassage dupPublishMassageServer;
    @Autowired
    private IDupPubRelMassage dupPubRelMassageServer;

    @Override
    protected T getType() {
        return (T) T.PUBREC;
    }

    /**
     * QoS2消息回执消息
     *
     * @param channel
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        int messageId = ((MqttMessageIdVariableHeader) msg.variableHeader()).messageId();
        if (log.isDebugEnabled())
            log.debug("PUBREC - PubRec request arrives: clientId:{}, messageId:{}",clientId, messageId);
        dupPublishMassageServer.remove(clientId, messageId);
        MqttMessage pubRelMassage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        DupPubRelMassage dupPubRelMassageStore = new DupPubRelMassage(clientId, messageId);
        dupPubRelMassageServer.put(clientId, dupPubRelMassageStore);
        channel.writeAndFlush(pubRelMassage);
    }
}
