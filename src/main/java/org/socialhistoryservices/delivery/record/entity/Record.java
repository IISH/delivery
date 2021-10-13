package org.socialhistoryservices.delivery.record.entity;

import org.apache.commons.collections.functors.InstantiateFactory;
import org.apache.commons.collections.list.LazyList;
import org.socialhistoryservices.delivery.reproduction.util.Copies;
import org.socialhistoryservices.delivery.reproduction.util.Pages;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Record entity representing an archive, book, item in an archive,
 * or any other item in the IISH collection.
 */
@Entity
@Table(name = "records", indexes = {@Index(columnList = "external_info_id", name = "records_external_info_fk")})
public class Record {

    @Column(name = "cataloged", columnDefinition = "boolean default true not null")
    private boolean cataloged = true;

    /**
     * Type of restrictions on the use of the record.
     */
    public enum RestrictionType {
        RESTRICTED,
        OPEN,
        CLOSED,
        INHERIT,
    }

    /**
     * Stores information about the number of pages.
     */
    @Transient
    private Pages pages;

    /**
     * Stores information about the number of copies
     */
    @Transient
    private Copies copies;

    /**
     * The Record's id.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    /**
     * Get the Record's id.
     *
     * @return the Record's id.
     */
    public int getId() {
        return id;
    }

    /**
     * The Record's pid.
     */
    @NotBlank
    @Size(max = 255)
    @Column(name = "pid", nullable = false, unique = true)
    private String pid;

    /**
     * Get the Record's pid.
     *
     * @return the Record's pid.
     */
    public String getPid() {
        return pid;
    }

    /**
     * Get the parent pid, unless there is no parent. Then take this pid.
     *
     * @return The parent pid.
     */
    public String getParentPid() {
        if (parent == null)
            return pid;
        return parent.pid;
    }

    /**
     * Set the Record's pid.
     *
     * @param pid the Record's pid.
     */
    public void setPid(String pid) {
        this.pid = pid;
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "external_info_id")
    private ExternalRecordInfo externalInfo;

    /**
     * Get the external record info.
     *
     * @return The info object.
     */
    public ExternalRecordInfo getExternalInfo() {
        return externalInfo;
    }

    /**
     * Set the external info (preferably from IISHRecordLookupService).
     *
     * @param info The info.
     */
    public void setExternalInfo(ExternalRecordInfo info) {
        this.externalInfo = info;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "external_info_updated")
    private Date externalInfoUpdated;

    /**
     * Get the date/time the external info of this record was last updated.
     *
     * @return The date/time the external info of this record was last updated.
     */
    public Date getExternalInfoUpdated() {
        return externalInfoUpdated;
    }

    /**
     * Set the date/time the external info of this record was last updated.
     *
     * @param externalInfoUpdated The date/time the external info of this record was last updated.
     */
    public void setExternalInfoUpdated(Date externalInfoUpdated) {
        this.externalInfoUpdated = externalInfoUpdated;
    }

    /**
     * Get the Record's title.
     *
     * @return the Record's title.
     */
    public String getTitle() {
        return externalInfo.getTitle();
    }

    /**
     * Set the Record's title.
     *
     * @param title the Record's title.
     */
    public void setTitle(String title) {
        externalInfo.setTitle(title);
    }

    /**
     * Get the copyright holder.
     *
     * @return The holder of the copyright.
     */
    public String getCopyright() {
        return externalInfo.getCopyright();
    }

    /**
     * Returns whether IISH is (one of) the copyright holder.
     *
     * @return Whether IISH is (one of) the copyright holder.
     */
    public boolean isCopyrightIISH() {
        if (externalInfo.getCopyright() != null) {
            String copyright = externalInfo.getCopyright().toLowerCase();
            return (copyright.contains("iish") || copyright.contains("iisg"));
        }
        return false;
    }

    /**
     * Get the publication status.
     *
     * @return the publication status.
     */
    public ExternalRecordInfo.PublicationStatus getPublicationStatus() {
        return externalInfo.getPublicationStatus();
    }

