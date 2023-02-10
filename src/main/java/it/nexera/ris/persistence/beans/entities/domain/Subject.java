package it.nexera.ris.persistence.beans.entities.domain;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Formula;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.enums.SubjectXMLElements;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.interfaces.BeforeSave;
import it.nexera.ris.web.beans.pages.FormalityCreateBean;
import it.nexera.ris.web.beans.wrappers.logic.RelationshipGroupingWrapper;

@Entity
@Table(name = "subject")
public class Subject extends IndexedEntity implements BeforeSave{
    public transient final Log log = LogFactory.getLog(getClass());

    private static final long serialVersionUID = 6933140712000620300L;

    @Column(name = "first_name")
    private String name;

    @Column(name = "last_name")
    private String surname;

    @Column(name = "birth_date")
    private Date birthDate;

    @ManyToOne
    @JoinColumn(name = "birth_province_id")
    private Province birthProvince;

    @ManyToOne
    @JoinColumn(name = "birth_city_id")
    private City birthCity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(name = "sex")
    private Long sex;

    @Column(name = "born_in_foreign_state")
    private Boolean bornInForeignState;

    @Column(name = "foreign_country")
    private Boolean foreignCountry;

    @Column(name = "foreign_state")
    private String foreignState;

    @Column(name = "fiscal_code")
    private String fiscalCode;

    //enum SubjectType
    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "number_vat")
    private String numberVAT;

    @Column(name = "elected_mortgage_home")
    private String electedMortgageHome;

    @Column(name = "incomplete", columnDefinition = "NUMERIC(1) DEFAULT 0")
    private boolean incomplete;

    @ManyToMany(mappedBy = "subject")
    private List<SectionC> sectionC;

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private List<Request> requestList;

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private List<Relationship> relationshipList;

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private List<DocumentSubject> documentSubjectList;

    @ManyToMany
    @JoinTable(name = "subject_formality_external", joinColumns = {
            @JoinColumn(name = "subject_id", table = "subject")
    }, inverseJoinColumns = {
            @JoinColumn(name = "formality_id", table = "formality")
    })
    private List<Formality> formalityExternalList;

