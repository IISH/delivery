package org.socialhistoryservices.delivery.permission.service;

import org.socialhistoryservices.delivery.permission.entity.Permission;
import org.socialhistoryservices.delivery.permission.entity.Permission_;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo_;
import org.socialhistoryservices.delivery.record.entity.Record;
import org.socialhistoryservices.delivery.record.entity.Record_;
import org.socialhistoryservices.delivery.request.service.ListRequestSearch;

import javax.persistence.criteria.*;
import java.util.Map;

/**
 * Permission search helper class, with support for paging.
 */
public class PermissionSearch extends ListRequestSearch<Permission> {

    /**
     * Creates a new permission search helper.
     *
     * @param cb The criteria builder.
     * @param p  The parameters from the user.
     */
    public PermissionSearch(CriteriaBuilder cb, Map<String, String[]> p) {
        super(Permission.class, cb, p);
    }

    /**
     * Build the query.
     *
     * @param pRoot   The root entity.
     * @param cq      The query to build upon.
     * @param isCount Whether the query is a count or not.
     */
    @Override
    protected void build(Root<Permission> pRoot, CriteriaQuery<?> cq, boolean isCount) {
        Predicate where = addDateFilter(pRoot, null);
        where = addNameFilter(pRoot, where);
        where = addEmailFilter(pRoot, where);
        where = addResearchOrganizationFilter(pRoot, where);
        where = addResearchSubjectFilter(pRoot, where);
        where = addAddressFilter(pRoot, where);
        where = addExplanationFilter(pRoot, where);
        where = addPermissionFilter(pRoot, where);
        where = addSearchFilter(pRoot, where);

        // Set the where clause
        if (where != null)
            cq.where(where);

        // Set sort order and sort column
        if (!isCount)
            cq.orderBy(parseOrderFilter(pRoot));
    }

    /**
     * Parse the sort and sort_dir filters into an Order to be used in a query.
     *
     * @param pRoot The root of the permission used to construct the Order.
     * @return The order the query should be in (asc/desc) sorted on provided
     * column. Defaults to asc on the PK column.
     */
    private Order parseOrderFilter(Root<Permission> pRoot) {
        boolean containsSort = p.containsKey("sort");
        boolean containsSortDir = p.containsKey("sort_dir");
        Expression<?> e = pRoot.get(Permission_.id);

        if (containsSort) {
            String sort = p.get("sort")[0];
            switch (sort) {
                case "visitor_name":
                    e = pRoot.get(Permission_.name);
                    break;
                case "date_granted":
                    e = pRoot.get(Permission_.dateGranted);
                    break;
                case "permission":
                    e = pRoot.get(Permission_.granted);
                    break;
            }
        }

        if (containsSortDir && p.get("sort_dir")[0].toLowerCase().equals("asc"))
            return cb.asc(e);

        return cb.desc(e);
    }

