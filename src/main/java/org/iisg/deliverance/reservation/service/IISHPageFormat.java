package org.iisg.deliverance.reservation.service;

import java.awt.print.PageFormat;
import java.awt.print.Paper;

/**
 * The page format used for printing by the IISH.
 */
public class IISHPageFormat extends PageFormat {

    public IISHPageFormat() {
        Paper p = new Paper();

        // 612 = 25.19 cm in 1/72 inches.
        p.setSize(612, 612);
        p.setImageableArea(10, 10, 592, 592);
        setPaper(p);
        setOrientation(PORTRAIT);

    }
}
