package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.comparators.CommercialValueHistoryComparator;
import it.nexera.ris.common.comparators.EstimateOMIComparator;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.RealEstateType;
import it.nexera.ris.common.enums.RelationshipType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.omi.OMIHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.DocumentRelationshipWrapper;
import it.nexera.ris.web.beans.wrappers.logic.DocumentWrapper;
import it.nexera.ris.web.beans.wrappers.logic.SubjectRelationshipWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UploadDocumentWrapper;
import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@ManagedBean(name = "realEstateBean")
@ViewScoped
public class RealEstateBean extends EntityEditPageBean<Property>
        implements Serializable {

    private static final long serialVersionUID = 1433621662981493692L;

    private boolean onlyView;

    private List<SelectItem> provinces;

    private List<SelectItem> addressCities;

    private List<SelectItem> realEstateTypes;

    private List<SelectItem> categories;

    private Long addressProvinceId;

    private Long addressCityId;

    private Long categoryId;

    private CadastralData newCadastralData;

    private List<CadastralData> cadastralData;

    private Integer tempId;

    private Integer deleteTempId;

    private List<CadastralData> cadastralDataForDelete;

    private Boolean renderHeader;

    private Boolean renderFooter;

    private Boolean showSave;

    private Boolean showCancel;

    private Boolean fromRequestPrint;

    private List<EstimateOMIHistory> estimateOMIHistory;

    private List<CommercialValueHistory> commercialValueHistory;

    private String currencyValue;

    private Date currencyDate;

    private String commercialValue;

    private Date commercialDate;

    private List<SubjectRelationshipWrapper> subjectWrappers;

    private Long relationshipEditId;

    private SubjectRelationshipWrapper editSubjectWrapper;

    private AtomicLong ingexSubjectWrapper;

    private List<SelectItem> allSubjects;

    private Long relationshipDeleteId;

    private List<SubjectRelationshipWrapper> subjectWrappersForDelete;

    private List<DocumentRelationshipWrapper> documentWrappers;

    private AtomicLong ingexDocumentWrapper;

    private Long downloadFileId;

    private Long selectedTypeId;

    private List<SelectItem> documentTypes;

    private String documentTitle;

    private UploadedFile document;

    private Date documentDate;

    private DocumentWrapper currentDocumentWrapper;

    private Request requestEntity;

    private String selectedSubject;

    private Boolean saveScheda;

    private String zone;

    @Override
    protected void preLoad() throws PersistenceBeanException {
        if ("true".equalsIgnoreCase(
                this.getRequestParameter(RedirectHelper.ONLY_VIEW))) {
            setOnlyView(true);
        }
    }

    @Override
    protected void pageLoadStatic() throws PersistenceBeanException {
        if (SessionHelper.get("fromPrintRequest") != null
                && ((Boolean) SessionHelper.get("fromPrintRequest"))) {
            if (SessionHelper.get("fromViewSubject") != null
                    && ((Boolean) SessionHelper.get("fromViewSubject"))) {
                setOnlyView(true);
                SessionHelper.removeObject("fromViewSubject");
            }

            setRenderHeader(Boolean.FALSE);
            setRenderFooter(Boolean.FALSE);
            setShowSave(Boolean.FALSE);
            setShowCancel(Boolean.FALSE);
            setFromRequestPrint(Boolean.TRUE);

            if (SessionHelper.get("editPropertyId") != null) {
                this.setEntityId((Long) SessionHelper.get("editPropertyId"));
            }

            SessionHelper.removeObject("fromPrintRequest");
            SessionHelper.removeObject("editPropertyId");
        } else {
            setRenderHeader(Boolean.TRUE);
            setRenderFooter(Boolean.TRUE);
            setShowSave(Boolean.TRUE);
            setShowCancel(Boolean.TRUE);
            setFromRequestPrint(Boolean.FALSE);
        }
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        String requestId = getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
        if (!ValidationHelper.isNullOrEmpty(requestId)) {
            setRequestEntity(DaoManager.get(Request.class, Long.parseLong(requestId)));
        }

        this.tempId = 0;
        this.setIngexSubjectWrapper(new AtomicLong());
        this.setIngexDocumentWrapper(new AtomicLong());
        this.setEditSubjectWrapper(new SubjectRelationshipWrapper(getIngexSubjectWrapper().incrementAndGet()));

        this.setSubjectWrappersForDelete(new ArrayList<>());

        this.setRealEstateTypes(ComboboxHelper.fillList(RealEstateType.class, false));
        this.setCategories(ComboboxHelper.fillList(CadastralCategory.class));
        this.setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
        this.setAddressCities(ComboboxHelper.fillList(new ArrayList<City>(), true));

        if (this.getEntity().getProvince() != null) {
            this.setAddressProvinceId(this.getEntity().getProvince().getId());
            handleAddressProvinceChange();
        }

        if (this.getEntity().getCity() != null) {
            this.setAddressCityId(this.getEntity().getCity().getId());
        }

        if (this.getEntity().getCategory() != null) {
            this.setCategoryId(this.getEntity().getCategory().getId());
        }

        if (this.getEntity().getZone() != null) {
            this.setZone(this.getEntity().getZone());
        }

        this.setNewCadastralData(new CadastralData());
        this.setCadastralData(new ArrayList<>());
        this.addCadastralData(this.getEntity().getCadastralData());

        if (getEntity().getEstimateOMIHistory() != null) {
            setEstimateOMIHistory(getEntity().getEstimateOMIHistory());
            getEstimateOMIHistory().sort(new EstimateOMIComparator());
        }

        if (getEntity().getCommercialValueHistory() != null) {
            setCommercialValueHistory(getEntity().getCommercialValueHistory());
            getCommercialValueHistory().sort(new CommercialValueHistoryComparator());
        }

        if (!getEntity().isNew()) {
            List<Relationship> relationships = DaoManager.load(Relationship.class, new Criterion[]{
                    Restrictions.eq("property.id", getEntity().getId())
            });

            setSubjectWrappers(relationships.stream().filter(r -> !ValidationHelper.isNullOrEmpty(r.getSubject()))
                    .map(r -> new SubjectRelationshipWrapper(r, getIngexSubjectWrapper().incrementAndGet()))
                    .collect(Collectors.toList()));

            setDocumentWrappers(relationships.stream().filter(r -> !ValidationHelper.isNullOrEmpty(r.getTableId()))
                    .map(r -> new DocumentRelationshipWrapper(r, getIngexDocumentWrapper().incrementAndGet()))
                    .filter(distinctByKey(dw -> dw.getDocument().getId()))
                    .collect(Collectors.toList()));
        } else {

            setSubjectWrappers(new ArrayList<>());
            setDocumentWrappers(new ArrayList<>());
            if (!ValidationHelper.isNullOrEmpty(getRequestEntity())) {
                Relationship relationship = new Relationship();
                relationship.setProperty(getEntity());
                relationship.setSubject(getRequestEntity().getSubject());
                getSubjectWrappers().add(new SubjectRelationshipWrapper(relationship, getIngexSubjectWrapper().incrementAndGet()));
            }

        }

        this.setDocumentTypes(ComboboxHelper.fillList(DocumentType.class, false));

        setCurrentDocumentWrapper(new DocumentWrapper(null, null, null));
    }

    public void editRelationship() {
        if (!ValidationHelper.isNullOrEmptyMultiple(getRelationshipEditId(), getSubjectWrappers())) {
            for (SubjectRelationshipWrapper sw : getSubjectWrappers()) {
                if (getRelationshipEditId().equals(sw.getId())) {
                    setEditSubjectWrapper(sw);
                    setSelectedSubject(sw.getSubject().toString());
                }
            }
        }
    }

    public List<String> filterSubjects(String query) throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.load(Subject.class, new Criterion[]{
                Restrictions.or(
                        Restrictions.like("name", query, MatchMode.ANYWHERE),
                        Restrictions.like("surname", query, MatchMode.ANYWHERE),
                        Restrictions.like("businessName", query, MatchMode.ANYWHERE),
                        Restrictions.like("numberVAT", query, MatchMode.ANYWHERE),
                        Restrictions.like("fiscalCode", query, MatchMode.ANYWHERE)
                ),
                Restrictions.or(
                        Restrictions.ne("numberVAT", ""),
                        Restrictions.ne("fiscalCode", "")
                )}, new Order[]{}, 10).stream().map(Subject::toString).collect(Collectors.toList());
    }

    public void addRelationship() throws PersistenceBeanException, IllegalAccessException {
        if (getAllSubjects() == null) {
            this.setAllSubjects(ComboboxHelper.fillList(Subject.class));
        }
        setEditSubjectWrapper(new SubjectRelationshipWrapper(getIngexSubjectWrapper().incrementAndGet()));
        setSelectedSubject(null);
    }

    public void saveRelationship() {
        this.cleanValidation();

        if (ValidationHelper.isNullOrEmpty(getSelectedSubject())) {
            addRequiredFieldException("form:acSimple");

            return;
        }
        if (ValidationHelper.isNullOrEmpty(getEditSubjectWrapper().getQuote1())
                || ValidationHelper.isNullOrEmpty(getEditSubjectWrapper().getQuote2())
                || Double.parseDouble(getEditSubjectWrapper().getQuote1().replaceAll(",", "."))
                / Double.parseDouble(getEditSubjectWrapper().getQuote2().replaceAll(",", ".")) > 1.0) {
            addRequiredFieldException("form:inputQuote1");
            addRequiredFieldException("form:inputQuote2");
            return;
        }

        try {
            String[] subjectPieces = getSelectedSubject().trim().split(" ");
            List<Subject> subjectList = DaoManager.load(Subject.class, new Criterion[]{
                    Restrictions.or(
                            Restrictions.eq("fiscalCode", subjectPieces[subjectPieces.length - 1]),
                            Restrictions.eq("numberVAT", subjectPieces[subjectPieces.length - 1])
                    )
            });
            Subject finalSubject = null;
            if (subjectList.size() == 1) {
                finalSubject = subjectList.get(0);
            } else {
                finalSubject = subjectList.stream().filter(s -> s.getBusinessName().equals(
                        getSelectedSubject().replaceAll(subjectPieces[subjectPieces.length - 1], "")))
                        .findAny().orElse(null);
                if (finalSubject == null) {
                    finalSubject = subjectList.stream().filter(s -> s.getSurname().startsWith(subjectPieces[0]))
                            .findAny().orElse(null);
                }
            }
            Subject subject = finalSubject;
            if (getRelationshipEditId() == null && getSubjectWrappers().stream()
                    .anyMatch(sw -> subject.getId().equals(sw.getSubject().getId()))) {
                addRequiredFieldException("form:acSimple");
                return;
            }
            getEditSubjectWrapper().getRelationship().setSubject(subject);
            getEditSubjectWrapper().setSubject(subject);
            getEditSubjectWrapper().setQuote(String.format("%s/%s",
                    getEditSubjectWrapper().getQuote1().replaceAll(",", "."),
                    getEditSubjectWrapper().getQuote2().replaceAll(",", ".")));
            getEditSubjectWrapper().setPropertyTypeStr(getEditSubjectWrapper().getPropertyType().getDescription());

            if (getSubjectWrappers().stream().anyMatch(sw -> getEditSubjectWrapper().getId().equals(sw.getId()))) {
                getSubjectWrappers().stream()
                        .filter(sw -> getEditSubjectWrapper().getId().equals(sw.getId()))
                        .forEach(sw -> sw = getEditSubjectWrapper());
            } else {
                getSubjectWrappers().add(getEditSubjectWrapper());
            }
            setRelationshipEditId(null);

            executeJS("PF('addSubjectDLGWV').hide();");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void deleteSubject() {
        if (!ValidationHelper.isNullOrEmpty(getRelationshipDeleteId())) {
            for (int i = 0; i < getSubjectWrappers().size(); ++i) {
                if (getRelationshipDeleteId().equals(getSubjectWrappers().get(i).getId())) {
                    getSubjectWrappersForDelete().add(getSubjectWrappers().remove(i));

                    break;
                }
            }
        }
    }

    public void openSubjectDlg() {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("resizable", false);
        options.put("draggable", false);
        options.put("modal", true);
        options.put("contentHeight", Integer.valueOf(450));
        options.put("contentWidth", Integer.valueOf(1130));
        SessionHelper.put("fromProperty", Boolean.TRUE);

        RequestContext.getCurrentInstance().openDialog("Subject", options, null);
    }

    public void addNewCommercialValue() {
        this.cleanValidation();

        if (ValidationHelper.isNullOrEmpty(getCommercialValue())) {
            markInvalid("commercialValue", "requiredField");

            return;
        }

        if (getCommercialDate() == null) {
            setCommercialDate(new Date());
        }

        CommercialValueHistory history = new CommercialValueHistory();

        history.setCommercialValue(getCommercialValue());
        history.setCommercialValueDate(getCommercialDate());

        setCommercialValue(null);
        setCommercialDate(null);

        try {
            history.setUser(DaoManager.get(User.class, getCurrentUser().getId()));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (getCommercialValueHistory() == null) {
            setCommercialValueHistory(new ArrayList<>());
        }

        getCommercialValueHistory().add(history);

        getCommercialValueHistory().sort(new CommercialValueHistoryComparator());

        executeJS("PF('addNewCommercialValueDlgWV').hide();");
    }

    public void addNewEstimateOMI() {
        this.cleanValidation();

        if (ValidationHelper.isNullOrEmpty(getCurrencyValue())) {
            markInvalid("estimateOMIValue", "requiredField");

            return;
        }

        if (getCurrencyDate() == null) {
            setCurrencyDate(new Date());
        }

        EstimateOMIHistory history = new EstimateOMIHistory();

        history.setEstimateOMI(getCurrencyValue());
        history.setPropertyAssessmentDate(getCurrencyDate());

        setCurrencyValue(null);
        setCurrencyDate(null);

        try {
            history.setUser(DaoManager.get(User.class, getCurrentUser().getId()));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (getEstimateOMIHistory() == null) {
            setEstimateOMIHistory(new ArrayList<>());
        }

        getEstimateOMIHistory().add(history);
        getEstimateOMIHistory().sort(new EstimateOMIComparator());

        executeJS("PF('addNewOMIDlgWV').hide();");
    }

    public void cancelSave() {
        this.cleanValidation();
        setCurrencyValue(null);
        setCurrencyDate(null);
        setCommercialValue(null);
        setCommercialDate(null);
    }

    private void addCadastralData(List<CadastralData> list) {
        if (this.getCadastralData() == null) {
            this.setCadastralData(new ArrayList<>());
        }

        if (list != null) {
            for (CadastralData data : list) {
                data.setTempId(++tempId);

                this.getCadastralData().add(data);
            }
        }
    }

    private void addCadastralData(CadastralData data) {
        if (this.getCadastralData() == null) {
            this.setCadastralData(new ArrayList<>());
        }

        if (this.getSaveScheda() != null && !this.getSaveScheda()) {
            data.setScheda("");
        }

        if (data != null) {
            data.setTempId(++tempId);

            this.getCadastralData().add(data);
        }

        setSaveScheda(false);
    }

    public void deleteCadastralData() {
        if (this.getCadastralData() != null) {
            for (CadastralData data : this.getCadastralData()) {
                if (data.getTempId().equals(getDeleteTempId())) {
                    if (this.getCadastralDataForDelete() == null) {
                        this.setCadastralDataForDelete(new ArrayList<>());
                    }

                    this.getCadastralDataForDelete().add(data);
                    this.getCadastralData().remove(data);

                    break;
                }
            }
        }
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getType())) {
            addRequiredFieldException("form:realEstateType");
        }

        if (ValidationHelper.isNullOrEmpty(this.getAddressProvinceId())) {
            addRequiredFieldException("form:province");
        }

        if (ValidationHelper.isNullOrEmpty(this.getAddressCityId())) {
            addRequiredFieldException("form:city");
        }

    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if (getEntity().isNew()) {
            Property propertyDB = RealEstateHelper.getExistingPropertyByCD(getCadastralData(), DaoManager.getSession());
            if (propertyDB != null) {
                if (RealEstateHelper.propertyChanged(propertyDB, getEntity())) {
                    propertyDB.setModified(true);
                    DaoManager.save(propertyDB);
                } else {
                    setEntity(RealEstateHelper.modifyExistingProperty(propertyDB, getEntity(), DaoManager.getSession()));
                }
            }
        }
        if (!ValidationHelper.isNullOrEmpty(this.getAddressProvinceId())) {
            Province province = DaoManager.get(Province.class,
                    this.getAddressProvinceId());

            getEntity().setProvince(province);
        } else {
            getEntity().setProvince(null);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getAddressCityId())) {
            City city = DaoManager.get(City.class, this.getAddressCityId());

            getEntity().setCity(city);
        } else {
            getEntity().setCity(null);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getZone())) {
            getEntity().setZone(this.getZone());
        } else {
            getEntity().setZone(null);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getCategoryId())) {
            getEntity().setCategory(DaoManager.get(CadastralCategory.class, this.getCategoryId()));
        } else {
            getEntity().setCategory(null);
        }

        if (!ValidationHelper.isNullOrEmpty(getRequestEntity())) {
            if (ValidationHelper.isNullOrEmpty(getEntity().getRequestList())) {
                getEntity().setRequestList(new LinkedList<>());
            }
            getEntity().getRequestList().add(getRequestEntity());
        }
        if (getEntity().getType() == null) {
            getEntity().setType(RealEstateType.BUILDING.getId());
        }

        RealEstateHelper.saveCadastralData(getEntity(), getCadastralData(), getCadastralDataForDelete(),
                DaoManager.getSession());
        DaoManager.save(getEntity());
        RealEstateHelper.saveHistory(getEntity(), getEstimateOMIHistory(), getCommercialValueHistory(),
                DaoManager.getSession());
        saveRelationships();
    }

    private void saveRelationships() {
        for (SubjectRelationshipWrapper wrapper : getSubjectWrappers()) {
            wrapper.getRelationship().setQuote(wrapper.getQuote());
            wrapper.getRelationship().setPropertyType(wrapper.getPropertyTypeStr());
            wrapper.getRelationship().setProperty(getEntity());

            if (wrapper.getRelationship().getRelationshipTypeId() == null) {
                wrapper.getRelationship().setRelationshipTypeId(
                        RelationshipType.MANUAL_ENTRY.getId());
            }

            try {
                DaoManager.save(wrapper.getRelationship());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        getSubjectWrappersForDelete().stream()
                .filter(sw -> !sw.getRelationship().isNew()).forEach(sw -> {
            try {
                DaoManager.remove(sw.getRelationship());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        });

        getDocumentWrappers().stream()
                .filter(dw -> dw.getRelationship().isNew()).forEach(dw -> {
            dw.getRelationship().setProperty(getEntity());
            dw.getRelationship().setRelationshipTypeId(
                    RelationshipType.MANUAL_ENTRY.getId());

            try {
                DaoManager.save(dw.getRelationship());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        });
    }

    public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(getEntity());
    }

    public void findZoneByKMLFile() throws Exception {
        setZone(String.join("-", OMIHelper.findZoneByPropertyInKML(getEntity())));
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

    public void onEditSubjectDlgClose(SelectEvent event) {
        Subject subject = (Subject) event.getObject();

        if (subject != null && !subject.isNew()) {
            getAllSubjects()
                    .add(new SelectItem(subject.getId(), subject.toString()));
        }
    }

    @Override
    public void goBack() {
        if (ValidationHelper.isNullOrEmpty(getRequestEntity())) {
            RedirectHelper.goTo(PageTypes.DATABASE_LIST);
        } else {
            RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_VIEW,
                    getRequestEntity().getId(), null);
        }

    }

    public void downloadFile() {
        if (!ValidationHelper.isNullOrEmpty(getDownloadFileId())) {
            Document doc = null;

            try {
                doc = DaoManager.get(Document.class, getDownloadFileId());
            } catch (Exception e) {
                LogHelper.log(log, e);
            } finally {
                setDownloadFileId(null);
            }

            if (doc != null) {
                File file = new File(doc.getPath());
                try {
                    FileHelper.sendFile(file.getName(),
                            new FileInputStream(file), (int) file.length());
                } catch (FileNotFoundException e) {
                    FacesMessage msg = new FacesMessage(
                            ResourcesHelper.getValidation("noDocumentOnServer"),
                            "");
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                }
            }
        }
    }

    public void cancelSaveFile() {
        this.setDocument(null);
        this.setDocumentTitle(null);
        this.setDocumentDate(null);
    }

    public void saveFile() throws PersistenceBeanException, IllegalAccessException {
        UploadDocumentWrapper uploadDocument = GeneralFunctionsHelper
                .handleFileUpload(getDocument().getFileName(),
                        getDocument().getContents(), getSelectedTypeId(),
                        getDocumentTitle(), getDocumentDate(), null,
                        null, DaoManager.getSession());

        if (uploadDocument != null) {
            if (uploadDocument.getDocument() != null) {
                getDocumentWrappers().add(new DocumentRelationshipWrapper(
                        uploadDocument.getDocument(),
                        getIngexDocumentWrapper().incrementAndGet()));
            }
        }

        this.setDocumentTitle(null);
        this.setDocumentDate(null);
    }

    public void addCadastralData() {
        boolean isValid = true;

        this.cleanValidation();

        if (ValidationHelper.isNullOrEmpty(getNewCadastralData().getSheet())) {
            markInvalid("addCadastralDataDLGsheet", "requiredField");
            isValid = false;
        }

        if (ValidationHelper.isNullOrEmpty(getNewCadastralData().getParticle())) {
            markInvalid("addCadastralDataDLGparticle", "requiredField");
            isValid = false;
        }

        if (this.getIsBuilding() && ValidationHelper
                .isNullOrEmpty(getNewCadastralData().getSub())) {
            markInvalid("addCadastralDataDLGsub", "requiredField");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        this.addCadastralData(getNewCadastralData());
        this.setNewCadastralData(new CadastralData());

        this.executeJS("updateForm();");
    }

    public void handleAddressProvinceChange() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getRequestEntity())
                && !ValidationHelper.isNullOrEmpty(getRequestEntity().getAggregationLandChargesRegistry())) {
            List<LandChargesRegistry> list = DaoManager.load(LandChargesRegistry.class, new CriteriaAlias[]{
                    new CriteriaAlias("aggregationLandChargesRegistries", "aggr", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("aggr.id", getRequestEntity().getAggregationLandChargesRegistry().getId())
            });
            Boolean containsProvince = list.stream().map(LandChargesRegistry::getProvinces).flatMap(List::stream)
                    .anyMatch(p -> p.getId().equals(getAddressProvinceId()));
            if (!containsProvince) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        ResourcesHelper.getValidation("warning"),
                        ResourcesHelper.getValidation("notConservatoryProvince"));
            }
        }
        this.setAddressCities(ComboboxHelper.fillList(City.class,
                Order.asc("description"),
                new Criterion[]{
                        Restrictions.eq("province.id", this.getAddressProvinceId()),
                        Restrictions.eq("external", Boolean.TRUE)
                }));
        
    }

    public boolean getIsBuilding() {
        return this.getEntity().getType() == null || RealEstateType.BUILDING
                .getId().equals(this.getEntity().getType());
    }

    public String getCityDescription() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        return this.loadDescriptionById(City.class, this.getAddressCityId());
    }

    public String getProvinceDescription() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        return this.loadDescriptionById(Province.class,
                this.getAddressProvinceId());
    }

    public String getCategoryCode() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        return this.loadCodeById(CadastralCategory.class,
                this.getCategoryId());
    }

    public String getCategoryDescription() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        return this.loadDescriptionById(CadastralCategory.class,
                this.getCategoryId());
    }

    public void editSubject() {
        this.setOnlyView(false);
    }

    public boolean isOnlyView() {
        return onlyView;
    }

    public void setOnlyView(boolean onlyView) {
        this.onlyView = onlyView;
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

    public List<SelectItem> getRealEstateTypes() {
        return realEstateTypes;
    }

    public void setRealEstateTypes(List<SelectItem> realEstateTypes) {
        this.realEstateTypes = realEstateTypes;
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

    public List<SelectItem> getCategories() {
        return categories;
    }

    public void setCategories(List<SelectItem> categories) {
        this.categories = categories;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public CadastralData getNewCadastralData() {
        return newCadastralData;
    }

    public void setNewCadastralData(CadastralData newCadastralData) {
        this.newCadastralData = newCadastralData;
    }

    public List<CadastralData> getCadastralData() {
        return cadastralData;
    }

    public void setCadastralData(List<CadastralData> cadastralData) {
        this.cadastralData = cadastralData;
    }

    public Integer getDeleteTempId() {
        return deleteTempId;
    }

    public void setDeleteTempId(Integer deleteTempId) {
        this.deleteTempId = deleteTempId;
    }

    public List<CadastralData> getCadastralDataForDelete() {
        return cadastralDataForDelete;
    }

    public void setCadastralDataForDelete(
            List<CadastralData> cadastralDataForDelete) {
        this.cadastralDataForDelete = cadastralDataForDelete;
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

    public Boolean getFromRequestPrint() {
        return fromRequestPrint;
    }

    public void setFromRequestPrint(Boolean fromRequestPrint) {
        this.fromRequestPrint = fromRequestPrint;
    }

    public List<EstimateOMIHistory> getEstimateOMIHistory() {
        return estimateOMIHistory;
    }

    public void setEstimateOMIHistory(
            List<EstimateOMIHistory> estimateOMIHistory) {
        this.estimateOMIHistory = estimateOMIHistory;
    }

    public List<CommercialValueHistory> getCommercialValueHistory() {
        return commercialValueHistory;
    }

    public void setCommercialValueHistory(
            List<CommercialValueHistory> commercialValueHistory) {
        this.commercialValueHistory = commercialValueHistory;
    }

    public String getCurrencyValue() {
        return currencyValue;
    }

    public void setCurrencyValue(String currencyValue) {
        this.currencyValue = currencyValue;
    }

    public Date getCurrencyDate() {
        return currencyDate;
    }

    public void setCurrencyDate(Date currencyDate) {
        this.currencyDate = currencyDate;
    }

    public String getCommercialValue() {
        return commercialValue;
    }

    public void setCommercialValue(String commercialValue) {
        this.commercialValue = commercialValue;
    }

    public Date getCommercialDate() {
        return commercialDate;
    }

    public void setCommercialDate(Date commercialDate) {
        this.commercialDate = commercialDate;
    }

    public List<SubjectRelationshipWrapper> getSubjectWrappers() {
        return subjectWrappers;
    }

    public void setSubjectWrappers(
            List<SubjectRelationshipWrapper> subjectWrappers) {
        this.subjectWrappers = subjectWrappers;
    }

    public Long getRelationshipEditId() {
        return relationshipEditId;
    }

    public void setRelationshipEditId(Long relationshipEditId) {
        this.relationshipEditId = relationshipEditId;
    }

    public SubjectRelationshipWrapper getEditSubjectWrapper() {
        return editSubjectWrapper;
    }

    public void setEditSubjectWrapper(
            SubjectRelationshipWrapper editSubjectWrapper) {
        this.editSubjectWrapper = editSubjectWrapper;
    }

    public AtomicLong getIngexSubjectWrapper() {
        return ingexSubjectWrapper;
    }

    public void setIngexSubjectWrapper(AtomicLong ingexSubjectWrapper) {
        this.ingexSubjectWrapper = ingexSubjectWrapper;
    }

    public List<SelectItem> getAllSubjects() {
        return allSubjects;
    }

    public void setAllSubjects(List<SelectItem> allSubjects) {
        this.allSubjects = allSubjects;
    }

    public String getSelectedSubject() {
        return selectedSubject;
    }

    public void setSelectedSubject(String selectedSubject) {
        this.selectedSubject = selectedSubject;
    }

    public Long getRelationshipDeleteId() {
        return relationshipDeleteId;
    }

    public void setRelationshipDeleteId(Long relationshipDeleteId) {
        this.relationshipDeleteId = relationshipDeleteId;
    }

    public List<SubjectRelationshipWrapper> getSubjectWrappersForDelete() {
        return subjectWrappersForDelete;
    }

    public void setSubjectWrappersForDelete(
            List<SubjectRelationshipWrapper> subjectWrappersForDelete) {
        this.subjectWrappersForDelete = subjectWrappersForDelete;
    }

    public List<DocumentRelationshipWrapper> getDocumentWrappers() {
        return documentWrappers;
    }

    public void setDocumentWrappers(
            List<DocumentRelationshipWrapper> documentWrappers) {
        this.documentWrappers = documentWrappers;
    }

    public AtomicLong getIngexDocumentWrapper() {
        return ingexDocumentWrapper;
    }

    public void setIngexDocumentWrapper(AtomicLong ingexDocumentWrapper) {
        this.ingexDocumentWrapper = ingexDocumentWrapper;
    }

    public Long getDownloadFileId() {
        return downloadFileId;
    }

    public void setDownloadFileId(Long downloadFileId) {
        this.downloadFileId = downloadFileId;
    }

    public Long getSelectedTypeId() {
        return selectedTypeId;
    }

    public void setSelectedTypeId(Long selectedTypeId) {
        this.selectedTypeId = selectedTypeId;
    }

    public List<SelectItem> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<SelectItem> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public UploadedFile getDocument() {
        return document;
    }

    public void setDocument(UploadedFile document) {
        this.document = document;
    }

    public Date getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(Date documentDate) {
        this.documentDate = documentDate;
    }

    public DocumentWrapper getCurrentDocumentWrapper() {
        return currentDocumentWrapper;
    }

    public void setCurrentDocumentWrapper(
            DocumentWrapper currentDocumentWrapper) {
        this.currentDocumentWrapper = currentDocumentWrapper;
    }

    public Request getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(Request requestEntity) {
        this.requestEntity = requestEntity;
    }

    public Boolean getSaveScheda() {
        return saveScheda;
    }

    public void setSaveScheda(Boolean saveScheda) {
        this.saveScheda = saveScheda;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}