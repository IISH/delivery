package org.socialhistoryservices.delivery.reservation.service;

import org.socialhistoryservices.delivery.config.DeliveryProperties;
import org.socialhistoryservices.delivery.request.service.RequestPrintable;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation;
import org.springframework.context.MessageSource;

import java.text.DateFormat;

/**
 * Represents a printable reservation.
 */
public class ReservationPrintable extends RequestPrintable {

    /**
     * Construct the printable.
     *
     * @param hr      The holding reservation to construct from.
     * @param mSource The message source to fetch localized messages.
     * @param format  The date format to use.
     */
    public ReservationPrintable(HoldingReservation hr, MessageSource mSource, DateFormat format,
                                DeliveryProperties prop) {
        super(hr, mSource, format, prop);
    }

    /**
     * Draw the reservation information.
     *
     * @param drawInfo Draw offsets.
     */
    @Override
    protected void draw(DrawInfo drawInfo) {
        drawBarcode(drawInfo, this.holdingRequest.getId());
        drawReturnNotice(drawInfo);

        drawName(drawInfo);
        drawDate(drawInfo);
        drawCreationDate(drawInfo);

        drawNewLine(drawInfo);

        drawRecordInfo(drawInfo);
        drawMaterialInfo(drawInfo);
        drawType(drawInfo, holdingRequest.getHolding().getSignature());
        drawLocationInfo(drawInfo);
    }

    /**
     * The intended recipient of this print.
     *
     * @return The recipient.
     */
    @Override
    protected String getRecipient() {
        return holdingRequest.getRequest().getName();
    }

    /**
     * Draws the date of access.
     *
     * @param drawInfo Draw offsets.
     */
    protected void drawDate(DrawInfo drawInfo) {
        HoldingReservation hr = (HoldingReservation) holdingRequest;
        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("reservation.date", "Date");
        drawValueInfo.value = df.format(hr.getReservation().getDate());
        drawKeyValueNewLine(drawValueInfo);
    }
}
