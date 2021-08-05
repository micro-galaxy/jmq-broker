package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.message.IMessagePacketId;
import github.microgalaxy.mqtt.broker.message.IDupPubRelMessage;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * QoS2消息完成
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class MqttPubComp<T extends MqttMessageType, M extends MqttMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private IMessagePacketId messagePacketIdServer;
    @Autowired
    private IDupPubRelMessage dupPubRelMessageServer;

    /**
     * QoS2消息完成消息
     *
     * @param channel
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {
        int messageId = ((MqttMessageIdVariableHeader) msg.variableHeader()).messageId();
        dupPubRelMessageServer.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        messagePacketIdServer.releaseMessageId(messageId);
        if (log.isDebugEnabled())
            log.debug("<== PUBCOMP - PubComp request arrives: clientId:{}, messageId:{}",
                    channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
    }

    @Override
    public MqttMessageType getHandleType() {
        return T.PUBCOMP;
    }
}
