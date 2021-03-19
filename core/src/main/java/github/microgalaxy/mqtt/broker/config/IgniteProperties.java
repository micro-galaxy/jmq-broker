package github.microgalaxy.mqtt.broker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * apache ignite 配置
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Configuration
@ConfigurationProperties("spring.jmq.broker.cache")
public class IgniteProperties {

    /**
     * 持久化内存缓存初始化大小,MB
     */
    private int persistenceInitialSize = 64;

    /**
     * 持久化内存缓存最大大小,MB
     */
    private int persistenceMaxSize = 128;
}
