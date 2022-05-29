package it.nexera.ris.common.helpers.create.xls;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import it.nexera.ris.persistence.beans.entities.domain.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.RegionUtil;
import org.bouncycastle.ocsp.Req;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import it.nexera.ris.common.enums.BillingTypeFields;
import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.common.enums.MortgageType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.CostCalculationHelper;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.SaveRequestDocumentsHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.web.beans.wrappers.logic.ExcelDataWrapper;

public class CreateExcelRequestsReportHelper extends CreateExcelReportHelper {

    private static String[] requestsColumns = {
            ResourcesHelper.getString("requestedDate"),
            ResourcesHelper.getString("nominative"),
            ResourcesHelper.getString("codFiscIva"),
            ResourcesHelper.getString("permissionRequest"),
            ResourcesHelper.getString("requestListCorservatoryName"),
            ResourcesHelper.getString("requestPrintFormalityPresentationDate"),
            ResourcesHelper.getString("excelForm"),
            ResourcesHelper.getString("mortgageRights"),
            ResourcesHelper.getString("landRegistryRights"),
            ResourcesHelper.getString("compensation"),
            ResourcesHelper.getString("formalityTotal"),
            ResourcesHelper.getString("excelNote"),
            ResourcesHelper.getString("excelCDR"),
            ResourcesHelper.getString("excelNDG"),
            ResourcesHelper.getString("excelPosition"),
            ResourcesHelper.getString("excelStamps"),
            ResourcesHelper.getString("excelPostalExpenses")};


    private static String[] requestsDefaultColumns = {
            ResourcesHelper.getString("requestedDate"),
            ResourcesHelper.getString("nominative"),
            ResourcesHelper.getString("codFiscIva"),
            ResourcesHelper.getString("permissionRequest"),
            ResourcesHelper.getString("requestListCorservatoryName"),
            ResourcesHelper.getString("requestPrintFormalityPresentationDate"),
            ResourcesHelper.getString("excelForm"),
            ResourcesHelper.getString("mortgageRights"),
            ResourcesHelper.getString("landRegistryRights"),
            ResourcesHelper.getString("compensation"),
            ResourcesHelper.getString("formalityTotal"),
            ResourcesHelper.getString("excelNote"),
            ResourcesHelper.getString("excelCDR"),
            ResourcesHelper.getString("excelNDG"),
            ResourcesHelper.getString("excelPosition")};

    private static String[] requestsEvasionColumns = {
            ResourcesHelper.getString("excelDate"),
            ResourcesHelper.getString("excelUser"),
            ResourcesHelper.getString("excelOffice"),
            ResourcesHelper.getString("excelName"),
            ResourcesHelper.getString("excelCode"),
            ResourcesHelper.getString("excelRequestType"),
            ResourcesHelper.getString("excelConservatoria"),
            ResourcesHelper.getString("excelForm"),
            ResourcesHelper.getString("excelMortgageExpenses"),
            ResourcesHelper.getString("excelCatastalExpenses"),
            ResourcesHelper.getString("excelCompensation"),
            ResourcesHelper.getString("excelTotal"),
            ResourcesHelper.getString("excelNote"),
            ResourcesHelper.getString("excelCDR"),
            ResourcesHelper.getString("excelNDG"),
            ResourcesHelper.getString("excelPosition"),
            ResourcesHelper.getString("excelStamps"),
            ResourcesHelper.getString("excelPostalExpenses")};

    private static String[] requestsEvasionDefaultColumns = {
            ResourcesHelper.getString("excelDate"),
            ResourcesHelper.getString("excelUser"),
            ResourcesHelper.getString("excelOffice"),
            ResourcesHelper.getString("excelName"),
            ResourcesHelper.getString("excelCode"),
            ResourcesHelper.getString("excelRequestType"),
            ResourcesHelper.getString("excelConservatoria"),
            ResourcesHelper.getString("excelForm"),
            ResourcesHelper.getString("excelMortgageExpenses"),
            ResourcesHelper.getString("excelCatastalExpenses"),
            ResourcesHelper.getString("excelCompensation"),
            ResourcesHelper.getString("excelTotal"),
            ResourcesHelper.getString("excelNote"),
            ResourcesHelper.getString("excelCDR"),
            ResourcesHelper.getString("excelNDG"),
            ResourcesHelper.getString("excelPosition")};


    private static final int COLUMNS_WITHOUT_COSTS = requestsColumns.length - 5;
    private static final int EVASION_COLUMNS_WITHOUT_COSTS = requestsEvasionColumns.length - 6;

    private boolean isItInvoiceReport;

    public CreateExcelRequestsReportHelper() {
    }

    public CreateExcelRequestsReportHelper(boolean isItInvoiceReport) {
        this.isItInvoiceReport = isItInvoiceReport;
    }

    private void initStandardReport() {
        setColumns(requestsColumns);
        createSheet("Costs");
    }

    private void initEvasionReport() {
        setColumns(requestsEvasionColumns);
        createSheet("excelEvasionSheet");
    }

    public byte[] convertMailDataToExcel(List<Request> requests, Document existingDocumentForThisRequests)
            throws IOException, PersistenceBeanException, IllegalAccessException, InstantiationException {
        Request firstRequest = requests.get(0);
        Client requestClient = firstRequest.getClient();
        initStandardReport();
        CreationHelper createHelper = getWorkbook().getCreationHelper();

        Row row = createRow();

        Font headerFont = getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setFontName("Verdana");
        headerFont.setFontHeightInPoints((short) 10);

        Font mainFont = getWorkbook().createFont();
        mainFont.setFontName("Verdana");
        mainFont.setFontHeightInPoints((short) 10);

        Font requestTypeFont = getWorkbook().createFont();
        requestTypeFont.setBold(true);
        requestTypeFont.setFontName("Verdana");
        requestTypeFont.setFontHeightInPoints((short) 12);

        CellStyle requestTypeCellStyle = getWorkbook().createCellStyle();
        requestTypeCellStyle.setFont(requestTypeFont);

        CellStyle headerCellStyle = getWorkbook().createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);

        CellStyle separatorCellStyle = getWorkbook().createCellStyle();
        separatorCellStyle.setFillForegroundColor(IndexedColors.TAN.getIndex());
        separatorCellStyle.setFillPattern(FillPatternType.forInt(1));
        separatorCellStyle.setFont(headerFont);

        CellStyle mainCellStyle = getWorkbook().createCellStyle();
        mainCellStyle.setFont(mainFont);

        CellStyle dateCellStyle = getWorkbook().createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

        addHeader(firstRequest, existingDocumentForThisRequests, mainCellStyle);

        Map<RequestType, List<Request>> sortedRequests = new HashMap<>();
        sortRequestsByType(requests, sortedRequests);
        List<CellRangeAddress> cellRangeAddresses = new ArrayList<CellRangeAddress>();

        for (Map.Entry<RequestType, List<Request>> entry : sortedRequests.entrySet()) {
            if(ValidationHelper.isNullOrEmpty(entry.getKey()))
                continue;

            row = createRowAfterDefinedEmptyRows(4);

            Cell cellHeader = row.createCell(1);
            cellHeader.setCellValue(entry.getKey().getName());
            cellHeader.setCellStyle(requestTypeCellStyle);

            row = createRowAfterDefinedEmptyRows(2);
            if (!ValidationHelper.isNullOrEmpty(requestClient)) {
                List<ClientInvoiceManageColumn> columns = DaoManager.load(ClientInvoiceManageColumn.class,
                        new CriteriaAlias[]{new CriteriaAlias("client", "client", JoinType.INNER_JOIN),
                                new CriteriaAlias("requestType", "requestType", JoinType.INNER_JOIN)},
                        new Criterion[]{
                                Restrictions.and(Restrictions.eq("client.id", requestClient.getId())
                                        ,Restrictions.eq("requestType.id",entry.getKey().getId()))
                        });
                if (!ValidationHelper.isNullOrEmpty(columns)) {
                    List<String> excelColumns = new ArrayList<String>();
                    for (ClientInvoiceManageColumn column : columns) {
                        if(!excelColumns.contains(getColumnNameByField(column.getField())))
                            excelColumns.add(getColumnNameByField(column.getField()));
                    }
                    setColumns(excelColumns.toArray(new String[0]));
                    setRequestsColumns(excelColumns.toArray(new String[0]));
                } else {
                    setColumns(requestsDefaultColumns);
                    setRequestsColumns(requestsDefaultColumns);
                }
            } else {
                setColumns(requestsDefaultColumns);
                setRequestsColumns(requestsDefaultColumns);
            }

            addSeparator(row, separatorCellStyle);
            int firstRowNum = row.getRowNum();

            addCosts(entry.getValue(), mainCellStyle, mainFont);

            addFooter(entry.getValue(), headerCellStyle, headerFont);
            cellRangeAddresses.add(new CellRangeAddress(
                    firstRowNum, getSheet().getLastRowNum(), 0, getColumns().length - 1));
        }

        autoSizeColumnsAndSetSizeToAnotherByDefault(COLUMNS_WITHOUT_COSTS);
        if (isItInvoiceReport()) {
            optimizeColumnSizeIfItLessMinimalSize(getIndex(ResourcesHelper.getString("excelNote"), requestsColumns) + 1);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getWorkbook().write(baos);
        byte[] xls = baos.toByteArray();

        baos.close();

        getWorkbook().close();

        String excelName = String.format("%s.%s", RandomStringUtils.randomAlphanumeric(8), "xls");
        Path path = Paths.get(System.getProperty("java.io.tmpdir") + "/" + excelName);
        Files.write(path, xls);

        try {
            FileInputStream file = new FileInputStream(path.toFile());

            HSSFWorkbook workbook = new HSSFWorkbook(file);

            HSSFSheet sheet = workbook.getSheetAt(0);

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


            Iterator<Row> rowIterator = sheet.iterator();
            while(rowIterator.hasNext()) {
                Row drow = rowIterator.next();
                for(CellRangeAddress cellRangeAddress : cellRangeAddresses) {
                    if(cellRangeAddress.getFirstRow() <= drow.getRowNum()
                            && drow.getRowNum() <= cellRangeAddress.getLastRow()) {
                        HSSFCellStyle cellStyle = workbook.createCellStyle();
                        Iterator<Cell> cellIterator = drow.cellIterator();
                        int index = 0;
                        while(cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();
                            try {
                                if(index == 0 && cell.getStringCellValue().equalsIgnoreCase(ResourcesHelper.getString("formalityTotal").toUpperCase())) {
                                    continue;
                                }
                            } catch (Exception e) {
                            }
                            cellStyle.cloneStyleFrom(((HSSFCell) cell).getCellStyle());
                            cellStyle.setBorderBottom(BorderStyle.THIN);
                            cellStyle.setBorderTop(BorderStyle.THIN);
                            cellStyle.setBorderRight(BorderStyle.THIN);
                            cellStyle.setBorderLeft(BorderStyle.THIN);
                            cell.setCellStyle(cellStyle);
                            index++;
                        }
                    }
                }
            }

            for(CellRangeAddress cellRangeAddress : cellRangeAddresses) {
                for (int rowIdx = cellRangeAddress.getFirstRow(); rowIdx <= cellRangeAddress.getLastRow(); ++rowIdx) {
                    Row drow = sheet.getRow(rowIdx);
                    if (drow == null) {
                        drow = sheet.createRow(rowIdx);
                    }
                    HSSFCellStyle cellStyle = workbook.createCellStyle();
                    for (int cn = cellRangeAddress.getFirstColumn(); cn <= cellRangeAddress.getLastColumn(); cn++) {
                        Cell cell = drow.getCell(cn);
                        if(cell == null) {
                            cell = drow.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            cellStyle.cloneStyleFrom(((HSSFCell) cell).getCellStyle());
                            cellStyle.setBorderBottom(BorderStyle.THIN);
                            cellStyle.setBorderTop(BorderStyle.THIN);
                            cellStyle.setBorderRight(BorderStyle.THIN);
                            cellStyle.setBorderLeft(BorderStyle.THIN);
                            cell.setCellStyle(cellStyle);
                        }
                    }
                }
            }
            baos = new ByteArrayOutputStream();
            workbook.write(baos);
            xls = baos.toByteArray();
            workbook.close();
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
        }

        return xls;
    }