//    @ManyToMany(mappedBy = "subjectList")
//    private List<Request> manyRequestList;
    
    @OneToMany(mappedBy = "subject", cascade = CascadeType.REFRESH)
    private List<RequestSubject> requestSubjects;
    
    @Column(name = "old_number_vat")
    private String oldNumberVAT;

    @Formula(value = "concat(last_name,' ', first_name)")
    private String completeName;

    @Transient
    private Formality tempFormality;

    @Transient
    private Long tempId;

    @Transient
    private String quote;

    @Transient
    private String typeReport;

    @Transient
    private String cityDesc;

    @Transient
    private int numberInFormalityGroup;

    @Transient
    private Long selectProvinceId;

    @Transient
    private Long selectedCityId;

    @Transient
    private Long selectedNationId;

    @Transient
    private Long selectedJuridicalNationId;

    @Transient
    private Long selectedSexTypeId;

    @Transient
    private List<Relationship> relationshipsToView;

    @Transient
    private List<Request> manyRequestList;
    
    @Override
    public String toString() {
        return String.format("%s %s", getFullName(),
                getFiscalCode() == null ? "" : getFiscalCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subject subject = (Subject) o;

        if (incomplete != subject.incomplete) return false;
        if (numberInFormalityGroup != subject.numberInFormalityGroup) return false;
        if (name != null ? !name.equals(subject.name) : subject.name != null) return false;
        if (surname != null ? !surname.equals(subject.surname) : subject.surname != null) return false;
        if (birthDate != null ? !birthDate.equals(subject.birthDate) : subject.birthDate != null) return false;
        if (birthProvince != null ? !birthProvince.equals(subject.birthProvince) : subject.birthProvince != null)
            return false;
        if (birthCity != null ? !birthCity.equals(subject.birthCity) : subject.birthCity != null) return false;
        if (country != null ? !country.equals(subject.country) : subject.country != null) return false;
        if (sex != null ? !sex.equals(subject.sex) : subject.sex != null) return false;
        if (bornInForeignState != null ? !bornInForeignState.equals(subject.bornInForeignState) : subject.bornInForeignState != null)
            return false;
        if (foreignCountry != null ? !foreignCountry.equals(subject.foreignCountry) : subject.foreignCountry != null)
            return false;
        if (foreignState != null ? !foreignState.equals(subject.foreignState) : subject.foreignState != null)
            return false;
        if (fiscalCode != null ? !fiscalCode.equals(subject.fiscalCode) : subject.fiscalCode != null) return false;
        if (typeId != null ? !typeId.equals(subject.typeId) : subject.typeId != null) return false;
        if (businessName != null ? !businessName.equals(subject.businessName) : subject.businessName != null)
            return false;
        if (numberVAT != null ? !numberVAT.equals(subject.numberVAT) : subject.numberVAT != null) return false;
        if (oldNumberVAT != null ? !oldNumberVAT.equals(subject.oldNumberVAT) : subject.oldNumberVAT != null) return false;
        if (electedMortgageHome != null ? !electedMortgageHome.equals(subject.electedMortgageHome) : subject.electedMortgageHome != null)
            return false;
        if (sectionC != null ? !sectionC.equals(subject.sectionC) : subject.sectionC != null) return false;
        if (requestList != null ? !requestList.equals(subject.requestList) : subject.requestList != null) return false;
        if (relationshipList != null ? !relationshipList.equals(subject.relationshipList) : subject.relationshipList != null)
            return false;
        if (documentSubjectList != null ? !documentSubjectList.equals(subject.documentSubjectList) : subject.documentSubjectList != null)
            return false;
        if (tempFormality != null ? !tempFormality.equals(subject.tempFormality) : subject.tempFormality != null)
            return false;
        if (tempId != null ? !tempId.equals(subject.tempId) : subject.tempId != null) return false;
        if (quote != null ? !quote.equals(subject.quote) : subject.quote != null) return false;
        if (typeReport != null ? !typeReport.equals(subject.typeReport) : subject.typeReport != null) return false;
        return cityDesc != null ? cityDesc.equals(subject.cityDesc) : subject.cityDesc == null;
    }

    public String toFormalityTableString() {
        if (getTypeIsPhysicalPerson()) {
            return String.format("<b>%s %s</b>%s", getSurname().toUpperCase(), getName().toUpperCase(), 
                    !ValidationHelper.isNullOrEmpty(getBirthCity()) ? getBirthDateAndCity(): getBirthDateAndCountry());
        } else {
            return String.format("<b>%s</b> con sede in %s partita IVA %s", getBusinessName().toUpperCase(),
                    getBirthCity() == null ? (getCountry() != null ? getCountry().getCamelCountryDescription() : ""): getBirthCity().getCamelCityDescription(), getNumberVAT());
        }
    }

    public String toFormalityTableAttachmentCString() {
        if (getTypeIsPhysicalPerson()) {
            return String.format("<b>%s %s</b>", getSurname().toUpperCase(), getName().toUpperCase());
        } else {
            return String.format("<b>%s</b>", getBusinessName().toUpperCase());
        }
    }


    public String toFormalityTableDebitore() {
        return String.format("%s %s %s %s", getSurname().toUpperCase(), getName().toUpperCase(),
                getBirthCity() != null ? WordUtils.capitalizeFully(getBirthCity().getDescription()) : "",
                        getBirthDate() != null ? DateTimeHelper.toStringDateWithDots(getBirthDate()) : "");
    }

    private String getBirthDateAndCity() {
        if (getBirthDate() == null && getBirthCity() == null) {
            return "";
        } else {
            return String.format(" (%s %s)",
                    getBirthCity() != null ? WordUtils.capitalizeFully(getBirthCity().getDescription()) : "",
                            getBirthDate() != null ? DateTimeHelper.toStringDateWithDots(getBirthDate()) : "");
        }
    }
    
    private String getBirthDateAndCountry() {
        if (getBirthDate() == null && getCountry() == null) {
            return "";
        } else {
            return String.format(" (%s %s)",
                    getCountry() != null ? WordUtils.capitalizeFully(getCountry().getDescription()) : "",
                            getBirthDate() != null ? DateTimeHelper.toStringDateWithDots(getBirthDate()) : "");
        }
    }

    public String getFiscalCodeVATNamber() {
        if (this.getTypeIsPhysicalPerson()) {
            return this.getFiscalCode();
        } else {
            return this.getNumberVAT();
        }
    }

    public String getSectionCStr() {
        if (getTypeIsPhysicalPerson()) {
            String city = "";
            if(!ValidationHelper.isNullOrEmpty(getBirthCity())) {
                city = getBirthCity().getDescription();
            } else if (!ValidationHelper.isNullOrEmpty(getCountry())) {
                city = getCountry().getDescription();
            }
            return String.format("%s %s (%s %s)", getSurname(), getName(),
                    WordUtils.capitalizeFully(city),
                    DateTimeHelper.toStringDateWithDots(getBirthDate()));
        } else {
            return String.format("%s con sede in %s partita Iva n. %s", getBusinessName(),
                    getBirthCity() != null ? getBirthCity().getDescription() : "", getNumberVAT());
        }
    }

    public String getFullName() {
        if (getTypeIsPhysicalPerson()) {
            return String.format("%s %s", getSurname(), getName());
        } else if (SubjectType.LEGAL_PERSON.getId().equals(getTypeId())) {
            return getBusinessName();
        }

        return "";
    }

    public String getFullNameCapitalize() {
        if (getTypeIsPhysicalPerson()) {
            return String.format("%s %s", WordUtils.capitalizeFully(getSurname()), WordUtils.capitalizeFully(getName()));
        } else {
            return getBusinessName();
        }
    }

    public Long numberOfProperties() {
        return getRelationshipList().stream().map(Relationship::getProperty).distinct().count();
    }

    public String getNameBirthDateCity() {
        if (getTypeId() == null || getTypeId() == 1l) {
            return String.format("%s %s (%s %s)", getSurname(), getName(),
                    getBirthCityDescription(),
                    DateTimeHelper.toString(getBirthDate()));
        } else if (getTypeId() == 2) {
            return String.format("%s (%s)", getBusinessName(),
                    getBirthCityDescription());
        }

        return "";
    }

    public String getNameBirthDateUpperCase() {
        String description = "";
        if(getBirthCity() == null && getCountry() != null)
            description = WordUtils.capitalizeFully(getCountry().getDescription());
        else if(getBirthCity() != null ){
            if (getTypeIsPhysicalPerson())
                description = WordUtils.capitalizeFully(getBirthCityDescription());
            else
                description = getBirthCity().getCamelCityDescription();
        }
        if (getTypeIsPhysicalPerson()) {
            return String.format("<b>%s %s</b> (%s %s)", getSurname().toUpperCase(), getName().toUpperCase(),
                    description, DateTimeHelper.toString(getBirthDate()));
        } else {
            return String.format(ResourcesHelper.getString("alienatedTableGiur"),
                    getBusinessName(), description,getNumberVAT());
        }
    }

    public String getNameBirthDateUpperCaseBold() {
        if (getTypeIsPhysicalPerson()) {
            return String.format("<b>%s %s</b> (%s %s)", getSurname().toUpperCase(), getName().toUpperCase(),
                    WordUtils.capitalizeFully(getBirthCityDescription()),
                    DateTimeHelper.toString(getBirthDate()));
        } else {
            return String.format("<b>%s </b>(%s %s)", getBusinessName(),
                    WordUtils.capitalizeFully(getBirthCityDescription()),
                    DateTimeHelper.toString(getBirthDate()));
        }
    }

    public String getNameBirthCity() {
        String result = "";
        if (!ValidationHelper.isNullOrEmpty(getBirthCityDescription())) {
            result += getBirthCityDescription();
            if (!ValidationHelper.isNullOrEmpty(getBirthProvinceDescription())) {
                result += " (" + getBirthProvinceDescription() + ")";
            }
        } else if (!ValidationHelper.isNullOrEmpty(this.getCountry())) {
            result += getCountry().getDescription();
        }
        return result;
    }

    public String getBirthCityDescription() {
        if (getBirthCity() != null && getBirthCity().getDescription() != null) {
            return getBirthCity().getDescription();
        } else {
            return null;
        }
    }

    public String getBirthProvinceDescription() {
        if (getBirthProvince() != null
                && getBirthProvince().getDescription() != null) {
            return getBirthProvince().getDescription();
        } else {
            return null;
        }
    }

    public String getTypeTitle() {
        if (getTypeId() == null) {
            return "";
        } else if (getTypeId() == 1l) {
            return ResourcesHelper
                    .getEnum("documentGenerationTagsSUBJECT_TYPE1");
        } else if (getTypeId() == 2) {
            return ResourcesHelper
                    .getEnum("documentGenerationTagsSUBJECT_TYPE2");
        }
        return "";
    }

    public Boolean getTypeIsPhysicalPerson() {
        return getTypeId() != null ? SubjectType.PHYSICAL_PERSON.getId().equals(getTypeId())
                : ValidationHelper.isNullOrEmpty(getBusinessName());
    }

    public SexTypes getSexType() {
        return SexTypes.getById(getSex());
    }

    public String getBargainingUnitStr(String key) throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getTempFormality())) {
            List<Relationship> relationshipList = DaoManager.load(Relationship.class, new Criterion[]{
                    Restrictions.eq("subject.id", getId()),
                    Restrictions.eq("formality.id", getTempFormality().getId()),
                    Restrictions.eq("sectionCType", key)
            });
            if (!ValidationHelper.isNullOrEmpty(relationshipList)) {

                List<Relationship> intermediateResult = getGroupedRelationships(relationshipList);

                String format = "<nobr> &nbsp;&nbsp;Relativamente all'unit\u00E0 negoziale n. %s &#x2001;&#x2001;Per il diritto di %s &#x2001;&#x2001;<br/>&nbsp;&nbsp;Per la quota di %s %s";
                List<RelationshipGroupingWrapper> output = new LinkedList<>();
                for (Relationship r : intermediateResult) {
                    output.add(new RelationshipGroupingWrapper(r));
                }
                Comparator<RelationshipGroupingWrapper> comparator = Comparator.comparing(RelationshipGroupingWrapper::getUnitaNeg);
                comparator = comparator.thenComparing(RelationshipGroupingWrapper::getPropertyType)
                        .thenComparing(RelationshipGroupingWrapper::getRegime);

                return output.stream().distinct().sorted(comparator).map(out -> String.format(format, out.getUnitaNeg(),
                        out.getPropertyType(), out.getQuote(),
                        out.getRegime() == null ? "" : "&#x2001;&#x2001;In regime di " + out.getRegime()))
                        .collect(Collectors.joining("</nobr><br/>"));
            }
        }
        return "";
    }

    private List<Relationship> getGroupedRelationships(List<Relationship> relationshipList) {
        List<Relationship> intermediateResult = new ArrayList<>();

        Map<String, Map<String, List<Relationship>>> collect = relationshipList.stream()
                .collect(Collectors.groupingBy(Relationship::getQuote,
                        Collectors.groupingBy(Relationship::getUnitaNeg)));

        for (Map.Entry<String, Map<String, List<Relationship>>> stringMapEntry : collect.entrySet()) {
            for (Map.Entry<String, List<Relationship>> stringListEntry : stringMapEntry.getValue().entrySet()) {
                intermediateResult.add(stringListEntry.getValue().get(0));
            }
        }
        return intermediateResult;
    }

    public String getFullDescriptionByEnumLine() {
        return getFullDescriptionByEnum(" ");
    }

    public String getFullDescriptionByEnum() {
        return getFullDescriptionByEnum("<br/>");
    }

    public String getFullDescriptionByEnum(String delimer) {
        StringJoiner joiner = new StringJoiner(delimer, delimer, delimer);
        if (!ValidationHelper.isNullOrEmpty(getSex()))
            joiner.add(SubjectXMLElements.SEX.getElement().replaceAll("\\w+\\.", "")
                    + " " + SexTypes.getById(getSex()).getShortValue());
        if (!ValidationHelper.isNullOrEmpty(getSurname()))
            joiner.add(SubjectXMLElements.LAST_NAME.getElement().replaceAll("\\w+\\.", "")
                    + " " + getSurname());
        if (!ValidationHelper.isNullOrEmpty(getName()))
            joiner.add(SubjectXMLElements.FIRST_NAME.getElement().replaceAll("\\w+\\.", "")
                    + " " + getName());
        if (!ValidationHelper.isNullOrEmpty(getBirthCityDescription()))
            joiner.add(SubjectXMLElements.BIRTH_CITY_DESCRIPTION.getElement().replaceAll("\\w+\\.", "")
                    + " " + getBirthCityDescription());
        if (!ValidationHelper.isNullOrEmpty(getBirthDate()))
            joiner.add(SubjectXMLElements.BIRTH_DATE.getElement().replaceAll("\\w+\\.", "")
                    + " " + DateTimeHelper.toStringDateWithDots(getBirthDate()));
        if (!ValidationHelper.isNullOrEmpty(getFiscalCode()))
            joiner.add(SubjectXMLElements.FISCAL_CODE.getElement().replaceAll("\\w+\\.", "")
                    + " " + getFiscalCode());
        if (!ValidationHelper.isNullOrEmpty(getNumberVAT()))
            joiner.add(SubjectXMLElements.NUMBER_VAT.getElement().replaceAll("\\w+\\.", "")
                    + " " + getNumberVAT());
        if (!ValidationHelper.isNullOrEmpty(getBornInForeignState()))
            joiner.add(SubjectXMLElements.BORN_IN_FOREIGN_STATE.getElement().replaceAll("\\w+\\.", "")
                    + " " + getBornInForeignState());
        if (!ValidationHelper.isNullOrEmpty(getBusinessName()))
            joiner.add(SubjectXMLElements.BUSINESS_NAME.getElement().replaceAll("\\w+\\.", "")
                    + " " + getBusinessName());
        if (!ValidationHelper.isNullOrEmpty(getElectedMortgageHome()))
            joiner.add(SubjectXMLElements.ELECTED_MORTGAGE_HOME.getElement().replaceAll("\\w+\\.", "")
                    + " " + getElectedMortgageHome());
        if (!ValidationHelper.isNullOrEmpty(getForeignState()))
            joiner.add(SubjectXMLElements.FOREIGN_STATE.getElement().replaceAll("\\w+\\.", "")
                    + " " + getForeignState());
        return joiner.toString();
    }


    public List<String> getExpansionText() throws PersistenceBeanException, IllegalAccessException {
        List<String> result = new LinkedList<>();
        if (!ValidationHelper.isNullOrEmpty(this.getRelationshipList())) {
            Long formalityRelatedId = getParameterIdRelatedFormality();
            List<Relationship> filteredRelationships = this.getRelationshipList();
            if(!ValidationHelper.isNullOrEmpty(formalityRelatedId)) {
                filteredRelationships = this.getRelationshipList().stream()
                        .filter(x -> ((x.getFormality() == null)
                                || ((x.getFormality().getId() != null) && (formalityRelatedId.equals(x.getFormality().getId())))))
                        .collect(Collectors.toList());
            }
            for (Relationship relationship : filteredRelationships) {
                result.add("Relativamente all'unit\u00E0 negoziale n. " + relationship.getUnitaNeg()
                + " per il diritto di " + relationship.getPropertyType()
                + " per la quota di " + relationship.getQuote() + " in regime di "
                + relationship.getRegime());
            }
        }
        return result;
    }

    public void updateRelationshipsToView() throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getRelationshipList())) {
            Long formalityRelatedId = getParameterIdRelatedFormality();
            List<Relationship> filteredRelationships = this.getRelationshipList();
            if(!ValidationHelper.isNullOrEmpty(formalityRelatedId)) {
                filteredRelationships = this.getRelationshipList().stream()
                        .filter(x -> ((x.getId() == null)
                                || ((x.getFormality() != null) && (formalityRelatedId.equals(x.getFormality().getId())))))
                        .collect(Collectors.toList());
            } else {
                filteredRelationships = this.getRelationshipList().stream()
                        .filter(x -> ((x.getId() == null))).collect(Collectors.toList());
            }
            List<Relationship> intermediateResult = getGroupedRelationships(filteredRelationships);

            for (Relationship relationship : intermediateResult) {
                relationship.setText("Relativamente all'unit\u00E0 negoziale n. " + relationship.getUnitaNeg()
                + " per il diritto di " + relationship.getPropertyType()
                + " per la quota di " + relationship.getQuote() + " in regime di "
                + relationship.getRegime());
            }
            setRelationshipsToView(intermediateResult);

        }
    }

    private Long getParameterIdRelatedFormality() {

        Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();
        FormalityCreateBean formalityCreateBean = (FormalityCreateBean) viewMap.get("formalityCreateBean");
        if(!ValidationHelper.isNullOrEmpty(formalityCreateBean.getFormalityId())) {
            return Long.valueOf(formalityCreateBean.getFormalityId());
        }
        return null;
    }

    public String getExpansionTextStr() throws PersistenceBeanException, IllegalAccessException {
        return getExpansionText().stream().collect(Collectors.joining("<br/>"));
    }
    

    @Override
    public void beforeSave() {
        if (!Hibernate.isInitialized(getRequestSubjects()) 
                || getRequestSubjects() == null) {
            return;
        }
        List<RequestSubject> listToRemove = new ArrayList<>();
        List<RequestSubject> listToAdd = new ArrayList<>();
        emptyIfNull(getRequestSubjects()).stream()
        .filter(f -> emptyIfNull(getManyRequestList()).stream().noneMatch(r -> r.getId().equals(f.getRequest().getId())))
        .forEach(r -> {
                    DaoManager.removeWeak(r, false);
                    listToRemove.add(r);
                }
        );
        getRequestSubjects().removeAll(listToRemove);
        emptyIfNull(getManyRequestList()).stream()
        .filter(f -> emptyIfNull(getRequestSubjects()).stream().noneMatch(r -> r.getRequest().getId().equals(f.getId())))
        .forEach(r -> {
                    RequestSubject requestSubject = new RequestSubject(r, this);
                    DaoManager.saveWeak(requestSubject, false);
                    listToAdd.add(requestSubject);
                }
        );
        getRequestSubjects().addAll(listToAdd);
    }
    
    public void reloadRequestSubjects() throws PersistenceBeanException, IllegalAccessException {
        setRequestSubjects(DaoManager.load(RequestSubject.class, new Criterion[]{
                Restrictions.eq("subject.id", this.getId())
        }));
    }

    public String getName() {
        return name;
    }

    public String getNameUpper() {
        return name == null ? "" : name.toUpperCase();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSurnameUpper() {
        return surname == null ? "" : surname.toUpperCase();
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Province getBirthProvince() {
        return birthProvince;
    }

    public void setBirthProvince(Province birthProvince) {
        this.birthProvince = birthProvince;
    }

    public City getBirthCity() {
        return birthCity;
    }

    public void setBirthCity(City birthCity) {
        this.birthCity = birthCity;
    }

    public Long getSex() {
        return sex;
    }

    public void setSex(Long sex) {
        this.sex = sex;
    }

    public Boolean getBornInForeignState() {
        return bornInForeignState;
    }

    public void setBornInForeignState(Boolean bornInForeignState) {
        this.bornInForeignState = bornInForeignState;
    }

    public String getForeignState() {
        return foreignState;
    }

    public void setForeignState(String foreignState) {
        this.foreignState = foreignState;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getNumberVAT() {
        return numberVAT;
    }

    public void setNumberVAT(String numberVAT) {
        this.numberVAT = numberVAT;
    }

    public String getElectedMortgageHome() {
        return electedMortgageHome;
    }

    public void setElectedMortgageHome(String electedMortgageHome) {
        this.electedMortgageHome = electedMortgageHome;
    }

    public List<SectionC> getSectionC() {
        if (!Hibernate.isInitialized(sectionC)) {
            try {
                sectionC = DaoManager.load(SectionC.class, new CriteriaAlias[]{
                        new CriteriaAlias("subject", "sub", JoinType.LEFT_OUTER_JOIN)
                }, new Criterion[]{
                        Restrictions.eq("sub.id", this.getId())
                });
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }
        }
        return sectionC;
    }

    public List<SectionC> getSectionCSimple() {
        return sectionC;
    }


    public void setSectionC(List<SectionC> sectionC) {
        this.sectionC = sectionC;
    }

    public Long getTempId() {
        return tempId;
    }

    public void setTempId(Long tempId) {
        this.tempId = tempId;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Boolean getForeignCountry() {
        return foreignCountry;
    }

    public boolean getForeignCountryExist(){
        return getForeignCountry() != null && getForeignCountry();
    }

    public void setForeignCountry(Boolean foreignCountry) {
        this.foreignCountry = foreignCountry;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getTypeReport() {
        return typeReport;
    }

    public void setTypeReport(String typeReport) {
        this.typeReport = typeReport;
    }

    public List<Relationship> getRelationshipList() {
        return relationshipList;
    }

    public void setRelationshipList(List<Relationship> relationshipList) {
        this.relationshipList = relationshipList;
    }

    public List<Request> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<Request> requestList) {
        this.requestList = requestList;
    }

    public Formality getTempFormality() {
        return tempFormality;
    }

    public void setTempFormality(Formality tempFormality) {
        this.tempFormality = tempFormality;
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    public void setIncomplete(boolean incomplete) {
        this.incomplete = incomplete;
    }

    public List<DocumentSubject> getDocumentSubjectList() {
        return documentSubjectList;
    }

    public void setDocumentSubjectList(List<DocumentSubject> documentSubjectList) {
        this.documentSubjectList = documentSubjectList;
    }

    public String getCityDesc() {
        return cityDesc;
    }

    public void setCityDesc(String cityDesc) {
        this.cityDesc = cityDesc;
    }

    public int getNumberInFormalityGroup() {
        return numberInFormalityGroup;
    }

    public void setNumberInFormalityGroup(int numberInFormalityGroup) {
        this.numberInFormalityGroup = numberInFormalityGroup;
    }

    public Long getSelectProvinceId() {
        if (!ValidationHelper.isNullOrEmpty(this.selectProvinceId)) {
            return this.selectProvinceId;
        }
        if (!ValidationHelper.isNullOrEmpty(this.getBirthProvince())) {
            return this.getBirthProvince().getId();
        }
        return selectProvinceId;
    }

    public void setSelectProvinceId(Long selectProvinceId) {
        this.selectProvinceId = selectProvinceId;
    }

    public Long getSelectedCityId() {
        if (!ValidationHelper.isNullOrEmpty(this.selectedCityId)) {
            return this.selectedCityId;
        }
        if (!ValidationHelper.isNullOrEmpty(this.getBirthCity())) {
            return this.getBirthCity().getId();
        }
        return selectedCityId;
    }

    public void setSelectedCityId(Long selectedCityId) {
        this.selectedCityId = selectedCityId;
    }

    public Long getSelectedNationId() {
        if (!ValidationHelper.isNullOrEmpty(this.getCountry())) {
            return this.getCountry().getId();
        }
        return selectedNationId;
    }

    public void setSelectedNationId(Long selectedNationId) {
        this.selectedNationId = selectedNationId;
    }

    public Long getSelectedJuridicalNationId() {
        if (!ValidationHelper.isNullOrEmpty(this.getCountry())) {
            return this.getCountry().getId();
        }
        return selectedJuridicalNationId;
    }

    public void setSelectedJuridicalNationId(Long selectedJuridicalNationId) {
        this.selectedJuridicalNationId = selectedJuridicalNationId;
    }

    public Long getSelectedSexTypeId() {
        return selectedSexTypeId;
    }

    public void setSelectedSexTypeId(Long selectedSexTypeId) {
        this.selectedSexTypeId = selectedSexTypeId;
    }

    public List<Formality> getFormalityExternalList() {
        return formalityExternalList;
    }

    public void setFormalityExternalList(List<Formality> formalityExternalList) {
        this.formalityExternalList = formalityExternalList;
    }

    public List<Relationship> getRelationshipsToView() {
        if(ValidationHelper.isNullOrEmpty(relationshipsToView)) {
            try {
                updateRelationshipsToView();
            } catch (PersistenceBeanException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return relationshipsToView;
    }

    public void setRelationshipsToView(List<Relationship> relationshipsToView) {
        this.relationshipsToView = relationshipsToView;
    }

    public List<Request> getManyRequestList() {
        if (manyRequestList == null) {
            manyRequestList = emptyIfNull(getRequestSubjects())
                    .stream()
                    .map(RequestSubject::getRequest).collect(Collectors.toList());
        }
        return manyRequestList;
    }

    public void setManyRequestList(List<Request> manyRequestList) {
        this.manyRequestList = manyRequestList;
    }

    public String getOldNumberVAT() {
        return oldNumberVAT;
    }

    public void setOldNumberVAT(String oldNumberVAT) {
        this.oldNumberVAT = oldNumberVAT;
    }

    public List<RequestSubject> getRequestSubjects() {
        return requestSubjects;
    }

    public void setRequestSubjects(List<RequestSubject> requestSubjects) {
        this.requestSubjects = requestSubjects;
    }

    public String getCompleteName() {
        return completeName;
    }

    public void setCompleteName(String completeName) {
        this.completeName = completeName;
    }
}
