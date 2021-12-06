package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.Relationship;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class SubjectWrapper implements Serializable {

    private static final long serialVersionUID = -9010959418342450087L;

    public transient final Log log = LogFactory
            .getLog(SubjectWrapper.class);

    private Long id;

    private String name;

    private String surname;

    private String businessName;

    private Date birthDate;

    private City birthCity;

    private Long provinceId;

    private Long cityId;

    private Long selectedSexTypeId;

    private String fiscalCode;

    private String numberVAT;

    private List<SubjectWrapper> potentialSubjects;

    private String differsField;

    private Document document;

    private Boolean isSelected;

    private Long documentId;

    private Long typeId;

    private List<SelectItem> cities;

    private Long selectedJuridicalNationId;

    private Long selectedNationId;
    
    private String oldNumberVAT;

    private List<Subject> subjectsToRestore;
    
    private Subject selectedSubjectToRestore;
    
    private List<SelectItem> persons;
    
    private String sectionCType;
    
    public SubjectWrapper() {

    }

    private SubjectWrapper closeWithoutDocument(SubjectWrapper sw) {
        SubjectWrapper newSw = new SubjectWrapper(sw.getName(), sw.getSurname(),
                sw.getBirthDate(), sw.getProvinceId(), sw.getCityId(),
                sw.getSelectedSexTypeId());

        newSw.setDiffersField(sw.getDiffersField());

        return newSw;
    }

    public SubjectWrapper(String name, String surname, Date birthDate,
                          Long provinceId, Long cityId, Long selectedSexTypeId) {
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.provinceId = provinceId;
        this.cityId = cityId;
        this.selectedSexTypeId = selectedSexTypeId;
    }

    public String getBirthCityDescription() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (getCityId() != null && (getBirthCity() == null || !getBirthCity().getId().equals(getCityId()))) {
            setBirthCity(DaoManager.get(City.class, getCityId()));
        }
        if (getBirthCity() != null && getBirthCity().getDescription() != null) {
            return getBirthCity().getDescription();
        } else {
            return null;
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

    public String getFiscalCodeVATNamber() {
        if (this.getTypeIsPhysicalPerson()) {
            return this.getFiscalCode();
        } else {
            return this.getNumberVAT();
        }
    }

    public Boolean getTypeIsPhysicalPerson() {
        return getTypeId() != null ? SubjectType.PHYSICAL_PERSON.getId().equals(getTypeId())
                : ValidationHelper.isNullOrEmpty(getBusinessName());
    }

    public void onChangeProvince() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        setCities(ComboboxHelper.fillList(City.class, Order.asc("description"), new Criterion[]{
                Restrictions.eq("province.id", getProvinceId()),
                Restrictions.eq("external", Boolean.TRUE)
        }));
        setFiscalCode(null);
    }

    public void generateFiscalCode() throws Exception {
        if (ValidationHelper.isNullOrEmpty(getProvinceId())) {
            setCityId(null);
            setSelectedNationId(null);
        } else if (getProvinceId() != -1) {
            setSelectedNationId(null);
        } else {
            setCityId(null);
        }

        String fiscalCode = SubjectHelper.createFiscalCode(getCityId(), getSelectedNationId(),
                getSelectedSexTypeId(), getName(), getSurname(), getBirthDate());
        setFiscalCode(fiscalCode);
    }
    
    public void restoreFiscalCode() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        boolean showFailedMsg = true;
        if ((SubjectType.PHYSICAL_PERSON.getId().equals(getTypeId())
                && !ValidationHelper.isNullOrEmpty(getFiscalCode()))
                || (SubjectType.LEGAL_PERSON.getId().equals(getTypeId())
                        && !ValidationHelper.isNullOrEmpty(getNumberVAT()))) {
            List<Subject> subjectList = DaoManager.load(Subject.class, new Criterion[]{
                    SubjectType.PHYSICAL_PERSON.getId().equals(getTypeId())
                    ? Restrictions.eq("fiscalCode", getFiscalCode())
                            : Restrictions.eq("numberVAT", getNumberVAT())
            });
            
            if (!ValidationHelper.isNullOrEmpty(subjectList)) {
                showFailedMsg = false;
                if (subjectList.size() == 1) {
                    selectRestoredSubject(subjectList.get(0));
                } else {
                    setSubjectsToRestore(subjectList);
                    executeJS("PF('subjectRestoreDlg').show();");
                }
            }
        }
        if (showFailedMsg) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    ResourcesHelper.getValidation("warning"),
                    ResourcesHelper.getValidation("noSubjectToRestore"));
        }
    }
    
    public void selectRestoredSubject() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedSubjectToRestore())) {
            selectRestoredSubject(getSelectedSubjectToRestore());
            setSubjectsToRestore(null);
            setSelectedSubjectToRestore(null);
        }
    }
    
    public void selectRestoredSubject(Subject subject)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (SubjectType.PHYSICAL_PERSON.getId().equals(subject.getTypeId())) {
            setSurname(subject.getSurname());
            setName(subject.getName());
            setSelectedSexTypeId(subject.getSex());
            setBirthDate(subject.getBirthDate());
        } else {
            setBusinessName(subject.getBusinessName());
            setNumberVAT(subject.getNumberVAT());
        }
        if (!ValidationHelper.isNullOrEmpty(subject.getCountry())) {
            setSelectedNationId(subject.getCountry().getId());
            setSelectedJuridicalNationId(subject.getCountry().getId());
            setProvinceId(-1L);
        } else {
            if(!ValidationHelper.isNullOrEmpty(subject.getBirthProvince())) {
                setProvinceId(subject.getBirthProvince().getId());
                onChangeProvince();
            }else {
                setProvinceId(-1L);
            }
            if(!ValidationHelper.isNullOrEmpty(subject.getBirthCity())) {
                setCityId(subject.getBirthCity().getId());
            }
                
        }
        setFiscalCode(subject.getFiscalCode());
    }

    public void parsePotentialSubjects(List<Object> subjects) {
        if (subjects != null) {
            this.setPotentialSubjects(new ArrayList<>());

            for (Object so : subjects) {
                Object[] oblects = (Object[]) so;

                SubjectWrapper sw = new SubjectWrapper();

                sw.setId((Long) oblects[0]);
                sw.setName((String) oblects[1]);
                sw.setSurname((String) oblects[2]);
                sw.setBirthDate((Date) oblects[3]);
                sw.setProvinceId((Long) oblects[4]);
                sw.setCityId((Long) oblects[5]);
                sw.setSelectedSexTypeId((Long) oblects[6]);
                sw.setDocumentId((Long) oblects[7]);

                if (checkDiffersField(sw)) {
                    loadDocument(sw);
                }
            }
        }
    }

    private void loadDocument(SubjectWrapper sw) {
        try {
            List<Long> documentIds = DaoManager.loadField(Relationship.class,
                    "tableId", Long.class, new Criterion[]
                            {
                                    Restrictions.eq("subject.id", sw.getId())
                            });

            if (!ValidationHelper.isNullOrEmpty(documentIds)) {
                for (Long id : documentIds) {
                    SubjectWrapper newSw = this.closeWithoutDocument(sw);
                    newSw.setDocument(DaoManager.get(Document.class, id));

                    this.getPotentialSubjects().add(newSw);
                }
            }

            if (sw.getDocumentId() != null) {
                SubjectWrapper newSw = this.closeWithoutDocument(sw);
                newSw.setDocument(
                        DaoManager.get(Document.class, sw.getDocumentId()));

                this.getPotentialSubjects().add(newSw);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private boolean checkDiffersField(SubjectWrapper sw) {
        boolean presentDiffersField = true;

        if (RequestHelper.isDifferent(getName(), sw.getName())) {
            sw.setDiffersField(String.format("%s: %s",
                    ResourcesHelper.getString("requestDiffersFieldName"),
                    sw.getName()));
            presentDiffersField = true;
        } else if (RequestHelper.isDifferent(getSurname(), sw.getSurname())) {
            sw.setDiffersField(String.format("%s: %s",
                    ResourcesHelper.getString("requestDiffersFieldSurname"),
                    sw.getSurname()));
            presentDiffersField = true;
        } else if (RequestHelper.isDifferent(getBirthDate(), sw.getBirthDate())) {
            sw.setDiffersField(String.format("%s: %s",
                    ResourcesHelper.getString("requestDiffersFieldBirthDate"),
                    DateTimeHelper.toString(sw.getBirthDate())));
            presentDiffersField = true;
        } else if (RequestHelper.isDifferent(getProvinceId(), sw.getProvinceId())) {
            try {
                sw.setDiffersField(String.format("%s: %s",
                        ResourcesHelper
                                .getString("requestDiffersFieldProvince"),
                        DaoManager.getField(Province.class, "description",
                                new Criterion[]
                                        {
                                                Restrictions.eq("id",
                                                        sw.getProvinceId())
                                        }, new CriteriaAlias[]
                                        {})));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            presentDiffersField = true;
        } else if (RequestHelper.isDifferent(getCityId(), sw.getCityId())) {
            try {
                sw.setDiffersField(String.format("%s: %s",
                        ResourcesHelper.getString("requestDiffersFieldCity"),
                        DaoManager.getField(City.class, "description",
                                new Criterion[]
                                        {
                                                Restrictions.eq("id", sw.getCityId())
                                        }, new CriteriaAlias[]
                                        {})));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            presentDiffersField = true;
        } else if (RequestHelper.isDifferent(getSelectedSexTypeId(), sw.getSelectedSexTypeId())) {
            SexTypes sex = SexTypes.getById(sw.getSelectedSexTypeId());

            sw.setDiffersField(String.format("%s: %s",
                    ResourcesHelper.getString("requestDiffersFieldSex"),
                    sex == null ? "" : sex.toString()));
            presentDiffersField = true;
        } else {
            sw.setDiffersField(
                    ResourcesHelper.getString("requestDiffersFieldAllEquals"));
            presentDiffersField = false;
        }

        return presentDiffersField;
    }
    
    public void executeJS(String str) {
        PFRequestContextHelper.executeJS(str);
    }

    public Log getLog() {
        return log;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getBusinessName() {
        return businessName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public City getBirthCity() {
        return birthCity;
    }

    public Long getProvinceId() {
        return provinceId;
    }

    public Long getCityId() {
        return cityId;
    }

    public Long getSelectedSexTypeId() {
        return selectedSexTypeId;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public String getNumberVAT() {
        return numberVAT;
    }

    public List<SubjectWrapper> getPotentialSubjects() {
        return potentialSubjects;
    }

    public String getDiffersField() {
        return differsField;
    }

    public Document getDocument() {
        return document;
    }

    public Boolean getIsSelected() {
        return isSelected;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public List<SelectItem> getCities() {
        return cities;
    }

    public Long getSelectedJuridicalNationId() {
        return selectedJuridicalNationId;
    }

    public Long getSelectedNationId() {
        return selectedNationId;
    }

    public String getOldNumberVAT() {
        return oldNumberVAT;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public void setBirthCity(City birthCity) {
        this.birthCity = birthCity;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public void setSelectedSexTypeId(Long selectedSexTypeId) {
        this.selectedSexTypeId = selectedSexTypeId;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public void setNumberVAT(String numberVAT) {
        this.numberVAT = numberVAT;
    }

    public void setPotentialSubjects(List<SubjectWrapper> potentialSubjects) {
        this.potentialSubjects = potentialSubjects;
    }

    public void setDiffersField(String differsField) {
        this.differsField = differsField;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public void setCities(List<SelectItem> cities) {
        this.cities = cities;
    }

    public void setSelectedJuridicalNationId(Long selectedJuridicalNationId) {
        this.selectedJuridicalNationId = selectedJuridicalNationId;
    }

    public void setSelectedNationId(Long selectedNationId) {
        this.selectedNationId = selectedNationId;
    }

    public void setOldNumberVAT(String oldNumberVAT) {
        this.oldNumberVAT = oldNumberVAT;
    }

    public List<Subject> getSubjectsToRestore() {
        return subjectsToRestore;
    }

    public void setSubjectsToRestore(List<Subject> subjectsToRestore) {
        this.subjectsToRestore = subjectsToRestore;
    }

    public Subject getSelectedSubjectToRestore() {
        return selectedSubjectToRestore;
    }

    public void setSelectedSubjectToRestore(Subject selectedSubjectToRestore) {
        this.selectedSubjectToRestore = selectedSubjectToRestore;
    }
    
    public List<SelectItem> getPersons() {
        return persons;
    }

    public void setPersons(List<SelectItem> persons) {
        this.persons = persons;
    }

     public String getSectionCType() {
        return sectionCType;
    }

    public void setSectionCType(String sectionCType) {
        this.sectionCType = sectionCType;
    }

}