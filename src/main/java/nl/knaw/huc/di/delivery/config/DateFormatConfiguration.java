package nl.knaw.huc.di.delivery.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.text.SimpleDateFormat;

@Configuration
@EnableConfigurationProperties(DeliveryProperties.class)
public class DateFormatConfiguration {
    private final DeliveryProperties deliveryProperties;

    public DateFormatConfiguration(DeliveryProperties deliveryProperties) {
        this.deliveryProperties = deliveryProperties;
    }

    @Bean
    public SimpleDateFormat dateFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(deliveryProperties.getDateFormat());
        simpleDateFormat.setLenient(false);
        return simpleDateFormat;
    }
}