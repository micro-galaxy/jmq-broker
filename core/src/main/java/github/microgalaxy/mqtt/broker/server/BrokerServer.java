package github.microgalaxy.mqtt.broker.server;


import github.microgalaxy.mqtt.broker.codec.MqttWebsocketCodec;
import github.microgalaxy.mqtt.broker.config.BrokerProperties;
import github.microgalaxy.mqtt.broker.handler.MqttMsgHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * mqtt broker server
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Service
public class BrokerServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerServer.class);

    private BrokerProperties brokerProperties;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventLoopGroup businessGroup;
    private Channel mqttChannel;
    private Channel websocketChannel;

    @Autowired
    private void setBrokerProperties(BrokerProperties brokerProperties) {
        this.brokerProperties = brokerProperties;
    }

    @PostConstruct
    public void start() throws Exception {
        LOGGER.info("Starting MQTT Broker ..., node:{}", brokerProperties.getBrokerId());
        bossGroup = brokerProperties.isUseEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        workerGroup = brokerProperties.isUseEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        businessGroup = brokerProperties.isUseEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        mqttServer();
        websocketServer();
        shutdownHook();
        LOGGER.info("MQTT Broker is running. Mqtt port:{}, Websocket port:{}", brokerProperties.getMqttPort(), brokerProperties.getMqttWsPort());
    }

    private void mqttServer() throws Exception {
        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(brokerProperties.isUseEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                //Netty heartbeat detection
                                .addFirst("idleStateHandler", new IdleStateHandler(0, 0, brokerProperties.getKeepAlive()))
                                .addLast("decoder", new MqttDecoder(brokerProperties.getPayloadLength()))
                                .addLast("encoder", MqttEncoder.INSTANCE)
                                .addLast(businessGroup, "mqttMsgHandler", new MqttMsgHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, brokerProperties.getSoBacklog())
                .childOption(ChannelOption.SO_KEEPALIVE, brokerProperties.isSoKeepAlive())
                .childOption(ChannelOption.TCP_NODELAY, brokerProperties.isTcpNoDelay());
        mqttChannel = server.bind(brokerProperties.getMqttPort()).sync().channel();
    }

    private void websocketServer() throws Exception {
        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(brokerProperties.isUseEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                //Netty heartbeat detection
                                .addFirst("idleStateHandler", new IdleStateHandler(0, 0, brokerProperties.getKeepAlive()))
                                //Decoding/encoding of messages into HTTP messages
                                .addLast("http-codec", new HttpServerCodec())
                                //Combine the HTTP message line, message header, and message body into one complete HTTP message (FullHttpRequest)
                                .addLast("aggregator",
                                        //Request header + body
                                        new HttpObjectAggregator(512 * 1024 + brokerProperties.getPayloadLength(), true))
                                //HTTP message compression
                                .addLast("compressor", new HttpContentCompressor())
                                .addLast("websocketProtocol", new WebSocketServerProtocolHandler(brokerProperties.getWsPath(), "mqtt,mqttv3.1,mqttv3.1.1"))
                                .addLast("websocketToMqtt", new MqttWebsocketCodec())
                                .addLast("decoder", new MqttDecoder(brokerProperties.getPayloadLength()))
                                .addLast("encoder", MqttEncoder.INSTANCE)
                                .addLast(businessGroup, "mqttMsgHandler", new MqttMsgHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, brokerProperties.getSoBacklog())
                .childOption(ChannelOption.SO_KEEPALIVE, brokerProperties.isSoKeepAlive())
                .childOption(ChannelOption.TCP_NODELAY, brokerProperties.isTcpNoDelay());
        websocketChannel = server.bind(brokerProperties.getMqttWsPort()).sync().channel();
    }

    private void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("MQTT Broker is shutdown...");
            bossGroup.shutdownGracefully();
            businessGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            mqttChannel.close();
            websocketChannel.close();
        }));
    }
}
