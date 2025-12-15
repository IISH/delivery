package org.socialhistoryservices.delivery.config;

import org.socialhistoryservices.delivery.util.RequestContextToViewInterceptor;
import org.socialhistoryservices.delivery.api.IISHRecordLookupService;
import org.socialhistoryservices.delivery.api.PaymentService;
import org.socialhistoryservices.delivery.api.SharedObjectRepositoryService;
import org.socialhistoryservices.delivery.user.controller.SecurityToViewInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

@Configuration
@EnableConfigurationProperties(DeliveryProperties.class)
public class RootContextConfiguration implements WebMvcConfigurer {
    @Autowired
    DeliveryProperties deliveryProperties;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(secIntercept());
        registry.addInterceptor(reqIntercept());
    }

    @Bean
    public IISHRecordLookupService myLookupService() {
        IISHRecordLookupService iishRecordLookupService = new IISHRecordLookupService();
        iishRecordLookupService.setDeliveryProperties(deliveryProperties);
        return iishRecordLookupService;
    }

    @Bean
    public PaymentService paymentService() {
        return new PaymentService(
                deliveryProperties.getUrlSelf(),
                deliveryProperties.getMollieApiKey(),
                deliveryProperties.getMollieProfile());
    }

    @Bean
    public SharedObjectRepositoryService sharedObjectRepositoryService() {
        return new SharedObjectRepositoryService(deliveryProperties.getSorAddress());
    }

    @Bean
    public SecurityToViewInterceptor secIntercept() {
        return new SecurityToViewInterceptor();
    }

    @Bean
    public RequestContextToViewInterceptor reqIntercept() {
        return new RequestContextToViewInterceptor(deliveryProperties);
    }
}
