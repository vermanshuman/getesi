package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.LandChargesRegistryType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.AggregationLandChargesRegistryNote;
import it.nexera.ris.persistence.beans.entities.domain.AggregationLandChargesRegistryReference;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean(name = "conservatoryListBean")
@ViewScoped
@Getter
@Setter
public class ConservatoryListBean extends EntityLazyListPageBean<AggregationLandChargesRegistry> implements Serializable {

    private static final long serialVersionUID = 1012464146929819767L;

    private AggregationLandChargesRegistry entity;

    private String nameFilter;

    private String codeOfficeFilter;

    private Long conservatoryId;

    private Set<AggregationLandChargesRegistry> aggregationLandChargesRegistrySet;

    private String filterCode;

    private String selectedFilterType;

    private String filterAddress;

    private String filterEmail;

    private List<SelectItem> filterTypes;

    private Boolean referencePresent;

    private String otherFileNameTemp;

    private byte[] otherDocumentContents;

    private List<Document> otherDocuments;

    private List<Document> deletedOtherDocuments;

    private String otherDocumentName;

    private Document otherDocument;

    private String otherDocumentNote;

    private Long otherDocumentId;

    private Boolean showDocumentSection;

    private String fromPage;

    @Override
    protected void preLoad() {
        String conservatory = getRequestParameter(RedirectHelper.CONSERVATORY_ID);
        if (StringUtils.isNotBlank(conservatory)) {
            try {
                setConservatoryId(Long.parseLong(conservatory));
            } catch (Exception e) {
                e.printStackTrace();
                LogHelper.log(log, e);
            }
        } else
            setConservatoryId(null);

        if (StringUtils.isNotBlank(getRequestParameter(RedirectHelper.FROM_PARAMETER))) {
            setFromPage(getRequestParameter(RedirectHelper.FROM_PARAMETER));
        }
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        setFilterTypes(ComboboxHelper.fillList(LandChargesRegistryType.class, Boolean.TRUE));
        setEntity(new AggregationLandChargesRegistry());
        loadList(AggregationLandChargesRegistry.class,
                new Criterion[]{
                        Restrictions.or(
                                Restrictions.eq("isDeleted", Boolean.FALSE),
                                Restrictions.isNull("isDeleted"))}, new Order[]{
                        Order.asc("name")
                }, new CriteriaAlias[]{
                        new CriteriaAlias("landChargesRegistries", "l", JoinType.LEFT_OUTER_JOIN)});

        if (getConservatoryId() != null && getConservatoryId() > 0) {
            setEntityEditId(getConservatoryId());
            this.editEntity();
        }
        filterTableFromPanel();
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        setEntity(new AggregationLandChargesRegistry());
        this.cleanValidation();
        RequestContext.getCurrentInstance().update("addConservatoryDialog");
        executeJS("PF('addConservatoryDialogWV').show();");
        setShowDocumentSection(Boolean.FALSE);
    }

    @Override
    public void editEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        this.cleanValidation();
        if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
            try {
                setShowDocumentSection(Boolean.TRUE);
                setOtherDocuments(new ArrayList<>());
                setDeletedOtherDocuments(new ArrayList<>());
                this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                if (ValidationHelper.isNullOrEmpty(getEntity().getAggregationLandChargesRegistryReferences())) {
                    AggregationLandChargesRegistryReference aggregationLandChargesRegistryReference = new AggregationLandChargesRegistryReference();
                    aggregationLandChargesRegistryReference.setAggregationLandChargesRegistry(getEntity());
                    getEntity().setAggregationLandChargesRegistryReferences(new ArrayList<>());
                    getEntity().getAggregationLandChargesRegistryReferences().add(aggregationLandChargesRegistryReference);
                }
                if (ValidationHelper.isNullOrEmpty(getEntity().getAggregationLandChargesRegistryNotes())) {
                    AggregationLandChargesRegistryNote aggregationLandChargesRegistryNote = new AggregationLandChargesRegistryNote();
                    aggregationLandChargesRegistryNote.setAggregationLandChargesRegistry(getEntity());
                    getEntity().setAggregationLandChargesRegistryNotes(new ArrayList<>());
                    getEntity().getAggregationLandChargesRegistryNotes().add(aggregationLandChargesRegistryNote);
                }
                DaoManager.getSession().evict(this.getEntity());
                loadOtherDocumentList();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        RequestContext.getCurrentInstance().update("addConservatoryDialog");
        executeJS("PF('addConservatoryDialogWV').show();");
    }

