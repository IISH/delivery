package org.socialhistoryservices.delivery.user.controller;

import org.socialhistoryservices.delivery.util.ErrorHandlingController;
import org.socialhistoryservices.delivery.util.InvalidRequestException;
import org.socialhistoryservices.delivery.user.entity.Group;
import org.socialhistoryservices.delivery.user.entity.User;
import org.socialhistoryservices.delivery.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller which handles all /user/* requests
 */
@Controller
@Transactional
@RequestMapping(value = "/user")
public class UserController extends ErrorHandlingController {

    @Autowired
    @Qualifier("userDetailsService")
    private UserService users;

    /**
     * The login page.
     * @param error An error message.
     * @param model The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
//            String msg = msgSource.getMessage("security.error", null, "Invalid username and password!",
            String msg = messageSource.getMessage("security.error", null, "Invalid username and password!",
                    LocaleContextHolder.getLocale());
            model.addAttribute("error", msg);
        }
        return "user_login";
    }

    /**
     * Get the list of users to manage.
     * @param model The model to add attributes to.
     * @param request The HTTP request.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER_MODIFY')")
    public String list(Model model, HttpServletRequest request) {
        model.addAttribute("users", users.listUsers());
        model.addAttribute("groups", users.listGroups());
        return "user_management";
    }

    /**
     * Display a logout page.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/logout-success", method = RequestMethod.GET)
    public String logoutSuccess() {
        return "user_logout_success";
    }

    /**
     * Change the group a user is in.
     * @param user The user id.
     * @param groups The group ids.
     * @param model The model to add attributes to.
     * @param request The HTTP request.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/", method = RequestMethod.POST,
                    params = "action=chgrp")
    @PreAuthorize("hasRole('ROLE_USER_MODIFY')")
    public String chgrp(
            @RequestParam int user,
            @RequestParam(required = false) int[] groups,
            Model model, HttpServletRequest request) {

        User userObj = users.getUserById(user);

        if (userObj == null) {
            throw new InvalidRequestException("Invalid user id specified.");
        }

        UserDetails currentUserDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        User currentUser = users.getUserByName(currentUserDetails.getUsername());

        if ((currentUser != null) && (userObj.getId() == currentUser.getId())) {
            throw new InvalidRequestException("Cannot modify own user groups.");
        }


        userObj.getGroups().clear();

        if (groups != null) {
            for (int grpID : groups) {
                Group grp = users.getGroupById(grpID);
                userObj.getGroups().add(grp);
            }
        }

        users.saveUser(userObj);
        return "redirect:/user/";
    }
}
