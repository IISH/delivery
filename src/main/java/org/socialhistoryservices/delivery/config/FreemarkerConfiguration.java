package org.socialhistoryservices.delivery.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.HashMap;
import java.util.Map;

/**
 * Overrides the default spring-boot configuration to allow adding shared variables to the freemarker context
 */
@Configuration
@EnableConfigurationProperties(DeliveryProperties.class)
public class FreemarkerConfiguration implements BeanPostProcessor {
    @Autowired
    private DeliveryProperties deliveryProperties;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FreeMarkerConfigurer) {
            FreeMarkerConfigurer configurer = (FreeMarkerConfigurer) bean;
            Map<String, Object> sharedVariables = new HashMap<>();
            sharedVariables.put("delivery", deliveryProperties);
            configurer.setFreemarkerVariables(sharedVariables);
        }
        return bean;
    }
}
