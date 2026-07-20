package nl.knaw.huc.di.delivery.config;

import com.octo.captcha.service.image.ImageCaptchaService;
import nl.knaw.huc.di.delivery.user.service.CustomOidcUserService;
import nl.knaw.huc.di.delivery.user.service.LocalUserServiceImpl;
import nl.knaw.huc.di.delivery.util.CaptchaEngine;
import nl.knaw.huc.di.delivery.util.DefaultImageCaptchaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static jakarta.servlet.DispatcherType.ERROR;
import static jakarta.servlet.DispatcherType.FORWARD;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final CustomOidcUserService oAuth2UserService;

    private final LocalUserServiceImpl localUserService;

    public SecurityConfiguration(CustomOidcUserService oAuth2UserService, LocalUserServiceImpl localUserService) {
        this.oAuth2UserService = oAuth2UserService;
        this.localUserService = localUserService;
    }

    @Bean
    @Profile("production")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // URLs are allowed by any authenticated user.
                .authorizeHttpRequests(authorize ->
                        authorize.requestMatchers("/css/**",
                                        "/js/**",
                                        "/logo.ico",
                                        "/favicon.ico",
                                        "/favicon-16x16.png",
                                        "/favicon-32x32.png",
                                        "/403",
                                        "/error",
                                        "/record/10622*",
                                        "/permission/createform/*",
                                        "/reservation/createform/*",
                                        "/reproduction/createform/*",
                                        "/reproduction/confirm/*",
                                        "/reproduction/order/*",
                                        "/captcha",
                                        "/").permitAll().
                                requestMatchers("/actuator/**").hasRole("ACTUATOR"). // internal monitoring management
                                anyRequest().hasRole("DELIVERY_USER") // Minimal role
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/403") // Redirects 403 to this endpoint
                )
                // Disable HTTP Basic authentication
                .httpBasic(HttpBasicConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint((userInfo) -> userInfo
                                .oidcUserService(oAuth2UserService))
                        .defaultSuccessUrl("/"))
                .oauth2Client(withDefaults())
                .logout(withDefaults());
        return http.build();
    }

    @Bean
    @Profile("development")
    public SecurityFilterChain securityFilterChainLocal(HttpSecurity http, LocalUserServiceImpl localUserServiceImpl) throws Exception {

        http
                // URLs are allowed by any authenticated user.
                .authorizeHttpRequests(authorize ->
                        authorize
                                .dispatcherTypeMatchers(FORWARD, ERROR).permitAll()
                                .requestMatchers("/css/**", "/js/**", "/logo.ico", "/favicon.ico", "/favicon-16x16.png", "/favicon-32x32.png").permitAll()
                                .requestMatchers("/h2-console/**").permitAll()
                                .anyRequest().authenticated()
                ).formLogin(withDefaults())
                .logout(withDefaults())
                .userDetailsService(localUserService)
                // Disable Cross-Site Request Forgery token
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        return http.build();
    }

    @Bean
    public ImageCaptchaService captchaService() {
        return new DefaultImageCaptchaService(new CaptchaEngine());
    }
}