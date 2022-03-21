package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.comparators.CadastralCategoryComparator;
import it.nexera.ris.common.comparators.CommercialValueHistoryComparator;
import it.nexera.ris.common.comparators.EstimateOMIComparator;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.RealEstateType;
import it.nexera.ris.common.enums.RelationshipType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.SubjectRelationshipWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import static it.nexera.ris.common.helpers.TemplatePdfTableHelper.distinctByKey;

@ManagedBean(name = "realEstateEditBean")
@ViewScoped
public class RealEstateEditBean extends EntityEditPageBean<Property> implements Serializable {

    private static final long serialVersionUID = -3465566421308411955L;

    private Request requestEntity;

    private List<SelectItem> provinces;

    private List<SelectItem> addressCities;

    private List<SelectItem> realEstateTypes;

    private Long addressProvinceId;

    private Long addressCityId;

    private Long selectedTypeId;

    private boolean renderForm;

    private boolean showHistoryTables;

    private boolean saveScheda;

    private CadastralData newCadastralData;

    private CadastralData newCadastralDataDlg;

    private List<CadastralData> cadastralDataList;

    private List<CadastralData> cadastralDataForDeleteList;

    private AtomicInteger tempId;

    private Integer deleteTempId;

    private List<SelectItem> categories;

    private Long categoryId;

    private List<SelectItem> allSubjects;

    private List<SubjectRelationshipWrapper> subjectWrappersForDelete;

    private List<SubjectRelationshipWrapper> subjectWrappers;

    private SubjectRelationshipWrapper editSubjectWrapper;

    private Long relationshipEditId;

    private Long relationshipDeleteId;

    private Long downloadFileId;

    private String selectedSubject;

    private AtomicLong ingexSubjectWrapper;

    private boolean needValidateCadastral;

    private boolean saveDuplicate;

    private List<EstimateOMIHistory> estimateOMIHistory;

    private List<CommercialValueHistory> commercialValueHistory;

    private String currencyValue;

    private Date currencyDate;

    private String commercialValue;

    private Date commercialDate;

    private List<CadastralCategory> cadastralCategoryList;

    private static final String CADASTRAL_CATEGORY_CODE_FOR_DISPLAY = "R";

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {

        String requestId = getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
        if (!ValidationHelper.isNullOrEmpty(requestId)) {
            setRequestEntity(DaoManager.get(Request.class, Long.parseLong(requestId)));
        }

        if (!getEntity().isNew()) {
            setSelectedItems();
        }
        loadSelectLists();
        checkCanRender();
        setTempId(new AtomicInteger());
        setNewCadastralData(new CadastralData());
        setNewCadastralDataDlg(new CadastralData());
        setCadastralDataList(new ArrayList<>());
        if (!getEntity().isNew()) {
            for (CadastralData cd : CollectionUtils.emptyIfNull(getEntity().getCadastralData())
                    .stream().filter(distinctByKey(x -> x.getId()))
                    .collect(Collectors.toList())) {
                addCadastralData(cd);
            }
        }
        updateFieldsCadastralData();
        setIngexSubjectWrapper(new AtomicLong());
        setEditSubjectWrapper(new SubjectRelationshipWrapper(getIngexSubjectWrapper().incrementAndGet()));
        setNeedValidateCadastral(true);

        if (getEntity().isNew()) {
            setSubjectWrappers(new ArrayList<>());
            if (!ValidationHelper.isNullOrEmpty(getRequestEntity())
                    && !ValidationHelper.isNullOrEmpty(getRequestEntity().getSubject())) {
                Relationship relationship = new Relationship();
                relationship.setProperty(getEntity());
                relationship.setSubject(getRequestEntity().getSubject());
                getSubjectWrappers().add(new SubjectRelationshipWrapper(relationship, getIngexSubjectWrapper().incrementAndGet()));
            }
        } else {
            List<Relationship> relationships = DaoManager.load(Relationship.class, new Criterion[]{
                    Restrictions.eq("property.id", getEntity().getId())
            });
            setSubjectWrappers(relationships.stream().filter(r -> !ValidationHelper.isNullOrEmpty(r.getSubject()))
                    .map(r -> new SubjectRelationshipWrapper(r, getIngexSubjectWrapper().incrementAndGet()))
                    .collect(Collectors.toList()));
        }
        if (getEntity().getEstimateOMIHistory() != null) {
            setEstimateOMIHistory(getEntity().getEstimateOMIHistory());
            getEstimateOMIHistory().sort(new EstimateOMIComparator());
        }

        if (getEntity().getCommercialValueHistory() != null) {
            setCommercialValueHistory(getEntity().getCommercialValueHistory());
            getCommercialValueHistory().sort(new CommercialValueHistoryComparator());
        }
    }

