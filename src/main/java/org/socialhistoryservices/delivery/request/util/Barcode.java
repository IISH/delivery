package org.socialhistoryservices.delivery.request.util;

import org.krysalis.barcode4j.ClassicBarcodeLogicHandler;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.Code128LogicImpl;
import org.krysalis.barcode4j.output.Canvas;
import org.krysalis.barcode4j.output.CanvasProvider;

/**
 * Override to use an overridden CanvasLogicHandler.
 */
public class Barcode extends Code128Bean {
    /**
     * Generates a barcode using the given Canvas to render the barcode to its output format.
     * Override to use an overridden CanvasLogicHandler.
     *
     * @param canvas CanvasProvider that the barcode is to be rendered on.
     * @param msg    message to encode
     */
    @Override
    public void generateBarcode(CanvasProvider canvas, String msg) {
        ClassicBarcodeLogicHandler handler = new CanvasLogicHandler(this, new Canvas(canvas));
        Code128LogicImpl impl = new Code128LogicImpl(getCodeset());
        impl.generateBarcodeLogic(handler, msg);
    }
}
