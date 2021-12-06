package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.xml.wrappers.RequestWrapper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.SectionC;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Nationality;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubjectHelper {


    public static Subject getSubjectIfExists(Subject subject, Long typeId)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (SubjectType.PHYSICAL_PERSON.getId().equals(typeId)) {
            List<Subject> sub = DaoManager.load(Subject.class, getCriterionsPhysical(subject));
            if (!ValidationHelper.isNullOrEmpty(sub)) return sub.get(0);
        } else {
            List<Subject> sub = DaoManager.load(Subject.class, getCriterionsLegal(subject));
            if (!ValidationHelper.isNullOrEmpty(sub)) {
                for (Subject s : sub) {
                    if (RequestHelper.isBusinessNameFunctionallyEqual(subject.getBusinessName(),
                            s.getBusinessName()))
                        return s;
                }
            }
        }
        return null;
    }

    public static void fillSubjectFromWrapper(Subject subject, RequestWrapper wrapper) {
        if (wrapper.getSelectProvinceId() != null) {
            subject.setSelectProvinceId(wrapper.getSelectProvinceId());
        }

        if (wrapper.getSelectedCityId() != null) {
            subject.setSelectedCityId(wrapper.getSelectedCityId());
        }

        if (wrapper.getSelectedNationId() != null) {
            subject.setSelectedNationId(wrapper.getSelectedNationId());
        }

        if (wrapper.getSelectedJuridicalNationId() != null) {
            subject.setSelectedJuridicalNationId(wrapper.getSelectedJuridicalNationId());
        }

        deleteRedundantSpaces(subject);
    }

    private static void deleteRedundantSpaces(Subject subject) {
        if (!ValidationHelper.isNullOrEmpty(subject.getName())) {
            subject.setName(subject.getName().trim());
        }
        if (!ValidationHelper.isNullOrEmpty(subject.getSurname())) {
            subject.setSurname(subject.getSurname().trim());
        }
        if (!ValidationHelper.isNullOrEmpty(subject.getBusinessName())) {
            subject.setBusinessName(subject.getBusinessName().trim());
        }
    }

    private static Criterion[] getCriterionsPhysical(Subject subject) {
        List<Criterion> restrictionsList = new ArrayList<>();

        restrictionsList.add(Restrictions.eq("name", subject.getName()));
        restrictionsList.add(Restrictions.eq("surname", subject.getSurname()));
        restrictionsList.add(Restrictions.eq("birthDate", subject.getBirthDate()));


        if ((subject.getSelectProvinceId() == null && subject.getBirthProvince() == null)
                || subject.getSelectProvinceId().equals(-1L)) {
            restrictionsList.add(Restrictions.isNull("birthProvince"));
        } else {
            restrictionsList.add(Restrictions.eq("birthProvince.id", subject.getSelectProvinceId()));
        }

        if (subject.getSelectedCityId() == null && subject.getBirthCity() == null) {
            restrictionsList.add(Restrictions.isNull("birthCity"));
        } else {
            restrictionsList.add(Restrictions.eq("birthCity.id", subject.getSelectedCityId()));
        }

        if ((subject.getSelectedNationId() == null && subject.getCountry() == null)
                || subject.getSelectedNationId().equals(-1L)) {
            restrictionsList.add(Restrictions.isNull("country"));
        } else {
            restrictionsList.add(Restrictions.eq("country.id", subject.getSelectedNationId()));
        }

        restrictionsList.add(Restrictions.eq("fiscalCode", subject.getFiscalCode()));
        return restrictionsList.toArray(new Criterion[0]);
    }

    private static Criterion[] getCriterionsLegal(Subject subject) {
        List<Criterion> restrictionsList = new ArrayList<>();

        restrictionsList.add(Restrictions.eq("numberVAT", subject.getNumberVAT()));

        if (!ValidationHelper.isNullOrEmpty(subject.getFiscalCode())) {
            restrictionsList.add(Restrictions.eq("fiscalCode", subject.getFiscalCode()));
        }

        if ((subject.getSelectProvinceId() == null && subject.getBirthProvince() == null)
                || subject.getSelectProvinceId().equals(-1L)) {
            restrictionsList.add(Restrictions.isNull("birthProvince"));
        } else {
            restrictionsList.add(Restrictions.eq("birthProvince.id", subject.getSelectProvinceId()));
        }

        if (subject.getSelectedCityId() == null && subject.getBirthCity() == null) {
            restrictionsList.add(Restrictions.isNull("birthCity"));
        } else {
            restrictionsList.add(Restrictions.eq("birthCity.id", subject.getSelectedCityId()));
        }

        if ((subject.getSelectedJuridicalNationId() == null && subject.getCountry() == null)
                || subject.getSelectedJuridicalNationId().equals(-1L)) {
            restrictionsList.add(Restrictions.isNull("country"));
        } else {
            restrictionsList.add(Restrictions.eq("country.id", subject.getSelectedJuridicalNationId()));
        }

        return restrictionsList.toArray(new Criterion[0]);
    }

    public static String createFiscalCode(Long cityId, Long nationalityId, Long sexTypeId, String name, String surname,
                                          Date birthDate) throws Exception {
        String result = "";
        City city = null;
        if (!ValidationHelper.isNullOrEmpty(cityId)) {
            city = DaoManager.get(City.class, cityId);
        }
        if (!ValidationHelper.isNullOrEmpty(name)
                && !ValidationHelper.isNullOrEmpty(surname)
                && !ValidationHelper.isNullOrEmpty(sexTypeId)
                && !ValidationHelper.isNullOrEmpty(birthDate)) {
            if (!ValidationHelper.isNullOrEmpty(city)) {
                result = CalcoloCodiceFiscale.calcola(
                        name, surname,
                        birthDate, city.getCfis(),
                        SexTypes.getById(sexTypeId));
            }
            Nationality nationality = null;
            if (!ValidationHelper.isNullOrEmpty(nationalityId)) {
                nationality = DaoManager.get(Nationality.class, nationalityId);
            }
            if (!ValidationHelper.isNullOrEmpty(nationality)) {
                result = CalcoloCodiceFiscale.calcola(
                        name, surname,
                        birthDate, nationality.getCfis(),
                        SexTypes.getById(sexTypeId));
            }
        }
        return result;
    }

    public static List<Subject> deleteUnsuitable(List<Subject> presumableSubjects, List<Formality> formalities) {
        List<Subject> list = new ArrayList<>();
        for (Subject subject : presumableSubjects) {
            boolean match = false;
            for (SectionC c : subject.getSectionC()) {
                if (formalities.contains(c.getFormality())) {
                    match = true;
                }
            }
            if (!match) {
                list.add(subject);
            }
        }
        return list;
    }

    public static List<Subject> getPresumablesForSubject(Subject subject) {
        List<Subject> presumables = new ArrayList<>();

        try {
            List<Criterion> criteria = new ArrayList<>();

            criteria.add(Restrictions.eq("name", subject.getName()));
            criteria.add(Restrictions.eq("surname", subject.getSurname()));
            criteria.add(Restrictions.eq("sex", subject.getSex()));
            criteria.add(Restrictions.eq("birthDate", subject.getBirthDate()));
            criteria.add(Restrictions.eq("fiscalCode", subject.getFiscalCode()));

            if (Boolean.TRUE.equals(subject.getBornInForeignState()))
                criteria.add(Restrictions.eq("foreignState", subject.getForeignState()));

            if (subject.getBirthCity() != null)
                criteria.add(Restrictions.eq("birthCity.id", subject.getBirthCity().getId()));

            if (subject.getBirthProvince() != null)
                criteria.add(Restrictions.eq("birthProvince.id", subject.getBirthProvince().getId()));

            for (int i = 0, l = criteria.size(); i < l; i++) {
                List<Criterion> ic = new ArrayList<>(criteria);

                ic.remove(i);

                ic.add(Restrictions.ne("id", subject.getId()));

                List<Subject> list = DaoManager.load(Subject.class,
                        ic.toArray(new Criterion[0]), Order.desc("createDate"));

                ListHelper.add(presumables, list);
            }

            return presumables;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public static Subject copySubject(Subject source) 
            throws HibernateException, InstantiationException,
    IllegalAccessException, PersistenceBeanException {
        Subject subject = new Subject();

        subject.setCreateUserId(source.getCreateUserId());
        subject.setUpdateUserId(source.getUpdateUserId());
        subject.setVersion(source.getVersion());
        subject.setBirthDate(source.getBirthDate());
        subject.setBornInForeignState(source.getBornInForeignState());
        subject.setBusinessName(source.getBusinessName());
        subject.setElectedMortgageHome(source.getElectedMortgageHome());
        subject.setFiscalCode(source.getFiscalCode());
        subject.setForeignState(source.getForeignState());
        subject.setName(source.getName());
        subject.setNumberVAT(source.getNumberVAT());
        subject.setSex(source.getSex());
        subject.setSurname(source.getSurname());
        subject.setCountry(source.getCountry());
        subject.setBirthCity(source.getBirthCity());
        subject.setBirthProvince(source.getBirthProvince());
        subject.setCityDesc(source.getCityDesc());
        subject.setOldNumberVAT(source.getOldNumberVAT());
        if (!ValidationHelper.isNullOrEmpty(source.getFiscalCode()) 
                && ValidationHelper.isNullOrEmpty(source.getNumberVAT())) {
            subject.setTypeId(SubjectType.PHYSICAL_PERSON.getId());
        } else if (!ValidationHelper.isNullOrEmpty(source.getNumberVAT()) && 
                ValidationHelper.isNullOrEmpty(source.getFiscalCode())) {
            subject.setTypeId(SubjectType.LEGAL_PERSON.getId());
        } else {
            subject.setTypeId(source.getTypeId());
        }
        return subject;
    }

}
