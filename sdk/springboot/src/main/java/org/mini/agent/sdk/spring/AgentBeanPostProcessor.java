package org.mini.agent.sdk.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.mini.agent.sdk.core.Mpsc;
import org.mini.agent.sdk.core.event.MpscTopicItem;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 
 * @Author shiben
 * @Date 2023年8月02日
 * @Version 1.0
 *
 */
@Component
public class AgentBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean != null) {
            selectMpscAnnotation(bean.getClass());
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    private void selectMpscAnnotation(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            Mpsc mpsc = method.getAnnotation(Mpsc.class);
            if (mpsc == null) {
                continue;
            }

            String topicName = mpsc.topic();
            String name = mpsc.name();

            if (StringUtils.hasLength(name) && StringUtils.hasLength(topicName)) {
                List<String> callbacks = getAllCompleteRoutesForPost(clazz, method, topicName);

                for (String callback : callbacks) {
                    MpscTopicItem item = new MpscTopicItem();
                    item.setTopic(topicName);
                    item.setName(name);
                    item.setCallback(confirmLeadingSlash(callback));
                    MpscTopicItemsStore.add(item);
                }
            }
        }
    }

    // copy from dapr java-sdk
    private static List<String> getAllCompleteRoutesForPost(Class<?> clazz, Method method, String topicName) {
        List<String> routesList = new ArrayList<>();
        RequestMapping clazzRequestMapping = clazz.getAnnotation(RequestMapping.class);
        String[] clazzLevelRoute = null;
        if (clazzRequestMapping != null) {
            clazzLevelRoute = clazzRequestMapping.value();
        }
        String[] postValueArray = getRoutesForPost(method, topicName);
        if (postValueArray.length >= 1) {
            for (String postValue : postValueArray) {
                if (clazzLevelRoute != null && clazzLevelRoute.length >= 1) {
                    for (String clazzLevelValue : clazzLevelRoute) {
                        String route = clazzLevelValue + confirmLeadingSlash(postValue);
                        routesList.add(route);
                    }
                } else {
                    routesList.add(postValue);
                }
            }
        }
        return routesList;
    }

    // copy from dapr java-sdk
    private static String[] getRoutesForPost(Method method, String topicName) {
        String[] postValueArray = new String[] { topicName };
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            if (postMapping.path().length >= 1) {
                postValueArray = postMapping.path();
            } else if (postMapping.value().length >= 1) {
                postValueArray = postMapping.value();
            }
        } else {
            RequestMapping reqMapping = method.getAnnotation(RequestMapping.class);
            for (RequestMethod reqMethod : reqMapping.method()) {
                if (reqMethod == RequestMethod.POST) {
                    if (reqMapping.path().length >= 1) {
                        postValueArray = reqMapping.path();
                    } else if (reqMapping.value().length >= 1) {
                        postValueArray = reqMapping.value();
                    }
                    break;
                }
            }
        }
        return postValueArray;
    }

    // copy from dapr java-sdk
    private static String confirmLeadingSlash(String path) {
        if (path != null && path.length() >= 1 && !path.substring(0, 1).equals("/")) {
            return "/" + path;
        }
        return path;
    }
}
