package org.socialhistoryservices.delivery.reproduction.service;

import org.socialhistoryservices.delivery.config.DeliveryProperties;
import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction;
import org.socialhistoryservices.delivery.reproduction.entity.Order;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction;
import org.socialhistoryservices.delivery.reproduction.entity.ReproductionStandardOption;
import org.socialhistoryservices.delivery.request.service.RequestPrintable;
import org.springframework.context.MessageSource;

import java.text.DateFormat;

/**
 * Represents a printable reproduction.
 */
public class ReproductionPrintable extends RequestPrintable {

    /**
     * Construct the printable.
     *
     * @param hr      The holding reproduction to construct from.
     * @param mSource The message source to fetch localized messages.
     * @param format  The date format to use.
     */
    public ReproductionPrintable(HoldingReproduction hr, MessageSource mSource, DateFormat format, DeliveryProperties prop) {
        super(hr, mSource, format, prop);
    }

    /**
     * Draw the reproduction information.
     *
     * @param drawInfo Draw offsets.
     */
    @Override
    protected void draw(DrawInfo drawInfo) {
        drawBarcode(drawInfo, this.holdingRequest.getId());
        drawReturnNotice(drawInfo);

        drawRepro(drawInfo);
        drawId(drawInfo);
        drawCreationDate(drawInfo);
        drawPayed(drawInfo);

        drawNewLine(drawInfo);

        drawName(drawInfo);
        drawEmail(drawInfo);

        drawNewLine(drawInfo);

        drawRecordInfo(drawInfo);
        drawMaterialInfo(drawInfo);
        drawHoldingPid(drawInfo, holdingRequest.getHolding().determinePid());
        drawType(drawInfo, holdingRequest.getHolding().getSignature());
        drawLocationInfo(drawInfo);

        drawNewLine(drawInfo);
        drawReproductionInformation(drawInfo);

        drawNewLine(drawInfo);
        drawReproductionComment(drawInfo);
    }

    /**
     * The intended recipient of this print.
     *
     * @return The recipient.
     */
    @Override
    protected String getRecipient() {
        return getMessage("print.repro", "Intended for repro");
    }

    /**
     * Draw the notice that this print is intended for repro.
     *
     * @param drawInfo Draw offsets.
     */
    private void drawRepro(DrawInfo drawInfo) {
        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.value = getMessage("print.repro", "Intended for repro");
        drawValueInfo.font = boldFont;
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draws the number (id) of the reproduction.
     *
     * @param drawInfo Draw offsets.
     */
    private void drawId(DrawInfo drawInfo) {
        HoldingReproduction hr = (HoldingReproduction) holdingRequest;
        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("reproduction.short.id", "Repr. nr");
        drawValueInfo.value = String.valueOf(hr.getReproduction().getId());
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draws the number (id) of the reproduction.
     *
     * @param drawInfo Draw offsets.
     */
    private void drawPayed(DrawInfo drawInfo) {
        HoldingReproduction hr = (HoldingReproduction) holdingRequest;
        Order order = hr.getReproduction().getOrder();

        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("reproduction.payed", "Paid?");

        if ((order != null) && (hr.getReproduction().getStatus().ordinal() >= Reproduction.Status.ACTIVE.ordinal())) {
            drawValueInfo.value = getMessage("yes", "Yes") + " (#" + String.valueOf(order.getId()) + ")";
        }
        else {
            drawValueInfo.value = getMessage("no", "No");
        }

        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draws the email address of the person making the request.
     *
     * @param drawInfo Draw offsets.
     */
    private void drawEmail(DrawInfo drawInfo) {
        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("request.email", "Email");
        drawValueInfo.value = holdingRequest.getRequest().getEmail();
        drawValueInfo.font = italicFont;
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draws the reproduction information.
     *
     * @param drawInfo Draw offsets.
     */
    private void drawReproductionInformation(DrawInfo drawInfo) {
        HoldingReproduction hr = (HoldingReproduction) holdingRequest;
        if (hr.getStandardOption() != null) {
            ReproductionStandardOption standardOption = hr.getStandardOption();

            DrawValueInfo drawValueInfoOption = new DrawValueInfo(drawInfo);
            drawValueInfoOption.value = standardOption.getOptionName();
            drawValueInfoOption.font = boldFont;
            drawKeyValueNewLine(drawValueInfoOption);

            DrawValueInfo drawValueInfoDescr = new DrawValueInfo(drawInfo);
            drawValueInfoDescr.value = standardOption.getOptionDescription();
            drawValueInfoDescr.font = italicFont;
            drawKeyValueNewLine(drawValueInfoDescr);
        }
        else {
            DrawValueInfo drawValueInfoCustomHeading = new DrawValueInfo(drawInfo);
            drawValueInfoCustomHeading.value =
                getMessage("reproduction.customReproduction.backend", "Custom reproduction");
            drawValueInfoCustomHeading.font = boldFont;
            drawKeyValueNewLine(drawValueInfoCustomHeading);

            DrawValueInfo drawValueInfoCustom = new DrawValueInfo(drawInfo);
            drawValueInfoCustom.value = hr.getCustomReproductionCustomer();
            drawValueInfoCustom.font = italicFont;
            drawKeyValueNewLine(drawValueInfoCustom);
        }
    }

    /**
     * Draws the reproduction information.
     *
     * @param drawInfo Draw offsets.
     */
    private void drawReproductionComment(DrawInfo drawInfo) {
        HoldingReproduction hr = (HoldingReproduction) holdingRequest;

        if (hr.getReproduction().getComment() != null) {
            DrawValueInfo drawValueInfoCustomHeading = new DrawValueInfo(drawInfo);
            drawValueInfoCustomHeading.value =
                    getMessage("reproduction.comment_singular", "Comment");
            drawValueInfoCustomHeading.font = boldFont;
            drawKeyValueNewLine(drawValueInfoCustomHeading);

            DrawValueInfo drawValueInfoCustom = new DrawValueInfo(drawInfo);
            drawValueInfoCustom.value = hr.getReproduction().getComment();
            drawValueInfoCustom.font = italicFont;
            drawKeyValueNewLine(drawValueInfoCustom);
        }
    }

    /**
     * Draw the PID of the holding.
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawHoldingPid(DrawInfo drawInfo, String value) {
        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("holding.pid", "Item PID");
        drawValueInfo.value = value;
        drawValueInfo.font = italicFont;
        drawValueInfo.underline = true;
        drawKeyValueNewLine(drawValueInfo);
    }
}
