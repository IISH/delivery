package org.socialhistoryservices.delivery.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.ui.Model;

import java.util.Locale;

/**
 * Abstract class to be extended by mailers.
 */
public abstract class Mailer extends TemplatePreparation {
    /**
     * The object to use for sending mail.
     */
    @Autowired
    protected JavaMailSender mailSender;

    /**
     * Parse a template to a string.
     *
     * @param name  The name of the template.
     * @param model The model to pass to the template.
     * @return The template contents.
     * @throws org.springframework.mail.MailPreparationException Thrown when template parsing fails.
     */
    protected String templateToString(String name, Model model) throws MailPreparationException {
        try {
            return super.templateToString(name, model);
        } catch (TemplatePreparationException mpe) {
            throw new MailPreparationException(mpe);
        }
    }

    /**
     * Parse a template to a string providing the locale.
     *
     * @param name   The name of the template.
     * @param model  The model to pass to the template.
     * @param locale The locale to parse the template in.
     * @return The template contents.
     * @throws org.springframework.mail.MailPreparationException Thrown when template parsing fails.
     */
    protected String templateToString(String name, Model model, Locale locale) throws MailPreparationException {
        try {
            return super.templateToString(name, model, locale);
        } catch (TemplatePreparationException mpe) {
            throw new MailPreparationException(mpe);
        }
    }
}
