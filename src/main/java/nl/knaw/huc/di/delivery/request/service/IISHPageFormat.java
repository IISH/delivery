package nl.knaw.huc.di.delivery.request.service;

import java.awt.print.PageFormat;
import java.awt.print.Paper;

/**
 * The page format used for printing by the IISH.
 */
public class IISHPageFormat extends PageFormat {
    public IISHPageFormat() {
        Paper p = new Paper();

        // A4 width = 210mm = 8.26771654 inches = 595.28 size (72 times inches).
        // Width and height are same due to paper being square.
        p.setSize(595, 595);

        // 10 margin on top for barcode to correctly appear.
        // smaller area in width to make sure the characters don't fall off due to left margin of printer.
        p.setImageableArea(10, 0, 595, 585);
        setPaper(p);
        setOrientation(LANDSCAPE);
    }
}
