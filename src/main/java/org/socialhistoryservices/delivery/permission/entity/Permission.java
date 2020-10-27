package org.socialhistoryservices.delivery.permission.entity;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.socialhistoryservices.delivery.record.entity.Record;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotBlank;
import java.util.*;

/**
 * A permission request in order to view a particular set of restricted records.
 */
@Entity
@Table(name = "permissions")
@TypeDef(name = "list-array", typeClass = ListArrayType.class)
public class Permission {
    /**
     * The Permission's id.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    /**
     * Get the Permission's id.
     *
     * @return the Permission's id.
     */
    public int getId() {
        return id;
    }

    /**
     * Set the Permission's id.
     *
     * @param id the Permission's id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * The Permission's name.
     */
    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Get the Permission's name.
     *
     * @return the Permission's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the Permission's name.
     *
     * @param name the Permission's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Permission's code.
     */
    @NotBlank
    @Size(max = 255)
    @Column(name = "code")
    private String code;

    /**
     * Get the Permission's code.
     *
     * @return the Permission's code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Set the Permission's code.
     *
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

    /**
     * The Permission's email.
     */
    @NotBlank
    @Size(max = 255)
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * Get the Permission's email.
     *
     * @return the Permission's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the Permission's email.
     *
     * @param email the Permission's email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * The Permission's researchSubject.
     */
    @NotBlank
    @Column(name = "research_subject", columnDefinition = "TEXT", nullable = false)
    private String researchSubject;

    /**
     * Get the Permission's researchSubject.
     *
     * @return the Permission's researchSubject.
     */
    public String getResearchSubject() {
        return researchSubject;
    }

    /**
     * Set the Permission's researchSubject.
     *
     * @param researchSubject the Permission's researchSubject.
     */
    public void setResearchSubject(String researchSubject) {
        this.researchSubject = researchSubject;
    }

    /**
     * The Permission's researchOrganization.
     */
    @NotBlank
    @Column(name = "research_organization", columnDefinition = "TEXT", nullable = false)
    private String researchOrganization;

    /**
     * Get the Permission's researchOrganization.
     *
     * @return the Permission's researchOrganization.
     */
    public String getResearchOrganization() {
        return researchOrganization;
    }

    /**
     * Set the Permission's researchOrganization.
     *
     * @param researchOrganization the Permission's researchOrganization.
     */
    public void setResearchOrganization(String researchOrganization) {
        this.researchOrganization = researchOrganization;
    }

    /**
     * The Permission's explanation.
     */
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    /**
     * Get the Permission's explanation.
     *
     * @return the Permission's explanation.
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Set the Permission's explanation.
     *
     * @param explanation the Permission's explanation.
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /**
     * The Permission's address.
     */
    @Size(max = 255)
    @Column(name = "address")
    private String address;

    /**
     * Get the Permission's address.
     *
     * @return the Permission's address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set the Permission's address.
     *
     * @param address the Permission's address.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * The Permission's request.
     */
    @Size(max = 255)
    @Column(name = "request")
    private String request;

    /**
     * Get the Permission's request.
     *
     * @return the Permission's request.
     */
    public String getRequest() {
        return request;
    }

    /**
     * Set the Permission's request.
     *
     * @param request the Permission's request.
     */
    public void setRequest(String request) {
        this.request = request;
    }

    /**
     * The Permission's request locale.
     */
    @Column(name = "requestlocale", nullable = false)
    private Locale requestLocale;

    /**
     * Get the locale in which this permission was requested.
     *
     * @return the Permission's request locale.
     */
    public Locale getRequestLocale() {
        return requestLocale;
    }

    /**
     * Set the Permission's request locale.
     *
     * @param locale the Permission's request locale.
     */
    public void setRequestLocale(Locale locale) {
        requestLocale = locale;
    }

    /**
     * The Permission's record.
     */
    @ManyToOne
    @JoinColumn(name = "record_id")
    private Record record;

    /**
     * Get the Permission's record.
     *
     * @return the Permission's record.
     */
    public Record getRecord() {
        return record;
    }

    /**
     * Set the Permission's record.
     *
     * @param record the Permission's record.
     */
    public void setRecord(Record record) {
        this.record = record;
    }

    /**
     * The Permission's granted.
     */
    @Column(name = "granted", nullable = false)
    private boolean granted;

    /**
     * Get the Permission's granted.
     *
     * @return the Permission's granted.
     */
    public boolean getGranted() {
        return granted;
    }

    /**
     * Set the Permission's granted.
     *
     * @param granted the Permission's granted.
     */
    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    /**
     * The Permission's motivation.
     */
    @Column(name = "motivation", columnDefinition = "TEXT")
    private String motivation;

    /**
     * Set the Permission's motivation.
     *
     * @param mot The Permission's motivation string.
     */
    public void setMotivation(String mot) {
        this.motivation = mot;
    }

    /**
     * Get the Permission's motivation.
     *
     * @return The Permission's motivation string.
     */
    public String getMotivation() {
        return motivation;
    }

    /**
     * The Permission's date granted.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "date_granted")
    private Date dateGranted;

    /**
     * Get the Permission's date granted.
     *
     * @return The Permission's date granted.
     */
    public Date getDateGranted() {
        return dateGranted;
    }

    /**
     * Set the Permission's date granted.
     *
     * @param dateGranted The Permission's date granted.
     */
    public void setDateGranted(Date dateGranted) {
        this.dateGranted = dateGranted;
    }

    /**
     * Inventory numbers granted in this permission request.
     */
    @Type(type = "list-array")
    @Column(name = "inv_nos_granted", columnDefinition = "varchar(50)[]", nullable = false)
    private List<String> invNosGranted;

    /**
     * Get the Permission's granted inventory numbers.
     *
     * @return The Permission's granted inventory numbers.
     */
    public List<String> getInvNosGranted() {
        return invNosGranted;
    }

    /**
     * Set the Permission's granted inventory numbers.
     *
     * @param invNosGranted The Permission's granted inventory numbers.
     */
    public void setInvNosGranted(List<String> invNosGranted) {
        this.invNosGranted = invNosGranted;
    }

    /**
     * Check if this permission has a granted clause for the given record.
     *
     * @param rec Record to check for.
     * @return Whether permission has been granted for this record.
     */
    public boolean hasGranted(Record rec) {
        Calendar minDateCalender = Calendar.getInstance();
        minDateCalender.add(Calendar.YEAR, -1);
        if (getDateGranted() == null || getDateGranted().before(minDateCalender.getTime()))
            return false;

        return record.getId() == rec.getId() || invNosGranted.contains(rec.getHoldings().get(0).getSignature());
    }

    /**
     * Default constructor.
     */
    public Permission() {
        invNosGranted = new ArrayList<>();
        setRequestLocale(LocaleContextHolder.getLocale());
    }
}
