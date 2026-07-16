package nl.knaw.huc.di.delivery.home;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/403")
public class AccessDeniedController {

    /**
     * Show a 403 page.
     *
     * @return The view to resolve.
     */
    @RequestMapping(value = "/")
    public String index() {
        return "error/403";
    }
}
