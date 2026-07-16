package nl.knaw.huc.di.delivery.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.info.GitProperties;
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
    private final GitProperties gitProperties;

    public FreemarkerConfiguration(DeliveryProperties deliveryProperties, GitProperties gitProperties
    ) {
        this.deliveryProperties = deliveryProperties;
        this.gitProperties = gitProperties;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FreeMarkerConfigurer configurer) {
            Map<String, Object> sharedVariables = new HashMap<>();
            sharedVariables.put("delivery", deliveryProperties);

            Map<String, String> git = new HashMap<>(2);
            git.put("tag", gitProperties.get("closest.tag.name"));
            git.put("commit", gitProperties.getCommitId());
            sharedVariables.put("git", git);
            configurer.setFreemarkerVariables(sharedVariables);
        }
        return bean;
    }
}