    /**
     * Add the search filter to the where clause, if present.
     *
     * @param pRoot The permission root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Predicate addSearchFilter(Root<Permission> pRoot, Predicate where) {
        if (p.containsKey("search") && !p.get("search")[0].trim().equals("")) {
            String search = p.get("search")[0].trim().toLowerCase();

            Join<Permission, Record> record = pRoot.join(Permission_.record);
            Join<Record, ExternalRecordInfo> eRoot = record.join(Record_.externalInfo);

            Predicate exSearch = cb.or(
                    cb.like(cb.lower(eRoot.get(ExternalRecordInfo_.title)),
                            "%" + search + "%"),
                    cb.like(cb.lower(pRoot.get(Permission_.name)),
                            "%" + search + "%"),
                    cb.like(cb.lower(pRoot.get(Permission_.email)),
                            "%" + search + "%"),
                    cb.like(cb.lower(pRoot.get(Permission_.explanation)),
                            "%" + search + "%"),
                    cb.like(cb.lower(pRoot.get(Permission_.researchOrganization)),
                            "%" + search + "%"),
                    cb.like(cb.lower(pRoot.get(Permission_.researchSubject)),
                            "%" + search + "%"),
                    cb.like(cb.lower(pRoot.get(Permission_.address)),
                            "%" + search + "%")
            );
            where = where != null ? cb.and(where, exSearch) : exSearch;
        }

        return where;
    }

    /**
     * Add the permission filter to the where clause, if present.
     *
     * @param pRoot The permission root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Predicate addPermissionFilter(Root<Permission> pRoot, Predicate where) {
        if (p.containsKey("permission")) {
            Predicate exPermission = null;
            String permission = p.get("permission")[0].trim().toUpperCase();

            if (permission.equals("TRUE"))
                exPermission = cb.equal(pRoot.get(Permission_.granted), true);

            if (permission.equals("FALSE"))
                exPermission = cb.equal(pRoot.get(Permission_.granted), false);

            if (permission.equals("NULL"))
                exPermission = cb.isNull(pRoot.get(Permission_.dateGranted));

            if (exPermission != null)
                where = where != null ? cb.and(where, exPermission) : exPermission;
        }

        return where;
    }

    /**
     * Add the explanation filter to the where clause, if present.
     *
     * @param pRoot The permission root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Predicate addExplanationFilter(Root<Permission> pRoot, Predicate where) {
        if (p.containsKey("explanation")) {
            Predicate exExplanation = cb.like(
                    pRoot.get(Permission_.explanation),
                    "%" + p.get("explanation")[0].trim() + "%");
            where = where != null ? cb.and(where, exExplanation) : exExplanation;
        }

        return where;
    }

    /**
     * Add the address filter to the where clause, if present.
     *
     * @param pRoot The permission root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Predicate addAddressFilter(Root<Permission> pRoot, Predicate where) {
        if (p.containsKey("address")) {
            Predicate exAddress = cb.like(pRoot.get(Permission_.address),
                    "%" + p.get("address")[0].trim() + "%");
            where = where != null ? cb.and(where, exAddress) : exAddress;
        }

        return where;
    }

    /**
     * Add the research subject filter to the where clause, if present.
     *
     * @param pRoot The permission root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Predicate addResearchSubjectFilter(Root<Permission> pRoot, Predicate where) {
        if (p.containsKey("research_subject")) {
            Predicate exResearch = cb.like(pRoot.get(Permission_.researchSubject),
                    "%" + p.get("research_subject")[0].trim() + "%");
            where = where != null ? cb.and(where, exResearch) : exResearch;
        }

        return where;
    }

    /**
     * Add the research organization filter to the where clause, if present.
     *
     * @param pRoot The permission root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Predicate addResearchOrganizationFilter(Root<Permission> pRoot, Predicate where) {
        if (p.containsKey("research_organization")) {
            Predicate exResearchOrg = cb.like(pRoot.get(Permission_.researchOrganization),
                    "%" + p.get("research_organization")[0].trim() + "%");
            where = where != null ? cb.and(where, exResearchOrg) : exResearchOrg;
        }

        return where;
    }

    /**
     * Add the email filter to the where clause, if present.
     *
     * @param pRoot The permission root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Predicate addEmailFilter(Root<Permission> pRoot, Predicate where) {
        if (p.containsKey("visitor_email")) {
            Predicate exEmail = cb.like(pRoot.get(Permission_.email),
                    "%" + p.get("visitor_email")[0].trim() + "%");
            where = where != null ? cb.and(where, exEmail) : exEmail;
        }

        return where;
    }

    /**
     * Add the name filter to the where clause, if present.
     *
     * @param pRoot The permission root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Predicate addNameFilter(Root<Permission> pRoot, Predicate where) {
        if (p.containsKey("visitor_name")) {
            Predicate exName = cb.like(pRoot.get(Permission_.name),
                    "%" + p.get("visitor_name")[0].trim() + "%");
            where = where != null ? cb.and(where, exName) : exName;
        }

        return where;
    }

    /**
     * Add the date granted filter to the where clause, if present.
     *
     * @param pRoot The permission root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Predicate addDateFilter(Root<Permission> pRoot, Predicate where) {
        Predicate datePredicate = getDatePredicate(pRoot.get(Permission_.dateGranted), false);
        if (datePredicate != null)
            where = (where != null) ? cb.and(where, datePredicate) : datePredicate;

        return where;
    }
}
