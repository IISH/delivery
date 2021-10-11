package org.socialhistoryservices.delivery.request.entity;

import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.record.entity.Record;

public abstract class HoldingRequest {
    /**
     * Get the HoldingRequest's id.
     *
     * @return the HoldingRequest's id.
     */
    public abstract int getId();

    /**
     * Get the HoldingRequest's holding.
     *
     * @return the HoldingRequest's holding.
     */
    public abstract Holding getHolding();

    /**
     * Set the HoldingRequest's holding.
     *
     * @param holding the HoldingRequest's holding.
     */
    public abstract void setHolding(Holding holding);

    /**
     * Get the HoldingRequest's request.
     *
     * @return the HoldingRequest's request.
     */
    public abstract Request getRequest();

    /**
     * Set the HoldingRequest's request.
     *
     * @param request the HoldingRequest's request.
     */
    public abstract void setRequest(Request request);

    /**
     * Get the comment on a specific holding in a request.
     *
     * @return The comment.
     */
    public abstract String getComment();

    /**
     * Set the comment on a specific holding in a request.
     *
     * @param val The value to set the comment to.
     */
    public abstract void setComment(String val);

    /**
     * Set whether the request was printed or not.
     *
     * @param b True to consider the request to be printed at least once, false otherwise.
     */
    public abstract void setPrinted(boolean b);

    /**
     * Check if the Request (is considered) to be printed at least once.
     *
     * @return True if the Request was printed at least once, false otherwise.
     */
    public abstract boolean isPrinted();

    /**
     * Is the holding completed for this request?
     *
     * @return Whether this holding is completed for this request.
     */
    public abstract boolean isCompleted();

    /**
     * Set the holding completed for this request?
     *
     * @param completed Whether the holding has been completed for this request.
     */
    public abstract void setCompleted(boolean completed);

    /**
     * Merge this HoldingRequest with another HoldingRequest.
     *
     * @param other Another HoldingRequest.
     */
    public void mergeWith(HoldingRequest other) {
        setComment(other.getComment());
        setHolding(other.getHolding());
    }

    public String toShortString() {
        Record record = getHolding().getRecord();
        ExternalRecordInfo externalInfo = record.getExternalInfo();

        StringBuilder sb = new StringBuilder();
        sb.append(externalInfo.getTitle());
        sb.append(" - ");

        boolean isArchive = externalInfo.getMaterialType() == ExternalRecordInfo.MaterialType.ARCHIVE;
        if (isArchive && (record.getParent() != null)) {
            sb.append(record.getParent().getHoldings().get(0).getSignature());
            sb.append(" - ");
        }
        sb.append(getHolding().getSignature());

        if (getComment() != null) {
            sb.append(" - ");
            sb.append(getComment());
        }

        return sb.toString().trim();
    }

    @Override
    public String toString() {
        Record record = getHolding().getRecord();
        ExternalRecordInfo externalInfo = record.getExternalInfo();

        StringBuilder sb = new StringBuilder();
        sb.append(externalInfo.getTitle());
        sb.append(" - ");

        boolean isArchive = externalInfo.getMaterialType() == ExternalRecordInfo.MaterialType.ARCHIVE;
        if (isArchive && (record.getParent() != null)) {
            sb.append(record.getParent().getHoldings().get(0).getSignature());
            sb.append(" - ");
        }
        sb.append(getHolding().getSignature());

        if (!isArchive && (externalInfo.getAuthor() != null)) {
            sb.append(" / ");
            sb.append(externalInfo.getAuthor());
        }

        if (getComment() != null) {
            sb.append(" - ");
            sb.append(getComment());
        }

        return sb.toString().trim();
    }
}
