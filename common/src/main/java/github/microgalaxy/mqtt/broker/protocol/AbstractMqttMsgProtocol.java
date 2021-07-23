package github.microgalaxy.mqtt.broker.protocol;

import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * mqtt消息协议接口
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public abstract class AbstractMqttMsgProtocol<T, M extends MqttMessage> implements IMqttMsgProtocol<M> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取实现类消息类型
     *
     * @return
     */
    public abstract MqttMessageType getHandleType();

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
        Optional<? extends Class<?>> optional = Arrays.stream(getClass().getMethods())
                .filter(method -> method.getName().equals(IMqttMsgProtocol.class.getMethods()[0].getName()))
                .filter(method -> Arrays.stream(method.getParameterTypes()).anyMatch(type -> Objects.equals(type.getSuperclass(), MqttMessage.class)))
                .map(method -> method.getParameterTypes()[1])
                .findFirst();
        return optional.isPresent() ? optional.get() : Object.class;
    }
}
