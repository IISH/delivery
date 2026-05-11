package nl.knaw.huc.di.delivery.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.HashMap;
import java.util.Map;

/**
 * Overrides the default spring-boot configuration to allow adding shared variables to the freemarker context
 */
@Configuration
public class FreemarkerConfiguration implements BeanPostProcessor {
    private final DeliveryProperties deliveryProperties;
    private final InfoEndpoint infoEndpoint;

    public FreemarkerConfiguration(DeliveryProperties deliveryProperties, InfoEndpoint infoEndpoint) {
        this.deliveryProperties = deliveryProperties;
        this.infoEndpoint = infoEndpoint;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FreeMarkerConfigurer configurer) {
            Map<String, Object> sharedVariables = new HashMap<>();
            sharedVariables.put("delivery", deliveryProperties);

            if (infoEndpoint.info().containsKey("git")) {
                sharedVariables.put("git", infoEndpoint.info().get("git"));
            }

            configurer.setFreemarkerVariables(sharedVariables);
        }
        return bean;
    }
}