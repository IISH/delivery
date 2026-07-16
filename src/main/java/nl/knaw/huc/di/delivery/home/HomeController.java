package nl.knaw.huc.di.delivery.home;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Default controller when / is accessed.
 * Any authennticated user can access this page
 */
@Controller
@PreAuthorize("isAuthenticated()")
public class HomeController {

    /**
     * Show a home overview page.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String list() {
        return "home";
    }
}
