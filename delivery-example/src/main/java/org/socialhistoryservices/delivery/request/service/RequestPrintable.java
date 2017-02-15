package org.socialhistoryservices.delivery.request.service;

import org.krysalis.barcode4j.BarcodeDimension;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.java2d.Java2DCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.record.entity.Record;
import org.socialhistoryservices.delivery.request.entity.HoldingRequest;
import org.springframework.context.MessageSource;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;

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
    protected Properties properties;
    protected final HoldingRequest holdingRequest;

    private Locale l;
    private MessageSource msgSource;

    protected class DrawInfo {
        private int offsetX;
        private int offsetY;
        private int width;
        private int height;
        private float valueOffset;
        private Graphics2D g2d;

        public DrawInfo(Graphics2D g2d) {
            this.g2d = g2d;
        }

        public Graphics2D getG2d() {
            return g2d;
        }

        public int getOffsetX() {
            return offsetX;
        }

        public void setOffsetX(int offsetX) {
            this.offsetX = offsetX;
        }

        public int getOffsetY() {
            return offsetY;
        }

        public void setOffsetY(int offsetY) {
            this.offsetY = offsetY;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int h) {
            this.height = h;
        }

        public float getValueOffset() {
            return valueOffset;
        }

        public void setValueOffset(float valueOffset) {
            this.valueOffset = valueOffset;
        }
    }

    /**
     * Construct the printable.
     *
     * @param hr      The holding request to construct from.
     * @param mSource The message source to fetch localized messages.
     * @param format  The date format to use.
     */
    public RequestPrintable(HoldingRequest hr, MessageSource mSource, DateFormat format, Properties prop) {
        properties = prop;
        holdingRequest = hr;

        l = new Locale("en");
        msgSource = mSource;
        df = format;

        normalFont = new Font("Arial", Font.PLAIN, 10);
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
     * @throws java.awt.print.PrinterException Thrown when something went wrong.
     */
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        int pageWidth = (int) pf.getImageableWidth();
        int pageHeight = (int) pf.getImageableHeight();

        // Set the imageable area to correspond with the g2d draw area.
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        int halfWidth = pageWidth / 2;

        DrawInfo drawInfo = new DrawInfo(g2d);
        drawInfo.setHeight(pageHeight);
        int rightMargin = 20;

        // Draw all info to the g2d.
        for (int i = 1; i <= 2; i++) {
            drawInfo.setWidth(halfWidth - rightMargin);
            drawInfo.setOffsetX(halfWidth * (i - 1) + 10);
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
     * Draw a barcode on the provided graphics.
     *
     * @param drawInfo Draw offsets.
     * @param number   The barcode number to create a barcode from.
     */
    protected void drawBarcode(DrawInfo drawInfo, int number) {
        Code128Bean barcode = new Code128Bean();
        barcode.setModuleWidth(UnitConv.in2mm(5.0f / DPI));
        barcode.setBarHeight(30);
        barcode.setFontSize(12);

        String msg = String.format("%09d", number);
        BarcodeDimension dim = barcode.calcDimensions(msg);

        // Align to the right of the page
        barcode.doQuietZone(true);
        barcode.setQuietZone(drawInfo.getWidth() + drawInfo.getOffsetX() - dim.getWidth() - 10);
        barcode.setVerticalQuietZone(0);

        // Generate the barcode
        Java2DCanvasProvider canvas = new Java2DCanvasProvider(drawInfo.getG2d(), 0);
        barcode.generateBarcode(canvas, msg);
        drawInfo.setOffsetY((int) dim.getHeight() + 30);
    }

    /**
     * Draws the name of the person making the request.
     *
     * @param drawInfo Draw offsets.
     */
    protected void drawName(DrawInfo drawInfo) {
        String nameLabel = getMessage("request.name", "Name");
        drawKeyValueNewLine(drawInfo, nameLabel, holdingRequest.getRequest().getName(), italicFont, true);
    }

    /**
     * Draws the creation date of the request.
     *
     * @param drawInfo Draw offsets.
     */
    protected void drawCreationDate(DrawInfo drawInfo) {
        String dateLabel = getMessage("request.creationDate", "Created at");
        String dateTimeFormat = properties.getProperty("prop_dateFormat") + " " +
            properties.getProperty("prop_timeFormat", "HH:mm:ss");

        SimpleDateFormat spdf = new SimpleDateFormat(dateTimeFormat);
        drawKeyValueNewLine(drawInfo, dateLabel, spdf.format(holdingRequest.getRequest().getCreationDate()));
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

        drawFloor(drawInfo, h.getFloor());
        drawDirection(drawInfo, h.getDirection());
        drawCabinet(drawInfo, h.getCabinet());
        drawShelf(drawInfo, h.getShelf());
    }

    /**
     * Draw the type of the location.
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    protected void drawType(DrawInfo drawInfo, String value) {
        String typeLabel = getMessage("holding.signature", "Signature");
        drawKeyValueNewLine(drawInfo, typeLabel, value, italicFont, true);
    }

    /**
     * Draw the return notice.
     *
     * @param drawInfo Draw offsets.
     */
    protected void drawReturnNotice(DrawInfo drawInfo) {
        String returnKeep = getMessage("print.return", "Return or Keep?");
        String returnForm = getMessage("print.returnForm", "Please return form along with item.");

        Graphics2D g2d = drawInfo.getG2d();
        g2d.setFont(boldFont);

        int x = drawInfo.getOffsetX();
        int y = drawInfo.getOffsetY();
        g2d.drawString(returnKeep, x, y);
        y += 25;
        g2d.drawString(returnForm, x, y);
        y += 25;
        drawInfo.setOffsetY(y);
    }

    /**
     * Draw the comment of the holding reservation (used to specify
     * years/numbers for serials).
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawComment(DrawInfo drawInfo, String value) {
        String commentLabel = getMessage("holdingReservations.comment", "Comment");
        drawKeyValueNewLine(drawInfo, commentLabel, value);
    }

    /**
     * Draw the shelf of the location.
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawShelf(DrawInfo drawInfo, String value) {
        if (value == null) {
            return;
        }
        String shelfLabel = getMessage("holding.shelf", "Shelf");
        drawKeyValueNewLine(drawInfo, shelfLabel, value);
    }

    /**
     * Draw the direction of the location.
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawDirection(DrawInfo drawInfo, String value) {
        String directionLabel = getMessage("holding.direction", "Direction");
        drawKeyValueNewLine(drawInfo, directionLabel, value);
    }

    /**
     * Draw the cabinet of the location.
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawCabinet(DrawInfo drawInfo, String value) {
        if (value == null) {
            return;
        }
        String cabinetLabel = getMessage("holding.cabinet", "Cabinet");
        drawKeyValueNewLine(drawInfo, cabinetLabel, value);
    }

    /**
     * Draw the floor of the location.
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawFloor(DrawInfo drawInfo, Integer value) {
        if (value == null) {
            return;
        }
        String floorLabel = getMessage("holding.floor", "Floor");
        drawKeyValueNewLine(drawInfo, floorLabel, String.valueOf(value));
    }

    /**
     * Draw the pid of the record.
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawPid(DrawInfo drawInfo, String value) {
        drawKeyValueNewLine(drawInfo, "PID", value);
    }

    /**
     * Draw the material type.
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawMaterialType(DrawInfo drawInfo, ExternalRecordInfo.MaterialType value) {
        String typeLabel = getMessage("record.externalInfo.materialType", "Material");
        String val = getMessage("record.externalInfo.materialType." + value, "");
        drawKeyValueNewLine(drawInfo, typeLabel, val);
    }

    /**
     * Draw the title of the record.
     *
     * @param drawInfo Draw offsets.
     */
    private void drawTitle(DrawInfo drawInfo, String value) {
        String titleLabel = getMessage("record.title", "Title");
        drawKeyValueNewLine(drawInfo, titleLabel, value);
    }

    /**
     * Draw the author of the record (if applicable).
     *
     * @param drawInfo Draw offsets.
     * @param value    The value to draw.
     */
    private void drawAuthor(DrawInfo drawInfo, String value) {
        String authorLabel = getMessage("record.externalInfo.author", "Author");
        drawKeyValueNewLine(drawInfo, authorLabel, value);
    }

    /**
     * Draw a key value pair (and a new line) on the provided graphics object.
     *
     * @param drawInfo Draw offsets.
     * @param key      The key to use.
     * @param value    The value to use.
     */
    protected void drawKeyValueNewLine(DrawInfo drawInfo, String key, String value) {
        drawKeyValue(drawInfo, key, value, true);
        drawNewLine(drawInfo);
    }

    /**
     * Draw a key value pair (and a new line) on the provided graphics object.
     *
     * @param drawInfo  Draw offsets.
     * @param key       The key to use.
     * @param value     The value to use.
     * @param font      The font to use.
     * @param underline Whether to underline the value
     */
    protected void drawKeyValueNewLine(DrawInfo drawInfo, String key, String value, Font font, boolean underline) {
        drawKeyValue(drawInfo, key, value, font, underline, true);
        drawNewLine(drawInfo);
    }

    /**
     * Draw a key value pair (and a new line) on the provided graphics object.
     *
     * @param drawInfo Draw offsets.
     * @param key      The key to use.
     * @param value    The value to use.
     */
    protected void drawKeyValue(DrawInfo drawInfo, String key, String value, boolean tab) {
        drawKeyValue(drawInfo, key, value, normalFont, false, tab);
    }

    /**
     * Draw a key value pair (and a new line) on the provided graphics object.
     *
     * @param drawInfo  Draw offsets.
     * @param key       The key to use.
     * @param value     The value to use.
     * @param font      The font to use.
     * @param underline Whether to underline the value
     * @param tab       Add a tab after the key
     */
    protected void drawKeyValue(DrawInfo drawInfo, String key, String value, Font font, boolean underline, boolean tab) {
        // Do not print key-value pairs with missing value.
        if (value == null || value.isEmpty()) {
            return;
        }

        // Draw key
        if (key != null) {
            Graphics2D g2d = drawInfo.getG2d();
            g2d.setFont(boldFont.deriveFont((float) font.getSize()));
            g2d.drawString(key + ":", drawInfo.getOffsetX() + drawInfo.getValueOffset(), drawInfo.getOffsetY());

            float offset = tab ? KEY_TAB_OFFSET : g2d.getFontMetrics().stringWidth(key + ": ");
            drawInfo.setValueOffset(drawInfo.getValueOffset() + offset);
        }

        drawValue(drawInfo, value, font, underline);
    }

    /**
     * Draw a value and a new line on the provided graphics object.
     *
     * @param drawInfo  Draw offsets.
     * @param value     The value to use.
     * @param font      The font to use.
     * @param underline Whether to underline the value
     */
    protected void drawValueNewLine(DrawInfo drawInfo, String value, Font font, boolean underline) {
        drawValue(drawInfo, value, font, underline);
        drawNewLine(drawInfo);
    }

    /**
     * Draw a value on the provided graphics object.
     *
     * @param drawInfo  Draw offsets.
     * @param value     The value to use.
     * @param font      The font to use.
     * @param underline Whether to underline the value
     */
    protected void drawValue(DrawInfo drawInfo, String value, Font font, boolean underline) {
        // Do not print key-value pairs with missing value.
        if (value == null || value.isEmpty()) {
            return;
        }
        Graphics2D g2d = drawInfo.getG2d();
        int x = drawInfo.getOffsetX();

        // Draw value (word-wrap enabled)
        FontMetrics fm = g2d.getFontMetrics();
        FontRenderContext frc = g2d.getFontRenderContext();

        AttributedString styledText = new AttributedString(value.replace("\n", " "));
        styledText.addAttribute(TextAttribute.FONT, font);
        styledText.addAttribute(TextAttribute.UNDERLINE, underline ? 1 : -1);

        AttributedCharacterIterator styledTextIterator = styledText.getIterator();
        LineBreakMeasurer measurer = new LineBreakMeasurer(styledTextIterator, frc);

        boolean firstLine = true;
        while (measurer.getPosition() < value.length()) {
            if (!firstLine)
                drawNewLine(drawInfo);

            float width = drawInfo.getWidth() - drawInfo.getValueOffset();
            TextLayout textLayout = measurer.nextLayout(width);
            textLayout.draw(g2d, x + drawInfo.getValueOffset(), drawInfo.getOffsetY());

            firstLine = false;
            drawInfo.setValueOffset(drawInfo.getValueOffset() + textLayout.getAdvance() + fm.stringWidth(" "));
        }
    }

    /**
     * Draw a new line on the provided graphics object.
     *
     * @param drawInfo Draw offsets.
     */
    protected void drawNewLine(DrawInfo drawInfo) {
        drawInfo.setValueOffset(0);
        drawInfo.setOffsetY(drawInfo.getOffsetY() + drawInfo.getG2d().getFontMetrics().getHeight());
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

        Graphics2D g2d = drawInfo.getG2d();
        LineBreakMeasurer measurer = new LineBreakMeasurer(styledText.getIterator(),  g2d.getFontRenderContext());

        int height = 0;
        float offset = drawInfo.getValueOffset();
        while (measurer.getPosition() < value.length()) {
            FontMetrics fm = g2d.getFontMetrics(font);
            TextLayout textLayout = measurer.nextLayout(drawInfo.getWidth() - offset);
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
        return msgSource.getMessage(code, null, def, l);
    }
}
