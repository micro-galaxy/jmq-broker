package github.microgalaxy.mqtt.broker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * apache ignite 配置
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Configuration
@ConfigurationProperties("jmq.broker.cache")
public class BrokerCacheProperties {

    /**
     * 持久化内存缓存初始化大小,MB
     */
    private int memoryPersistenceInitialSize = 64;

    /**
     * 持久化内存缓存最大大小,MB
     */
    private int memoryPersistenceMaxSize = 128;

    /**
     * 持久化磁盘缓存初始化大小,MB
     */
    private int filePersistenceInitialSize = 64;

    /**
     * 持久化磁盘缓存最大大小,MB
     */
    private int filePersistenceMaxSize = 128;

    public int getMemoryPersistenceInitialSize() {
        return memoryPersistenceInitialSize;
    }

    public void setMemoryPersistenceInitialSize(int memoryPersistenceInitialSize) {
        this.memoryPersistenceInitialSize = memoryPersistenceInitialSize;
    }

    public int getMemoryPersistenceMaxSize() {
        return memoryPersistenceMaxSize;
    }

    public void setMemoryPersistenceMaxSize(int memoryPersistenceMaxSize) {
        this.memoryPersistenceMaxSize = memoryPersistenceMaxSize;
    }

    public int getFilePersistenceInitialSize() {
        return filePersistenceInitialSize;
    }

    public void setFilePersistenceInitialSize(int filePersistenceInitialSize) {
        this.filePersistenceInitialSize = filePersistenceInitialSize;
    }

    public int getFilePersistenceMaxSize() {
        return filePersistenceMaxSize;
    }

    public void setFilePersistenceMaxSize(int filePersistenceMaxSize) {
        this.filePersistenceMaxSize = filePersistenceMaxSize;
    }
}
