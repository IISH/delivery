/**
 * Copyright (C) 2013 International Institute of Social History
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.socialhistoryservices.delivery.record.entity;

import org.apache.commons.collections.functors.InstantiateFactory;
import org.apache.commons.collections.list.LazyList;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.NotBlank;
import org.socialhistoryservices.delivery.reproduction.util.Pages;

import javax.persistence.*;
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
@Table(name="records")
public class Record {
    /** Type of restrictions on the use of the record. */
    public enum RestrictionType {
        RESTRICTED,
        OPEN,
        CLOSED,
        INHERIT,
    }

    /** Stores information about the number of pages. */
    @Transient
    private Pages pages;

    /** The Record's id. */
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    /**
     * Get the Record's id.
     * @return the Record's id.
     */
    public int getId() {
        return id;
    }

    /** The Record's pid. */
    @NotBlank
    @Size(max=255)
    @Column(name="pid", nullable=false, unique = true)
    private String pid;

    /**
     * Get the Record's pid.
     * @return the Record's pid.
     */
    public String getPid() {
        return pid;
    }

    /**
     * Set the Record's pid.
     * @param pid the Record's pid.
     */
    public void setPid(String pid) {
        this.pid = pid;
    }

    @Index(name="records_external_info_fk")
    @OneToOne(cascade=CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name="external_info_id")
    private ExternalRecordInfo externalInfo;

    /**
     * Get the external record info.
     * @return The info object.
     */
    public ExternalRecordInfo getExternalInfo() {
        return externalInfo;
    }

    /**
     * Set the external info (preferably from IISHRecordLookupService
     * .getRecordMetaDataByPid).
     * @param info The info.
     */
    public void setExternalInfo(ExternalRecordInfo info) {
        this.externalInfo = info;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="external_info_updated")
    private Date externalInfoUpdated;

    /**
     * Get the date/time the external info of this record was last updated.
     * @return The date/time the external info of this record was last updated.
     */
    public Date getExternalInfoUpdated() {
        return externalInfoUpdated;
    }

    /**
     * Set the date/time the external info of this record was last updated.
     * @param externalInfoUpdated The date/time the external info of this record was last updated.
     */
    public void setExternalInfoUpdated(Date externalInfoUpdated) {
        this.externalInfoUpdated = externalInfoUpdated;
    }

    /**
     * Get the Record's title.
     * @return the Record's title.
     */
    public String getTitle() {
        return externalInfo.getTitle();
    }

    /**
     * Set the Record's title.
     * @param title the Record's title.
     */
    public void setTitle(String title) {
        externalInfo.setTitle(title);
    }

    /**
     * Get the copyright holder.
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
     * @return the publication status.
     */
    public ExternalRecordInfo.PublicationStatus getPublicationStatus() {
        return externalInfo.getPublicationStatus();
    }

    /**
     * Get the restriction.
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
     * @return the physical description.
     */
    public String getPhysicalDescription() {
        return externalInfo.getPhysicalDescription();
    }

    /** The Record's comments. */
    @Column(name="comments", columnDefinition="TEXT")
    private String comments;

    /**
     * Get the Record's comments.
     * @return the Record's comments.
     */
    public String getComments() {
        return comments;
    }

    /**
     * Get the comments, getting it from the parent record if not specified.
     * @return Any comments data in the parent tree.
     */
    public String getRealComments() {
        // Check parents
        Record obj = this;
        while (obj != null) {
            if (obj.getComments() != null) {
                return obj.getComments();
            }
            obj = obj.getParent();
        }
        return null;
    }

    /**
     * Set the Record's comments.
     * @param comments the Record's comments.
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /** The Record's parent. */
    @ManyToOne
    @JoinColumn(name="parent_id")
    private Record parent;

    /**
     * Get the Record's parent.
     * @return the Record's parent.
     */
    public Record getParent() {
        return parent;
    }

    /**
     * Set the Record's parent.
     * @param parent the Record's parent.
     */
    public void setParent(Record parent) {
        this.parent = parent;
    }

    /**
     * Get the top-level root of this record.
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
    @OneToMany(mappedBy="parent", cascade=CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<Record> children;

    /**
     * Set the set of children associated with this record.
     * @param cl The list of child records.
     */
    public void setChildren(List<Record> cl) {
        children = cl;
    }

    /**
     * Get the set of children associated with this record.
     * @return The set of children.
     */
    public List<Record> getChildren() {
        return children;
    }

    /**
     * Return all the children of this record with a different state.
     * @return A list of all children with a different state.
     */
    public List<Record> getChildrenDifferentState() {
        List<Record> children = new ArrayList<Record>();
        for (Record childRecord : getChildren()) {
            if (childRecord.hasDifferentStateThanParent())
                children.add(childRecord);
        }
        return children;
    }

    /** The Record's contact. */
    @ManyToOne(cascade=CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name="contact_id")
    private Contact contact;

    /**
     * Get the Record's contact.
     * @return the Record's contact.
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * Get the contact, getting it from the parent record if not specified.
     * @return Any contact data in the parent tree.
     */
    public Contact getRealContact() {
        // Check parents
        Record obj = this;
        while (obj != null) {
            if (obj.getContact() != null) {
                return obj.getContact();
            }
            obj = obj.getParent();
        }
        return null;
    }

    /**
     * Set the Record's contact.
     * @param contact the Record's contact.
     */
    public void setContact(Contact contact) {
        this.contact = contact;
    }

    /**
     * Holdings associated with the record.
     */
    @NotNull
    @OrderBy
    @OneToMany(mappedBy="record", cascade=CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<Holding> holdings;

    /**
     * Get the set of holdings associated with this record.
     * @return The set of holdings.
     */
    public List<Holding> getHoldings() {
        return holdings;
    }

    /**
     * Add a set of holdings to this record.
     * @param hs The set of holdings to add.
     */
    public void setHoldings(List<Holding> hs) {
        holdings = hs;
    }

	/**
	 * Add a holding to this record.
	 * @param h The holding to add.
	 */
	public void addHolding(Holding h) {
		holdings.add(h);
	}

    /**
     * Merge other record's data with this record. ID, title,
     * and PID are not copied.
     * @param other The other record.
     */
    public void mergeWith(Record other) {
        setTitle(other.getTitle());
        setPid(other.getPid());
        setParent(other.getParent());
        setComments(other.getComments());

        getExternalInfo().mergeWith(other.getExternalInfo());

        // Merge contact.
        Contact c = getContact();
        Contact oc = other.getContact();
        if (c == null || oc == null) {
            setContact(oc);
        } else {
            c.mergeWith(oc);
        }

        // Merge holdings.
        if (other.getHoldings() == null) {
            holdings = new ArrayList<Holding>();
        } else {
            // Delete holdings that were not provided.
            deleteHoldingsNotInProvidedRecord(other);

            // Add/update provided.
            addOrUpdateHoldingsProvidedByRecord(other);
        }
    }

    /**
     * Add/Update the holdings provided by the provided record.
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
     * @return The Pages object.
     */
    public Pages getPages() {
        if (pages == null)
            pages = new Pages(this);
        return pages;
    }

    /**
     * Helper method to determine the price on the number of pages for this reocrd.
     * @param price The price per page.
     * @return The total price for this record based on the given price per page.
     */
    public BigDecimal determinePriceByPages(BigDecimal price) {
        Pages pages = getPages();
        if (pages.containsNumberOfPages())
            price = price.multiply(new BigDecimal(pages.getNumberOfPages()));
        return price;
    }

    /**
     * Determine whether this record is open for reproductions.
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

    /**
     * Determine if the state is different than the parent record.
     * @return Whether the state differs.
     */
    public boolean hasDifferentStateThanParent() {
        if (parent == null)
            return false;

        Holding h = getHoldings().get(0), hParent = parent.getHoldings().get(0);
        return ((getRestriction() != parent.getRestriction())
            || (getPublicationStatus() != parent.getPublicationStatus())
            || (isOpenForReproduction() != parent.isOpenForReproduction())
            || (h.getStatus() != hParent.getStatus())
            || (h.getUsageRestriction() != hParent.getUsageRestriction()));
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
        holdings = LazyList.decorate(new ArrayList<Holding>(), new InstantiateFactory(Holding.class));
        children = new ArrayList<Record>();
        externalInfo = new ExternalRecordInfo();
        setExternalInfoUpdated(new Date());
    }
}