    /**
     * Get the restriction.
     *
     * @return the restriction.
     */
    public ExternalRecordInfo.Restriction getRestriction() {
        ExternalRecordInfo.Restriction restriction = externalInfo.getRestriction();
        if (restriction == ExternalRecordInfo.Restriction.DATE_RESTRICTED)
            return ExternalRecordInfo.Restriction.RESTRICTED;
        return restriction;
    }

    /**
     * Get the physical description.
     *
     * @return the physical description.
     */
    public String getPhysicalDescription() {
        return externalInfo.getPhysicalDescription();
    }

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArchiveHoldingInfo> archiveHoldingInfo = new ArrayList<>();

    /**
     * Get the archive holding info.
     *
     * @return The info object.
     */
    public List<ArchiveHoldingInfo> getArchiveHoldingInfo() {
        return archiveHoldingInfo;
    }

    /**
     * Set the archive holding info (preferably from IISHRecordLookupService.getArchiveHoldingInfoByPid).
     *
     * @param archiveHoldingInfo The info.
     */
    public void setArchiveHoldingInfo(List<ArchiveHoldingInfo> archiveHoldingInfo) {
        this.archiveHoldingInfo.clear();
        for (ArchiveHoldingInfo ahi : archiveHoldingInfo) {
            ahi.setRecord(this);
            this.archiveHoldingInfo.add(ahi);
        }
    }

    /**
     * The Record's parent.
     */
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Record parent;

    /**
     * Get the Record's parent.
     *
     * @return the Record's parent.
     */
    public Record getParent() {
        return parent;
    }

    /**
     * Set the Record's parent.
     *
     * @param parent the Record's parent.
     */
    public void setParent(Record parent) {
        this.parent = parent;
    }

    /**
     * Get the top-level root of this record.
     *
     * @return The top record.
     */
    public Record getRoot() {
        Record root = this;
        while (root.getParent() != null)
            root = root.getParent();
        return root;
    }

    /**
     * Child records in this parent.
     */
    @OrderBy("pid asc")
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Record> children;

    /**
     * Set the set of children associated with this record.
     *
     * @param cl The list of child records.
     */
    public void setChildren(List<Record> cl) {
        children = cl;
    }

    /**
     * Get the set of children associated with this record.
     *
     * @return The set of children.
     */
    public List<Record> getChildren() {
        return children;
    }

    /**
     * Get the origin of the record, which is either a catalog or delivery.
     *
     * @return True if the record lives in a catalog
     */
    public boolean isCataloged() {
        return cataloged;
    }

    /**
     * Get the origin of the record, which is either a catalog (true) or delivery.
     * @param cataloged True if from a catalog
     */
    public void setCataloged(boolean cataloged) {
        this.cataloged = cataloged;
    }


    /**
     * Holdings associated with the record.
     */
    @NotNull
    @OrderBy
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Holding> holdings;

    /**
     * Get the set of holdings associated with this record.
     *
     * @return The set of holdings.
     */
    public List<Holding> getHoldings() {
        return holdings;
    }

    /**
     * Add a set of holdings to this record.
     *
     * @param hs The set of holdings to add.
     */
    public void setHoldings(List<Holding> hs) {
        holdings = hs;
    }

    /**
     * Add a holding to this record.
     *
     * @param h The holding to add.
     */
    public void addHolding(Holding h) {
        holdings.add(h);
    }

    /**
     * Merge other record's data with this record. ID, title,
     * and PID are not copied.
     *
     * @param other The other record.
     */
    public void mergeWith(Record other) {
        setTitle(other.getTitle());
        setPid(other.getPid());
        setParent(other.getParent());

        getExternalInfo().mergeWith(other.getExternalInfo());

        // Merge holdings.
        if (other.getHoldings() == null) {
            holdings = new ArrayList<>();
        }
        else {
            // Delete holdings that were not provided.
            deleteHoldingsNotInProvidedRecord(other);

            // Add/update provided.
            addOrUpdateHoldingsProvidedByRecord(other);
        }
    }

