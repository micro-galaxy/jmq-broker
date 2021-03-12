package github.microgalaxy.mqtt.broker.protocol;

import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;

import javax.annotation.PostConstruct;

/**
 * mqtt消息协议接口
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public abstract class AbstractMqttMsgProtocol<T extends MqttMessageType, M extends MqttMessage> implements IMqttMsgProtocol<M> {

    /**
     * 获取消息类型
     *
     * @return
     */
    protected abstract T getType();

    /**
     * constructor
     *
     * @return
     */
    @PostConstruct
    void registerMsgProcess() {
        MqttMsgProtocolFactory.registerMsgProcess(getType(), this);
    }


}
