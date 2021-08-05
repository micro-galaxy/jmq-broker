package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.message.IMessagePacketId;
import github.microgalaxy.mqtt.broker.message.IDupPublishMessage;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
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
    private IMessagePacketId messagePacketIdServer;
    @Autowired
    private IDupPublishMessage dupPublishMessageServer;
    /**
     * 发布回执消息
     *
     * @param channel
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {
        int messageId = ((MqttMessageIdVariableHeader) msg.variableHeader()).messageId();
        dupPublishMessageServer.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        messagePacketIdServer.releaseMessageId(messageId);
        if (log.isDebugEnabled())
            log.debug("<== PUBACK - PubAck request arrives: clientId:{}, messageId:{}",
                    channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
    }


    @Override
    public MqttMessageType getHandleType() {
        return T.PUBACK;
    }
}
