package org.mini.agent.sdk.core.event;

import lombok.Data;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
@Data
public class MpscTopicItem {
    private String topic;
    private String name;
    private String callback;
}