    public byte[] convertMailUserDataToExcel(List<Request> requests,
                                             Document existingDocumentForThisRequests,
                                             ExcelDataWrapper excelDataWrapper)
            throws IOException, PersistenceBeanException, IllegalAccessException, InstantiationException {
        // if(!ValidationHelper.isNullOrEmpty(requests)) {
        Request firstRequest = requests.get(0);
        Client requestClient = firstRequest.getClient();
        initStandardReport();
        CreationHelper createHelper = getWorkbook().getCreationHelper();

        Row row = createRow();

        Font headerFont = getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setFontName("Calibri");
        headerFont.setFontHeightInPoints((short) 10);

        Font mainFont = getWorkbook().createFont();
        mainFont.setFontName("Calibri");
        mainFont.setFontHeightInPoints((short) 10);

        Font requestTypeFont = getWorkbook().createFont();
        requestTypeFont.setBold(true);
        requestTypeFont.setFontName("Calibri");
        requestTypeFont.setFontHeightInPoints((short) 12);

        CellStyle requestTypeCellStyle = getWorkbook().createCellStyle();
        requestTypeCellStyle.setFont(requestTypeFont);

        CellStyle headerCellStyle = getWorkbook().createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);

        CellStyle separatorCellStyle = getWorkbook().createCellStyle();
        separatorCellStyle.setFillForegroundColor(IndexedColors.TAN.getIndex());
        separatorCellStyle.setFillPattern(FillPatternType.forInt(1));
        separatorCellStyle.setFont(headerFont);

        CellStyle mainCellStyle = getWorkbook().createCellStyle();
        mainCellStyle.setFont(mainFont);

        CellStyle dateCellStyle = getWorkbook().createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
        List<CellRangeAddress> cellRangeAddresses = new ArrayList<CellRangeAddress>();
        addHeader(mainCellStyle,excelDataWrapper);

        Map<RequestType, List<Request>> sortedRequests = new HashMap<>();
        sortRequestsByType(requests, sortedRequests);
        for (Map.Entry<RequestType, List<Request>> entry : sortedRequests.entrySet()) {
            row = createRowAfterDefinedEmptyRows(4);

            Cell cellHeader = row.createCell(0);
            cellHeader.setCellValue(entry.getKey().getName());
            cellHeader.setCellStyle(requestTypeCellStyle);

            row = createRowAfterDefinedEmptyRows(2);
            if (!ValidationHelper.isNullOrEmpty(requestClient)) {
                List<ClientInvoiceManageColumn> columns = DaoManager.load(ClientInvoiceManageColumn.class,
                        new CriteriaAlias[]{new CriteriaAlias("client", "client", JoinType.INNER_JOIN),
                                new CriteriaAlias("requestType", "requestType", JoinType.INNER_JOIN)},
                        new Criterion[]{
                                Restrictions.and(Restrictions.eq("client.id", requestClient.getId())
                                        ,Restrictions.eq("requestType.id",entry.getKey().getId()))
                        },Order.asc("position"));
                if (!ValidationHelper.isNullOrEmpty(columns)) {
                    List<String> excelColumns = new ArrayList<>();
                    for (ClientInvoiceManageColumn column : columns) {
                        if(!excelColumns.contains(getColumnNameByField(column.getField())))
                            excelColumns.add(getColumnNameByField(column.getField()));
                    }
                    setColumns(excelColumns.toArray(new String[0]));
                    setRequestsColumns(excelColumns.toArray(new String[0]));
                } else {
                    setColumns(requestsDefaultColumns);
                    setRequestsColumns(requestsDefaultColumns);
                }
            } else {
                setColumns(requestsDefaultColumns);
                setRequestsColumns(requestsDefaultColumns);
            }

            addShiftedSeparator(row, separatorCellStyle);
            int firstRowNum = row.getRowNum();

            addCosts(entry.getValue(), mainCellStyle, mainFont);

            addFooter(entry.getValue(), headerCellStyle, headerFont);
            cellRangeAddresses.add(new CellRangeAddress(
                    firstRowNum, getSheet().getLastRowNum(), 0, getColumns().length-1));
            resizeRequestColumns();
        }

        int colIndex = getIndex(ResourcesHelper.getString("excelForm"), requestsColumns);
        getSheet().setColumnWidth(colIndex, 12*256);


        colIndex = getIndex(ResourcesHelper.getString("mortgageRights"), requestsColumns);
        getSheet().setColumnWidth(colIndex, 20*256);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getWorkbook().write(baos);
        byte[] xls = baos.toByteArray();

        baos.close();

        getWorkbook().close();

        String excelName = String.format("%s.%s", RandomStringUtils.randomAlphanumeric(8), "xls");
        Path path = Paths.get(System.getProperty("java.io.tmpdir") + "/" + excelName);
        Files.write(path, xls);

        try {
            FileInputStream file = new FileInputStream(path.toFile());

            HSSFWorkbook workbook = new HSSFWorkbook(file);

            HSSFSheet sheet = workbook.getSheetAt(0);

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


            for (CellRangeAddress cellRangeAddress : cellRangeAddresses) {
                for (int r = cellRangeAddress.getFirstRow()+1; r <= cellRangeAddress.getLastRow()+1; r++) {

                    for (int c = cellRangeAddress.getFirstColumn(); c <= cellRangeAddress.getLastColumn(); c++) {

                        String cr = CellReference.convertNumToColString(c) + r;
                        RegionUtil.setBorderBottom(BorderStyle.THIN,
                                CellRangeAddress.valueOf(cr + ":" + cr), sheet);
                        RegionUtil.setBorderTop(BorderStyle.THIN,
                                CellRangeAddress.valueOf(cr + ":" + cr), sheet);
                        RegionUtil.setBorderLeft(BorderStyle.THIN,
                                CellRangeAddress.valueOf(cr + ":" + cr), sheet);
                        RegionUtil.setBorderRight(BorderStyle.THIN,
                                CellRangeAddress.valueOf(cr + ":" + cr), sheet);
                    }
                }
            }
            baos = new ByteArrayOutputStream();
            workbook.write(baos);
            xls = baos.toByteArray();
            baos.close();
            workbook.close();
            file.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
        }

