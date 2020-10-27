package org.socialhistoryservices.delivery.permission.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.socialhistoryservices.delivery.api.NoSuchPidException;
import org.socialhistoryservices.delivery.util.InvalidRequestException;
import org.socialhistoryservices.delivery.util.ResourceNotFoundException;
import org.socialhistoryservices.delivery.permission.entity.Permission;
import org.socialhistoryservices.delivery.permission.service.PermissionMailer;
import org.socialhistoryservices.delivery.permission.service.PermissionSearch;
import org.socialhistoryservices.delivery.permission.service.PermissionService;
import org.socialhistoryservices.delivery.record.entity.*;
import org.socialhistoryservices.delivery.record.service.RecordService;
import org.socialhistoryservices.delivery.request.controller.AbstractRequestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller used to handle all incoming requests on /permission/*
 */
@Controller
@Transactional
@RequestMapping("/permission")
public class PermissionController extends AbstractRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionController.class);

    @Autowired
    protected Validator mvcValidator;

    @Autowired
    private PermissionService permissions;

    @Autowired
    private RecordService records;

    @Autowired
    private PermissionMailer pmMailer;

    @Autowired
    private SimpleDateFormat df;

    /**
     * Fetches one specific permission.
     *
     * @param id    ID of the permission to fetch.
     * @param model Passed view model.
     * @param req   The request.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_PERMISSION_MODIFY')")
    public String getSingle(@PathVariable int id, Model model, HttpServletRequest req) {
        Permission pm = permissions.getPermissionById(id);
        if (pm == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("permission", pm);

        return "permission_get";
    }

    /**
     * Get a list of permissions.
     *
     * @param req   The HTTP request object.
     * @param model Passed view model.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_PERMISSION_VIEW')")
    public String get(HttpServletRequest req, Model model) {
        Map<String, String[]> p = req.getParameterMap();
        CriteriaBuilder cb = permissions.getPermissionCriteriaBuilder();

        PermissionSearch search = new PermissionSearch(cb, p);
        CriteriaQuery<Permission> cq = search.list();
        CriteriaQuery<Long> cqCount = search.count();

        // Fetch result set
        List<Permission> matchedPermissions = permissions.listPermissions(cq, getFirstResult(p), getMaxResults(p));
        model.addAttribute("permissions", matchedPermissions);

        long permissionsSize = permissions.countPermissions(cqCount);
        model.addAttribute("permissionsSize", permissionsSize);

        initOverviewModel(model);

        return "permission_get_list";
    }

    /**
     * Guarantee a unique code to be generated for a new permission.
     *
     * @param obj The permission to generate the code for.
     */
    private void guaranteeUniqueCode(Permission obj) {
        do {
            obj.generateCode();
        }
        while (permissions.getPermissionByCode(obj.getCode()) != null);
    }

    /**
     * Updates a list of record permissions (rp.id -> true/false)
     *
     * @param pm The permission to update.
     * @param p  The parameter map in which tuples are stored.
     */
    private void updateRecordPermissions(Permission pm, Map<String, String[]> p) {
        if (p.containsKey("granted") && !p.get("granted")[0].trim().equals("null")) {
            pm.setGranted(p.get("granted")[0].trim().equals("true"));
            pm.setDateGranted(new Date());
        }

        pm.setMotivation(null);
        if (p.containsKey("motivation")) {
            String motivation = p.get("motivation")[0].trim();
            if (!motivation.isEmpty())
                pm.setMotivation(motivation);
        }

        pm.setInvNosGranted(new ArrayList<>());
        if (pm.getRecord().getExternalInfo().getInventory() != null && pm.getDateGranted() != null && pm.getGranted()) {
            if (p.containsKey("invNosGranted") && !p.get("invNosGranted")[0].isEmpty())
                pm.setInvNosGranted(Arrays.asList(p.get("invNosGranted")[0].split("__")));

            if (pm.getInvNosGranted().isEmpty()) {
                pm.setGranted(false);
                pm.setDateGranted(null);
            }
        }

        permissions.savePermission(pm);
    }

    /**
     * Save a permission with the save button in the /permission/[id] form.
     *
     * @param id  The id of the permission to save.
     * @param req The request.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/process", method = RequestMethod.POST, params = "save")
    @PreAuthorize("hasRole('ROLE_PERMISSION_MODIFY')")
    public String formSave(@RequestParam int id, HttpServletRequest req) {
        Permission pm = permissions.getPermissionById(id);
        if (pm == null) {
            throw new InvalidRequestException("No such permission");
        }
        updateRecordPermissions(pm, req.getParameterMap());

        return "redirect:/permission/";
    }

    /**
     * Save a permission and send a message to the requester.
     *
     * @param id  The id of the permission to save.
     * @param req The request.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/process", method = RequestMethod.POST, params = "saveandemail")
    @PreAuthorize("hasRole('ROLE_PERMISSION_MODIFY')")
    public String formSaveAndFinish(@RequestParam int id, HttpServletRequest req) {
        Permission pm = permissions.getPermissionById(id);
        if (pm == null) {
            throw new InvalidRequestException("No such permission.");
        }
        updateRecordPermissions(pm, req.getParameterMap());

        // Notify the requester.
        try {
            if (pm.getDateGranted() != null)
                pmMailer.mailCode(pm);
        }
        catch (MailException e) {
            LOGGER.error("Failed to send email", e);
            throw e;
        }

        return "redirect:/permission/";
    }

    /**
     * Handle permission request form.
     *
     * @param req    The HTTP request.
     * @param pid    The record to request.
     * @param model  The page's model.
     * @param form   (Optional) form that was filled in.
     * @param result (Optional) result of form validation.
     * @return View name to render.
     */
    public String create(HttpServletRequest req, String pid, Model model, PermissionForm form, BindingResult result) {
        Record record;
        try {
            record = records.getRecordByPidAndCreate(pid);
            if (record == null) {
                model.addAttribute("error", "invalid");
                return "permission_error";
            }
        }
        catch (NoSuchPidException e) {
            model.addAttribute("error", "invalid");
            return "permission_error";
        }

        model.addAttribute("record", record);

        if (form == null) {
            form = new PermissionForm();
        }
        else if (result != null) {
            checkCaptcha(req, result, model);

            if (!result.hasErrors()) {
                Permission obj = new Permission();
                form.fillInto(obj, df);

                obj.setRecord(record);
                guaranteeUniqueCode(obj);

                permissions.addPermission(obj);

                try {
                    pmMailer.mailConfirmation(obj);
                    pmMailer.mailReadingRoom(obj);
                }
                catch (MailException e) {
                    LOGGER.error("Failed to send email", e);
                    model.addAttribute("error", "mail");
                }

                return "permission_success";
            }
        }

        if (result == null) {
            model.addAttribute("permission", form);
        }

        return "permission_create";
    }

    /**
     * Form to request permission for a set of records.
     *
     * @param pid   The PID to create a permission for.
     * @param model The model to use.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/createform/{pid}", method = RequestMethod.GET)
    public String createForm(HttpServletRequest req, @PathVariable String pid, Model model) {
        return create(req, pid, model, null, null);
    }

    /**
     * Submitted form to request permission.
     *
     * @param pid        The PIDs to create a permission for.
     * @param permission The permission form submitted.
     * @param result     The result of validating the form.
     * @param model      The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/createform/{pid}", method = RequestMethod.POST)
    public String createForm(HttpServletRequest req, @PathVariable String pid,
                             @ModelAttribute("permission") @Valid PermissionForm permission,
                             BindingResult result, Model model) {
        return create(req, pid, model, permission, result);
    }

    /**
     * Delete permissions.
     *
     * @param id The id of the permission to remove.
     */
    private void remove(int id) {
        Permission pm = permissions.getPermissionById(id);
        if (pm != null) {
            permissions.removePermission(pm);
        }
    }

    /**
     * Remove a permission (DELETE method).
     *
     * @param id  The id of the permission to remove.
     * @param req The request.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_PERMISSION_DELETE')")
    public String apiDelete(@PathVariable int id, HttpServletRequest req) {
        remove(id);
        return "";
    }

    /**
     * Remove a permission with the delete button in the /permission/[id] form.
     *
     * @param id  The id of the permission to remove.
     * @param req The request.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/process", method = RequestMethod.POST, params = "delete")
    @PreAuthorize("hasRole('ROLE_PERMISSION_DELETE')")
    public String formDelete(@RequestParam int id, HttpServletRequest req) {
        remove(id);
        return "redirect:/permission/";
    }

    /**
     * Remove a permission (POST method, !DELETE in path).
     *
     * @param id  The id of the permission to remove.
     * @param req The request.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/{id}!DELETE", method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_PERMISSION_DELETE')")
    public String apiFakeDelete(@PathVariable int id, HttpServletRequest req) {
        remove(id);
        return "";
    }
}
