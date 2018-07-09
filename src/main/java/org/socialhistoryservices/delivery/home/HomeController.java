package org.socialhistoryservices.delivery.home;

import org.socialhistoryservices.delivery.config.PrinterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Default controller when / is accessed.
 */
@Controller
public class HomeController {
    @Autowired
    private PrinterConfiguration printerConfiguration;

    /**
     * Show a home overview page.
     *
     * @param model The model.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String list(Model model) {
        model.addAttribute("printer", printerConfiguration.getState().name());
        return "home";
    }

    /**
     * Setup the printer configuration.
     *
     * @param printer The printer to use.
     * @param model   The model.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/", method = RequestMethod.POST, params = "printerSubmit")
    @PreAuthorize("isAuthenticated()")
    public String list(@ModelAttribute("printer") String printer, Model model) {
        PrinterConfiguration.PrinterState state = PrinterConfiguration.PrinterState.valueOf(printer);
        printerConfiguration.setState(state);

        model.addAttribute("printer", printerConfiguration.getState().name());

        return "home";
    }
}
