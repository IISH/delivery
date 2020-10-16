package org.socialhistoryservices.delivery.util.gimpy;

import com.octo.captcha.CaptchaException;
import java.util.Arrays;

public abstract class ListImageCaptchaEngine extends ImageCaptchaEngine {
    public ListImageCaptchaEngine() {
        this.buildInitialFactories();
        this.checkFactoriesSize();
    }

    protected abstract void buildInitialFactories();

    public boolean addFactory(ImageCaptchaFactory factory) {
        return factory != null && this.factories.add(factory);
    }

    public void addFactories(ImageCaptchaFactory[] factories) {
        this.checkNotNullOrEmpty(factories);
        this.factories.addAll(Arrays.asList(factories));
    }

    private void checkFactoriesSize() {
        if (this.factories.size() == 0) {
            throw new CaptchaException("This gimpy has no factories. Please initialize it properly with the buildInitialFactory() called by the constructor or the addFactory() mehtod later!");
        }
    }
}
