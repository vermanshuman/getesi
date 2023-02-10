package it.nexera.ris.common.helpers.create.xlsx;

import it.nexera.ris.common.enums.RealEstateType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.create.pdf.CreateReportHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.web.beans.wrappers.logic.RelationshipGroupingWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.*;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

public class CreateExcelXReportHelper extends CreateReportHelper {

    private String[] columns;

    private XSSFWorkbook workbook;

    private XSSFSheet sheet;

    private Boolean showTaxAssessmentString;

    public CreateExcelXReportHelper() {
        workbook = new XSSFWorkbook();
    }

    private static String[] evasionAttachmentCColumns = {
            ResourcesHelper.getString("city").toUpperCase(),
            ResourcesHelper.getString("requestExtraCostLandRegistry").toUpperCase(),
            ResourcesHelper.getString("estateSezionePrefix").toUpperCase(),
            ResourcesHelper.getString("estateStreetGraftPrefix").toUpperCase(),
            ResourcesHelper.getString("requestParticleShort").toUpperCase(),
            ResourcesHelper.getString("excelRequestSub").toUpperCase(),
            ResourcesHelper.getString("excelRequestCat").toUpperCase(),
            ResourcesHelper.getString("excelRequestConsistencyPrefix").toUpperCase(),
            ResourcesHelper.getString("excelRequestRevenue").toUpperCase(),
            ResourcesHelper.getString("excelRequestAgriculturalIncome").toUpperCase(),
            ResourcesHelper.getString("excelRequestDominicalIncome").toUpperCase(),
            ResourcesHelper.getString("attachmentCCulture").toUpperCase(),
            ResourcesHelper.getString("attachmentCExtension").toUpperCase(),
            ResourcesHelper.getString("databaseListRealEstateQuote").toUpperCase(),
            ResourcesHelper.getString("rights").toUpperCase(),
            ResourcesHelper.getString("excelRequestOMIValue").toUpperCase(),
            ResourcesHelper.getString("databaseListRealEstateCommercialValue").toUpperCase(),
            ResourcesHelper.getString("textEditTableGravami").toUpperCase(),
            ResourcesHelper.getString("gravamiDetail").toUpperCase()
    };

    public byte[] createEvasionAttachmentCExcel(Request request)
            throws IOException, IllegalAccessException, PersistenceBeanException, InstantiationException, ParseException {

        initEvasionAttachmentCReport();

        boolean propertyExist = Boolean.FALSE;
        XSSFFont boldFonts = getWorkbook().createFont();
        boldFonts.setBold(true);
        boldFonts.setFontHeightInPoints((short) 14);
        StringBuilder sb = new StringBuilder();
        getWorkbook().getCreationHelper();
        IndexedColorMap colorMap = getWorkbook().getStylesSource().getIndexedColors();
        XSSFColor lightBlue = new XSSFColor(new java.awt.Color(221, 235, 247), colorMap);
        XSSFCellStyle firstHeaderStyle = getWorkbook().createCellStyle();
        firstHeaderStyle.setFillForegroundColor(lightBlue);
        firstHeaderStyle.setFillPattern(FillPatternType.forInt(1));
        firstHeaderStyle.setBorderBottom(BorderStyle.MEDIUM);
        firstHeaderStyle.setBorderTop(BorderStyle.MEDIUM);
        firstHeaderStyle.setFont(boldFonts);
        Row row = createRowAfterDefinedEmptyRows(3);
        Cell cell = row.createCell(0);
        sb.append(ResourcesHelper.getString("requestEditSubjectListName"));
        sb.append(": ");
        if (!ValidationHelper.isNullOrEmpty(request) &&
                !ValidationHelper.isNullOrEmpty(request.getSubject()))
            sb.append(request.getSubject().getFullName());
        cell.setCellValue(sb.toString());
        cell.setCellStyle(firstHeaderStyle);
        CellRangeAddress cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum(),
                0, 3);
        getSheet().addMergedRegion(cellRangeAddress);

        RegionUtil.setBorderTop(BorderStyle.MEDIUM, cellRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.MEDIUM, cellRangeAddress, sheet);

