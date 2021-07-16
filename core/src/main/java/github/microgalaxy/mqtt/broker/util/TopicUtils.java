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
            if (topicName.indexOf(BrokerConstant.ShareSubscribe.SUBSCRIBE_MULTIPLE_TIER) > BrokerConstant.ShareSubscribe.SUBSCRIBE_MULTIPLE_TIER.length())
                return false;
        }
        //valid share topic
        if (StringUtils.startsWithIgnoreCase(topicName, "$") &&
                !StringUtils.startsWithIgnoreCase(topicName, BrokerConstant.ShareSubscribe.SUBSCRIBE_SHARE_PREFIX))
            return false;
        //valid one tier matching
        if (topicName.contains(BrokerConstant.ShareSubscribe.SUBSCRIBE_ONE_TIER)) {
            if (topicName.indexOf(BrokerConstant.ShareSubscribe.SUBSCRIBE_ONE_TIER) !=
                    topicName.indexOf(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT + BrokerConstant.ShareSubscribe.SUBSCRIBE_ONE_TIER))
                return false;
        }
        return true;
    }

    public static boolean matchingTopic(String originTopic, String topicFilter) {
        String[] topicFilters = topicFilter.split(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT);
        String[] originTopics = originTopic.split(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT);
        if (topicFilters.length > originTopics.length) return false;
        String matchingTopic = IntStream.range(0, topicFilters.length).mapToObj(i -> {
            String tier = topicFilters[i];
            if (Objects.equals(tier, BrokerConstant.ShareSubscribe.SUBSCRIBE_ONE_TIER) ||
                    Objects.equals(tier, BrokerConstant.ShareSubscribe.SUBSCRIBE_MULTIPLE_TIER)) return tier;
            return originTopics[i];
        }).collect(Collectors.joining(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT));
        return Objects.equals(matchingTopic, topicFilter);
    }


}
