package nl.knaw.huc.di.delivery.config;

import org.apache.tomcat.util.buf.EncodedSolidusHandling;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
/**
 * A central variable is a persistent identifier that is passed along in a url to the controller.
 * E.g. pid=00000%f12345 -> using in http://localhost:8080/record/00000%f12345
 * That would trigger a tomcat exception Invalid URI: [The encoded slash character is not allowed]
 * Hence Spring needs to handle the incoming request.
 *
 * Also see RootContextConfiguration.configurePathMatch
 */
public class TomcatConfig {

    @Bean
    public TomcatConnectorCustomizer connectorCustomizer() {
        return (connector) -> connector.setEncodedSolidusHandling(
                EncodedSolidusHandling.DECODE.getValue());
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        // Explicitly allow semicolons for jsessionid support. OpenID adds the semicolon.
        firewall.setAllowSemicolon(true);
        return firewall;
    }
}