package github.microgalaxy.mqtt.broker.server;


import github.microgalaxy.mqtt.broker.config.BrokerProperties;
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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
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
        mqttServer();
        websocketServer();
        LOGGER.info("MQTT Broker is running. Mqtt port:{}, Websocket port:{}", brokerProperties.getMqttPort(), brokerProperties.getMqttWsPort());
    }


    private void mqttServer() throws Exception {
        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup,workerGroup)
                .channel(brokerProperties.isUseEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {

                    }
                })
                .option(ChannelOption.SO_BACKLOG,brokerProperties.getSoBacklog())
                .childOption();
        mqttChannel = server.bind(brokerProperties.getMqttPort()).sync().channel();

    }

    private void websocketServer() {


    }
}
