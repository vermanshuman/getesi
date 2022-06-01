package it.nexera.ris.common.helpers.create.xls;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.InvoiceHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.Invoice;
import it.nexera.ris.persistence.beans.entities.domain.InvoiceItem;
import it.nexera.ris.web.beans.wrappers.GoodsServicesFieldWrapper;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    private void initInvoicesReport() {
        setColumns(invoicesColumns);
        createSheet("InvoicesReport");
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

        CellStyle alignBorderCellStyle = getWorkbook().createCellStyle();
        alignBorderCellStyle.cloneStyleFrom(borderCellStyle);
        alignBorderCellStyle.setAlignment(HorizontalAlignment.CENTER);

        addSeparator(row, headerCellStyle);

        row.getCell(5).setCellStyle(headerGreyCellStyle);
        row.getCell(6).setCellStyle(headerGreyCellStyle);
        row.getCell(10).setCellStyle(headerGreyCellStyle);

        createFreezePane(0, 1, 0, 1);
        InvoiceHelper invoiceHelper = new InvoiceHelper();
        for (Invoice invoice : invoices) {
            row = createRowAfterDefinedEmptyRows(1);
            row.createCell(0).setCellValue((invoice.getCreateDate() != null) ?
                    DateTimeHelper.toFormatedString(invoice.getCreateDate(),
                            DateTimeHelper.getDatePattern()) : "");
            row.createCell(1).setCellValue(invoice.getNumber());
            if(!ValidationHelper.isNullOrEmpty(invoice.getClient())
                    && !ValidationHelper.isNullOrEmpty(invoice.getClient().getClientName())){
                row.createCell(2).setCellValue(invoice.getClient().getClientName());
            }

            List<GoodsServicesFieldWrapper>  wrapperList = goodsServicesFields(invoice, invoiceHelper);
            row.createCell(3).setCellValue(
                    String.valueOf(invoiceHelper.getAllTotalLine(wrapperList)).replace(".",","));
            row.createCell(5).setCellValue(
                    String.valueOf(invoiceHelper.getTotalVat(wrapperList)).replace(".",","));
            Double grossAmount = invoiceHelper.getTotalGrossAmount(wrapperList);
            row.createCell(6).setCellValue(String.valueOf(grossAmount).replace(".",","));
            Double totalPayments = invoiceHelper.getTotalPayment(invoice);
            row.createCell(8).setCellValue(String.valueOf(totalPayments).replace(".",","));
            row.createCell(10).setCellValue(String.valueOf(grossAmount - totalPayments).replace(".",","));
        }
        autoSizeColumnsAndSetSizeToAnotherByDefault(getColumns().length);

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
