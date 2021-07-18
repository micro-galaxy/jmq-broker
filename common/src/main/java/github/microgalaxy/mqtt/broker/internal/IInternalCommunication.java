package github.microgalaxy.mqtt.broker.internal;

/**
 * Internal communication interface for client clusters
 *
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public interface IInternalCommunication {
    /**
     * InternalMassage arrives
     *
     * @param massage
     */
    void onInternalMassage(InternalMassage massage);

    /**
     * Sending internal broadcast messages
     *
     * @param massage
     */
    void sendInternalMassage(InternalMassage massage);
}