    public void mergeHoldingsWith(Record other) {
        deleteHoldingsNotInProvidedRecord(other);

        // Add/update provided.
        addOrUpdateHoldingsProvidedByRecord(other);
    }

    /**
     * Add/Update the holdings provided by the provided record.
     *
     * @param other The provided record.
     */
    private void addOrUpdateHoldingsProvidedByRecord(Record other) {
        for (Holding h : other.getHoldings()) {
            boolean has = false;
            for (Holding h2 : holdings) {
                if (h.getSignature().equals(h2.getSignature())) {
                    h2.mergeWith(h);
                    has = true;
                }
            }

            if (!has) {
                h.setRecord(this);
                h.setStatus(Holding.Status.AVAILABLE);
                holdings.add(h);
            }
        }
    }

    /**
     * Remove the holdings from this record, which are not in the other record.
     *
     * @param other The other record.
     */
    private void deleteHoldingsNotInProvidedRecord(Record other) {
        Iterator<Holding> it = getHoldings().iterator();
        while (it.hasNext()) {
            Holding h = it.next();

            boolean has = false;
            for (Holding h2 : other.getHoldings()) {
                if (h.getSignature().equals(h2.getSignature())) {
                    has = true;
                    break;
                }
            }

            if (!has) {
                it.remove();
            }
        }
    }

    /**
     * Returns the Pages object for this record.
     *
     * @return The Pages object.
     */
    public Pages getPages() {
        if (pages == null)
            pages = new Pages(this);
        return pages;
    }

    /**
     * Returns the Copies object for this record.
     *
     * @return The Copies object.
     */
    public Copies getCopies() {
        if (copies == null)
            copies = new Copies(this);
        return copies;
    }

    /**
     * Helper method to determine the price for this reocrd.
     *
     * @param price The price per page/copy.
     * @return The total price for this record based on the given price per page/copy.
     */
    public BigDecimal determinePrice(BigDecimal price) {
        if (this.externalInfo.getMaterialType() == ExternalRecordInfo.MaterialType.BOOK) {
            if (getPages().containsNumberOfPages())
                price = price.multiply(new BigDecimal(pages.getNumberOfPages()));
        }
        else {
            if (getCopies().containsNumberOfCopies())
                price = price.multiply(new BigDecimal(copies.getNumberOfCopies()));
        }
        return price;
    }

    /**
     * Determine whether this record is open for reproductions.
     *
     * @return True if this record is open for reproduction requests.
     */
    public boolean isOpenForReproduction() {
        if (getPublicationStatus() == ExternalRecordInfo.PublicationStatus.UNKNOWN) {
            return (getExternalInfo().getMaterialType() != ExternalRecordInfo.MaterialType.VISUAL) &&
                    (getExternalInfo().getMaterialType() != ExternalRecordInfo.MaterialType.MOVING_VISUAL);
        }

        return (getPublicationStatus() != ExternalRecordInfo.PublicationStatus.MINIMAL) &&
                (getPublicationStatus() != ExternalRecordInfo.PublicationStatus.CLOSED);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTitle());

        boolean isArchive = getExternalInfo().getMaterialType() == ExternalRecordInfo.MaterialType.ARCHIVE;
        if (isArchive) {
            sb.append(" - ");
            if (getParent() != null) {
                sb.append(getParent().getHoldings().get(0).getSignature());
                sb.append(" - ");
            }
            sb.append(getHoldings().get(0).getSignature());
        }

        if (!isArchive && (getExternalInfo().getAuthor() != null)) {
            sb.append(" / ");
            sb.append(getExternalInfo().getAuthor());
        }

        return sb.toString().trim();
    }

    /**
     * Initialize defaults.
     */
    public Record() {
        // TODO: Reason for Lazy List?
        holdings = LazyList.decorate(new ArrayList<Holding>(), new InstantiateFactory(Holding.class));
        children = new ArrayList<>();
        externalInfo = new ExternalRecordInfo();
        setExternalInfoUpdated(new Date());
    }
}
