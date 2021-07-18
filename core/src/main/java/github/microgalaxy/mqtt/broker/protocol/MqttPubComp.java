package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.massage.IMassagePacketId;
import github.microgalaxy.mqtt.broker.store.IDupPubRelMassage;
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
public class MqttPubComp<T extends MessageHandleType.PubComp, M extends MqttMessage> extends AbstractMqttMsgProtocol<T, M> {
    @Autowired
    private IMassagePacketId massageIdServer;
    @Autowired
    private IDupPubRelMassage dupPubRelMassageServer;

    /**
     * QoS2消息完成消息
     *
     * @param channel
     * @param msg
     */
    @Override
    public void onMqttMsg(Channel channel, M msg) {
        int messageId = ((MqttMessageIdVariableHeader) msg.variableHeader()).messageId();
        dupPubRelMassageServer.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        massageIdServer.releaseMassageId(messageId);
        if (log.isDebugEnabled())
            log.debug("PUBCOMP - PubComp request arrives: clientId:{}, messageId:{}",
                    channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
    }
}
