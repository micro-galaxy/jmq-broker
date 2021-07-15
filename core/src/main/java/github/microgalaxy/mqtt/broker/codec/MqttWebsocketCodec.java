package github.microgalaxy.mqtt.broker.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * Websocket Mqtt Message Encoder
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public class MqttWebsocketCodec extends MessageToMessageCodec<BinaryWebSocketFrame, ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf msg, List<Object> out) throws Exception {
        out.add(new BinaryWebSocketFrame(msg.retain()));
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, BinaryWebSocketFrame msg, List<Object> out) throws Exception {
        out.add(msg.retain().content());
    }
}
