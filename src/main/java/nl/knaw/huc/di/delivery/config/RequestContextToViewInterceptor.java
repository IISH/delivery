package nl.knaw.huc.di.delivery.config;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.util.UrlPathHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Inserts the request context attribute into the model. This class is needed
 * because there is no other way to achieve setting URLDecode to false when retrieving context URIs.
 */
public class RequestContextToViewInterceptor implements AsyncHandlerInterceptor {

    DeliveryProperties properties;

    public RequestContextToViewInterceptor(DeliveryProperties properties) {
        this.properties = properties;
    }

    /**
     * Initializes the model for use with an overview.
     *
     * @param model The model.
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView model) {
        if (model != null) {
            UrlPathHelper uph = new UrlPathHelper();
            uph.setUrlDecode(false);
            uph.setDefaultEncoding("utf-8");
            RequestContext rc = new RequestContext(request);
            rc.setUrlPathHelper(uph);
            model.addObject("rc", rc);

            Calendar cal = GregorianCalendar.getInstance();
            model.addObject("today", cal.getTime());

            cal.add(Calendar.MONTH, -3);
            model.addObject("min3months", cal.getTime());

            cal.add(Calendar.MONTH, 3);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            model.addObject("tomorrow", cal.getTime());
        }
    }
}
