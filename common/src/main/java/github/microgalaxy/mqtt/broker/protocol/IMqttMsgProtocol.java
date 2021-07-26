package github.microgalaxy.mqtt.broker.protocol;

import io.netty.channel.Channel;

/**
 * mqtt消息协议接口
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface IMqttMsgProtocol<M> {

    /**
     * mqtt消息协议
     *
     * @param channel
     * @param msg
     */
    void onMqttMsg(Channel channel, M msg);
}
