package org.socialhistoryservices.delivery.request.util;

import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.DefaultCanvasLogicHandler;
import org.krysalis.barcode4j.output.Canvas;

/**
 * Override of the DefaultCanvasLogicHandler to fix an issue
 * with the vertical text position when applying a vertical quiet zone.
 */
public class CanvasLogicHandler extends DefaultCanvasLogicHandler {
    /**
     * Main constructor.
     *
     * @param bcBean The barcode implementation class.
     * @param canvas The canvas to paint to.
     */
    public CanvasLogicHandler(AbstractBarcodeBean bcBean, Canvas canvas) {
        super(bcBean, canvas);
    }

    /**
     * Returns the vertical text baseline position.
     * Override to fix vertical text position when applying a vertical quiet zone.
     *
     * @return the vertical text baseline position
     */
    @Override
    protected double getTextBaselinePosition() {
        double ty = bcBean.getVerticalQuietZone();
        if (bcBean.getMsgPosition() == HumanReadablePlacement.HRP_TOP) {
            ty += bcBean.getHumanReadableHeight();
            if (/*bcBean.hasFontDescender()*/ false) {
                ty -= bcBean.getHumanReadableHeight() / 13 * 3;
            }
            return ty;
        }
        else if (bcBean.getMsgPosition() == HumanReadablePlacement.HRP_BOTTOM) {
            ty += bcBean.getHeight();
            if (/*bcBean.hasFontDescender()*/ false) {
                ty -= bcBean.getHumanReadableHeight() / 13 * 3;
            }
            return ty;
        }
        else {
            throw new IllegalStateException("not applicable");
        }
    }
}
