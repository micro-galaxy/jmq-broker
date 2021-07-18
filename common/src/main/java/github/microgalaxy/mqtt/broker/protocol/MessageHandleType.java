package github.microgalaxy.mqtt.broker.protocol;

/**
 * @author Microgalaxy
 */
public final class MessageHandleType {
    public interface Connect{};
    public interface ConnAck{};
    public interface Publish{};
    public interface PubAck{};
    public interface PubRec{};
    public interface PubRel{};
    public interface PubComp{};
    public interface Subscribe{};
    public interface SubAck{};
    public interface Unsubscribe{};
    public interface UnsubAck{};
    public interface PingReq{};
    public interface PingResp{};
    public interface Disconnect{};
    public interface Auth{};
}
