package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.massage.IMassagePacketId;
import github.microgalaxy.mqtt.broker.protocol.AbstractMqttMsgProtocol;
import github.microgalaxy.mqtt.broker.store.IDupPublishMassage;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 发布回执
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttPubAck<T extends MqttMessageType, M extends MqttMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private IMassagePacketId massageIdServer;
    @Autowired
    private IDupPublishMassage dupPublishMassageServer;

    @Override
    protected T getType() {
        return (T) T.PUBACK;
    }

    /**
     * 发布回执消息
     *
     * @param channel
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {
        int messageId = ((MqttMessageIdVariableHeader) msg.variableHeader()).messageId();
        dupPublishMassageServer.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        massageIdServer.releaseMassageId(messageId);
        if (log.isDebugEnabled())
            log.debug("PUBACK - PubAck request arrives: clientId:{}, messageId:{}",
                    channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
    }
}