        return xls;
    }

    private String getColumnNameByField(BillingTypeFields field) {
        switch (field) {
            case EXCEL_DATE:
                return (ResourcesHelper.getString("requestPrintFormalityPresentationDate"));
            case EXCEL_CONSERVATORIA:
                return (ResourcesHelper.getString("requestListCorservatoryName"));
            case EXCEL_NAME:
                return (ResourcesHelper.getString("nominative"));
            case EXCEL_CODE:
                return (ResourcesHelper.getString("codFiscIva"));
            case EXCEL_REQUEST_TYPE:
                return (ResourcesHelper.getString("permissionRequest"));
            case EXCEL_FORMALITY:
                return (ResourcesHelper.getString("excelForm"));
            case EXCEL_MORTGAGE_EXPENSES:
                return (ResourcesHelper.getString("mortgageRights"));
            case EXCEL_CATASTAL_EXPENSES:
                return (ResourcesHelper.getString("landRegistryRights"));
            case EXCEL_COMPENSATION:
                return (ResourcesHelper.getString("compensation"));
            case EXCEL_TOTAL:
                return (ResourcesHelper.getString("formalityTotal"));
            case EXCEL_NOTE:
                return (ResourcesHelper.getString("excelNote"));
            case EXCEL_CDR:
                return (ResourcesHelper.getString("excelCDR"));
            case EXCEL_NDG:
                return (ResourcesHelper.getString("excelNDG"));
            case EXCEL_USER:
                return (ResourcesHelper.getString("excelUser"));
            case EXCEL_OFFICE:
                return (ResourcesHelper.getString("excelOffice"));
            case EXCEL_POSITION:
                return (ResourcesHelper.getString("excelPosition"));
            case EXCEL_STAMPS:
                return (ResourcesHelper.getString("excelStamps"));
            case EXCEL_POSTAL_EXPENSES:
                return (ResourcesHelper.getString("excelPostalExpenses"));
            default:
                return null;
        }
    }

    public byte[] convertFilteredRequestsToExcel(List<Request> requests)
            throws IOException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        setRequestsEvasionColumns(requestsEvasionDefaultColumns);
        initEvasionReport();

        HSSFFont headerFont = getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);

        HSSFCellStyle headerCellStyle = getWorkbook().createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.TAN.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.forInt(1));
        headerCellStyle.setFont(headerFont);

        HSSFCellStyle dateCellStyle = getWorkbook().createCellStyle();
        CreationHelper createHelper = getWorkbook().getCreationHelper();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

        HSSFCellStyle euroStyle = getEuroStyle();

        Row row = createRow();
        addSeparator(row, headerCellStyle);
        addCosts(requests, dateCellStyle, euroStyle);

        autoSizeColumnsAndSetSizeToAnotherByDefault(EVASION_COLUMNS_WITHOUT_COSTS);

        optimizeColumnSizeIfItLessMinimalSize(getIndex(ResourcesHelper.getString("excelNote"), requestsColumns) + 1);
        optimizeColumnSizeIfItLessMinimalSize(getColumns().length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getWorkbook().write(baos);
        byte[] xls = baos.toByteArray();
        baos.close();

        getWorkbook().close();
        return xls;
    }

    private void addCosts(List<Request> requests, CellStyle dateCellStyle, CellStyle euroStyle) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        for (Request request : requests) {
            List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", request.getId())});
            Double result = 0d;
            for (ExtraCost cost : extraCost) {

                if(ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                    result = cost.getPrice();
                    try {
                        Request newRequest = request.reportCopy();
                        newRequest.setTempId(UUID.randomUUID().toString());
                        newRequest.setEstateFormalityList(request.getEstateFormalityList());
                        newRequest.setEvasionDate(request.getEvasionDate());
                        newRequest.setExcelUserName(request.getRequestExcelUserName());
                        addCost(newRequest, null, dateCellStyle, euroStyle,-2, result);
                    } catch (CloneNotSupportedException e) {
                        LogHelper.log(log, e);
                    }
                }
            }

            if (!ValidationHelper.isNullOrEmpty(request.getService())) {
                addCost(request, null, dateCellStyle, euroStyle,-1,-1);
            }else if (!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                int index =0;
                for (Service service : request.getMultipleServices()) {
                    addCost(request, service, dateCellStyle, euroStyle,index++,-1);
                }
            }
        }
    }

    private void addCost(Request request, Service service,CellStyle dateCellStyle,  CellStyle euroStyle,
                         int index,double nationalCost) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Row row = null;
        row = createRowAfterDefinedEmptyRows(1);
        Cell dateCell = row.createCell(0);
        dateCell.setCellValue(DateTimeHelper.toString(request.getEvasionDate()));
        dateCell.setCellStyle(dateCellStyle);

        if(index == -2) {
            row.createCell(1).setCellValue(request.getExcelUserName());
        }else
            row.createCell(1).setCellValue(request.getRequestExcelUserName());

        Office office = DaoManager.get(Office.class, request.getUserOfficeId());
        if (!ValidationHelper.isNullOrEmpty(office)) {
            row.createCell(2).setCellValue(office.getCode() + " " + office.getDescription());
        } else {
            row.createCell(2).setCellValue("");
        }
        row.createCell(3)
                .setCellValue(request.getSubject() != null ? request.getSubject().getFullName().toUpperCase() : "");

        row.createCell(4).setCellValue(
                request.getSubject() != null
                        ? (request.getSubject().getTypeIsPhysicalPerson() ? request.getSubject().getFiscalCode()
                        : request.getSubject().getNumberVAT())
                        : "");

        if(service == null) {
            row.createCell(5).setCellValue(request.getServiceName());
        }else {
            row.createCell(5).setCellValue(service.toString());
        }

        row.createCell(6).setCellValue(request.getAggregationLandChargesRegistryName());
        if(index != -2) {
            row.createCell(6).setCellValue(request.getAggregationLandChargesRegistryName());
        }else {
            List<AggregationLandChargesRegistry> aggregationLandChargesRegistries =
                    DaoManager.load(AggregationLandChargesRegistry.class, new Criterion[]{
                            Restrictions.eq("national", Boolean.TRUE)
                    });
            if(!ValidationHelper.isNullOrEmpty(aggregationLandChargesRegistries)) {
                row.createCell(6).setCellValue(aggregationLandChargesRegistries.get(0).toString());
            }
        }

        row.createCell(7).setCellValue(
                !ValidationHelper.isNullOrEmpty(request.getNumberActUpdate()) ? request.getNumberActUpdate()
                        : getNumActs(request.getId()).longValue());

        if(index != -2) {
            row.createCell(8, CellType.NUMERIC).setCellValue(getMortgageCost(request));
        }

        row.createCell(9, CellType.NUMERIC).setCellValue(getCatastalCost(request));
        row.createCell(10, CellType.NUMERIC)
                .setCellValue(ValidationHelper.isNullOrEmpty(request.getCostPay()) ? 0d : request.getCostPay());
        if(index != -2) {
            Double result = 0d;
            if(!ValidationHelper.isNullOrEmpty(service)) {
                if(index == 0) {
                    List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                            Restrictions.eq("requestId", request.getId())});
                    for (ExtraCost cost : extraCost) {
                        result += cost.getPrice();
                    }
                }
