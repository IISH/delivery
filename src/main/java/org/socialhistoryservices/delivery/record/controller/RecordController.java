package org.socialhistoryservices.delivery.record.controller;

import org.socialhistoryservices.delivery.util.ErrorHandlingController;
import org.socialhistoryservices.delivery.util.InvalidRequestException;
import org.socialhistoryservices.delivery.util.ResourceNotFoundException;
import org.socialhistoryservices.delivery.api.NoSuchPidException;
import org.socialhistoryservices.delivery.api.RecordLookupService;
import org.socialhistoryservices.delivery.record.entity.*;
import org.socialhistoryservices.delivery.record.service.NoSuchParentException;
import org.socialhistoryservices.delivery.record.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pages to manage or request record metadata.
 */
@Controller
@Transactional
@RequestMapping(value = "/record")
public class RecordController extends ErrorHandlingController {

    @Autowired
    private RecordService records;

    @Autowired
    private RecordLookupService lookup;

    /**
     * Try to URL encode a string using utf-8 charset.
     *
     * @param s The string to encode
     * @return The url encoded string.
     */
    protected String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Try to URL decode a string using utf-8 charset.
     *
     * @param s The string to decode
     * @return The url decoded string.
     */
    protected String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // {{{ Get API

    /**
     * Get information about records.
     *
     * @param pids     The persistent identifiers to get the records of.
     * @param model    The model to write the result to.
     * @param response The HTTP response.
     * @return The view name to render the result in.
     */
    private String get(String[] pids, Model model, HttpServletResponse response) {
        List<Record> recs = new ArrayList<>();
        Map<String, List<Record>> reservedChilds = new HashMap<>();

        for (String pid : pids) {
            synchronized (this) { // Issue #139: Make sure that when A enters, B has to wait, and will detect the insert into the database by B when entering.
                Record rec = records.getRecordByPid(pid);
                if (rec == null) { // Issue #108
                    // Try creating the record.
                    try {
                        rec = records.createRecordByPid(pid);
                        records.addRecord(rec);
                    }
                    catch (NoSuchPidException e) {
                        // Pass, catch if no of the requested PIDs are available below.
                    }
                } else if (records.updateExternalInfo(rec, false)) {
                    records.saveRecord(rec);
                }

                if (rec != null) {
                    recs.add(rec);

                    List<Record> reserved = records.getReservedChildRecords(rec);
                    reservedChilds.put(rec.getPid(), reserved);
                }
            }
        }

        if (recs.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("records", recs);
        model.addAttribute("reservedChilds", reservedChilds);

        response.setHeader("Content-Type", "application/javascript");

        return "json/record_get.json";
    }

    /**
     * Request information about multiple records.
     *
     * @param encPids  The pids separated by a comma, to get information of.
     * @param callback A callback, if provided, for the JSONP response.
     * @param model    The model to add the result to.
     * @param response The HTTP response.
     * @return The name of the view to resolve.
     */
    @RequestMapping(value = "/{encPids:.*}",
            method = RequestMethod.GET)
    public String get(
            @PathVariable String encPids,
            @RequestParam(required = false) String callback,
            Model model,
            HttpServletResponse response
    ) {
        model.addAttribute("callback", callback);
        return get(getPidsFromURL(encPids), model, response);
    }
    // }}}
    // {{{ Delete API

    /**
     * Delete records.
     *
     * @param pids The persistent identifiers of the records to remove.
     */
    private void remove(String[] pids) {
        for (String pid : pids) {
            Record rec = records.getRecordByPid(pid);
            if (rec != null) {
                records.removeRecord(rec);
            }
        }
    }

    /**
     * Remove a record (DELETE method).
     *
     * @param encPids The PIDs of the records to remove (comma separated).
     * @return The view to resolve.
     */
    @RequestMapping(value = "/{encPids:.*}",
            method = RequestMethod.DELETE)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_RECORD_DELETE')")
    public String apiDelete(@PathVariable String encPids) {
        remove(getPidsFromURL(encPids));
        return "";
    }

    /**
     * Remove a record (POST method, !DELETE in path).
     *
     * @param encPids The PIDs of the records to remove (comma separated).
     * @return The view to resolve.
     */
    @RequestMapping(value = "/{encPids:.*}!DELETE",
            method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_RECORD_DELETE')")
    public String apiFakeDelete(@PathVariable String encPids) {
        remove(getPidsFromURL(encPids));
        return "";
    }
    // }}}
    // {{{ Model data

    /**
     * Usage Restriction type enumeration in Map format for use in views.
     *
     * @return The map with usage restriction types.
     */
    @ModelAttribute("usageRestriction_types")
    public Map<String, Holding.UsageRestriction> usageRestrictionTypes() {
        Map<String, Holding.UsageRestriction> data = new HashMap<>();
        data.put("OPEN", Holding.UsageRestriction.OPEN);
        data.put("CLOSED", Holding.UsageRestriction.CLOSED);
        return data;
    }

    // }}}
    // {{{ Form

    /**
     * Homepage for choosing records to edit.
     *
     * @return The view to resolve.
     */
    @RequestMapping(value = "/",
            method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_RECORD_MODIFY')")
    public String showHome() {
        return "record_home";
    }

    /**
     * Redirect to corresponding edit form.
     *
     * @param model The model to add attributes to.
     * @param pid   The posted PID.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/",
            method = RequestMethod.GET,
            params = "searchPid")
    @PreAuthorize("hasRole('ROLE_RECORD_MODIFY')")
    public String processHomeSearchPid(Model model,
                                       @RequestParam String pid) {
        if (pid != null) {
            try {
                // First search locally, if that fails search remote.
                if (records.getRecordByPid(pid) != null ||
                        lookup.getRecordMetaDataByPid(pid) != null) {
                    return "redirect:/record/editform/" + urlEncode(pid);
                }
            }
            catch (NoSuchPidException e) {
            }
        }
        Map<String, String> results = new HashMap<>();
        model.addAttribute("results", results);
        return "record_home";
    }

    /**
     * Search the API for records by title.
     *
     * @param model The model to add attributes to.
     * @param title The title to search for.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/",
            method = RequestMethod.GET,
            params = "searchApi")
    @PreAuthorize("hasRole('ROLE_RECORD_MODIFY')")
    public String processHomeSearchApi(Model model,
                                       @RequestParam String title, @RequestParam(defaultValue = "1", required = false) int resultStart) {
        if (title == null) {
            return "record_home";
        }
        title = urlDecode(title);
        RecordLookupService.PageChunk pc = lookup.getRecordsByTitle(title, deliveryProperties.getRecordPageLen(), resultStart);
        model.addAttribute("pageChunk", pc);
        model.addAttribute("recordTitle", title);
        return "record_home";
    }

    /**
     * Search locally for records by title.
     *
     * @param model The model to add attributes to.
     * @param title The title to search for.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/",
            method = RequestMethod.POST,
            params = "searchLocal")
    @PreAuthorize("hasRole('ROLE_RECORD_MODIFY')")
    @Deprecated
    public String processHomeSearchLocal(Model model,
                                         @RequestParam String title) {
        Map<String, String> results = searchLocalHelper(title);
        model.addAttribute("results", results);
        return "record_home";
    }

    @Deprecated
    private Map<String, String> searchLocalHelper(String title) {
        Map<String, String> results = new HashMap<>();
        if (title == null) {
            return results;
        }

        CriteriaBuilder cb = records.getRecordCriteriaBuilder();
        CriteriaQuery<Record> cq = cb.createQuery(Record.class);
        Root<Record> rRoot = cq.from(Record.class);
        cq.select(rRoot);
        Join<Record, ExternalRecordInfo> eRoot = rRoot.join(Record_.externalInfo);

        String[] lowSearchTitle = title.toLowerCase().replaceAll("\\s+", " ").split(" ");
        Expression<Boolean> titleWhere = null;
        for (String s : lowSearchTitle) {
            Expression<Boolean> titleSearch = cb.like(cb.lower(eRoot.get(ExternalRecordInfo_.title)), "%" + s + "%");
            titleWhere = titleWhere == null ? titleSearch : cb.and(titleWhere,
                    titleSearch);
        }

        // Make sure only records that do not have a parent (i.e. whole
        // archives, serials, books) match.
        Expression<Boolean> where = cb.and(titleWhere,
                cb.equal(rRoot.get(Record_.parent),
                        cb.nullLiteral(Record.class)));
        cq.where(where);

        List<Record> rl = records.listRecords(cq);

        for (Record r : rl) {
            results.put(r.getPid(), r.getTitle());
        }

        return results;
    }

    /**
     * Edit form of record metadata.
     *
     * @param encPid The PID to edit (URL encoded).
     * @param model  The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/editform/{encPid:.*}",
            method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_RECORD_MODIFY')")
    public String showEditForm(@PathVariable String encPid, Model model) {
        // Check if the record already exists, lookup to check if valid
        // otherwise.
        String pid = urlDecode(encPid);
        Record r = records.getRecordByPid(pid);
        if (r == null) {
            try {
                r = records.createRecordByPid(pid);
                model.addAttribute("isNewRecord", true);
            }
            catch (NoSuchPidException e) {
                // This should not happen with normal usage through
                // record-home.
                throw new InvalidRequestException("No such PID. Are you sure the " +
                        "record you want to add is available in the SRW API?");
            }
        } else {
            // Add holding/other API info if present
            records.updateExternalInfo(r, true);
        }
        model.addAttribute("record", r);
        return "record_edit";
    }


    /**
     * Processing of the edit form.
     *
     * @param newRecord The updated/new record to add to the database.
     * @param result    The binding result to use for errors.
     * @param encPid    The PID to edit (URL encoded).
     * @param model     The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/editform/{encPid:.*}",
            method = RequestMethod.POST,
            params = "action=save")
    @PreAuthorize("hasRole('ROLE_RECORD_MODIFY')")
    public String processEditForm(@ModelAttribute("record") Record newRecord,
                                  BindingResult result,
                                  @PathVariable String encPid,
                                  Model model) {
        String pid = urlDecode(encPid);
        Record oldRecord = records.getRecordByPid(pid);
        newRecord.setPid(pid);
        if (oldRecord != null) {
            newRecord.setChildren(oldRecord.getChildren());
            for (Holding oh : oldRecord.getHoldings()) {
                for (Holding nh : newRecord.getHoldings()) {
                    if (oh.getSignature().equals(nh.getSignature())) {
                        nh.setStatus(oh.getStatus());
                    }
                }
            }
        }

        try {
            records.createOrEdit(newRecord, oldRecord, result);
        }
        catch (NoSuchParentException e) {
            // Cannot get here with normal use.
            throw new InvalidRequestException(e.getMessage());
        }

        model.addAttribute("record", newRecord);

        return "record_edit";
    }

    /**
     * Processes the delete button in the edit form.
     *
     * @param encPids The PIDs to remove (comma separated).
     * @return The view to resolve.
     */
    @RequestMapping(value = "/editform/{encPids:.*}",
            method = RequestMethod.POST,
            params = "action=delete")
    @PreAuthorize("hasRole('ROLE_RECORD_DELETE')")
    public String formDelete(@PathVariable String encPids) {
        remove(getPidsFromURL(encPids));
        return "redirect:/record/";
    }

    /**
     * Edit a child item of a record.
     *
     * @param edit The PID of the parent record.
     * @param item The item number of the child.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/editform/{encPids:.*}",
            method = RequestMethod.POST,
            params = "action=edititem")
    @PreAuthorize("hasRole('ROLE_RECORD_MODIFY')")
    public String editChildRedirect(@RequestParam String edit,
                                    @RequestParam String item) {
        edit = urlEncode(edit);
        item = urlEncode(item);
        String itemSeparator = deliveryProperties.getItemSeparator();
        return "redirect:/record/editform/" + edit + itemSeparator + item;
    }

    // }}}


}
