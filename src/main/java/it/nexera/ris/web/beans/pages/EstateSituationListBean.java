package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import it.nexera.ris.persistence.beans.entities.domain.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.FileUploadEvent;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.common.enums.MailEditType;
import it.nexera.ris.common.enums.MortgageType;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.RequestOutputTypes;
import it.nexera.ris.common.enums.RoleTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.CostManipulationHelper;
import it.nexera.ris.common.helpers.EstateSituationHelper;
import it.nexera.ris.common.helpers.GeneralFunctionsHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.SaveRequestDocumentsHelper;
import it.nexera.ris.common.helpers.SendNotificationHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.view.FormalityView;
import it.nexera.ris.web.beans.EntityListPageBean;
import it.nexera.ris.web.beans.wrappers.logic.UploadDocumentWrapper;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@ManagedBean(name = "estateSituationListBean")
@ViewScoped
public class EstateSituationListBean extends EntityListPageBean<EstateSituation> implements Serializable {

    private static final long serialVersionUID = -8044652160413440896L;

    private Long requestId;

    private List<Document> documentList;

    private Request examRequest;

    private List<Document> requestDocuments;

    private CostManipulationHelper costManipulationHelper;
    
    private String costNote;
    
    private Boolean hideExtraCost = Boolean.FALSE;
    
    private List<Document> otherDocuments;

    private List<EstateSituation> salesEstateSituationList;

    private Long editSalesId;

    private Long deletedSalesId;

    private Boolean showRequestCost = Boolean.TRUE;

    @Getter
    @Setter
    private List<Document> requestNonSaleDocuments;

