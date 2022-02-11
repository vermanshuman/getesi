package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.RealEstateType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.PrintPDFHelper;
import it.nexera.ris.common.helpers.RealEstateHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.omi.OMIHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.DocumentProperty;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.web.beans.EntityViewPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ManagedBean(name = "realEstateViewBean")
@ViewScoped
public class RealEstateViewBean extends EntityViewPageBean<Property> implements Serializable {

    private static final long serialVersionUID = 822443201168769638L;

    private List<SelectItem> categories;

    private List<SelectItem> realEstateTypes;

    private List<SelectItem> provinces;

    private List<SelectItem> addressCities;

    private List<Formality> formalityList;

    private List<DocumentProperty> cadastralList;

    private Long selectedId;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getEntity().getCategory())) {
            setCategories(Collections.singletonList(new SelectItem(getEntity().getCategory().getId(),
                    getEntity().getCategory().toString())));
        }
        if (!ValidationHelper.isNullOrEmpty(getEntity().getProvince())) {
            setProvinces(Collections.singletonList(new SelectItem(getEntity().getProvince().getId(),
                    getEntity().getProvince().toString())));
        }
        if (!ValidationHelper.isNullOrEmpty(getEntity().getCity())) {
            setAddressCities(Collections.singletonList(new SelectItem(getEntity().getCity().getId(),
                    getEntity().getCity().toString())));
        }
        if (!ValidationHelper.isNullOrEmpty(getEntity().getType())) {
            setRealEstateTypes(ComboboxHelper.fillList(RealEstateType.class, false, false));
        }

        if (ValidationHelper.isNullOrEmpty(getEntity().getZone()) && !ValidationHelper.isNullOrEmpty(getEntity().getAddress())) {
            try {
                List<String> zoneByPropertyInKML = OMIHelper.findZoneByPropertyInKML(getEntity());
                if (!zoneByPropertyInKML.isEmpty()) {
                    getEntity().setZone(String.join("-", zoneByPropertyInKML));
                    DaoManager.save(getEntity(), true);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        List<Property> tempList = RealEstateHelper.getCadastralDatesEqualsProperties(getEntity(), DaoManager.getSession());

        setFormalityList(DaoManager.load(Formality.class, new CriteriaAlias[]{
                new CriteriaAlias("sectionB", "sb", JoinType.INNER_JOIN),
                new CriteriaAlias("sb.properties", "p", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("p.type", getEntity().getType()),
                Restrictions.eq("p.city.id", getEntity().getCity().getId()),
                Restrictions.eq("p.province.id", getEntity().getProvince().getId()),
                Restrictions.in("p.id", tempList.stream().map(Property::getId).collect(Collectors.toList()))
        }));
        setCadastralList(DaoManager.load(DocumentProperty.class, new CriteriaAlias[]{
                new CriteriaAlias("property", "p", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("p.type", getEntity().getType()),
                Restrictions.eq("p.city.id", getEntity().getCity().getId()),
                Restrictions.eq("p.province.id", getEntity().getProvince().getId()),
                Restrictions.in("p.id", tempList.stream().map(Property::getId).collect(Collectors.toList()))
        }));
    }

    @Override
    public void goBack() {
        RedirectHelper.goTo(PageTypes.DATABASE_LIST);
    }

      public void downloadPropertyPDF() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedId())) {
            String projectUrl = this.getRequest().getHeader("referer");
            projectUrl = projectUrl.substring(0, projectUrl.indexOf(this.getCurrentPage().getPagesContext())) + "/";
            PrintPDFHelper.generatePDFOnDocument(getSelectedId(), projectUrl);
        }
    }

    public void downloadEstateFormalityPDF() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedId())) {
            String projectUrl = this.getRequest().getHeader("referer");
            projectUrl = projectUrl.substring(0, projectUrl.indexOf(this.getCurrentPage().getPagesContext())) + "/";
            PrintPDFHelper.generatePDFOnDocument(getSelectedId(), projectUrl);
        }
    }

    public void goToFormality() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedId())) {
            RedirectHelper.goTo(PageTypes.REQUEST_FORMALITY, getSelectedId());
        }
    }

    public String getFormalityTabTitle() {
        return getTabTitle(getFormalityList().size(), "formality");
    }

    public String getCadastralTabTitle() {
        return getTabTitle(getCadastralList().size(), "subjectViewCadastral");
    }

    public String getTabTitle(int numberOfRows, String resourceId) {
        if (numberOfRows != 0L) {
            return String.format("%s (%d)", ResourcesHelper.getString(resourceId), numberOfRows);
        } else {
            return ResourcesHelper.getString(resourceId);
        }
    }

    public void downloadFormalityPDF() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedId())) {
            Document document = DaoManager.get(Document.class, getSelectedId());
            File file = new File(document.getPath());
            try (FileInputStream fis = new FileInputStream(file)) {
                FileHelper.sendFile(FileHelper.getFileName(document.getPath()), fis, (int) file.length());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public List<SelectItem> getCategories() {
        return categories;
    }

    public void setCategories(List<SelectItem> categories) {
        this.categories = categories;
    }

    public List<SelectItem> getRealEstateTypes() {
        return realEstateTypes;
    }

    public void setRealEstateTypes(List<SelectItem> realEstateTypes) {
        this.realEstateTypes = realEstateTypes;
    }

    public List<SelectItem> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<SelectItem> provinces) {
        this.provinces = provinces;
    }

    public List<SelectItem> getAddressCities() {
        return addressCities;
    }

    public void setAddressCities(List<SelectItem> addressCities) {
        this.addressCities = addressCities;
    }

    public List<Formality> getFormalityList() {
        return formalityList;
    }

    public void setFormalityList(List<Formality> formalityList) {
        this.formalityList = formalityList;
    }

    public List<DocumentProperty> getCadastralList() {
        return cadastralList;
    }

    public void setCadastralList(List<DocumentProperty> cadastralList) {
        this.cadastralList = cadastralList;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }
}
