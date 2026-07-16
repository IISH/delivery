package nl.knaw.huc.di.delivery.permission.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.huc.di.delivery.api.NoSuchPidException;
import nl.knaw.huc.di.delivery.config.DeliveryProperties;
import nl.knaw.huc.di.delivery.request.service.GeneralRequestService;
import nl.knaw.huc.di.delivery.config.InvalidRequestException;
import nl.knaw.huc.di.delivery.config.ResourceNotFoundException;
import nl.knaw.huc.di.delivery.permission.entity.Permission;
import nl.knaw.huc.di.delivery.permission.service.PermissionMailer;
import nl.knaw.huc.di.delivery.permission.service.PermissionSearch;
import nl.knaw.huc.di.delivery.permission.service.PermissionService;
import nl.knaw.huc.di.delivery.record.entity.Record;
import nl.knaw.huc.di.delivery.record.service.RecordService;
import nl.knaw.huc.di.delivery.request.controller.AbstractRequestController;
import nl.knaw.huc.di.delivery.services.DetermineHumanService;
import nl.knaw.huc.di.delivery.services.UrlDecoderService;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller used to handle all incoming requests on /permission/*
 */
@Controller
@RequestMapping("/permission")
public class PermissionController extends AbstractRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionController.class);

    private final PermissionService permissions;
    private final RecordService records;
    private final PermissionMailer pmMailer;
    private final SimpleDateFormat df;
    private final DetermineHumanService determineHumanService;

    public PermissionController(PermissionService permissions, RecordService records, PermissionMailer pmMailer, SimpleDateFormat df, MessageSource messageSource, GeneralRequestService generalRequestService, RecordService recordService, DeliveryProperties deliveryProperties, DetermineHumanService determineHumanService, UrlDecoderService urlDecoderService) {
        super(deliveryProperties, messageSource, generalRequestService, recordService, urlDecoderService);
        this.permissions = permissions;
        this.records = records;
        this.pmMailer = pmMailer;
        this.df = df;
        this.determineHumanService = determineHumanService;
    }

    /**
     * Fetches one specific permission.
     *
     * @param id    ID of the permission to fetch.
     * @param model Passed view model.
     * @param req   The request.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('PERMISSION_MODIFY')")
    public String getSingle(@PathVariable int id, Model model, HttpServletRequest req) {
        Optional<Permission> opm = permissions.findById(id);
        Permission pm = opm.orElseThrow(ResourceNotFoundException::new);
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
    @PreAuthorize("hasRole('PERMISSION_VIEW')")
    public String get(HttpServletRequest req, Model model, Pageable pageable) {
        Map<String, String[]> p = req.getParameterMap();
        CriteriaBuilder cb = permissions.getPermissionCriteriaBuilder();
        PermissionSearch search = new PermissionSearch(cb, p);
        CriteriaQuery<Permission> cq = search.list();
        Page<Permission> pagedItem = permissions.findAll(cq, pageable);
        model.addAttribute("pagedItem", pagedItem);
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
    @PreAuthorize("hasRole('PERMISSION_MODIFY')")
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
    @PreAuthorize("hasRole('PERMISSION_MODIFY')")
    public String formSaveAndFinish(@RequestParam int id, HttpServletRequest req) {
        Permission pm = permissions.getPermissionById(id);
        if (pm == null) {
            throw new InvalidRequestException("No such permission.");
        }
        updateRecordPermissions(pm, req.getParameterMap());

        try {
            pmMailer.mailPermissionOutcome(pm);
        } catch (MailException e) {
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
        } catch (NoSuchPidException e) {
            model.addAttribute("error", "invalid");
            return "permission_error";
        }

        model.addAttribute("record", record);

        if (form == null) {
            form = new PermissionForm();
        } else if (result != null) {
            determineHumanService.checkCaptcha(req, result, model);

            if (!result.hasErrors()) {
                Permission obj = new Permission();
                form.fillInto(obj, df);

                obj.setRecord(record);
                guaranteeUniqueCode(obj);

                permissions.addPermission(obj);

                try {
                    pmMailer.mailConfirmation(obj);
                    pmMailer.mailReadingRoom(obj);
                } catch (MailException e) {
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
     * @return The view to resolve.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    @PreAuthorize("hasRole('PERMISSION_DELETE')")
    public String apiDelete(@PathVariable int id) {
        remove(id);
        return "";
    }

    /**
     * Remove a permission with the delete button in the /permission/[id] form.
     *
     * @param id  The id of the permission to remove.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/process", method = RequestMethod.POST, params = "delete")
    @PreAuthorize("hasRole('PERMISSION_DELETE')")
    public String formDelete(@RequestParam int id) {
        remove(id);
        return "redirect:/permission/";
    }

    /**
     * Remove a permission (POST method, !DELETE in path).
     *
     * @param id  The id of the permission to remove.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/{id}!DELETE", method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("hasRole('PERMISSION_DELETE')")
    public String apiFakeDelete(@PathVariable int id) {
        remove(id);
        return "";
    }
}
