package github.microgalaxy.mqtt.broker.config;

import org.apache.ignite.Ignite;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * apache ignite配置
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Configuration
public class IgniteAutoConfig {

    @Bean
    public Ignite ignite(){
        return null;
    }
}
