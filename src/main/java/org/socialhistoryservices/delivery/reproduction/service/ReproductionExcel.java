package org.socialhistoryservices.delivery.reproduction.service;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds an Excel export of payed reproductions.
 */
public class ReproductionExcel {
    private final List<Reproduction> reproductions;
    private final MessageSource messageSource;

    private final HSSFWorkbook workbook;
    private final HSSFSheet sheet;

    private CellStyle headerStyle;
    private CellStyle defaultStyle;
    private CellStyle moneyStyle;

    /**
     * Build a new Excel export of the given payed reproductions.
     *
     * @param reproductions The reproductions to export. The reproductions should have payed orders!
     * @param messageSource Used for building the header columns.
     */
    public ReproductionExcel(List<Reproduction> reproductions, MessageSource messageSource) {
        this.reproductions = reproductions;
        this.messageSource = messageSource;

        this.workbook = new HSSFWorkbook();
        this.sheet = workbook.createSheet("Reproductions");

        createStyles();

        build();
    }

    /**
     * Writes the Excel file to the given output stream.
     *
     * @param outputStream The output stream in question.
     * @throws IOException
     */
    public void writeToStream(OutputStream outputStream) throws IOException {
        workbook.write(outputStream);
        outputStream.flush();
    }

    /**
     * Creates the styles used troughout the workbook.
     */
    private void createStyles() {
        // Headers are bold and have a bottom border
        headerStyle = workbook.createCellStyle();
        HSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Just a default cell style
        defaultStyle = workbook.createCellStyle();
        defaultStyle.setFont(workbook.createFont());
        defaultStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Money values should have a money format
        moneyStyle = workbook.createCellStyle();
        moneyStyle.setFont(workbook.createFont());
        String moneyFormat = "€ #,##0.00";
        CreationHelper creationHelper = workbook.getCreationHelper();
        short dataFormatIdentifier = creationHelper.createDataFormat().getFormat(moneyFormat);
        moneyStyle.setDataFormat(dataFormatIdentifier);
        moneyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    /**
     * Build the Excel sheet.
     */
    private void build() {
        buildHeader();
        for (Reproduction reproduction : reproductions) {
            buildRows(reproduction);
        }
        autoSizeColumns();
    }

    /**
     * Builds the header of the Excel sheet.
     */
    private void buildHeader() {
        Row headerRow = sheet.createRow(0);
        sheet.createFreezePane(0, 1);

        Locale locale = LocaleContextHolder.getLocale();

        Cell cell = headerRow.createCell(0);
        cell.setCellValue(messageSource.getMessage("reproductionExport.reproductionId", new Object[0], locale));
        cell.setCellStyle(headerStyle);

        cell = headerRow.createCell(1);
        cell.setCellValue(messageSource.getMessage("reproductionExport.orderId", new Object[0], locale));
        cell.setCellStyle(headerStyle);

        cell = headerRow.createCell(2);
        cell.setCellValue(messageSource.getMessage("reproductionExport.totalPrice", new Object[0], locale));
        cell.setCellStyle(headerStyle);

        cell = headerRow.createCell(3);
        cell.setCellValue(messageSource.getMessage("reproductionExport.btwPercentage", new Object[0], locale));
        cell.setCellStyle(headerStyle);

        cell = headerRow.createCell(4);
        cell.setCellValue(messageSource.getMessage("reproductionExport.totalBtw", new Object[0], locale));
        cell.setCellStyle(headerStyle);
    }

    /**
     * Builds the rows for a single reproduction in the Excel sheet.
     *
     * @param reproduction The reproduction to use.
     */
    private void buildRows(Reproduction reproduction) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);

        Cell cell = row.createCell(0);
        cell.setCellValue(reproduction.getId());
        cell.setCellStyle(defaultStyle);

        cell = row.createCell(1);
        cell.setCellValue(reproduction.getOrderId() != null ? reproduction.getOrderId() : "");
        cell.setCellStyle(defaultStyle);

        cell = row.createCell(2);
        cell.setCellValue(reproduction.getTotalPrice().doubleValue());
        cell.setCellStyle(moneyStyle);

        boolean isFirst = true;
        Map<String, BigDecimal> totalBtw = reproduction.getTotalBTW();
        for (Map.Entry<String, BigDecimal> entry : totalBtw.entrySet()) {
            // Multiple BTW values, merge the first three cells with the row above
            if (!isFirst) {
                row = sheet.createRow(sheet.getLastRowNum() + 1);

                row.createCell(0);
                row.createCell(1);
                row.createCell(2);

                sheet.addMergedRegion(new CellRangeAddress(row.getRowNum() - 1, row.getRowNum(), 0, 0));
                sheet.addMergedRegion(new CellRangeAddress(row.getRowNum() - 1, row.getRowNum(), 1, 1));
                sheet.addMergedRegion(new CellRangeAddress(row.getRowNum() - 1, row.getRowNum(), 2, 2));
            }

            cell = row.createCell(3);
            cell.setCellValue(Integer.parseInt(entry.getKey()));
            cell.setCellStyle(defaultStyle);

            cell = row.createCell(4);
            cell.setCellValue(entry.getValue().doubleValue());
            cell.setCellStyle(moneyStyle);

            isFirst = false;
        }
    }

    /**
     * Automatically resize all columns, based on the length of the entered data.
     */
    private void autoSizeColumns() {
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
