package github.microgalaxy.mqtt.broker.handler;

import github.microgalaxy.mqtt.broker.protocol.MqttMsgProtocolFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttMessage;

/**
 * mqtt协议处理器
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public abstract class MqttProtocolHandler extends SimpleChannelInboundHandler<MqttMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        MqttMsgProtocolFactory.processMsg(ctx.channel(), msg);
    }
}
