package org.socialhistoryservices.delivery;

import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Abstract class to be extended by any service building templates.
 */
public abstract class TemplatePreparation {
    /**
     * The freemarker configuration holder.
     */
    @Autowired
    private FreeMarkerConfig fConfig;

    /**
     * The source to get localized messages of.
     */
    @Autowired
    private MessageSource msgSource;

    /**
     * The property files.
     */
    @Autowired
    @Qualifier("myCustomProperties")
    protected Properties properties;

    /**
     * Get a localized message.
     *
     * @param code   The code of the message.
     * @param def    The default to return.
     * @param locale The locale to get the message in.
     * @return The message if found, or the default.
     */
    protected String getMessage(String code, String def, Locale locale) {
        return msgSource.getMessage(code, null, def, locale);
    }

    /**
     * Get a localized message.
     *
     * @param code The code of the message.
     * @param def  The default to return.
     * @return The message if found, or the default.
     */
    protected String getMessage(String code, String def) {
        return getMessage(code, def, LocaleContextHolder.getLocale());
    }

    /**
     * Parse a template to a string.
     *
     * @param name  The name of the template.
     * @param model The model to pass to the template.
     * @return The template contents.
     * @throws TemplatePreparationException Thrown when template parsing fails.
     */
    protected String templateToString(String name, Model model) throws TemplatePreparationException {
        return templateToString(name, model, LocaleContextHolder.getLocale());
    }

    /**
     * Parse a template to a string providing the locale.
     *
     * @param name   The name of the template.
     * @param model  The model to pass to the template.
     * @param locale The locale to parse the template in.
     * @return The template contents.
     * @throws TemplatePreparationException Thrown when template parsing fails.
     */
    protected String templateToString(String name, Model model, Locale locale) throws TemplatePreparationException {
        model.addAttribute("msgResolver", new EasyMessage(locale));

        Map map = new HashMap();
        CollectionUtils.mergePropertiesIntoMap(properties, map);
        model.addAllAttributes(map);

        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(
                    fConfig.getConfiguration().getTemplate(name, locale), model
            );
        } catch (TemplateException ex) {
            throw new TemplatePreparationException(ex);
        } catch (IOException ex) {
            throw new TemplatePreparationException(ex);
        }
    }

    /**
     * Get a localized message easily, without need of the spring macro context.
     */
    public class EasyMessage {
        private Locale locale;

        public EasyMessage(Locale l) {
            locale = l;
        }

        /**
         * Get a localized message.
         *
         * @param code The code of the message.
         * @param def  The default to return.
         * @return The message if found, or the default.
         */
        public String getMessage(String code, String def) {
            return msgSource.getMessage(code, null, def, locale);
        }
    }
}
