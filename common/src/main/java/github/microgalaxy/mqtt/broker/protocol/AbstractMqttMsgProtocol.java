package github.microgalaxy.mqtt.broker.protocol;

import io.netty.handler.codec.mqtt.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;

/**
 * mqtt消息协议接口
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public abstract class AbstractMqttMsgProtocol<T, M extends MqttMessage> implements IMqttMsgProtocol<M> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    public Class<?> getMessageType(){
        Optional<? extends Class<?>> optional = Arrays.stream(getClass().getMethods())
                .filter(method -> method.getName().equals(IMqttMsgProtocol.class.getMethods()[0].getName()))
                .filter(method -> Arrays.stream(method.getParameterTypes()).noneMatch(type -> type.getName().equals(Object.class.getName())))
                .map(method -> method.getParameterTypes()[0])
                .findFirst();
        return optional.isPresent() ? optional.get() : Object.class;
    }

    @PostConstruct
    private void registerMsgHandle() {
        MqttMsgProtocolFactory.registerMsgHandle(getMsgType(), getBean());
    }


    private String getMsgType() {
        return getGenericClass(getClass(), 0).getSimpleName().toLowerCase();
    }

    private AbstractMqttMsgProtocol getBean() {
        return null;
    }


    private Class<?> getGenericClass(Class<?> clazz, int genericIndex) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType))
            throw new IllegalArgumentException("GenericSuperclass type error !");
        return (Class<?>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[genericIndex];
    }
}
