package org.mini.agent.sdk.core.event;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
@Data
@AllArgsConstructor
public class MpscTopics {
    private Collection<MpscTopicItem> topics;
}
