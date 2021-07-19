package github.microgalaxy.mqtt.broker;

import github.microgalaxy.mqtt.broker.server.BrokerServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@SpringBootApplication(scanBasePackages = "github.microgalaxy.mqtt.broker")
public class JmqApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(JmqApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

    @Bean
    public BrokerServer brokerServer(){
        return new BrokerServer();
    }
}