        cell = row.createCell(4);
        cell.setCellStyle(firstHeaderStyle);
        cell = row.createCell(5);
        cell.setCellStyle(firstHeaderStyle);
        cell = row.createCell(6);
        cell.setCellStyle(firstHeaderStyle);
        cell = row.createCell(7);
        cell.setCellStyle(firstHeaderStyle);
        cell = row.createCell(11);
        cell.setCellStyle(firstHeaderStyle);
        cell = row.createCell(12);
        cell.setCellStyle(firstHeaderStyle);
        cell = row.createCell(13);
        cell.setCellStyle(firstHeaderStyle);
        cell = row.createCell(14);
        cell.setCellStyle(firstHeaderStyle);
        cell = row.createCell(15);
        cell.setCellStyle(firstHeaderStyle);
        sb.setLength(0);

        sb.append(ResourcesHelper.getString("requestPrintCorservatoryName"));
        sb.append(": ");
        if (!ValidationHelper.isNullOrEmpty(request) &&
                !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry()))
            sb.append(request.getAggregationLandChargesRegistry().getName());
        cell = row.createCell(8);
        cell.setCellValue(sb.toString());
        cell.setCellStyle(firstHeaderStyle);

        cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 8, 10);
        getSheet().addMergedRegion(cellRangeAddress);

        RegionUtil.setBorderTop(BorderStyle.MEDIUM, cellRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.MEDIUM, cellRangeAddress, sheet);

        cell = row.createCell(13);
        sb.setLength(0);
        sb.append(ResourcesHelper.getString("excelUpdateDate"));
        sb.append(": ");
        sb.append(TemplatePdfTableHelper.getEstateFormalityConservationDate(request));
        cell.setCellValue(sb.toString());
        cell.setCellStyle(firstHeaderStyle);

        row = createRowAfterDefinedEmptyRows(5);

        XSSFFont headerFont = getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);

        XSSFCellStyle headerCellStyle = getWorkbook().createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.forInt(1));
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.MEDIUM);
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        XSSFCellStyle separatorCellStyle = getWorkbook().createCellStyle();
        separatorCellStyle.setBorderBottom(BorderStyle.THIN);
        separatorCellStyle.setBorderTop(BorderStyle.MEDIUM);
        separatorCellStyle.setBorderRight(BorderStyle.THIN);
        separatorCellStyle.setBorderLeft(BorderStyle.THIN);
        XSSFColor lightOrange = new XSSFColor(new java.awt.Color(252, 228, 214), colorMap);
        headerCellStyle.setFillForegroundColor(lightOrange);
        headerCellStyle.setFillPattern(FillPatternType.forInt(1));
        createHeader(row, headerCellStyle);
        row = createRow();
        addSeparator(row, separatorCellStyle);
        Font fontSuperscript = getWorkbook().createFont();
        fontSuperscript.setTypeOffset(Font.SS_SUPER);

        setShowTaxAssessmentString(Boolean.FALSE);
        int maxWidth = 0;
        if (!ValidationHelper.isNullOrEmpty(request)) {
            List<EstateSituation> estateSituations = request.getSituationEstateLocations();

            if (!ValidationHelper.isNullOrEmpty(estateSituations)) {
                XSSFCellStyle euroStyle = getWorkbook().createCellStyle(); //getEuroStyle();
                euroStyle.setBorderBottom(BorderStyle.THIN);
                euroStyle.setBorderTop(BorderStyle.THIN);
                euroStyle.setBorderRight(BorderStyle.THIN);
                euroStyle.setBorderLeft(BorderStyle.THIN);
                euroStyle.setAlignment(HorizontalAlignment.CENTER);
                XSSFFont rowFont = getWorkbook().createFont();
                rowFont.setFontHeightInPoints((short) 11);
                XSSFCellStyle rowCellStyle = getWorkbook().createCellStyle();
                rowCellStyle.setBorderBottom(BorderStyle.THIN);
                rowCellStyle.setBorderTop(BorderStyle.THIN);
                rowCellStyle.setBorderRight(BorderStyle.THIN);
                rowCellStyle.setBorderLeft(BorderStyle.THIN);
                rowCellStyle.setAlignment(HorizontalAlignment.CENTER);
                rowCellStyle.setFont(rowFont);
                euroStyle.setFont(rowFont);

                XSSFCellStyle wrapStyle = getWorkbook().createCellStyle();
                wrapStyle.setBorderBottom(BorderStyle.THIN);
                wrapStyle.setBorderTop(BorderStyle.THIN);
                wrapStyle.setBorderRight(BorderStyle.THIN);
                wrapStyle.setBorderLeft(BorderStyle.THIN);
                wrapStyle.setFont(rowFont);
                wrapStyle.setWrapText(true);

                NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
                DecimalFormat df = new DecimalFormat(
                        "#,##0.00",
                        new DecimalFormatSymbols(new Locale("pt", "BR")));

                for (EstateSituation estateSituation : estateSituations) {
                    List<Property> buildingProperties = estateSituation.getPropertyList()
                            .stream()
                            .filter(p -> !ValidationHelper.isNullOrEmpty(p.getType()) &&
                                    RealEstateType.BUILDING.getId().equals(p.getType()))
                            .collect(Collectors.toList());

                    if(!ValidationHelper.isNullOrEmpty(buildingProperties))
                        propertyExist = Boolean.TRUE;
                    List<Formality> formalityList = estateSituation.getFormalityList()
                            .stream().filter(f -> !ValidationHelper.isNullOrEmpty(f.getDicTypeFormality())
                                    && !ValidationHelper.isNullOrEmpty(f.getDicTypeFormality().getPrejudicial())
                                    && f.getDicTypeFormality().getPrejudicial())
                            .collect(Collectors.toList());


                    List<Formality> filteredFormalityList = formalityList
                            .stream().distinct()
                            .collect(Collectors.toList());

                    buildingProperties.sort(Comparator.comparing(Property::getSortedSheets)
                            .thenComparing(Property::getSortedParticles)
                            .thenComparing(Property::getSortedSubs));
                    int currentWidth = createPropertyData(buildingProperties, rowCellStyle, euroStyle, wrapStyle,
                            request, nf, filteredFormalityList, sb, df, fontSuperscript);
                    if(maxWidth < currentWidth)
                        maxWidth = currentWidth;
                }

                for (EstateSituation estateSituation : estateSituations) {
                    List<Property> landProperties = estateSituation.getPropertyList()
                            .stream()
                            .filter(p -> !ValidationHelper.isNullOrEmpty(p.getType()) &&
                                    RealEstateType.LAND.getId().equals(p.getType()))
                            .collect(Collectors.toList());

                    List<Formality> formalityList = estateSituation.getFormalityList()
                            .stream().filter(f -> !ValidationHelper.isNullOrEmpty(f.getDicTypeFormality())
                                    && !ValidationHelper.isNullOrEmpty(f.getDicTypeFormality().getPrejudicial())
                                    && f.getDicTypeFormality().getPrejudicial())
                            .collect(Collectors.toList());

                    if(!ValidationHelper.isNullOrEmpty(landProperties))
                        propertyExist = Boolean.TRUE;

                    List<Formality> filteredFormalityList = formalityList
                            .stream().distinct()
                            .collect(Collectors.toList());

                    landProperties.sort(Comparator.comparing(Property::getSortedSheets)
                            .thenComparing(Property::getSortedParticles)
                            .thenComparing(Property::getSortedSubs));
                    int currentWidth = createPropertyData(landProperties, rowCellStyle, euroStyle, wrapStyle, request,
                            nf, filteredFormalityList, sb, df, fontSuperscript);
                    if(maxWidth < currentWidth)
                        maxWidth = currentWidth;

                }
            }
        }
        row = createRowAfterDefinedEmptyRows(3);
        cell = row.createCell(0);
        sb.setLength(0);
        sb.append("*");

        if(getShowTaxAssessmentString() != null && getShowTaxAssessmentString()){
            String data = ResourcesHelper.getString("attachmentCTaxAssessmentText");
            RichTextString richString = new XSSFRichTextString(data);
            richString.applyFont(0,1, fontSuperscript);
            cell.setCellValue(richString);
            cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum(),
                    0, 3);
            getSheet().addMergedRegion(cellRangeAddress);
        }

        if(maxWidth > 0)
            sheet.setColumnWidth(18, calculateWidth(Double.valueOf(maxWidth) + 0.3 * maxWidth));
        else
            sheet.setColumnWidth(18, calculateWidth(18.67));


        for (int i = 0; i < columns.length; ++i) {
            if(i != 18)
                getSheet().autoSizeColumn(i);
        }

        if(propertyExist){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            getWorkbook().write(baos);
            byte[] xls = baos.toByteArray();
            baos.close();
            getWorkbook().close();
            return xls;
        }
        return null;
    }

    private int createPropertyData(List<Property> propertyList, XSSFCellStyle rowCellStyle, XSSFCellStyle euroStyle, XSSFCellStyle wrapStyle,
                                    Request request, NumberFormat nf,  List<Formality> filteredFormalityList,
                                   StringBuilder sb, DecimalFormat df,  Font fontSuperscript)
            throws PersistenceBeanException, IllegalAccessException {
        int maxWidth = 0;
        for (Property property : emptyIfNull(propertyList)){
            int colIndex = 0;
            Row row = createRow();
            Cell dateCell = row.createCell(colIndex++);
            StringBuilder cityData = new StringBuilder();
            cityData.append(property.getCityDescription());
            if(StringUtils.isNotBlank(property.getSectionCity())){
                if(cityData.length() > 0)
                    cityData.append("\n");
                cityData.append("(Sez. ");
                cityData.append(property.getSectionCity());
                cityData.append(")");
            }

            dateCell.setCellValue(cityData.toString());
            dateCell.setCellStyle(wrapStyle);

            dateCell = row.createCell(colIndex++);
            if(!ValidationHelper.isNullOrEmpty(property.getType())
                    && property.getType().equals(RealEstateType.LAND.getId())){
                dateCell.setCellValue(
                        ResourcesHelper.getString("realEstateLand"));
            }else if(!ValidationHelper.isNullOrEmpty(property.getType())
                    && property.getType().equals(RealEstateType.BUILDING.getId())){
                dateCell.setCellValue(ResourcesHelper.getString("realEstateBuild"));
            }
            dateCell.setCellStyle(rowCellStyle);


            String section = "";
            String sheet = "";
            String particle = "";
            String sub = "";
            int index = 0;
            for(CadastralData cadastralData : property.getCadastralData()) {
                if(index > 0){
                    section += "\n";
                    sheet += "\n";
                    particle += "\n";
                    sub += "\n";
                }
                if (!ValidationHelper.isNullOrEmpty(cadastralData.getSection()))
                    section += cadastralData.getSection();
                if(StringUtils.isNotBlank(cadastralData.getSheet()))
                    sheet += cadastralData.getSheet();
                if(StringUtils.isNotBlank(cadastralData.getParticle()))
                    particle += cadastralData.getParticle();
                if(StringUtils.isNotBlank(cadastralData.getSub()))
                    sub += cadastralData.getSub();
                index++;
            }

            dateCell = row.createCell(colIndex++);
            dateCell.setCellValue(section);
            dateCell.setCellStyle(wrapStyle);

            dateCell = row.createCell(colIndex++);
            dateCell.setCellValue(sheet);
            dateCell.setCellStyle(wrapStyle);

            dateCell = row.createCell(colIndex++);
            dateCell.setCellValue(particle);
            dateCell.setCellStyle(wrapStyle);

            dateCell = row.createCell(colIndex++);
            dateCell.setCellValue(sub);
            dateCell.setCellStyle(wrapStyle);

            dateCell = row.createCell(colIndex++);
            if(!ValidationHelper.isNullOrEmpty(property.getCategory())
                    && RealEstateType.BUILDING.getId().equals(property.getType()))
                dateCell.setCellValue(property.getCategory().getCodeInVisura());
            dateCell.setCellStyle(rowCellStyle);

            dateCell = row.createCell(colIndex++);
            if(RealEstateType.BUILDING.getId().equals(property.getType())){

                if(!ValidationHelper.isNullOrEmpty(property.getCadastralArea()) && property.getCadastralArea() > 0){
                    String cadastralArea =  property.getCadastralArea().toString();
                    if(cadastralArea.endsWith(".00") || cadastralArea.endsWith(".0"))
                        cadastralArea = cadastralArea.substring(0, cadastralArea.lastIndexOf("."));
                    if(StringUtils.isNotBlank(cadastralArea) && !cadastralArea.trim().equalsIgnoreCase("0")){
                        dateCell.setCellValue(cadastralArea + " MQ");
                    }
                } else if(!ValidationHelper.isNullOrEmpty(property.getConsistency()) &&
                        !property.getConsistency().trim().equalsIgnoreCase("0 mq") ) {
                    dateCell.setCellValue(property.getConsistency().toUpperCase());
                }
            }
            dateCell.setCellStyle(rowCellStyle);

            dateCell = row.createCell(colIndex++);
            if(StringUtils.isNotBlank(property.getRevenue())
                    && (!property.getRevenue().trim().equals("0") ||
                    !property.getRevenue().trim().startsWith("0.0"))){
                dateCell.setCellValue(property.getRevenue() + " €");
            }
            dateCell.setCellStyle(rowCellStyle);
            dateCell = row.createCell(colIndex++);
            if(StringUtils.isNotBlank(property.getAgriculturalIncome()) &&
                    !property.getAgriculturalIncome().trim().equals("0") &&
                    !property.getAgriculturalIncome().trim().startsWith("0.0")){
                dateCell.setCellValue(InvoiceHelper.format(Double.parseDouble(property.getAgriculturalIncome())) + " €");
            }
            dateCell.setCellStyle(rowCellStyle);

            dateCell = row.createCell(colIndex++);
            if(StringUtils.isNotBlank(property.getCadastralIncome()) &&
                    !property.getCadastralIncome().trim().equals("0") &&
                            !property.getCadastralIncome().trim().startsWith("0.0")){
                dateCell.setCellValue(InvoiceHelper.format(Double.parseDouble(property.getCadastralIncome())) + " €");
            }
            dateCell.setCellStyle(rowCellStyle);

            dateCell = row.createCell(colIndex++);
            String landCultureName = "";
            List<LandCadastralCulture> landCadastralCultures = null;
            if (!ValidationHelper.isNullOrEmpty(property.getQuality())) {
                landCadastralCultures = DaoManager.load(LandCadastralCulture.class,
                        new Criterion[]{Restrictions.eq("description", property.getQuality()).ignoreCase()
                        });
                if (!ValidationHelper.isNullOrEmpty(landCadastralCultures)) {
                    LandCulture landCulture = landCadastralCultures.get(0).getLandCulture();
                    if (!ValidationHelper.isNullOrEmpty(landCulture)
                            && (ValidationHelper.isNullOrEmpty(landCulture.getUnavailable())
                            || !landCulture.getUnavailable())) {
                        landCultureName = landCulture.getName();
                    }
                }
            }
            dateCell.setCellValue(landCultureName);
            dateCell.setCellStyle(rowCellStyle);

            dateCell = row.createCell(colIndex++);
            if(RealEstateType.LAND.getId().equals(property.getType())){
                dateCell.setCellValue(property.getConsistency());
            }
            dateCell.setCellStyle(rowCellStyle);

            String quota = "";
            String diritti ="";
            property.setCurrentRequest(request);
            if (!ValidationHelper.isNullOrEmpty(property.getRelations())){
                List<RelationshipGroupingWrapper> pairs = new LinkedList<>();
                for (Relationship relationship : property.getRelations()) {
                    if (relationship.getPropertyType() != null) {
                        List<EstateSituation> estateSituationList = relationship.getProperty().getEstateSituationList();

                        Boolean showRegime = null;
                        if(request != null){
                            Optional<EstateSituation> estateSituationR = CollectionUtils.emptyIfNull(estateSituationList)
                                    .stream()
                                    .filter(es -> !ValidationHelper.isNullOrEmpty(es.getRegime()) && es.getRegime())
                                    .findFirst();
                            if(estateSituationR.isPresent())
                                showRegime = true;

                            if(showRegime == null){
                                estateSituationR = CollectionUtils.emptyIfNull(estateSituationList)
                                        .stream()
                                        .filter(es -> !ValidationHelper.isNullOrEmpty(es.getRegime()) && !es.getRegime())
                                        .findFirst();
                                if(estateSituationR.isPresent())
                                    showRegime = false;
                            }


                            if(showRegime == null){
                                if(request.getRegime() != null)
                                    showRegime = request.getRegime();
                            }

                            if(showRegime == null){
                                if(request.getClient() != null && request.getClient().getRegime() != null)
                                    showRegime = request.getClient().getRegime();
                            }
                        }
                        RelationshipGroupingWrapper relationshipGroupingWrapper = new RelationshipGroupingWrapper(
                                relationship.getQuote() == null ? "" : relationship.getQuote(),
                                relationship.getPropertyType().toUpperCase(),
                                showRegime == null || !showRegime ? "" : relationship.getRegime(),
                                relationship.getProperty().getCity(), relationship.getProperty().getSectionCity());
                        if (pairs.stream().noneMatch(p -> p.equals(relationshipGroupingWrapper))) {
                            pairs.add(relationshipGroupingWrapper);
                        }
                    }
                }

                List<String> relationshipQuoteData = pairs.stream()
                        .filter(r -> StringUtils.isNotBlank(r.getQuote()))
                        .map(RelationshipGroupingWrapper::getQuote).collect(Collectors.toList());
                quota = String.join("\n ", relationshipQuoteData);
                List<String> relationshipTypeData = pairs.stream()
                        .map(RelationshipGroupingWrapper::getPropertyType).collect(Collectors.toList());
                diritti = String.join("\n ", relationshipTypeData);
            }
            dateCell = row.createCell(colIndex++);
            dateCell.setCellValue(quota);
            dateCell.setCellStyle(rowCellStyle);

            dateCell = row.createCell(colIndex++);
            dateCell.setCellValue(diritti.toUpperCase());
            dateCell.setCellStyle(rowCellStyle);

            int cindex = colIndex++;
            String estimateOMIRequestText = "";
            String estimateLastCommercialValueRequestText = PropertyEntityHelper.getLastEstimateLastCommercialValueRequestText(property)
                    .replaceAll("\\." , "")
                    .replaceAll("," , ".");;
            if(!ValidationHelper.isNullOrEmpty(property.getType())
                    && property.getType().equals(RealEstateType.LAND.getId())){
                if(!ValidationHelper.isNullOrEmpty(landCadastralCultures)){
                    List<LandCulture> landCultures = emptyIfNull(landCadastralCultures)
                            .stream()
                            .filter(lcc -> !ValidationHelper.isNullOrEmpty(lcc.getLandCulture()))
                            .map(LandCadastralCulture::getLandCulture)
                            .collect(Collectors.toList());
                    if (!ValidationHelper.isNullOrEmpty(landCultures)) {
                        List<LandOmiValue> landOmiValues = DaoManager.load(LandOmiValue.class,
                                new Criterion[]{Restrictions.in("landCulture", landCultures)
                                });
                        if (!ValidationHelper.isNullOrEmpty(landOmiValues)) {
                                List<LandOmiValue> cityLandOmiValues = landOmiValues
                                    .stream()
                                    .filter(lov -> !ValidationHelper.isNullOrEmpty(lov.getLandOmi())
                                            && !ValidationHelper.isNullOrEmpty(lov.getLandOmi().getCities())
                                            && lov.getLandOmi().getCities().contains(property.getCity()))
                                    .collect(Collectors.toList());
                            if (!ValidationHelper.isNullOrEmpty(cityLandOmiValues)
                                    && !ValidationHelper.isNullOrEmpty(property.getTagLandMQ())) {
                                String landMQ= property.getTagLandMQ();
                                if(landMQ.endsWith(".00") || landMQ.endsWith(".0"))
                                    landMQ = landMQ.substring(0, landMQ.lastIndexOf("."));
                                if(landMQ.contains(".")){
                                    String [] toks = landMQ.split("\\.");
                                    if(toks.length > 1 && toks[1].length() == 3){
                                        landMQ = landMQ.replaceAll("\\.", "");
                                    }else if(toks.length > 1 && toks[1].length() == 2){
                                        landMQ = landMQ.replaceAll("\\.", "");
                                        landMQ = landMQ + "0";
                                    }else if(toks.length > 1 && toks[1].length() == 1){
                                        landMQ = landMQ.replaceAll("\\.", "");
                                        landMQ = landMQ + "00";
                                    }
                                }
                                Double landMqValue =  Double.parseDouble(landMQ);
                                Double omiValue = (cityLandOmiValues.get(0).getValue()/10000) * landMqValue;
                                BigDecimal value = new BigDecimal(omiValue);
                                estimateOMIRequestText = df.format(value.doubleValue())
                                        .replaceAll("\\." , "")
                                        .replaceAll("," , ".");
                            }
                        }
                    }
                }
            }else {
                estimateOMIRequestText = PropertyEntityHelper.getLastEstimateOMIRequestText(property)
                                .replaceAll("\\." , "")
                        .replaceAll("," , ".");
            }

            if(StringUtils.isNotBlank(estimateOMIRequestText.trim())){
                row.createCell(cindex, CellType.NUMERIC);
                row.getCell(cindex).setCellValue(InvoiceHelper.format(Double.parseDouble(estimateOMIRequestText)) + " €");
                row.getCell(cindex).setCellStyle(euroStyle);
            }else{
                row.createCell(cindex);
                row.getCell(cindex).setCellStyle(rowCellStyle);
            }

            cindex = colIndex++;
            String estateIndicativeFiscalValue = "";
            try {
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getClient())
                        && !ValidationHelper.isNullOrEmpty(request.getClient().getFiscalValue()) &&
                        request.getClient().getFiscalValue()){
                    if(StringUtils.isNotBlank(property.getRevenue()) && !ValidationHelper.isNullOrEmpty(property.getCategory())){
                        estateIndicativeFiscalValue = PropertyEntityHelper.getFiscalValue(property);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
                log.error(log, e);
            }
            if(StringUtils.isNotBlank(estimateLastCommercialValueRequestText.trim())
                    || StringUtils.isNotBlank(estateIndicativeFiscalValue)){
                if(StringUtils.isNotBlank(estimateLastCommercialValueRequestText.trim())){
                    estimateLastCommercialValueRequestText = InvoiceHelper.format(Double.parseDouble(estimateLastCommercialValueRequestText));
                }
                row.createCell(cindex);
                if(StringUtils.isNotBlank(estateIndicativeFiscalValue)){
                    setShowTaxAssessmentString(Boolean.TRUE);
                    StringBuffer attachmentBuffer = new StringBuffer();
                    if(StringUtils.isNotBlank(estimateLastCommercialValueRequestText)){
                        attachmentBuffer.append(estimateLastCommercialValueRequestText);
                        attachmentBuffer.append(" €");
                        attachmentBuffer.append("\n");
                    }
                    attachmentBuffer.append(estateIndicativeFiscalValue);
                    attachmentBuffer.append(" €");
                    attachmentBuffer.append("*");
                    String data = attachmentBuffer.toString();
                    RichTextString richString = new XSSFRichTextString(data);
                    richString.applyFont(data.length()-1, data.length(), fontSuperscript);
                    row.getCell(cindex).setCellValue(richString);
                    XSSFCellStyle centeredWrapStyle = getWorkbook().createCellStyle();
                    centeredWrapStyle.setBorderBottom(BorderStyle.THIN);
                    centeredWrapStyle.setBorderTop(BorderStyle.THIN);
                    centeredWrapStyle.setBorderRight(BorderStyle.THIN);
                    centeredWrapStyle.setBorderLeft(BorderStyle.THIN);
                    centeredWrapStyle.setAlignment(HorizontalAlignment.CENTER);
                    centeredWrapStyle.setFont(wrapStyle.getFont());
                    centeredWrapStyle.setWrapText(true);

                    row.getCell(cindex).setCellStyle(centeredWrapStyle);
                }else if(StringUtils.isNotBlank(estimateLastCommercialValueRequestText.trim())){
                    row.getCell(cindex).setCellValue(estimateLastCommercialValueRequestText + " €");
                    row.getCell(cindex).setCellStyle(rowCellStyle);
                }
            }else{
                row.createCell(cindex);
                row.getCell(cindex).setCellStyle(rowCellStyle);
            }
            dateCell = row.createCell(colIndex++);

            if(filteredFormalityList.size() > 0)
                dateCell.setCellValue("SI");
            dateCell.setCellStyle(rowCellStyle);

            dateCell = row.createCell(colIndex++);
            if(!ValidationHelper.isNullOrEmpty(filteredFormalityList)){
                filteredFormalityList.sort(Comparator.comparing(Formality::getComparedDate)
                        .thenComparing(Formality::getGeneralRegister)
                        .thenComparing(Formality::getParticularRegister));
                sb.setLength(0);
                for(int f = 0; f < filteredFormalityList.size(); f++){
                    StringBuilder fb = new StringBuilder();
                    fb.append("- ");
                    fb.append(filteredFormalityList.get(f).getAttachmentCFormalityData());
                    if(f < (filteredFormalityList.size()-1)){
                        fb.append("\n");
                    }
                    if(maxWidth < fb.length())
                        maxWidth = fb.length();
                    sb.append(fb);
                }
                dateCell.setCellValue(sb.toString().replaceAll("<b>", "").replaceAll("</b>", ""));
            }
            dateCell.setCellStyle(wrapStyle);
        }

        return maxWidth;
    }
    protected XSSFCellStyle getEuroStyle() {
        XSSFCellStyle euroStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        euroStyle.setDataFormat(format.getFormat(
                new String("_-* #,##0.00 \"€\"_-;-* #,##0.00 \"€\"_-;_-* \"-\"?? \"€\"_-;_-@_-".getBytes(),
                        Charset.defaultCharset())));
        return euroStyle;
    }

    protected Row createRow() {
        return getSheet().createRow(getSheet().getLastRowNum() + 1);
    }

    private void createHeader(Row row, XSSFCellStyle headerCellStyle) {

        for (int i = 0; i < getColumns().length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }
        sheet.setColumnWidth(0, calculateWidth(10.89));
        sheet.setColumnWidth(1, calculateWidth(13.22));
        sheet.setColumnWidth(2, calculateWidth(6.33));
        sheet.setColumnWidth(3, calculateWidth(9.56));
        sheet.setColumnWidth(4, calculateWidth(8.33));
        sheet.setColumnWidth(5, calculateWidth(8.33));
        sheet.setColumnWidth(6, calculateWidth(8.89));
        sheet.setColumnWidth(7, calculateWidth(11.22));
        sheet.setColumnWidth(8, calculateWidth(14.56));
        sheet.setColumnWidth(9, calculateWidth(21.78));
        sheet.setColumnWidth(10, calculateWidth(21.78));
        sheet.setColumnWidth(11, calculateWidth(13.56));
        sheet.setColumnWidth(12, calculateWidth(14.56));
        sheet.setColumnWidth(13, calculateWidth(9.22));
        sheet.setColumnWidth(14, calculateWidth(15.11));
        sheet.setColumnWidth(15, calculateWidth(19.67));
        sheet.setColumnWidth(16, calculateWidth(21.78));
        sheet.setColumnWidth(17, calculateWidth(13.67));
    }

    protected void addSeparator(Row row, CellStyle cellStyle) {
        for (int i = 0; i < columns.length; ++i) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
        }
    }

    private Integer calculateWidth(Double width) {
    	Double totalWidth = width * 256 + 200;
    	//maximum allowed values for XSSFSheet column width is 255*256 = 65280
    	if(totalWidth.intValue() > 65280)
    		return 65280;
        return totalWidth.intValue();
    }

    private void initEvasionAttachmentCReport() {
        setColumns(evasionAttachmentCColumns);
        createSheet("Foglio1");
    }

    protected String[] getColumns() {
        return columns;
    }

    protected void setColumns(String[] columns) {
        this.columns = columns;
    }

    protected void createSheet(String sheetName) {
        this.sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
        //this.sheet = workbook.createSheet(sheetName);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setLandscape(true);
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);
        ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
        sheet.setFitToPage(true);
        sheet.setAutobreaks(true);

        ps.setFooterMargin(0.25);
        sheet.setMargin(HSSFSheet.TopMargin, 0.10);
        sheet.setMargin(HSSFSheet.BottomMargin, 0.10);
        sheet.setMargin(HSSFSheet.LeftMargin, 0.10);
        sheet.setMargin(HSSFSheet.RightMargin, 0.10);
    }

    protected XSSFWorkbook getWorkbook() {
        return workbook;
    }

    protected Sheet getSheet() {
        return this.sheet;
    }

    protected Row createRowAfterDefinedEmptyRows(int countOfEmptyRows) {
        return getSheet().createRow(getSheet().getLastRowNum() + countOfEmptyRows);
    }

    public Boolean getShowTaxAssessmentString() {
        return showTaxAssessmentString;
    }

    public void setShowTaxAssessmentString(Boolean showTaxAssessmentString) {
        this.showTaxAssessmentString = showTaxAssessmentString;
    }
}