//                if (!ValidationHelper.isNullOrEmpty(request.getCostCadastral())) {
//                    result += request.getCostCadastral();
//                }

                Boolean billingClient = isBillingClient(request);
                result += getCostCadastral(request);
                boolean restrictionForPriceList = restrictionForPriceList(request);
                result += getCostEstate(request, service, billingClient, restrictionForPriceList);
                result += getCostExtra(request, service, billingClient, restrictionForPriceList);
                result += getCostEstateFormality(request, service, billingClient, restrictionForPriceList);
                result += getCostPay(request, service, billingClient, restrictionForPriceList);
                result += getCostPlus(request, service, billingClient, restrictionForPriceList);
                result = (double) Math.round((result)* 100000d) / 100000d;
            }else {
                result = ValidationHelper.isNullOrEmpty(request.getTotalCost()) ? 0d
                        : Double.parseDouble(request.getTotalCost().replaceAll(",", "."));
            }
            row.createCell(11, CellType.NUMERIC).setCellValue(result);
        }else {
            row.createCell(11, CellType.NUMERIC)
                    .setCellValue(nationalCost);
            row.createCell(8, CellType.NUMERIC).setCellValue(nationalCost);
        }

        List<Document> requestDocuments = DaoManager.load(Document.class,
                new CriteriaAlias[]{new CriteriaAlias("request", "request", JoinType.INNER_JOIN)},
                new Criterion[]{Restrictions.and(Restrictions.eq("request.id", request.getId()), Restrictions.eq("typeId", 2L))});
        String note = "";
        if(index != -2) {
            if(!ValidationHelper.isNullOrEmpty(request.getCostNote())) {
                note = request.getCostNote();
            }else {
                boolean isAdded = Boolean.FALSE;
                if (!ValidationHelper.isNullOrEmpty(requestDocuments)) {
                    if(request.getService() !=null
                            && request.getService().getUnauthorizedQuote()!=null && request.getService().getUnauthorizedQuote()){
                        note = "Preventivo non autorizzato";
                        isAdded = Boolean.TRUE;
                    }
                }
                if(!isAdded && request.getAuthorizedQuote()!=null &&  request.getAuthorizedQuote()){
                    note = "Preventivo autorizzato";
                }

                if(!isAdded && request.getUnauthorizedQuote()!=null
                        &&  request.getUnauthorizedQuote()){
                    note = "Preventivo non autorizzato";
                }

                String requestNote = generateCorrectNote(request);
                requestNote = requestNote.replaceAll("(?i)<br\\p{javaSpaceChar}*(?:/>|>)", "\n");
                note = note.trim().isEmpty() ? requestNote : note.concat(" ").concat(requestNote);
            }

        }else {
            note = "nazionale positiva";
        }

        row.createCell(12, CellType.STRING).setCellValue(note);
        row.createCell(13, CellType.STRING).setCellValue(request.getCdr());
        row.createCell(14, CellType.STRING).setCellValue(request.getNdg());
        row.createCell(15, CellType.STRING).setCellValue(request.getPosition());
        checkTotalCostSpecialColumn(row, request, getColumns().length);

        row.getCell(8).setCellStyle(euroStyle);
        row.getCell(9).setCellStyle(euroStyle);
        row.getCell(10).setCellStyle(euroStyle);
        row.getCell(11).setCellStyle(euroStyle);

    }

    public String generateCorrectNote(Request request) throws PersistenceBeanException, IllegalAccessException {
        String result = "";
        Long maxNumberOfDistinctLandCharesRegistry = getMaxNumberOfDistinctLandCharesRegistry(request);
        String requestExtraCostDistinctTypes = getRequestExtraCostDistinctTypes(request, maxNumberOfDistinctLandCharesRegistry);
        if (!ValidationHelper.isNullOrEmpty(requestExtraCostDistinctTypes)) {
            result = requestExtraCostDistinctTypes;
        } else if(maxNumberOfDistinctLandCharesRegistry > 0L) {
            String prefix = getPrefixCosts(null, maxNumberOfDistinctLandCharesRegistry);
            if (!ValidationHelper.isNullOrEmpty(prefix))
                if (prefix.equalsIgnoreCase("doppia ") || prefix.equalsIgnoreCase("tripla ")) {
                    result = prefix + "ispezione ipotecaria";
                } else {
                    result = prefix + "ispezioni ipotecarie";
                }
        }
        String altroCost = getAltroCostsNote(request);
        if(StringUtils.isNotBlank(altroCost)){
            if(StringUtils.isNotBlank(result))
                result += "<br/>";
            result += altroCost;
        }
        if(!ValidationHelper.isNullOrEmpty(request.getService()) && !ValidationHelper.isNullOrEmpty(request.getService().getIsUpdate()) &&
                request.getService().getIsUpdate()) {
            CostCalculationHelper costCalculationHelper = new CostCalculationHelper(request);
            Boolean billingClient = isBillingClient(request);
            boolean restrictionForPriceList = restrictionForPriceList(request);
            List<PriceList> priceList = costCalculationHelper.loadPriceList(billingClient, restrictionForPriceList);
            System.out.println(">> " + priceList);
            if (!ValidationHelper.isNullOrEmpty(priceList)) {
                double fixedCost = 0;
                PriceList first = priceList.get(0);
                List<Double> numberOfGroupedEstateFormality = request.getSumOfGroupedEstateFormalities();
                Integer numberOfGroupsByDocumentOfEstateFormality = numberOfGroupedEstateFormality.size();
                for (Integer i = 0; i < numberOfGroupsByDocumentOfEstateFormality; i++) {
                    if (!ValidationHelper.isNullOrEmpty(first.getNumberNextBlock())
                            && !ValidationHelper.isNullOrEmpty(first.getNextPrice())) {
                        if (numberOfGroupedEstateFormality.size() > i
                                && numberOfGroupedEstateFormality.get(i) > Double.parseDouble(first.getNumberFirstBlock())) {
                            double y = (numberOfGroupedEstateFormality.get(i) - Double.parseDouble(first.getNumberFirstBlock()))
                                    / Double.parseDouble(first.getNumberNextBlock());
                            y = Math.ceil(y);
                            double yCost = y * Double.parseDouble(first.getNextPrice().replaceAll(",", "."));

                            fixedCost += yCost + Double.parseDouble(first.getFirstPrice().replaceAll(",", "."));
                        } else {
                            fixedCost += Double.parseDouble(first.getFirstPrice().replaceAll(",", "."));
                        }
                    }
                }
                if (fixedCost > 0d) {
                    if (StringUtils.isNotBlank(result))
                        result += "<br/>";
                    result += "Costo ispezione ipotecaria: €" + fixedCost;
                }
            }
        }
        return result;
    }

    public byte[] convertFilteredRequestsToExcel(List<Request> requests, Long selectedClientId)
            throws IOException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(selectedClientId) && selectedClientId > 0) {
            List<ClientInvoiceManageColumn> columns =
                    DaoManager.load(ClientInvoiceManageColumn.class, new CriteriaAlias[]{
                            new CriteriaAlias("client", "client", JoinType.INNER_JOIN)
                    }, new Criterion[]{
                            Restrictions.eq("client.id", selectedClientId)
                    });
            if (!ValidationHelper.isNullOrEmpty(columns)) {

                columns.sort(new Comparator<ClientInvoiceManageColumn>() {
                    @Override
                    public int compare(ClientInvoiceManageColumn c1, ClientInvoiceManageColumn c2) {
                        return c1.getPosition().compareTo(c2.getPosition());
                    }
                });
                requestsEvasionColumns = columns.stream()
                        .filter(c -> ValidationHelper.isNullOrEmpty(c.getRequestType()))
                        .map(c -> c.getField().toString())
                        .toArray(String[]::new);
            } else {
                setRequestsEvasionColumns(requestsEvasionDefaultColumns);
            }
        } else {
            setRequestsEvasionColumns(requestsEvasionDefaultColumns);
        }
        initEvasionReport();
        HSSFFont headerFont = getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);

        HSSFCellStyle headerCellStyle = getWorkbook().createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.TAN.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.forInt(1));
        headerCellStyle.setFont(headerFont);

        HSSFCellStyle dateCellStyle = getWorkbook().createCellStyle();
        CreationHelper createHelper = getWorkbook().getCreationHelper();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

        HSSFCellStyle euroStyle = getEuroStyle();
        Row row = createRow();
        addSeparator(row, headerCellStyle);

        addRequestCosts(requests, dateCellStyle, euroStyle);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getWorkbook().write(baos);
        byte[] xls = baos.toByteArray();
        baos.close();

        getWorkbook().close();
        return xls;
    }

    private void addRequestCosts(List<Request> requests,
                                 CellStyle dateCellStyle, CellStyle euroStyle) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        for (Request request : requests) {
            List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", request.getId())});
            Double result = 0d;
            for (ExtraCost cost : extraCost) {

                if(ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                    result = cost.getPrice();
                    try {
                        Request newRequest = request.reportCopy();
                        newRequest.setTempId(UUID.randomUUID().toString());
                        newRequest.setEstateFormalityList(request.getEstateFormalityList());
                        newRequest.setEvasionDate(request.getEvasionDate());
                        newRequest.setExcelUserName(request.getRequestExcelUserName());
                        addRequestCost(newRequest, null,dateCellStyle, euroStyle,-2, result);
                    } catch (CloneNotSupportedException e) {
                        LogHelper.log(log, e);
                    }
                }
            }

            if (!ValidationHelper.isNullOrEmpty(request.getService())) {
                addRequestCost(request, null,dateCellStyle, euroStyle, -1,-1);
            }else if (!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                int index =0;
                for (Service service : request.getMultipleServices()) {
                    addRequestCost(request, service,dateCellStyle, euroStyle,index++,-1);
                }
            }
        }
    }

    private void addRequestCost(Request request, Service service,
                                CellStyle dateCellStyle, CellStyle euroStyle, int index,double nationalCost) throws PersistenceBeanException, IllegalAccessException, InstantiationException {

        Row row = null;
        row = createRowAfterDefinedEmptyRows(1);

        int colIndex = getIndex(BillingTypeFields.EXCEL_DATE.toString(), requestsEvasionColumns);
        if (colIndex > -1) {
            Cell dateCell = row.createCell(colIndex);
            dateCell.setCellValue(DateTimeHelper.toString(request.getEvasionDate()));
            dateCell.setCellStyle(dateCellStyle);
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_USER.toString(), requestsEvasionColumns);
        if (colIndex > -1) {
            if(index == -2) {
                row.createCell(colIndex).setCellValue(request.getExcelUserName());
            }else
                row.createCell(colIndex).setCellValue(request.getRequestExcelUserName());
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_OFFICE.toString(), requestsEvasionColumns);
        if (colIndex > -1) {
            Office office = DaoManager.get(Office.class, request.getUserOfficeId());
            if (!ValidationHelper.isNullOrEmpty(office)) {
                row.createCell(colIndex).setCellValue(office.getCode() + " " + office.getDescription());
            } else {
                row.createCell(colIndex).setCellValue("");
            }
        }
        colIndex = getIndex(BillingTypeFields.EXCEL_NAME.toString(), requestsEvasionColumns);
        if (colIndex > -1)
            row.createCell(colIndex).setCellValue(request.getSubject() != null ? request.getSubject().getFullName().toUpperCase() : "");
        colIndex = getIndex(BillingTypeFields.EXCEL_CODE.toString(), requestsEvasionColumns);
        if (colIndex > -1)
            row.createCell(colIndex).setCellValue(request.getSubject() != null ? (request.getSubject().getTypeIsPhysicalPerson() ?
                    request.getSubject().getFiscalCode() : request.getSubject().getNumberVAT()) : "");
        colIndex = getIndex(BillingTypeFields.EXCEL_REQUEST_TYPE.toString(), requestsEvasionColumns);
        if (colIndex > -1)
            row.createCell(colIndex).setCellValue(request.getServiceName());
        colIndex = getIndex(BillingTypeFields.EXCEL_CONSERVATORIA.toString(), requestsEvasionColumns);
        if (colIndex > -1) {
            if(index != -2) {
                row.createCell(colIndex).setCellValue(request.getAggregationLandChargesRegistryName());
            }else {
                List<AggregationLandChargesRegistry> aggregationLandChargesRegistries =
                        DaoManager.load(AggregationLandChargesRegistry.class, new Criterion[]{
                                Restrictions.eq("national", Boolean.TRUE)
                        });
                if(!ValidationHelper.isNullOrEmpty(aggregationLandChargesRegistries)) {
                    row.createCell(colIndex).setCellValue(aggregationLandChargesRegistries.get(0).toString());
                }
            }
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_FORMALITY.toString(), requestsEvasionColumns);
        if (colIndex > -1)
            row.createCell(colIndex).setCellValue(!ValidationHelper.isNullOrEmpty(request.getNumberActUpdate()) ?
                    request.getNumberActUpdate() : getNumActs(request.getId()).longValue());
        colIndex = getIndex(BillingTypeFields.EXCEL_MORTGAGE_EXPENSES.toString(), requestsEvasionColumns);
        if (colIndex > -1) {
            if(index != -2) {
                row.createCell(colIndex, CellType.NUMERIC).setCellValue(getMortgageCost(request));
            }else {
                row.createCell(colIndex, CellType.NUMERIC).setCellValue(nationalCost);
            }
            row.getCell(colIndex).setCellStyle(euroStyle);
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_CATASTAL_EXPENSES.toString(), requestsEvasionColumns);
        if (colIndex > -1) {
            row.createCell(colIndex, CellType.NUMERIC).setCellValue(getCatastalCost(request));
            row.getCell(colIndex).setCellStyle(euroStyle);
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_COMPENSATION.toString(), requestsEvasionColumns);
        if (colIndex > -1) {
            row.createCell(colIndex, CellType.NUMERIC).setCellValue(ValidationHelper.isNullOrEmpty(request.getCostPay()) ?
                    0d : request.getCostPay());
            row.getCell(colIndex).setCellStyle(euroStyle);
        }
        colIndex = getIndex(BillingTypeFields.EXCEL_TOTAL.toString(), requestsEvasionColumns);
        if (colIndex > -1) {
            if(index != -2) {
                Double result = 0d;
                if(service != null) {
                    if(index == 0) {
                        List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                                Restrictions.eq("requestId", request.getId())});
                        for (ExtraCost cost : extraCost) {
                            result += cost.getPrice();
                        }
                    }
                    Boolean billingClient = isBillingClient(request);
                    boolean restrictionForPriceList = restrictionForPriceList(request);
                    result += getCostCadastral(request);
                    result += getCostEstate(request, service, billingClient, restrictionForPriceList);
                    result += getCostExtra(request, service, billingClient, restrictionForPriceList);
                    result += getCostEstateFormality(request, service, billingClient, restrictionForPriceList);
                    result += getCostPay(request, service, billingClient, restrictionForPriceList);
                    result += getCostPlus(request, service, billingClient, restrictionForPriceList);
                    result = (double) Math.round((result)* 100000d) / 100000d;
                }else {
                    result = ValidationHelper.isNullOrEmpty(request.getTotalCost()) ? 0d
                            : Double.parseDouble(request.getTotalCost().replaceAll(",", "."));
                }
                row.createCell(colIndex, CellType.NUMERIC).setCellValue(result);

            }else {
                row.createCell(colIndex, CellType.NUMERIC).setCellValue(nationalCost);
            }

            row.getCell(colIndex).setCellStyle(euroStyle);
        }
        colIndex = getIndex(BillingTypeFields.EXCEL_NOTE.toString(), requestsEvasionColumns);
        if (colIndex > -1) {

            String note = "";
            if(index != -2) {
                if(!ValidationHelper.isNullOrEmpty(request.getCostNote())) {
                    note = request.getCostNote();
                }else {
                    boolean isAdded = Boolean.FALSE;
                    List<Document> requestDocuments = DaoManager.load(Document.class,
                            new CriteriaAlias[]{new CriteriaAlias("request", "request", JoinType.INNER_JOIN)},
                            new Criterion[]{Restrictions.and(Restrictions.eq("request.id", request.getId()),
                                    Restrictions.eq("typeId", 2L))});
                    if (!ValidationHelper.isNullOrEmpty(requestDocuments)) {
                        if(request.getService() !=null
                                && request.getService().getUnauthorizedQuote()!=null && request.getService().getUnauthorizedQuote()){
                            note = "Preventivo non autorizzato";
                            isAdded = Boolean.TRUE;
                        }
                    }
                    if(!isAdded && request.getAuthorizedQuote()!=null &&  request.getAuthorizedQuote()){
                        note = "Preventivo autorizzato";
                    }
                    if(!isAdded && request.getUnauthorizedQuote() != null
                            &&  request.getUnauthorizedQuote()){
                        note = "Preventivo non autorizzato";
                    }
                    String requestNote = generateCorrectNote(request);
                    requestNote = requestNote.replaceAll("(?i)<br\\p{javaSpaceChar}*(?:/>|>)", "\n");
                    note = note.trim().isEmpty() ? requestNote : note.concat(" ").concat(requestNote);
                }
            }else {
                note = "nazionale positiva";
            }
            row.createCell(colIndex, CellType.STRING).setCellValue(note);
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_CDR.toString(), requestsEvasionColumns);
        if (colIndex > -1)
            row.createCell(colIndex, CellType.STRING).setCellValue(request.getCdr());

        colIndex = getIndex(BillingTypeFields.EXCEL_NDG.toString(), requestsEvasionColumns);
        if (colIndex > -1)
            row.createCell(colIndex, CellType.STRING).setCellValue(request.getNdg());

        colIndex = getIndex(BillingTypeFields.EXCEL_POSITION.toString(), requestsEvasionColumns);
        if (colIndex > -1 && !ValidationHelper.isNullOrEmpty(request.getPosition())) {
            row.createCell(colIndex, CellType.STRING).setCellValue(request.getPosition());
        }

        checkTotalCostSpecialColumn(row, request, getColumns().length);
        autoSizeColumnsAndSetSizeToAnotherByDefault(EVASION_COLUMNS_WITHOUT_COSTS);

        optimizeColumnSizeIfItLessMinimalSize(getIndex(ResourcesHelper.getString("excelNote"), requestsColumns) + 1);
        optimizeColumnSizeIfItLessMinimalSize(getColumns().length);
    }

    private void checkTotalCostSpecialColumn(Row row, Request request, int colIndex)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        double calculatedTotalCost = new CostCalculationHelper(request).calculateTotalCost(Boolean.TRUE);
        double totalCostFromRequest = ValidationHelper.isNullOrEmpty(request.getTotalCost()) ?
                0d : Double.parseDouble(request.getTotalCost().replaceAll(",", "."));
        double totalCostByColumns = getMortgageCost(request) + getCatastalCost(request)
                + (ValidationHelper.isNullOrEmpty(request.getCostPay()) ? 0d : request.getCostPay());

        if (Math.abs(calculatedTotalCost - totalCostFromRequest) > 0.0001
                || Math.abs(totalCostByColumns - totalCostFromRequest) > 0.0001) {
            row.createCell(colIndex, CellType.STRING)
                    .setCellValue(ResourcesHelper.getString("requestListTotalCostDifferent"));
        }
    }
    public void checkTotalCostSpecialColumn(Row row, Request request, int colIndex, boolean isExcelData,
                                             HSSFCellStyle wrapyStyle)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        double calculatedTotalCost = new CostCalculationHelper(request).calculateTotalCost(Boolean.TRUE);
        double totalCostFromRequest = ValidationHelper.isNullOrEmpty(request.getTotalCost()) ?
                0d : Double.parseDouble(request.getTotalCost().replaceAll(",", "."));
        double totalCostByColumns = getMortgageCost(request) + getCatastalCost(request)
                + (ValidationHelper.isNullOrEmpty(request.getCostPay()) ? 0d : request.getCostPay());

        if (Math.abs(calculatedTotalCost - totalCostFromRequest) > 0.0001
                || Math.abs(totalCostByColumns - totalCostFromRequest) > 0.0001) {
            if(ValidationHelper.isNullOrEmpty(isExcelData) || !isExcelData){
                row.createCell(colIndex, CellType.STRING)
                        .setCellValue(ResourcesHelper.getString("requestListTotalCostDifferent"));
            }else {
                row.createCell(colIndex, CellType.STRING)
                        .setCellValue(ResourcesHelper.getString("requestListTotalCostDifferent"));

                colIndex = getIndex(ResourcesHelper.getString("excelNote"), requestsColumns);
                Cell cell =row.getCell(colIndex);
                if(cell != null){
                    String cellValue = cell.getStringCellValue();
                    cell.setCellStyle(wrapyStyle);
                    String updatedValue = "";
                    int startIndex = 0;

                    if(!ValidationHelper.isNullOrEmpty(cellValue)){
                        if(cellValue.equals("0")){
                            cellValue ="";
                        }
                        if(!ValidationHelper.isNullOrEmpty(cellValue)){
                            startIndex = cellValue.length() + 1;
                            updatedValue = cellValue + "\nAnomalia costi";
                        }else {
                            updatedValue ="Anomalia costi";
                        }
                    }
                    HSSFFont orangeFont = getWorkbook().createFont();
                    orangeFont.setColor(IndexedColors.ORANGE.getIndex());
                    HSSFRichTextString richString = new HSSFRichTextString(updatedValue);
                    richString.applyFont(startIndex, updatedValue.length(), orangeFont);
                    cell.setCellValue(richString);
                }
            }

        }
    }
    public boolean checkTotalCostSpecialColumn(Request request)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        request.setCalculateCost(Boolean.TRUE);
        double calculatedTotalCost = new CostCalculationHelper(request).calculateTotalCost(Boolean.TRUE);
        double totalCostFromRequest = ValidationHelper.isNullOrEmpty(request.getTotalCost()) ?
                0d : Double.parseDouble(request.getTotalCost().replaceAll(",", "."));
       double totalCostByColumns = getMortgageCost(request) + getCatastalCost(request)
                + (ValidationHelper.isNullOrEmpty(request.getCostPay()) ? 0d : request.getCostPay());
        request.setCalculateCost(null);
       /*  List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId())});

        for (ExtraCost cost : extraCost) {
            if(ValidationHelper.isNullOrEmpty(cost.getType()) ||
                    !ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType()) ||
                    !ExtraCostType.IPOTECARIO.equals(cost.getType()) ||
                    !ExtraCostType.CATASTO.equals(cost.getType())) {
                totalCostByColumns += cost.getPrice();
            }
        } */
        log.info("For " + request.getFiscalCodeVATNamber());
        log.info("calculatedTotalCost " + calculatedTotalCost + ", totalCostFromRequest " + totalCostFromRequest + ", totalCostByColumns  " + totalCostByColumns);
        if (Math.abs(calculatedTotalCost - totalCostFromRequest) > 0.0001) {
            //    || Math.abs(totalCostByColumns - totalCostFromRequest) > 0.0001) {
            return true;
        }
        return false;
    }

    private int getIndex(String columnName, String[] columns) {
        return Arrays.asList(columns)
                .stream()
                .filter(col -> !ValidationHelper.isNullOrEmpty(col))
                .map(col -> col.toUpperCase())
                .collect(Collectors.toList())
                .indexOf(columnName.toUpperCase());
    }

    private void addHeader(Request firstRequest, Document existingDocumentForThisRequests, CellStyle cellStyle) {
        String billingClient = "";
        String client = "";
        String office = "";
        String trust = "";
        String ndg = "";
        String referenceRequest = "";
        if (!ValidationHelper.isNullOrEmpty(firstRequest.getMail())) {
            if (!ValidationHelper.isNullOrEmpty(firstRequest.getMail().getManagers())) {
                client = firstRequest.getMail().getManagers().stream().map(Client::toString).collect(Collectors.joining(", "));

                if (!ValidationHelper.isNullOrEmpty(firstRequest.getMail().getManagers().get(0).getOffice())) {
                    office = firstRequest.getMail().getManagers().get(0).getOffice().getDescription();
                }
            }

            if (!ValidationHelper.isNullOrEmpty(firstRequest.getMail().getClientFiduciary())) {
                trust = firstRequest.getMail().getClientFiduciary().toString();
                if (!ValidationHelper.isNullOrEmpty(firstRequest.getMail().getClientFiduciary().getOffice())) {
                    office = firstRequest.getMail().getClientFiduciary().getOffice().getDescription();
                }
            }
            if (!ValidationHelper.isNullOrEmpty(firstRequest.getMail().getClientInvoice())) {
                billingClient = firstRequest.getMail().getClientInvoice().toString();
            }
        }

        if (!ValidationHelper.isNullOrEmpty(firstRequest.getMail())
                && !ValidationHelper.isNullOrEmpty(firstRequest.getMail().getNdg())) {
            ndg = firstRequest.getMail().getNdg();
        }
        if (!ValidationHelper.isNullOrEmpty(firstRequest.getMail())
                && !ValidationHelper.isNullOrEmpty(firstRequest.getMail().getReferenceRequest())) {
            referenceRequest = firstRequest.getMail().getReferenceRequest();
        }

        Row row = createRowAfterDefinedEmptyRows(1);
        row.createCell(1).setCellValue(ResourcesHelper.getString("billingCustomer") + billingClient);
        row.getCell(1).setCellStyle(cellStyle);

        row = createRowAfterDefinedEmptyRows(2);

        row.createCell(1).setCellValue(ResourcesHelper.getString("reportN") + " "
                + (existingDocumentForThisRequests == null || existingDocumentForThisRequests.getInvoiceNumber() == null ?
                SaveRequestDocumentsHelper.getLastInvoiceNumber() + 1 : existingDocumentForThisRequests.getInvoiceNumber()));
        row.getCell(1).setCellStyle(cellStyle);
        row.createCell(2).setCellValue(ResourcesHelper.getString("referenceInvoice"));
        row.getCell(2).setCellStyle(cellStyle);

        row = createRowAfterDefinedEmptyRows(2);

        row.createCell(1).setCellValue(ResourcesHelper.getString("officeText") + office);
        row.getCell(1).setCellStyle(cellStyle);
        row.createCell(2).setCellValue(ResourcesHelper.getString("managerText") + client);
        row.getCell(2).setCellStyle(cellStyle);
        row.createCell(4).setCellValue(ResourcesHelper.getString("trust") + trust);
        row.getCell(4).setCellStyle(cellStyle);

        row = createRowAfterDefinedEmptyRows(2);

        row.createCell(1).setCellValue(ResourcesHelper.getString("ndgText") + ndg);
        row.getCell(1).setCellStyle(cellStyle);
        row.createCell(2).setCellValue(ResourcesHelper.getString("rifText") + referenceRequest);
        row.getCell(2).setCellStyle(cellStyle);
    }


    private void addHeader(CellStyle cellStyle,
                           ExcelDataWrapper excelDataWrapper) {
        String billingClient = "";
        String client = "";
        String office = excelDataWrapper.getOffice();
        String trust = "";
        String ndg = excelDataWrapper.getNdg() != null ? excelDataWrapper.getNdg() : "";
        String reportn = excelDataWrapper.getReportn() != null ? String.valueOf(excelDataWrapper.getReportn()) : "";
        String fatturaN = excelDataWrapper.getFatturan() != null ? String.valueOf(excelDataWrapper.getFatturan()) : "";
        String data = excelDataWrapper.getData() != null ? DateTimeHelper.toFormatedString(excelDataWrapper.getData(), DateTimeHelper.getDatePattern()) : "";
        //String fatturaDiRiferimento = excelDataWrapper.getFatturaDiRiferimento() != null ? excelDataWrapper.getFatturaDiRiferimento() : "";

        String referenceRequest = excelDataWrapper.getReferenceRequest() != null ? excelDataWrapper.getReferenceRequest() : "";

        if (!ValidationHelper.isNullOrEmpty(excelDataWrapper.getManagers())) {
            client = excelDataWrapper.getManagers().stream().map(Client::toString).collect(Collectors.joining(", "));
//            if (!ValidationHelper.isNullOrEmpty(excelDataWrapper.getManagers().get(0).getOffice())) {
//                    office = excelDataWrapper.getManagers().get(0).getOffice().getDescription();
//            }
        }

        if (!ValidationHelper.isNullOrEmpty(excelDataWrapper.getClientFiduciary())) {
            trust = excelDataWrapper.getClientFiduciary().toString();
//                if (!ValidationHelper.isNullOrEmpty(excelDataWrapper.getClientFiduciary().getOffice())) {
//                    office = excelDataWrapper.getClientFiduciary().getOffice().getDescription();
//                }
        }

        if (!ValidationHelper.isNullOrEmpty(excelDataWrapper.getClientInvoice())) {
            billingClient = excelDataWrapper.getClientInvoice().toString();
        }

        HSSFFont boldFonts = getWorkbook().createFont();
        boldFonts.setBold(true);
        Row row = createRowAfterDefinedEmptyRows(1);
        Cell cell = row.createCell(0);
        String value = ValidationHelper.isNullOrEmpty(billingClient) ? ResourcesHelper.getString("billingCustomer"):ResourcesHelper.getString("billingCustomer")+" "+billingClient;
        cell.setCellValue(value);
        if(!ValidationHelper.isNullOrEmpty(billingClient)) {
            RichTextString rts = cell.getRichStringCellValue();
            rts.applyFont(value.indexOf(billingClient), value.length(), boldFonts);
            cell.setCellValue(rts);
        }
        row.getCell(0).setCellStyle(cellStyle);

        row = createRowAfterDefinedEmptyRows(2);

        cell = row.createCell(0);
        boolean emptyFattura = ValidationHelper.isNullOrEmpty(fatturaN);
        value = emptyFattura ? ResourcesHelper.getString("fatturaN"):ResourcesHelper.getString("fatturaN")+" "+fatturaN;
        cell.setCellValue(value);
        if(!emptyFattura) {

            RichTextString rts = cell.getRichStringCellValue();
            rts.applyFont(value.indexOf(fatturaN), value.length(), boldFonts);
            cell.setCellValue(rts);
        }

        row.getCell(0).setCellStyle(cellStyle);

        cell = row.createCell(2);
        boolean emptyData = ValidationHelper.isNullOrEmpty(fatturaN);
        value = emptyData ? ResourcesHelper.getString("excelData"):ResourcesHelper.getString("excelData") + " "+ data;
        cell.setCellValue(value);
        if(!emptyData) {

            RichTextString rts = cell.getRichStringCellValue();
            rts.applyFont(value.indexOf(data), value.length(), boldFonts);
            cell.setCellValue(rts);
        }

        row.getCell(2).setCellStyle(cellStyle);

        row.createCell(4).setCellValue(ResourcesHelper.getString("reportN") + " "+ reportn);
        row.getCell(4).setCellStyle(cellStyle);

//        row.createCell(2).setCellValue(ResourcesHelper.getString("referenceInvoice") + " "+ fatturaDiRiferimento);
//        row.getCell(2).setCellStyle(cellStyle);

        row = createRowAfterDefinedEmptyRows(2);


        row.createCell(0).setCellValue(ResourcesHelper.getString("officeText") + (office != null ? office.trim() : ""));
        row.getCell(0).setCellStyle(cellStyle);
        row.createCell(2).setCellValue(ResourcesHelper.getString("managerText") + client);
        row.getCell(2).setCellStyle(cellStyle);
        row.createCell(4).setCellValue(ResourcesHelper.getString("trust"));
        row.getCell(4).setCellStyle(cellStyle);
        row.createCell(5).setCellValue(trust);
        row.getCell(5).setCellStyle(cellStyle);

        row = createRowAfterDefinedEmptyRows(2);

        row.createCell(0).setCellValue(ResourcesHelper.getString("ndgText") + " " + ndg);
        row.getCell(0).setCellStyle(cellStyle);
        row.createCell(2).setCellValue(ResourcesHelper.getString("rifText") + referenceRequest);
        row.getCell(2).setCellStyle(cellStyle);
    }

    private void addCosts(List<Request> requests, CellStyle cellStyle, Font font) throws PersistenceBeanException, IllegalAccessException, InstantiationException {

        List<Request> nonNational = new ArrayList<>();
        boolean isNational = false;
        String cf = "";

        Map<Long, Request> nationalRequests = new HashMap<>();
        for (Request request : requests) {
            List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", request.getId())});
            Double result;

            for (ExtraCost cost : extraCost) {
                if(ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                    result = cost.getPrice();
                    try {
                        Request newRequest = request.reportCopy();
                        newRequest.setTempId(UUID.randomUUID().toString());
                        newRequest.setEstateFormalityList(request.getEstateFormalityList());
                        newRequest.setEvasionDate(request.getEvasionDate());
                        newRequest.setResult(result);
                        nationalRequests.put(request.getId(), newRequest);
                    } catch (CloneNotSupportedException e) {
                        LogHelper.log(log, e);
                    }
                }
            }
        }
        List<Long> processedIds = new ArrayList<>();
        for (Map.Entry<Long, Request> entry : nationalRequests.entrySet()) {
            Request nationalRequest = nationalRequests.get(entry.getKey());
            addCost(nationalRequest, null, cellStyle, font,-2, nationalRequest.getResult());
            List<Request> matchedCfRequests = requests.
                    stream()
                    .filter(r -> !ValidationHelper.isNullOrEmpty(r.getFiscalCodeVATNamber()) &&
                    r.getFiscalCodeVATNamber().equalsIgnoreCase(nationalRequest.getFiscalCodeVATNamber()))
                    .collect(Collectors.toList());

            for (Request request : matchedCfRequests) {
                processRequest(request, cellStyle, font);
                processedIds.add(request.getId());
            }
        }
        for (Request request : requests) {
            if(!processedIds.contains(request.getId())){
                 processRequest(request, cellStyle, font);
            }
        }
//c1
//        for (Request request : requests) {
//            List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
//                    Restrictions.eq("requestId", request.getId())});
//            Double result = 0d;
//
//            for (ExtraCost cost : extraCost) {
//                if(ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
//                    result = cost.getPrice();
//                    try {
//                        Request newRequest = request.reportCopy();
//                        newRequest.setTempId(UUID.randomUUID().toString());
//                        newRequest.setEstateFormalityList(request.getEstateFormalityList());
//                        newRequest.setEvasionDate(request.getEvasionDate());
//                        addCost(newRequest, null, cellStyle, font,-2, result);
//                        isNational = true;
//                    } catch (CloneNotSupportedException e) {
//                        LogHelper.log(log, e);
//                    }
//                }
//            }
//            if(isNational) {
//                cf = request.getFiscalCodeVATNamber();
//                processRequest(request, cellStyle, font);
//            }else {
//                nonNational.add(request);
//            }
//        }
//        if(isNational){
//            for (Request request : nonNational) {
//                if(cf.equalsIgnoreCase(request.getFiscalCodeVATNamber())){
//                    processRequest(request, cellStyle, font);
//                }
//            }
//            for (Request request : nonNational) {
//                if(!cf.equalsIgnoreCase(request.getFiscalCodeVATNamber())){
//                    processRequest(request, cellStyle, font);
//                }
//            }
//        }else {
//            for (Request request : nonNational) {
//                processRequest(request, cellStyle, font);
//            }
//        }
    }

    private void processRequest(Request request, CellStyle cellStyle, Font font)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(request.getService())) {
            addCost(request, null, cellStyle, font,-1,-1);
        }else if (!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
            int index =0;
            for (Service service : request.getMultipleServices()) {
                addCost(request, service, cellStyle, font,index++,-1);
            }
        }
    }

    private void addCost(Request request, Service service, CellStyle cellStyle, Font font,
                         int index,double nationalCost) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        HSSFCellStyle euroStyle = getEuroStyle();
        euroStyle.setFont(font);

