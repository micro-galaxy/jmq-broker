package github.microgalaxy.mqtt.broker.handler;

import github.microgalaxy.mqtt.broker.protocol.MqttMsgProtocolFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttMessage;

/**
 * MQTT消息处理
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public class MqttMsgHandler extends SimpleChannelInboundHandler<MqttMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        MqttMsgProtocolFactory.processMsg(ctx.channel(), msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
    }
}
