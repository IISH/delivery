package org.socialhistoryservices.delivery.permission.entity;

import org.socialhistoryservices.delivery.record.entity.Record;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * A permission on a specific restricted record (links a permission request
 * to records, specifying for each record whether permission is granted or not).
 */
@Entity
@Table(name = "recordpermissions")
public class RecordPermission {
    /**
     * The RecordPermission's id.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    /**
     * Get the RecordPermission's id.
     *
     * @return the RecordPermission's id.
     */
    public int getId() {
        return id;
    }

    /**
     * Set the RecordPermission's id.
     *
     * @param id the RecordPermission's id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * The RecordPermission's granted.
     */
    @Column(name = "granted", nullable = false)
    private boolean granted;

    /**
     * Get the RecordPermission's granted.
     *
     * @return the RecordPermission's granted.
     */
    public boolean getGranted() {
        return granted;
    }

    /**
     * Set the RecordPermission's granted.
     *
     * @param granted the RecordPermission's granted.
     */
    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    @Column(name = "motivation", columnDefinition = "TEXT")
    private String motivation;

    /**
     * Set the RecordPermission's motivation.
     *
     * @param mot The motivation string.
     */
    public void setMotivation(String mot) {
        this.motivation = mot;
    }

    /**
     * Get the motivation.
     *
     * @return The motivation string.
     */
    public String getMotivation() {
        return motivation;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "date_granted")
    private Date dateGranted;

    /**
     * Get the date granted.
     *
     * @return The date granted.
     */
    public Date getDateGranted() {
        return dateGranted;
    }

    /**
     * Set the date granted.
     *
     * @param dateGranted The date granted.
     */
    public void setDateGranted(Date dateGranted) {
        this.dateGranted = dateGranted;
    }

    @Size(max = 500)
    @Column(name = "org_request_pids")
    private String originalRequestPids;

    /**
     * Get the originally requested PIDs, when this has changed to a collection level allow/deny.
     *
     * @return the originally requested PIDs.
     */
    public String getOriginalRequestPids() {
        return originalRequestPids;
    }

    /**
     * Set the originally requested PIDs, when this has changed to a collection level allow/deny.
     *
     * @param originalRequestPids the originally requested PIDs.
     */
    public void setOriginalRequestPids(String originalRequestPids) {
        this.originalRequestPids = originalRequestPids;
    }

    /**
     * The RecordPermission's permission.
     */
    @ManyToOne
    @JoinColumn(name = "permission_id")
    private Permission permission;

    /**
     * Get the RecordPermission's permission.
     *
     * @return the RecordPermission's permission.
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * Set the RecordPermission's permission.
     *
     * @param permission the RecordPermission's permission.
     */
    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    /**
     * The RecordPermission's record.
     */
    @ManyToOne
    @JoinColumn(name = "record_id")
    private Record record;

    /**
     * Get the RecordPermission's record.
     *
     * @return the RecordPermission's record.
     */
    public Record getRecord() {
        return record;
    }

    /**
     * Set the RecordPermission's record.
     *
     * @param record the RecordPermission's record.
     */
    public void setRecord(Record record) {
        this.record = record;
    }

    /**
     * Default contructor.
     */
    public RecordPermission() {
        setGranted(false);
    }

}
