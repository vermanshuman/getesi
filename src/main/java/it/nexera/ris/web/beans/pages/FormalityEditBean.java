package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.comparators.CadastralCategoryComparator;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ManagedBean(name = "formalityEditBean")
@ViewScoped
public class FormalityEditBean extends EntityEditPageBean<Formality> implements Serializable {

    private static final long serialVersionUID = 2255442898397167303L;

    private Long requestId;

    private Long selectedReclamePropertyServiceId;

    private Long selectedProvincialOfficeId;

    private Long selectedId;

    private List<SelectItem> conservatories;

    private List<SelectItem> categories;

    private List<SelectItem> sexTypes;

    private SectionA sectionA;

    private Map<String, List<Subject>> sectionC;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException, IllegalAccessException {
        String requestId = getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
        if (!ValidationHelper.isNullOrEmpty(requestId) && !requestId.equals("null")) {
            setRequestId(Long.parseLong(requestId));
        }
        if (!getEntity().isNew()) {
            if (!ValidationHelper.isNullOrEmpty(getEntity().getReclamePropertyService())) {
                setSelectedId(getEntity().getReclamePropertyService().getId());
            } else if (!ValidationHelper.isNullOrEmpty(getEntity().getProvincialOffice())) {
                setSelectedId(getEntity().getProvincialOffice().getId());
            }
            setSectionA(getEntity().getSectionA());

        }
        if (ValidationHelper.isNullOrEmpty(getSectionA())) {
            setSectionA(new SectionA());
        }
        setConservatories(ComboboxHelper.fillList(LandChargesRegistry.class));
        setSectionC(new HashMap<>());
        List<Subject> aFavoreSubjects = getEntity().getSectionC().stream()
                .filter(c -> "A favore".equals(c.getSectionCType())).map(SectionC::getSubject)
                .flatMap(List::stream).peek(s -> {s.setTempFormality(getEntity()); s.setSelectedSexTypeId(s.getSex());}).collect(Collectors.toList());
        List<Subject> controSubjects = getEntity().getSectionC().stream()
                .filter(c -> "Contro".equals(c.getSectionCType())).map(SectionC::getSubject)
                .flatMap(List::stream).peek(s -> {s.setTempFormality(getEntity()); s.setSelectedSexTypeId(s.getSex());}).collect(Collectors.toList());
        List<Subject> debitoriSubjects = getEntity().getSectionC().stream()
                .filter(c -> "Debitori non datori di ipoteca".equals(c.getSectionCType())).map(SectionC::getSubject)
                .flatMap(List::stream).peek(s -> {s.setTempFormality(getEntity()); s.setSelectedSexTypeId(s.getSex());}).collect(Collectors.toList());
        getSectionC().put(ResourcesHelper.getString("formalityInFavor"), aFavoreSubjects);
        getSectionC().put(ResourcesHelper.getString("formalityVersus"), controSubjects);
        getSectionC().put(ResourcesHelper.getString("formalityDebitori"), debitoriSubjects);

        this.setSexTypes(ComboboxHelper.fillList(SexTypes.class, false));

        List<CadastralCategory> cadastralCategoryList = DaoManager.load(CadastralCategory.class);
        cadastralCategoryList.sort(new CadastralCategoryComparator());
        setCategories(ComboboxHelper.fillList(cadastralCategoryList, true, false));
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {

    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException,
            IOException, InstantiationException, IllegalAccessException {

        if (!ValidationHelper.isNullOrEmpty(getEntity().getReclamePropertyService())){
            if (!ValidationHelper.isNullOrEmpty(getSelectedId())) {
                getEntity().setReclamePropertyService(DaoManager.get(LandChargesRegistry.class,
                        getSelectedId()));
            } else {
                getEntity().setReclamePropertyService(null);
            }
        } else if (!ValidationHelper.isNullOrEmpty(getEntity().getProvincialOffice())) {
            if (!ValidationHelper.isNullOrEmpty(getSelectedId())) {
                getEntity().setProvincialOffice(DaoManager.get(LandChargesRegistry.class, getSelectedId()));
            } else {
                getEntity().setProvincialOffice(null);
            }
        }

        DaoManager.save(getEntity());

        saveSectionAToDB();

        saveSectionBToDB();

        saveSectionCToDB();

    }

    private void saveSectionAToDB() throws PersistenceBeanException {
        getSectionA().setFormality(getEntity());
        DaoManager.save(getSectionA());
    }

    private void saveSectionBToDB() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntity().getSectionBMap())) {
            for (Map.Entry<Integer, List<Property>> entry : getEntity().getSectionBMap()) {
                for (Property property : entry.getValue()) {
                    City city = DaoManager.get(City.class, property.getSelectedCityId());
                    property.setCity(city);
                    DaoManager.save(property);
                }
            }
        }
    }

    private void saveSectionCToDB() throws PersistenceBeanException {
        for (Map.Entry<String, List<Subject>> entry : getSectionCEntries()) {
            for (Subject subject : entry.getValue()) {
                subject.setSex(subject.getSelectedSexTypeId());
                DaoManager.save(subject);
            }
        }
    }

    @Override
    public void goBack() {
        if (ValidationHelper.isNullOrEmpty(getRequestId())) {
            RedirectHelper.goTo(PageTypes.DATABASE_LIST);
        } else {
            RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_VIEW,
                    getRequestId(), null);
        }

    }

    public Long getSelectedReclamePropertyServiceId() {
        return selectedReclamePropertyServiceId;
    }

    public void setSelectedReclamePropertyServiceId(Long selectedReclamePropertyServiceId) {
        this.selectedReclamePropertyServiceId = selectedReclamePropertyServiceId;
    }

    public Long getSelectedProvincialOfficeId() {
        return selectedProvincialOfficeId;
    }

    public void setSelectedProvincialOfficeId(Long selectedProvincialOfficeId) {
        this.selectedProvincialOfficeId = selectedProvincialOfficeId;
    }

    public List<SelectItem> getConservatories() {
        return conservatories;
    }

    public void setConservatories(List<SelectItem> conservatories) {
        this.conservatories = conservatories;
    }

    public SectionA getSectionA() {
        return sectionA;
    }

    public void setSectionA(SectionA sectionA) {
        this.sectionA = sectionA;
    }

    public Set<Map.Entry<String, List<Subject>>> getSectionCEntries() {
        return sectionC.entrySet();
    }

    public Map<String, List<Subject>> getSectionC() {
        return sectionC;
    }

    public void setSectionC(Map<String, List<Subject>> sectionC) {
        this.sectionC = sectionC;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }

    public List<SelectItem> getSexTypes() {
        return sexTypes;
    }

    public void setSexTypes(List<SelectItem> sexTypes) {
        this.sexTypes = sexTypes;
    }

    public List<SelectItem> getCategories() {
        return categories;
    }

    public void setCategories(List<SelectItem> categories) {
        this.categories = categories;
    }
}
