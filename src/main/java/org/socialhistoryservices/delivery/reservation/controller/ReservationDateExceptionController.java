package org.socialhistoryservices.delivery.reservation.controller;

import org.socialhistoryservices.delivery.request.controller.AbstractRequestController;
import org.socialhistoryservices.delivery.reservation.entity.ReservationDateException;
import org.socialhistoryservices.delivery.reservation.entity.ReservationDateException_;
import org.socialhistoryservices.delivery.reservation.service.ReservationDateExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Controller of the ReservationDateException package, handles all /reservation_date_exception/* requests.
 */
@Controller
@Transactional
@RequestMapping(value = "/reservation_date_exception")
public class ReservationDateExceptionController extends AbstractRequestController {
    @Autowired
    private ReservationDateExceptionService reservationDateExceptions;


    /**
     * Process the deletion of selected ReservationDateExceptions. If none selected it goes back to the default page.
     * @param checked List of checked ReservationDateException's ids.
     * @param model The Model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/date_exception",
                    method = RequestMethod.POST,
                    params = "deleteDateException")
    @PreAuthorize("hasRole('ROLE_DATE_EXCEPTION_DELETE')")
    public String processDateExceptionDeleteForm(@RequestParam(required = false) ArrayList<String> checked,
                                                    Model model,
                                                    @ModelAttribute("reservationDateExceptions")
                                                    ArrayList<ReservationDateException> resExceptions,
                                                    BindingResult result) {
        if(checked != null){
            List<ReservationDateException> reservationDateExceptionList = new ArrayList<>();
            for(String s : checked) {
                reservationDateExceptionList.add(reservationDateExceptions.getReservationDateExceptionsById(Integer.parseInt(s)));
            }
            try {
                for (ReservationDateException res : reservationDateExceptionList) {
                    reservationDateExceptions.removeReservationDateException(res);
                }
            }catch (Exception e){
                String msg = messageSource.getMessage("reservationDateExceptionOverview.cannotDelete", new Object[]{}, LocaleContextHolder.getLocale());
                result.addError(new FieldError(result.getObjectName(), "",
                    resExceptions, false,
                    null, null, msg));
            }
        }
        if(!result.hasErrors())
            return "redirect:/reservation_date_exception/date_exception";
        else
            return "reservation_date_exception/date_exception";
    }

    /**
     * Show the date exception form for reservation date exceptions.
     * @param model The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/date_exception",
        method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_DATE_EXCEPTION_VIEW')")
    public String showDateExceptionForm(Model model, HttpServletRequest req){
        model.addAttribute("reservationDateExceptions", getDateExceptions(req));
        ReservationDateException reservationDateException = new ReservationDateException();
        model.addAttribute("reservationDateException", reservationDateException);
        return "reservation_date_exception";
    }

    /**
     * Process the adding of a ReservationDateException.
     * @param req The HTTP request object.
     * @param newResDate The ReservationDateException.
     * @param result The BindingResult to hold the errors.
     * @param model The Model to add attributes to.
     * @return The view to resolve
     */
    @RequestMapping(value = "/date_exception",
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_DATE_EXCEPTION_CREATE')")
    public String processDateExceptionForm(HttpServletRequest req, @ModelAttribute("reservationDateException")
                                            ReservationDateException newResDate,
                                            BindingResult result,
                                            Model model){
        if(reservationDateExceptions.isValid(newResDate,result)) {
            try {
                if (newResDate.getEndDate() == null) {
//                    newResDate.setEndDate(new Date(7226582400000L));
                }
                reservationDateExceptions.addReservationDateException(newResDate);
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }

        model.addAttribute("reservationDateException", newResDate);
        if(!result.hasErrors())
            return "redirect:/reservation_date_exception/date_exception";
        else {
            model.addAttribute("reservationDateExceptions", getDateExceptions(req));
            return "reservation_date_exception";
        }
    }

    /**
     * List all ReservationDateExceptions existing in the database whilst discarding the date
     * exceptions before the current date.
     * @return A list with existing ReservationDateExceptions.
     */
    public List<ReservationDateException> getDateExceptions(HttpServletRequest req){
        Map<String, String[]> p = req.getParameterMap();
        CriteriaBuilder builder = reservationDateExceptions.getReservationDateExceptionCriteriaBuilder();
        CriteriaQuery<ReservationDateException> query = builder.createQuery(ReservationDateException.class);
        Root<ReservationDateException> root = query.from(ReservationDateException.class);
        query.select(root);

        // Expression to be the where clause of the query
        Expression<Boolean> where = null;

        // Filters
        where = addExceptionStartDateFilter(p, builder, root, where);
        where = addExceptionEndDateFilter(p, builder, root, where);

        // Set the where clause
        if(where != null){
            query.where(where);
        }

        query.orderBy(parseDateExceptionSortFilter(p, builder, root));

        List<ReservationDateException> result = reservationDateExceptions.listReservationDateExceptions(query);

        // Remove the dates that are before the current date so only future dates are shown
        Calendar cal = Calendar.getInstance();
        for (Iterator<ReservationDateException> iterator = result.iterator(); iterator.hasNext();) {
            while (iterator.hasNext()) {
                ReservationDateException temp = iterator.next();
                if(temp.getEndDate() != null){
                    if(temp.getEndDate().before(cal.getTime())){
                        iterator.remove();
                    }
                }
                else{
                    if(temp.getStartDate().before(cal.getTime())){
                        iterator.remove();
                    }
                }
            }
        }
        return result;
    }

    /**
     * Parse the sort and sort_dir filters into an Order to be used in a query
     * @param p The parameter list to search the filter values in.
     * @param cb The CriteriaBuilder used to construct the Order.
     * @param resRoot The root of the ReservationDateException used to construct the Order.
     * @return The order the query should be sorted in (asc/desc) on provided column. Defaults
     * to asc.
     */
    private Order parseDateExceptionSortFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                               From<?,ReservationDateException> resRoot) {
        boolean containsSort = p.containsKey("sort");
        boolean containsSortDir = p.containsKey("sort_dir");
        Expression e = resRoot.get(ReservationDateException_.startDate);
        if (containsSort) {
            String sort = p.get("sort")[0];
            if (sort.equals("startDate")) {
                e = resRoot.get(ReservationDateException_.startDate);
            }else if(sort.equals("endDate")) {
                e = resRoot.get(ReservationDateException_.endDate);
            }
        }
        if (containsSortDir &&
            p.get("sort_dir")[0].toLowerCase().equals("asc")) {
            return cb.asc(e);
        }
        return cb.desc(e);
    }

    /**
     * Add the startDate filter to the where clause, if present.
     * @param p The parameter list to search the given filter value in.
     * @param cb The CriteriaBuilder used to construct the order.
     * @param resRoot The ReservationDateException root used to construct the order.
     * @param where The already present where clause, or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addExceptionStartDateFilter(Map<String, String[]> p,
                                                            CriteriaBuilder cb,
                                                            Root<ReservationDateException> resRoot,
                                                            Expression<Boolean> where) {
        Date date = getDateFilter(p);
        if (date != null) {
            Expression<Boolean> exDate = cb.equal(resRoot.get(ReservationDateException_.startDate), date);
            where = where != null ? cb.and(where, exDate) : exDate;
        }
        return where;
    }

    /**
     * Add the endDate filter to the where clause, if present.
     * @param p The parameter list to search the given filter value in.
     * @param cb The CriteriaBuilder used to construct the order.
     * @param resRoot The ReservationDateException root used to construct the order.
     * @param where The already present where clause, or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addExceptionEndDateFilter(Map<String, String[]> p,
                                                          CriteriaBuilder cb,
                                                          Root<ReservationDateException> resRoot,
                                                          Expression<Boolean> where) {
        Date date = getDateFilter(p);
        if (date != null) {
            Expression<Boolean> exDate = cb.equal(resRoot.get(ReservationDateException_.endDate), date);
            where = where != null ? cb.and(where, exDate) : exDate;
        }
        return where;
    }
}
