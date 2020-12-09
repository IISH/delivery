package org.socialhistoryservices.delivery.request.service;

import org.krysalis.barcode4j.BarcodeDimension;
import org.krysalis.barcode4j.output.java2d.Java2DCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.socialhistoryservices.delivery.config.DeliveryProperties;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.record.entity.Record;
import org.socialhistoryservices.delivery.request.entity.HoldingRequest;
import org.socialhistoryservices.delivery.request.util.Barcode;
import org.springframework.context.MessageSource;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Represents a printable request.
 */
public abstract class RequestPrintable implements Printable {
    protected static final int DPI = 150;
    protected static final int KEY_TAB_OFFSET = 80;

    protected Font normalFont;
    protected Font boldFont;
    protected Font italicFont;

    protected Font smallNormalFont;
    protected Font smallBoldFont;
    protected Font smallItalicFont;

    protected DateFormat df;
    protected DeliveryProperties deliveryProperties;
    protected final HoldingRequest holdingRequest;

    private final MessageSource msgSource;

    protected static class DrawInfo {
        public int offsetX;
        public int offsetY;
        public int width;
        public int height;
        public float valueOffset;
        public Graphics2D g2d;

        public DrawInfo(Graphics2D g2d) {
            this.g2d = g2d;
        }
    }

    protected final class DrawValueInfo {
        public DrawInfo drawInfo;
        public String key = null;
        public String value = null;
        public Font font = normalFont;
        public boolean boldKey = true;
        public boolean underline = false;
        public boolean tab = true;

        public DrawValueInfo(DrawInfo drawInfo) {
            this.drawInfo = drawInfo;
        }
    }

    /**
     * Construct the printable.
     *
     * @param hr      The holding request to construct from.
     * @param mSource The message source to fetch localized messages.
     * @param format  The date format to use.
     */
    public RequestPrintable(HoldingRequest hr, MessageSource mSource, DateFormat format, DeliveryProperties prop) {
        deliveryProperties = prop;
        holdingRequest = hr;

        msgSource = mSource;
        df = format;

        normalFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        boldFont = normalFont.deriveFont(Font.BOLD);
        italicFont = normalFont.deriveFont(Font.ITALIC);

        smallNormalFont = normalFont.deriveFont(8f);
        smallBoldFont = smallNormalFont.deriveFont(Font.BOLD);
        smallItalicFont = smallNormalFont.deriveFont(Font.ITALIC);
    }

    /**
     * Returns the holding request to be printed.
     *
     * @return The holding request.
     */
    public HoldingRequest getHoldingRequest() {
        return holdingRequest;
    }

    /**
     * Prints one page for a reservation (one record per page).
     *
     * @param g    The graphics object to draw on.
     * @param pf   The page format.
     * @param page The current page number.
     * @return Whether the page rendered successfully.
     */
    public int print(Graphics g, PageFormat pf, int page) {
        int pageWidth = (int) pf.getImageableWidth();
        int pageHeight = (int) pf.getImageableHeight();

        // Set the imageable area to correspond with the g2d draw area.
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        int halfWidth = pageWidth / 2;

        DrawInfo drawInfo = new DrawInfo(g2d);
        drawInfo.height = pageHeight;
        int rightMargin = 20;

        // Draw all info to the g2d.
        for (int i = 1; i <= 2; i++) {
            drawInfo.width = halfWidth - rightMargin;
            drawInfo.offsetX = halfWidth * (i - 1) + 10;
            drawInfo.offsetY = 15;
            draw(drawInfo);
        }

        // Tell the caller that this page is part of the printed document.
        return PAGE_EXISTS;
    }

    /**
     * Draw the request information.
     *
     * @param drawInfo Draw offsets.
     */
    protected abstract void draw(DrawInfo drawInfo);

    /**
     * The intended recipient of this print.
     *
     * @return The recipient.
     */
    protected abstract String getRecipient();

    /**
     * Draw a barcode on the provided graphics.
     *
     * @param drawInfo Draw offsets.
     * @param number   The barcode number to create a barcode from.
     */
    protected void drawBarcode(DrawInfo drawInfo, int number) {
        Barcode barcode = new Barcode();
        barcode.setModuleWidth(UnitConv.in2mm(5.0f / DPI));
        barcode.setBarHeight(30);
        barcode.setFontSize(12);

        String msg = String.format("%09d", number);
        BarcodeDimension dim = barcode.calcDimensions(msg);

        // Align to the bottom right of the page
        barcode.doQuietZone(true);
        barcode.setQuietZone(drawInfo.width + drawInfo.offsetX - dim.getWidth() - 20);
        barcode.setVerticalQuietZone(drawInfo.height + drawInfo.offsetY - dim.getHeight() - 45);

        // Generate the barcode
        Java2DCanvasProvider canvas = new Java2DCanvasProvider(drawInfo.g2d, 0);
        barcode.generateBarcode(canvas, msg);
    }

