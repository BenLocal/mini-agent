package org.mini.agent.sdk.spring;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mini.agent.sdk.core.event.MpscTopicItem;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
public final class MpscTopicItemsStore {
    private MpscTopicItemsStore() {
    }

    private static Map<String, MpscTopicItem> topics = new ConcurrentHashMap<>();

    public static void add(MpscTopicItem item) {
        topics.put(item.getTopic(), item);
    }

    public static Collection<MpscTopicItem> getAll() {
        return topics.values();
    }
}
