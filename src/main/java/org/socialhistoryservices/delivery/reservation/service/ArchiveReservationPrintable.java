package org.socialhistoryservices.delivery.reservation.service;

import org.socialhistoryservices.delivery.config.DeliveryProperties;
import org.socialhistoryservices.delivery.record.entity.ArchiveHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.record.entity.Record;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation;
import org.springframework.context.MessageSource;

import java.text.DateFormat;
import java.util.List;

/**
 * Represents a printable archive reservation.
 */
public class ArchiveReservationPrintable extends ReservationPrintable {

    /**
     * Construct the printable.
     *
     * @param hr      The holding reservation to construct from.
     * @param mSource The message source to fetch localized messages.
     * @param format  The date format to use.
     */
    public ArchiveReservationPrintable(HoldingReservation hr, MessageSource mSource, DateFormat format, DeliveryProperties prop) {
        super(hr, mSource, format, prop);
    }

    /**
     * Draw the archive reservation information.
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

        drawArchive(drawInfo);

        // if there is container info show it, else show inventory number
        if (holdingRequest.getHolding().getRecord().getParent() != null &&
                holdingRequest.getHolding().getRecord().getExternalInfo() != null &&
                !holdingRequest.getHolding().getRecord().getExternalInfo().getContainer().equals("")) {
            drawContainer(drawInfo);
        } else {
            drawInventoryNumber(drawInfo);
        }

        drawRestrictedNotice(drawInfo);

        drawNewLine(drawInfo);

        drawHoldingInfo(drawInfo);
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
     * Draw the archive.
     *
     * @param drawInfo Draw offsets.
     */
    private void drawArchive(DrawInfo drawInfo) {
        Record record = getHoldingRequest().getHolding().getRecord();
        if (record.getParent() != null) {
            record = record.getParent();
        }

        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.value = record.getTitle() + " (" + record.getHoldings().get(0).getSignature() + ")";
        drawValueInfo.font = boldFont;
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draw the inventory number.
     *
     * @param drawInfo Draw offsets.
     */
    private void drawContainer(DrawInfo drawInfo) {
        if (holdingRequest.getHolding().getRecord().getParent() != null) {
            DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
            drawValueInfo.key = getMessage("record.container", "Container");
            drawValueInfo.value = holdingRequest.getHolding().getRecord().getExternalInfo().getContainer();
            drawKeyValueNewLine(drawValueInfo);
        }
    }

    /**
     * Draw the inventory number.
     *
     * @param drawInfo Draw offsets.
     */
    private void drawInventoryNumber(DrawInfo drawInfo) {
        if (holdingRequest.getHolding().getRecord().getParent() != null) {
            DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
            drawValueInfo.key = getMessage("record.inventory", "Inventory Nr");
            drawValueInfo.value = holdingRequest.getHolding().getSignature();
            drawKeyValueNewLine(drawValueInfo);
        }
    }

    /**
     * Draw the restriction notice.
     *
     * @param drawInfo Draw restriction notice.
     */
    private void drawRestrictedNotice(DrawInfo drawInfo) {
        ExternalRecordInfo.Restriction restriction =
                holdingRequest.getHolding().getRecord().getExternalInfo().getRestriction();
        String restrictionValue = getMessage("record.externalInfo.restriction." + restriction, "");

        String permissionRequired = "";
        if (holdingRequest.getHolding().getRecord().getRestriction() == ExternalRecordInfo.Restriction.RESTRICTED) {
            permissionRequired = " (";
            permissionRequired += getMessage("record.restriction.required", "Permission required!");
            permissionRequired += ")";
        }

        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("record.restriction", "Restriction");
        drawValueInfo.value = restrictionValue + permissionRequired;
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draw the archive holding info.
     *
     * @param drawInfo Draw offsets.
     */
    private void drawHoldingInfo(DrawInfo drawInfo) {
        Record record = holdingRequest.getHolding().getRecord();
        if (record.getParent() != null) {
            record = record.getParent();
        }

        boolean printNoMoreSpaceNotice = true;
        List<ArchiveHoldingInfo> archiveHoldingInfo = record.getArchiveHoldingInfo();
        boolean smallFont = archiveHoldingInfo.size() > 10;
        for (ArchiveHoldingInfo ahi : archiveHoldingInfo) {
            int heightOneLine = determineHeight(drawInfo, "-", (smallFont) ? smallNormalFont : normalFont);

            // Empty string: assumption that the first line will never exceed more than one line
            int heightFirstLine = 0;
            if ((ahi.getShelvingLocation() != null) || (ahi.getMeter() != null) || (ahi.getNumbers() != null)
                    || (ahi.getFormat() != null)) {
                heightFirstLine = heightOneLine;
            }

            int heightLineNote = 0;
            if (ahi.getNote() != null) {
                heightLineNote = determineHeight(drawInfo, ahi.getNote(), (smallFont) ? smallNormalFont : normalFont);
            }

            // Only if there is still space, draw holding info
            int afterOffsetY = (drawInfo.offsetY + heightFirstLine + heightLineNote);
            int maxOffsetY = (drawInfo.height - 100 - (heightOneLine * 2));
            if (afterOffsetY < maxOffsetY) {
                if (ahi.getShelvingLocation() != null) {
                    DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
                    drawValueInfo.key = getMessage("archiveHoldingInfo.shelvingLocation", "Location");
                    drawValueInfo.value = ahi.getShelvingLocation();
                    drawValueInfo.font = (smallFont) ? smallBoldFont : boldFont;
                    drawValueInfo.boldKey = false;
                    drawValueInfo.tab = false;
                    drawKeyValue(drawValueInfo);
                }

                if (ahi.getMeter() != null) {
                    DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
                    drawValueInfo.key = getMessage("archiveHoldingInfo.meter", "Meters");
                    drawValueInfo.value = ahi.getMeter();
                    drawValueInfo.font = (smallFont) ? smallBoldFont : boldFont;
                    drawValueInfo.boldKey = false;
                    drawValueInfo.tab = false;
                    drawKeyValue(drawValueInfo);
                }

                if (ahi.getNumbers() != null) {
                    DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
                    drawValueInfo.key = getMessage("archiveHoldingInfo.numbers", "Amount");
                    drawValueInfo.value = ahi.getNumbers();
                    drawValueInfo.font = (smallFont) ? smallBoldFont : boldFont;
                    drawValueInfo.boldKey = false;
                    drawValueInfo.tab = false;
                    drawKeyValue(drawValueInfo);
                }

                if (ahi.getFormat() != null) {
                    DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
                    drawValueInfo.key = getMessage("archiveHoldingInfo.format", "Format");
                    drawValueInfo.value = ahi.getFormat();
                    drawValueInfo.font = (smallFont) ? smallBoldFont : boldFont;
                    drawValueInfo.boldKey = false;
                    drawValueInfo.tab = false;
                    drawKeyValue(drawValueInfo);
                }

                if (ahi.getNote() != null) {
                    drawNewLine(drawInfo);
                    DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
                    drawValueInfo.value = ahi.getNote();
                    drawValueInfo.font = (smallFont) ? smallItalicFont : italicFont;
                    drawKeyValue(drawValueInfo);
                }

                drawNewLine(drawInfo);
                drawInfo.offsetY = drawInfo.offsetY + 2;
            } else if (printNoMoreSpaceNotice) {
                printNoMoreSpaceNotice = false;

                DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
                drawValueInfo.value = getMessage("archiveHoldingInfo.more", "");
                drawValueInfo.font = (smallFont) ? smallBoldFont : boldFont;
                drawValueInfo.underline = true;
                drawKeyValue(drawValueInfo);
                drawNewLine(drawInfo);
            }
        }
    }
}
