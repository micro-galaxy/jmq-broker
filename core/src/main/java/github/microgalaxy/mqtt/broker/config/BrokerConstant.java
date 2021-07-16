package github.microgalaxy.mqtt.broker.config;

/**
 * @author Microgalaxy
 */
public abstract class BrokerConstant {
    public abstract static class ShareSubscribe {
        public static final String SUBSCRIBE_SHARE_PREFIX = "$share";

        public static final String SUBSCRIBE_TIER_SPLIT = "/";

        public static final String SUBSCRIBE_ONE_TIER = "+";

        public static final String SUBSCRIBE_MULTIPLE_TIER = "#";
    }
}
