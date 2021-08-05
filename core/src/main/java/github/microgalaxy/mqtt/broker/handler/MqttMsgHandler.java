package github.microgalaxy.mqtt.broker.handler;

import github.microgalaxy.mqtt.broker.protocol.MqttDisConnect;
import github.microgalaxy.mqtt.broker.protocol.MqttMsgProtocolFactory;
import github.microgalaxy.mqtt.broker.protocol.MqttPublish;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * MQTT消息处理
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public class MqttMsgHandler extends MqttProtocolHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent)) {
            super.userEventTriggered(ctx, evt);
            return;
        }
        IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
        if (idleStateEvent.state() != IdleState.ALL_IDLE) return;
        MqttPublish mqttPublish = MqttMsgProtocolFactory.getHandler(MqttMessageType.PUBLISH, MqttPublish.class);
        mqttPublish.sendWillMessage(ctx.channel());
        ctx.channel().close();
        MqttMsgProtocolFactory.getHandler(MqttMessageType.DISCONNECT, MqttDisConnect.class)
                .cleanSession(ctx.channel());
        log.info("DISCONNECT - The server closed an existing connection, reason: The client heartbeat timeout, clientId:{}, ", ctx.channel().attr(AttributeKey.valueOf("clientId")).get());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!(cause instanceof IOException)) {
            super.exceptionCaught(ctx, cause);
            return;
        }
        ctx.channel().close();
        MqttMsgProtocolFactory.getHandler(MqttMessageType.DISCONNECT, MqttDisConnect.class)
                .cleanSession(ctx.channel());
        log.info("DISCONNECT - The client forcibly closed an existing connection:{} ,clientId:{}", cause.getMessage(),
                ctx.channel().attr(AttributeKey.valueOf("clientId")).get(), cause);
    }
}
