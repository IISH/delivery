package nl.knaw.huc.di.delivery.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

public interface DetermineHumanService {
    void checkCaptcha(HttpServletRequest req, BindingResult result, Model model);
}
