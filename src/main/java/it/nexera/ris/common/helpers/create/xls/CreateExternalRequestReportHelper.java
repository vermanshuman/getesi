package it.nexera.ris.common.helpers.create.xls;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Request;

public class CreateExternalRequestReportHelper extends CreateExcelReportHelper {

	private CreationHelper createHelper;
	private Date currentDate;
	
	Row row;
	CellStyle mainCellStyle;
	CellStyle headerRowCellStyle;
	List<CellRangeAddress> mergeRows = new ArrayList<CellRangeAddress>();
	
    private void initStandardReport(String name) {
        createSheet(name);
        PrintSetup ps = getSheet().getPrintSetup();

        ps.setFitWidth ( (short)1 );
        ps.setFitHeight ( (short)0 );
        ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
        getSheet().setFitToPage ( true );
        getSheet().setAutobreaks ( true );

        ps.setFooterMargin ( 0.25 );
        
        getSheet().setMargin(HSSFSheet.TopMargin, 0.10);
        getSheet().setMargin(HSSFSheet.BottomMargin, 0.10);
        getSheet().setMargin(HSSFSheet.LeftMargin, 0.10);
        getSheet().setMargin(HSSFSheet.RightMargin, 0.10);

    }

    public void createReport(Date currentDate, List<Request> distictRequests, OutputStream os)
    	throws Exception {
    	this.currentDate = currentDate;
    	initStandardReport("Richieste esterne "+DateTimeHelper.toStringWithMinutes(currentDate));
        createHelper = getWorkbook().getCreationHelper();

        Font mainFont = getWorkbook().createFont();
        mainFont.setFontName("Verdana");
        mainFont.setFontHeightInPoints((short) 10);

        Font requestTypeFont = getWorkbook().createFont();
        requestTypeFont.setBold(true);
        requestTypeFont.setFontName("Verdana");
        requestTypeFont.setFontHeightInPoints((short) 12);

        headerRowCellStyle = getWorkbook().createCellStyle();
        headerRowCellStyle.setFont(requestTypeFont);
        headerRowCellStyle.setBorderBottom(BorderStyle.THIN);
        headerRowCellStyle.setBorderTop(BorderStyle.THIN);
        headerRowCellStyle.setBorderRight(BorderStyle.THIN);
        headerRowCellStyle.setBorderLeft(BorderStyle.THIN);
        headerRowCellStyle.setFillForegroundColor(IndexedColors.TAN.getIndex());
        headerRowCellStyle.setFillPattern(FillPatternType.forInt(1));
        headerRowCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);



        mainCellStyle = getWorkbook().createCellStyle();
        mainCellStyle.setFont(mainFont);

        row = createRowAfterDefinedEmptyRows(2);
        addHeader();

        row = createRowAfterDefinedEmptyRows(2);
    	Cell cellHeader = row.createCell(0);
        cellHeader.setCellValue(ResourcesHelper.getString("requestEditSubjectListName").toUpperCase());
        cellHeader.setCellStyle(headerRowCellStyle);

        cellHeader = row.createCell(1);
        cellHeader.setCellValue(ResourcesHelper.getString("requestEditSubjectListFiscalCode").toUpperCase());
        cellHeader.setCellStyle(headerRowCellStyle);

        cellHeader = row.createCell(2);
        cellHeader.setCellStyle(headerRowCellStyle);
        cellHeader.setCellValue(ResourcesHelper.getString("requestListService").toUpperCase());
        
        row = createRow();
        for (Request request : distictRequests) {
            if(!ValidationHelper.isNullOrEmpty(request.getSubject())) {
                Cell nominativo = row.createCell(0);
                nominativo.setCellStyle(mainCellStyle);
                nominativo.setCellValue(request.getSubject().getFullName());
                
                Cell fiscalCode = row.createCell(1);
                fiscalCode.setCellStyle(mainCellStyle);
                fiscalCode.setCellValue(request.getSubject().getFiscalCodeVATNamber());
                
                Cell service = row.createCell(2);
                service.setCellStyle(mainCellStyle);
                
                if (!ValidationHelper.isNullOrEmpty(request.getService())) {
                    service.setCellValue(request.getServiceName());
                } else {
                    
                    service.setCellValue(request.getMultipleServiceNames());
                }
            }
            row = createRow();
        }
        getSheet().setColumnWidth(0, 40*256);
        getSheet().setColumnWidth(1, 30*256);
        getSheet().setColumnWidth(2, 40*256);

        getWorkbook().write(os);
    }
    
    public void addHeader() {
        Cell cell = row.createCell(0);
        cell.setCellValue(ResourcesHelper.getString("mailManagerEmailReceiveDate").toUpperCase()+":");
        CellStyle dateCellStyle = getWorkbook().createCellStyle();
        cell = row.createCell(1);
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
        cell.setCellStyle(dateCellStyle);
        cell.setCellValue(DateTimeHelper.toString(currentDate));
    }
}