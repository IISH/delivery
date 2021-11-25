package org.socialhistoryservices.delivery.record.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents archival holding info. This information is cached, cannot be relied upon to be persistent.
 */
@Entity
@Table(name = "archive_holding_info")
public class ArchiveHoldingInfo {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Size(max = 255)
    @Column(name = "shelvinglocation")
    private String shelvingLocation;

    public String getShelvingLocation() {
        return shelvingLocation;
    }

    public void setShelvingLocation(String shelvingLocation) {
        this.shelvingLocation = shelvingLocation;
    }

    @Size(max = 50)
    @Column(name = "meter")
    private String meter;

    public String getMeter() {
        return meter;
    }

    public void setMeter(String meter) {
        this.meter = meter;
    }

    @Size(max = 50)
    @Column(name = "numbers")
    private String numbers;

    public String getNumbers() {
        return numbers;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
    }

    @Size(max = 50)
    @Column(name = "format")
    private String format;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = (format == null || format.length() <= 50) ? format : format.substring(0, 50);
    }

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @NotNull
    @ManyToOne
    @JoinColumn(name = "record_id", nullable = false)
    private Record record;

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public void mergeWith(ArchiveHoldingInfo other) {
        setShelvingLocation(other.getShelvingLocation());
        setMeter(other.getMeter());
        setNumbers(other.getNumbers());
        setFormat(other.getFormat());
        setNote(other.getNote());
    }
}
