package org.socialhistoryservices.delivery.util;

import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Compare the path urldecoded.
 */
public class UrlDecodedUrlPathHelper extends UrlPathHelper {

    /**
     * Compares the url decoded path instead of the encoded.
     *
     * @param request The request.
     * @return The difference.
     */
    public String getPathWithinServletMapping(HttpServletRequest request) {
        String pathWithinApp = getPathWithinApplication(request);
        String servletPath = getServletPath(request);
        String decodedPathWithinApp;

        // If the URL decoded pathWithinApp is alright, return it.
        try {
            decodedPathWithinApp = URLDecoder.decode(pathWithinApp, "UTF-8");

            if (decodedPathWithinApp.startsWith(servletPath)) {
                // Normal case: URI contains servlet path.
                return decodedPathWithinApp.substring(servletPath.length());
            }
            else {
                // Special case: URI is different from servlet path.
                // Can happen e.g. with index page: URI="/", servletPath="/index.html"
                // Use path info if available, as it indicates an index page within
                // a servlet mapping. Otherwise, use the full servlet path.
                String pathInfo = request.getPathInfo();
                return (pathInfo != null ? pathInfo : servletPath);
            }
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
