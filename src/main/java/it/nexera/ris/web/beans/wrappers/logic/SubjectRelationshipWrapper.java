package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.PropertyTypeEnum;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Relationship;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class SubjectRelationshipWrapper implements Serializable {

    private static final long serialVersionUID = -7208484964647624503L;

    public transient final Log log = LogFactory
            .getLog(SubjectRelationshipWrapper.class);

    private Long id;

    private String surname;

    private String name;

    private Date birthDate;

    private City birthCity;

    private String fiscalCode;

    private String quote;

    private String quote1;

    private String quote2;

    private String propertyTypeStr;

    private PropertyTypeEnum propertyType;

    private List<SelectItem> propertyTypeList;

    private Relationship relationship;

    private Subject subject;

    private String businessName;

    public SubjectRelationshipWrapper(Relationship relationship, long id) {
        this.relationship = relationship;
        this.subject = relationship.getSubject();
        this.id = new Long(id);
        this.surname = relationship.getSubject().getSurname();
        this.name = relationship.getSubject().getName();
        this.birthDate = relationship.getSubject().getBirthDate();
        this.birthCity = relationship.getSubject().getBirthCity();
        this.fiscalCode = relationship.getSubject().getFiscalCode();
        this.businessName = relationship.getSubject().getBusinessName();
        if(!ValidationHelper.isNullOrEmpty(relationship.getQuote())) {
            this.quote = relationship.getQuote();
            String[] quoteMas = relationship.getQuote().split("/");
            this.quote1 = quoteMas[0];
            this.quote2 = quoteMas[1];
        }
        this.propertyTypeStr = relationship.getPropertyType();
        this.propertyType = PropertyTypeEnum.getByDescription(relationship.getPropertyType());
        this.propertyTypeList = ComboboxHelper.fillList(PropertyTypeEnum.values(),
                false, false);
    }

    public SubjectRelationshipWrapper(long id) {
        this.relationship = new Relationship();
        this.subject = new Subject();
        this.id = new Long(id);
        this.propertyTypeList = ComboboxHelper.fillList(PropertyTypeEnum.values(),
                false, false);
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public City getBirthCity() {
        return birthCity;
    }

    public void setBirthCity(City birthCity) {
        this.birthCity = birthCity;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public String getQuote1() {
        return quote1;
    }

    public void setQuote1(String quote1) {
        this.quote1 = quote1;
    }

    public String getQuote2() {
        return quote2;
    }

    public void setQuote2(String quote2) {
        this.quote2 = quote2;
    }

    public PropertyTypeEnum getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyTypeEnum propertyType) {
        this.propertyType = propertyType;
    }

    public List<SelectItem> getPropertyTypeList() {
        return propertyTypeList;
    }

    public void setPropertyTypeList(List<SelectItem> propertyTypeList) {
        this.propertyTypeList = propertyTypeList;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
        this.surname = subject.getSurname();
        this.name = subject.getName();
        this.birthDate = subject.getBirthDate();
        this.birthCity = subject.getBirthCity();
        this.fiscalCode = subject.getFiscalCode();
        this.businessName = subject.getBusinessName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getPropertyTypeStr() {
        return propertyTypeStr;
    }

    public void setPropertyTypeStr(String propertyTypeStr) {
        this.propertyTypeStr = propertyTypeStr;
    }
}
