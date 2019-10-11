package org.socialhistoryservices.delivery.permission.entity;

import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.socialhistoryservices.delivery.record.entity.Record;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.*;

/**
 * A permission request in order to view a particular set of restricted records.
 */
@Entity
@Table(name="permissions")
public class Permission {
    /** The Permission's id. */
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    /**
     * Get the Permission's id.
     * @return the Permission's id.
     */
    public int getId() {
        return id;
    }

    /**
     * Set the Permission's id.
     * @param id the Permission's id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /** The Permission's name. */
    @NotBlank
    @Size(max = 255)
    @Column(name="name", nullable=false)
    private String name;

    /**
     * Get the Permission's name.
     * @return the Permission's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the Permission's name.
     * @param name the Permission's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /** The Permission's code. */
    @NotBlank
    @Size(max = 255)
    @Column(name="code")
    private String code;

    /**
     * Get the Permission's code.
     * @return the Permission's code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Set the Permission's code.
     * @param code the Permission's code.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Generate a completely random code token.
     */
    public void generateCode() {
        code = RandomStringUtils.randomAlphanumeric(20);
    }

    /** The Permission's email. */
    @NotBlank
    @Size(max = 255)
    @Column(name="email", nullable=false)
    private String email;

    /**
     * Get the Permission's email.
     * @return the Permission's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the Permission's email.
     * @param email the Permission's email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /** The Permission's researchSubject. */
    @NotBlank
    @Column(name="research_subject", columnDefinition="TEXT", nullable=false)
    private String researchSubject;

    /**
     * Get the Permission's researchSubject.
     * @return the Permission's researchSubject.
     */
    public String getResearchSubject() {
        return researchSubject;
    }

    /**
     * Set the Permission's researchSubject.
     * @param researchSubject the Permission's researchSubject.
     */
    public void setResearchSubject(String researchSubject) {
        this.researchSubject = researchSubject;
    }

    /** The Permission's researchOrganization. */
    @NotBlank
    @Column(name="research_organization", columnDefinition="TEXT", nullable=false)
    private String researchOrganization;

    /**
     * Get the Permission's researchOrganization.
     * @return the Permission's researchOrganization.
     */
    public String getResearchOrganization() {
        return researchOrganization;
    }

    /**
     * Set the Permission's researchOrganization.
     * @param researchOrganization the Permission's researchOrganization.
     */
    public void setResearchOrganization(String researchOrganization) {
        this.researchOrganization = researchOrganization;
    }

    /** The Permission's explanation. */
    @Column(name="explanation", columnDefinition="TEXT")
    private String explanation;

    /**
     * Get the Permission's explanation.
     * @return the Permission's explanation.
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Set the Permission's explanation.
     * @param explanation the Permission's explanation.
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /** The Permission's address. */
    @Size(max = 255)
    @Column(name="address")
    private String address;

    /**
     * Get the Permission's address.
     * @return the Permission's address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set the Permission's address.
     * @param address the Permission's address.
     */
    public void setAddress(String address) {
        this.address = address;
    }


    /* Date requested */
    @Temporal(TemporalType.DATE)
    @Column(name="date_requested")
    private Date dateRequested = new Date();

    /**
     * Get the date requested.
     * @return The date requested.
     */
    public Date getDateRequested() {
        return dateRequested;
    }

    /**
     * Set the date requested.
     * @param dateRequested The date requested.
     */
    public void setDateRequested(Date dateRequested) {
        this.dateRequested = dateRequested;
    }




    /** The Permission's request locale. */
    @Column(name="requestlocale", nullable=false)
    private Locale requestLocale;

    /**
     * Get the locale in which this permission was requested.
     * @return the Permission's request locale.
     */
    public Locale getRequestLocale() {
        return requestLocale;
    }

    /**
     * Set the Permission's request locale.
     * @param locale the Permission's request locale.
     */
    public void setRequestLocale(Locale locale) {
        requestLocale = locale;
    }

    /**
     * Permissions per record in this permission request.
     */
    @OneToMany(mappedBy="permission", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<RecordPermission> recordPermissions;

    /**
     * Get the per record permissions.
     * @return A list of permissions per record.
     */
    public List<RecordPermission> getRecordPermissions() {
        return recordPermissions;
    }

    /**
     * Add a record permission to a permission.
     * @param rp The record permission to add.
     */
    public void addRecordPermission(RecordPermission rp) {
        recordPermissions.add(rp);
    }

    /**
     * Remove a record permission from a permission.
     * @param rp The record permission to remove.
     */
    public void removeRecordPermission(RecordPermission rp) {
        recordPermissions.remove(rp);
    }

    /**
     * Check if this permission has a granted clause for
     * the given record.
     * @param rec Record to check for.
     * @return Whether permission has been granted for this record.
     */
    public boolean hasGranted(Record rec) {
        Calendar minDateCalender = Calendar.getInstance();

        minDateCalender.add(Calendar.YEAR, -1);

        for (RecordPermission p : recordPermissions) {
            boolean recordMatch = p.getRecord().equals(rec) ||
                ((rec.getParent() != null) && p.getRecord().equals(rec.getParent()));
            boolean valid = (p.getDateGranted() != null) && p.getDateGranted().after(minDateCalender.getTime());

            if (recordMatch && p.getGranted() && valid)
                return true;
        }
        return false;
    }

    /**
     * Default contstructor.
     */
    public Permission() {
        recordPermissions = new ArrayList<>();
        setRequestLocale(LocaleContextHolder.getLocale());
    }

}
