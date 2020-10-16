package org.socialhistoryservices.delivery.record.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Extra info extracted from the external API.
 */
@Entity
@Table(name = "external_holding_info")
public class ExternalHoldingInfo {
    /**
     * The id.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    /**
     * Get the info's id.
     *
     * @return the info's id.
     */
    public int getId() {
        return id;
    }

    /**
     * The Holding's barcode.
     */
    @Size(max = 255)
    @Column(name = "barcode", unique = true)
    private String barcode;

    /**
     * Get the Holding's barcode.
     *
     * @return the Holding's barcode.
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Set the Holding's barcode.
     *
     * @param barcode the Holding's barcode.
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    @Column(name = "serialnumbers", columnDefinition = "TEXT")
    private String serialNumbers;

    /**
     * Get the available serial numbers.
     *
     * @return The serial numbers available.
     */
    public String getSerialNumbers() {
        return serialNumbers;
    }

    /**
     * Set the available serial numbers.
     *
     * @param nrs The available serial numbers.
     */
    public void setSerialNumbers(String nrs) {
        serialNumbers = nrs;
    }

    /**
     * The Holding's shelving location.
     */
    @Size(max = 255)
    @Column(name = "shelvinglocation")
    private String shelvingLocation;

    /**
     * Set the shelving location.
     *
     * @return the shelving location.
     */
    public String getShelvingLocation() {
        return shelvingLocation;
    }

    /**
     * Get the shelving location.
     *
     * @param shelvingLocation the shelving location.
     */
    public void setShelvingLocation(String shelvingLocation) {
        this.shelvingLocation = shelvingLocation;
    }

    /**
     * Merge other record's data with this record.
     *
     * @param other The other record.
     */
    public void mergeWith(ExternalHoldingInfo other) {
        setBarcode(other.getBarcode());
        setSerialNumbers(other.getSerialNumbers());
        setShelvingLocation(other.getShelvingLocation());
    }

    /**
     * Creates a new and empty ExternalHoldingInfo as a placeholder for non-existing holdings.
     *
     * @return The new and empty getEmptyExternalInfo.
     */
    public static ExternalHoldingInfo getEmptyExternalInfo() {
        ExternalHoldingInfo externalHoldingInfo = new ExternalHoldingInfo();
        externalHoldingInfo.setSerialNumbers("Non-existing holding");
        return externalHoldingInfo;
    }
}
