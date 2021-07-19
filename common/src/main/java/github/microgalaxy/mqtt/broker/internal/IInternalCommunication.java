package github.microgalaxy.mqtt.broker.internal;

/**
 * Internal communication interface for client clusters
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface IInternalCommunication {
    /**
     * InternalMessage arrives
     *
     * @param message
     */
    void onInternalMessage(InternalMessage message);

    /**
     * Sending internal broadcast messages
     *
     * @param message
     */
    void sendInternalMessage(InternalMessage message);
}
