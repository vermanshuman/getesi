package it.nexera.ris.common.helpers.create.xls;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.InvoiceHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.web.beans.wrappers.GoodsServicesFieldWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class CreateExcelInvoicesReportHelper extends CreateExcelReportHelper {

    private static String[] invoicesColumns = {ResourcesHelper.getString("invoicesExcelDate"),
            ResourcesHelper.getString("invoicesExcelInvoiceNumber"),
            ResourcesHelper.getString("invoicesExcelName"),
            ResourcesHelper.getString("invoicesExcelTotalCost"),
            ResourcesHelper.getString("invoicesExcelNonTotalCost"),
            ResourcesHelper.getString("invoicesExcelVAT"),
            ResourcesHelper.getString("invoicesExcelTotal"),
            ResourcesHelper.getString("invoicesExcelPaymentOutcome"),
            ResourcesHelper.getString("invoicesExcelPaid"),
            ResourcesHelper.getString("invoicesExcelDeadline"),
            ResourcesHelper.getString("invoicesExcelSaldoAvere")};



    private static String[] invoiceClientColumns = {ResourcesHelper.getString("invoicesExcelClient"),
            ResourcesHelper.getString("invoicesExcelTotalCost"),
            ResourcesHelper.getString("invoicesExcelClientNonTotalCost"),
            ResourcesHelper.getString("invoicesExcelVAT"),
            ResourcesHelper.getString("invoicesExcelTotal"),
            ResourcesHelper.getString("invoicesExcelClientPaidAmount"),
            ResourcesHelper.getString("invoicesExcelClientUnusual"),"%"};

    private static String[] invoiceMonthColumns = {ResourcesHelper.getString("invoicesExcelMonth"),
            ResourcesHelper.getString("invoicesExcelTotalCost"),
            ResourcesHelper.getString("invoicesExcelClientNonTotalCost"),
            ResourcesHelper.getString("invoicesExcelVAT"),
            ResourcesHelper.getString("invoicesExcelTotal"),
            ResourcesHelper.getString("invoicesExcelClientPaidAmount"),
            ResourcesHelper.getString("invoicesExcelClientUnusual"),"%"};

    private void initInvoicesReport() {
        setColumns(invoicesColumns);
        createSheet("Elenco Emesse");
    }

    private void initInvoiceClientReport() {
        setColumns(invoiceClientColumns);
        createSheet("Emesse per cliente");
    }

    private void initInvoiceMonthReport() {
        setColumns(invoiceMonthColumns);
        createSheet("Emesse per mese");
    }

    private static String[] billingInvoicesColumns = {ResourcesHelper.getString("invoicesExcelDate"),
            ResourcesHelper.getString("invoicesExcelInvoiceNumber"),
            ResourcesHelper.getString("invoicesExcelName"),
            ResourcesHelper.getString("totalSkills"),
            "",ResourcesHelper.getString("totalVat")};

    private void initBillingInvoicesReport() {
        setColumns(billingInvoicesColumns);
        createSheet("InvoicesReport");
    }


    public byte[] createInvoiceExcel(List<Invoice> invoices) throws IOException, PersistenceBeanException, IllegalAccessException, InstantiationException {

        initInvoicesReport();

        Row row = createRow();

        Font headerFont = getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 9);
        headerFont.setColor(IndexedColors.GREEN.getIndex());

        CellStyle borderCellStyle = getWorkbook().createCellStyle();
        borderCellStyle.setBorderBottom(BorderStyle.THIN);
        borderCellStyle.setBorderLeft(BorderStyle.THIN);
        borderCellStyle.setBorderRight(BorderStyle.THIN);
        borderCellStyle.setBorderTop(BorderStyle.THIN);

        CellStyle headerCellStyle = getWorkbook().createCellStyle();
        headerCellStyle.cloneStyleFrom(borderCellStyle);
        headerCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setFillPattern(FillPatternType.forInt(1));
        headerCellStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerCellStyle.setFont(headerFont);

        CellStyle headerGreyCellStyle = getWorkbook().createCellStyle();
        headerGreyCellStyle.cloneStyleFrom(headerCellStyle);
        headerGreyCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());

        CellStyle redCellStyle = getWorkbook().createCellStyle();
        redCellStyle.cloneStyleFrom(borderCellStyle);
        redCellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        redCellStyle.setFillPattern(FillPatternType.forInt(1));

        CellStyle centerTextStyle = getWorkbook().createCellStyle();
        centerTextStyle.cloneStyleFrom(borderCellStyle);
        centerTextStyle.setAlignment(HorizontalAlignment.CENTER);

        CreationHelper createHelper = getWorkbook().getCreationHelper();
        CellStyle numberCellStyle = getWorkbook().createCellStyle();
        numberCellStyle.cloneStyleFrom(borderCellStyle);
        numberCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));

        HSSFCellStyle dateCellStyle = getWorkbook().createCellStyle();
        dateCellStyle.cloneStyleFrom(borderCellStyle);
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

        CellStyle percentageCellStyle = getWorkbook().createCellStyle();
        percentageCellStyle.cloneStyleFrom(borderCellStyle);
        percentageCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("0.00%"));

        addSeparator(row, headerCellStyle);

        row.getCell(5).setCellStyle(headerGreyCellStyle);
        row.getCell(6).setCellStyle(headerGreyCellStyle);
        row.getCell(10).setCellStyle(headerGreyCellStyle);

        createFreezePane(0, 1, 0, 1);
        InvoiceHelper invoiceHelper = new InvoiceHelper();
        invoices.sort(Comparator.comparing(Invoice::getNumber,
                Comparator.nullsFirst(Comparator.naturalOrder())));
        int count = invoices.size() + 1;
        int index = 2;
        Map<Long, Double> taxMapping = new HashMap<>();
        Map<Long, Double> ivaMapping = new HashMap<>();
        Map<Long, Double> amountMapping = new HashMap<>();
        for (Invoice invoice : invoices) {
            row = createRowAfterDefinedEmptyRows(1);
            row.createCell(0).setCellValue((invoice.getCreateDate() != null) ?
                    DateTimeHelper.toFormatedString(invoice.getCreateDate(),
                            DateTimeHelper.getDatePattern()) : "");
            row.getCell(0).setCellStyle(dateCellStyle);
            if(invoice.getNumber() != null){
                row.createCell(1).setCellValue(invoice.getNumber());
                row.getCell(1).setCellStyle(centerTextStyle);
            }
            if(!ValidationHelper.isNullOrEmpty(invoice.getClient())
                    && !ValidationHelper.isNullOrEmpty(invoice.getClient().getClientName())){
                row.createCell(2).setCellValue(invoice.getClient().getClientName().toUpperCase());
                row.getCell(2).setCellStyle(borderCellStyle);
            }

            List<GoodsServicesFieldWrapper>  wrapperList = goodsServicesFields(invoice, invoiceHelper);
            Double value = invoiceHelper.getAllTotalLine(wrapperList);
            taxMapping.put(invoice.getId(), value);
            row.createCell(3).setCellValue(value);
//                            .replace(".",","));
            row.getCell(3).setCellStyle(numberCellStyle);
            row.createCell(4).setCellStyle(borderCellStyle);
            value = invoiceHelper.getTotalVat(wrapperList);
            ivaMapping.put(invoice.getId(), value);
            row.createCell(5).setCellValue(value);//.replace(".",","));
            row.getCell(5).setCellStyle(numberCellStyle);
            Double grossAmount = invoiceHelper.getTotalGrossAmount(wrapperList);
            row.createCell(6).setCellValue(grossAmount);//.replace(".",","));
            row.getCell(6).setCellStyle(numberCellStyle);
            row.createCell(7).setCellStyle(borderCellStyle);
            Double totalPayments = invoiceHelper.getTotalPayment(invoice);
            amountMapping.put(invoice.getId(), totalPayments);
            row.createCell(8).setCellValue(totalPayments);//.replace(".",","));
            row.getCell(8).setCellStyle(numberCellStyle);
            row.createCell(9).setCellStyle(borderCellStyle);
            row.createCell(10).setCellValue(grossAmount - totalPayments);//.replace(".",","));
            row.getCell(10).setCellStyle(numberCellStyle);
            index++;
        }

        for (int i = 0; i < getColumns().length; i++) {
            if(i == 2){
                getSheet().setColumnWidth(i, 256 * 44);
            }else if(i == 5){
                getSheet().setColumnWidth(i, 256 * 9);
            }else if(i == 6){
                getSheet().setColumnWidth(i, 256 * 11);
            }else if(i == 7){
                getSheet().setColumnWidth(i, 256 * 26);
            }else
                getSheet().autoSizeColumn(i);

        }
        initInvoiceClientReport();

        Map<Client, List<Invoice>> groupedInvoicesByClient = invoices.stream()
                .filter(i -> !ValidationHelper.isNullOrEmpty(i.getClient()))
                .collect(Collectors.groupingBy(Invoice::getClient));

        row = createRow();
        addSeparator(row, headerCellStyle);

        for (int i = 0; i < getColumns().length; i++) {
            if(i == 0){
                getSheet().setColumnWidth(i, 256 * 46);
            }else if(i == 1){
                getSheet().setColumnWidth(i, 256 * 16);
            }else if(i == 2){
                getSheet().setColumnWidth(i, 256 * 15);
            }else {
                getSheet().setColumnWidth(i, 256 * 16);
            }
        }

        index = 2;

        for (Map.Entry<Client, List<Invoice>> entry : groupedInvoicesByClient.entrySet()) {
            row = createRowAfterDefinedEmptyRows(1);
            String clientName = "";
            if(!ValidationHelper.isNullOrEmpty(entry.getKey())
                    && !ValidationHelper.isNullOrEmpty(entry.getKey().getClientName())){
                clientName = entry.getKey().getClientName().toUpperCase();
                row.createCell(0).setCellValue(clientName);
                row.getCell(0).setCellStyle(borderCellStyle);
            }
            if(StringUtils.isNotBlank(clientName)){
                row.createCell(1).setCellFormula("SUMIF('Elenco Emesse'!$C$2:$C$" + count + ",$A" + index + ",'Elenco Emesse'!$D$2:$D$" + count + ")");
                row.getCell(1).setCellStyle(numberCellStyle);
                row.createCell(2).setCellFormula("SUMIF('Elenco Emesse'!$C$2:$C$" + count + ",$A" + index + ",'Elenco Emesse'!$E$2:$E$" + count + ")");
                row.getCell(2).setCellStyle(numberCellStyle);
                row.createCell(3).setCellFormula("SUMIF('Elenco Emesse'!$C$2:$C$" + count + ",$A" + index + ",'Elenco Emesse'!$F$2:$F$" + count + ")");
                row.getCell(3).setCellStyle(numberCellStyle);
                row.createCell(4).setCellFormula("B" + index + "+ C" + index + "+ D" + index);
                row.getCell(4).setCellStyle(numberCellStyle);
                row.createCell(5).setCellFormula("SUMIF('Elenco Emesse'!$C$2:$C$" + count + ",$A" + index + ",'Elenco Emesse'!$I$2:$I$" + count + ")");
                row.getCell(5).setCellStyle(numberCellStyle);
                row.createCell(6).setCellFormula("E" + index + "-F" + index);
                row.getCell(6).setCellStyle(numberCellStyle);
                row.createCell(7).setCellFormula("IF(E" + index + ">0,G" + index + "/E" + index + ",0)");
                row.getCell(7).setCellStyle(percentageCellStyle);
            }


            index++;
        }

        initInvoiceMonthReport();
        Map<Integer, List<Invoice>> groupedInvoicesByMonth = invoices.stream()
                .filter(i -> !ValidationHelper.isNullOrEmpty(i.getDate()))
                .collect(Collectors.groupingBy(i -> DateTimeHelper.getMonth(i.getDate())));

        row = createRow();
        addSeparator(row, headerCellStyle);

        for (int i = 0; i < getColumns().length; i++) {
            getSheet().setColumnWidth(i, 256 * 14);
        }
        index = 3;
        createRowAfterDefinedEmptyRows(1);
        for (Map.Entry<Integer, List<Invoice>> entry : groupedInvoicesByMonth.entrySet()) {
            row = createRowAfterDefinedEmptyRows(1);
            row.createCell(0).setCellValue(DateTimeHelper.getMonth(entry.getKey()));
            row.getCell(0).setCellStyle(borderCellStyle);
            double totalTax = entry.getValue()
                    .stream()
                    .filter(i -> taxMapping.containsKey(i.getId()))
                    .mapToDouble(o->taxMapping.get(o.getId())).sum();
            row.createCell(1).setCellValue(totalTax);
            row.getCell(1).setCellStyle(numberCellStyle);
            row.createCell(2).setCellStyle(borderCellStyle);
            double totalIva = entry.getValue()
                    .stream()
                    .filter(i -> ivaMapping.containsKey(i.getId()))
                    .mapToDouble(o->ivaMapping.get(o.getId())).sum();
            row.createCell(3).setCellValue(totalIva);
            row.getCell(3).setCellStyle(numberCellStyle);
            row.createCell(4).setCellFormula("B" + index + "+ C" + index + "+ D" + index);
            row.getCell(4).setCellStyle(numberCellStyle);
            double totalAmount = entry.getValue()
                    .stream()
                    .filter(i -> amountMapping.containsKey(i.getId()))
                    .mapToDouble(o->amountMapping.get(o.getId())).sum();
            row.createCell(5).setCellValue(totalAmount);
            row.getCell(5).setCellStyle(numberCellStyle);
            row.createCell(6).setCellFormula("E" + index + "-F" + index);
            row.getCell(6).setCellStyle(numberCellStyle);
            row.createCell(7).setCellFormula("IF(E" + index + ">0,G" + index + "/E" + index + ",0)");
            row.getCell(7).setCellStyle(percentageCellStyle);
            index++;
        }

        HSSFPalette palette = getWorkbook().getCustomPalette();
        HSSFColor blueColor = palette.findSimilarColor(0, 0, 255);
        Font totalFont = getWorkbook().createFont();
        totalFont.setBold(true);
        totalFont.setFontHeightInPoints((short) 9);
        totalFont.setColor(blueColor.getIndex());

        CellStyle totalCellStyle = getWorkbook().createCellStyle();
        totalCellStyle.cloneStyleFrom(borderCellStyle);
        totalCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        totalCellStyle.setFillPattern(FillPatternType.forInt(1));
        totalCellStyle.setFont(totalFont);

        Font valueFont = getWorkbook().createFont();
        valueFont.setColor(blueColor.getIndex());
        valueFont.setBold(true);

        CellStyle totalValueCellStyle = getWorkbook().createCellStyle();
        totalValueCellStyle.cloneStyleFrom(numberCellStyle);
        totalValueCellStyle.setFont(valueFont);

        CellStyle percentageValueStyle = getWorkbook().createCellStyle();
        percentageValueStyle.cloneStyleFrom(borderCellStyle);
        percentageValueStyle.setDataFormat(createHelper.createDataFormat().getFormat("0.00%"));
        percentageValueStyle.setFont(valueFont);

        row = createRowAfterDefinedEmptyRows(2);
        row.createCell(0).setCellValue(ResourcesHelper.getString("invoicesExcelTotal"));
        row.getCell(0).setCellStyle(totalCellStyle);
        row.createCell(1).setCellFormula("SUM(B3:B" + (index-1) + ")");
        row.getCell(1).setCellStyle(totalValueCellStyle);
        row.createCell(2).setCellFormula("SUM(C3:C" + (index-1) + ")");
        row.getCell(2).setCellStyle(totalValueCellStyle);
        row.createCell(3).setCellFormula("SUM(D3:D" + (index-1) + ")");
        row.getCell(3).setCellStyle(totalValueCellStyle);
        row.createCell(4).setCellFormula("SUM(E3:E" + (index-1) + ")");
        row.getCell(4).setCellStyle(totalValueCellStyle);
        row.createCell(5).setCellFormula("SUM(F3:F" + (index-1) + ")");
        row.getCell(5).setCellStyle(totalValueCellStyle);
        row.createCell(6).setCellFormula("SUM(G3:G" + (index-1) + ")");
        row.getCell(6).setCellStyle(totalValueCellStyle);
        row.createCell(7).setCellFormula("IF(E" + (row.getRowNum() + 1) + ">0,G" + (row.getRowNum() + 1) + "/E" + (row.getRowNum() + 1 ) + ",0)");
        row.getCell(7).setCellStyle(percentageValueStyle);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getWorkbook().write(baos);
        byte[] xls = baos.toByteArray();

        baos.close();

        getWorkbook().close();
        return xls;
    }


    private  List<GoodsServicesFieldWrapper> goodsServicesFields(Invoice invoice, InvoiceHelper invoiceHelper) throws PersistenceBeanException, IllegalAccessException {
        List<GoodsServicesFieldWrapper> wrapperList = new ArrayList<>();
        int counter = 1;
        List<InvoiceItem> invoiceItems = DaoManager.load(InvoiceItem.class,
                new Criterion[]{Restrictions.eq("invoice", invoice)});
        for (InvoiceItem invoiceItem : invoiceItems) {
            GoodsServicesFieldWrapper wrapper = invoiceHelper.createGoodsServicesFieldWrapper();
            wrapper.setCounter(counter);
//            if(invoiceItem.getId() == null){
//                wrapper.setInvoiceItemId(invoiceItem.getId());
//            }else {
//                invoiceItem.setUuid(UUID.randomUUID().toString());
//                wrapper.setInvoiceItemUUID(invoiceItem.getUuid());
//            }
            wrapper.setInvoiceTotalCost(invoiceItem.getInvoiceTotalCost());
            wrapper.setSelectedTaxRateId(invoiceItem.getTaxRate().getId());
            wrapper.setInvoiceItemAmount(ValidationHelper.isNullOrEmpty(invoiceItem.getAmount()) ? 0.0 : invoiceItem.getAmount());
            double totalcost = !(ValidationHelper.isNullOrEmpty(invoiceItem.getInvoiceTotalCost())) ? invoiceItem.getInvoiceTotalCost().doubleValue() : 0.0;
            double amount = !(ValidationHelper.isNullOrEmpty(invoiceItem.getAmount())) ? invoiceItem.getAmount().doubleValue() : 0.0;
            double totalLine;
            if(amount != 0.0) {
                totalLine = totalcost * amount;
            } else {
                totalLine = totalcost;
            }
            wrapper.setTotalLine(totalLine);
            if(!ValidationHelper.isNullOrEmpty(invoiceItem.getDescription()))
                wrapper.setDescription(invoiceItem.getDescription());
            wrapperList.add(wrapper);
            counter = counter + 1;
        }
        return wrapperList;
    }
    public byte[] convertInvoicesToExcel(List<Document> documents) throws IOException {
        initInvoicesReport();

        Row row = createRow();

        Font headerFont = getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 9);
        headerFont.setColor(IndexedColors.GREEN.getIndex());

        CellStyle borderCellStyle = getWorkbook().createCellStyle();
        borderCellStyle.setBorderBottom(BorderStyle.THIN);
        borderCellStyle.setBorderLeft(BorderStyle.THIN);
        borderCellStyle.setBorderRight(BorderStyle.THIN);
        borderCellStyle.setBorderTop(BorderStyle.THIN);

        CellStyle headerCellStyle = getWorkbook().createCellStyle();
        headerCellStyle.cloneStyleFrom(borderCellStyle);
        headerCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setFillPattern(FillPatternType.forInt(1));
        headerCellStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerCellStyle.setFont(headerFont);

        CellStyle headerGreyCellStyle = getWorkbook().createCellStyle();
        headerGreyCellStyle.cloneStyleFrom(headerCellStyle);
        headerGreyCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());

        CellStyle redCellStyle = getWorkbook().createCellStyle();
        redCellStyle.cloneStyleFrom(borderCellStyle);
        redCellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        redCellStyle.setFillPattern(FillPatternType.forInt(1));

        CellStyle alignBorderCellStyle = getWorkbook().createCellStyle();
        alignBorderCellStyle.cloneStyleFrom(borderCellStyle);
        alignBorderCellStyle.setAlignment(HorizontalAlignment.CENTER);

        addSeparator(row, headerCellStyle);

        row.getCell(5).setCellStyle(headerGreyCellStyle);
        row.getCell(6).setCellStyle(headerGreyCellStyle);
        row.getCell(10).setCellStyle(headerGreyCellStyle);

        createFreezePane(0, 1, 0, 1);

        for (Document document : documents) {
            row = createRowAfterDefinedEmptyRows(1);
            row.createCell(0).setCellValue((document.getMail() != null && document.getMail().getCreateDate() != null) ?
                    DateTimeHelper.toStringDateWithDots(document.getMail().getCreateDate()) : "");
            row.createCell(1).setCellValue(document.getInvoiceNumber() == null ? 0 : document.getInvoiceNumber());
            row.createCell(2).setCellValue("");
            row.createCell(3).setCellValue(document.getCost() == null ? "" : document.getCost());
            row.createCell(4).setCellValue("");
            row.createCell(5).setCellValue("");
            row.createCell(6).setCellValue("");
            row.createCell(7).setCellValue("");
            row.createCell(8).setCellValue("");
            row.createCell(9).setCellValue("");
            row.createCell(10).setCellValue("");
            row.createCell(15).setCellValue(0);
            row.createCell(16).setCellValue(0);

            row.getCell(0).setCellStyle(borderCellStyle);
            row.getCell(1).setCellStyle(alignBorderCellStyle);
            row.getCell(2).setCellStyle(borderCellStyle);
            row.getCell(3).setCellStyle(borderCellStyle);
            row.getCell(4).setCellStyle(borderCellStyle);
            row.getCell(5).setCellStyle(borderCellStyle);
            row.getCell(6).setCellStyle(borderCellStyle);
            row.getCell(7).setCellStyle(borderCellStyle);
            row.getCell(8).setCellStyle(borderCellStyle);
            row.getCell(9).setCellStyle(borderCellStyle);
            row.getCell(10).setCellStyle(borderCellStyle);
            row.getCell(15).setCellStyle(redCellStyle);
            row.getCell(16).setCellStyle(redCellStyle);
        }

        autoSizeColumnsAndSetSizeToAnotherByDefault(getColumns().length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getWorkbook().write(baos);
        byte[] xls = baos.toByteArray();

        baos.close();

        getWorkbook().close();
        return xls;
    }
}
