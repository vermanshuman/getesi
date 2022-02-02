package it.nexera.ris.common.xml.wrappers.importXLSX;

import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubjectXLSXWrapper {

    private String type;

    private String businessName;

    private String birthCity;

    private String firstName;

    private String cfisCity;

    private String lastName;

    private String fiscalCode;

    private Date BirthDate;

    public Subject toEntity(Session session) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        City city = getCity(session);
        List<Subject> subjects = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getType())) {
            if (getType().equalsIgnoreCase("G")) {
                setFiscalCode(ValidationHelper.validateNumberVat(getFiscalCode()));
                subjects = ConnectionManager.load(Subject.class, new CriteriaAlias[]{
                        new CriteriaAlias("birthCity", "city", JoinType.LEFT_OUTER_JOIN)
                }, new Criterion[]{
                        getRestrictionBusinessName(),
                        Restrictions.eq("numberVAT", getFiscalCode()),
                        getRestrictionCity(city)
                }, session);
            } else {
                subjects = ConnectionManager.load(Subject.class, new CriteriaAlias[]{
                        new CriteriaAlias("birthCity", "city", JoinType.LEFT_OUTER_JOIN)
                }, new Criterion[]{
                        Restrictions.eq("name", getFirstName()),
                        (getLastName() == null ? Restrictions.isNotNull("surname") :
                                Restrictions.eq("surname", getLastName())),
                        Restrictions.eq("fiscalCode", getFiscalCode()),
                        getRestrictionCity(city),
                        SexTypes.getByShortValue(getType()) == null ? Restrictions.isNull("sex") :
                                Restrictions.eq("sex", SexTypes.getByShortValue(getType()).getId())
                }, session);
            }
        }
        Subject subject = null;
        if (!ValidationHelper.isNullOrEmpty(subjects)) {
            subject = subjects.get(0);
        }
        if (subject == null) {
            subject = new Subject();
            if (!ValidationHelper.isNullOrEmpty(getType()) && getType().equalsIgnoreCase("G")) {
                subject.setTypeId(SubjectType.LEGAL_PERSON.getId());
                subject.setBusinessName(getBusinessName());
                subject.setNumberVAT(getFiscalCode());
                subject.setFiscalCode(getFiscalCode());

            } else if (!ValidationHelper.isNullOrEmpty(getType())) {
                subject.setTypeId(SubjectType.PHYSICAL_PERSON.getId());
                subject.setName(getFirstName());
                subject.setSurname(getLastName());
                subject.setFiscalCode(getFiscalCode());
                subject.setNumberVAT(getFiscalCode());
                if (!ValidationHelper.isNullOrEmpty(getType())) {
                    subject.setSex(SexTypes.getByShortValue(getType()).getId());
                }
                subject.setBirthDate(getBirthDate());

            }
        }
        if (!ValidationHelper.isNullOrEmpty(getType())) {
            setBirthCityAndProvince(subject, city);
        }
        return subject;
    }

    private City getCity(Session session) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        City city = null;
        Criterion restriction = null;
        List<City> cities = null;
        if (getBirthCity() != null && getCfisCity() != null) {
            restriction = Restrictions.and(Restrictions.eq("description", getBirthCity()), Restrictions.eq("cfis",
                    getCfisCity()));
        } else if (getBirthCity() != null) {
            List<City> existsCity = ConnectionManager.load(City.class, new Criterion[]{
                    Restrictions.eq("description", getBirthCity())}, session);
            if (!ValidationHelper.isNullOrEmpty(existsCity)) {
                restriction = Restrictions.eq("description", getBirthCity());
            }
        } else if (getCfisCity() != null) {
            restriction = Restrictions.eq("cfis", getCfisCity());
        }
        if (restriction != null) {
            cities = ConnectionManager.load(City.class, new Criterion[]{
                    restriction
            }, session);
        }
        if (!ValidationHelper.isNullOrEmpty(cities)) {
            city = cities.get(0);
        }
        return city;
    }

    private void setBirthCityAndProvince(Subject subject, City city) {
        if (city == null) {
            return;
        }
        if (subject.getBirthCity() == null) {
            subject.setBirthCity(city);
        }
        if (subject.getBirthProvince() == null) {
            subject.setBirthProvince(city.getProvince());
        }
    }

    private Criterion getRestrictionCity(City city) {
        if (!ValidationHelper.isNullOrEmpty(city)) {
            return Restrictions.eq("city.description", city.getDescription()).ignoreCase();
        } else {
            return Restrictions.isNull("birthCity");
        }
    }

    private Criterion getRestrictionBusinessName() {
        if (!ValidationHelper.isNullOrEmpty(getBusinessName()) && getBusinessName().contains(".")) {
            return Restrictions.or(Restrictions.eq("businessName", getBusinessName()).ignoreCase(),
                    Restrictions.eq("businessName", getBusinessName().replaceAll("\\.", "")).ignoreCase());
        }
        return Restrictions.eq("businessName", getBusinessName()).ignoreCase();
    }

    private List<SectionCXLSXWrapper> sectionCList;

    public List<SectionCXLSXWrapper> getSectionCList() {
        return sectionCList;
    }

    public void setSectionCList(List<SectionCXLSXWrapper> sectionCList) {
        this.sectionCList = sectionCList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBirthCity() {
        return birthCity;
    }

    public void setBirthCity(String birthCity) {
        this.birthCity = birthCity;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public String getCfisCity() {
        return cfisCity;
    }

    public void setCfisCity(String cfisCity) {
        this.cfisCity = cfisCity;
    }

    public Date getBirthDate() {
        return BirthDate;
    }

    public void setBirthDate(Date birthDate) {
        BirthDate = birthDate;
    }
}
