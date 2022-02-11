package it.nexera.ris.common.helpers.create.xls;

import it.nexera.ris.common.enums.FormalityStateType;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class CreateExcelDocumentImportReportHelper extends CreateExcelReportHelper {

    private static String[] reportColumns = {
            ResourcesHelper.getString("excelDocumentImportReportFileName"),
            ResourcesHelper.getString("excelDocumentImportReportFormality"),
            ResourcesHelper.getString("excelDocumentImportReportPresentationDate"),
            ResourcesHelper.getString("excelDocumentImportReportGeneralRegister"),
            ResourcesHelper.getString("excelDocumentImportReportParticularRegister"),
            ResourcesHelper.getString("excelDocumentImportReportPropertyServiceName"),
            ResourcesHelper.getString("excelDocumentImportReportAcquisitionStatus")};

    public byte[] createReport(List<Document> structuredFiles, List<Document> duplicates, List<Document> otherFiles)
            throws IOException {
        createSheet(ResourcesHelper.getString("excelDocumentImportReportHeader").toUpperCase());
        setColumns(reportColumns);

        Row row = createRow();

        createHeader(row);

        HSSFCellStyle dateCellStyle = getWorkbook().createCellStyle();
        CreationHelper createHelper = getWorkbook().getCreationHelper();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

        addDocumentRecords(structuredFiles, dateCellStyle,
                x -> ResourcesHelper.getString("excelDocumentImportReportFileStructured"));
        addDocumentRecords(duplicates, dateCellStyle,
                x -> String.format(ResourcesHelper.getString("excelDocumentImportReportFileDuplicate"),
                        DateTimeHelper.toString(x.getFormalityDuplicatedDocument().getCreateDate())));
        addDocumentRecords(otherFiles, dateCellStyle,
                x -> !ValidationHelper.isNullOrEmpty(x.getFormalityDuplicated()) ?
                        ResourcesHelper.getString("excelDocumentImportReportFileOttico") :
                        ResourcesHelper.getString("excelDocumentImportReportNoFormality"));

        autoSizeColumnsAndSetSizeToAnotherByDefault(getColumns().length);
        optimizeSizeOfHeaderMergedCells(getSheet().getRow(0));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getWorkbook().write(baos);
        byte[] xls = baos.toByteArray();
        baos.close();

        getWorkbook().close();
        return xls;
    }

    private void addDocumentRecords(List<Document> documents, HSSFCellStyle dateCellStyle, Function<Document, String> docState) {
        for (Document document : documents) {
            Formality formality = null;
            if (!ValidationHelper.isNullOrEmpty(document.getFormality())) {
                formality = document.getFormality().get(document.getFormality().size() - 1);
            }
            Row row = createRowAfterDefinedEmptyRows(1);
            row.createCell(0).setCellValue(FileHelper.getFileName(document.getPath()));
            row.createCell(1).setCellValue(formality != null ? getFormalityType(formality) : "");
            Cell presentationDateCell = row.createCell(2);
            presentationDateCell.setCellValue(formality != null ? DateTimeHelper.toString(formality.getPresentationDate()) : "");
            presentationDateCell.setCellStyle(dateCellStyle);
            row.createCell(3).setCellValue(formality != null ? formality.getGeneralRegister() : "");
            row.createCell(4).setCellValue(formality != null ? formality.getParticularRegister() : "");
            row.createCell(5).setCellValue(formality != null ? formality.getConservatoryStr() : "");
            row.createCell(6).setCellValue(docState.apply(document));
        }
    }

    private String getFormalityType(Formality formality) {
        if (!ValidationHelper.isNullOrEmpty(formality.getSectionA())) {
            if (!ValidationHelper.isNullOrEmpty(formality.getSectionC())
                    || !ValidationHelper.isNullOrEmpty(formality.getSectionB())) {
                return ResourcesHelper.getString("excelDocumentImportReportFormalityTypeStructured");
            } else {
                return ResourcesHelper.getString("excelDocumentImportReportFormalityTypeOttico");
            }
        } else if (FormalityStateType.TITOLO.getId().equals(formality.getState())) {
            return ResourcesHelper.getString("excelDocumentImportReportFormalityTypeTitle");
        }
        return null;
    }

    private void createHeader(Row row) {
        HSSFFont headerFont = getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);

        HSSFCellStyle headerCellStyle = getWorkbook().createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.TAN.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.forInt(1));
        headerCellStyle.setFont(headerFont);

        for (int i = 0; i < getColumns().length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(headerCellStyle);
        }

        HSSFFont columnsHeaderFont = getWorkbook().createFont();
        columnsHeaderFont.setBold(true);
        columnsHeaderFont.setFontHeightInPoints((short) 10);

        HSSFCellStyle columnsHeaderCellStyle = getWorkbook().createCellStyle();
        columnsHeaderCellStyle.setFont(headerFont);
        columnsHeaderCellStyle.setFillForegroundColor(IndexedColors.TAN.getIndex());
        columnsHeaderCellStyle.setFillPattern(FillPatternType.forInt(1));
        columnsHeaderCellStyle.setFont(columnsHeaderFont);

        row = createRowAfterDefinedEmptyRows(2);
        addSeparator(row, columnsHeaderCellStyle);
    }

    private void optimizeSizeOfHeaderMergedCells(Row row) {
        int headerMergedCell = getColumns().length / 2;
        getSheet().addMergedRegion(new CellRangeAddress(0, 0, headerMergedCell - 1, headerMergedCell));
        row.getCell(headerMergedCell - 1).setCellValue(
                ResourcesHelper.getString("excelDocumentImportReportHeader").toUpperCase());
        getSheet().autoSizeColumn(headerMergedCell - 1, true);
        getSheet().autoSizeColumn(headerMergedCell, true);

        int headerMergedCellWidth = getSheet().getColumnWidth(headerMergedCell - 1) + getSheet().getColumnWidth(headerMergedCell);
        getSheet().setColumnWidth(headerMergedCell - 1, headerMergedCellWidth / 2);
        getSheet().setColumnWidth(headerMergedCell, headerMergedCellWidth / 2);
    }

}