    @Override
    public void deleteEntity() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        this.setEntity(DaoManager.get(getType(), this.getEntityDeleteId()));
        getEntity().setIsDeleted(Boolean.TRUE);
        saveEntity();
    }

    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        this.cleanValidation();
        this.setValidationFailed(false);

        try {
            this.validate();
        } catch (PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
        if (this.getValidationFailed()) {
            return;
        }
        saveEntity();
        getOtherDocuments()
                .stream()
                .forEach(d -> {
                    try {
                        DaoManager.save(d, true);
                    } catch (PersistenceBeanException e) {
                        LogHelper.log(log, e);
                        e.printStackTrace();
                    }
                });
        getDeletedOtherDocuments()
                .stream()

                .forEach(d -> {
                    if(!d.isNew()){
                        try {
                            DaoManager.remove(d, true);
                        } catch (PersistenceBeanException e) {
                            LogHelper.log(log, e);
                            e.printStackTrace();
                        }
                    }

                });
        this.resetFields();
        executeJS("PF('addConservatoryDialogWV').hide()");
        executeJS("refreshTable()");
    }

    private void saveEntity() {
        Transaction tr = null;
        try {
            tr = PersistenceSessionManager.getBean().getSession().beginTransaction();
            DaoManager.save(this.getEntity());
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
            LogHelper.log(log, e);
        } finally {
            if (tr != null && !tr.wasRolledBack() && tr.isActive()) {
                tr.commit();
            }
        }
    }

    protected void validate() throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        } else if (!ValidationHelper.isUnique(AggregationLandChargesRegistry.class, "name", getEntity().getName(),
                this.getEntity().getId())) {
            addFieldException("form:name", "nameAlreadyInUse");
        }

    }

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new AggregationLandChargesRegistry());
        this.cleanValidation();
        //this.filterTableFromPanel();
    }

    public void clearFiltraPanel() throws PersistenceBeanException, IllegalAccessException {
        setFilterCode(null);
        setSelectedFilterType(null);
        setFilterAddress(null);
        setFilterEmail(null);
        setReferencePresent(null);
        filterTableFromPanel();
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException {
        searchConservatory();
    }

    public void searchConservatory() throws IllegalAccessException, PersistenceBeanException {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getNameFilter())) {
            restrictions.add(Restrictions.like("name", getNameFilter(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(getCodeOfficeFilter())) {
            restrictions.add(Restrictions.like("codeOffice", getNameFilter(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getFilterCode())) {
            restrictions.add(Restrictions.like("codeOffice", getFilterCode(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedFilterType())) {
            restrictions.add(Restrictions.eq("type", getSelectedFilterType().equals("CONSERVATORY") ? "C" : "T"));
        }

        if (!ValidationHelper.isNullOrEmpty(getFilterAddress())) {
            restrictions.add(Restrictions.like("address", getFilterAddress(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(getFilterEmail())) {
            restrictions.add(Restrictions.like("mail", getFilterEmail(), MatchMode.ANYWHERE));
        }
        if(getReferencePresent() != null && getReferencePresent()){
            restrictions.add(Restrictions.isNotEmpty("aggregationLandChargesRegistryReferences"));
        }
        restrictions.add(Restrictions.or(
                Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));

        List<AggregationLandChargesRegistry> aggregationLandChargesRegistries = DaoManager.load(
                AggregationLandChargesRegistry.class, restrictions.toArray(new Criterion[0]), new Order[]{
                        Order.asc("name")});
        aggregationLandChargesRegistrySet = new HashSet<>();
        aggregationLandChargesRegistrySet = aggregationLandChargesRegistries.stream()
                .sorted(Comparator.comparing(AggregationLandChargesRegistry::getName)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void addNewReferences() {
        if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
            AggregationLandChargesRegistryReference aggregationLandChargesRegistryReference = new AggregationLandChargesRegistryReference();
            aggregationLandChargesRegistryReference.setAggregationLandChargesRegistry(getEntity());
            getEntity().getAggregationLandChargesRegistryReferences().add(aggregationLandChargesRegistryReference);
        }
    }

    public void deleteReferences(AggregationLandChargesRegistryReference aggregationLandChargesRegistryReference) {
        getEntity().getAggregationLandChargesRegistryReferences().remove(aggregationLandChargesRegistryReference);
    }

    public void addNewNote() {
        if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
            AggregationLandChargesRegistryNote aggregationLandChargesRegistryNote = new AggregationLandChargesRegistryNote();
            aggregationLandChargesRegistryNote.setAggregationLandChargesRegistry(getEntity());
            getEntity().getAggregationLandChargesRegistryNotes().add(aggregationLandChargesRegistryNote);
        }
    }

    public void deleteNote(AggregationLandChargesRegistryNote aggregationLandChargesRegistryNote) {
        getEntity().getAggregationLandChargesRegistryNotes().remove(aggregationLandChargesRegistryNote);
    }

    public void deleteOtherTempDocument() {
        setOtherDocumentContents(null);
        setOtherFileNameTemp(null);
    }
    public void handleOtherDocumentUpload(FileUploadEvent event) {
        setOtherDocumentContents(event.getFile().getContents());
        setOtherFileNameTemp(event.getFile().getFileName());
    }

    public void saveOtherDocument() throws HibernateException, PersistenceBeanException, IOException, IllegalAccessException {
        if(ValidationHelper.isNullOrEmpty(getOtherDocumentContents()))
            return;
        StringBuilder sb = new StringBuilder();

        sb.append(FileHelper.getDocumentSavePath());
        sb.append(DateTimeHelper.ToFilePathString(new Date()));
        sb.append(getCurrentUser().getId());
        sb.append("\\");

        File filePath = new File(sb.toString());
        String fileName = "";
        if(StringUtils.isNotBlank(getOtherDocumentName()))
            fileName = getOtherDocumentName() + ".pdf";
        else
            fileName = getOtherFileNameTemp();
        FileHelper.writeFileToFolder(fileName, filePath, getOtherDocumentContents());
        Document document = new Document();
        if(StringUtils.isNotBlank(getOtherDocumentName()))
            document.setTitle(getOtherDocumentName());
        else
            document.setTitle(FileHelper.getFileNameWOExtension(getOtherFileNameTemp()));
        document.setPath(sb + fileName);
        document.setTypeId(DocumentType.ALLEGATI.getId());
        document.setNote(getOtherDocumentNote());
        document.setDate(new Date());
        document.setAggregationLandChargesRegistry(getEntity());
        getOtherDocuments().add(document);
        setOtherDocumentName(null);
        setOtherDocumentNote(null);
        setOtherFileNameTemp(null);
        setOtherDocumentContents(null);
    }

    private void loadOtherDocumentList() throws PersistenceBeanException, IllegalAccessException {
        List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("aggregationLandChargesRegistry", getEntity()),
                Restrictions.eq("typeId", DocumentType.ALLEGATI.getId())});
        if(!ValidationHelper.isNullOrEmpty(documents)) {
            getOtherDocuments().addAll(documents);
        }
    }

    public void deleteOtherDocument(int index) throws HibernateException, NumberFormatException {
        if(getOtherDocuments() != null && index > -1 && index <= getOtherDocuments().size()){
            getDeletedOtherDocuments().add(getOtherDocuments().get(index));
            getOtherDocuments().remove(index);
        }
    }

    public void handleCloseDialog() {
        if(StringUtils.isNotBlank(getFromPage())){
            executeJS("closeWindow();");
        }
    }
}
