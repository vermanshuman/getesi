package it.nexera.ris.common.helpers.omi;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.create.xls.XlsxHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.CategoryItemGroupOmi;
import it.nexera.ris.persistence.beans.entities.domain.CategoryPercentValue;
import it.nexera.ris.persistence.beans.entities.domain.ItemGroupOmi;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.OmiValue;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import lombok.Data;

/**
 * @author Dmitrii
 */
public final class OMIHelper {

    protected static final Log log = LogFactory.getLog(OMIHelper.class);

    private static Collection<CategoryCodeForOmi> CATEGORY_CODE_FOR_OMI_COLLECTION;

    private static final String CATEGORIA_COLUMN_IN_CATEGORY_XLSX = "Categoria";
    private static final String CODICE_COLUMN_IN_CATEGORY_XSLX = "Codice";

    static {
        initCategoryCodes();
    }

    public synchronized static void initCategoryCodes() {
        CATEGORY_CODE_FOR_OMI_COLLECTION = new ArrayList<>();

        String omiCategoryPath = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.OMI_CATEGORY_FILE).getValue();
        if (ValidationHelper.isNullOrEmpty(omiCategoryPath)) {
            return;
        }

        try {
            Sheet sheet = XlsxHelper.readSheet(omiCategoryPath);
            Iterator<Row> rowIterator = sheet.rowIterator();

            Row row = rowIterator.next();

            int cellWithPropertyCode = XlsxHelper.findColumnIndexByName(row, CATEGORIA_COLUMN_IN_CATEGORY_XLSX);
            int cellWithResultCode = XlsxHelper.findColumnIndexByName(row, CODICE_COLUMN_IN_CATEGORY_XSLX);

            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                Cell cell = row.getCell(cellWithPropertyCode);
                if (cell != null) {
                    String[] categories = cell.getStringCellValue().split("-");
                    for (String category : categories) {
                        CategoryCodeForOmi categoryCodeForOmi = new CategoryCodeForOmi();
                        categoryCodeForOmi.setCategory(category.trim());
                        categoryCodeForOmi.setCode(row.getCell(cellWithResultCode).getStringCellValue());
                        CATEGORY_CODE_FOR_OMI_COLLECTION.add(categoryCodeForOmi);
                    }
                }
            }
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
    }

    /**
     * Calculate OMI by the property`s data.
     * <p>
     * It’s better to check all conditions before searching for a zone in a KML file
     * to reduce the number of Internet searches
     *
     * @param property property
     * @return Optional with calculated OMI
     */
    public static CalculatedOmi calculateOMI(Property property, boolean savePropertyIfZoneWasChanged) throws Exception {
        CalculatedOmi calculatedOmi = new CalculatedOmi();

        /*
       String code = getCode(property.getCategoryCode());
        if (ValidationHelper.isNullOrEmpty(code)) {
            return calculatedOmi;
        }
        */

        List<Long> codes = getCodes(property.getCategoryCode());
        if (ValidationHelper.isNullOrEmpty(codes)) {
            return calculatedOmi;
        }


        String zones = property.getZone();
        
        if (ValidationHelper.isNullOrEmpty(zones)) {
            List<Pair<Double, Double>> coordinates = new ArrayList<Pair<Double,Double>>();
            if(property != null){
                coordinates = GeolocationHelper.checkCoordinates(property.getCityDescription() + " " + property.getAddress());
            }
            

            if(coordinates == null || (coordinates != null && coordinates.size() < 2)) {
                zones = String.join("-", findZoneByPropertyInKML(property));
                calculatedOmi.setMultipleCoordinates(false);
            }else {
                log.info("Multiple coordinates found");
                zones = findZoneByCoordinates(property, coordinates).stream().distinct().collect(Collectors.joining("-"));
                calculatedOmi.setMultipleCoordinates(true);
                //return calculatedOmi;
            }
            
            property.setZone(zones);
            if (savePropertyIfZoneWasChanged) {
                DaoManager.save(property, true);
            }
        }
        if (ValidationHelper.isNullOrEmpty(zones)) {
            return calculatedOmi;
        }

        String[] zonesArr = zones.split("-");
        calculatedOmi.setSeveralZones(zonesArr.length > 1);
        List<Double> calculatedOmiValues = new ArrayList<>();
        for (String zone : zonesArr) {
            List<OmiValue> omiValues = getOmiValues(property.getCity().getCfis(), zone, codes);
            
            if(ValidationHelper.isNullOrEmpty(omiValues) &&
                    !ValidationHelper.isNullOrEmpty(property.getCategory())) {
                try {
                    calculatedOmi.setCategoryCodeMissing(true);
                    List<CategoryItemGroupOmi> categoryItemGroupOmis = DaoManager.load(
                            CategoryItemGroupOmi.class,
                            new Criterion[]{Restrictions.eq("category", property.getCategory())});

                    if(!ValidationHelper.isNullOrEmpty(categoryItemGroupOmis)) {
                        CategoryItemGroupOmi categoryItemGroupOmi = categoryItemGroupOmis.get(0);
                        ItemGroupOmi itemGroupOmi = categoryItemGroupOmi.getItemGroupOmi();
                        int position = categoryItemGroupOmi.getItemGroupOmi().getPosition();
                        boolean isPrevious = true;
                        boolean isNext = true;
                        int currentPosition = 1;
                        int matchedPosition = -1;
                        int previousPosition = position - currentPosition;
                        int nextPosition = position + currentPosition;
                        while(previousPosition >= -1) {
                            List<CategoryItemGroupOmi> previousCategoryItemGroupOmis = DaoManager.load(
                                    CategoryItemGroupOmi.class, 
                                    new CriteriaAlias[]{
                                            new CriteriaAlias("itemGroupOmi", "itemGroupOmi", JoinType.INNER_JOIN)
                                    },
                                    new Criterion[]{
                                            Restrictions.and(Restrictions.eq("itemGroupOmi.position", previousPosition),
                                                    Restrictions.eq("itemGroupOmi.groupOmi", itemGroupOmi.getGroupOmi()))
                                    });
                            if(ValidationHelper.isNullOrEmpty(previousCategoryItemGroupOmis)) {
                                isPrevious = false;
                            }else {
                                CategoryItemGroupOmi previousCategoryItemGroupOmi = previousCategoryItemGroupOmis.get(0);
                                String preCode = getCode(previousCategoryItemGroupOmi.getCategory().getCode());
                                if(!ValidationHelper.isNullOrEmpty(preCode)) 
                                    omiValues = getOmiValues(property.getCity().getCfis(), zone, preCode);
                            }
                            if(!ValidationHelper.isNullOrEmpty(omiValues)) {
                                matchedPosition = previousPosition;
                                break;
                            }
                            List<CategoryItemGroupOmi> nextCategoryItemGroupOmis = DaoManager.load(
                                    CategoryItemGroupOmi.class,
                                    new CriteriaAlias[]{
                                            new CriteriaAlias("itemGroupOmi", "itemGroupOmi", JoinType.INNER_JOIN)
                                    },
                                    new Criterion[]{
                                            Restrictions.and(Restrictions.eq("itemGroupOmi.position", nextPosition),
                                                    Restrictions.eq("itemGroupOmi.groupOmi", itemGroupOmi.getGroupOmi()))
                                    });
                            if(ValidationHelper.isNullOrEmpty(nextCategoryItemGroupOmis)) {
                                isNext = false;
                            }else {
                                CategoryItemGroupOmi nextCategoryItemGroupOmi = nextCategoryItemGroupOmis.get(0);
                                String nextCode = getCode(nextCategoryItemGroupOmi.getCategory().getCode());
                                if(!ValidationHelper.isNullOrEmpty(nextCode))
                                    omiValues = getOmiValues(property.getCity().getCfis(), zone, nextCode);
                            }

                            if(!ValidationHelper.isNullOrEmpty(omiValues)) {
                                matchedPosition = nextPosition;
                                break;
                            }

                            if(isPrevious && isNext)
                                break;

                            currentPosition++;
                            previousPosition = position - currentPosition;
                            nextPosition = position + currentPosition;
                        }

                        LogHelper.debugInfo(log, "Matched position " + matchedPosition + ". For poisition : " + position);
                        if(matchedPosition > 0) {
                            int difference = matchedPosition - position;
                            final double percentage = itemGroupOmi.getGroupOmi().getStepValue() * difference;
                            omiValues.forEach(omi ->  {
                                Double comprMax = omi.getComprMax() + ((omi.getComprMax() * percentage)/100);
                                Double comprMin = omi.getComprMin() + ((omi.getComprMin() * percentage)/100);
                                omi.setMaxValue(comprMax);
                                omi.setMinValue(comprMin);
                                omi.setComprMax(comprMax.longValue());
                                omi.setComprMin(comprMin.longValue());
                            });
                        }

                    }
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }

            }else {
                calculatedOmi.setCategoryCodeMissing(false);
                omiValues.forEach(omi ->  {
                    if(!ValidationHelper.isNullOrEmpty(omi.getComprMax()))
                        omi.setMaxValue(omi.getComprMax().doubleValue());
                    
                    if(!ValidationHelper.isNullOrEmpty(omi.getComprMin()))
                        omi.setMinValue(omi.getComprMin().doubleValue());
                });
            }
            
            calculatedOmi.setSeveralComprs(omiValues.size() > 1);
            double x = omiValues.stream().mapToDouble(omi -> (omi.getMaxValue() != null ? omi.getMaxValue() : 0.0 )+ (omi.getMinValue() != null ? omi.getMinValue() : 0.0)).average().orElse(0d);
            
            if (x != 0) {
                double y = 0;
                List<CategoryPercentValue> categoryPercentValues = DaoManager.load(CategoryPercentValue.class, new Criterion[]{
                        Restrictions.eq("cadastralCategory", property.getCategory())
                });
                if (ValidationHelper.isNullOrEmpty(categoryPercentValues) || categoryPercentValues.get(0).getPercentOmi() == null) {
                    y = x / 2;
                } else if (categoryPercentValues.get(0).getPercentOmi() != null) {
                    y = x * (((double) categoryPercentValues.get(0).getPercentOmi()) / 100);
                }

                if (!ValidationHelper.isNullOrEmpty(property.getCadastralArea()) && property.getCadastralArea() > 0) {
                    calculatedOmiValues.add(y * property.getCadastralArea());
                } else if (!ValidationHelper.isNullOrEmpty(property.getConsistency())) {
                    String consistency = property.getConsistency().toLowerCase().replaceAll(",", ".");
                    if (consistency.contains("mq")) {
                        double mq = Double.parseDouble(consistency.replaceAll("mq", ""));
                        calculatedOmiValues.add(y * mq);
                    } else if (consistency.contains("vani")) {
                        double vani = Double.parseDouble(consistency.replaceAll("vani", ""));
                        calculatedOmiValues.add(y * vani * 20);
                    } else if (consistency.contains("metri quadri")) {
                        double vani = Double.parseDouble(consistency.replaceAll("metri quadri", ""));
                        calculatedOmiValues.add(y * vani * 20);
                    } else if (consistency.contains("metri quadrati")) {
                        double vani = Double.parseDouble(consistency.replaceAll("metri quadrati", ""));
                        calculatedOmiValues.add(y * vani * 20);
                    }
                }
            }
        }

        calculatedOmi.setValue(calculatedOmiValues.stream().filter(x -> !x.equals(0d)).mapToDouble(x -> x).average().orElse(0d));
        return calculatedOmi;
    }

    private static List<OmiValue> getOmiValues(String propertyCityCfis, String zone, String code)
            throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.load(OmiValue.class, new Criterion[]{
                Restrictions.eq("zone", zone),
                Restrictions.eq("cityCfis", propertyCityCfis),
                Restrictions.eq("categoryCode", Long.parseLong(code))
        });
    }

    private static List<OmiValue> getOmiValues(String propertyCityCfis, String zone, List<Long> codes)
            throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.load(OmiValue.class, new Criterion[]{
                Restrictions.eq("zone", zone),
                Restrictions.eq("cityCfis", propertyCityCfis),
                Restrictions.in("categoryCode", codes)
        });
    }

    public static double calculateCommercialOmi(Property property, double calcOmi)
            throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(property) && !ValidationHelper.isNullOrEmpty(property.getCategory())) {
            List<CategoryPercentValue> categoryPercentValues = DaoManager.load(CategoryPercentValue.class, new Criterion[]{
                    Restrictions.eq("cadastralCategory", property.getCategory())
            });
            if (!ValidationHelper.isNullOrEmpty(categoryPercentValues)) {
                CategoryPercentValue categoryPercentValue = categoryPercentValues.get(0);
                if (!ValidationHelper.isNullOrEmpty(categoryPercentValue.getPercentCommercial())) {
                    BigDecimal result = new BigDecimal(
                            calcOmi * (((double) categoryPercentValue.getPercentCommercial()) / 100) + calcOmi);
                    if (result.compareTo(BigDecimal.valueOf(20000D)) >= 0) {
                        return round(result, BigDecimal.valueOf(5000)).doubleValue();
                    } else {
                        return round(result, BigDecimal.valueOf(1000)).doubleValue();
                    }
                }
            }
        }
        return 0;
    }

    private static BigDecimal round(BigDecimal value, BigDecimal increment) {
        if (increment.signum() == 0) {
            return value;
        } else {
            BigDecimal divided = value.divide(increment, 0, RoundingMode.UP);
            return divided.multiply(increment);
        }
    }

    public static String getCode(String propertyCategoryCode) {
        
        Optional<CategoryCodeForOmi> categoryCodeForOmi = CATEGORY_CODE_FOR_OMI_COLLECTION.stream()
                .filter(c -> c.getCategory().equals(propertyCategoryCode)).findFirst();
        return categoryCodeForOmi.map(CategoryCodeForOmi::getCode).orElse(null);
    }

    public static List<Long> getCodes(String propertyCategoryCode) {

        return CATEGORY_CODE_FOR_OMI_COLLECTION.stream()
                .filter(c -> c.getCategory().equals(propertyCategoryCode))
                .map(CategoryCodeForOmi::getCode)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    public static List<String> findZoneByPropertyInKML(Property property) throws Exception {
        String path = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.OMI_KML_FILES).getValue();
        if (!ValidationHelper.isNullOrEmpty(path) && property != null) {
            File[] kmlFilesInFolder = new File(path).listFiles();
            if (kmlFilesInFolder == null) {
                return new ArrayList<>();
            }
            File kmlFileToLook = null;
            for (final File fileEntry : kmlFilesInFolder) {
                if (!fileEntry.isDirectory()
                        && fileEntry.getName().substring(fileEntry.getName().lastIndexOf('.') + 1).equals("kml")
                        && fileEntry.getName().contains(property.getCity().getCfis())) {
                    kmlFileToLook = fileEntry;
                    break;
                }
            }
            if (kmlFileToLook == null) {
                return new ArrayList<>();
            }
            return GeolocationHelper.findZoneByAddress(
                    property.getCityDescription() + " " + property.getAddress(),
                    FileHelper.loadContentByPath(kmlFileToLook.getPath()));
        }
        return new ArrayList<>();
    }
    
    public static List<String> findZoneByPropertyInKML(Property property,List<Pair<Double, Double>> coordinates) throws Exception {
        String path = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.OMI_KML_FILES).getValue();
        if (!ValidationHelper.isNullOrEmpty(path) && property != null) {
            File[] kmlFilesInFolder = new File(path).listFiles();
            if (kmlFilesInFolder == null) {
                return new ArrayList<>();
            }
            File kmlFileToLook = null;
            for (final File fileEntry : kmlFilesInFolder) {
                if (!fileEntry.isDirectory()
                        && fileEntry.getName().substring(fileEntry.getName().lastIndexOf('.') + 1).equals("kml")
                        && fileEntry.getName().contains(property.getCity().getCfis())) {
                    kmlFileToLook = fileEntry;
                    break;
                }
            }
            if (kmlFileToLook == null) {
                return new ArrayList<>();
            }
            return GeolocationHelper.findZoneByAddress(
                    property.getCityDescription() + " " + property.getAddress(),
                    FileHelper.loadContentByPath(kmlFileToLook.getPath()), coordinates);
        }
        return new ArrayList<>();
    }
    
    public static List<String> findZoneByCoordinates(Property property,List<Pair<Double, Double>> coordinates) throws Exception {
        String path = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.OMI_KML_FILES).getValue();
        if (!ValidationHelper.isNullOrEmpty(path) && property != null) {
            File[] kmlFilesInFolder = new File(path).listFiles();
            if (kmlFilesInFolder == null) {
                return new ArrayList<>();
            }
            File kmlFileToLook = null;
            for (final File fileEntry : kmlFilesInFolder) {
                if (!fileEntry.isDirectory()
                        && fileEntry.getName().substring(fileEntry.getName().lastIndexOf('.') + 1).equals("kml")
                        && fileEntry.getName().contains(property.getCity().getCfis())) {
                    kmlFileToLook = fileEntry;
                    break;
                }
            }
            if (kmlFileToLook == null) {
                return new ArrayList<>();
            }
            return GeolocationHelper.findZoneByCoordinates(coordinates, kmlFileToLook);
        }
        return new ArrayList<>();
    }

    @Data
    private static class CategoryCodeForOmi {
        private String category;
        private String code;
    }

    @Data
    public static class CalculatedOmi {
        private double value;
        private boolean severalZones;
        private boolean severalComprs;
        private boolean multipleCoordinates;
        private boolean categoryCodeMissing;
    }
}