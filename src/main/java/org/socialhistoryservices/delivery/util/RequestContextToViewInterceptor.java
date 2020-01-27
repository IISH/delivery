package org.socialhistoryservices.delivery.util;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Inserts the request context attribute into the model. This class is needed
 * because there is no other way to achieve setting URLDecode to false when retrieving context URIs.
 */
public class RequestContextToViewInterceptor extends HandlerInterceptorAdapter {
    /**
     * Expose the request context to the views.
     */
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView model) {
        if (model != null) {
            UrlPathHelper uph = new UrlPathHelper();
            uph.setUrlDecode(false);
            uph.setDefaultEncoding("utf-8");
            RequestContext rc = new RequestContext(request);
            rc.setUrlPathHelper(uph);
            model.addObject("rc", rc);
        }
    }
}
