package nl.knaw.huc.di.delivery.services;

import com.octo.captcha.service.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Service
public class DetermineHumanServiceImpl implements DetermineHumanService {
    private final CaptchaService captchaService;
    private final MessageSource messageSource;

    public DetermineHumanServiceImpl(CaptchaService captchaService, MessageSource messageSource) {

        this.captchaService = captchaService;
        this.messageSource = messageSource;
    }

    @Override
    public void checkCaptcha(HttpServletRequest req, BindingResult result, Model model) {
        boolean isCaptchaCorrect = false;
        String id = req.getSession().getId();
        String responseField = req.getParameter("captcha_response_field");

        try {
            if (responseField != null) {
                isCaptchaCorrect = captchaService.validateResponseForID(id, responseField);
            }
        }
        finally {
            if (!isCaptchaCorrect) {
                String msg = messageSource.getMessage("captcha.error", null, LocaleContextHolder.getLocale());

                // This prevents the createOrEdit from submitting to the database.
                // Sadly, because the captcha is not part of the model,
                // no corresponding error will be displayed in the form. We have to do this manually.
                result.addError(new FieldError(result.getObjectName(), "captcha_response_field", "",
                        false, null, null, msg));
                model.addAttribute("captchaError", msg);
            }
        }
    }
}
