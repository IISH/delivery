/**
 * Copyright (C) 2013 International Institute of Social History
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.socialhistoryservices.delivery;

import com.octo.captcha.service.CaptchaService;
import com.octo.captcha.service.CaptchaServiceException;
import org.socialhistoryservices.delivery.config.DeliveryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base class to be used to extend controllers from which can throw special
 * runtime exceptions.
 */
public class ErrorHandlingController {

    @Autowired
    private SimpleDateFormat df;

    @Autowired
    protected MessageSource msgSource;

    @Autowired
    protected CaptchaService captchaService;

    @Autowired
    protected DeliveryProperties deliveryProperties;

    /**
     * Split a set of pids given in a url to an array of pids.
     * @param pids The combined pids (URL encoded).
     * @return Separate pids.
     */
    protected String[] getPidsFromURL(String pids) {
        try {
            return URLDecoder.decode(pids, "utf-8").split(deliveryProperties.getPidSeperator());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(df,
                true));
        binder.registerCustomEditor(String.class, new StringTrimmerEditor
                (true));
    }


    /**
     * Exception handler for showing invalid request exceptions in a
     * human-readable way.
     * @param exception The exception that was thrown.
     * @param response The response to send to the user.
     * @return The response body.
     */
    @ExceptionHandler(InvalidRequestException.class)
    @ResponseBody
    public String handleInvalid(Throwable exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return exception.getMessage();
    }

    protected void checkCaptcha(HttpServletRequest req, BindingResult result, Model model) {
	    boolean isCaptchaCorrect = false;
	    String id = req.getSession().getId();
	    String responseField = req.getParameter("captcha_response_field");

	    try {
		    if (responseField != null) {
			    isCaptchaCorrect = captchaService.validateResponseForID(id, responseField);
		    }
	    }
	    catch (CaptchaServiceException e) {
		    // Should not happen, may be thrown if the id is not valid
		    isCaptchaCorrect = false;
	    }
	    finally {
		    if (!isCaptchaCorrect) {
			    String msg = msgSource.getMessage("captcha.error", null, LocaleContextHolder.getLocale());

			    // This prevents the createOrEdit from submitting to the database. Sadly, because the captcha is not part of the model,
			    // no corresponding error will be displayed in the form. We have to do this manually.
			    result.addError(
					    new FieldError(result.getObjectName(), "captcha_response_field", "", false, null, null, msg));
			    model.addAttribute("captchaError", msg);
		    }
	    }
    }
}
