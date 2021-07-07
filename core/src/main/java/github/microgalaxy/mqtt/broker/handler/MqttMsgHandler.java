package github.microgalaxy.mqtt.broker.handler;

import io.netty.channel.ChannelHandlerContext;

/**
 * MQTT消息处理
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public class MqttMsgHandler extends MqttProtocolHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
    }
}
