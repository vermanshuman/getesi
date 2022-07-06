package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.comparators.CommercialValueHistoryComparator;
import it.nexera.ris.common.comparators.EstimateOMIComparator;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.RealEstateType;
import it.nexera.ris.common.enums.RelationshipType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralTopology;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.NoCamelCaseWord;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.interfaces.BeforeSave;
import it.nexera.ris.web.converters.PropertyAreaConverter;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.Formula;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.model.SelectItem;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Entity
@Table(name = "property")
public class Property extends IndexedEntity implements BeforeSave {

    private static final long serialVersionUID = 441517417940667029L;

    protected static transient final Log log = LogFactory.getLog(Property.class);

    /**
     * (non-Javadoc)
     *
     * @see it.nexera.ris.common.enums.RealEstateType
     */
    @Column(name = "type_id")
    private Long type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    private Province province;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "section_city")
    private String sectionCity;

    @Column(name = "area")
    private String area;

    @Column(name = "micro_zone")
    private String microZone;

    @ManyToOne
    @JoinColumn(name = "cadastral_category_id")
    private CadastralCategory category;

    @Column(name = "class_real_estate")
    private String classRealEstate;

    @Column(name = "consistency")
    private String consistency;

    @Column(name = "cadastral_area")
    private Double cadastralArea;

    @Column(name = "portion")
    private String portion;

    @Column(name = "quality")
    private String quality;

    @Column(name = "hectares")
    private Double hectares;

    @Column(name = "ares")
    private Double ares;

    @Column(name = "centiares")
    private Double centiares;

    @Column(name = "deduction")
    private String deduction;

    @Column(name = "cadastral_income")
    private String cadastralIncome;

    @Column(name = "agricultural_income")
    private String agriculturalIncome;

    @Column(name = "revenue")
    private String revenue;

    @Column(name = "address")
    private String address;

    @Column(name = "data_from")
    private String dataFrom;

    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;

    @Column(name = "floor")
    private String floor;

    @Column(name = "number_of_rooms")
    private Double numberOfRooms;

    @Column(name = "scala")
    private String scala;

    @Column(name = "interno")
    private String interno;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "cadastral_property", joinColumns = {
            @JoinColumn(name = "property_id", table = "property")
    }, inverseJoinColumns = {
            @JoinColumn(name = "cadastral_data_id", table = "cadastral_data")
    })
    private List<CadastralData> cadastralData;

    @OneToMany(mappedBy = "property", cascade = CascadeType.REMOVE)
    private List<Relationship> relationships;

    @ManyToMany(mappedBy = "properties")
    private List<SectionB> sectionB;

    @OneToMany(mappedBy = "property")
    private List<EstimateOMIHistory> estimateOMIHistory;

    @OneToMany(mappedBy = "property")
    private List<CommercialValueHistory> commercialValueHistory;

    @Transient
    private List<EstateSituation> estateSituationList;

    @OneToMany(mappedBy = "property")
    private List<DatafromProperty> datafromProperties;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private List<SituationProperty> situationProperties;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<EstateSituationFormalityProperty> estateSituationFormalityPropertyList;

    @ManyToMany
    @JoinTable(name = "request_property", joinColumns = {
            @JoinColumn(name = "property_id", table = "property")
    }, inverseJoinColumns = {
            @JoinColumn(name = "request_id", table = "request")
    })
    private List<Request> requestList;

    @Formula("IFNULL(centiares, 0)+(IFNULL(ares,0)*100)+(IFNULL(hectares,0)*10000)")
    private Long metersLand;

    @Column(name = "comment", columnDefinition = "LONGTEXT")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "replaced_subject_id")
    private Subject replacedSubject;

    @OneToOne(mappedBy = "oldProperty")
    private OldProperty oldProperty;

    @Column(name = "modified")
    private Boolean modified;

    @Column(name = "exclused_area")
    private Double exclusedArea;

    @Column(name = "zone")
    private String zone;

    @Transient
    private Boolean visible;

    @Transient
    private Boolean matchByFields;

    @Transient
    private boolean used;

    @Transient
    private boolean selected;

    @Transient
    private String arisingFromData;

    @Transient
    private String quote;

    @Transient
    private String propertyType;

    @Transient
    private Long relationshipTypeId;

    @Transient
    private Long aggregationLandChargedRegistryId;

    @Transient
    private int numberInFormalityGroup;

    @Transient
    private List<Property> oldPropertiesToView;

    @Transient
    private List<SelectItem> editCities;

    @Transient
    private Long selectedCityId;

    @Transient
    private Request currentRequest;

    @Transient
    private LandCadastralCulture cadastralCulture;

    @Transient
    private LandCulture landCulture;

    @Transient
    private Boolean landOmiValueRelated;

    @Override
    public void beforeSave() {
        if (ValidationHelper.isNullOrEmpty(getEstateSituationListWithoutInit())) {
            return;
        }
        List<SituationProperty> listToRemove = new ArrayList<>();
        List<SituationProperty> listToAdd = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getSituationProperties())) {
            getSituationProperties().stream()
                    .filter(sp -> getEstateSituationListWithoutInit().stream().noneMatch(es -> es.getId().equals(sp.getSituation().getId())))
                    .forEach(sp -> {
                                sp.removeAllDatafromPropertiesAssociations();
                                sp.getSituation().getSituationProperties().remove(sp);
                                DaoManager.removeWeak(sp, false);
                                listToRemove.add(sp);
                            }
                    );
            getSituationProperties().removeAll(listToRemove);
        }

        if (getSituationProperties() == null) {
            setSituationProperties(new ArrayList<>());
        }
        getEstateSituationListWithoutInit().stream()
                .filter(es -> getSituationProperties().stream().noneMatch(sp -> sp.getSituation().getId().equals(es.getId())))
                .forEach(es -> {
                            SituationProperty situationProperty = new SituationProperty(es, this);
                            es.getSituationProperties().add(situationProperty);
                            DaoManager.saveWeak(situationProperty, false);
                            listToAdd.add(situationProperty);
                        }
                );
        getSituationProperties().addAll(listToAdd);
    }

    public String getPrejudicial() {
        try {
            Long count = DaoManager.getCount(Relationship.class, "id", new Criterion[]{
                    Restrictions.eq("property.id", getId()),
                    Restrictions.eq("relationshipTypeId",
                            RelationshipType.FORMALITY.getId())
            });

            return count == 0L ? "NO" : "SI";
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return "";
    }

    public List<DatafromProperty> getAssociatedDatafromPropertiesWithEstateSituationById(Long estateSituationId) {
        SituationProperty situationProperty = this.getSituationPropertyByEstateSituationId(estateSituationId);
        if (!ValidationHelper.isNullOrEmpty(situationProperty)) {
            if (!Hibernate.isInitialized(situationProperty.getDatafromProperties())) {
                Hibernate.initialize(situationProperty.getDatafromProperties());
            }
            return situationProperty.getDatafromProperties();
        }
        return null;
    }

    public SituationProperty getSituationPropertyByEstateSituationId(Long estateSituationId) {
        if (!ValidationHelper.isNullOrEmpty(this.getSituationProperties()) && !ValidationHelper.isNullOrEmpty(estateSituationId)) {
            Optional<SituationProperty> situationPropertyOptional = this.getSituationProperties().stream()
                    .filter(sp -> !ValidationHelper.isNullOrEmpty(sp.getSituation())
                            && estateSituationId.equals(sp.getSituation().getId())).findFirst();
            if (situationPropertyOptional.isPresent()) {
                return situationPropertyOptional.get();
            }
        }
        return null;
    }

    public String getCategoryType() {
        if (!ValidationHelper.isNullOrEmpty(getCategory())) {
            return String.format("%s - %s", getCategory().getCode(), getCategory().getDescription());
        }
        return null;
    }

    public String getSource() {
        RelationshipType type = RelationshipType
                .getById(getRelationshipTypeId());

        if (!ValidationHelper.isNullOrEmpty(type)) {
            switch (type) {
                case CADASTRAL_DOCUMENT:
                    return "C";

                case FORMALITY:
                    return "F";

                case MANUAL_ENTRY:
                    return "M";
            }
        }

        return "";
    }

    public String getRealEstateType() {
        for (RealEstateType type : RealEstateType.values()) {
            if (type.getId().equals(this.getType())) {
                return type.toString();
            }
        }

        return "";
    }

    public String getRealEstateShortType() {
        for (RealEstateType type : RealEstateType.values()) {
            if (type.getId().equals(this.getType())) {
                return type.getShortValue();
            }
        }

        return "";
    }

    public boolean isPresumable(Property property) {
        return Objects.equals(this.getType(), property.getType()) && Objects.equals(this.getCity(), property.getCity())
                && this.getCadastralData().stream().allMatch(cd -> property.getCadastralData().stream().anyMatch(cd::isPresumable));
    }

    public List<String> getRelationData() throws PersistenceBeanException, IllegalAccessException {
        List<Relationship> relationships = null;

        if (getCurrentRequest().getDistraintFormality() != null) {
            List<Long> documentsIds = getCurrentRequest().getDocumentsRequest().stream()
                    .filter(x -> DocumentType.CADASTRAL.getId().equals(x.getTypeId())).map(IndexedEntity::getId)
                    .collect(Collectors.toList());

            relationships = getRelationships().stream()
                    .filter(r -> RelationshipType.MANUAL_ENTRY.getId().equals(r.getRelationshipTypeId())
                            && documentsIds.contains(r.getTableId())).collect(Collectors.toList());

            if (ValidationHelper.isNullOrEmpty(relationships)) {
                relationships = getRelationships().stream()
                        .filter(r -> RelationshipType.CADASTRAL_DOCUMENT.getId().equals(r.getRelationshipTypeId())
                                && documentsIds.contains(r.getTableId())).collect(Collectors.toList());
            }

            if (!ValidationHelper.isNullOrEmpty(relationships)) {
                List<Relationship> relationshipsDistinct = new ArrayList<>();
                for (Relationship r : relationships) {
                    relationshipsDistinct.removeIf(rd -> rd.getSubject().equals(r.getSubject())
                            && rd.getCadastralDate().compareTo(r.getCadastralDate()) < 0);
                    relationshipsDistinct.add(r);
                }
                relationships = relationshipsDistinct;
            }

        } else if (!ValidationHelper.isNullOrEmpty(getCurrentRequest().getSubject())) {
            relationships = getRelationships().stream()
                    .filter(r -> RelationshipType.MANUAL_ENTRY.getId().equals(r.getRelationshipTypeId())
                            && r.getSubject().getId().equals(getCurrentRequest().getSubject().getId()))
                    .collect(Collectors.toList());

            if (ValidationHelper.isNullOrEmpty(relationships)) {
                relationships = getRelationships().stream()
                        .filter(r -> RelationshipType.CADASTRAL_DOCUMENT.getId().equals(r.getRelationshipTypeId())
                                && r.getSubject().getId().equals(getCurrentRequest().getSubject().getId()))
                        .collect(Collectors.toList());
            }
        }

        if (!ValidationHelper.isNullOrEmpty(relationships)) {
            return relationships.stream().map(Relationship::getPropertyReportStr).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public List<Relationship> getRelations() throws PersistenceBeanException, IllegalAccessException {
        List<Relationship> relationships = null;

        if (getCurrentRequest().getDistraintFormality() != null) {
            List<Long> documentsIds = getCurrentRequest().getDocumentsRequest().stream()
                    .filter(x -> DocumentType.CADASTRAL.getId().equals(x.getTypeId())).map(IndexedEntity::getId)
                    .collect(Collectors.toList());

            relationships = getRelationships().stream()
                    .filter(r -> RelationshipType.MANUAL_ENTRY.getId().equals(r.getRelationshipTypeId())
                            && documentsIds.contains(r.getTableId())).collect(Collectors.toList());

            if (ValidationHelper.isNullOrEmpty(relationships)) {
                relationships = getRelationships().stream()
                        .filter(r -> RelationshipType.CADASTRAL_DOCUMENT.getId().equals(r.getRelationshipTypeId())
                                && documentsIds.contains(r.getTableId())).collect(Collectors.toList());
            }

            if (!ValidationHelper.isNullOrEmpty(relationships)) {
                List<Relationship> relationshipsDistinct = new ArrayList<>();
                for (Relationship r : relationships) {
                    relationshipsDistinct.removeIf(rd -> rd.getSubject().equals(r.getSubject())
                            && rd.getCadastralDate().compareTo(r.getCadastralDate()) < 0);
                    relationshipsDistinct.add(r);
                }
                relationships = relationshipsDistinct;
            }

        } else if (!ValidationHelper.isNullOrEmpty(getCurrentRequest().getSubject())) {
            relationships = getRelationships().stream()
                    .filter(r -> RelationshipType.MANUAL_ENTRY.getId().equals(r.getRelationshipTypeId())
                            && r.getSubject().getId().equals(getCurrentRequest().getSubject().getId()))
                    .collect(Collectors.toList());

            if (ValidationHelper.isNullOrEmpty(relationships)) {
                relationships = getRelationships().stream()
                        .filter(r -> RelationshipType.CADASTRAL_DOCUMENT.getId().equals(r.getRelationshipTypeId())
                                && r.getSubject().getId().equals(getCurrentRequest().getSubject().getId()))
                        .collect(Collectors.toList());
            }
        }

        if (!ValidationHelper.isNullOrEmpty(relationships)) {
            return relationships;
        } else {
            return null;
        }
    }
    public List<String> getSchedaList() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralData())) {
            return getCadastralData().stream().distinct()
                    .map(p -> !ValidationHelper.isNullOrEmpty(p.getScheda()) ? p.getScheda() : "")
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public String getSchedas() {
        return getSchedaList().stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    public List<String> getDataSchedaList() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralData())) {
            return getCadastralData().stream().distinct()
                    .map(p -> !ValidationHelper.isNullOrEmpty(p.getDataScheda()) ? p.getDataScheda() : "")
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public String getDataSchedas() {
        return getDataSchedaList().stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    public List<String> getSectionList() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralData())) {
            return getCadastralData().stream().distinct()
                    .map(p -> !ValidationHelper.isNullOrEmpty(p.getSection()) ? p.getSection() : "")
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public String getSections() {
        return getSectionList().stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    public List<String> getSheetList() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralData())) {
            return getCadastralData().stream().distinct()
                    .map(p -> !ValidationHelper.isNullOrEmpty(p.getSheet()) ? p.getSheet() : "")
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public String getSheets() {
        return getSheetList().stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    public List<String> getParticleList() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralData())) {
            return getCadastralData().stream().distinct()
                    .map(p -> !ValidationHelper.isNullOrEmpty(p.getParticle()) ? p.getParticle() : "")
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public String getParticles() {
        return getParticleList().stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    public List<String> getSubList() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralData())) {
            return getCadastralData().stream().distinct()
                    .map(p -> !ValidationHelper.isNullOrEmpty(p.getSub()) ? p.getSub() : "")
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public String getSubs() {
        return getSubList().stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    public String getTypeTitle() {
        if (getType() == null) {
            return "";
        } else if (getType() == 1L) {
            return ResourcesHelper
                    .getEnum("documentGenerationTagsPROPERTY_TYPE1");
        } else if (getType() == 2L) {
            return ResourcesHelper
                    .getEnum("documentGenerationTagsPROPERTY_TYPE2");
        }
        return "";
    }

    public String getCategoryDescription() {
        if (getCategory() != null) {
            return getCategory().getDescription() == null ? ""
                    : getCategory().getDescription();
        } else {
            return "";
        }
    }

    public String getCategoryCode() {
        if (getCategory() != null) {
            return getCategory().getCode() == null ? ""
                    : getCategory().getCode();
        } else {
            return "";
        }
    }

    public String getCityDescription() {
        if (getCity() != null) {
            return getCity().getDescription() == null ? ""
                    : getCity().getDescription();
        } else {
            return "";
        }
    }

    public String getProvinceDescription() {
        if (getProvince() != null) {
            return getProvince().getDescription() == null ? ""
                    : getProvince().getDescription();
        } else {
            return "";
        }
    }

    public String getAllFields(boolean addCommercialAndOmi, boolean addLandData) {
        String estateCardList = ResourcesHelper.getString("estateCardListPrefix");
        String estatePlan = ResourcesHelper.getString("estatePlanPrefix");
        String estateConsistency = ResourcesHelper.getString("estateConsistencyPrefix");
        String estateCategory = ResourcesHelper.getString("estateCategoryPrefix");
        String estateInterno = ResourcesHelper.getString("estateInternoPrefix");
        String estateScala = ResourcesHelper.getString("estateScalaPrefix");
        String estateMq = ResourcesHelper.getString("estateMqPrefix");
        String estateAnnuity = ResourcesHelper.getString("estateAnnuityPrefix");
        String estateValueOMI = ResourcesHelper.getString("estateValueOMIPrefix");
        String estateIndicativeCommercial = ResourcesHelper.getString("estateIndicativeCommercialPrefix");

        DaoManager.refresh(this);

        String beggingOfCommentWrap = "";
        String estimateOMIRequestText = PropertyEntityHelper.getLastEstimateOMIRequestText(this);
        String estimateLastCommercialValueRequestText = PropertyEntityHelper.getLastEstimateLastCommercialValueRequestText(this);

        if (ValidationHelper.isNullOrEmpty(estimateOMIRequestText)
                && ValidationHelper.isNullOrEmpty(estimateLastCommercialValueRequestText)) {
            beggingOfCommentWrap = "<br/>";
        }

        StringBuilder sb = new StringBuilder("<div style=\"text-align: justify;\">- ");

        sb.append((getCategory() != null ? getCurrentString("", getCategory().getTextInTag(), false) : ""))
                .append(getCurrentString((getAddress() == null ? "" : getAddressPrefix()) + " ",
                        getAddress() == null ? "" : capitalizeExceptNoCamelCaseWords(getAddress()), false))
                .append("<br/>")
                .append((getCadastralData() == null ? null : estateCardList))
                .append(getDraftString())
                .append((getCategory() != null ? getCurrentString(estateCategory, getCategory().getCodeInVisura(), false) : ""))
                .append(getCurrentString(estatePlan, getFloor(), false))
                .append(getCurrentString(estateInterno, getInterno(), false))
                .append(getCurrentString(estateScala, getScala(), false))
                .append((getCategory() != null && getCategory().getCode() != null && getCategory().getCode().startsWith("A") ? getCurrentString(estateConsistency, getConsistencyTrimmed(), false) :
                        getCategory() != null && getCategory().getCode() != null && getCategory().getCode().startsWith("C") && (getCadastralArea() == null || getCadastralArea() == 0) ? getCurrentString(estateMq, getConsistencyNumber(), false) : getCategory() != null && getCategory().getCode() != null && getCategory().getCode().equalsIgnoreCase("EU")  ? getCurrentString(estateMq, getConsistencEu(), false) : ""))
                .append(getCurrentString(estateMq, (getCadastralArea() == null || getCadastralArea() == 0) ? null : Long.toString(getCadastralArea().longValue()), false))
                .append(getCurrentString(estateAnnuity, manageMoneyView(getRevenue()), true));

        if (addCommercialAndOmi) {
            if(!StringUtils.endsWith(sb, "<br/>"))
        	    sb.append("<br/>");
            sb.append(NumberUtils.isParsable(estimateOMIRequestText.replaceAll(",", ".")) ?
                    getCurrentString(estateValueOMI, manageMoneyView(estimateOMIRequestText), true) :
                    getCurrentString(estateValueOMI.replaceAll("&euro;", ""), estimateOMIRequestText, true));
            sb.append(NumberUtils.isParsable(estimateLastCommercialValueRequestText.replaceAll(",", ".")) ?
                    getCurrentString(estateIndicativeCommercial, manageMoneyView(estimateLastCommercialValueRequestText), true) :
                    getCurrentString(estateIndicativeCommercial.replaceAll("&euro;", ""), estimateLastCommercialValueRequestText, true));
        }
        if (addLandData ) {
            sb.append("&nbsp;mq&nbsp;");
            String landMQ= getTagLandMQ();
            if(landMQ.endsWith(".00") || landMQ.endsWith(".0"))
                landMQ = landMQ.substring(0, landMQ.lastIndexOf("."));
            if(!landMQ.contains(".") && !landMQ.contains(",")){
                landMQ = GeneralFunctionsHelper.formatDoubleString(landMQ);
            }
            sb.append(landMQ);
        }
        sb.append((!ValidationHelper.isNullOrEmpty(getComment()) &&
                !getComment().equals(ResourcesHelper.getString("propertyCommentDefaultValue")) ?
                beggingOfCommentWrap + "<i>" + getComment() + "</i>" : ""))
                .append("</div>");

        return sb.toString();
    }

    public String getLandData() {
        StringBuilder sb = new StringBuilder();
        if (ValidationHelper.isNullOrEmpty(this.getHectares()) || this.getHectares() == 0.0) {
            if ((!ValidationHelper.isNullOrEmpty(this.getAres()) && !ValidationHelper.isNullOrEmpty(this.getCentiares()))
                    && (!this.getAres().equals(0.0) || !this.getCentiares().equals(0.0))) {
                sb.append("&nbsp;are&nbsp;")
                        .append(this.getAres() == null ? "00" : PropertyAreaConverter.getString(this.getAres()))
                        .append(".")
                        .append(this.getCentiares() == null ? "" : PropertyAreaConverter.getString(this.getCentiares()));
            }
        }
        if(sb.length() == 0){
            if((ValidationHelper.isNullOrEmpty(getCentiares()) || getCentiares().equals(0.0)) &&
                    (ValidationHelper.isNullOrEmpty(getAres()) || getAres().equals(0.0)) &&
                    (ValidationHelper.isNullOrEmpty(getHectares()) || getHectares().equals(0.0)) && !ValidationHelper.isNullOrEmpty(getConsistency())){
                sb.append("&nbsp;mq&nbsp;")
                        .append(getConsistencyNumber());
            }else {
                sb.append("&nbsp;ha&nbsp;")
                        .append(this.getHectares() == null ? "" : PropertyAreaConverter.getString(this.getHectares()))
                        .append(".")
                        .append(this.getAres() == null ? "00" : PropertyAreaConverter.getString(this.getAres()))
                        .append(".")


                        .append(this.getCentiares() == null ? "" : PropertyAreaConverter.getString(this.getCentiares()));
            }
        }

        return sb.toString();
    }

    private String capitalizeExceptNoCamelCaseWords(String address) {
        String result = "";

        try {
            List<NoCamelCaseWord> noCamelCaseWords = DaoManager.load(NoCamelCaseWord.class);

            if (!ValidationHelper.isNullOrEmpty(noCamelCaseWords)) {
                for (NoCamelCaseWord caseWord : noCamelCaseWords) {
                    String wordToFind = caseWord.getDescription();
                     Pattern word = Pattern.compile(wordToFind);
                    Matcher match = word.matcher(address);
                    while (match.find()) {
                        boolean isStart = true;
                        boolean isEnd = true;
                        if(match.end() < address.length()){
                            if(StringUtils.isNotBlank(address.substring(match.end(),match.end()+1)))
                                isStart = false;
                        }
                        if(match.start() > 0){
                            if(StringUtils.isNotBlank(address.substring(match.start()-1,match.start())))
                                isEnd = false;
                        }
                        if(isStart && isEnd){
                            result += WordUtils.capitalizeFully(address.substring(0, match.start()))
                                    + address.substring(match.start(), match.end())
                                    + WordUtils.capitalizeFully(address.substring(match.end()));
                        }
                    }
                }
            }
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }

        return !ValidationHelper.isNullOrEmpty(result) ? result : WordUtils.capitalizeFully(address);
    }

    private String manageMoneyView(String number) {
        if (!ValidationHelper.isNullOrEmpty(number)) {
            String temp = "";
            String rest = "";
            if (number.contains(",")) {
                temp = number.substring(0, number.indexOf(","));
                rest = number.substring(number.indexOf(","), number.length());
            } else {
                temp = number;
                rest = ",00";
            }

            if ((temp.length() <= 3) || temp.contains(".")) {
                return number;
            } else {
                temp = new StringBuffer(temp).reverse().toString();
                String[] array = temp.split("");
                StringBuffer result = new StringBuffer();
                for (int i = 0; i < array.length; i++) {
                    int nextDigit = (i + 1);
                    result.append(array[i]);
                    if ((nextDigit % 3 == 0) && nextDigit != array.length) {

                        result.append(".");
                    }
                }
                result = result.reverse();
                result.append(rest);
                return result.toString();
            }
        }
        return "";
    }

    public String getAddressPrefix() {
        if (!ValidationHelper.isNullOrEmpty(getAddress())) {
            String addressStart = getAddress().split(" ")[0];
            List<CadastralTopology> topologyList = null;
            try {
                topologyList = DaoManager.load(CadastralTopology.class, new Criterion[]{
                        Restrictions.ilike("description", addressStart, MatchMode.START)
                });
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }
            if (!ValidationHelper.isNullOrEmpty(topologyList)) {
                return topologyList.stream()
                        .filter(topology -> getAddress().toLowerCase().startsWith(topology.getDescription().toLowerCase()))
                        .sorted((t1, t2) -> Integer.compare(t2.getDescription().length(), t1.getDescription().length()))
                        .findFirst().map(CadastralTopology::getArticle).orElse("alla");
            }
        }
        return "";
    }

    private String getCurrentString(String prefix, String str, boolean br) {
        return str == null || Objects.equals(str, "") ? "" : br ? " " + prefix + str + "<br/>" : " " + prefix + str;
    }

    private String getCurrentString(String str, boolean br) {
        return str == null || Objects.equals(str, "") ? "" : br ? str + "<br/>" : str;
    }

    private String getCurrentString(String prefix, String str, boolean br, boolean bold) {
        String spanBold = "<span style=\"font-weight: bold;\">";
        String body = str == null || Objects.equals(str, "") ? "" : br ? " " + prefix + str + "<br/>" : " " + prefix + str;
        String end = "</span>";
        return bold ? spanBold + body + end : body;
    }

    private String getDraftString() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralData()) && getCadastralData().size() == 1) {
            StringBuilder str = new StringBuilder();
            String estateSezione = ResourcesHelper.getString("estateSezionePrefix");
            String estateCard = ResourcesHelper.getString("estateCardPrefix");
            String estateStreetGraftGlobal = ResourcesHelper.getString("estateStreetGraftGlobalPrefix");
            String estateStreetGraft = ResourcesHelper.getString("estateStreetGraftPrefix");
            String estatePart = ResourcesHelper.getString("estateParticlePrefix");
            String estateParticleGraftGlobal = ResourcesHelper.getString("estateParticleGraftGlobalPrefix");
            String estateParticleGraft = ResourcesHelper.getString("estateParticleGraftPrefix");
            String estateSub = ResourcesHelper.getString("estateSubordinatePrefix");
            String estateSubGraftGlobal = ResourcesHelper.getString("estateSubGraftGlobalPrefix");
            String estateSubGraft = ResourcesHelper.getString("estateSubGraftPrefix");
            String scheda = ResourcesHelper.getString("schedaPrefix");
            String dataScheda = ResourcesHelper.getString("dataSchedaPrefix");
            if (!ValidationHelper.isNullOrEmpty(getSectionList().get(0))) {
                str.append(getCurrentString(estateSezione, getSectionList().get(0), false, true));
            }
            if (ValidationHelper.isNullOrEmpty(getSchedaList().get(0))) {
                str.append(getCurrentString(estateCard, getSheetList().get(0), false, true));
                str.append(getCurrentString(estatePart, getParticleList().get(0), false, true));
                str.append(getCurrentString(estateSub, getSubList().get(0), false, true));
            } else {
                str.append(getCurrentString(scheda, getSchedaList().get(0), false, true));
                str.append(getCurrentString(dataScheda, getDataSchedaList().get(0), false, true));
            }
            return str.toString();
        }
        if (!ValidationHelper.isNullOrEmpty(getCadastralData()) && getCadastralData().size() > 1) {
            return getCadastralData().stream().map(d -> String.format("%s %s %s %s ",
                    !ValidationHelper.isNullOrEmpty(d.getSection()) ? "sez. " + d.getSection() : "",
                    !ValidationHelper.isNullOrEmpty(d.getSheet()) ? "foglio " + d.getSheet() : "",
                    !ValidationHelper.isNullOrEmpty(d.getParticle()) ? "p.lla " + d.getParticle() : "",
                    !ValidationHelper.isNullOrEmpty(d.getSub()) ? "sub " + d.getSub() : ""))
                    .collect(Collectors.joining("e", "<b>", RealEstateType.BUILDING.getId().equals(getType())
                            ? "(graffate) </b>" : "(p.lla graffate) </b>"));
        }
        return "";
    }

    public String getCategoryStr() {
        if (!ValidationHelper.isNullOrEmpty(getCategory())) {
            return String.format("%s - %s", getCategory().getCodeInVisura() == null ? "" : getCategory().getCodeInVisura(),
                    getCategory().getTextInTag() == null ? "" : getCategory().getTextInTag());
        } else {
            return "";
        }
    }

    public boolean isHasFormalities() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        return isHasFormalities(DaoManager.getSession());
    }

    public List<Long> getAggregationLandChargesRegistersIds(Session session) throws IllegalAccessException, InstantiationException {
        List<Long> landIds;
        if (ValidationHelper.isNullOrEmpty(getAggregationLandChargedRegistryId())) {
            landIds = Collections.singletonList(0L);
        } else {
            AggregationLandChargesRegistry aggregation =
                    ConnectionManager.get(AggregationLandChargesRegistry.class, getAggregationLandChargedRegistryId(), session);
            landIds = aggregation.getAggregationLandChargesRegistersIds();
        }
        return landIds;
    }

    public boolean isHasFormalities(Session session) throws IllegalAccessException, InstantiationException {
        List<Long> landIds = getAggregationLandChargesRegistersIds(session);

        List<Property> tempList = RealEstateHelper.getCadastralDatesEqualsProperties(this, session);
        if (ValidationHelper.isNullOrEmpty(tempList)) {
            return false;
        } else {
            return 0 < ConnectionManager.getCount(Formality.class, "id", new CriteriaAlias[]{
                    new CriteriaAlias("sectionB", "sb", JoinType.INNER_JOIN),
                    new CriteriaAlias("sb.properties", "p", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("p.type", getType()),
                    Restrictions.eq("p.city.id", getCity().getId()),
                    Restrictions.eq("p.province.id", getProvince().getId()),
                    Restrictions.in("p.id", tempList.stream().map(Property::getId).collect(Collectors.toList())),
                    Restrictions.or(
                            Restrictions.in("reclamePropertyService.id", landIds),
                            Restrictions.in("provincialOffice.id", landIds)
                    )
            }, session);
        }
    }

    public boolean isLandDataAreExistAndNotEmpty() {
        return !ValidationHelper.isNullOrEmpty(this.getAres()) && !this.getAres().equals(0.0D)
                || !ValidationHelper.isNullOrEmpty(this.getHectares()) && !this.getHectares().equals(0.0D)
                || !ValidationHelper.isNullOrEmpty(this.getCentiares()) && !this.getCentiares().equals(0.0D);
    }

    public String getLandRegistry() {
        return (RealEstateType.BUILDING.getId().equals(getType()))
                ? ResourcesHelper.getString("realEstateBuild")
                : "";
    }

    public String getRightConsistency() {
        if (RealEstateType.BUILDING.getId().equals(getType())) {
            return getConsistency();
        } else if (RealEstateType.LAND.getId().equals(getType())) {
            String result = "";
            if (!ValidationHelper.isNullOrEmpty(getHectares())) {
                result += PropertyAreaConverter.getString(getHectares()) + " ha ";
            }
            if (!ValidationHelper.isNullOrEmpty(getAres())) {
                result += PropertyAreaConverter.getString(getAres()) + " are ";
            }
            if (!ValidationHelper.isNullOrEmpty(getCentiares())) {
                result += PropertyAreaConverter.getString(getCentiares()) + " ca ";
            }
            return result;
        }
        return "";
    }

    public void fillSelectLists() throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getProvince())) {
            setEditCities(ComboboxHelper.fillList(City.class,
                    Order.asc("description"), new Criterion[]{
                            Restrictions.eq("province.id",getProvince().getId()),
                            Restrictions.eq("external", Boolean.TRUE)
                    }));
        }

        setSelectedCityId(getCity().getId());
    }

    public List<DatafromProperty> getDatafromPropertiesUpdated() throws PersistenceBeanException, IllegalAccessException {
        if (!Hibernate.isInitialized(getDatafromProperties())) {
            setDatafromProperties(DaoManager.load(DatafromProperty.class, new Criterion[]{
                    Restrictions.eq("property", this)}));
        }
        return getDatafromProperties();
    }

    public String getCommentWithoutInitialize() {
        return comment;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getMicroZone() {
        return microZone;
    }

    public void setMicroZone(String microZone) {
        this.microZone = microZone;
    }

    public CadastralCategory getCategory() {
        return category;
    }

    public void setCategory(CadastralCategory category) {
        this.category = category;
    }

    public String getClassRealEstate() {
        return classRealEstate;
    }

    public void setClassRealEstate(String classRealEstate) {
        this.classRealEstate = classRealEstate;
    }

    public String getConsistencyTrimmed() {
        if (!ValidationHelper.isNullOrEmpty(getConsistency())) {
            return getConsistency().replaceAll("[vV][aA][nN][iI]", "").trim();
        } else {
            return "";
        }
    }

    public String getConsistencyNumber() {
        if (!ValidationHelper.isNullOrEmpty(getConsistency())) {
            return getConsistency().replaceAll("([vV][aA][nN][iI])|([mM][qQ])", "").trim();
        } else {
            return "";
        }
    }

    public Double getLandMQ() {
        if((ValidationHelper.isNullOrEmpty(getCentiares()) || getCentiares().equals(0.0)) &&
                (ValidationHelper.isNullOrEmpty(getAres()) || getAres().equals(0.0)) &&
                (ValidationHelper.isNullOrEmpty(getHectares()) || getHectares().equals(0.0))){

            return !ValidationHelper.isNullOrEmpty(getConsistency()) ? Double.parseDouble(getConsistencyNumber()) : 0.0;
        }else {
            return  !ValidationHelper.isNullOrEmpty(getMetersLand()) ? getMetersLand().doubleValue() : 0.0;
        }
    }

    public String getTagLandMQ() {
        if((ValidationHelper.isNullOrEmpty(getCentiares()) || getCentiares().equals(0.0)) &&
                (ValidationHelper.isNullOrEmpty(getAres()) || getAres().equals(0.0)) &&
                (ValidationHelper.isNullOrEmpty(getHectares()) || getHectares().equals(0.0))){

            return !ValidationHelper.isNullOrEmpty(getConsistency()) ? getConsistencyNumber() : "0.0";
        }else {
            return  !ValidationHelper.isNullOrEmpty(getMetersLand()) ? String.valueOf(getMetersLand()) : "0.0";
        }
    }
    
    public String getEstateLandMQ() {
    	String landMQ= getTagLandMQ();
        if(landMQ.endsWith(".00") || landMQ.endsWith(".0"))
            landMQ = landMQ.substring(0, landMQ.lastIndexOf("."));
        if(!landMQ.contains(".") && !landMQ.contains(",")){
            landMQ = GeneralFunctionsHelper.formatDoubleString(landMQ);
        }
        return landMQ;
    }

    public String getConsistencEu() {
        if (!ValidationHelper.isNullOrEmpty(getConsistency())) {
            return getConsistency().replace("centiare", "").replace("are", "").replaceAll("\\s", "").trim();
        } else {
            return "";
        }
    }

    public String getConsistency() {
        return consistency;
    }

    public void setConsistency(String consistency) {
        this.consistency = consistency;
    }

    public Double getCadastralArea() {
        return cadastralArea;
    }

    public void setCadastralArea(Double cadastralArea) {
        this.cadastralArea = cadastralArea;
    }

    public String getPortion() {
        return portion;
    }

    public void setPortion(String portion) {
        this.portion = portion;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public Double getHectares() {
        return hectares;
    }

    public void setHectares(Double hectares) {
        this.hectares = hectares;
    }

    public Double getAres() {
        return ares;
    }

    public void setAres(Double ares) {
        this.ares = ares;
    }

    public Double getCentiares() {
        return centiares;
    }

    public void setCentiares(Double centiares) {
        this.centiares = centiares;
    }

    public String getDeduction() {
        return deduction;
    }

    public void setDeduction(String deduction) {
        this.deduction = deduction;
    }

    public String getCadastralIncome() {
        return cadastralIncome;
    }

    public void setCadastralIncome(String cadastralIncome) {
        this.cadastralIncome = cadastralIncome;
    }

    public String getAgriculturalIncome() {
        return agriculturalIncome;
    }

    public void setAgriculturalIncome(String agriculturalIncome) {
        this.agriculturalIncome = agriculturalIncome;
    }

    public String getRevenue() {
        return revenue;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
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

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    public Double getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(Double numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public List<CadastralData> getCadastralData() {
        return cadastralData;
    }

    public void setCadastralData(List<CadastralData> cadastralData) {
        this.cadastralData = cadastralData;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }

    public List<SectionB> getSectionB() {
        return sectionB;
    }

    public void setSectionB(List<SectionB> sectionB) {
        this.sectionB = sectionB;
    }

    public EstimateOMIHistory getLastEstimateOMI() {
        if (!ValidationHelper.isNullOrEmpty(getEstimateOMIHistory())) {
            List<EstimateOMIHistory> list = getEstimateOMIHistory();

            list.sort(new EstimateOMIComparator());

            return list.get(0);
        }
        return null;
    }

    public List<EstimateOMIHistory> getEstimateOMIHistory() {
        return estimateOMIHistory;
    }

    public void setEstimateOMIHistory(
            List<EstimateOMIHistory> estimateOMIHistory) {
        this.estimateOMIHistory = estimateOMIHistory;
    }

    public String getLastCommercialValue() {
        if (!ValidationHelper.isNullOrEmpty(getCommercialValueHistory())) {
            List<CommercialValueHistory> list = getCommercialValueHistory();

            list.sort(new CommercialValueHistoryComparator());

            return list.get(0).getCommercialValue();
        }

        return null;
    }

    public boolean isHasReplacedSubject() {
        return !ValidationHelper.isNullOrEmpty(getReplacedSubject())
                && !getReplacedSubject().getId().equals(getCurrentRequest().getSubject() != null
                ? getCurrentRequest().getSubject().getId() : null);
    }

    public List<CommercialValueHistory> getCommercialValueHistory() {
        return commercialValueHistory;
    }

    public void setCommercialValueHistory(
            List<CommercialValueHistory> commercialValueHistory) {
        this.commercialValueHistory = commercialValueHistory;
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

    public Long getRelationshipTypeId() {
        return relationshipTypeId;
    }

    public void setRelationshipTypeId(Long relationshipTypeId) {
        this.relationshipTypeId = relationshipTypeId;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public List<EstateSituation> getEstateSituationList() {
        if (estateSituationList == null) {
            if (!ValidationHelper.isNullOrEmpty(getSituationProperties())) {
                estateSituationList = getSituationProperties().stream()
                        .map(SituationProperty::getSituation).collect(Collectors.toList());
            } else {
                estateSituationList = new ArrayList<>();
            }
        }
        return estateSituationList;
    }

    private List<EstateSituation> getEstateSituationListWithoutInit() {
        return estateSituationList;
    }

    public void setEstateSituationList(List<EstateSituation> estateSituationList) {
        this.estateSituationList = estateSituationList;
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

    public List<Request> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<Request> requestList) {
        this.requestList = requestList;
    }

    public Long getMetersLand() {
        return metersLand;
    }

    public void setMetersLand(Long metersLand) {
        this.metersLand = metersLand;
    }

    public String getComment() {
        if (comment == null) {
            comment = ResourcesHelper.getString("propertyCommentDefaultValue");
        }
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Subject getReplacedSubject() {
        return replacedSubject;
    }

    public void setReplacedSubject(Subject replacedSubject) {
        this.replacedSubject = replacedSubject;
    }

    public Long getAggregationLandChargedRegistryId() {
        return aggregationLandChargedRegistryId;
    }

    public void setAggregationLandChargedRegistryId(Long aggregationLandChargedRegistryId) {
        this.aggregationLandChargedRegistryId = aggregationLandChargedRegistryId;
    }

    public Boolean getModified() {
        return modified;
    }

    public void setModified(Boolean modified) {
        this.modified = modified;
    }

    public int getNumberInFormalityGroup() {
        return numberInFormalityGroup;
    }

    public void setNumberInFormalityGroup(int numberInFormalityGroup) {
        this.numberInFormalityGroup = numberInFormalityGroup;
    }

    public Boolean getMatchByFields() {
        return matchByFields;
    }

    public void setMatchByFields(Boolean matchByFields) {
        this.matchByFields = matchByFields;
    }

    public OldProperty getOldProperty() {
        return oldProperty;
    }

    public void setOldProperty(OldProperty oldProperty) {
        this.oldProperty = oldProperty;
    }

    public List<Property> getOldPropertiesToView() {
        return oldPropertiesToView;
    }

    public void setOldPropertiesToView(List<Property> oldPropertiesToView) {
        this.oldPropertiesToView = oldPropertiesToView;
    }

    public List<SelectItem> getEditCities() {
        return editCities;
    }

    public void setEditCities(List<SelectItem> editCities) {
        this.editCities = editCities;
    }

    public Long getSelectedCityId() {
        return selectedCityId;
    }

    public void setSelectedCityId(Long selectedCityId) {
        this.selectedCityId = selectedCityId;
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

    public Request getCurrentRequest() {
        return currentRequest;
    }

    public void setCurrentRequest(Request currentRequest) {
        this.currentRequest = currentRequest;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public List<EstateSituationFormalityProperty> getEstateSituationFormalityPropertyList() {
        return estateSituationFormalityPropertyList;
    }

    public void setEstateSituationFormalityPropertyList(List<EstateSituationFormalityProperty> estateSituationFormalityPropertyList) {
        this.estateSituationFormalityPropertyList = estateSituationFormalityPropertyList;
    }

    public List<SituationProperty> getSituationProperties() {
        return situationProperties;
    }

    public void setSituationProperties(List<SituationProperty> situationProperties) {
        this.situationProperties = situationProperties;
    }

    public List<DatafromProperty> getDatafromProperties() {
        return datafromProperties;
    }

    public void setDatafromProperties(List<DatafromProperty> datafromProperties) {
        this.datafromProperties = datafromProperties;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public LandCadastralCulture getCadastralCulture() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if(ValidationHelper.isNullOrEmpty(cadastralCulture) && !ValidationHelper.isNullOrEmpty(getQuality())) {
            cadastralCulture = DaoManager.get(LandCadastralCulture.class, new Criterion[] {
                    Restrictions.eq("description", getQuality()).ignoreCase()
            });
        }
        return cadastralCulture;
    }
    public LandCulture getLandCulture() throws PersistenceBeanException, IllegalAccessException {
        return landCulture;
    }

    public void setLandCulture(LandCulture landCulture) {
        this.landCulture = landCulture;
    }

    public Boolean getLandOmiValueRelated() throws PersistenceBeanException, IllegalAccessException {
        landOmiValueRelated = Boolean.FALSE;
        if(!ValidationHelper.isNullOrEmpty(getQuality())) {
            List<LandCadastralCulture> landCadastralCultures = DaoManager.load(LandCadastralCulture.class,
                    new Criterion[]{Restrictions.eq("description", getQuality()).ignoreCase()
                    });
            if (!ValidationHelper.isNullOrEmpty(landCadastralCultures)) {
                LandCulture landCulture = landCadastralCultures.get(0).getLandCulture();
                if(!ValidationHelper.isNullOrEmpty(landCulture)){
                    List<LandOmiValue> landOmiValues = DaoManager.load(LandOmiValue.class,
                            new Criterion[]{Restrictions.eq("landCulture", landCulture)
                            });
                    if (!ValidationHelper.isNullOrEmpty(landOmiValues) && !ValidationHelper.isNullOrEmpty(getCity())) {
                        List<LandOmiValue> cityLandOmiValues = landOmiValues
                                .stream()
                                .filter(lov -> !ValidationHelper.isNullOrEmpty(lov.getLandOmi())
                                        && !ValidationHelper.isNullOrEmpty(lov.getLandOmi().getCities())
                                        && lov.getLandOmi().getCities().contains(getCity()))
                                .collect(Collectors.toList());
                        if (!ValidationHelper.isNullOrEmpty(cityLandOmiValues))
                            landOmiValueRelated = Boolean.TRUE;
                    }
                }
            }
        }
        return landOmiValueRelated;
    }

}