//        HSSFCellStyle wrapStyle = getWrapTextStyle();
//        wrapStyle.setFont(font);
        HSSFCellStyle currencyStyle = getCurrencyStyle();
        currencyStyle.setFont(font);

        Row row = null;
        row = createRowAfterDefinedEmptyRows(1);
        Boolean billingClient = isBillingClient(request);
        boolean restrictionForPriceList = restrictionForPriceList(request);
        int colIndex = getIndex(ResourcesHelper.getString("requestedDate"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex).setCellValue(request.getCreateDateStr());
            row.getCell(colIndex).setCellStyle(cellStyle);
        }
        colIndex = getIndex(ResourcesHelper.getString("nominative"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex)
                    .setCellValue(request.getSubject() != null ? (request.getSubject().getFullName() != null ? request.getSubject().getFullName().toUpperCase() : "") : "");
            row.getCell(colIndex).setCellStyle(cellStyle);
        }
        colIndex = getIndex(ResourcesHelper.getString("codFiscIva"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex).setCellValue(request.getFiscalCodeVATNamber());
            row.getCell(colIndex).setCellStyle(cellStyle);
        }

        colIndex = getIndex(ResourcesHelper.getString("permissionRequest"), requestsColumns);
        if (colIndex > -1) {
            //row.createCell(colIndex).setCellValue(service== null ? "" : service.toString().toUpperCase());
            String serviceName = request.getServiceName()!=null?request.getServiceName().toUpperCase():"";
            serviceName = service== null ? serviceName : service.toString().toUpperCase();
            row.createCell(colIndex).setCellValue(serviceName);
            row.getCell(colIndex).setCellStyle(cellStyle);
        }

        colIndex = getIndex(ResourcesHelper.getString("requestListCorservatoryName"), requestsColumns);
        if (colIndex > -1) {
            if(index != -2) {
                row.createCell(colIndex).setCellValue(request.getAggregationLandChargesRegistryName());
            }else {
                List<AggregationLandChargesRegistry> aggregationLandChargesRegistries =
                        DaoManager.load(AggregationLandChargesRegistry.class, new Criterion[]
                                {Restrictions.eq("national", Boolean.TRUE)});
                if(aggregationLandChargesRegistries.size() > 0) {
                    row.createCell(colIndex).setCellValue(aggregationLandChargesRegistries.get(0).getName());
                }
            }
            row.getCell(colIndex).setCellStyle(cellStyle);
        }
        colIndex = getIndex(ResourcesHelper.getString("requestPrintFormalityPresentationDate"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex).setCellValue(DateTimeHelper.toString(request.getEvasionDate()));
            row.getCell(colIndex).setCellStyle(cellStyle);
        }
        colIndex = getIndex(ResourcesHelper.getString("excelForm"), requestsColumns);
        if (colIndex > -1 && index != -2) {
            Long val = request.getNumberActOrSumOfEstateFormalitiesAndOther().longValue() + getRequestExtraCostValue(request).longValue();
            row.createCell(colIndex).setCellValue(val);
            row.getCell(colIndex).setCellStyle(cellStyle);
        }
        colIndex = getIndex(ResourcesHelper.getString("mortgageRights"), requestsColumns);
        if (colIndex > -1) {
            Double cost = 0d;
            if(index != -2) {
                if(!ValidationHelper.isNullOrEmpty(service)) {
                    cost = getCostEstateFormalityAndExtraCostRelated(
                            request,service,billingClient, restrictionForPriceList);
                }else {
                    cost = getCostEstateFormalityAndExtraCostRelated(request);
                }
            }else {
                cost += nationalCost;
            }

            if(cost > 0) {
                row.createCell(colIndex, CellType.NUMERIC).setCellValue(cost);
                row.getCell(colIndex).setCellStyle(currencyStyle);
            }
        }
        if(index != -2) {
            colIndex = getIndex(ResourcesHelper.getString("landRegistryRights"), requestsColumns);
            if (colIndex > -1) {
                Double cost = getCostCadastralAndExtraCostRelated(request);
                if(cost > 0) {
                    row.createCell(colIndex, CellType.NUMERIC).setCellValue(cost);
                    row.getCell(colIndex).setCellStyle(currencyStyle);
                }
            }
            colIndex = getIndex(ResourcesHelper.getString("compensation"), requestsColumns);
            if (colIndex > -1) {
                Double costpay = 0d;
                if(!ValidationHelper.isNullOrEmpty(service)) {
                    costpay = getCostPay(
                            request, service, billingClient, restrictionForPriceList);
                }else {
                    costpay = ValidationHelper.isNullOrEmpty(request.getCostPay()) ? 0d : request.getCostPay();
                }
                if(costpay > 0) {
                    row.createCell(colIndex, CellType.NUMERIC).setCellValue(costpay);
                    row.getCell(colIndex).setCellStyle(currencyStyle);
                }
            }
        }

        colIndex = getIndex(ResourcesHelper.getString("formalityTotal"), requestsColumns);
        if (colIndex > -1) {
            if(index != -2) {
                Double result = 0d;
                if(!ValidationHelper.isNullOrEmpty(service)) {
                    if (!ValidationHelper.isNullOrEmpty(request.getCostCadastral())) {
                        result += request.getCostCadastral();
                    }
                    result += getCostExtra(request, service, billingClient, restrictionForPriceList);
                    result += getCostEstateFormality(request, service, billingClient, restrictionForPriceList);
                    result += getCostPay(request, service, billingClient, restrictionForPriceList);
                    result = (double) Math.round((result)* 100000d) / 100000d;
                }else {
                    result = ValidationHelper.isNullOrEmpty(request.getTotalCost()) ? 0d
                            : Double.parseDouble(request.getTotalCost().replaceAll(",", "."));
                }

                if(result > 0) {
                    row.createCell(colIndex, CellType.NUMERIC).setCellValue(result);
                    row.getCell(colIndex).setCellStyle(currencyStyle);
                }
            }else {
                if(nationalCost > 0) {
                    row.createCell(colIndex, CellType.NUMERIC).setCellValue(nationalCost);
                    row.getCell(colIndex).setCellStyle(currencyStyle);
                }
            }
        }
        colIndex = getIndex(ResourcesHelper.getString("excelNote"), requestsColumns);
        if (colIndex > -1) {
            if(index == 0) {
                Double result = 0d;
                List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", request.getId())});
                for (ExtraCost cost : extraCost) {
                    result += cost.getPrice();
                }
                if(result > 0) {
                    row.createCell(colIndex, CellType.STRING).setCellValue("Costo aggiuntivo: " + result);
                    row.getCell(colIndex).setCellStyle(cellStyle);
                }
            }else  if(index != -2) {
                if (!ValidationHelper.isNullOrEmpty(request.getCostNote())) {
                    row.createCell(colIndex, CellType.STRING).setCellValue(request.getCostNote());
                    row.getCell(colIndex).setCellStyle(cellStyle);
                } else {
                    String requestNote = generateCorrectNote(request);
                    requestNote = requestNote.replaceAll("(?i)<br\\p{javaSpaceChar}*(?:/>|>)", "\n");
                    row.createCell(colIndex, CellType.STRING).setCellValue(requestNote);
                    row.getCell(colIndex).setCellStyle(cellStyle);
                }
            }else {
                row.createCell(colIndex, CellType.STRING).setCellValue("nazionale positiva");
                row.getCell(colIndex).setCellStyle(cellStyle);
            }
        }
        colIndex = getIndex(ResourcesHelper.getString("excelCDR"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex, CellType.STRING).setCellValue(request.getCdr());
            row.getCell(colIndex).setCellStyle(cellStyle);
        }

        colIndex = getIndex(ResourcesHelper.getString("excelNDG"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex, CellType.STRING).setCellValue(request.getNdg());
            row.getCell(colIndex).setCellStyle(cellStyle);
        }

        colIndex = getIndex(ResourcesHelper.getString("excelUser"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex).setCellValue(request.getRequestExcelUserName());
            row.getCell(colIndex).setCellStyle(cellStyle);
        }

        colIndex = getIndex(ResourcesHelper.getString("excelPosition"), requestsColumns);
        if (colIndex > -1 && !ValidationHelper.isNullOrEmpty(request.getPosition())) {
            row.createCell(colIndex, CellType.STRING).setCellValue(request.getPosition());
            row.getCell(colIndex).setCellStyle(cellStyle);
        }

        colIndex = getIndex(ResourcesHelper.getString("excelStamps"), requestsColumns);
        if (colIndex > -1) {
            if (index != -2) {
                Double result = getExtraCostRelated(request, ExtraCostType.MARCA);
                if(result > 0) {
                    row.createCell(colIndex, CellType.NUMERIC).setCellValue(result);
                    row.getCell(colIndex).setCellStyle(currencyStyle);
                }
            }
        }
        colIndex = getIndex(ResourcesHelper.getString("excelPostalExpenses"), requestsColumns);
        if (colIndex > -1) {
            if (index != -2) {
                Double result = getExtraCostRelated(request, ExtraCostType.POSTALE);
                if(result > 0) {
                    row.createCell(colIndex, CellType.NUMERIC).setCellValue(result);
                    row.getCell(colIndex).setCellStyle(currencyStyle);
                }
            }
        }

       // checkTotalCostSpecialColumn(row, request, getColumns().length, true, wrapStyle);
    }

    private void addFooter(List<Request> requests, CellStyle cellStyle, Font font) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        HSSFCellStyle euroStyle = getEuroStyle();
        euroStyle.setFont(font);

        Row row = createRowAfterDefinedEmptyRows(2);

        row.createCell(0).setCellValue(ResourcesHelper.getString("formalityTotal").toUpperCase());
        row.getCell(0).setCellStyle(cellStyle);
        int colIndex = getIndex(ResourcesHelper.getString("mortgageRights"), requestsColumns);
        if (colIndex > -1) {
            Double result = getSumOfCostEstateFormalityService(requests);
            /*Code Optimization Start. Making one db call to get data*/
            List<Long> ids = requests.stream()
                    .map(Request::getId).collect(Collectors.toList());
            List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.in("requestId", ids)});
            for (ExtraCost cost : extraCost) {
                if(cost.getType() != null &&
                        ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                    result += cost.getPrice();
                }
            }
            /*Code Optimization End.*/
