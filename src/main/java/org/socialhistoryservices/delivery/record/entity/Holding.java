package org.socialhistoryservices.delivery.record.entity;

import org.hibernate.validator.constraints.NotBlank;
import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction;
import org.socialhistoryservices.delivery.reproduction.entity.ReproductionStandardOption;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Holding information associated with a Record.
 */
@Entity
@Table(name = "holdings", indexes = {@Index(columnList = "record_id", name = "holdings_record_fk"),@Index(columnList = "external_info_id", name = "holdings_external_info_fk")})
public class Holding {
    /** The usage restriction of the holding. */
    public enum UsageRestriction {
        OPEN,
        CLOSED
    }

     /** Status of the holding. */
    public enum Status {
        AVAILABLE,
        RESERVED,
        IN_USE,
        RETURNED,
    }

    /** The Holding's id. */
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    /**
     * Get the Holding's id.
     * @return the Holding's id.
     */
    public int getId() {
        return id;
    }

    /** The Holding's type. */
    @NotBlank
    @Column(name="signature", nullable=false)
    private String signature;

    /**
     * Get the Holding's type.
     * @return the Holding's type.
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Set the Holding's type.
     * @param sig the Holding's type.
     */
    public void setSignature(String sig) {
        signature = sig;
        this.checkSignature();
    }

    public void checkSignature() {
        // Determine the usage restriction by checking the signature for patterns
        String checkSignature = signature.trim().toLowerCase();

        if (
                checkSignature.endsWith(".x")
                        || checkSignature.endsWith("(missing)")
                        || checkSignature.startsWith("no circulation")
                        || checkSignature.startsWith("niet ter inzage")
                        || checkSignature.endsWith("(not available)")
        ) {
            this.setUsageRestriction(UsageRestriction.CLOSED);
        }
    }

    /** The Holding's record. */
    @NotNull
    @ManyToOne
    @JoinColumn(name="record_id")
    private Record record;

    /**
     * Get the Holding's record.
     * @return the Holding's record.
     */
    public Record getRecord() {
        return record;
    }

    /**
     * Set the Holding's record.
     * @param record the Holding's record.
     */
    public void setRecord(Record record) {
        this.record = record;
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="usage_restriction", nullable=false)
    private UsageRestriction usageRestriction;

    /**
     * Get the usage restriction.
     * @return The value of the user restriction.
     */
    public UsageRestriction getUsageRestriction() {
        return usageRestriction;
    }

    /**
     * Set the Holding's usage restriction.
     * @param u The value to set the usage to.
     */
    public void setUsageRestriction(UsageRestriction u) {
        usageRestriction = u;
    }

    /** The Holding's status. */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false)
    private Status status;

    /**
     * Get the Holding's status.
     * @return the Record's status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Set the Holding's status.
     * @param status the Record's status.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
    @JoinColumn(name="external_info_id")
    private ExternalHoldingInfo externalInfo;

    /**
     * Get the external holding info.
     * @return The info object.
     */
    public ExternalHoldingInfo getExternalInfo() {
        return externalInfo;
    }

    /**
     * Set the external info (preferably from IISHRecordLookupService
     * .getRecordMetaDataByPid).
     * @param info The info.
     */
    public void setExternalInfo(ExternalHoldingInfo info) {
        this.externalInfo = info;
    }

	@OneToMany(mappedBy="holding", cascade=CascadeType.ALL, orphanRemoval=true)
	private List<HoldingReservation> holdingReservations;

    public void setHoldingReservations(List<HoldingReservation> hrs) {
            holdingReservations = hrs;
    }

    public List<HoldingReservation> getHoldingReservations() {
		return holdingReservations;
	}

    @OneToMany(mappedBy="holding", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<HoldingReproduction> holdingReproductions;

    public List<HoldingReproduction> getHoldingReproductions() {
        return holdingReproductions;
    }

    public void setHoldingReproductions(List<HoldingReproduction> holdingReproductions) {
        this.holdingReproductions = holdingReproductions;
    }

    /**
     * Merge other's fields with this holding. All fields except ID,
     * signature and status are merged.
     * @param other The other holding.
     */
    public void mergeWith(Holding other) {
        setUsageRestriction(other.getUsageRestriction()); // update
        getExternalInfo().mergeWith(other.getExternalInfo());
    }

    /**
     * Add default data.
     */
    public Holding() {
        setStatus(Status.AVAILABLE);
        setUsageRestriction(UsageRestriction.OPEN);
        setExternalInfo(ExternalHoldingInfo.getEmptyExternalInfo());
    }

    /**
     * Determines the PID for the holding, based on the barcode.
     * @return The PID for this holding
     */
    public String determinePid() {
        return "10622/" + externalInfo.getBarcode();
    }

    /**
     * Returns whether customers requesting a reproduction of this holding should choose a custom reproduction.
     * @return Whether this holding only allows a custom reproduction.
     */
    public boolean allowOnlyCustomReproduction() {
        return "KNAW".equals(externalInfo.getShelvingLocation());
    }

    /**
     * Returns whether the holding accepts the given standard reproduction option.
     * @param standardOption The standard reproduction option.
     * @return Whether the holding accepts the given standard reproduction option.
     */
    public boolean acceptsReproductionOption(ReproductionStandardOption standardOption) {
        // Material types have to match
        if (record.getExternalInfo().getMaterialType() != standardOption.getMaterialType())
            return false;

        // In case of books, the reproduction option is based on the number of pages
        if (record.getExternalInfo().getMaterialType() == ExternalRecordInfo.MaterialType.BOOK)
            return record.getPages().containsNumberOfPages();

        // In case of visuals, it matters whether it is a poster or not
        if (record.getExternalInfo().getMaterialType() == ExternalRecordInfo.MaterialType.VISUAL) {
            boolean genresContainsPoster = record.getExternalInfo().getGenresSet().contains("poster");
            return standardOption.isPoster() == genresContainsPoster;
        }

        return true;
    }

    public String toString() {
        return String.valueOf(id);
    }
}
