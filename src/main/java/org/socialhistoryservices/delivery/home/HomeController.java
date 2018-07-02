package org.socialhistoryservices.delivery.home;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * Default controller when / is accessed.
 */
@Controller
public class HomeController {

    /**
     * Show a home overview page.
     * @param request The HTTP request to use.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String list(HttpServletRequest request) {
        return "home";
    }

}