    public boolean getIsBuilding() {
        return getSelectedTypeId() == null || RealEstateType.BUILDING.getId().equals(getSelectedTypeId());
    }

    private void setSelectedItems() throws PersistenceBeanException, IllegalAccessException {
        setAddressProvinceId(getEntity().getProvince().getId());
        if (!ValidationHelper.isNullOrEmpty(getAddressProvinceId())) {
            handleAddressProvinceChange();
        }
        setAddressCityId(getEntity().getCity().getId());
        setSelectedTypeId(getEntity().getType());
        if (this.getEntity().getCategory() != null) {
            setCategoryId(this.getEntity().getCategory().getId());
        }
    }

    private void loadSelectLists() throws PersistenceBeanException, IllegalAccessException {
        if (getRequestEntity() != null && getRequestEntity().getAggregationLandChargesRegistry() != null) {
            setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description"), new CriteriaAlias[]{
                    new CriteriaAlias("landChargesRegistries", "lcr", JoinType.INNER_JOIN),
                    new CriteriaAlias("lcr.aggregationLandChargesRegistries", "alcr", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("alcr.id", getRequestEntity().getAggregationLandChargesRegistry().getId())
            }, true, false));
        } else {
            setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
        }
        setRealEstateTypes(ComboboxHelper.fillList(RealEstateType.class, false));

        setCadastralCategoryList(DaoManager.load(CadastralCategory.class));
        getCadastralCategoryList().sort(new CadastralCategoryComparator());
        initializeCadastralCategoriesList();

        setSubjectWrappersForDelete(new ArrayList<>());
    }

    private void checkCanRender() {
        setRenderForm(!ValidationHelper.isNullOrEmpty(getAddressProvinceId())
                && !ValidationHelper.isNullOrEmpty(getAddressCityId())
                && !ValidationHelper.isNullOrEmpty(getSelectedTypeId()));
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getSelectedTypeId())) {
            addRequiredFieldException("form:realEstateType");
        }

        if (ValidationHelper.isNullOrEmpty(getAddressProvinceId())) {
            addRequiredFieldException("form:province");
        }

        if (ValidationHelper.isNullOrEmpty(getAddressCityId())) {
            addRequiredFieldException("form:city");
        }

        if (isSaveScheda() && !ValidationHelper.isNullOrEmpty(getNewCadastralData().getScheda())
                && !ValidationHelper.isNullOrEmpty(getNewCadastralData().getDataScheda())) {
            if (getCadastralDataList() == null) {
                setCadastralDataList(new ArrayList<>(1));
            }
            getCadastralDataList().add(getNewCadastralData());
        }

        if (!isSaveScheda() && !ValidationHelper.isNullOrEmpty(getNewCadastralData().getSheet())
                && !ValidationHelper.isNullOrEmpty(getNewCadastralData().getParticle())) {
            if (getCadastralDataList() == null) {
                setCadastralDataList(new ArrayList<>(1));
            }
            getCadastralDataList().add(getNewCadastralData());
        }

        if (ValidationHelper.isNullOrEmpty(getCadastralDataList())) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("validationFailed"),
                    ResourcesHelper.getValidation("cadastralDataRequired"));
        }

        if (isNeedValidateCadastral() && !ValidationHelper.isNullOrEmpty(getCadastralDataList())
                && getCadastralDataList().stream().anyMatch(cd ->
                !ValidationHelper.isNullOrEmpty(cd.getSheet()) && ValidationHelper.isNullOrEmpty(cd.getSub()))) {
            setValidationFailed(true);
            executeJS("PF('cadastralDataValidationDlg').show();");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException,
            IOException, InstantiationException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getEntity().getAres()) && ValidationHelper.isNullOrEmpty(getEntity().getHectares())
                && !ValidationHelper.isNullOrEmpty(getEntity().getCentiares())) {
            getEntity().setAres(0d);
        }
        if (isSaveDuplicate()) {
            pageSaveDuplicate();
        } else {
            pageSaveOrigin();
        }
    }

    private void pageSaveOrigin() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (getEntity().isNew()) {
            if (!ValidationHelper.isNullOrEmpty(this.getCategoryId())) {
                getEntity().setCategory(DaoManager.get(CadastralCategory.class, getCategoryId()));
            } else {
                getEntity().setCategory(null);
            }
            Property propertyDB = RealEstateHelper.getExistingPropertyByCDAndPropertyFields(getCadastralDataList(),
                    getEntity(), DaoManager.getSession());
            if (!ValidationHelper.isNullOrEmpty(propertyDB)) {
                if (RealEstateHelper.propertyChanged(propertyDB, getEntity())) {
                    propertyDB.setModified(true);
                    addRequestToProperty(propertyDB);
                    DaoManager.save(propertyDB);
                    return;
                } else {
                    setEntity(RealEstateHelper.modifyExistingProperty(propertyDB, getEntity(), DaoManager.getSession()));
                }
            }
        }
        if (!ValidationHelper.isNullOrEmpty(getAddressProvinceId())) {
            Province province = DaoManager.get(Province.class, this.getAddressProvinceId());
            getEntity().setProvince(province);
        } else {
            getEntity().setProvince(null);
        }

        if (!ValidationHelper.isNullOrEmpty(getAddressCityId())) {
            City city = DaoManager.get(City.class, getAddressCityId());
            getEntity().setCity(city);
        } else {
            getEntity().setCity(null);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getCategoryId())) {
            getEntity().setCategory(DaoManager.get(CadastralCategory.class, getCategoryId()));
        } else {
            getEntity().setCategory(null);
        }
        getEntity().setType(getSelectedTypeId());

        addRequestToProperty(getEntity());
        RealEstateHelper.saveCadastralData(getEntity(), getCadastralDataList(), getCadastralDataForDeleteList(),
                DaoManager.getSession());
        DaoManager.save(getEntity());
        RealEstateHelper.saveHistory(getEntity(), getEstimateOMIHistory(), getCommercialValueHistory(),
                DaoManager.getSession());
        RealEstateHelper.saveRelationships(getEntity(), getSubjectWrappers(), getSubjectWrappersForDelete(),
                DaoManager.getSession());
    }

    private void addRequestToProperty(Property property) {
        if (!ValidationHelper.isNullOrEmpty(getRequestEntity())) {
            if (ValidationHelper.isNullOrEmpty(property.getRequestList())) {
                property.setRequestList(new ArrayList<>());
            }
            if (!property.getRequestList().contains(getRequestEntity())) {
                property.getRequestList().add(getRequestEntity());
            }
        }
    }

    private void pageSaveDuplicate() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Property property = null;
        if (getEntity().isNew()) {
            Property propertyDB = RealEstateHelper.getExistingPropertyByCD(getCadastralDataList(),
                    DaoManager.getSession());
            if (!ValidationHelper.isNullOrEmpty(propertyDB)) {
                if (RealEstateHelper.propertyChanged(propertyDB, getEntity())) {
                    propertyDB.setModified(true);
                    DaoManager.save(propertyDB);
                    return;
                } else {
                    property = RealEstateHelper.modifyExistingProperty(propertyDB, getEntity(), DaoManager.getSession());
                }
            }
            if (property == null) {
                property = RealEstateHelper.modifyExistingProperty(new Property(), getEntity(), DaoManager.getSession());
            }
        } else {
            property = getEntity();
        }
        if (!ValidationHelper.isNullOrEmpty(getAddressProvinceId())) {
            Province province = DaoManager.get(Province.class, this.getAddressProvinceId());
            property.setProvince(province);
        } else {
            property.setProvince(null);
        }

        if (!ValidationHelper.isNullOrEmpty(getAddressCityId())) {
            City city = DaoManager.get(City.class, getAddressCityId());
            property.setCity(city);
        } else {
            property.setCity(null);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getCategoryId())) {
            property.setCategory(DaoManager.get(CadastralCategory.class, getCategoryId()));
        } else {
            property.setCategory(null);
        }
        property.setType(getSelectedTypeId());

        if (!ValidationHelper.isNullOrEmpty(getRequestEntity())) {
            if (ValidationHelper.isNullOrEmpty(property.getRequestList())) {
                property.setRequestList(new ArrayList<>());
            }
            property.getRequestList().add(getRequestEntity());
        }
        RealEstateHelper.saveCadastralData(property, getCadastralDataList(), getCadastralDataForDeleteList(),
                DaoManager.getSession());
        DaoManager.save(property);
        RealEstateHelper.saveHistory(property, getEstimateOMIHistory(), getCommercialValueHistory(),
                DaoManager.getSession());
        RealEstateHelper.saveRelationships(property, getSubjectWrappers(), getSubjectWrappersForDelete(),
                DaoManager.getSession());
    }

    @Override
    public void goBack() {
        if (isSaveDuplicate()) {
            setSaveDuplicate(false);
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO, "",
                    ResourcesHelper.getString("propertySaved"));
        } else {
            if (ValidationHelper.isNullOrEmpty(getRequestEntity())) {
                RedirectHelper.goTo(PageTypes.DATABASE_LIST);
            } else {
                RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_VIEW, getRequestEntity().getId(), null);
            }
        }
    }

    public void handleAddressProvinceChange() throws PersistenceBeanException, IllegalAccessException {
        if (getRequestEntity() != null && getRequestEntity().getAggregationLandChargesRegistry() != null) {
            setAddressCities(ComboboxHelper.fillList(City.class, Order.asc("description"), 
                    new CriteriaAlias[]{
                    new CriteriaAlias("landChargesRegistries", "lcr", JoinType.INNER_JOIN),
                    new CriteriaAlias("lcr.aggregationLandChargesRegistries", "alcr", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("alcr.id", getRequestEntity().getAggregationLandChargesRegistry().getId()),
                    Restrictions.eq("province.id", getAddressProvinceId()),
                    Restrictions.eq("external", Boolean.TRUE)
            }, true, false));
        } else {
            setAddressCities(ComboboxHelper.fillList(City.class, Order.asc("description"),
                    new Criterion[]{
                            Restrictions.eq("province.id", getAddressProvinceId()),
                            Restrictions.eq("external", Boolean.TRUE)
                    }));
        }
        if (getAddressCities().size() == 2) {
            setAddressCityId((Long) getAddressCities().get(1).getValue());
        } else {
            setAddressCityId(null);
        }
        checkCanRender();
    }

    public void handleAddressCityChange() {
        checkCanRender();
    }

    public void handleTypeChange() {
        initializeCadastralCategoriesList();
        checkCanRender();
    }

    private void initializeCadastralCategoriesList() {
        setCategories(ComboboxHelper.fillList(
                getCadastralCategoryList().stream().filter(c -> !RealEstateType.LAND.getId().equals(getSelectedTypeId())
                        || c.getCode().equals(CADASTRAL_CATEGORY_CODE_FOR_DISPLAY)).collect(Collectors.toList()),
                true, false));
    }

    private void addCadastralData(CadastralData data) {
        if (getCadastralDataList() == null) {
            setCadastralDataList(new ArrayList<>());
        }
        if (data != null && !getCadastralDataList().contains(data)) {
            data.setTempId(getTempId().getAndIncrement());
            getCadastralDataList().add(data);
        }
        setSaveScheda(false);
    }

    public void addCadastralDataDlg() {
        boolean isValid = true;
        cleanValidation();

        addCadastralData(getNewCadastralDataDlg());
        setNewCadastralDataDlg(new CadastralData());
        updateFieldsCadastralData();
    }

    public void updateFieldsCadastralData() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralDataList())) {
            setNewCadastralData(getCadastralDataList().get(0));
        }
    }

    public void addCadastralData() {
        cleanValidation();
        if (!ValidationHelper.isNullOrEmpty(getNewCadastralData().getSheet())
                || !ValidationHelper.isNullOrEmpty(getNewCadastralData().getScheda())
                || !ValidationHelper.isNullOrEmpty(getNewCadastralData().getDataScheda())) {

            if (isSaveScheda()) {
                if (ValidationHelper.isNullOrEmpty(getNewCadastralData().getScheda())) {
                    addRequiredFieldException("schedaText");
                }
                if (ValidationHelper.isNullOrEmpty(getNewCadastralData().getDataScheda())) {
                    addRequiredFieldException("dataSchedaText");
                }
            }

            if (getValidationFailed()) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                        ResourcesHelper.getValidation("validationFailed"),
                        ResourcesHelper.getValidation("cadastralDataRequired"));
                return;
            }
            if (!getCadastralDataList().contains(getNewCadastralData())) {
                addCadastralData(getNewCadastralData());
            }
            setNewCadastralData(new CadastralData());
            updateFieldsCadastralData();

        }

        executeJS("PF('cadastralDialogWV').show();");
    }

    public void deleteCadastralData() {
        if (getCadastralDataList() != null) {
            for (CadastralData data : getCadastralDataList()) {
                if (data.getTempId().equals(getDeleteTempId())) {
                    if (getCadastralDataForDeleteList() == null) {
                        setCadastralDataForDeleteList(new ArrayList<>());
                    }

                    getCadastralDataForDeleteList().add(data);
                    getCadastralDataList().remove(data);

                    break;
                }
            }
            updateFieldsCadastralData();
        }
    }

    public String getCategoryDescription() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        return loadDescriptionById(CadastralCategory.class, getCategoryId());
    }

    public void addRelationship() throws PersistenceBeanException, IllegalAccessException {
        if (getAllSubjects() == null) {
            setAllSubjects(ComboboxHelper.fillList(Subject.class));
        }
        setEditSubjectWrapper(new SubjectRelationshipWrapper(getIngexSubjectWrapper().incrementAndGet()));
        setSelectedSubject(null);
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
                    FileHelper.sendFile(file.getName(), new FileInputStream(file), (int) file.length());
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

    public void openSubjectDlg() {
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", false);
        options.put("draggable", false);
        options.put("modal", true);
        options.put("contentHeight", Integer.valueOf(450));
        options.put("contentWidth", Integer.valueOf(1130));
        SessionHelper.put("fromProperty", Boolean.TRUE);

        RequestContext.getCurrentInstance().openDialog("Subject", options, null);
    }

    public void onEditSubjectDlgClose(SelectEvent event) {
        Subject subject = (Subject) event.getObject();
        if (subject != null && !subject.isNew()) {
            getAllSubjects().add(new SelectItem(subject.getId(), subject.toString()));
        }
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
            Subject finalSubject;
            if (subjectList.size() == 1) {
                finalSubject = subjectList.get(0);
            } else {
                finalSubject = subjectList.stream()
                        .<Map<String, Subject>> collect(HashMap::new,(m,e)->m.put(e.getFiscalCode(), e), Map::putAll).values()
                        .stream()
                        .filter(s -> s.getBusinessName() != null)
                        .filter(s -> s.getBusinessName().equals(
                        getSelectedSubject().replaceAll(subjectPieces[subjectPieces.length - 1], "")))
                        .findFirst().orElse(null);
                if (finalSubject == null) {
                    finalSubject = subjectList.stream().filter(s -> s.getId().equals(getEditSubjectWrapper().getSubject().getId()))
                            .findFirst().orElse(null);
                }
                if (finalSubject == null) {
                    finalSubject = subjectList.stream().filter(s -> s.getSurname().startsWith(subjectPieces[0]))
                            .findAny().orElse(null);
                }
            }
            Subject subject = finalSubject;
            if (getRelationshipEditId() == null && getSubjectWrappers().stream().anyMatch(sw ->
                    RelationshipType.MANUAL_ENTRY.getId().equals(sw.getRelationship().getRelationshipTypeId())
                            && sw.getSubject().getId().equals(subject.getId())
                            && getEditSubjectWrapper().getPropertyType().equals(sw.getPropertyType()))) {
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

    public void showEditHistory() {
        setShowHistoryTables(!isShowHistoryTables());
    }

    public void addNewCommercialValue() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        cleanValidation();
        if (ValidationHelper.isNullOrEmpty(getCommercialValue())) {
            markInvalid("commercialValue", "requiredField");
            return;
        }

        CommercialValueHistory history = new CommercialValueHistory();
        if(getCommercialValue().trim().endsWith(".00")) {
            StringBuffer sb = new StringBuffer(getCommercialValue());
            sb.replace(getCommercialValue().lastIndexOf("."), getCommercialValue().lastIndexOf(".") + 1, ",");
            history.setCommercialValue(sb.toString());
        } else 
         history.setCommercialValue(getCommercialValue());
        
        if (ValidationHelper.isNullOrEmpty(getCommercialDate())) {
            history.setCommercialValueDate(new Date());
        } else {
            history.setCommercialValueDate(getCommercialDate());
        }
        history.setUser(DaoManager.get(User.class, getCurrentUser().getId()));

        if (getCommercialValueHistory() == null) {
            setCommercialValueHistory(new ArrayList<>());
        }
        getCommercialValueHistory().add(history);
        getCommercialValueHistory().sort(new CommercialValueHistoryComparator());

        executeJS("PF('addNewCommercialValueDlgWV').hide();");

        setCommercialValue(null);
        setCommercialDate(null);
    }

    public void addNewEstimateOMI() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        cleanValidation();
        if (ValidationHelper.isNullOrEmpty(getCurrencyValue())) {
            markInvalid("estimateOMIValue", "requiredField");
            return;
        }

        EstimateOMIHistory history = new EstimateOMIHistory();

        history.setEstimateOMI(getCurrencyValue());
        
        if(getCurrencyValue().trim().endsWith(".00")) {
            StringBuffer sb = new StringBuffer(getCurrencyValue());
            sb.replace(getCurrencyValue().lastIndexOf("."), getCurrencyValue().lastIndexOf(".") + 1, ",");
            history.setEstimateOMI(sb.toString());
        } else 
         history.setEstimateOMI(getCurrencyValue());
        
        if (ValidationHelper.isNullOrEmpty(getCurrencyDate())) {
            history.setPropertyAssessmentDate(new Date());
        } else {
            history.setPropertyAssessmentDate(getCurrencyDate());
        }
        history.setUser(DaoManager.get(User.class, getCurrentUser().getId()));

        if (getEstimateOMIHistory() == null) {
            setEstimateOMIHistory(new ArrayList<>());
        }
        getEstimateOMIHistory().add(history);
        getEstimateOMIHistory().sort(new EstimateOMIComparator());

        executeJS("PF('addNewOMIDlgWV').hide();");

        setCurrencyValue(null);
        setCurrencyDate(null);
    }

    public Request getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(Request requestEntity) {
        this.requestEntity = requestEntity;
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

    public Long getSelectedTypeId() {
        return selectedTypeId;
    }

    public void setSelectedTypeId(Long selectedTypeId) {
        this.selectedTypeId = selectedTypeId;
    }

    public boolean isRenderForm() {
        return renderForm;
    }

    public void setRenderForm(boolean renderForm) {
        this.renderForm = renderForm;
    }

    public boolean isSaveScheda() {
        return saveScheda;
    }

    public void setSaveScheda(boolean saveScheda) {
        this.saveScheda = saveScheda;
    }

    public CadastralData getNewCadastralData() {
        return newCadastralData;
    }

    public void setNewCadastralData(CadastralData newCadastralData) {
        this.newCadastralData = newCadastralData;
    }

    public CadastralData getNewCadastralDataDlg() {
        return newCadastralDataDlg;
    }

    public void setNewCadastralDataDlg(CadastralData newCadastralDataDlg) {
        this.newCadastralDataDlg = newCadastralDataDlg;
    }

    public List<CadastralData> getCadastralDataList() {
        return cadastralDataList;
    }

    public void setCadastralDataList(List<CadastralData> cadastralDataList) {
        this.cadastralDataList = cadastralDataList;
    }

    public List<CadastralData> getCadastralDataForDeleteList() {
        return cadastralDataForDeleteList;
    }

    public void setCadastralDataForDeleteList(List<CadastralData> cadastralDataForDeleteList) {
        this.cadastralDataForDeleteList = cadastralDataForDeleteList;
    }

    public AtomicInteger getTempId() {
        return tempId;
    }

    public void setTempId(AtomicInteger tempId) {
        this.tempId = tempId;
    }

    public Integer getDeleteTempId() {
        return deleteTempId;
    }

    public void setDeleteTempId(Integer deleteTempId) {
        this.deleteTempId = deleteTempId;
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

    public List<SubjectRelationshipWrapper> getSubjectWrappers() {
        return subjectWrappers;
    }

    public void setSubjectWrappers(List<SubjectRelationshipWrapper> subjectWrappers) {
        this.subjectWrappers = subjectWrappers;
    }

    public SubjectRelationshipWrapper getEditSubjectWrapper() {
        return editSubjectWrapper;
    }

    public void setEditSubjectWrapper(SubjectRelationshipWrapper editSubjectWrapper) {
        this.editSubjectWrapper = editSubjectWrapper;
    }

    public Long getRelationshipEditId() {
        return relationshipEditId;
    }

    public void setRelationshipEditId(Long relationshipEditId) {
        this.relationshipEditId = relationshipEditId;
    }

    public Long getRelationshipDeleteId() {
        return relationshipDeleteId;
    }

    public void setRelationshipDeleteId(Long relationshipDeleteId) {
        this.relationshipDeleteId = relationshipDeleteId;
    }

    public Long getDownloadFileId() {
        return downloadFileId;
    }

    public void setDownloadFileId(Long downloadFileId) {
        this.downloadFileId = downloadFileId;
    }

    public String getSelectedSubject() {
        return selectedSubject;
    }

    public void setSelectedSubject(String selectedSubject) {
        this.selectedSubject = selectedSubject;
    }

    public List<SelectItem> getAllSubjects() {
        return allSubjects;
    }

    public void setAllSubjects(List<SelectItem> allSubjects) {
        this.allSubjects = allSubjects;
    }

    public List<SubjectRelationshipWrapper> getSubjectWrappersForDelete() {
        return subjectWrappersForDelete;
    }

    public void setSubjectWrappersForDelete(List<SubjectRelationshipWrapper> subjectWrappersForDelete) {
        this.subjectWrappersForDelete = subjectWrappersForDelete;
    }

    public AtomicLong getIngexSubjectWrapper() {
        return ingexSubjectWrapper;
    }

    public void setIngexSubjectWrapper(AtomicLong ingexSubjectWrapper) {
        this.ingexSubjectWrapper = ingexSubjectWrapper;
    }

    public boolean isNeedValidateCadastral() {
        return needValidateCadastral;
    }

    public void setNeedValidateCadastral(boolean needValidateCadastral) {
        this.needValidateCadastral = needValidateCadastral;
    }

    public boolean isSaveDuplicate() {
        return saveDuplicate;
    }

    public void setSaveDuplicate(boolean saveDuplicate) {
        this.saveDuplicate = saveDuplicate;
    }

    public boolean isShowHistoryTables() {
        return showHistoryTables;
    }

    public void setShowHistoryTables(boolean showHistoryTables) {
        this.showHistoryTables = showHistoryTables;
    }

    public List<EstimateOMIHistory> getEstimateOMIHistory() {
        return estimateOMIHistory;
    }

    public void setEstimateOMIHistory(List<EstimateOMIHistory> estimateOMIHistory) {
        this.estimateOMIHistory = estimateOMIHistory;
    }

    public List<CommercialValueHistory> getCommercialValueHistory() {
        return commercialValueHistory;
    }

    public void setCommercialValueHistory(List<CommercialValueHistory> commercialValueHistory) {
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

    public List<CadastralCategory> getCadastralCategoryList() {
        return cadastralCategoryList;
    }

    public void setCadastralCategoryList(List<CadastralCategory> cadastralCategoryList) {
        this.cadastralCategoryList = cadastralCategoryList;
    }
}
