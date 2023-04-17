package org.socialhistoryservices.delivery.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Configuration to control which printers are in use.
 */
@Component
public class PrinterConfiguration {
    public enum PrinterState {
        BOTH, ARCHIVE, READING_ROOM
    }

    @Autowired
    private DeliveryProperties deliveryProperties;

    private PrinterState state = PrinterState.ARCHIVE;

    public PrinterState getState() {
        return state;
    }

    public void setState(PrinterState state) {
        this.state = state;
    }

    public String getPrinterNameArchive() {
        return (state == PrinterState.READING_ROOM)
                ? deliveryProperties.getPrinterReadingRoom()
                : deliveryProperties.getPrinterArchive();
    }

    public String getPrinterNameReadingRoom() {
        return (state == PrinterState.ARCHIVE)
                ? deliveryProperties.getPrinterArchive()
                : deliveryProperties.getPrinterReadingRoom();
    }
}
