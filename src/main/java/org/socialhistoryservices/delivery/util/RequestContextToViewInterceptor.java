package org.socialhistoryservices.delivery.util;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Inserts the request context attribute into the model. This class is needed
 * because there is no other way to achieve setting URLDecode to false when retrieving context URIs.
 */
public class RequestContextToViewInterceptor implements AsyncHandlerInterceptor {

    final String release;
    final String profile;

    public RequestContextToViewInterceptor(String release, String profile) {
        this.release = release;
        this.profile = profile;
    }

    /**
     * Expose the request context to the views.
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

            model.addObject("profile", profile);
            model.addObject("release", release);
        }
    }
}
