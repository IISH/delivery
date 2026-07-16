package nl.knaw.huc.di.delivery.config;

import nl.knaw.huc.di.delivery.api.IISHRecordLookupService;
import nl.knaw.huc.di.delivery.api.PaymentService;
import nl.knaw.huc.di.delivery.api.SharedObjectRepositoryService;
import nl.knaw.huc.di.delivery.user.controller.SecurityToViewInterceptor;
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
    private final DeliveryProperties deliveryProperties;

    public RootContextConfiguration(DeliveryProperties deliveryProperties) {
        this.deliveryProperties = deliveryProperties;
    }

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
