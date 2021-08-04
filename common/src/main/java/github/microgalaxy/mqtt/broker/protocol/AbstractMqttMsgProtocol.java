package github.microgalaxy.mqtt.broker.protocol;

import github.microgalaxy.mqtt.broker.handler.MqttException;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * mqtt消息协议接口
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public abstract class AbstractMqttMsgProtocol<T, M> implements IMqttMsgProtocol<M> {
    private Class<?> clazz;
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取实现类消息类型
     *
     * @return
     */
    public abstract MqttMessageType getHandleType();

    /**
     * 消息处理器异常处理
     *
     * @param channel
     * @param msg
     * @param ex
     * @return
     */
    public void onHandlerError(Channel channel, M msg, MqttException ex) {
        log.info(ex.getMessage(), ex);
    }


    @PostConstruct
    private void registerMsgHandle() {
        MqttMsgProtocolFactory.registerMsgHandle(getHandleType(), this);
    }


    /**
     * 获取实现类消息类型
     *
     * @return
     */
    public Class<?> getMessageType() {
        if (!ObjectUtils.isEmpty(clazz)) return clazz;
        Optional<? extends Class<?>> optional = Arrays.stream(getClass().getMethods())
                .filter(method -> method.getName().equals(IMqttMsgProtocol.class.getMethods()[0].getName()))
                .filter(method -> Arrays.stream(method.getParameterTypes()).anyMatch(type -> Objects.equals(type.getSuperclass(), MqttMessage.class)))
                .map(method -> method.getParameterTypes()[1])
                .findFirst();
        clazz = optional.isPresent() ? optional.get() : Object.class;
        return clazz;
    }
}
