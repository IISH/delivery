package nl.knaw.huc.di.delivery.user.controller;

import nl.knaw.huc.di.delivery.user.dao.GroupRepository;
import nl.knaw.huc.di.delivery.user.dao.UserRepository;
import nl.knaw.huc.di.delivery.config.InvalidRequestException;
import nl.knaw.huc.di.delivery.user.entity.Group;
import nl.knaw.huc.di.delivery.user.entity.User;
import org.mockito.internal.exceptions.ExceptionIncludingMockitoWarnings;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

/**
 * Controller which handles all /user/* requests
 */
@Controller
@RequestMapping(value = "/user")
@Secured("ROLE_USER_MODIFY")
public class UserController {


    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public UserController(UserRepository userRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    /**
     * Get the list of users to manage.
     *
     * @param model The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("groups", groupRepository.findAll());
        return "user_management";
    }

    /**
     * Display a logout page.
     *
     * @return The view to resolve.
     */
    @RequestMapping(value = "/logout-success", method = RequestMethod.GET)
    public String logoutSuccess() {
        return "user_logout_success";
    }

    /**
     * Change the group a user is in.
     *
     * @param user   The user id.
     * @param groups The group ids.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/", method = RequestMethod.POST, params = "action=chgrp")
    public String chgrp(@RequestParam int user, @RequestParam(defaultValue = "") int[] groups) {
        Optional<User> ou = userRepository.findById(user);
        User userObj = ou.orElseThrow(() -> new InvalidRequestException("Invalid user id specified."));

        userObj.getGroups().clear();

        if (groups.length == 0) {
            userRepository.delete(userObj);
        } else {
            for (int grpID : groups) {
                Optional<Group> ogrp = groupRepository.findById(grpID);
                ogrp.ifPresent(userObj.getGroups()::add);
            }
            userRepository.save(userObj);
        }
        return "redirect:/user/";
    }
}
