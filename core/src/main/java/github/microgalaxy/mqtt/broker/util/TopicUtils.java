package github.microgalaxy.mqtt.broker.util;

import github.microgalaxy.mqtt.broker.client.Subscribe;
import github.microgalaxy.mqtt.broker.config.BrokerConstant;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
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
        if (StringUtils.startsWithIgnoreCase(topicName, "$"))
            return StringUtils.startsWithIgnoreCase(topicName,
                    BrokerConstant.ShareSubscribe.SUBSCRIBE_SHARE_PREFIX + BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT)
                    && validTopic(shareToBaseTopic(topicName));

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
        AtomicBoolean multipleTierMatch = new AtomicBoolean(false);
        String matchingTopic = IntStream.range(0, publishTopics.length).mapToObj(i -> {
            if (multipleTierMatch.get() && i >= subscriptTopics.length) return publishTopics[i];
            String tier = subscriptTopics[i];
            if (Objects.equals(tier, BrokerConstant.ShareSubscribe.SUBSCRIBE_ONE_TIER)) return publishTopics[i];
            if (Objects.equals(tier, BrokerConstant.ShareSubscribe.SUBSCRIBE_MULTIPLE_TIER)) {
                multipleTierMatch.set(true);
                return publishTopics[i];
            }
            return tier;
        }).collect(Collectors.joining(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT));
        return Objects.equals(matchingTopic, publishTopic);
    }

    public static boolean matchingShareTopic(String subscriptTopic, String publishTopic) {
        String baseSubscriptTopic = shareToBaseTopic(subscriptTopic);
        return matchingTopic(baseSubscriptTopic, publishTopic);
    }

    /**
     * filter shareTopic、normalTopic
     *
     * @param shareSubscribes
     * @param subscribes
     * @return
     */
    public static Collection<Subscribe> filterTopic(Collection<Subscribe> shareSubscribes, Collection<Subscribe> subscribes) {
        if (CollectionUtils.isEmpty(shareSubscribes)) return subscribes;
        Collection<Subscribe> shareSubscribeList = shareSubscribes.stream()
                .collect(Collectors.groupingBy(Subscribe::getTopic,
                        Collectors.collectingAndThen(Collectors.toList(), randomClient(subscribes)))).values();
        if (CollectionUtils.isEmpty(subscribes)) return shareSubscribeList;
        subscribes.addAll(shareSubscribeList);
        return subscribes;
    }


    private static Function<List<Subscribe>, Subscribe> randomClient(Collection<Subscribe> subscribes) {
        Map<String, List<Subscribe>> subscribeMap = subscribes.stream()
                .collect(Collectors.groupingBy(k -> String.join("-", k.getTopic(), k.getClientId()), Collectors.toList()));
        return l -> {
            //filter share subscribe
            l.forEach(v -> {
                List<Subscribe> removeSubscribe = subscribeMap.get(String.join("-", shareToBaseTopic(v.getTopic()), v.getClientId()));
                if (!CollectionUtils.isEmpty(removeSubscribe)) subscribes.removeAll(removeSubscribe);
            });
            Subscribe shareSubscribe = l.get(ThreadLocalRandom.current().nextInt(0, l.size()) % l.size());
            List<Subscribe> subscribe = Optional.ofNullable(subscribeMap.get(String.join("-",
                    shareToBaseTopic(shareSubscribe.getTopic()), shareSubscribe.getClientId())))
                    .orElse(Collections.emptyList());
            if (CollectionUtils.isEmpty(subscribe)) return shareSubscribe;
            return shareSubscribe.getSubTimestamp() > subscribe.get(0).getSubTimestamp() ? shareSubscribe : subscribe.get(0);
        };
    }

    private static String shareToBaseTopic(String shareTopic) {
        // $share/{group}
        int shareTier = 2;
        String[] shareTopicSplit = shareTopic.split(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT);
        return IntStream.range(0, shareTopicSplit.length).mapToObj(i -> {
            if (i < shareTier) return Strings.EMPTY;
            return shareTopicSplit[i];
        }).filter(v -> !Objects.equals(v, Strings.EMPTY))
                .collect(Collectors.joining(BrokerConstant.ShareSubscribe.SUBSCRIBE_TIER_SPLIT));
    }
}