    @Getter
    @Setter
    private List<Document> requestSaleDocuments;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        String parameter = getRequestParameter(RedirectHelper.ID_PARAMETER);
        if (!ValidationHelper.isNullOrEmpty(parameter)) {
            setRequestId(Long.parseLong(parameter));
            setExamRequest(DaoManager.get(Request.class, getRequestId()));
            updateTable();
        }
        loadDocumentList();
        setCostManipulationHelper(new CostManipulationHelper());
        getCostManipulationHelper().setMortgageTypeList(ComboboxHelper.fillList(MortgageType.class, false, false));
        updateSalesTable();
    }

    private void loadDocumentList() throws PersistenceBeanException, IllegalAccessException {
        setDocumentList(DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request.id", getRequestId()),
                Restrictions.eq("typeId", DocumentType.OTHER.getId())
        }));
    }

    private void updateTable() throws PersistenceBeanException, IllegalAccessException {
        setList(DaoManager.load(EstateSituation.class, new Criterion[]{
                Restrictions.eq("request.id", getRequestId()),
                Restrictions.or(Restrictions.isNull("salesDevelopment"),
                        Restrictions.eq("salesDevelopment", Boolean.FALSE))
        }));
    }

    private void updateSalesTable() throws PersistenceBeanException, IllegalAccessException {
        setSalesEstateSituationList(DaoManager.load(EstateSituation.class, new Criterion[]{
                Restrictions.eq("request.id", getRequestId()),
                Restrictions.and(Restrictions.isNotNull("salesDevelopment"),
                        Restrictions.eq("salesDevelopment", Boolean.TRUE))
        }));
    }


    @Override
    public void addEntity() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_EDIT, getRequestId(), null);
    }

    @Override
    public void editEntity() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_EDIT, getRequestId(), getEntityEditId());
    }


    @Override
    public boolean getCanCreate() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return true;
    }

    @Override
    public boolean getCanEdit() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return true;
    }

    @Override
    public boolean getCanDelete() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return true;
    }

    public void goBack() {
        RedirectHelper.goTo(PageTypes.REQUEST_EDIT, getRequestId());
    }

    public void goTextEdit() {
        RedirectHelper.goTo(PageTypes.REQUEST_TEXT_EDIT, getRequestId());
    }

    @Override
    public void deleteEntity() throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        DaoManager.removeWithRefreshRequest(EstateSituation.class, getEntityDeleteId(), true);
        updateTable();
    }

    public void deleteSalesEntity() throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, NumberFormatException {
        DaoManager.removeWithRefreshRequest(EstateSituation.class, getDeletedSalesId(), true);
        updateSalesTable();
    }


    public void deleteEntityDocument() throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        DaoManager.remove(Document.class, getEntityDeleteId(), true);
        updateTable();
        onLoad();
    }

    public void printSpecialPdf() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getExamRequest())) {
            fillRequestDocumentList();
            log.info("before fillOtherDocumentList");
            fillOtherDocumentList();
            log.info("after fillOtherDocumentList");
        }
    }

    private void fillRequestDocumentList() throws PersistenceBeanException, IllegalAccessException {
        RequestOutputTypes type;
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getService())) {
            type = getExamRequest().getService().getRequestOutputType();
        } else {
            type = RequestOutputTypes.ALL;
        }

        List<Document> documentListToView = EstateSituationHelper.getDocuments(type, getExamRequest(), null);

        for (Document document : documentListToView) {
            if (Objects.equals(document.getTypeId(), DocumentType.OTHER.getId())
                    || Objects.equals(document.getTypeId(), DocumentType.REQUEST_REPORT.getId())) {
                document.setSelectedForEmail(true);
            } else {
                document.setSelectedForEmail(false);
            }
        }

        setRequestDocuments(documentListToView);
    }

    public void onConfirm() throws Exception {
        saveRequestDocumentsAndNotify();
    }
    
    public void saveRequestDocumentsAndNotify() throws Exception {
        SaveRequestDocumentsHelper.saveRequestDocuments(getExamRequest(), getRequestDocuments(),true);
        if(!ValidationHelper.isNullOrEmpty(getOtherDocuments())){
            SaveRequestDocumentsHelper.saveRequestDocuments(getExamRequest(), getOtherDocuments(), true);
        }

        SendNotificationHelper.checkAndSendNotification(getExamRequest());

        RedirectHelper.goTo(PageTypes.REQUEST_LIST);
    }

    public void openMailManagerEditor() throws PersistenceBeanException {
        RedirectHelper.goToMailEditRequest(getRequestId(), getExamRequest().getMail() != null
                ? MailEditType.REQUEST_REPLY_ALL : MailEditType.REQUEST);
    }

    public void addEstate() {
        RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_VIEW, getRequestId(), null);
    }

    public void saveDocumentXML(FileUploadEvent event)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        UploadDocumentWrapper uploadDocument = GeneralFunctionsHelper.handleFileUpload(event.getFile().getFileName(),
                event.getFile().getContents(), DocumentType.OTHER.getId(), event.getFile().getFileName(), new Date(),
                null, DaoManager.get(Request.class, getRequestId()), DaoManager.getSession());
        loadDocumentList();
    }

    public String getExternalRequestFrom() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        User createRequestUser = DaoManager.get(User.class, getExamRequest().getCreateUserId());
        String beginning = "RICHIESTA ESTERNA DA ";
        String body = createRequestUser.getFullname() + " (" + (createRequestUser.getClient() == null
                ? "" : createRequestUser.getClient().toString()) + ")";
        return beginning + body;
    }

    public boolean getCreatedByExternalUser() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getExamRequest())
                && !ValidationHelper.isNullOrEmpty(getExamRequest().getCreateUserId())) {

            User user = DaoManager.get(User.class, getExamRequest().getCreateUserId());
            for (Role role : user.getRoles()) {
                if (RoleTypes.EXTERNAL.equals(role.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void saveRequestEstateFormalityCost() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        getCostManipulationHelper().saveRequestEstateFormalityCost(getExamRequest());
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getNumberActUpdate())) {
            boolean reCalculate = true;
            if(getExamRequest().getCostButtonConfirmClicked() != null && getExamRequest().getCostButtonConfirmClicked()){
                reCalculate = false;
            }
            getCostManipulationHelper().viewExtraCost(getExamRequest(), reCalculate);
        }
    }

    public boolean showAuthorizedQuote() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getExamRequest()) && (ValidationHelper.isNullOrEmpty(getExamRequest().getCostButtonConfirmClicked())
                || !getExamRequest().getCostButtonConfirmClicked())
        		 && !ValidationHelper.isNullOrEmpty(getExamRequest().getClient())
                && !ValidationHelper.isNull(getExamRequest().getClient().getMaxNumberAct()) &&
                !ValidationHelper.isNullOrEmpty(getExamRequest().getNumberActOrSumOfEstateFormalitiesAndOther()) &&
                getExamRequest().getNumberActOrSumOfEstateFormalitiesAndOther() > getExamRequest().getClient().getMaxNumberAct()) {
           return true;
        }else {
        	return false;
        }
    }
    
    public void viewExtraCost() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        viewExtraCost(false);
    }

    public void viewExtraCost(boolean recalculate) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        getCostManipulationHelper().viewExtraCost(getExamRequest(), recalculate);
    }

    public void addExtraCost(String extraCostValue) {
        getCostManipulationHelper().addExtraCost(extraCostValue, getRequestId());
    }

    public void deleteExtraCost(ExtraCost extraCostToDelete) {
        getCostManipulationHelper().getRequestExtraCosts().remove(extraCostToDelete);
        getExamRequest().setIncludeNationalCost(null);
    }

    public void saveRequestExtraCost() throws Exception {
        getCostManipulationHelper().saveRequestExtraCost(getExamRequest());
    }

    public void updateNationalCost() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        
        if(!ValidationHelper.isNullOrEmpty(getCostManipulationHelper().getIncludeNationalCost())
                && getCostManipulationHelper().getIncludeNationalCost()) {
            Request request = DaoManager.get(Request.class, new Criterion[]{
                    Restrictions.eq("id", getRequestId())});
            
            if(!ValidationHelper.isNullOrEmpty(request.getMail())) {
                List<Request> requestsWithSameMailId = DaoManager.load(Request.class, new Criterion[] {Restrictions.eq("mail.id", request.getMail().getId())});
                boolean haveAnyWithIncludeSet = requestsWithSameMailId.stream().anyMatch(
                        x->!ValidationHelper.isNullOrEmpty(x.getIncludeNationalCost()) && x.getIncludeNationalCost());
                if(haveAnyWithIncludeSet) {
                    getCostManipulationHelper().setIncludeNationalCost(false);
                    executeJS("PF('includeNationalCostDialogWV').show();");
                    return;
                }
            }
            
            if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getService())
                    && !ValidationHelper.isNullOrEmpty(request.getService().getNationalPrice())) {
                getCostManipulationHelper().setExtraCostOther(request.getService().getNationalPrice().toString());
                getCostManipulationHelper().setExtraCostOtherNote(ResourcesHelper.getString("requestServiceNationalPriceNote"));    
                getCostManipulationHelper().addExtraCost("NAZIONALEPOSITIVA", getRequestId());    
            }
        }else {
            if(!ValidationHelper.isNullOrEmpty(getCostManipulationHelper().getRequestExtraCosts())) {
                Optional<ExtraCost> nationalExtraCost =  getCostManipulationHelper().getRequestExtraCosts()
                        .stream()
                        .filter(ec -> ec.getType().equals(ExtraCostType.NAZIONALEPOSITIVA))
                        .findFirst();
                if(nationalExtraCost.isPresent()) {
                    deleteExtraCost(nationalExtraCost.get());    
                }
            }
        }
    }
    
    private void fillOtherDocumentList()  {
        List<FormalityView> formalityPDFList = null;
        setOtherDocuments(new ArrayList<>());
        try {
            if (!ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality())) {
                formalityPDFList = EstateSituationHelper.loadFormalityViewByDistraint(getExamRequest());
            } else {
                formalityPDFList = EstateSituationHelper.loadFormalityView(getExamRequest());
            }
            if(!ValidationHelper.isNullOrEmpty(formalityPDFList)) {
                for(FormalityView formality: formalityPDFList) {
                    if(!ValidationHelper.isNullOrEmpty(formality.getDocumentId())) {
                        Document otherDoc = DaoManager.get(Document.class, formality.getDocumentId());
                        if(!getRequestDocuments().contains(otherDoc)){
                            if (Objects.equals(otherDoc.getTypeId(), DocumentType.OTHER.getId())
                                    || Objects.equals(otherDoc.getTypeId(), DocumentType.REQUEST_REPORT.getId())) {
                                otherDoc.setSelectedForDialogList(true);
                            } else {
                                otherDoc.setSelectedForDialogList(false);
                            }
                            getOtherDocuments().add(otherDoc);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    
    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public List<Document> getDocumentList() {
        return documentList;
    }

    public void setDocumentList(List<Document> documentList) {
        this.documentList = documentList;
    }

    public Request getExamRequest() {
        return examRequest;
    }

    public void setExamRequest(Request examRequest) {
        this.examRequest = examRequest;
    }

    public List<Document> getRequestDocuments() {
        return requestDocuments;
    }

    public void setRequestDocuments(List<Document> requestDocuments) {
        this.requestDocuments = requestDocuments;
    }

    public CostManipulationHelper getCostManipulationHelper() {
        return costManipulationHelper;
    }

    public void setCostManipulationHelper(CostManipulationHelper costManipulationHelper) {
        this.costManipulationHelper = costManipulationHelper;
    }

    public Boolean getHideExtraCost() {
        return hideExtraCost;
    }

    public void setHideExtraCost(Boolean hideExtraCost) {
        this.hideExtraCost = hideExtraCost;
    }

    public String getCostNote() {
        return costNote;
    }

    public void setCostNote(String costNote) {
        this.costNote = costNote;
    }
    
    public List<Document> getOtherDocuments() {
        return otherDocuments;
    }

    public void setOtherDocuments(List<Document> otherDocuments) {
        this.otherDocuments = otherDocuments;
    }

    public void addSalesEntity() throws HibernateException {
        RedirectHelper.goToSalesDevelopment(PageTypes.REQUEST_ESTATE_SITUATION_EDIT, getRequestId(), null);
    }

    public List<EstateSituation> getSalesEstateSituationList() {
        return salesEstateSituationList;
    }

    public void setSalesEstateSituationList(List<EstateSituation> salesEstateSituationList) {
        this.salesEstateSituationList = salesEstateSituationList;
    }

    public boolean isSalesDevelopmentShowing() {
        if(!ValidationHelper.isNullOrEmpty(getExamRequest()) && !ValidationHelper.isNullOrEmpty(getExamRequest().getClient()) &&
                !ValidationHelper.isNullOrEmpty(getExamRequest().getClient().getSalesDevelopment()) &&
            getExamRequest().getClient().getSalesDevelopment()){
            return true;
        }

        if(!ValidationHelper.isNullOrEmpty(getExamRequest()) && !ValidationHelper.isNullOrEmpty(getExamRequest().getService()) &&
                !ValidationHelper.isNullOrEmpty(getExamRequest().getService().getSalesDevelopment()) &&
                getExamRequest().getService().getSalesDevelopment()){
            return true;
        }
        return false;
    }

    public void autoAddEntity() throws PersistenceBeanException, IllegalAccessException {
        List<FormalityView> formalityPDFList = EstateSituationHelper.loadFormalityViewForSales(getExamRequest());
        List<Formality> formalityList;
        if (!ValidationHelper.isNullOrEmpty(formalityPDFList)) {
            formalityList = DaoManager.load(Formality.class, new Criterion[]{
                    Restrictions.in("id", formalityPDFList.stream()
                            .map(FormalityView::getId)
                            .collect(Collectors.toList()))
            });
            if(ValidationHelper.isNullOrEmpty(formalityList))
                return;
            Calendar cal = Calendar.getInstance();
            Date today = cal.getTime();
            cal.add(Calendar.YEAR, -5);
            Date endYear = cal.getTime();
            emptyIfNull(formalityList).removeIf(formality ->
                    (formality.getPresentationDate() != null && (formality.getPresentationDate().after(today) ||
                            formality.getPresentationDate().before(endYear))));
            if(!ValidationHelper.isNullOrEmpty(formalityList)){
                EstateSituation estateSituation = new EstateSituation();
                estateSituation.setRequest(getExamRequest());
                estateSituation.setSalesDevelopment(Boolean.TRUE);
                estateSituation.setFormalityList(formalityList);
                estateSituation.setOtherType(Boolean.FALSE);
                estateSituation.setReportRelationship(Boolean.TRUE);
                DaoManager.save(estateSituation, true);
                updateSalesTable();
            }
        }
    }

    public void editSalesEntity() throws HibernateException {
        //RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_EDIT, getRequestId(), getEntityEditId());
        RedirectHelper.goToSalesDevelopment(PageTypes.REQUEST_ESTATE_SITUATION_EDIT, getRequestId(), getEditSalesId());
    }

    public Long getEditSalesId() {
        return editSalesId;
    }

    public void setEditSalesId(Long editSalesId) {
        this.editSalesId = editSalesId;
    }

    public Long getDeletedSalesId() {
        return deletedSalesId;
    }

    public void setDeletedSalesId(Long deletedSalesId) {
        this.deletedSalesId = deletedSalesId;
    }

    public Boolean getShowRequestCost() {
        return showRequestCost;
    }

    public void setShowRequestCost(Boolean showRequestCost) {
        this.showRequestCost = showRequestCost;
    }
}
