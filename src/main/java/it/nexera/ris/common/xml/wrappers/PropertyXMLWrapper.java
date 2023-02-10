package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.common.enums.PropertyXMLElements;
import it.nexera.ris.common.enums.XMLElements;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class PropertyXMLWrapper extends BaseXMLWrapper<Property> {

    private static final int NUMBER_OF_THOUSANDS_DIGITS = 7; //1.234,56
    private String additionalData;

    private String address;

    private String dataFrom;

    private String scala;

    private String interno;

    private List<String> agriculturalIncome = new LinkedList<>();

    private String area;

    private List<Double> ares = new LinkedList<>();

    private Double cadastralArea;

    private Double exclusedArea;

    private List<String> cadastralIncome = new LinkedList<>();

    private List<Double> centiares = new LinkedList<>();

    private String classRealEstate;

    private String consistency;

    private String deduction;

    private String estimateOMI;

    private List<Double> hectares = new LinkedList<>();

    private String microZone;

    private Double numberOfRooms;

    private String portion;

    private Date propertyAssessmentDate;

    private String quality;

    private String revenue;

    private Long type;

    private String buildEvaluationMethod;

    private String buildEvaluationType;

    private String categoryCode;

    private String cityCode;

    private String provinceCode;

    private String arisingFromData;

    private String quote;

    private String propertyType;

    private String floor;

    private String sectionCity;

    private List<String> consistencyDataAlt = new LinkedList<>();

    @Override
    public Property toEntity()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return toEntity(DaoManager.getSession());
    }

    public Property toEntity(Session session)
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        Property property = new Property();

        property.setId(getId());
        property.setCreateUserId(getCreateUserId());
        property.setUpdateUserId(getUpdateUserId());
        property.setCreateDate(getCreateDate());
        property.setUpdateDate(getUpdateDate());
        property.setVersion(getVersion());
        property.setAdditionalData(getAdditionalData());
        property.setAddress(getAddress());
        property.setDataFrom(getDataFrom());
        Double sum = 0d;
        for (String temp : getAgriculturalIncome()) {
            if (temp.length() > NUMBER_OF_THOUSANDS_DIGITS) {
                temp = temp.replaceAll("\\.", "");
            }
            sum += Double.parseDouble(temp.replaceAll(",", "."));
        }
        property.setAgriculturalIncome(sum.toString());
        property.setArea(getArea());
        property.setCadastralArea(getCadastralArea());
        property.setExclusedArea(getExclusedArea());
        sum = 0d;
        for (String temp : getCadastralIncome()) {
            if (temp.length() > NUMBER_OF_THOUSANDS_DIGITS) {
                temp = temp.replaceAll("\\.", "");
            }
            sum += Double.parseDouble(temp.replaceAll(",", "."));
        }
        property.setCadastralIncome(sum.toString());

        property.setClassRealEstate(getClassRealEstate());
        if(!ValidationHelper.isNullOrEmpty(getConsistencyDataAlt())){
            sum = 0d;
            for (String temp : getConsistencyDataAlt()) {
                if (temp.length() > NUMBER_OF_THOUSANDS_DIGITS) {
                    temp = temp.replaceAll("\\.", "");
                }
                sum += Double.parseDouble(temp.replaceAll(",", "."));
            }
            String consistency = Double.toString(sum);
            if(consistency.endsWith(".00") || consistency.endsWith(".0")){
                consistency = consistency.substring(0, consistency.lastIndexOf("."));
            }
            property.setConsistency(consistency + " MQ");
        }else
            property.setConsistency(getConsistency());

        property.setDeduction(getDeduction());
        property.setMicroZone(getMicroZone());
        property.setNumberOfRooms(getNumberOfRooms());
        property.setPortion(getPortion());
        property.setQuality(getQuality());
        property.setRevenue(getRevenue());
        property.setType(getType());
        property.setArisingFromData(getArisingFromData());
        property.setQuote(getQuote());
        property.setPropertyType(getPropertyType());
        property.setFloor(getFloor());
        property.setScala(getScala());
        property.setInterno(getInterno());
        property.setSectionCity(getSectionCity());
        sumArea(property);

        if (!ValidationHelper.isNullOrEmpty(getCategoryCode()) && !getCategoryCode().contains("/")) {
            CadastralCategory category = ConnectionManager.get(CadastralCategory.class,
                    Restrictions.eq("code", getCategoryCode()), session);
            if (category == null && getCategoryCode().equalsIgnoreCase("T")) {
                category = new CadastralCategory();
                category.setCode("T");
                category.setDescription("Terreni");
                ConnectionManager.save(category, true, session);
            }
            property.setCategory(category);
        }

        List<City> cities = null;
        if(!ValidationHelper.isNullOrEmpty(getCityCode()) && 
                !ValidationHelper.isNullOrEmpty(getSectionCity())) {
            cities = ConnectionManager.load(City.class, new Criterion[]{Restrictions.eq("cfis", getCityCode() + getSectionCity())}, session);    
        }
        if (ValidationHelper.isNullOrEmpty(cities)) {
            cities = ConnectionManager.load(City.class, new Criterion[]{Restrictions.eq("cfis", getCityCode())}, session);
        }
        City city = new City();
        if (!ValidationHelper.isNullOrEmpty(cities)) {
            if(cities.stream().anyMatch(c->c.getDescription()!=null)){
                city = cities.stream().filter(c->c.getDescription()!=null).findFirst().get();
            } else {
                city = cities.get(0);
            }
        }

        if (ValidationHelper.isNullOrEmpty(city)) {
            city = new City();
            city.setCfis(getCityCode());
            ConnectionManager.save(city, true, session);
        }
        property.setCity(city);

        Province province = ConnectionManager.get(Province.class,
                Restrictions.eq("code", getProvinceCode()), session);

        property.setProvince(province);

        return property;
    }

    public static Property modifyDBProperty(Property propertyDB, Property property) {
        if (!ValidationHelper.isNullOrEmpty(property.getAdditionalData()))
            propertyDB.setAdditionalData(property.getAdditionalData());
        if (!ValidationHelper.isNullOrEmpty(property.getAddress()))
            propertyDB.setAddress(property.getAddress());
        if (!ValidationHelper.isNullOrEmpty(property.getDataFrom()))
            propertyDB.setDataFrom(property.getDataFrom());
        if (!ValidationHelper.isNullOrEmpty(property.getAgriculturalIncome()))
            propertyDB.setAgriculturalIncome(property.getAgriculturalIncome());
        if (!ValidationHelper.isNullOrEmpty(property.getArea()))
            propertyDB.setArea(property.getArea());
        if (!ValidationHelper.isNullOrEmpty(property.getCadastralArea()))
            propertyDB.setCadastralArea(property.getCadastralArea());
        if (!ValidationHelper.isNullOrEmpty(property.getCadastralIncome()))
            propertyDB.setCadastralIncome(property.getCadastralIncome());
        if (!ValidationHelper.isNullOrEmpty(property.getClassRealEstate()))
            propertyDB.setClassRealEstate(property.getClassRealEstate());
        if (!ValidationHelper.isNullOrEmpty(property.getConsistency()))
            propertyDB.setConsistency(property.getConsistency());
        if (!ValidationHelper.isNullOrEmpty(property.getDeduction()))
            propertyDB.setDeduction(property.getDeduction());
        if (!ValidationHelper.isNullOrEmpty(property.getMicroZone()))
            propertyDB.setMicroZone(property.getMicroZone());
        if (!ValidationHelper.isNullOrEmpty(property.getNumberOfRooms()))
            propertyDB.setNumberOfRooms(property.getNumberOfRooms());
        if (!ValidationHelper.isNullOrEmpty(property.getPortion()))
            propertyDB.setPortion(property.getPortion());
        if (!ValidationHelper.isNullOrEmpty(property.getQuality()))
            propertyDB.setQuality(property.getQuality());
        if (!ValidationHelper.isNullOrEmpty(property.getRevenue()))
            propertyDB.setRevenue(property.getRevenue());
        if (!ValidationHelper.isNullOrEmpty(property.getType()))
            propertyDB.setType(property.getType());
        if (!ValidationHelper.isNullOrEmpty(property.getArisingFromData()))
            propertyDB.setArisingFromData(property.getArisingFromData());
        if (!ValidationHelper.isNullOrEmpty(property.getQuote()))
            propertyDB.setQuote(property.getQuote());
        if (!ValidationHelper.isNullOrEmpty(property.getPropertyType()))
            propertyDB.setPropertyType(property.getPropertyType());
        if (!ValidationHelper.isNullOrEmpty(property.getFloor()))
            propertyDB.setFloor(property.getFloor());
        if (!ValidationHelper.isNullOrEmpty(property.getScala()))
            propertyDB.setScala(property.getScala());
        if (!ValidationHelper.isNullOrEmpty(property.getInterno()))
            propertyDB.setInterno(property.getInterno());
        if (!ValidationHelper.isNullOrEmpty(property.getHectares()))
            propertyDB.setHectares(property.getHectares());
        if (!ValidationHelper.isNullOrEmpty(property.getAres()))
            propertyDB.setAres(property.getAres());
        if (!ValidationHelper.isNullOrEmpty(property.getCentiares()))
            propertyDB.setCentiares(property.getCentiares());
        if (!ValidationHelper.isNullOrEmpty(property.getCategory()))
            propertyDB.setCategory(property.getCategory());
        if (!ValidationHelper.isNullOrEmpty(property.getCity()))
            propertyDB.setCity(property.getCity());
        if (!ValidationHelper.isNullOrEmpty(property.getProvince()))
            propertyDB.setProvince(property.getProvince());
        return propertyDB;
    }

    private void sumArea(Property property) {
        Double fullSum = 0d;
        for (Double he : getHectares()) {
            fullSum += (he * 1_00_00);
        }
        for (Double are : getAres()) {
            fullSum += (are * 1_00);
        }
        for (Double ca : getCentiares()) {
            fullSum += ca;
        }
        Integer i = (int) (fullSum / 1_00_00);
        property.setHectares(i.doubleValue());
        fullSum -= i * 1_00_00;
        i = (int) (fullSum / 1_00);
        property.setAres((double) i);
        i = (int) (fullSum % 1_00);
        property.setCentiares((double) i);
    }

    @Override
    public void setField(XMLElements element, String value) {
        if (!ValidationHelper.isNullOrEmpty(value)) {
            switch ((PropertyXMLElements) element) {
                case ADDITIONAL_DATA:
                    setAdditionalData(value);
                    break;

                case ADDRESS:
                    setAddress(value);
                    break;

                case DATA_FROM:
                case DATA_FROM_ALT:
                    setDataFrom(value);
                    break;

                case SCALA:
                    setScala(value);
                    break;

                case INTERNO:
                    setInterno(value);
                    break;

                case AGRICULTURAL_INCOME:
                case AGRICULTURAL_INCOME_ALT:
                    getAgriculturalIncome().add(value);
                    break;

                case AREA:
                    setArea(value);
                    break;

                case ARES:
                    getAres().add(Double.parseDouble(value.replaceAll(",", ".")));
                    break;

                case BUILD_EVALUATION_METHOD_ID:
                    setBuildEvaluationMethod(value);
                    break;

                case BUILD_EVALUATION_TYPE_ID:
                    setBuildEvaluationType(value);
                    break;

                case CADASTRAL_AREA:
                    setCadastralArea(Double.parseDouble(value.replaceAll(",", ".")));
                    break;

                case EXCLUSED_AREA:
                    setExclusedArea(Double.parseDouble(value.replaceAll(",", ".")));
                    break;

                case CADASTRAL_INCOME:
                case CADASTRAL_INCOME_ALT:
                    getCadastralIncome().add(value);
                    break;

                case CATEGORY_CODE:
                    setCategoryCode(value);
                    break;

                case CENTIARES:
                    getCentiares().add(Double.parseDouble(value.replaceAll(",", ".")));
                    break;

                case CITY_CODE:
                    setCityCode(value);
                    break;

                case CITY_CODE_POSTFIX:
                    if(StringUtils.isBlank(getSectionCity())){
                        setSectionCity(value);
                    }
                    break;

                case CITY_CODE_POSTFIX_ALT:
                    if(StringUtils.isNotBlank(value))
                        setSectionCity(value);
                    break;

                case CLASS_REAL_ESTATE:
                    setClassRealEstate(value);
                    break;

                case CONSISTENCY:
                    setConsistency(value);
                    break;

                case CONSISTENCY_ALT:
                    if(!ValidationHelper.isNullOrEmpty(value)){
                        getConsistencyDataAlt().add(value.replaceAll("\\.", ""));
                    }

                    break;

                case DEDUCTION:
                    setDeduction(value);
                    break;

                case ESTIMATE_OMI:
                    setEstimateOMI(value);
                    break;

                case HECTARES:
                    getHectares().add(Double.parseDouble(value.replaceAll(",", ".")));
                    break;

                case MICRO_ZONE:
                    setMicroZone(value);
                    break;

                case NUMBER_OF_ROOMS:
                    setNumberOfRooms(Double.parseDouble(value.replaceAll(",", ".")));
                    break;

                case PORTION:
                    setPortion(value);
                    break;

                case PROPERTY_ASSESSMENT_DATE:
                    setPropertyAssessmentDate(DateTimeHelper.fromXMLString(value));
                    break;

                case PROVINCE_CODE:
                    setProvinceCode(value);
                    break;

                case QUALITY:
                case QUALITY_ALT:
                    if(!ValidationHelper.isNullOrEmpty(value))
                        value = value.trim();
                    setQuality(value);
                    break;

                case REVENUE:
                    setRevenue(value);
                    break;

                case TYPE_ID:
                    setType(Long.parseLong(value));
                    break;

                case ARISING_FROM_DATA:
                    setArisingFromData(value);
                    break;

                case QUOTE:
                    setQuote(value);
                    break;

                case PROPERTYTYPE:
                    setPropertyType(value);
                    break;

                case FLOOR:
                    setFloor(value);
                    break;

                default:
                    break;

            }
        }
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDataFrom() {
        return dataFrom;
    }

    public void setDataFrom(String dataFrom) {
        this.dataFrom = dataFrom;
    }

    public List<String> getAgriculturalIncome() {
        return agriculturalIncome;
    }

    public void setAgriculturalIncome(List<String> agriculturalIncome) {
        this.agriculturalIncome = agriculturalIncome;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public List<Double> getAres() {
        return ares;
    }

    public void setAres(List<Double> ares) {
        this.ares = ares;
    }

    public List<Double> getCentiares() {
        return centiares;
    }

    public void setCentiares(List<Double> centiares) {
        this.centiares = centiares;
    }

    public List<Double> getHectares() {
        return hectares;
    }

    public void setHectares(List<Double> hectares) {
        this.hectares = hectares;
    }

    public Double getCadastralArea() {
        return cadastralArea;
    }

    public void setCadastralArea(Double cadastralArea) {
        this.cadastralArea = cadastralArea;
    }

    public List<String> getCadastralIncome() {
        return cadastralIncome;
    }

    public void setCadastralIncome(List<String> cadastralIncome) {
        this.cadastralIncome = cadastralIncome;
    }

    public String getClassRealEstate() {
        return classRealEstate;
    }

    public void setClassRealEstate(String classRealEstate) {
        this.classRealEstate = classRealEstate;
    }

    public String getDeduction() {
        return deduction;
    }

    public void setDeduction(String deduction) {
        this.deduction = deduction;
    }

    public String getEstimateOMI() {
        return estimateOMI;
    }

    public void setEstimateOMI(String estimateOMI) {
        this.estimateOMI = estimateOMI;
    }

    public String getMicroZone() {
        return microZone;
    }

    public void setMicroZone(String microZone) {
        this.microZone = microZone;
    }

    public Double getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(Double numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public String getPortion() {
        return portion;
    }

    public void setPortion(String portion) {
        this.portion = portion;
    }

    public Date getPropertyAssessmentDate() {
        return propertyAssessmentDate;
    }

    public void setPropertyAssessmentDate(Date propertyAssessmentDate) {
        this.propertyAssessmentDate = propertyAssessmentDate;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getRevenue() {
        return revenue;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public String getBuildEvaluationMethod() {
        return buildEvaluationMethod;
    }

    public void setBuildEvaluationMethod(String buildEvaluationMethod) {
        this.buildEvaluationMethod = buildEvaluationMethod;
    }

    public String getBuildEvaluationType() {
        return buildEvaluationType;
    }

    public void setBuildEvaluationType(String buildEvaluationType) {
        this.buildEvaluationType = buildEvaluationType;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getArisingFromData() {
        return arisingFromData;
    }

    public void setArisingFromData(String arisingFromData) {
        this.arisingFromData = arisingFromData;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getScala() {
        return scala;
    }

    public void setScala(String scala) {
        this.scala = scala;
    }

    public String getInterno() {
        return interno;
    }

    public void setInterno(String interno) {
        this.interno = interno;
    }

    public String getSectionCity() {
        return sectionCity;
    }

    public void setSectionCity(String sectionCity) {
        this.sectionCity = sectionCity;
    }

    public Double getExclusedArea() {
        return exclusedArea;
    }

    public void setExclusedArea(Double exclusedArea) {
        this.exclusedArea = exclusedArea;
    }

    public String getConsistency() {
        return consistency;
    }

    public void setConsistency(String consistency) {
        this.consistency = consistency;
    }

    public List<String> getConsistencyDataAlt() {
        return consistencyDataAlt;
    }

    public void setConsistencyDataAlt(List<String> consistencyDataAlt) {
        this.consistencyDataAlt = consistencyDataAlt;
    }
}
