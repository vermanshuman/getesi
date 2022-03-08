package it.nexera.ris.common.helpers.create.xls;

import java.nio.charset.Charset;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;

import it.nexera.ris.common.helpers.create.pdf.CreateReportHelper;

public abstract class CreateExcelReportHelper extends CreateReportHelper {

    private static final int MIN_COLUMN_WIDTH = 6000;

    private String[] columns;

    private HSSFWorkbook workbook;

    private Sheet sheet;

    protected CreateExcelReportHelper() {
        workbook = new HSSFWorkbook();
    }

    protected void autoSizeColumnsAndSetSizeToAnotherByDefault(int countOfAutoSizedColumns) {
        for (int i = 0; i < columns.length; i++) {
            if (i < countOfAutoSizedColumns) {
                sheet.autoSizeColumn(i);
            } else if (i == columns.length - 1) {
                sheet.setColumnWidth(i, 10000);
            } else {
                sheet.setColumnWidth(i, MIN_COLUMN_WIDTH);
            }
        }
    }

    protected void optimizeColumnSizeIfItLessMinimalSize(int column) {
        sheet.autoSizeColumn(column);
        if (sheet.getColumnWidth(column) < MIN_COLUMN_WIDTH) {
            sheet.setColumnWidth(column, MIN_COLUMN_WIDTH);
        }
    }

    protected void addSeparator(Row row, CellStyle cellStyle) {
        for (int i = 0; i < columns.length; ++i) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(cellStyle);
        }
    }
    
    protected void addShiftedSeparator(Row row, CellStyle cellStyle) {
        for (int i = 0; i < columns.length; ++i) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(cellStyle);
        }
    }


    protected Row createRow() {
        return getSheet().createRow(getSheet().getLastRowNum() + 1);
    }

    protected Row createRow(int rowIdx) {
        return getSheet().createRow(rowIdx);
    }

    protected Row createRowAfterDefinedEmptyRows(int countOfEmptyRows) {
        return getSheet().createRow(getSheet().getLastRowNum() + countOfEmptyRows);
    }

    protected void setBordersForAllCellInRegion(CellRangeAddress region) {
        for (int rowIdx = region.getFirstRow(); rowIdx <= region.getLastRow(); ++rowIdx) {
            Row row = getSheet().getRow(rowIdx);
            if (row == null) {
                row = getSheet().createRow(rowIdx);
            }
            for (int colIdx = region.getFirstColumn(); colIdx <= region.getLastColumn(); ++colIdx) {
                Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                HSSFCellStyle cellStyle = getWorkbook().createCellStyle();
                cellStyle.cloneStyleFrom(((HSSFCell) cell).getCellStyle());
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cell.setCellStyle(cellStyle);
            }
        }
    }

    protected HSSFCellStyle getEuroStyle() {
        HSSFCellStyle euroStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        euroStyle.setDataFormat(format.getFormat(
                new String("_([$€-2]\\ * # ##0.00_);_([$€-2]\\ * \\(# ##0.00\\);_([$€-2]\\ * \"0,00\"??_);_(@_)".getBytes(),
                        Charset.defaultCharset())));
        return euroStyle;
    }
    
    protected HSSFCellStyle getCurrencyStyle() {
        HSSFCellStyle euroStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        euroStyle.setDataFormat(format.getFormat(
                new String("_(* # ##0.00_);_(* \\(# ##0.00\\);_(* \"0,00\"??_);_(@_)".getBytes(),
                        Charset.defaultCharset())));
        return euroStyle;
    }

    protected void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow) {
        sheet.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow);
    }

    protected void createSheet(String sheetName) {
        this.sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
        //this.sheet = workbook.createSheet(sheetName);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setLandscape(true);
        ps.setFitWidth ( (short)1 );
        ps.setFitHeight ( (short)0 );
        ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
        sheet.setFitToPage ( true );
        sheet.setAutobreaks ( true );

        ps.setFooterMargin ( 0.25 );
        sheet.setMargin(HSSFSheet.TopMargin, 0.10);
        sheet.setMargin(HSSFSheet.BottomMargin, 0.10);
        sheet.setMargin(HSSFSheet.LeftMargin, 0.10);
        sheet.setMargin(HSSFSheet.RightMargin, 0.10);
    }

    protected Sheet getSheet() {
        return this.sheet;
    }

    protected HSSFWorkbook getWorkbook() {
        return workbook;
    }

    protected String[] getColumns() {
        return columns;
    }

    protected void setColumns(String[] columns) {
        this.columns = columns;
    }
}
