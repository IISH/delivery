package org.socialhistoryservices.delivery.permission.controller;

import org.hibernate.validator.constraints.Email;
import org.socialhistoryservices.delivery.permission.entity.Permission;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Form to handle modifying permissions.
 */
public class PermissionForm {

    /** The name of the visitor to view the record. */
    @NotNull
    @Size(min=1, max=255)
    private String visitorName;
    /**
     * Get the Permission's visitorName.
     * @return The visitorName.
     */
    public String getVisitorName() {
        return visitorName;
    }
    /**
     * Set the Permission's visitorName.
     * @param visitorName The new visitorName.
     */
    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    /** The email address of the visitor to view the record. */
    @NotNull
    @Email
    @Size(min=1,max=255)
    private String visitorEmail;
    /**
     * Get the Permission's visitorEmail.
     * @return The visitorEmail.
     */
    public String getVisitorEmail() {
        return visitorEmail;
    }
    /**
     * Set the Permission's visitorEmail.
     * @param visitorEmail The new visitorEmail.
     */
    public void setVisitorEmail(String visitorEmail) {
        this.visitorEmail = visitorEmail;
    }

    /**
     * The permission status. Can be null, in order to use the default
     * status.
     */
    private String status;
    /**
     * Get the Permission's status.
     * @return The status.
     */
    public String getStatus() {
        return status;
    }
    /**
     * Set the Permission's status.
     * @param status The new status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The address of the permission's applicant. */
    @Size(min=1, max=255)
    private String address;
    /**
     * Get the Permission's address.
     * @return The address.
     */
    public String getAddress() {
        return address;
    }
    /**
     * Set the Permission's address.
     * @param address The new address.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /** The explanation of the permission's applicant. */
    @Size(min=1)
    private String explanation;
    /**
     * Get the Permission's explanation.
     * @return The explanation.
     */
    public String getExplanation() {
        return explanation;
    }
    /**
     * Set the Permission's explanation.
     * @param explanation The new explanation.
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /** The researchOrganization of the permission's applicant. */
    @NotNull
    @Size(min=1, max=255)
    private String researchOrganization;
    /**
     * Get the Permission's research organization.
     * @return The the name of the organization.
     */
    public String getResearchOrganization() {
        return researchOrganization;
    }
    /**
     * Set the Permission's organization.
     * @param researchOrganization The new organization.
     */
    public void setResearchOrganization(String researchOrganization) {
        this.researchOrganization = researchOrganization;
    }

    /** The researchOrganization of the permission's applicant. */
    @NotNull
    @Size(min=1)
    private String researchSubject;
    /**
     * Get the Permission's research organization.
     * @return The the name of the organization.
     */
    public String getResearchSubject() {
        return researchSubject;
    }
    /**
     * Set the Permission's subject.
     * @param researchSubject The new subject.
     */
    public void setResearchSubject(String researchSubject) {
        this.researchSubject = researchSubject;
    }

    /** The date requested of the permission's applicant. */
    private Date dateRequested;
    /**
     * Get the Permission's date requested
     * @return The the name of the organization.
     */
    public Date getDateRequested() {
        return dateRequested;
    }
    /**
     * Set the Permission's date requested.
     * @param dateRequested The date requested.
     */
    public void setDateRequested(Date dateRequested) {
        this.dateRequested = dateRequested;
    }

    /**
     * Save data into a model.
     * @param obj Permission to save into.
     * @param df The date formatter
     */
    public void fillInto(Permission obj, SimpleDateFormat df) {
        obj.setName(getVisitorName());
        obj.setEmail(getVisitorEmail());
        obj.setAddress(getAddress());
        obj.setExplanation(getExplanation());
        obj.setResearchOrganization(getResearchOrganization());
        obj.setResearchSubject(getResearchSubject());
    }
}
