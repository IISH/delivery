package org.socialhistoryservices.delivery.config;

import org.h2.server.web.WebServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.text.SimpleDateFormat;

@Configuration
@EnableConfigurationProperties(DeliveryProperties.class)
public class ConfigConfiguration {

    @Autowired
    private DeliveryProperties deliveryProperties;

    @Bean
    public SimpleDateFormat dateFormat(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(deliveryProperties.getDateFormat());
        simpleDateFormat.setLenient(false);
        return simpleDateFormat;
    }

    @Bean
    @Profile("development")
    public ServletRegistrationBean h2ConsoleServletRegistration() {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(new WebServlet());
        registrationBean.addUrlMappings("/console/*");
        return registrationBean;
    }
}
