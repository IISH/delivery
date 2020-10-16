package org.socialhistoryservices.delivery.util.gimpy;

import com.octo.captcha.Captcha;
import com.octo.captcha.CaptchaFactory;

import java.util.Locale;

public abstract class ImageCaptchaFactory implements CaptchaFactory {
    public ImageCaptchaFactory() {
    }

    public final Captcha getCaptcha() {
        return this.getImageCaptcha();
    }

    public final Captcha getCaptcha(Locale locale) {
        return this.getImageCaptcha(locale);
    }

    public abstract ImageCaptcha getImageCaptcha();

    public abstract ImageCaptcha getImageCaptcha(Locale var1);
}
