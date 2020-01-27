package org.socialhistoryservices.delivery.request.entity;

import org.socialhistoryservices.delivery.record.entity.Holding;

import java.util.Date;
import java.util.List;

public abstract class Request {

    /**
     * Returns the name of the person making the request.
     *
     * @return The name of the person.
     */
    public abstract String getName();

    /**
     * Returns the email address of the person making the request.
     *
     * @return The email address of the person.
     */
    public abstract String getEmail();

    /**
     * Get the Request's creation date.
     *
     * @return the Request's creation date.
     */
    public abstract Date getCreationDate();

    /**
     * Set the Request's date.
     *
     * @param creationDate the Request's date.
     */
    public abstract void setCreationDate(Date creationDate);

    /**
     * Returns all holdings associated with this request.
     *
     * @return A list of holdings.
     */
    public abstract List<Holding> getHoldings();

    /**
     * Returns all HoldingRequests associated with this request.
     *
     * @return A list of HoldingRequests.
     */
    public abstract List<? extends HoldingRequest> getHoldingRequests();
}
