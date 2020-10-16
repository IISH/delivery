package org.socialhistoryservices.delivery.util.gimpy;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public class Gimpy extends ImageCaptcha implements Serializable {
    private final String response;

    Gimpy(String question, BufferedImage challenge, String response) {
        super(question, challenge);
        this.response = response;
    }

    public final Boolean validateResponse(Object response) {
        return response instanceof String && this.validateResponse((String) response);
    }

    private boolean validateResponse(String response) {
        return response.equals(this.response);
    }
}
