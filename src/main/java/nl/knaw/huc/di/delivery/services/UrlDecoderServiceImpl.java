package nl.knaw.huc.di.delivery.services;

import nl.knaw.huc.di.delivery.config.DeliveryProperties;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
public class UrlDecoderServiceImpl implements UrlDecoderService {

    private final DeliveryProperties deliveryProperties;

    public UrlDecoderServiceImpl(DeliveryProperties deliveryProperties) {
        this.deliveryProperties = deliveryProperties;
    }

    @Override
    public String[] getPidsFromURL(String pids) {
        return URLDecoder.decode(pids, StandardCharsets.UTF_8).split(deliveryProperties.getPidSeparator());

    }
}