    /**
     * Draws the name of the person making the request.
     *
     * @param drawInfo Draw offsets.
     */
    protected void drawName(DrawInfo drawInfo) {
        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("request.name", "Name");
        drawValueInfo.value = holdingRequest.getRequest().getName();
        drawValueInfo.font = italicFont;
        drawValueInfo.underline = true;
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draws the creation date of the request.
     *
     * @param drawInfo Draw offsets.
     */
    protected void drawCreationDate(DrawInfo drawInfo) {
        String dateTimeFormat = deliveryProperties.getDateFormat() + " " + deliveryProperties.getTimeFormat();
        SimpleDateFormat spdf = new SimpleDateFormat(dateTimeFormat);

        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("request.creationDate", "Created at");
        drawValueInfo.value = spdf.format(holdingRequest.getRequest().getCreationDate());
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draw record info.
     *
     * @param drawInfo Draw Offsets.
     */
    protected void drawRecordInfo(DrawInfo drawInfo) {
        Record r = holdingRequest.getHolding().getRecord();

        drawTitle(drawInfo, r.getTitle());
        drawAuthor(drawInfo, r.getExternalInfo().getAuthor());
    }

    /**
     * Draw material info.
     *
     * @param drawInfo Draw Offsets.
     */
    protected void drawMaterialInfo(DrawInfo drawInfo) {
        Record r = holdingRequest.getHolding().getRecord();

        drawMaterialType(drawInfo, r.getExternalInfo().getMaterialType());
        drawComment(drawInfo, holdingRequest.getComment());
    }

    /**
     * Draw location info.
     *
     * @param drawInfo Draw Offsets.
     */
    protected void drawLocationInfo(DrawInfo drawInfo) {
        Holding h = holdingRequest.getHolding();
        Record r = h.getRecord();
    }

    /**
     * Draw the type of the location.
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    protected void drawType(DrawInfo drawInfo, String value) {
        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("holding.signature", "Signature");
        drawValueInfo.value = value;
        drawValueInfo.font = italicFont;
        drawValueInfo.underline = true;
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draw the return notice.
     *
     * @param drawInfo Draw offsets.
     */
    protected void drawReturnNotice(DrawInfo drawInfo) {
        int orgOffsetY = drawInfo.offsetY;
        drawInfo.offsetY = drawInfo.height - 85;

        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.value = getMessage("print.returnForm", "Please return form along with item.");
        drawValueInfo.font = boldFont;
        drawKeyValueNewLine(drawValueInfo);
        drawInfo.offsetY += 15;

        drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.value = getMessage("print.return", "Return or Keep?");
        drawValueInfo.font = boldFont;
        drawKeyValueNewLine(drawValueInfo);
        drawInfo.offsetY += 5;

        drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.value = getRecipient();
        drawValueInfo.font = italicFont;
        drawValueInfo.underline = true;
        drawKeyValueNewLine(drawValueInfo);

        drawInfo.offsetY = orgOffsetY;
    }

    /**
     * Draw the comment of the holding reservation (used to specify
     * years/numbers for serials).
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawComment(DrawInfo drawInfo, String value) {
        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("holdingReservations.comment", "Comment");
        drawValueInfo.value = value;
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draw the pid of the record.
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawPid(DrawInfo drawInfo, String value) {
        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = "PID";
        drawValueInfo.value = value;
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draw the material type.
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawMaterialType(DrawInfo drawInfo, ExternalRecordInfo.MaterialType value) {
        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("record.externalInfo.materialType", "Material");
        drawValueInfo.value = getMessage("record.externalInfo.materialType." + value, "");
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draw the title of the record.
     *
     * @param drawInfo Draw offsets.
     */
    private void drawTitle(DrawInfo drawInfo, String value) {
        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("record.title", "Title");
        drawValueInfo.value = value;
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draw the author of the record (if applicable).
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawAuthor(DrawInfo drawInfo, String value) {
        DrawValueInfo drawValueInfo = new DrawValueInfo(drawInfo);
        drawValueInfo.key = getMessage("record.externalInfo.author", "Author");
        drawValueInfo.value = value;
        drawKeyValueNewLine(drawValueInfo);
    }

    /**
     * Draw a key value pair (and a new line) on the provided graphics object.
     *
     * @param drawValueInfo Draw value info.
     */
    protected void drawKeyValueNewLine(DrawValueInfo drawValueInfo) {
        drawKeyValue(drawValueInfo);
        drawNewLine(drawValueInfo.drawInfo);
    }

    /**
     * Draw a key value pair (and a new line) on the provided graphics object.
     *
     * @param drawValueInfo Draw value info.
     */
    protected void drawKeyValue(DrawValueInfo drawValueInfo) {
        // Do not print key-value pairs with missing value.
        if (drawValueInfo.value == null || drawValueInfo.value.isEmpty()) {
            return;
        }

        drawKey(drawValueInfo);
        drawValue(drawValueInfo);
    }

    /**
     * Draw a new line on the provided graphics object.
     *
     * @param drawInfo Draw offsets.
     */
    protected void drawNewLine(DrawInfo drawInfo) {
        drawInfo.valueOffset = 0;
        drawInfo.offsetY = drawInfo.offsetY + drawInfo.g2d.getFontMetrics().getHeight();
    }

    /**
     * Determine the height if the provided value is drawn on the provided graphics object.
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to use.
     * @param font     The font to use.
     * @return The height.
     */
    protected int determineHeight(DrawInfo drawInfo, String value, Font font) {
        if (value == null || value.isEmpty())
            return 0;

        AttributedString styledText = new AttributedString(value.replace("\n", " "));
        styledText.addAttribute(TextAttribute.FONT, font);

        Graphics2D g2d = drawInfo.g2d;
        LineBreakMeasurer measurer = new LineBreakMeasurer(styledText.getIterator(), g2d.getFontRenderContext());

        int height = 0;
        float offset = drawInfo.valueOffset;
        while (measurer.getPosition() < value.length()) {
            FontMetrics fm = g2d.getFontMetrics(font);
            TextLayout textLayout = measurer.nextLayout(drawInfo.width - offset);
            height += fm.getHeight();
            offset = 0;
        }
        return height;
    }

    /**
     * Get a localized message.
     *
     * @param code The code of the message.
     * @param def  The default to return.
     * @return The message if found, or the default.
     */
    protected String getMessage(String code, String def) {
        return msgSource.getMessage(code, null, def, Locale.ENGLISH);
    }


    /**
     * Draw a key on the provided graphics object.
     *
     * @param drawValueInfo Draw value information.
     */
    private void drawKey(DrawValueInfo drawValueInfo) {
        // Draw key
        if (drawValueInfo.key != null) {
            DrawInfo drawInfo = drawValueInfo.drawInfo;
            Graphics2D g2d = drawInfo.g2d;
            g2d.setFont(drawValueInfo.boldKey
                    ? boldFont.deriveFont((float) drawValueInfo.font.getSize())
                    : normalFont.deriveFont((float) drawValueInfo.font.getSize()));
            g2d.drawString(drawValueInfo.key + ":",
                    drawInfo.offsetX + drawInfo.valueOffset, drawInfo.offsetY);

            float offset = drawValueInfo.tab
                    ? KEY_TAB_OFFSET
                    : g2d.getFontMetrics().stringWidth(drawValueInfo.key + ": ");
            drawInfo.valueOffset = drawInfo.valueOffset + offset;
        }
    }

    /**
     * Draw a value on the provided graphics object.
     *
     * @param drawValueInfo Draw value information.
     */
    private void drawValue(DrawValueInfo drawValueInfo) {
        // Do not print key-value pairs with missing value.
        if (drawValueInfo.value == null || drawValueInfo.value.isEmpty()) {
            return;
        }
        DrawInfo drawInfo = drawValueInfo.drawInfo;
        Graphics2D g2d = drawInfo.g2d;
        int x = drawInfo.offsetX;

        // Draw value (word-wrap enabled)
        FontMetrics fm = g2d.getFontMetrics();
        FontRenderContext frc = g2d.getFontRenderContext();

        AttributedString styledText = new AttributedString(drawValueInfo.value.replace("\n", " "));
        styledText.addAttribute(TextAttribute.FONT, drawValueInfo.font);
        styledText.addAttribute(TextAttribute.UNDERLINE, drawValueInfo.underline ? 1 : -1);

        AttributedCharacterIterator styledTextIterator = styledText.getIterator();
        LineBreakMeasurer measurer = new LineBreakMeasurer(styledTextIterator, frc);

        boolean firstLine = true;
        while (measurer.getPosition() < drawValueInfo.value.length()) {
            if (!firstLine)
                drawNewLine(drawInfo);

            float width = drawInfo.width - drawInfo.valueOffset;
            TextLayout textLayout = measurer.nextLayout((width > 0) ? width : 0);
            textLayout.draw(g2d, x + drawInfo.valueOffset, drawInfo.offsetY);

            firstLine = false;
            drawInfo.valueOffset = drawInfo.valueOffset + textLayout.getAdvance() + fm.stringWidth(" ");
        }
    }
}
