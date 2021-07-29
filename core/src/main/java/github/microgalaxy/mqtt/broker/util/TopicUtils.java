package github.microgalaxy.mqtt.broker.util;

import github.microgalaxy.mqtt.broker.config.BrokerConstant;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Microgalaxy（https://github.com/micro-galaxy）
 */
public abstract class TopicUtils {

    public static boolean validTopic(String topicName) {
        //valid prefix and suffix
        if (StringUtils.startsWithIgnoreCase(topicName, BrokerConstant.ShareSubscribe.SUBSCRIBE_MULTIPLE_TIER) ||
                StringUtils.startsWithIgnoreCase(topicName, BrokerConstant.ShareSubscribe.SUBSCRIBE_ONE_TIER) ||
                StringUtils.endsWithIgnoreCase(topicName, BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT) ||
                !topicName.contains(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT)) return false;
        //valid multiple tier matching
        if (topicName.contains(BrokerConstant.ShareSubscribe.SUBSCRIBE_MULTIPLE_TIER)) {
            if (!StringUtils.endsWithIgnoreCase(topicName,
                    BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT + BrokerConstant.ShareSubscribe.SUBSCRIBE_MULTIPLE_TIER))
                return false;
            if (StringUtils.countOccurrencesOf(topicName, BrokerConstant.ShareSubscribe.SUBSCRIBE_MULTIPLE_TIER) >
                    BrokerConstant.ShareSubscribe.SUBSCRIBE_MULTIPLE_TIER.length())
                return false;
        }
        //valid share topic
        if (StringUtils.startsWithIgnoreCase(topicName, "$") &&
                !StringUtils.startsWithIgnoreCase(topicName,
                        BrokerConstant.ShareSubscribe.SUBSCRIBE_SHARE_PREFIX + BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT))
            return false;
        //valid one tier matching
        if (topicName.contains(BrokerConstant.ShareSubscribe.SUBSCRIBE_ONE_TIER)) {
            if (StringUtils.countOccurrencesOf(topicName, BrokerConstant.ShareSubscribe.SUBSCRIBE_ONE_TIER) !=
                    StringUtils.countOccurrencesOf(topicName, BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT + BrokerConstant.ShareSubscribe.SUBSCRIBE_ONE_TIER))
                return false;
        }
        return true;
    }

    public static boolean matchingTopic(String subscriptTopic, String publishTopic) {
        String[] publishTopics = publishTopic.split(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT);
        String[] subscriptTopics = subscriptTopic.split(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT);
        if (publishTopics.length < subscriptTopics.length) return false;
        String matchingTopic = IntStream.range(0, subscriptTopics.length).mapToObj(i -> {
            String tier = subscriptTopics[i];
            if (Objects.equals(tier, BrokerConstant.ShareSubscribe.SUBSCRIBE_ONE_TIER) ||
                    Objects.equals(tier, BrokerConstant.ShareSubscribe.SUBSCRIBE_MULTIPLE_TIER))
                return publishTopics[i] + BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT;
            return tier + BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT;
        }).collect(Collectors.joining(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT));
        return Objects.equals(matchingTopic, subscriptTopic);
    }

    public static boolean matchingShareTopic(String subscriptTopic, String publishTopic) {
        // $share/{group}
        int shareTier = 2;
        String[] publishTopics = publishTopic.split(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT);
        String[] subscriptTopics = subscriptTopic.split(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT);
        if (publishTopics.length < subscriptTopics.length) return false;
        String matchingTopic = IntStream.range(0, subscriptTopics.length).mapToObj(i -> {
            String tier = subscriptTopics[i];
            if (i < shareTier) return tier + BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT;
            if (Objects.equals(tier, BrokerConstant.ShareSubscribe.SUBSCRIBE_ONE_TIER) ||
                    Objects.equals(tier, BrokerConstant.ShareSubscribe.SUBSCRIBE_MULTIPLE_TIER))
                return publishTopics[i] + BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT;
            return tier + BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT;
        }).collect(Collectors.joining(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT));
        return Objects.equals(matchingTopic, subscriptTopic);
    }


}
