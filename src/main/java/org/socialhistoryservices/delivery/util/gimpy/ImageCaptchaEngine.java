package org.socialhistoryservices.delivery.util.gimpy;

import com.octo.captcha.Captcha;
import com.octo.captcha.CaptchaFactory;
import com.octo.captcha.engine.CaptchaEngine;
import com.octo.captcha.engine.CaptchaEngineException;

import java.security.SecureRandom;
import java.util.*;

public abstract class ImageCaptchaEngine implements CaptchaEngine {
    protected List<CaptchaFactory> factories = new ArrayList<>();
    protected Random myRandom = new SecureRandom();

    public ImageCaptchaEngine() {
    }

    public ImageCaptchaFactory getImageCaptchaFactory() {
        return (ImageCaptchaFactory) this.factories.get(this.myRandom.nextInt(this.factories.size()));
    }

    public final ImageCaptcha getNextImageCaptcha() {
        return this.getImageCaptchaFactory().getImageCaptcha();
    }

    public ImageCaptcha getNextImageCaptcha(Locale locale) {
        return this.getImageCaptchaFactory().getImageCaptcha(locale);
    }

    public final Captcha getNextCaptcha() {
        return this.getImageCaptchaFactory().getImageCaptcha();
    }

    public Captcha getNextCaptcha(Locale locale) {
        return this.getImageCaptchaFactory().getImageCaptcha(locale);
    }

    public CaptchaFactory[] getFactories() {
        return this.factories.toArray(new CaptchaFactory[this.factories.size()]);
    }

    public void setFactories(CaptchaFactory[] factories) throws CaptchaEngineException {
        this.checkNotNullOrEmpty(factories);
        this.factories = Arrays.asList(factories);
    }

    protected void checkNotNullOrEmpty(CaptchaFactory[] factories) {
        if (factories == null || factories.length == 0) {
            throw new CaptchaEngineException("impossible to set null or empty factories");
        }
    }
}