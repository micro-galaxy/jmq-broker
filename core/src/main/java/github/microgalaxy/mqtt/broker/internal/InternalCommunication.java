package github.microgalaxy.mqtt.broker.internal;

import org.springframework.stereotype.Component;

/**
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
@Component
public class InternalCommunication implements IInternalCommunication {
    @Override
    public void onInternalMessage(InternalMessage message) {

    }

    @Override
    public void sendInternalMessage(InternalMessage message) {

    }
}
