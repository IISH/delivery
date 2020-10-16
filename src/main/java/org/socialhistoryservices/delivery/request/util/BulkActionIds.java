package org.socialhistoryservices.delivery.request.util;

/**
 * Holds the request id and the holding id of a bulk request.
 */
public class BulkActionIds {
    private final int requestId;
    private final int holdingId;

    public BulkActionIds(int requestId, int holdingId) {
        this.requestId = requestId;
        this.holdingId = holdingId;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getHoldingId() {
        return holdingId;
    }
}
