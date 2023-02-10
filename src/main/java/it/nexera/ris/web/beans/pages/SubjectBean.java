package it.nexera.ris.web.beans.pages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import it.nexera.ris.common.helpers.*;
import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;

import it.nexera.ris.common.enums.DocumentGenerationPlaces;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.PropertyWrapperType;
import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
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
import it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper.CadastralBindingWrapper;
import it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper.FormalityBindingWrapper;
import it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper.FormalitySubjectBindingWrapper;
import it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper.RequestBindingWrapper;
import it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper.RequestTypeBindingWrapper;
import it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper.SyntheticBindingWrapper;
import it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper.VisureDHBindingWrapper;
import it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper.VisureRTFBindingWrapper;

@ManagedBean(name = "subjectBean")
@ViewScoped
public class SubjectBean extends EntityEditPageBean<Subject>
        implements Serializable {

    private static final long serialVersionUID = -8966192520979374022L;

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
    
    @Override
    protected void preLoad() throws PersistenceBeanException {
        if ("true".equalsIgnoreCase(
                this.getRequestParameter(RedirectHelper.ONLY_VIEW))) {
            setOnlyView(true);
        }
    }

    @Override
    protected void pageLoadStatic() throws PersistenceBeanException {
        if (SessionHelper.get("fromProperty") != null
                && ((Boolean) SessionHelper.get("fromProperty"))) {
            setRenderHeader(Boolean.FALSE);
            setRenderFooter(Boolean.FALSE);
            setShowSave(Boolean.FALSE);
            setShowCancel(Boolean.FALSE);
            SessionHelper.removeObject("fromProperty");
        } else {
            setRenderHeader(Boolean.TRUE);
            setRenderFooter(Boolean.TRUE);
            setShowSave(Boolean.TRUE);
            setShowCancel(Boolean.TRUE);
        }

        if (isOnlyView() && !getEntity().isNew()) {
            try {
                this.setSelectedPropertyType(0L);
                createTabView();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    private void createTabView() throws PersistenceBeanException, IllegalAccessException {
        TabView tabView = new TabView();
        tabView.setStyle("min-height:250px");
        tabView.setScrollable(true);
        tabView.setDynamic(false);
        List<Tab> tabList = createTabList();
        tabView.getChildren().addAll(tabList);
        setComponentTabView(tabView);
    }

    ValueExpression createValueExpression(String valueExpression, Class<?> valueType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(),
                valueExpression, valueType);
    }


    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        setProvinces(ComboboxHelper.fillList(Province.class,
                Order.asc("description")));
        getProvinces().add(new SelectItem(Province.FOREIGN_COUNTRY_ID,
                Province.FOREIGN_COUNTRY));
        this.setPropertyWrapperType(ComboboxHelper
                .fillList(PropertyWrapperType.class, false, true));

        this.setAddressCities(
                ComboboxHelper.fillList(new ArrayList<City>(), true));

        if (this.getEntity().getBirthProvince() != null) {
            this.setAddressProvinceId(
                    this.getEntity().getBirthProvince().getId());
            handleAddressProvinceChange();
        }

        setForeignCountry(getEntity().getForeignCountry());

        if (getForeignCountry()) {
            setAddressProvinceId(Province.FOREIGN_COUNTRY_ID);
        }

        if (this.getEntity().getBirthCity() != null) {
            this.setAddressCityId(this.getEntity().getBirthCity().getId());
        }

        this.setCountries(ComboboxHelper.fillList(Country.class,
                Order.asc("description"), new Criterion[]
                        {
                                Restrictions.ne("description", "ITALIA")
                        }));

        if (this.getEntity().getCountry() != null) {
            this.setSelectedCountryId(this.getEntity().getCountry().getId());
        }

        this.setSexTypes(ComboboxHelper.fillList(SexTypes.class, false));
        this.setSubjectTypes(ComboboxHelper.fillList(SubjectType.class, false));
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
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

        if (!getIsPhysicalPerson()
                && ValidationHelper
                .isNullOrEmpty(this.getEntity().getFiscalCode())
                && ValidationHelper
                .isNullOrEmpty(this.getEntity().getNumberVAT())) {
            addRequiredFieldException("form:numberVAT");
            addRequiredFieldException("form:codeFiscal");
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
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
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

    @Override
    public void goBack() {
        RedirectHelper.goTo(PageTypes.DATABASE_LIST);
    }

    public void editProperty() {
        SessionHelper.put("fromPrintRequest", Boolean.TRUE);
        SessionHelper.put("fromViewSubject", Boolean.TRUE);
        SessionHelper.put("editPropertyId", getEditPropertyId());
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("resizable", false);
        options.put("draggable", false);
        options.put("modal", true);
        options.put("contentHeight", Integer.valueOf(450));
        options.put("contentWidth", Integer.valueOf(1130));
        RequestContext.getCurrentInstance().openDialog("RealEstate", options,
                null);
    }

    private List<Tab> createTabList() throws PersistenceBeanException, IllegalAccessException {
        List<Tab> tabList = new ArrayList<>();

        List<Long> listIds = EstateSituationHelper.getIdSubjects(getEntity());
        listIds.add(getEntity().getId());
        setListIds(listIds);

        tabList.add(new RequestBindingWrapper(listIds, "subjectBean").getTab());
        Tab formalityTab = new FormalityBindingWrapper(getEntity(),getListIds()).getTab();
        formalityTab.setDisabled(Boolean.TRUE);
        tabList.add(formalityTab);
        //tabList.add(new FormalityBindingWrapper(getEntity(),listIds).getTab());

        Tab formalityCadastralTab = new  CadastralBindingWrapper(listIds, getEntity(), DocumentType.CADASTRAL).getTab();
        formalityCadastralTab.setDisabled(Boolean.TRUE);
        tabList.add(formalityCadastralTab);
       // tabList.add(new CadastralBindingWrapper(listIds, getEntity(), DocumentType.CADASTRAL).getTab());

        tabList.add(new SyntheticBindingWrapper(listIds).getTab());
        tabList.add(getEmptyTab("subjectViewMortgage"));

        tabList.add(new FormalitySubjectBindingWrapper(listIds).getTab());
        tabList.add(new VisureRTFBindingWrapper(getEntity()).getTab());
        tabList.add(new VisureDHBindingWrapper(getEntity()).getTab());
        List<RequestType> requestTypes = DaoManager.load(RequestType.class, new Criterion[]{Restrictions.isNotNull("name")});
        for(RequestType requestType : requestTypes) {
            tabList.add(new RequestTypeBindingWrapper(listIds,requestType, isOnlyView(), "subjectBean").getTab());
        }
        
//        tabList.add(getEmptyTab("subjectViewInvestigation"));
//        tabList.add(getEmptyTab("subjectViewRegistry"));
//        tabList.add(getEmptyTab("subjectViewChamber"));
//        tabList.add(getEmptyTab("subjectViewRealEstate"));
        formalityCadastralTab = new  CadastralBindingWrapper(listIds, getEntity(), DocumentType.INDIRECT_CADASTRAL).getTab();
        formalityCadastralTab.setDisabled(Boolean.TRUE);
        tabList.add(formalityCadastralTab);

        //tabList.add(new CadastralBindingWrapper(listIds, getEntity(), DocumentType.INDIRECT_CADASTRAL).getTab());
        tabList.add(getEmptyTab("subjectViewBilling"));
        return tabList;
    }

    private Tab getEmptyTab(String titleResourceId) {
        Tab tab = new Tab();
        tab.setTitle(ResourcesHelper.getString(titleResourceId));
        return tab;
    }

    public void editSubject() {
        this.setOnlyView(false);
    }

    public boolean getIsPhysicalPerson() {
        return getEntity().getTypeId() == null || SubjectType.PHYSICAL_PERSON
                .getId().equals(getEntity().getTypeId());
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

    public void loadAllegatiDocuments(Request request) throws PersistenceBeanException, IllegalAccessException {
        List<Document> documents = getAllegatiDocuments(request);
        setRequestDocuments(documents);
        executeJS("PF('documentDialog').show();");
    }

    private List<Document> getAllegatiDocuments(Request request)
            throws PersistenceBeanException, IllegalAccessException {
        List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request.id", request.getId()),
                Restrictions.eq("selectedForEmail", true)
                //Restrictions.eq("typeId", DocumentType.ALLEGATI.getId())
        });

        List<Document> formalities = DaoManager.load(Document.class, new CriteriaAlias[]{
                new CriteriaAlias("formality", "f", JoinType.INNER_JOIN),
                new CriteriaAlias("f.requestList", "r_f", JoinType.INNER_JOIN)
        }, new Criterion[]{
                //Restrictions.eq("r_f.id", request.getId()),
                //Restrictions.eq("typeId", DocumentType.ALLEGATI.getId())
                Restrictions.eq("request.id", request.getId()),
                Restrictions.eq("selectedForEmail", true)
        });

        if (!ValidationHelper.isNullOrEmpty(formalities)) {
            for (Document temp : formalities) {
                if (!documents.contains(temp)) {
                    documents.add(temp);
                }
            }
        }
        System.out.println("documents " + documents);
        return documents;
    }
    
   /* private List<Document> getAllegatiDocuments(Request request)
            throws PersistenceBeanException, IllegalAccessException {
            List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                    Restrictions.eq("request.id", request.getId()),
                    Restrictions.eq("typeId", DocumentType.ALLEGATI.getId())
            });

            List<Document> formalities = DaoManager.load(Document.class, new CriteriaAlias[]{
                    new CriteriaAlias("formality", "f", JoinType.INNER_JOIN),
                    new CriteriaAlias("f.requestList", "r_f", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("r_f.id", request.getId()),
                    Restrictions.eq("typeId", DocumentType.ALLEGATI.getId())
            });

            if (!ValidationHelper.isNullOrEmpty(formalities)) {
                for (Document temp : formalities) {
                    if (!documents.contains(temp)) {
                        documents.add(temp);
                    }
                }
            }
            return documents;
        }*/

    
    public void showFile(Request request) throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(request)) {
            try {
                this.setDownloadRequest(request);
                fillTemplates(request);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            if (!ValidationHelper.isNullOrEmpty(this.getTemplates())) {
                if (this.getTemplates().size() == 1) {
                    setSelectedTemplateId(
                            (Long) this.getTemplates().get(0).getValue());
                    generate();
                } else {
                    executeJS("PF('templates').show();");
                }
            } else {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        ResourcesHelper.getValidation("warning"),
                        ResourcesHelper.getValidation("noDocumentTemplates"));
            }
        }
    }

    public void generate() throws PersistenceBeanException, IllegalAccessException {
        if (getDownloadRequest() != null) {
            GeneralFunctionsHelper.showReport(getDownloadRequest(),
                    getSelectedTemplateId(), getCurrentUser(),
                    false, DaoManager.getSession());
            this.setDownloadRequest(null);
        }
    }

    private void fillTemplates(Request rec) {
        if (rec != null) {
            try {
                this.setTemplates(GeneralFunctionsHelper.fillTemplates(
                        DocumentGenerationPlaces.REQUEST_MANAGEMENT,
                        rec.getType(), null, DaoManager.getSession()));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void handleAddressProvinceChange() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (!Province.FOREIGN_COUNTRY_ID.equals(this.getAddressProvinceId())) {
            this.setForeignCountry(Boolean.FALSE);

            this.setAddressCities(ComboboxHelper.fillList(City.class,
                    Order.asc("description"), new Criterion[]{
                            Restrictions.eq("province.id",this.getAddressProvinceId()),
                            Restrictions.eq("external", Boolean.TRUE)
                    }));
        } else {
            this.setForeignCountry(Boolean.TRUE);
        }
    }

    public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(getEntity());
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
                closeDialog();
            }
        }
    }

    public void showPDF(Document doc) throws IOException {
        String randomFileName = FileHelper.getRandomFileName("1.pdf");
        FileHelper.writeFileToFolder(randomFileName, new File(FileHelper.getLocalFileDir()), FileHelper.loadContentByPath(doc.getPath()));
        RedirectHelper.sendRedirect("/File/" + randomFileName + "?pfdrid_c=true", true);
    }

    public void downloadFormalityPDF(Long doc) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(doc)) {
            Document document = DaoManager.get(Document.class, doc);
            File file = new File(document.getPath());
            String title = document.getTitle();
            if (document.getPath().endsWith(".tif") || document.getPath().endsWith(".htm")) {
                title = FileHelper.getFileName(document.getPath());
            } else if (!title.endsWith(".pdf")) {
                if (title.contains(".")) {
                    int point = title.lastIndexOf(".");
                    title = title.substring(0, point + 1) + "pdf";
                } else {
                    title += ".pdf";
                }
            }
            try {
                FileHelper.sendFile(title, new FileInputStream(file), (int) file.length());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void downloadVisureRTF(Long visureRTFId) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        VisureManageHelper.downloadVisureRTF(visureRTFId);
    }

    public void downloadPropertyPDF(Long doc) {
        if (!ValidationHelper.isNullOrEmpty(doc)) {
            String projectUrl = this.getRequest().getHeader("referer");
            projectUrl = projectUrl.substring(0, projectUrl.indexOf(this.getCurrentPage().getPagesContext())) + "/";
            PrintPDFHelper.generatePDFOnDocument(doc, projectUrl);
        }
    }

    public void downloadEstateFormalityPDF(Long doc) {
        if (!ValidationHelper.isNullOrEmpty(doc)) {
            String projectUrl = this.getRequest().getHeader("referer");
            projectUrl = projectUrl.substring(0, projectUrl.indexOf(this.getCurrentPage().getPagesContext())) + "/";
            PrintPDFHelper.generatePDFOnDocument(doc, projectUrl);
        }
    }
    
    public void downloadPdfFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        try {
            String body = RequestHelper.getPdfRequestBody(null, this.getEntity());
            
            FileHelper.sendFile("soggetto-" + this.getEntityId() + ".pdf",
                    PrintPDFHelper.convertToPDF(null, body, null,
                            DocumentType.ESTATE_FORMALITY));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void goToFormality(Long id) {
        if (id != null) {
            RedirectHelper.goTo(PageTypes.REQUEST_FORMALITY, id);
        }
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

    public void loadTabs(){
        List<Tab> tabList = new ArrayList<>();
        ListIterator<UIComponent> itr = getComponentTabView().getChildren().listIterator();
        while (itr.hasNext())
        {
            Tab tab = (Tab)itr.next();
            String title = tab.getTitle();
            try {
                if(!ValidationHelper.isNullOrEmpty(title)){
                    if(title.startsWith(ResourcesHelper.getString("subjectViewFormality"))){
                        FormalityBindingWrapper formalityTab = new FormalityBindingWrapper(getEntity(), getListIds());
                        formalityTab.loadData(Boolean.FALSE);
                        itr.set(formalityTab.getTab());
                        RequestContext.getCurrentInstance().update("tabView");
                    }else if(title.startsWith(ResourcesHelper.getString("subjectViewCadastral"))){
                        CadastralBindingWrapper cadastralBindingWrapper = new CadastralBindingWrapper(getListIds(), getEntity(), DocumentType.CADASTRAL);
                        cadastralBindingWrapper.loadData(true);
                        itr.set(cadastralBindingWrapper.getTab());
                        RequestContext.getCurrentInstance().update("tabView");
                    }else if(title.startsWith(ResourcesHelper.getString("subjectViewDocuments"))){
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
    public List<Long> getListIds() {
        return listIds;
    }

    public void setListIds(List<Long> listIds) {
        this.listIds = listIds;
    }
}
