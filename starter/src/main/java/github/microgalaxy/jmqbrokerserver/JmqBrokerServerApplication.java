package github.microgalaxy.jmqbrokerserver;

import github.microgalaxy.mqtt.broker.server.BrokerServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@SpringBootApplication(scanBasePackages = "github.microgalaxy.mqtt.broker")
public class JmqBrokerServerApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(JmqBrokerServerApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }
//
//    @Bean
//    public BrokerServer brokerProperties() {
//        return new BrokerServer();
//    }

}
