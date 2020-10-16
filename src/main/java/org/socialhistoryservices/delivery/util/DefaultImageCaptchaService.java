package org.socialhistoryservices.delivery.util;

import com.octo.captcha.engine.CaptchaEngine;
import com.octo.captcha.service.captchastore.FastHashMapCaptchaStore;
import com.octo.captcha.service.image.AbstractManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;

public class DefaultImageCaptchaService extends AbstractManageableImageCaptchaService implements ImageCaptchaService {
    public DefaultImageCaptchaService(CaptchaEngine captchaEngine) {
        super(new FastHashMapCaptchaStore(), captchaEngine,
                180, 100000, 75000);
    }
}
