package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.RequestHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.Date;
import java.util.List;

public class SubjectXMLWrapper extends BaseXMLWrapper<Subject> {

    private String name;

    private String surname;

    private Date birthDate;

    private String cityDescription;

    private String cityCode;

    private Long sex;

    private Boolean bornInForeignState;

    private String foreignState;

    private String fiscalCode;

    private Long typeId;

    private String businessName;

    private String numberVAT;

    private String electedMortgageHome;

    @Override
    public Subject toEntity() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return toEntity(DaoManager.getSession());
    }

    public Subject toEntity(Session session) throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        Subject subject = new Subject();

        subject.setId(getId());
        subject.setCreateUserId(getCreateUserId());
        subject.setUpdateUserId(getUpdateUserId());
        subject.setCreateDate(getCreateDate());
        subject.setUpdateDate(getUpdateDate());
        subject.setVersion(getVersion());
        subject.setBirthDate(getBirthDate());
        subject.setBornInForeignState(getBornInForeignState());
        subject.setBusinessName(getBusinessName());
        subject.setElectedMortgageHome(getElectedMortgageHome());
        subject.setFiscalCode(getFiscalCode());
        subject.setForeignState(getForeignState());
        subject.setName(getName());
        subject.setNumberVAT(getNumberVAT());
        subject.setSex(getSex());
        subject.setSurname(getSurname());

        if (!ValidationHelper.isNullOrEmpty(getFiscalCode())) {
            subject.setTypeId(SubjectType.PHYSICAL_PERSON.getId());
        } else if (!ValidationHelper.isNullOrEmpty(getNumberVAT())) {
            subject.setTypeId(SubjectType.LEGAL_PERSON.getId());
        } else {
            subject.setTypeId(getTypeId());
        }

        if (!ValidationHelper.isNullOrEmpty(getCityCode())) {
            City city = ConnectionManager.get(City.class,
                    Restrictions.eq("cfis", getCityCode()), session);

            if (city != null) {
                subject.setBirthCity(city);
                subject.setBirthProvince(city.getProvince());
            }
        }

        if (ValidationHelper.isNullOrEmpty(subject.getBirthCity())
                && !ValidationHelper.isNullOrEmpty(getCityDescription())) {
            List<City> cityList = ConnectionManager.load(City.class, new Criterion[]{
                    Restrictions.eq("description", getCityDescription())
            }, session);

            if (!ValidationHelper.isNullOrEmpty(cityList)) {
                if (cityList.size() == 1) {
                    subject.setBirthCity(cityList.get(0));
                    subject.setBirthProvince(cityList.get(0).getProvince());
                } else {
                    subject.setBirthCity(cityList.get(0));
                    subject.setBirthProvince(cityList.get(0).getProvince());
                    subject.setCityDesc(getCityDescription());
                }
            } else {
                List<Country> countryList = ConnectionManager.load(Country.class, new Criterion[]{
                        Restrictions.eq("description", getCityDescription())
                }, session);

                if (!ValidationHelper.isNullOrEmpty(countryList)) {
                    if (countryList.size() == 1) {
                        subject.setForeignCountry(true);
                        subject.setCountry(countryList.get(0));
                    }
                }
            }
        }

        return subject;
    }

    public Subject loadByFiscalCode(Session session)
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        Subject existSubject = new Subject();
        Subject subject = toEntity(session);

        if (!ValidationHelper.isNullOrEmpty(getFiscalCode())) {
            List<Subject> existSubjects = ConnectionManager.load(Subject.class, new CriteriaAlias[]{
                    new CriteriaAlias("birthCity", "city", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("name", subject.getName()),
                    Restrictions.eq("surname", subject.getSurname()),
                    Restrictions.eq("birthDate", subject.getBirthDate()),
                    (subject.getBirthProvince() != null
                            ? Restrictions.eq("birthProvince.id", subject.getBirthProvince().getId())
                            : Restrictions.isNotNull("birthProvince.id")),
                    (subject.getBirthProvince() != null
                            ? Restrictions.eq("city.description", subject.getBirthCity().getDescription())
                            : Restrictions.eq("city.description", subject.getCityDesc())),
                    Restrictions.eq("fiscalCode", subject.getFiscalCode())
            }, session);
            if (!ValidationHelper.isNullOrEmpty(existSubjects)) {
                existSubject = existSubjects.get(0);
            }
        } else if (!ValidationHelper.isNullOrEmpty(getNumberVAT())) {
            List<Subject> list = ConnectionManager.load(Subject.class, new CriteriaAlias[]{
                    new CriteriaAlias("birthCity", "city", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("numberVAT", subject.getNumberVAT()),
                    (subject.getBirthProvince() != null
                            ? Restrictions.eq("birthProvince.id", subject.getBirthProvince().getId())
                            : Restrictions.isNotNull("birthProvince.id")),
                    (subject.getBirthProvince() != null
                            ? Restrictions.eq("city.description", subject.getBirthCity().getDescription())
                            : Restrictions.eq("city.description", subject.getCityDesc()))
            }, session);

            if (!ValidationHelper.isNullOrEmpty(list))
                for (Subject s : list) {
                    if (RequestHelper.isBusinessNameFunctionallyEqual(s.getBusinessName(),
                            subject.getBusinessName())) {
                        existSubject = s;
                    }
                }
        }
        if (!ValidationHelper.isNullOrEmpty(existSubject)) {
            LogHelper.log(log,
                    "More then 1 subject with fiscal code (number vat) = "
                            + getFiscalCode() + " " + getNumberVAT());
            return existSubject;
        }

        return subject;
    }

    @Override
    public void setField(XMLElements element, String value) {
        if (!ValidationHelper.isNullOrEmpty(value)) {
            switch ((SubjectForSyntheticListXMLElements) element) {
                case BIRTH_CITY_CODE:
                    setCityDescription(value);
                    break;

                case CITY_DESCRIPTION:
                    setCityDescription(value);
                    break;

                case BIRTH_DATE:
                    setBirthDate(DateTimeHelper.fromXMLString(value));
                    break;

                case BORN_IN_FOREIGN_STATE:
                    setBornInForeignState(Boolean.TRUE.toString().equalsIgnoreCase(value));

                    break;

                case BUSINESS_NAME:
                    setBusinessName(value);
                    break;

                case ELECTED_MORTGAGE_HOME:
                    setElectedMortgageHome(value);
                    break;

                case FIRST_NAME:
                    setName(value);
                    break;

                case FISCAL_CODE:
                    setFiscalCode(value);
                    break;

                case FOREIGN_STATE:
                    setForeignState(value);
                    break;

                case LAST_NAME:
                    setSurname(value);
                    break;

                case NUMBER_VAT:
                    setNumberVAT(value);
                    break;

                case SEX:
                    SexTypes sex = SexTypes.getByShortValue(value);

                    if (sex != null) {
                        setSex(sex.getId());
                    }
                    break;

                case TYPE_ID:
                    break;

                default:
                    break;
            }
        }
    }

    public void setField(SubjectXMLElements element, String value) {
        if (!ValidationHelper.isNullOrEmpty(value)) {
            switch (element) {
                case BIRTH_CITY_DESCRIPTION:
                    setCityDescription(value);
                    break;

                case CITY_DESCRIPTION:
                    setCityDescription(value);
                    break;

                case BIRTH_DATE:
                    setBirthDate(DateTimeHelper.fromXMLString(value));
                    break;

                case BORN_IN_FOREIGN_STATE:
                    setBornInForeignState(Boolean.TRUE.toString().equalsIgnoreCase(value));
                    break;

                case BUSINESS_NAME:
                    setBusinessName(value);
                    break;

                case ELECTED_MORTGAGE_HOME:
                    setElectedMortgageHome(value);
                    break;

                case FIRST_NAME:
                    setName(value);
                    break;

                case FISCAL_CODE:
                    setFiscalCode(value);
                    break;

                case FOREIGN_STATE:
                    setForeignState(value);
                    break;

                case LAST_NAME:
                    setSurname(value);
                    break;

                case NUMBER_VAT:
                    setNumberVAT(value);
                    break;

                case SEX:
                    SexTypes sex = SexTypes.getByShortValue(value);

                    if (sex != null) {
                        setSex(sex.getId());
                    }
                    break;

                case CITY_CODE:
                    setCityCode(value);
                    break;

                case TYPE_ID:
                    break;

                default:
                    break;
            }
        }
    }

    public String getName() {
        return name;
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

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getCityDescription() {
        return cityDescription;
    }

    public void setCityDescription(String cityDescription) {
        this.cityDescription = cityDescription;
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

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

}