//            for (Request request : requests) {
//                List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
//                        Restrictions.eq("requestId", request.getId())});
//
//                for (ExtraCost cost : extraCost) {
//                    if(ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
//                        result += cost.getPrice();
//                    }
//                }
//            }
            row.createCell(colIndex, CellType.NUMERIC).setCellValue(result);
            row.getCell(colIndex).setCellStyle(euroStyle);
        }
        colIndex = getIndex(ResourcesHelper.getString("landRegistryRights"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex, CellType.NUMERIC).setCellValue(getSumOfCostCadastral(requests));
            row.getCell(colIndex).setCellStyle(euroStyle);
        }
        colIndex = getIndex(ResourcesHelper.getString("compensation"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex, CellType.NUMERIC).setCellValue(getSumOfCostPayServices(requests));
            row.getCell(colIndex).setCellStyle(euroStyle);
        }
        colIndex = getIndex(ResourcesHelper.getString("formalityTotal"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex, CellType.NUMERIC).setCellValue(getSumOfCostTotalServices(requests));
            row.getCell(colIndex).setCellStyle(euroStyle);
        }

        colIndex = getIndex(ResourcesHelper.getString("formalityTotal"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex, CellType.NUMERIC).setCellValue(getSumOfCostTotalServices(requests));
            row.getCell(colIndex).setCellStyle(euroStyle);
        }

        colIndex = getIndex(ResourcesHelper.getString("excelStamps"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex, CellType.NUMERIC).setCellValue(getSumOfExtraCost(requests, ExtraCostType.MARCA));
            row.getCell(colIndex).setCellStyle(euroStyle);
        }

        colIndex = getIndex(ResourcesHelper.getString("excelPostalExpenses"), requestsColumns);
        if (colIndex > -1) {
            row.createCell(colIndex, CellType.NUMERIC).setCellValue(getSumOfExtraCost(requests, ExtraCostType.POSTALE));
            row.getCell(colIndex).setCellStyle(euroStyle);
        }
    }

    private BigInteger getNumActs(Long requestId) throws PersistenceBeanException, IllegalAccessException {
        BigInteger countFormality = DaoManager.countQuery("select count(*) " +
                "from request_formality where request_id=" + requestId);
        BigInteger countSuccessFormality = DaoManager.countQuery("select count(*)" +
                "from (estate_formality_success inner join request_formality on " +
                "estate_formality_success.estate_formality_id=request_formality.formality_id) " +
                "where estate_formality_success.note_type=\"NOTE_TYPE_A\" and request_formality.request_id=" + requestId);
        BigInteger countCommunication = DaoManager.countQuery("select count(*) " +
                "from (communication inner join request_formality on " +
                "communication.estate_formality_id=request_formality.formality_id) " +
                "where request_formality.request_id=" + requestId);
        return countFormality.add(countSuccessFormality).add(countCommunication);
    }

    private Double getMortgageCost(Request request) throws PersistenceBeanException, IllegalAccessException {
        Double estateFormalityCost = !ValidationHelper.isNullOrEmpty(request.getCostEstateFormality()) ? request.getCostEstateFormality() : 0d;
        Double extraCostSum = getRequestExtraCostSumByType(request.getId(), ExtraCostType.IPOTECARIO);
        return estateFormalityCost + extraCostSum;
    }

    private Double getCatastalCost(Request request) throws PersistenceBeanException, IllegalAccessException {
        Double cadastralCost = !ValidationHelper.isNullOrEmpty(request.getCostCadastral()) ? request.getCostCadastral() : 0d;
        Double extraCostSum = getRequestExtraCostSumByType(request.getId(), ExtraCostType.CATASTO);
        return cadastralCost + extraCostSum;
    }

    private Double getAltroCost(Request request) throws PersistenceBeanException, IllegalAccessException {
        return getRequestExtraCostSumByType(request.getId(), ExtraCostType.ALTRO);
    }

    private Double getRequestExtraCostSumByType(Long requestId, ExtraCostType type)
            throws PersistenceBeanException, IllegalAccessException {
        Double extraCostSum = 0d;
        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", requestId),
                Restrictions.eq("type", type)});
        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            for (ExtraCost cost : extraCosts) {
                extraCostSum += cost.getPrice();
            }
        }
        return extraCostSum;
    }

    public Long getMaxNumberOfDistinctLandCharesRegistry(Request request)
            throws PersistenceBeanException, IllegalAccessException {
        Long result = 0L;
        List<DocumentSubject> documentSubjects = null;
        Optional<Long> maxNumberOfDistinctLandCharesRegistry = Optional.empty();
//        List<Document> documents = request.getDocumentsRequest().stream().filter(x -> x.getTypeId().equals(5L)).collect(Collectors.toList());
        List<Long> documentIds = CollectionUtils.emptyIfNull(request.getRequestFormalities())
                .stream()
                .filter(rf -> !ValidationHelper.isNullOrEmpty(rf.getDocumentId()))
                .map(RequestFormality::getDocumentId)
                .distinct()
                .skip(1).collect(Collectors.toList());

        List<Document> documents = null;
        if(documentIds.size() > 0)
            documents = DaoManager.load(Document.class, new Criterion[]{
                    Restrictions.in("id", documentIds)});

        if (!ValidationHelper.isNullOrEmpty(documents)) {
            documentSubjects = DaoManager.load(DocumentSubject.class,
                    new Criterion[]{Restrictions.in("document",
                            documents.stream().collect(Collectors.toList()))});
            maxNumberOfDistinctLandCharesRegistry = documentSubjects
                    .stream()
                    .map(DocumentSubject::getOffice)
                    .collect(Collectors.groupingBy(IndexedEntity::getId, Collectors.counting())).values().stream().max(Long::compareTo);
        }

        if (maxNumberOfDistinctLandCharesRegistry.isPresent() && maxNumberOfDistinctLandCharesRegistry.get() > 0L) {
            result = maxNumberOfDistinctLandCharesRegistry.get();// + " ispezioni ipotecarie";
        }

        return result;
    }

    public String getRequestExtraCostDistinctTypes(Request request) throws PersistenceBeanException, IllegalAccessException {
        String result = "";
        List<String> resultList = new ArrayList<>();
        Map<String, ExtraCost> extraCostMap = new HashMap<>();

        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId())});

        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            for (ExtraCost cost : extraCosts) {
                if (!ValidationHelper.isNullOrEmpty(cost.getNote()) && !extraCostMap.containsKey(cost.getNote()))
                    extraCostMap.put(cost.getNote(), cost);
            }

            for (Map.Entry<String, ExtraCost> entry : extraCostMap.entrySet()) {
                if (ExtraCostType.NAZIONALEPOSITIVA.equals(entry.getValue().getType()))
                    continue;

                if (ExtraCostType.IPOTECARIO.equals(entry.getValue().getType())) {
                    if (MortgageType.AdditionalFormality.toString().equals(entry.getValue().getNote())) {
                        resultList.add(entry.getValue().getNote().toLowerCase());
                    } else {
                        resultList.add(getPrefixMortgage(entry.getValue()) + "ispezione ipotecaria");
                    }
                } else if (ExtraCostType.ALTRO.equals(entry.getValue().getType())) {
                    if(!ValidationHelper.isNullOrEmpty(entry.getValue().getNote()))
                        resultList.add(entry.getValue().getNote());
                    else
                        resultList.add(entry.getValue().getType().name().toLowerCase());
                }else if (entry.getValue().getType() != null) {
                    resultList.add(entry.getValue().getType().name().toLowerCase());
                }
            }
            result = String.join(" + ", resultList);
        }

        return result;
    }

    public String getRequestExtraCostDistinctTypes(Request request, Long maxNumberOfDistinctLandCharesRegistry) throws PersistenceBeanException, IllegalAccessException {
        String result = "";

        Map<String, List<ExtraCost>> extraCostMap = new HashMap<>();

        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId())});

        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            for (ExtraCost cost : extraCosts) {
                if (!ValidationHelper.isNullOrEmpty(cost.getNote()) && !extraCostMap.containsKey(cost.getNote())){
                    List<ExtraCost> extraCostList = new ArrayList<>();
                    extraCostList.add(cost);
                    extraCostMap.put(cost.getNote(), extraCostList);
                }else   if (!ValidationHelper.isNullOrEmpty(cost.getNote())){
                    List<ExtraCost> extraCostList = extraCostMap.get(cost.getNote());
                    extraCostList.add(cost);
                    extraCostMap.put(cost.getNote(), extraCostList);
                }
            }

            for (Map.Entry<String, List<ExtraCost>> entry : extraCostMap.entrySet()) {
                List<ExtraCost> extraCostList = entry.getValue()
                        .stream()
                        .filter(extraCost -> ExtraCostType.IPOTECARIO.equals(extraCost.getType()) &&
                                MortgageType.Sintetico.toString().equals(extraCost.getNote()))
                        .collect(Collectors.toList());

                String prefix = getPrefixCosts(extraCostList, maxNumberOfDistinctLandCharesRegistry);
                if(!ValidationHelper.isNullOrEmpty(prefix))
                    if(prefix.equalsIgnoreCase("doppia ") || prefix.equalsIgnoreCase("tripla ")){
                        result = prefix + "ispezione ipotecaria";
                    }else {
                        result = prefix + "ispezioni ipotecarie";
                    }


//                if (!ExtraCostType.IPOTECARIO.equals(extraCost.getType()))
//                    continue;
//                if (MortgageType.Sintetico.toString().equals(extraCost.getNote())) {
//                    String prefix = getPrefixCost(extraCost, maxNumberOfDistinctLandCharesRegistry);
//                    if(!ValidationHelper.isNullOrEmpty(prefix))
//                        if(prefix.equalsIgnoreCase("doppia ") || prefix.equalsIgnoreCase("tripla ")){
//                            result = prefix + "ispezione ipotecaria";
//                        }else {
//                            result = prefix + "ispezioni ipotecarie";
//                        }
//                }

            }
        }
        return result;
    }


    public Double getRequestExtraCostValue(Request request) throws PersistenceBeanException, IllegalAccessException {
        Double value = 0d;
        Map<String, ExtraCost> extraCostMap = new HashMap<>();

        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId())});

        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            for (ExtraCost cost : extraCosts) {
                if (!ValidationHelper.isNullOrEmpty(cost.getNote()) && !extraCostMap.containsKey(cost.getNote()))
                    extraCostMap.put(cost.getNote(), cost);
            }
            for (Map.Entry<String, ExtraCost> entry : extraCostMap.entrySet()) {
                if (ExtraCostType.NAZIONALEPOSITIVA.equals(entry.getValue().getType()))
                    continue;
                if (ExtraCostType.IPOTECARIO.equals(entry.getValue().getType())) {
                    if (MortgageType.AdditionalFormality.toString().equals(entry.getValue().getNote())) {
                        value += entry.getValue().getPrice() /3.6 ;
                    }
//                    else {
//                        value += entry.getValue().getPrice() /6.3 ;
//                    }
                }
            }
        }
        return value;
    }

    private String getAltroCostsNote(Request request) throws PersistenceBeanException, IllegalAccessException {
        String result = "";
        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId()),
                Restrictions.eq("type", ExtraCostType.ALTRO)});

        Double val = CollectionUtils.emptyIfNull(extraCosts)
                .stream()
                .mapToDouble(ec -> (ec.getPrice()))
                .sum();

        if(val > 0d){
            result += "Costo aggiuntivo: €" + val;
        }
        return result;
    }

    private String getPrefixCost(ExtraCost value, Long maxNumberOfDistinctLandCharesRegistry) {
        String result = "";
        Double val = 0.0;
        if (!ValidationHelper.isNullOrEmpty(value.getPrice())) {
            if(ExtraCostType.IPOTECARIO.equals(value.getType())){
                val = value.getPrice() / 6.30d + maxNumberOfDistinctLandCharesRegistry;
            }
            if (val == 1d) {
                result = "doppia ";
            } else  if (val == 2d) {
                result = "tripla ";
            } else  if (val == 3d) {
                result = "quattro ";
            } else  if (val == 4d) {
                result = "cinque ";
            } else  if (val == 5d) {
                result = "sei ";
            } else  if (val == 6d) {
                result = "sette ";
            }
        }
        return result;
    }

    private String getPrefixCosts(List<ExtraCost> values, Long maxNumberOfDistinctLandCharesRegistry) {
        String result = "";
        Double val = CollectionUtils.emptyIfNull(values)
                .stream()
                .mapToDouble(ec -> (ec.getPrice()/ 6.30d))
                .sum();

        val += maxNumberOfDistinctLandCharesRegistry;
        if (val == 1d) {
            result = "doppia ";
        } else  if (val == 2d) {
            result = "tripla ";
        } else  if (val == 3d) {
            result = "quattro ";
        } else  if (val == 4d) {
            result = "cinque ";
        } else  if (val == 5d) {
            result = "sei ";
        } else  if (val == 6d) {
            result = "sette ";
        }
        return result;
    }

    private String getPrefixMortgage(ExtraCost value) {
        String result = "";

        if (!ValidationHelper.isNullOrEmpty(value.getPrice())) {
            if (value.getPrice() / 6.30d == 2d) {
                result = "doppia ";
            } else if (value.getPrice() / 6.30d == 3d) {
                result = "tripla ";
            }
        }
        return result;
    }

    private void resizeRequestColumns() {
        autoSizeColumnsAndSetSizeToAnotherByDefault(COLUMNS_WITHOUT_COSTS);

        int colIndex = getIndex(ResourcesHelper.getString("nominative"), requestsColumns);
        if(colIndex>-1) {
            getSheet().setColumnWidth(colIndex, 7200);
        }

        colIndex = getIndex(ResourcesHelper.getString("codFiscIva"), requestsColumns);
        if(colIndex>-1) {
            getSheet().setColumnWidth(colIndex, 6000);
        }

        colIndex = getIndex(ResourcesHelper.getString("requestListCorservatoryName"), requestsColumns);
        if(colIndex>-1) {
            getSheet().setColumnWidth(colIndex, 6000);
        }

        colIndex = getIndex(ResourcesHelper.getString("excelNote"), requestsColumns);
        if(colIndex>-1) {
            getSheet().setColumnWidth(colIndex, 6000);
        }

        colIndex = getIndex(ResourcesHelper.getString("compensation"), requestsColumns);
        if(colIndex>-1) {
            getSheet().setColumnWidth(colIndex, 5000);
        }

        colIndex = getIndex(ResourcesHelper.getString("formalityTotal"), requestsColumns);
        if(colIndex>-1) {
            getSheet().setColumnWidth(colIndex, 3000);
        }

        colIndex = getIndex(ResourcesHelper.getString("requestPrintFormalityPresentationDate"), requestsColumns);
        if(colIndex>-1) {
            getSheet().setColumnWidth(colIndex, 4000);
        }
    }


    public static void setRequestsEvasionColumns(String[] requestsEvasionColumns) {
        CreateExcelRequestsReportHelper.requestsEvasionColumns = requestsEvasionColumns;
    }

    public static void setRequestsColumns(String[] requestsColumns) {
        CreateExcelRequestsReportHelper.requestsColumns = requestsColumns;
    }

    public boolean isItInvoiceReport() {
        return isItInvoiceReport;
    }

    public void setItInvoiceReport(boolean itInvoiceReport) {
        isItInvoiceReport = itInvoiceReport;
    }

    public static String[] getRequestsColumns() {
        return requestsColumns;
    }
}