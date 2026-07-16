package nl.knaw.huc.di.delivery.printer;

import nl.knaw.huc.di.delivery.config.PrinterConfiguration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Default controller when /printer is accessed.
 */
@Controller
@RequestMapping(value = "/printer")
@PreAuthorize("hasRole('PRINTER_VIEW')")
public class PrinterController {
    private final PrinterConfiguration printerConfiguration;

    public PrinterController(PrinterConfiguration printerConfiguration) {
        this.printerConfiguration = printerConfiguration;
    }

    /**
     * Show a home overview page.
     *
     * @param model The model.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("printer", printerConfiguration.getState().name());
        return "printer";
    }

    /**
     * Setup the printer configuration.
     *
     * @param printer The printer to use.
     * @param model   The model.
     * @return The view to resolve.
     */
    @PreAuthorize("hasRole('PRINTER_MODIFY')")
    @RequestMapping(value = "/", method = RequestMethod.POST, params = "printerSubmit")
    public String list(@ModelAttribute("printer") String printer, Model model) {
        PrinterConfiguration.PrinterState state = PrinterConfiguration.PrinterState.valueOf(printer);
        printerConfiguration.setState(state);

        model.addAttribute("printer", printerConfiguration.getState().name());

        return "printer";
    }

}