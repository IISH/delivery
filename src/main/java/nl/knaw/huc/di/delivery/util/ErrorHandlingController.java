package nl.knaw.huc.di.delivery.util;

import nl.knaw.huc.di.delivery.config.InvalidRequestException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Base class to be used to extend controllers from which can throw special runtime exceptions.
 */
public class ErrorHandlingController {

    /**
     * Exception handler for showing invalid request exceptions in a
     * human-readable way.
     *
     * @param exception The exception that was thrown.
     * @param response  The response to send to the user.
     * @return The response body.
     */
    @ExceptionHandler(InvalidRequestException.class)
    @ResponseBody
    public String handleInvalid(Throwable exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return exception.getMessage();
    }

}
