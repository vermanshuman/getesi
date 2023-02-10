package it.nexera.ris.web.beans.wrappers.logic;

import com.sun.faces.renderkit.html_basic.HtmlResponseWriter;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.PropertyWrapperType;
import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.component.tabview.Tab;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class SelectedSubjectWrapper extends EntityEditPageBean<Subject> implements Serializable {

    private static final long serialVersionUID = -7366211875713976214L;

    private boolean onlyView;

    private Boolean foreignCountry;

    private Long addressProvinceId;

    private Long addressCityId;

    private Long selectedCountryId;

    private Long downloadFileId;

    private Long selectedTemplateId;

    private List<SelectItem> provinces;

    private List<SelectItem> addressCities;

    private List<SelectItem> sexTypes;

    private List<SelectItem> subjectTypes;

    private List<SelectItem> countries;

    private List<SelectItem> templates;

    private LazyDataModel<Property> lazyRealEstateModel;

    private Document printDocument;

    private Long editPropertyId;

    private Boolean renderHeader;

    private Boolean renderFooter;

    private Boolean showSave;

    private Boolean showCancel;

    private List<SelectItem> propertyWrapperType;

    private Long selectedPropertyType;

    private UIComponent componentTabView;

    private Request downloadRequest;

    private List<Document> requestDocuments;

    private Long selectedDocumentId;

    private List<Long> listIds;

    private UIComponent formalitaTab;

    private int activeTabIndex;

    private UIComponent catastiTab;

    private UIComponent elenchiSinteticiTab;

    private UIComponent visureIpotecarieTab;

    private UIComponent soggettiValidatiTab;

    private UIComponent visureTestoTab;

    private UIComponent visureDHTab;

    private Integer activePanelIndex;

    private UIComponent servizioAnagraficoTab;

    private UIComponent servizioImmobiliareTab;

    private UIComponent servizioInvestigativoTab;

    private UIComponent servizioCameraleTab;

    private Integer activePanelServicesIndex;

    private UIComponent richiestaTab;

    @Getter
    @Setter
    private RequestBindingWrapper requestBindingWrapper;

    @Getter
    @Setter
    private RequestTypeBindingWrapper registryServiceWrapper;

    @Getter
    @Setter
    private RequestTypeBindingWrapper realEstateServiceWrapper;

    @Getter
    @Setter
    private RequestTypeBindingWrapper investigativeServiceWrapper;

    @Getter
    @Setter
    private RequestTypeBindingWrapper commerceServiceWrapper;

    @Getter
    @Setter
    private FormalityBindingWrapper formalityWrapper;

    @Getter
    @Setter
    private CadastralBindingWrapper cadastralWrapper;

    @Getter
    @Setter
    private SyntheticBindingWrapper syntheticBindingWrapper;

    @Getter
    @Setter
    private FormalitySubjectBindingWrapper formalitySubjectBindingWrapper;

    @Getter
    @Setter
    private VisureRTFBindingWrapper visureRTFBindingWrapper;

    @Getter
    @Setter
    private VisureDHBindingWrapper visureDHBindingWrapper;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
    }

    public void onLoad(Long entityId) throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        this.setActiveTabIndex(0);
        this.setActivePanelIndex(-1);
        this.setActivePanelServicesIndex(0);
        this.setEntityId(entityId);
        setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
        getProvinces().add(new SelectItem(Province.FOREIGN_COUNTRY_ID, Province.FOREIGN_COUNTRY));
        this.setPropertyWrapperType(ComboboxHelper.fillList(PropertyWrapperType.class, false, true));

        this.setAddressCities(ComboboxHelper.fillList(new ArrayList<City>(), true));

        if (this.getEntity().getBirthProvince() != null) {
            this.setAddressProvinceId(this.getEntity().getBirthProvince().getId());
            handleAddressProvinceChange();
        }

        setForeignCountry(getEntity().getForeignCountry());

        if (getForeignCountry()) {
            setAddressProvinceId(Province.FOREIGN_COUNTRY_ID);
        }

        if (this.getEntity().getBirthCity() != null) {
            this.setAddressCityId(this.getEntity().getBirthCity().getId());
        }

        this.setCountries(ComboboxHelper.fillList(Country.class, Order.asc("description"),
                new Criterion[]{Restrictions.ne("description", "ITALIA")}));

        if (this.getEntity().getCountry() != null) {
            this.setSelectedCountryId(this.getEntity().getCountry().getId());
        }

        this.setSexTypes(ComboboxHelper.fillList(SexTypes.class, false));
        this.setSubjectTypes(ComboboxHelper.fillList(SubjectType.class, false));
        List<Long> listIds = EstateSituationHelper.getIdSubjects(getEntity());
        listIds.add(getEntity().getId());
        setListIds(listIds);
        loadFormalitaTab();
        loadCatastiTab();
        loadElenchiSinteticiTab();
        loadVisureIpotecarieTab();
        loadSoggettiValidatiTab();
        loadVisureTestoTab();
        loadVisureDHTab();
        loadRichiestaTab();
        loadServiziEvasiTabs();
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getTypeId())) {
            addRequiredFieldException("form:subjectType");
        }

        if (getIsPhysicalPerson() && ValidationHelper
                .isNullOrEmpty(this.getEntity().getSurname())) {
            addRequiredFieldException("form:lastName");
        }

        if (getIsPhysicalPerson()
                && ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        }

        if (getIsPhysicalPerson() && ValidationHelper
                .isNullOrEmpty(this.getEntity().getBirthDate())) {
            addRequiredFieldException("form:birthdate");
        }

        if (ValidationHelper.isNullOrEmpty(this.getAddressProvinceId())) {
            addRequiredFieldException("form:province");
        }

        if (!getForeignCountry()
                && ValidationHelper.isNullOrEmpty(this.getAddressCityId())) {
            addRequiredFieldException("form:city");
        }

        if (getForeignCountry()
                && ValidationHelper.isNullOrEmpty(this.getSelectedCountryId())) {
            addRequiredFieldException("form:country");
        }

        if (getIsPhysicalPerson()
                && ValidationHelper.isNullOrEmpty(this.getEntity().getSex())) {
            addRequiredFieldException("form:sex");
        }

        if (getIsPhysicalPerson() && ValidationHelper
                .isNullOrEmpty(this.getEntity().getFiscalCode())) {
            addRequiredFieldException("form:fiscalCode");
        }

        if (getIsPhysicalPerson()
                && Boolean.TRUE.equals(this.getEntity().getBornInForeignState())
                && ValidationHelper
                .isNullOrEmpty(this.getEntity().getForeignState())) {
            addRequiredFieldException("form:foreignState");
        }

        if (!getIsPhysicalPerson() && ValidationHelper
                .isNullOrEmpty(this.getEntity().getBusinessName())) {
            addRequiredFieldException("form:businessName");
        }

        if (!getIsPhysicalPerson() && ValidationHelper
                .isNullOrEmpty(this.getEntity().getFiscalCode())
        ) {
            addRequiredFieldException("form:codeFiscal");
        }

        if (!getIsPhysicalPerson() && ValidationHelper
                .isNullOrEmpty(this.getEntity().getNumberVAT())) {
            addRequiredFieldException("form:numberVAT");
        }
        try {
            if (!getValidationFailed()) {
                if (SubjectType.PHYSICAL_PERSON.getId().equals(getEntity().getTypeId())) {
                    if (DaoManager.getCount(Subject.class, "id", new CriteriaAlias[]{
                            new CriteriaAlias("birthCity", "city", JoinType.INNER_JOIN)
                    }, new Criterion[]{
                            Restrictions.eq("name", getEntity().getName()),
                            Restrictions.eq("surname", getEntity().getSurname()),
                            Restrictions.eq("birthDate", getEntity().getBirthDate()),
                            Restrictions.eq("birthProvince.id", getAddressProvinceId()),
                            Restrictions.eq("city.description",
                                    DaoManager.get(City.class, getAddressCityId()).getDescription()),
                            Restrictions.eq("fiscalCode", getEntity().getFiscalCode()),
                            getEntity().isNew() ?
                                    Restrictions.ne("id", 0L) :
                                    Restrictions.ne("id", getEntity().getId())
                    }) != 0) {
                        addFieldException("form:name", "databaseListSubjectFiscalCodeWarrning");
                        addFieldException("form:lastName", "databaseListSubjectFiscalCodeWarrning", false);
                        addFieldException("form:birthdate", "databaseListSubjectFiscalCodeWarrning", false);
                        addFieldException("form:city", "databaseListSubjectFiscalCodeWarrning", false);
                        addFieldException("form:province", "databaseListSubjectFiscalCodeWarrning", false);
                        addFieldException("form:fiscalCode", "databaseListSubjectFiscalCodeWarrning", false);
                    }
                } else {
                    if (DaoManager.getCount(Subject.class, "id", new CriteriaAlias[]{
                            new CriteriaAlias("birthCity", "city", JoinType.INNER_JOIN)
                    }, new Criterion[]{
                            Restrictions.eq("businessName", getEntity().getBusinessName()),
                            Restrictions.eq("numberVAT", getEntity().getNumberVAT()),
                            Restrictions.eq("birthProvince.id", getAddressProvinceId()),
                            Restrictions.eq("city.description",
                                    DaoManager.get(City.class, getAddressCityId()).getDescription()),
                            getEntity().isNew() ?
                                    Restrictions.ne("id", 0L) :
                                    Restrictions.ne("id", getEntity().getId())
                    }) != 0) {
                        addFieldException("form:businessName", "databaseListSubjectVatNumberWarrning");
                        addFieldException("form:numberVAT", "databaseListSubjectVatNumberWarrning", false);
                        addFieldException("form:city", "databaseListSubjectVatNumberWarrning", false);
                        addFieldException("form:province", "databaseListSubjectVatNumberWarrning", false);
                    }
                }
            }
        } catch (InstantiationException e) {
            LogHelper.log(log, e);
        }

    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
            InstantiationException, IllegalAccessException {
        if (this.getAddressCityId() != null) {
            City city = DaoManager.get(City.class, this.getAddressCityId());
            this.getEntity().setBirthCity(city);
        }

        if (this.getAddressProvinceId() != null && !Province.FOREIGN_COUNTRY_ID
                .equals(this.getAddressProvinceId())) {
            Province province = DaoManager.get(Province.class,
                    this.getAddressProvinceId());
            this.getEntity().setBirthProvince(province);
        }

        if (getForeignCountry()
                && !ValidationHelper.isNullOrEmpty(this.getSelectedCountryId())) {
            Country country = DaoManager.get(Country.class,
                    this.getSelectedCountryId());
            this.getEntity().setCountry(country);
        }

        this.getEntity().setForeignCountry(getForeignCountry());

        DaoManager.save(this.getEntity());

    }

    public void saveFromDialog() {
        if (this.getSaveFlag() == 0) {
            try {
                this.cleanValidation();
                this.setValidationFailed(false);
                this.onValidate();
                if (this.getValidationFailed()) {
                    return;
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
                return;
            }

            try {
                this.tr = DaoManager.getSession().beginTransaction();

                this.setSaveFlag(1);

                this.onSave();
            } catch (Exception e) {
                if (this.tr != null) {
                    this.tr.rollback();
                }
                LogHelper.log(log, e);
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "",
                        ResourcesHelper.getValidation("objectEditedException"));
            } finally {
                if (this.tr != null && !this.tr.wasRolledBack()
                        && this.tr.isActive()) {
                    try {
                        this.tr.commit();
                    } catch (StaleObjectStateException e) {
                        MessageHelper.addGlobalMessage(
                                FacesMessage.SEVERITY_ERROR, "",
                                ResourcesHelper.getValidation(
                                        "exceptionOccuredWhileSaving"));
                        LogHelper.log(log, e);
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                }
                this.setSaveFlag(0);
                //closeDialog();
            }
        }
    }

    public void handleAddressProvinceChange()
            throws HibernateException, PersistenceBeanException, IllegalAccessException {
        if (!Province.FOREIGN_COUNTRY_ID.equals(this.getAddressProvinceId())) {
            this.setForeignCountry(Boolean.FALSE);

            this.setAddressCities(ComboboxHelper.fillList(City.class, Order.asc("description"),
                    new Criterion[]{Restrictions.eq("province.id", this.getAddressProvinceId()),
                            Restrictions.eq("external", Boolean.TRUE)}));
        } else {
            this.setForeignCountry(Boolean.TRUE);
        }
    }

    public boolean getIsPhysicalPerson() {
        return getEntity().getTypeId() == null || SubjectType.PHYSICAL_PERSON
                .getId().equals(getEntity().getTypeId());
    }

    public String getCityDescription() {
        return this.loadDescriptionById(City.class, this.getAddressCityId());
    }

    public String getCountryDescription() {
        return this.loadDescriptionById(Country.class,
                this.getSelectedCountryId());
    }

    public String getProvinceDescription() {
        if (getForeignCountry()) {
            return Province.FOREIGN_COUNTRY;
        }

        return this.loadDescriptionById(Province.class,
                this.getAddressProvinceId());
    }

    public void loadTabs() {
        List<Tab> tabList = new ArrayList<>();
        ListIterator<UIComponent> itr = getComponentTabView().getChildren().listIterator();
        while (itr.hasNext()) {
            Tab tab = (Tab) itr.next();
            String title = tab.getTitle();
            try {
                if (!ValidationHelper.isNullOrEmpty(title)) {
                    if (title.startsWith(ResourcesHelper.getString("subjectViewFormality"))) {
                        FormalityBindingWrapper formalityTab = new FormalityBindingWrapper(getEntity(), getListIds());
                        formalityTab.loadData(Boolean.FALSE);
                        itr.set(formalityTab.getTab());
                        RequestContext.getCurrentInstance().update("tabView");
                    } else if (title.startsWith(ResourcesHelper.getString("subjectViewCadastral"))) {
                        CadastralBindingWrapper cadastralBindingWrapper = new CadastralBindingWrapper(getListIds(), getEntity(), DocumentType.CADASTRAL);
                        cadastralBindingWrapper.loadData(true);
                        itr.set(cadastralBindingWrapper.getTab());
                        RequestContext.getCurrentInstance().update("tabView");
                    } else if (title.startsWith(ResourcesHelper.getString("subjectViewDocuments"))) {
                        CadastralBindingWrapper cadastralBindingWrapper = new CadastralBindingWrapper(getListIds(), getEntity(), DocumentType.INDIRECT_CADASTRAL);
                        cadastralBindingWrapper.loadData(true);
                        itr.set(cadastralBindingWrapper.getTab());
                        RequestContext.getCurrentInstance().update("tabView");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void generateFiscalCode() {
        City city = null;
        try {
            if (!ValidationHelper.isNullOrEmpty(this.getAddressCityId())) {
                city = DaoManager.get(City.class, this.getAddressCityId());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            return;
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getSurname())) {
            return;
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getSex())) {
            return;
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getBirthDate())) {
            return;
        }

        if (ValidationHelper.isNullOrEmpty(city)) {
            return;
        }
        try {
            this.getEntity().setFiscalCode(CalcoloCodiceFiscale.calcola(
                    this.getEntity().getName(), this.getEntity().getSurname(),
                    this.getEntity().getBirthDate(), city.getCfis(),
                    SexTypes.getById(this.getEntity().getSex())));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void loadFormalitaTab() throws IllegalAccessException, PersistenceBeanException {
        setFormalityWrapper(new FormalityBindingWrapper(getEntity(), getListIds()));
       // FormalityBindingWrapper formalityBindingWrapper = new FormalityBindingWrapper(getEntity(), getListIds());
        getFormalityWrapper().loadData(Boolean.FALSE);
        Tab formalityTab = getFormalityWrapper().getTab();
        formalityTab.setDisabled(false);
        if(getFormalityWrapper().getCountTable().longValue() == 0l) {
            formalityTab.setDisabled(true);
        }else {
            this.setActivePanelIndex(0);
        }
        setFormalitaTab(formalityTab);
    }

    public void loadCatastiTab() throws IllegalAccessException, PersistenceBeanException {
        setCadastralWrapper(new CadastralBindingWrapper(getListIds(), getEntity(), DocumentType.CADASTRAL));
        //CadastralBindingWrapper cadastralBindingWrapper = new CadastralBindingWrapper(getListIds(), getEntity(), DocumentType.CADASTRAL);
        getCadastralWrapper().loadData(Boolean.TRUE);
        Tab catastiTab = getCadastralWrapper().getTab();
        catastiTab.setDisabled(false);
        if(getCadastralWrapper().getCountTable().longValue() == 0l) {
            catastiTab.setDisabled(true);
        }
        setCatastiTab(catastiTab);
    }

    public void loadElenchiSinteticiTab() throws IllegalAccessException, PersistenceBeanException {
        setSyntheticBindingWrapper(new SyntheticBindingWrapper(listIds));
        Tab elenchiSinteticiTab = getSyntheticBindingWrapper().getTab();
        elenchiSinteticiTab.setDisabled(false);
        if(getSyntheticBindingWrapper().getCountTable().longValue() == 0l) {
            elenchiSinteticiTab.setDisabled(true);
        }
        setElenchiSinteticiTab(elenchiSinteticiTab);
    }

    public void loadVisureIpotecarieTab() {
        Tab visureIpotecarieTab = getEmptyTab("subjectViewMortgage");
        visureIpotecarieTab.setDisabled(true);
        setVisureIpotecarieTab(visureIpotecarieTab);
    }

    private Tab getEmptyTab(String titleResourceId) {
        Tab tab = new Tab();
        tab.setTitle(ResourcesHelper.getString(titleResourceId));
        tab.setTitleStyle("font-weight: normal;");
        return tab;
    }

    public void loadSoggettiValidatiTab() throws IllegalAccessException, PersistenceBeanException {
        setFormalitySubjectBindingWrapper(new FormalitySubjectBindingWrapper(listIds));
        //FormalitySubjectBindingWrapper formalitySubjectBindingWrapper = new FormalitySubjectBindingWrapper(listIds);
        Tab soggettiValidatiTab = getFormalitySubjectBindingWrapper().getTab();
        soggettiValidatiTab.setDisabled(false);
        if(getFormalitySubjectBindingWrapper().getCountTable().longValue() == 0l) {
            soggettiValidatiTab.setDisabled(true);
        }
        setSoggettiValidatiTab(soggettiValidatiTab);
    }

    public void loadVisureTestoTab() throws IllegalAccessException, PersistenceBeanException {
        setVisureRTFBindingWrapper(new VisureRTFBindingWrapper(getEntity()));
        // VisureRTFBindingWrapper visureRTFBindingWrapper = new VisureRTFBindingWrapper(getEntity());
        Tab visureTestoTab = getVisureRTFBindingWrapper().getTab();
        visureTestoTab.setDisabled(false);
        if(getVisureRTFBindingWrapper().getCountTable().longValue() == 0l) {
            visureTestoTab.setDisabled(true);
        }
        setVisureTestoTab(visureTestoTab);
    }

    public void loadVisureDHTab() throws IllegalAccessException, PersistenceBeanException {
        setVisureDHBindingWrapper(new VisureDHBindingWrapper(getEntity()));
        // VisureDHBindingWrapper visureDHBindingWrapper = new VisureDHBindingWrapper(getEntity());
        Tab visureDHTab = getVisureDHBindingWrapper().getTab();
        visureDHTab.setDisabled(false);
        if(getVisureDHBindingWrapper().getCountTable().longValue() == 0l) {
            visureDHTab.setDisabled(true);
        }
        setVisureDHTab(visureDHTab);
    }

    public void loadServiziEvasiTabs() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        List<RequestType> requestTypes = DaoManager.load(RequestType.class, new Criterion[]{Restrictions.isNotNull("name")});
        for(RequestType requestType : requestTypes) {
            if(requestType.getName().equalsIgnoreCase("Servizio Anagrafico")) {
                setRegistryServiceWrapper( new RequestTypeBindingWrapper(listIds, requestType, isOnlyView()));
                // RequestTypeBindingWrapper requestTypeBindingWrapper = new RequestTypeBindingWrapper(listIds, requestType, isOnlyView());
                Tab servizioAnagraficoTab = getRegistryServiceWrapper().getTab();
                servizioAnagraficoTab.setDisabled(false);
                if(getRegistryServiceWrapper().getCountTable().longValue() == 0l) {
                    servizioAnagraficoTab.setDisabled(true);
                }
                setServizioAnagraficoTab(servizioAnagraficoTab);

            }
            if(requestType.getName().equalsIgnoreCase("Servizio Immobiliare")) {
                setRealEstateServiceWrapper( new RequestTypeBindingWrapper(listIds, requestType, isOnlyView()));
                //RequestTypeBindingWrapper requestTypeBindingWrapper = new RequestTypeBindingWrapper(listIds, requestType, isOnlyView());
                Tab servizioImmobiliareTab = getRealEstateServiceWrapper().getTab();
                servizioImmobiliareTab.setDisabled(false);
                if(getRealEstateServiceWrapper().getCountTable().longValue() == 0l) {
                    servizioImmobiliareTab.setDisabled(true);
                }
                setServizioImmobiliareTab(servizioImmobiliareTab);
            }
            if(requestType.getName().equalsIgnoreCase("Servizio Investigativo")) {
                setInvestigativeServiceWrapper( new RequestTypeBindingWrapper(listIds, requestType, isOnlyView()));
               // RequestTypeBindingWrapper requestTypeBindingWrapper = new RequestTypeBindingWrapper(listIds, requestType, isOnlyView());
                Tab servizioInvestigativoTab = getInvestigativeServiceWrapper().getTab();
                servizioInvestigativoTab.setDisabled(false);
                if(getInvestigativeServiceWrapper().getCountTable().longValue() == 0l) {
                    servizioInvestigativoTab.setDisabled(true);
                }
                setServizioInvestigativoTab(servizioInvestigativoTab);
            }
            if(requestType.getName().equalsIgnoreCase("Servizio Camerale")) {
                setCommerceServiceWrapper( new RequestTypeBindingWrapper(listIds, requestType, isOnlyView()));
                //RequestTypeBindingWrapper requestTypeBindingWrapper = new RequestTypeBindingWrapper(listIds, requestType, isOnlyView());
                Tab servizioCameraleTab = getCommerceServiceWrapper().getTab();
                servizioCameraleTab.setDisabled(false);
                if(getCommerceServiceWrapper().getCountTable().longValue() == 0l) {
                    servizioCameraleTab.setDisabled(true);
                }
                setServizioCameraleTab(servizioCameraleTab);
            }
        }
    }

    public void loadRichiestaTab() throws IllegalAccessException, PersistenceBeanException {
        // RequestBindingWrapper requestBindingWrapper = new RequestBindingWrapper(listIds);
        setRequestBindingWrapper(new RequestBindingWrapper(listIds));
        Tab richiestaTab = getRequestBindingWrapper().getTab();
        richiestaTab.setDisabled(false);
        if(getRequestBindingWrapper().getCountTable().longValue() == 0l) {
            richiestaTab.setDisabled(true);
        }
        setRichiestaTab(richiestaTab);
    }


    public boolean isOnlyView() {
        return onlyView;
    }

    public void setOnlyView(boolean onlyView) {
        this.onlyView = onlyView;
    }

    public Long getAddressProvinceId() {
        return addressProvinceId;
    }

    public void setAddressProvinceId(Long addressProvinceId) {
        this.addressProvinceId = addressProvinceId;
    }

    public Long getAddressCityId() {
        return addressCityId;
    }

    public void setAddressCityId(Long addressCityId) {
        this.addressCityId = addressCityId;
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

    public List<SelectItem> getSexTypes() {
        return sexTypes;
    }

    public void setSexTypes(List<SelectItem> sexTypes) {
        this.sexTypes = sexTypes;
    }

    public List<SelectItem> getSubjectTypes() {
        return subjectTypes;
    }

    public void setSubjectTypes(List<SelectItem> subjectTypes) {
        this.subjectTypes = subjectTypes;
    }

    public Long getSelectedCountryId() {
        return selectedCountryId;
    }

    public void setSelectedCountryId(Long selectedCountryId) {
        this.selectedCountryId = selectedCountryId;
    }

    public List<SelectItem> getCountries() {
        return countries;
    }

    public void setCountries(List<SelectItem> countries) {
        this.countries = countries;
    }

    public Boolean getForeignCountry() {
        return foreignCountry == null ? Boolean.FALSE : foreignCountry;
    }

    public void setForeignCountry(Boolean foreignCountry) {
        this.foreignCountry = foreignCountry;
    }

    public LazyDataModel<Property> getLazyRealEstateModel() {
        return lazyRealEstateModel;
    }

    public void setLazyRealEstateModel(
            LazyDataModel<Property> lazyRealEstateModel) {
        this.lazyRealEstateModel = lazyRealEstateModel;
    }

    public Long getDownloadFileId() {
        return downloadFileId;
    }

    public void setDownloadFileId(Long downloadFileId) {
        this.downloadFileId = downloadFileId;
    }

    public Document getPrintDocument() {
        return printDocument;
    }

    public void setPrintDocument(Document printDocument) {
        this.printDocument = printDocument;
    }

    public List<SelectItem> getTemplates() {
        return templates;
    }

    public void setTemplates(List<SelectItem> templates) {
        this.templates = templates;
    }

    public Long getSelectedTemplateId() {
        return selectedTemplateId;
    }

    public void setSelectedTemplateId(Long selectedTemplateId) {
        this.selectedTemplateId = selectedTemplateId;
    }

    public Long getEditPropertyId() {
        return editPropertyId;
    }

    public void setEditPropertyId(Long editPropertyId) {
        this.editPropertyId = editPropertyId;
    }

    public Boolean getRenderHeader() {
        return renderHeader;
    }

    public void setRenderHeader(Boolean renderHeader) {
        this.renderHeader = renderHeader;
    }

    public Boolean getRenderFooter() {
        return renderFooter;
    }

    public void setRenderFooter(Boolean renderFooter) {
        this.renderFooter = renderFooter;
    }

    public Boolean getShowSave() {
        return showSave;
    }

    public void setShowSave(Boolean showSave) {
        this.showSave = showSave;
    }

    public Boolean getShowCancel() {
        return showCancel;
    }

    public void setShowCancel(Boolean showCancel) {
        this.showCancel = showCancel;
    }

    public List<SelectItem> getPropertyWrapperType() {
        return propertyWrapperType;
    }

    public void setPropertyWrapperType(List<SelectItem> propertyWrapperType) {
        this.propertyWrapperType = propertyWrapperType;
    }

    public Long getSelectedPropertyType() {
        return selectedPropertyType;
    }

    public void setSelectedPropertyType(Long selectedPropertyType) {
        this.selectedPropertyType = selectedPropertyType;
    }

    public UIComponent getComponentTabView() {
        return componentTabView;
    }

    public void setComponentTabView(UIComponent componentTabView) {
        this.componentTabView = componentTabView;
    }

    public Request getDownloadRequest() {
        return downloadRequest;
    }

    public void setDownloadRequest(Request downloadRequest) {
        this.downloadRequest = downloadRequest;
    }

    public List<Document> getRequestDocuments() {
        return requestDocuments;
    }

    public void setRequestDocuments(List<Document> requestDocuments) {
        this.requestDocuments = requestDocuments;
    }

    public Long getSelectedDocumentId() {
        return selectedDocumentId;
    }

    public void setSelectedDocumentId(Long selectedDocumentId) {
        this.selectedDocumentId = selectedDocumentId;
    }

    public List<Long> getListIds() {
        return listIds;
    }

    public void setListIds(List<Long> listIds) {
        this.listIds = listIds;
    }


    public UIComponent getFormalitaTab() {
        return formalitaTab;
    }

    public void setFormalitaTab(UIComponent formalitaTab) {
        this.formalitaTab = formalitaTab;
    }

    public int getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(int activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    public UIComponent getCatastiTab() {
        return catastiTab;
    }

    public void setCatastiTab(UIComponent catastiTab) {
        this.catastiTab = catastiTab;
    }

    public UIComponent getElenchiSinteticiTab() {
        return elenchiSinteticiTab;
    }

    public void setElenchiSinteticiTab(UIComponent elenchiSinteticiTab) {
        this.elenchiSinteticiTab = elenchiSinteticiTab;
    }

    public UIComponent getVisureIpotecarieTab() {
        return visureIpotecarieTab;
    }

    public void setVisureIpotecarieTab(UIComponent visureIpotecarieTab) {
        this.visureIpotecarieTab = visureIpotecarieTab;
    }

    public UIComponent getSoggettiValidatiTab() {
        return soggettiValidatiTab;
    }

    public void setSoggettiValidatiTab(UIComponent soggettiValidatiTab) {
        this.soggettiValidatiTab = soggettiValidatiTab;
    }

    public UIComponent getVisureTestoTab() {
        return visureTestoTab;
    }

    public void setVisureTestoTab(UIComponent visureTestoTab) {
        this.visureTestoTab = visureTestoTab;
    }

    public UIComponent getVisureDHTab() {
        return visureDHTab;
    }

    public void setVisureDHTab(UIComponent visureDHTab) {
        this.visureDHTab = visureDHTab;
    }

    public Integer getActivePanelIndex() {
        return activePanelIndex;
    }

    public void setActivePanelIndex(Integer activePanelIndex) {
        this.activePanelIndex = activePanelIndex;
    }

    public UIComponent getServizioAnagraficoTab() {
        return servizioAnagraficoTab;
    }

    public void setServizioAnagraficoTab(UIComponent servizioAnagraficoTab) {
        this.servizioAnagraficoTab = servizioAnagraficoTab;
    }

    public UIComponent getServizioImmobiliareTab() {
        return servizioImmobiliareTab;
    }

    public void setServizioImmobiliareTab(UIComponent servizioImmobiliareTab) {
        this.servizioImmobiliareTab = servizioImmobiliareTab;
    }

    public UIComponent getServizioInvestigativoTab() {
        return servizioInvestigativoTab;
    }

    public void setServizioInvestigativoTab(UIComponent servizioInvestigativoTab) {
        this.servizioInvestigativoTab = servizioInvestigativoTab;
    }

    public UIComponent getServizioCameraleTab() {
        return servizioCameraleTab;
    }

    public void setServizioCameraleTab(UIComponent servizioCameraleTab) {
        this.servizioCameraleTab = servizioCameraleTab;
    }

    public Integer getActivePanelServicesIndex() {
        return activePanelServicesIndex;
    }

    public void setActivePanelServicesIndex(Integer activePanelServicesIndex) {
        this.activePanelServicesIndex = activePanelServicesIndex;
    }

    public UIComponent getRichiestaTab() {
        return richiestaTab;
    }

    public void setRichiestaTab(UIComponent richiestaTab) {
        this.richiestaTab = richiestaTab;
    }

    public void downloadRequestFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {

        if (!ValidationHelper.isNullOrEmpty(getSelectedDocumentId())) {
            Document document = DaoManager.get(Document.class, getSelectedDocumentId());
            switch (DocumentType.getById(document.getTypeId())) {
                case CADASTRAL:
                case ESTATE_FORMALITY: {
                    String projectUrl = this.getRequest().getHeader("referer");
                    projectUrl = projectUrl.substring(0, projectUrl.indexOf(this.getCurrentPage().getPagesContext())) + "/";
                    PrintPDFHelper.generatePDFOnDocument(document.getId(), projectUrl);
                }
                break;
                case FORMALITY:
                case REQUEST_REPORT:
                case ALLEGATI:
                case OTHER: {
                    File file = new File(document.getPath());
                    if (!ValidationHelper.isNullOrEmpty(document)) {
                        String title = prepareDocumentTitle(document);
                        try {
                            FileHelper.sendFile(title,
                                    new FileInputStream(file), (int) file.length());
                        } catch (Exception e) {
                            LogHelper.log(log, e);
                        }
                    }
                }
                break;
            }
        }
    }

    private String prepareDocumentTitle(Document document) {
        String path = !ValidationHelper.isNullOrEmpty(document.getPath()) ? document.getPath() : "";
        if (!ValidationHelper.isNullOrEmpty(document.getTitle())) {
            String title = document.getTitle();
            if (title.contains(".")) {
                int point = title.lastIndexOf(".");
                return title.substring(0, point) + path.substring(path.lastIndexOf("."));
            } else {
                return title + path.substring(path.lastIndexOf("."));
            }
        } else {
            return path.substring(path.lastIndexOf("\\") + 1);
        }
    }

    public void loadAllegatiDocuments() throws PersistenceBeanException, IllegalAccessException {
        executeJS("PF('documentDialog').show();");
    }

}
