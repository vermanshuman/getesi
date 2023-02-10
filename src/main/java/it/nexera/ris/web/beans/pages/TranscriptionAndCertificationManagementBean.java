package it.nexera.ris.web.beans.pages;

import it.nexera.ris.api.FatturaAPI;
import it.nexera.ris.api.FatturaAPIResponse;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.create.xls.CreateExcelRequestsReportHelper;
import it.nexera.ris.common.helpers.create.xls.ExcelTemplateHelper;
import it.nexera.ris.common.xml.wrappers.ConservatoriaSelectItem;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.BaseEntityPageBean;
import it.nexera.ris.web.beans.wrappers.GoodsServicesFieldWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ExcelDataWrapper;
import it.nexera.ris.web.beans.wrappers.logic.FileWrapper;
import it.nexera.ris.web.common.RequestPriceListModel;
import it.nexera.ris.web.exceltemplate.CertificationExpense;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@Setter
@Getter
@ManagedBean
@ViewScoped
public class TranscriptionAndCertificationManagementBean extends BaseEntityPageBean implements Serializable {

    private static final long serialVersionUID = -2486361254576344821L;

    private TranscriptionAndCertificationHelper transcriptionAndCertificationHelper;
    private TranscriptionData transcriptionDataEntity;
    private Long transcriptionDataEntityId;
    private Long certificationDataEntityId;

    private int activeTabIndex;
    private Request transcriptionRequest;
    private String courierFileName;
    private byte[] courierDocumentContents;
    private String entryFileName;
    private byte[] entryDocumentContents;
    private Document courierDocument;
    private Document entryDocument;
    private StreamedContent pdfOfLetterFile;
    private Double ipotecarioCost;
    private Double ipotecarioPostale;
    private CostManipulationHelper costManipulationHelper;
    private Boolean disableStampFields;
    private List<ImportF24Pdf> importF24Pdfs;
    private ImportF24Pdf importF24Pdf = new ImportF24Pdf();
    int editedRow = -1;
    private Integer stampValue;
    private List<ExtraCost> additionalCosts;
    private List<ExtraCost> deletedAdditionalCosts;
    private Boolean isPaymentDateSaved;
    private Boolean isImportF24Saved;
    private Boolean isCourierEnvelopeDataSaved;
    private Boolean isCourierFileSaved;
    private Boolean isExtraCostsSaved;
    private Boolean isEntryFileSaved;

    private CertificationData certificationDataEntity;
    private Request certificationRequest;
    private String cadastralFileName;
    private byte[] cadastralDocumentContents;
    private Document cadastralDocument;
    private String mapFileName;
    private byte[] mapDocumentContents;
    private Document mapDocument;
    private Boolean mapFileExists;
    private String signedCertificationFileName;
    private byte[] signedCertificationDocumentContents;
    private Document signedCertificationDocument;
    private Boolean sendCourier;
    private Boolean sendCertification;
    private Double cadastralCost;
    private Double mortgageCost;
    private Double personalCost;
    private Double postalCost;
    private List<SelectItem> supportTypes;
    private String abstractMapFileName;
    private Document abstractMapDocument;
    private byte[] abstractMapDocumentContents;
    private List<Document> abstractMapDocumentList;
    private Long abstractMapDocumentId;
    private String certificationProcessedFileName;
    private Document certificationProcessedDocument;
    private byte[] certificationProcessedDocumentContents;

    private Boolean transcriptionPage;
    private Boolean certificationPage;
    private Boolean renewalPage;
    private Boolean transcriptionCertificationPage;
    private Boolean transcriptionTab = true;

    private Document document;
    private String fileName;

    private Long authorization;

    private Integer hasAuthorization;

    private Boolean authCustomer;

    private List<Document> otherDocuments;
    private String otherDocumentName;
    private Document otherDocument;
    private byte[] otherDocumentContents;
    private String otherDocumentNote;
    private Long otherDocumentId;
    private String otherFileNameTemp;
    private String selectedConserItemIds;
    private List<ConservatoriaSelectItem> selectedConserItemId;

    private String notAllRequestsFulfilled;
    private String invoiceAlreadyCreated;
    private List<Request> otherRequestsConsideredForInvoice;
    private String otherRequestsExistsForInvoice;
    private List<Request> requestsConsideredForInvoice;
    private String invoiceNumber;
    private Long number;
    private Client selectedInvoiceClient;
    private Long selectedInvoiceClientId;
    private Date invoiceDate;
    private List<InvoiceItem> selectedInvoiceItems;
    private List<Request> invoicedRequests;
    private List<GoodsServicesFieldWrapper> goodsServicesFields;
    private List<GoodsServicesFieldWrapper> deletedGoodsServicesFields;
    private Invoice tempInvoice;
    String invoiceErrorMessage;
    private boolean showRequestTab;
    private List<PaymentInvoice> paymentInvoices;
    private List<SelectItem> invoiceClients;
    private List<SelectItem> docTypes;
    private String documentType;
    private Date competence;
    private List<SelectItem> vatCollectabilityList;
    private List<SelectItem> paymentTypes;
    private Invoice selectedInvoice;
    private Long selectedClientId;
    private Long vatCollectabilityId;
    private Long selectedPaymentTypeId;
    private String invoiceNote;
    private String emailSubject;
    private boolean invoiceSentStatus;
    private List<SelectItem> clientProvinces;
    private List<SelectItem> clientAddressCities;
    private Boolean invoiceSaveAsDraft;
    private String clientAddressStreet;
    private String clientAddressPostalCode;
    private Long clientAddressCityId;
    private Long clientAddressProvinceId;
    private String clientNumberVAT;
    private String clientFiscalCode;
    private String clientMailPEC;
    private String clientAddressSDI;
    private List<String> sendTo;
    private List<String> sendCC;
    private List<String> sendFrom;
    private String emailTo;
    private String emailCC;
    private String emailFrom;
    private String emailBodyToEditor;
    private List<SelectItem> ums;
    private List<FileWrapper> invoiceEmailAttachedFiles;
    private Double paymentAmount;
    private Date paymentDate;
    private List<SelectItem> paymentTypeDescriptions;
    private Long selectedPaymentTypeDescription;
    private List<SelectItem> paymentOutcomes;
    private Long selectedPaymentOutcome;
    private PaymentInvoice selectedPaymentInvoice;
    private String apiError;
    private List<Invoice> unlockedInvoicesDialog;
    private Invoice selectedUnlockedInvoiceDialog;
    private List<SelectItem> userFolders;
    private Long selectedFolderId;
    private Boolean showCreateFatturaButton;
    private InvoiceHelper invoiceHelper;
    private String changeVar;
    private Document expenseDocument;
    private String expenseFileName;
    private byte[] expenseFileContents;
    private StreamedContent invoicePDFFile;
    @ManagedProperty(value="#{invoiceDialogBean}")
    private InvoiceDialogBean invoiceDialogBean;

    public Long getTranscriptionDataEntityId() {
        if (this.getTranscriptionDataEntity() != null && this.getTranscriptionDataEntity().getId() != null) {
            return this.getTranscriptionDataEntity().getId();
        } else {
            if (this.transcriptionDataEntityId != null) {
                return transcriptionDataEntityId;
            }
            return null;
        }
    }

    public void setTranscriptionDataEntityId(Long transcriptionDataEntityId) {
        this.transcriptionDataEntityId = transcriptionDataEntityId;
    }

    public Long getCertificationDataEntityId() {
        if (this.getCertificationDataEntity() != null && this.getCertificationDataEntity().getId() != null) {
            return this.getCertificationDataEntity().getId();
        } else {
            if (this.certificationDataEntityId != null) {
                return certificationDataEntityId;
            }
            return null;
        }
    }

    public void setCertificationDataEntityId(Long certificationDataEntityId) {
        this.certificationDataEntityId = certificationDataEntityId;
    }

    @Override
    protected void onConstruct() throws NumberFormatException, HibernateException {

        setSelectedConserItemIds(null);
        setSelectedConserItemId(new LinkedList<>());
        String referrentId = getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
        String transcriptionId = getRequestParameter(RedirectHelper.TRANSCRIPTION_ID);
        String certificationId = getRequestParameter(RedirectHelper.CERTIFICATION_ID);
        String page = getRequestParameter(RedirectHelper.PAGE);
        if(StringUtils.isNotBlank(page)){
            if (page.equals(TranscriptionPage.TRANSCRIPTIONCERTIFICATION.getPage())) {
                log.info("both");
                setTranscriptionCertificationPage(Boolean.TRUE);
            } else if (page.equals(TranscriptionPage.TRANSCRIPTION.getPage())) {
                log.info("transcription");
                setTranscriptionPage(Boolean.TRUE);
            } else if (page.equals(TranscriptionPage.CERTIFICATION.getPage())) {
                log.info("certification");
                setCertificationPage(Boolean.TRUE);
            } else if (page.equals(TranscriptionPage.RENEWAL.getPage())) {
                log.info("renewal");
                setRenewalPage(Boolean.TRUE);
            }
        }
        if (StringUtils.isNotBlank(referrentId)) {
            Request request;
            try {
                request = DaoManager.get(Request.class, Long.parseLong(referrentId));
                if(!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())){
                    AggregationLandChargesRegistry aggregationLandChargesRegistry = DaoManager.get(
                            AggregationLandChargesRegistry.class,
                            new CriteriaAlias[]{
                                    new CriteriaAlias("landChargesRegistries", "landChargesRegistries", JoinType.INNER_JOIN)
                            },
                            new Criterion[]{
                                    Restrictions.eq("id", request.getAggregationLandChargesRegistry().getId())
                            }
                    );
                    if (!ValidationHelper.isNullOrEmpty(aggregationLandChargesRegistry.getLandChargesRegistries())) {
                        for (LandChargesRegistry registry : aggregationLandChargesRegistry.getLandChargesRegistries()) {
                            if (!ValidationHelper.isNullOrEmpty(registry.getType())) {
                                if (LandChargesRegistryType.CONSERVATORY.name().equalsIgnoreCase(registry.getType().name())) {
                                    ConservatoriaSelectItem item = new ConservatoriaSelectItem(request.getAggregationLandChargesRegistry());
                                    if (!getSelectedConserItemId().contains(item)) {
                                        getSelectedConserItemId().add(item);
                                    }
                                }
                            }
                        }
                        setSelectedConserItemIds(getSelectedConserItemId()
                                .stream()
                                .map(ConservatoriaSelectItem::getId)
                                .map(String::valueOf)
                                .collect(Collectors.joining(",")));
                    }
                }
                setTranscriptionRequest(request);
                setCertificationRequest(request);

                loadOtherDocumentList();

                if (!ValidationHelper.isNullOrEmpty(transcriptionId)) {
                    TranscriptionData transcriptionData = DaoManager.get(TranscriptionData.class, Long.parseLong(transcriptionId));
                    if (!ValidationHelper.isNullOrEmpty(transcriptionData)) {
                        setTranscriptionDataEntity(transcriptionData);
                    }
                }
                if (getTranscriptionDataEntity() == null)
                    setTranscriptionDataEntity(new TranscriptionData());

                if (!ValidationHelper.isNullOrEmpty(certificationId)) {
                    CertificationData certificationData = DaoManager.get(CertificationData.class, Long.parseLong(certificationId));
                    if (!ValidationHelper.isNullOrEmpty(certificationData)) {
                        setCertificationDataEntity(certificationData);
                    }
                }
                if (getCertificationDataEntity() == null)
                    setCertificationDataEntity(new CertificationData());

            } catch (InstantiationException | IllegalAccessException | PersistenceBeanException e) {
                e.printStackTrace();
            }
        } else {
            setTranscriptionRequest(null);
            setCertificationRequest(null);
        }
        try {
            loadTranscriptionData();
            loadCertificationData();
        } catch (IllegalAccessException | PersistenceBeanException | IOException | InstantiationException e) {
            e.printStackTrace();
        }
        setTranscriptionAndCertificationHelper(new TranscriptionAndCertificationHelper());
        setTranscriptionAmount();
        updateImportF24Anticipated();
        setRenewalData();
        invoiceHelper = new InvoiceHelper();
        Request request = (getTranscriptionPage() != null && getTranscriptionPage()) ? getTranscriptionRequest()
                : getCertificationRequest();
        if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getStateId())
            && request.getStateId().equals(RequestState.EVADED.getId()) && ValidationHelper.isNullOrEmpty(request.getInvoice())){
            setShowCreateFatturaButton(Boolean.TRUE);
        }else
            setShowCreateFatturaButton(Boolean.FALSE);


        if(SessionHelper.get("selectedTranscriptionTab") != null){
            this.setActiveTabIndex(Integer.parseInt(SessionHelper.get("selectedTranscriptionTab").toString()));
            SessionHelper.removeObject("selectedTranscriptionTab");
        }
        if(SessionHelper.get("selectedMainTab") != null){
            executeJS("$('#" + SessionHelper.get("selectedMainTab") + "').trigger('click');");
            SessionHelper.removeObject("selectedMainTab");
        }
    }

    private void setRenewalData(){
        setAuthCustomer(Boolean.FALSE);
        if(!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity())
                && getTranscriptionDataEntity().getAuthorizationCustomer() != null){
            setHasAuthorization(1);
            if(getTranscriptionDataEntity().getAuthorizationCustomer())
                setAuthCustomer(Boolean.TRUE);
        } else
            setHasAuthorization(0);
    }
    public void loadTranscriptionData() throws NumberFormatException, HibernateException, PersistenceBeanException, IllegalAccessException, IOException {
        if (!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity()) &&
                ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getCourierEnvelopeDate()))
            getTranscriptionDataEntity().setCourierEnvelopeDate(new Date());
        if (!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity()) && getTranscriptionDataEntity().isNew())
            getTranscriptionDataEntity().setRequest(getTranscriptionRequest());
        if(!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity()) && StringUtils.isBlank(getTranscriptionDataEntity().getNote()))
            getTranscriptionDataEntity().setNote("Nota:");
        setDisableStampFields(Boolean.FALSE);
        if (!ValidationHelper.isNullOrEmpty(getTranscriptionRequest())) {
            if (ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getAggregationLandChargesRegistry()) ||
                    ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getAggregationLandChargesRegistry().getStamp()) ||
                    !getTranscriptionRequest().getAggregationLandChargesRegistry().getStamp()) {
                setDisableStampFields(Boolean.TRUE);
            }
        }
        if (!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity()) &&
                !ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getCourierDocument())) {
            setCourierFileName(FileHelper.getFileName(getTranscriptionDataEntity().getCourierDocument().getPath()));
            setCourierDocumentContents(FileHelper.loadContentByPath(getTranscriptionDataEntity().getCourierDocument().getPath()));
            setCourierDocument(getTranscriptionDataEntity().getCourierDocument());
        } else {
            setCourierFileName(null);
            setCourierDocumentContents(null);
            setCourierDocument(null);
        }

        if (!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity()) &&
                !ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getEntryDocument())) {
            setEntryFileName(FileHelper.getFileName(getTranscriptionDataEntity().getEntryDocument().getPath()));
            setEntryDocumentContents(FileHelper.loadContentByPath(getTranscriptionDataEntity().getEntryDocument().getPath()));
            setEntryDocument(getTranscriptionDataEntity().getEntryDocument());
        } else {
            setEntryFileName(null);
            setEntryDocumentContents(null);
            setEntryDocument(null);
        }
        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", getTranscriptionRequest().getId()),
                Restrictions.eq("type", ExtraCostType.IPOTECARIO)});

        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            setIpotecarioCost(extraCosts.get(0).getPrice());
        }
        extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", getTranscriptionRequest().getId()),
                Restrictions.eq("type", ExtraCostType.POSTALE)});

        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            setIpotecarioPostale(extraCosts.get(0).getPrice());
        }

        setCostManipulationHelper(new CostManipulationHelper());
        getCostManipulationHelper().setMortgageTypeList(ComboboxHelper.fillList(MortgageType.class, false, false));
        if (!ValidationHelper.isNullOrEmpty(getTranscriptionRequest())){
            if (!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getClient()) &&
                    !ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getClient().getCostOutput())) {
                this.getCostManipulationHelper().setCostOutput(getTranscriptionRequest().getClient().getCostOutput());
            } else {
                this.getCostManipulationHelper().setCostOutput(false);
            }
        }

        setSupportTypes(ComboboxHelper.fillList(SupportType.class, false));

        loadImportF24PdfData();

        if (!ValidationHelper.isNullOrEmpty(getTranscriptionRequest())){
            List<ExtraCost> marcaCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", getTranscriptionRequest().getId()),
                    Restrictions.eq("type", ExtraCostType.MARCA)});

            Double costValue = emptyIfNull(marcaCosts).stream().filter(o -> o.getPrice() != null).mapToDouble(ExtraCost::getPrice).sum();
            setStampValue(costValue.intValue());

            setAdditionalCosts(DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", getTranscriptionRequest().getId()),
                    Restrictions.eq("type", ExtraCostType.ALTRO)}));
        }

        if(!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity())){
            if (!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getPaymentDate())) {
                setIsPaymentDateSaved(Boolean.TRUE);
            }
            if (!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getTranscriptionAmount())
                    && getTranscriptionDataEntity().getTranscriptionAmount().doubleValue() > 0.0) {
                setIsImportF24Saved(Boolean.TRUE);
            }
            if (!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getCourierEnvelope())
                    && getTranscriptionDataEntity().getCourierEnvelope() && !ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getCourierEnvelopeDate())) {
                setIsCourierEnvelopeDataSaved(Boolean.TRUE);
            }
        }
        if (!ValidationHelper.isNullOrEmpty(getCourierFileName())) {
            setIsCourierFileSaved(Boolean.TRUE);
        }
        if (!ValidationHelper.isNullOrEmpty(getAdditionalCosts())) {
            setIsExtraCostsSaved(Boolean.TRUE);
        }
        if (!ValidationHelper.isNullOrEmpty(getEntryFileName())) {
            setIsEntryFileSaved(Boolean.TRUE);
        }
    }

    public void saveTranscription() throws HibernateException, PersistenceBeanException {

        String selectedTranscriptionTab = SessionHelper.get("selectedTranscriptionTab") != null ?
                SessionHelper.get("selectedTranscriptionTab").toString() : "";
        if(!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity()) && getTranscriptionDataEntity().getManualF24() != null
            && getTranscriptionDataEntity().getManualF24()){
            getImportF24Pdfs()
                    .stream()
                    .filter(f -> !ValidationHelper.isNullOrEmpty(f.getImportF24Id()))
                    .forEach(f -> {

                        f.setManualF24(Boolean.TRUE);
                        f.setIdImportF24(f.getImportF24Id());
                        f.setTranscriptionData(getTranscriptionDataEntity());
                        try {
                            DaoManager.save(f, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            LogHelper.log(log, e);
                        }
                    });
        }

        boolean isNew = this.getTranscriptionDataEntity().isNew();
        if(StringUtils.isNotBlank(transcriptionDataEntity.getNote())
                && transcriptionDataEntity.getNote().trim().equalsIgnoreCase("Nota:"))
            transcriptionDataEntity.setNote(null);
        if(transcriptionDataEntity.getAnticipatedF24() != null &&
                !transcriptionDataEntity.getAnticipatedF24()) {
            transcriptionDataEntity.setImportF24Anticipated(null);
            setTranscriptionAmount();
        }else {
            transcriptionDataEntity.setTranscriptionAmount(null);
        }
        setTranscriptionDataEntity(getTranscriptionAndCertificationHelper().saveTranscription(getTranscriptionRequest(), getTranscriptionDataEntity(),
                getIpotecarioCost(), getIpotecarioPostale(), getCourierFileName(), getCourierDocument(), getCourierDocumentContents(),
                getEntryFileName(), getEntryDocument(), getEntryDocumentContents()));
        if (isNew) {
            if (ValidationHelper.isNullOrEmpty(getCertificationDataEntityId())) {
                if(getRenewalPage() != null && getRenewalPage()){
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.RENEWAL,
                            getTranscriptionRequest().getId(), getTranscriptionDataEntity().getId(), getCertificationDataEntityId());
                }else if (getTranscriptionCertificationPage() != null && getTranscriptionCertificationPage()) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTIONCERTIFICATION,
                            getTranscriptionRequest().getId(), getTranscriptionDataEntity().getId(), null);
                } else if (getTranscriptionPage() != null && getTranscriptionPage()) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTION,
                            getTranscriptionRequest().getId(), getTranscriptionDataEntity().getId(), null);
                }
            } else {
                if (getTranscriptionCertificationPage()!= null && getTranscriptionCertificationPage()) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTIONCERTIFICATION,
                            getTranscriptionRequest().getId(), getTranscriptionDataEntity().getId(), getCertificationDataEntityId());
                } else if (getTranscriptionPage()) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTION,
                            getTranscriptionRequest().getId(), getTranscriptionDataEntity().getId(), getCertificationDataEntityId());
                }
            }
        }

        if (!ValidationHelper.isNullOrEmpty(getAdditionalCosts())) {
            for (ExtraCost extraCost : getAdditionalCosts()) {
                extraCost.setRequestId(getTranscriptionRequest().getId());
                extraCost.setType(ExtraCostType.ALTRO);
                DaoManager.save(extraCost, true);
            }
        }
        if (!ValidationHelper.isNullOrEmpty(getDeletedAdditionalCosts())) {
            for (ExtraCost extraCost : getDeletedAdditionalCosts()) {
                if(!extraCost.isNew())
                    DaoManager.remove(extraCost, true);
            }
        }
        if (!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getPaymentDate())) {
            setIsPaymentDateSaved(Boolean.TRUE);
        }
        if (!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getTranscriptionAmount())
                && getTranscriptionDataEntity().getTranscriptionAmount().doubleValue() > 0.0) {
            setIsImportF24Saved(Boolean.TRUE);
        }
        if (!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getCourierEnvelope())
                && getTranscriptionDataEntity().getCourierEnvelope()
                && !ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getCourierEnvelopeDate())) {
            setIsCourierEnvelopeDataSaved(Boolean.TRUE);
        }
        if (!ValidationHelper.isNullOrEmpty(getCourierFileName())) {
            setIsCourierFileSaved(Boolean.TRUE);
        }
        if (!ValidationHelper.isNullOrEmpty(getAdditionalCosts())) {
            setIsExtraCostsSaved(Boolean.TRUE);
        }
        if (!ValidationHelper.isNullOrEmpty(getEntryFileName())) {
            setIsEntryFileSaved(Boolean.TRUE);
        }
        loadTabs();
        setRenewalData();
        setDeletedAdditionalCosts(null);
        if(StringUtils.isNotBlank(selectedTranscriptionTab)){
            this.setActiveTabIndex(Integer.parseInt(selectedTranscriptionTab));
        }
    }

    public void handleCourierFileUpload(FileUploadEvent event) throws PersistenceBeanException {
        setCourierFileName(event.getFile().getFileName());
        setCourierDocumentContents(event.getFile().getContents());
    }

    public void downloadCourierDocument() {
        if (!ValidationHelper.isNullOrEmpty(getCourierFileName()) && !ValidationHelper.isNullOrEmpty(getCourierDocumentContents())) {
            FileHelper.sendFile(getCourierFileName(), getCourierDocumentContents());
        }
    }

    public void deleteCourierDocument() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        setCourierFileName(null);
        setCourierDocumentContents(null);
        getTranscriptionDataEntity().setCourierDocument(null);
    }

    public void handleEntryFileUpload(FileUploadEvent event) throws PersistenceBeanException {
        setEntryFileName(event.getFile().getFileName());
        setEntryDocumentContents(event.getFile().getContents());
    }

    public void downloadEntryDocument() {
        if (!ValidationHelper.isNullOrEmpty(getEntryFileName()) && !ValidationHelper.isNullOrEmpty(getEntryDocumentContents())) {
            FileHelper.sendFile(getEntryFileName(), getEntryDocumentContents());
        }
    }

    public void deleteEntryDocument() {
        setEntryFileName(null);
        setEntryDocumentContents(null);
        getTranscriptionDataEntity().setEntryDocument(null);
    }

    public void preparePdfOfLetter() throws IOException, PersistenceBeanException, IllegalAccessException, InstantiationException {
        getTranscriptionAndCertificationHelper().preparePdfOfLetter(getTranscriptionRequest(), getTranscriptionDataEntity());
    }

    /*public void createNewMail(String documentType) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        RedirectHelper.goToMailEditTranscription(getTranscriptionRequest().getId(), getTranscriptionDataEntityId(), documentType, MailEditType.REQUEST_REPLY_ALL);
    }*/


    public void createNewNoteMail(String documentType) {
        if(StringUtils.isNotBlank(documentType) && documentType.equalsIgnoreCase(DocumentType.SIGNED_CERTIFICATE.toString())){
            if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getMail())) {
                RedirectHelper.goToMailEditCertification(getTranscriptionRequest().getId(), getCertificationDataEntityId(),
                        documentType, MailEditType.REQUEST_REPLY_ALL, TranscriptionEmailType.CERTIFICATION);
            } else {
                RedirectHelper.goToMailEditCertification(getTranscriptionRequest().getId(), getCertificationDataEntityId(),
                        documentType, MailEditType.NEW, TranscriptionEmailType.CERTIFICATION);
            }
        }else if(StringUtils.isNotBlank(documentType) && documentType.equalsIgnoreCase(DocumentType.ALLEGATI.toString())){
            RedirectHelper.goToMailEditTranscription(getTranscriptionRequest().getId(), getTranscriptionDataEntityId(),
                    documentType, MailEditType.NEW, TranscriptionEmailType.CLIENT_EMAIL);
        }else {
            RedirectHelper.goToMailEditTranscription(getTranscriptionRequest().getId(), getTranscriptionDataEntityId(),
                    documentType, getTranscriptionRequest().getMail() != null ? MailEditType.REQUEST_REPLY_ALL :
                            MailEditType.NEW, TranscriptionEmailType.NOTE);
        }

    }
    public void createNewMail(String transcriptionEmailType) {
        if(StringUtils.isNotBlank(transcriptionEmailType)
                && transcriptionEmailType.equalsIgnoreCase(TranscriptionEmailType.COURIER_CLIENT_EMAIL.toString())){
            if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getMail())) {
                RedirectHelper.goToMailEditTranscription(getTranscriptionRequest().getId(), getTranscriptionDataEntityId(),
                        null, MailEditType.REQUEST_REPLY_ALL, TranscriptionEmailType.COURIER_CLIENT_EMAIL);
            } else {
                RedirectHelper.goToMailEditTranscription(getTranscriptionRequest().getId(), getTranscriptionDataEntityId(),
                        null, MailEditType.NEW, TranscriptionEmailType.COURIER_CLIENT_EMAIL);
            }
        }
    }
    public void createNewDuploMail(String documentType) {
        log.info("createNewDuploMail : " + documentType);
        RedirectHelper.goToMailEditTranscription(getTranscriptionRequest().getId(), getTranscriptionDataEntityId(),
                documentType, MailEditType.REQUEST_REPLY_ALL, TranscriptionEmailType.DUPLO);
    }

    public void createNewCertificationMail(String documentType) {
        log.info("createNewCertificationMail : " + documentType);
        RedirectHelper.goToMailEditCertification(getTranscriptionRequest().getId(), getCertificationDataEntityId(),
                documentType, MailEditType.REQUEST_REPLY_ALL, TranscriptionEmailType.CERTIFICATION);
    }

    public void createNewNotaryCertificationMail(String documentType) {
        log.info("createNewNotaryCertificationMail : " + documentType);
        RedirectHelper.goToMailEditCertification(getTranscriptionRequest().getId(), getCertificationDataEntityId(),
                documentType, MailEditType.NEW, TranscriptionEmailType.NOTARY_CERTIFICATION);
    }
    
    public void createNewCourierCertificationMail(String transcriptionEmailType) {
        if(StringUtils.isNotBlank(transcriptionEmailType)
                && transcriptionEmailType.equalsIgnoreCase(TranscriptionEmailType.COURIER_CLIENT_EMAIL.toString())){
            if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getMail())) {
                RedirectHelper.goToMailEditTranscription(getCertificationRequest().getId(), getCertificationDataEntityId(),
                        null, MailEditType.REQUEST_REPLY_ALL, TranscriptionEmailType.COURIER_CLIENT_EMAIL);
            } else {
                RedirectHelper.goToMailEditTranscription(getTranscriptionRequest().getId(), getCertificationDataEntityId(),
                        null, MailEditType.NEW, TranscriptionEmailType.COURIER_CLIENT_EMAIL);
            }
        }
    }

    public void downloadPdfFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        try {
            if (!ValidationHelper.isNullOrEmpty(importF24Pdfs))
                getTranscriptionRequest().setImportF24PdfList(importF24Pdfs);

            String body = getTranscriptionAndCertificationHelper().generatePdfPage("I° COPIA PER LA BANCA/POSTE/AGENTE DELLA RISCOSSIONE", true, getTranscriptionRequest());
            body += "<pd4ml-page-break />";
            body += getTranscriptionAndCertificationHelper().generatePdfPage("2° COPIA PER LA BANCA/POSTE/AGENTE DELLA RISCOSSIONE", false, getTranscriptionRequest());
            body += "<pd4ml-page-break />";
            body += getTranscriptionAndCertificationHelper().generatePdfPage("COPIA PER IL SOGGETTO CHE EFFETTUA IL VERSAMENTO", false, getTranscriptionRequest());
            FileHelper.sendFile("transcription-" + this.getTranscriptionDataEntityId() + ".pdf",
                    PrintPDFHelper.convertToPDF(null, body, null,
                            DocumentType.TRANSCRIPTION_AND_CERTIFICATION));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void generateReportSpesePdf(Boolean isPdf) {
        try {
            log.info("Inside generate REPORT SPESE");
            byte[] excelFile = getTranscriptionAndCertificationHelper().getXlsBytesReportSpesePdf(transcriptionRequest);
            if (!ValidationHelper.isNullOrEmpty(excelFile)) {
                String tmpFileNameSuffix = "REPORT SPESE";
                
                String tempDir = FileHelper.getLocalTempDir();
                tempDir += File.separator + UUID.randomUUID();
                FileUtils.forceMkdir(new File(tempDir));

                FileHelper.writeFileToFolder(tmpFileNameSuffix + ".xls", new File(tempDir), excelFile);

                String path = tempDir + File.separator + tmpFileNameSuffix + ".xls";

                File file = new File(path);
                if(isPdf) {
                	String sofficeCommand = ApplicationSettingsHolder.getInstance().getByKey(
                                    ApplicationSettingsKeys.SOFFICE_COMMAND).getValue().trim();
                	VisureManageHelper.sendPDFfromXLSFile(file, sofficeCommand, tempDir, path);
                	log.info("Pdf REPORT SPESE generated");
                } else {
                	VisureManageHelper.sendXLSFile(tmpFileNameSuffix + ".xls", file, tempDir, path);
                	log.info("Excel REPORT SPESE generated");
                }
            }
            log.info("Leaving generate REPORT SPESE");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void loadImportF24PdfData() throws PersistenceBeanException, IllegalAccessException {
        List<ImportF24> importF24sData = transcriptionRequest != null && transcriptionRequest.getSpecialFormality() != null ?
                transcriptionRequest.getSpecialFormality().getImportF24List() : null;
        List<ImportF24Pdf> importF24PdfsData = null;
        if (getTranscriptionDataEntity() != null && getTranscriptionDataEntity().getId() != null) {
            importF24PdfsData = DaoManager.load(ImportF24Pdf.class,
                    new Criterion[]{Restrictions.eq("transcriptionData", getTranscriptionDataEntity())});
        }
        importF24Pdfs = new ArrayList<>();
        if (importF24sData != null) {
            Calendar lastNotificationDate = Calendar.getInstance();
            if (transcriptionRequest.getLastNotificationDate() != null) {
                lastNotificationDate.setTime(transcriptionRequest.getLastNotificationDate());
                lastNotificationDate.add(Calendar.DAY_OF_YEAR, 20);
            }
            Calendar currentDate = Calendar.getInstance();
            boolean isLastNotificationDateBeforeCurrentDate = transcriptionRequest.getLastNotificationDate() != null ? currentDate.getTimeInMillis() > lastNotificationDate.getTimeInMillis() : false;
            boolean penaltyCheck = (transcriptionRequest.getAggregationLandChargesRegistry() == null
                    || transcriptionRequest.getAggregationLandChargesRegistry().getPenalty() == null || !transcriptionRequest.getAggregationLandChargesRegistry().getPenalty()
                    || isLastNotificationDateBeforeCurrentDate);
            List<Long> existingImportF24Ids = emptyIfNull(importF24PdfsData)
                    .stream()
                    .filter(i -> !ValidationHelper.isNullOrEmpty(i.getIdImportF24()))
                    .map(ImportF24Pdf::getIdImportF24)
                    .collect(Collectors.toList());

            for (ImportF24 importF24 : importF24sData) {
                boolean isImportF24Added = false;
                if(existingImportF24Ids == null || !existingImportF24Ids.contains(importF24.getId())){
                    if (importF24.getPenalty() != null && importF24.getPenalty() && penaltyCheck) {
                        isImportF24Added = true;
                    } else if(ValidationHelper.isNullOrEmpty(importF24.getPenalty())
                            || (!ValidationHelper.isNullOrEmpty(importF24.getPenalty()) && !importF24.getPenalty())) {
                        isImportF24Added = true;
                    }
                }
                if(isImportF24Added) {
                    ImportF24Pdf importF24Pdf = new ImportF24Pdf();
                    importF24Pdf.setImportF24Id(importF24.getId());
                    if (importF24 != null && importF24.getIncludeNumber() != null && importF24.getIncludeNumber()
                            && !ValidationHelper.isNullOrEmpty(getTranscriptionRequest())) {
                        importF24Pdf.setF24IdentificationNumber(getTranscriptionRequest().getF24IdentificationNumber());
                    }
                    importF24Pdf.setCode(importF24.getCode());
                    importF24Pdf.setPercentage(importF24.getPercentage());
                    importF24Pdf.setReferenceYear(transcriptionRequest.getReferenceYear() != null ? transcriptionRequest.getReferenceYear() : null);
                    if (!ValidationHelper.isNullOrEmpty(importF24.getPercentage()) && importF24.getPercentage().doubleValue() > 0.0) {
                        if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getMortagageImport()) && transcriptionRequest.getMortagageImport().doubleValue() > 0.0) {
                            importF24Pdf.setF24Import(transcriptionRequest.getMortagageImport().doubleValue() * importF24.getPercentage().doubleValue() / 100);
                        }
                    } else {
                        importF24Pdf.setF24Import(importF24.getF24Import() != null && importF24.getF24Import() > 0 ? importF24.getF24Import() : null);
                    }
                    importF24Pdf.setType(importF24.getType());
                    if (importF24.getImportF24Pdf() != null) {
                        importF24Pdf.setId(importF24.getImportF24Pdf().getId());
                        importF24Pdf.setF24IdentificationNumber(importF24.getImportF24Pdf().getF24IdentificationNumber());
                        importF24Pdf.setCode(importF24.getCode());
                        importF24Pdf.setPercentage(importF24.getPercentage());
                        importF24Pdf.setReferenceYear(importF24.getImportF24Pdf().getReferenceYear() != null ? importF24.getImportF24Pdf().getReferenceYear() : null);
                        if (!ValidationHelper.isNullOrEmpty(importF24.getPercentage())
                                && importF24.getPercentage().doubleValue() > 0.0) {
                            if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getMortagageImport())
                                    && transcriptionRequest.getMortagageImport().doubleValue() > 0.0) {
                                importF24Pdf.setF24Import(transcriptionRequest.getMortagageImport().doubleValue()
                                        * importF24.getPercentage().doubleValue() / 100);
                            }
                        } else {
                            importF24Pdf.setF24Import(importF24.getImportF24Pdf().getF24Import() != null && importF24.getImportF24Pdf().getF24Import() > 0 ? importF24.getImportF24Pdf().getF24Import() : null);
                        }
                        importF24Pdf.setType(importF24.getImportF24Pdf().getType());
                    }
                    importF24Pdfs.add(importF24Pdf);
                }
            }
        }
        if (getTranscriptionDataEntity() != null && getTranscriptionDataEntity().getId() != null) {
            if (importF24PdfsData != null) {
                for (ImportF24Pdf importF24Pdf : importF24PdfsData) {
                    ImportF24Pdf importF24PdfExists = importF24Pdfs.stream()
                            .filter(importF24PdfFilter -> importF24Pdf.getId().equals(importF24PdfFilter.getId()))
                            .findAny()
                            .orElse(null);
                    if (importF24PdfExists == null) {
                        ImportF24Pdf importF24Pdf1 = new ImportF24Pdf();
                        importF24Pdf1.setId(importF24Pdf.getId());
                        importF24Pdf1.setF24IdentificationNumber(importF24Pdf.getF24IdentificationNumber());
                        importF24Pdf1.setCode(importF24Pdf.getCode());
                        importF24Pdf1.setPercentage(importF24Pdf.getPercentage());
                        importF24Pdf1.setReferenceYear(importF24Pdf.getReferenceYear() != null ? importF24Pdf.getReferenceYear() : null);

                        if (!ValidationHelper.isNullOrEmpty(importF24Pdf.getPercentage()) && importF24Pdf.getPercentage().doubleValue() > 0.0) {
                            if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getMortagageImport()) && transcriptionRequest.getMortagageImport().doubleValue() > 0.0) {
                                importF24Pdf1.setF24Import(transcriptionRequest.getMortagageImport().doubleValue() * importF24Pdf.getPercentage().doubleValue() / 100);
                            }
                        } else {
                            importF24Pdf1.setF24Import(importF24Pdf.getF24Import() != null
                                    && importF24Pdf.getF24Import() > 0 ? importF24Pdf.getF24Import() : null);
                        }
                        importF24Pdf1.setType(importF24Pdf.getType());
                        importF24Pdfs.add(importF24Pdf1);
                    }
                }
            }
        }
        importF24Pdfs.sort(Comparator.comparing(a -> a.getCode(),
                Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    public void add() {
        importF24Pdf = new ImportF24Pdf();
    }

    public void edit(ImportF24Pdf importF24, int row) {
        importF24Pdf.setId(importF24.getId());
        importF24Pdf.setF24IdentificationNumber(importF24.getF24IdentificationNumber());
        importF24Pdf.setCode(importF24.getCode());
        importF24Pdf.setReferenceYear(importF24.getReferenceYear());
        importF24Pdf.setF24Import(importF24.getF24Import());
        importF24Pdf.setImportF24Id(importF24.getImportF24Id());
        importF24Pdf.setType(importF24.getType());
        importF24Pdf.setPercentage(importF24.getPercentage());
        editedRow = row;
    }

    public void saveImportF24() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        boolean redirect = false;
        if (getTranscriptionDataEntity().isNew()) {
            DaoManager.save(getTranscriptionDataEntity());
            redirect = true;
        }
        if (editedRow != -1) {
           if (importF24Pdf.getId() != null) {
                ImportF24Pdf importF24PdfDb = DaoManager.get(ImportF24Pdf.class,
                        new Criterion[]{
                                Restrictions.eq("id", importF24Pdf.getId())
                });
                importF24PdfDb.setF24IdentificationNumber(importF24Pdf.getF24IdentificationNumber());
                importF24PdfDb.setCode(importF24Pdf.getCode());
                importF24PdfDb.setReferenceYear(importF24Pdf.getReferenceYear());
                importF24PdfDb.setPercentage(importF24Pdf.getPercentage());
                if (!ValidationHelper.isNullOrEmpty(importF24Pdf.getPercentage()) && importF24Pdf.getPercentage().doubleValue() > 0.0) {
                    if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getMortagageImport()) && transcriptionRequest.getMortagageImport().doubleValue() > 0.0) {
                        importF24PdfDb.setF24Import(transcriptionRequest.getMortagageImport().doubleValue() * importF24Pdf.getPercentage().doubleValue() / 100);
                    }
                }
                else {
                    importF24PdfDb.setF24Import(importF24Pdf.getF24Import());
                }
                importF24PdfDb.setType(importF24Pdf.getType());
                DaoManager.save(importF24PdfDb, true);
            } else {
                importF24Pdf.setTranscriptionData(getTranscriptionDataEntity());
                if(importF24Pdf.getImportF24Id() != null && getTranscriptionDataEntity().getManualF24() != null
                        && getTranscriptionDataEntity().getManualF24()){
                    importF24Pdf.setIdImportF24(importF24Pdf.getImportF24Id());
                    importF24Pdf.setManualF24(Boolean.TRUE);

                }else {
                    if (importF24Pdf.getImportF24Id() != null && (getTranscriptionDataEntity().getManualF24() == null
                            || getTranscriptionDataEntity().getManualF24())) {
                        importF24Pdf.setIdImportF24(null);
                        importF24Pdf.setManualF24(Boolean.FALSE);
                    }
                }
                DaoManager.save(importF24Pdf, true);
               ImportF24 importF24 = DaoManager.get(ImportF24.class, new Criterion[]{Restrictions.eq("id", importF24Pdf.getImportF24Id())});
               if (importF24 != null) {
                   importF24.setImportF24Pdf(importF24Pdf);
                   DaoManager.save(importF24, true);
               }
            }
        } else {
            ImportF24Pdf importF24PdfNew = new ImportF24Pdf();
            importF24PdfNew.setTranscriptionData(getTranscriptionDataEntity());
            importF24PdfNew.setF24IdentificationNumber(importF24Pdf.getF24IdentificationNumber());
            importF24PdfNew.setCode(importF24Pdf.getCode());
            importF24PdfNew.setPercentage(importF24Pdf.getPercentage());
            importF24PdfNew.setReferenceYear(importF24Pdf.getReferenceYear());
            if (!ValidationHelper.isNullOrEmpty(importF24Pdf.getPercentage()) && importF24Pdf.getPercentage().doubleValue() > 0.0) {
                if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getMortagageImport()) && transcriptionRequest.getMortagageImport().doubleValue() > 0.0) {
                    importF24PdfNew.setF24Import(transcriptionRequest.getMortagageImport().doubleValue() * importF24Pdf.getPercentage().doubleValue() / 100);
                }
            } else {
                importF24PdfNew.setF24Import(importF24Pdf.getF24Import());
            }
            importF24PdfNew.setType(importF24Pdf.getType());
            DaoManager.save(importF24PdfNew, true);
            getTranscriptionDataEntity().setTranscriptionAmount(getTranscriptionDataEntity().getTranscriptionAmount()  + importF24PdfNew.getF24Import());
            DaoManager.save(getTranscriptionDataEntity(), true);
        }
        loadImportF24PdfData();
        if (redirect) {
            if (getTranscriptionCertificationPage() != null && getTranscriptionCertificationPage()) {
                RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTIONCERTIFICATION,
                        getTranscriptionRequest().getId(), getTranscriptionDataEntity().getId(), null);
            } else if (getTranscriptionPage() != null && getTranscriptionPage()) {
                RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTION,
                        getTranscriptionRequest().getId(), getTranscriptionDataEntity().getId(), null);
            }
        }
    }

    public void beforeDelete(ImportF24Pdf importF24Pdf, int row) {
        this.importF24Pdf = importF24Pdf;
    }

    public void delete() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        log.info(importF24Pdf.getImportF24Id());
        log.info(importF24Pdf.getId());

        if (importF24Pdf.getImportF24Id() != null) {
            ImportF24 importF24 = DaoManager.get(ImportF24.class, new Criterion[]{Restrictions.eq("id", importF24Pdf.getImportF24Id())});
            if (importF24 != null) {
                importF24.setImportF24Pdf(null);
                DaoManager.save(importF24, true);
            }
        }
        if (importF24Pdf.getId() != null) {
            ImportF24Pdf importF24PdfDb = DaoManager.get(ImportF24Pdf.class, new Criterion[]{Restrictions.eq("id", importF24Pdf.getId())});
            ImportF24 importF24 = DaoManager.get(ImportF24.class, new Criterion[]{Restrictions.eq("importF24Pdf",importF24PdfDb)});
            if(!ValidationHelper.isNullOrEmpty(importF24)) {
                DaoManager.remove(importF24, true);
            } else {
                DaoManager.remove(importF24PdfDb, true);
            }
        }
        editedRow = -1;
        importF24Pdf = new ImportF24Pdf();
        loadImportF24PdfData();
    }

    public void setTranscriptionAmount() {
        if (transcriptionDataEntity != null
                && (transcriptionDataEntity.getAnticipatedF24() == null || !transcriptionDataEntity.getAnticipatedF24())) {
            try {
                loadImportF24PdfData();
            } catch (Exception e) {
                e.printStackTrace();
                LogHelper.log(log, e);
            }
            transcriptionDataEntity.setTranscriptionAmount(sumOfImport());
//            transcriptionDataEntity.setTranscriptionAmount(
//                    (transcriptionDataEntity.getTranscriptionAmount() != null
//                            && !transcriptionDataEntity.getTranscriptionAmount().equals(0)) ?
//                            transcriptionDataEntity.getTranscriptionAmount() : sumOfImport())   ;
        }
    }

    public Double sumOfImport() {
        if (!ValidationHelper.isNullOrEmpty(importF24Pdfs)) {
            return importF24Pdfs.stream().filter(x -> !ValidationHelper.isNullOrEmpty(x.getF24Import()))
                    .map(x -> x.getF24Import())
                    .collect(Collectors.summingDouble(Double::doubleValue));
        }
        return 0.00;
    }

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (newValue != null && !newValue.equals(oldValue)) {
            System.out.println("msg :: " + " Cell Changed Old: " + oldValue + ", New:" + newValue);
        }
    }

    public void onAddNew() {
        // Add one new extra cost to the table:
        getAdditionalCosts().add(new ExtraCost());
    }

    public void onDeleteCost(int index) {
        if(getAdditionalCosts() != null && index > -1 && index <= getAdditionalCosts().size()){
            if(getDeletedAdditionalCosts() == null)
                setDeletedAdditionalCosts(new ArrayList<>());
            getDeletedAdditionalCosts().add(getAdditionalCosts().get(index));
            getAdditionalCosts().remove(index);
        }
    }

    /************************************************* CERTIFICATION CODE **************************************************************************/

    public void loadCertificationData() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        if (getCertificationDataEntity().isNew())
            getCertificationDataEntity().setRequest(getCertificationRequest());

        //if (!ValidationHelper.isNullOrEmpty(getCertificationDataEntity().getCadastralDocument())) {
        //setCadastralFileName(FileHelper.getFileName(getCertificationDataEntity().getCadastralDocument().getPath()));
        //setCadastralDocumentContents(FileHelper.loadContentByPath(getCertificationDataEntity().getCadastralDocument().getPath()));
        //setCadastralDocument(getCertificationDataEntity().getCadastralDocument());
        //}
        if(!ValidationHelper.isNullOrEmpty(getCertificationRequest().getDistraintFormality())
                && !ValidationHelper.isNullOrEmpty(getCertificationRequest().getDistraintFormality().getDocument())) {
            Document document = getCertificationRequest().getDistraintFormality().getDocument();
            setCadastralFileName(FileHelper.getFileName(document.getPath()));
            setCadastralDocumentContents(FileHelper.loadContentByPath(document.getPath()));
            setCadastralDocument(document);
        } else {
            setCadastralFileName(null);
            setCadastralDocumentContents(null);
            setCadastralDocument(null);
        }

        loadAbstractMapDocumentList();

        if (!ValidationHelper.isNullOrEmpty(getCertificationDataEntity().getMapDocument())) {
            setMapFileName(FileHelper.getFileName(getCertificationDataEntity().getMapDocument().getPath()));
            setMapDocumentContents(FileHelper.loadContentByPath(getCertificationDataEntity().getMapDocument().getPath()));
            setMapDocument(getCertificationDataEntity().getMapDocument());
            setMapFileExists(Boolean.TRUE);
        } else {
            setMapFileName(null);
            setMapDocumentContents(null);
            setMapDocument(null);
        }

        if (!ValidationHelper.isNullOrEmpty(getCertificationDataEntity().getSignedCertificationDocument())) {
            setSignedCertificationFileName(FileHelper.getFileName(getCertificationDataEntity().getSignedCertificationDocument().getPath()));
            setSignedCertificationDocumentContents(FileHelper.loadContentByPath(getCertificationDataEntity().getSignedCertificationDocument().getPath()));
            setSignedCertificationDocument(getCertificationDataEntity().getSignedCertificationDocument());
        } else {
            setSignedCertificationFileName(null);
            setSignedCertificationDocumentContents(null);
            setSignedCertificationDocument(null);
        }

        if (!ValidationHelper.isNullOrEmpty(getCertificationRequest().getDocumentsRequest())) {
            List<Document> documents = getCertificationRequest().getDocumentsRequest();
            Document document = documents.stream()
                    .filter(d -> d.getTypeId().equals(DocumentType.REQUEST_REPORT.getId())
                            && !ValidationHelper.isNullOrEmpty(d.getSelectedForEmail()) && d.getSelectedForEmail())
                    .findAny().orElse(null);
            if(!ValidationHelper.isNullOrEmpty(document)) {
                setCertificationProcessedFileName(FileHelper.getFileName(document.getPath()));
                setCertificationProcessedDocumentContents(FileHelper.loadContentByPath(document.getPath()));
                setCertificationProcessedDocument(document);
            }
        } else {
            setCertificationProcessedFileName(null);
            setCertificationProcessedDocumentContents(null);
            setCertificationProcessedDocument(null);
        }

        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", getCertificationRequest().getId()),
                Restrictions.isNotNull("type"),
                Restrictions.or(
                        Restrictions.eq("type", ExtraCostType.CATASTO),
                        Restrictions.eq("type", ExtraCostType.IPOTECARIO),
                        Restrictions.eq("type", ExtraCostType.ANAGRAFICO),
                        Restrictions.eq("type", ExtraCostType.POSTALE)
                )
        });

        for (ExtraCost extraCost : extraCosts) {
            if (extraCost.getType().equals(ExtraCostType.CATASTO))
                setCadastralCost(extraCost.getPrice());
            else if (extraCost.getType().equals(ExtraCostType.IPOTECARIO))
                setMortgageCost(extraCost.getPrice());
            else if (extraCost.getType().equals(ExtraCostType.CATASTO))
                setPersonalCost(extraCost.getPrice());
            else if (extraCost.getType().equals(ExtraCostType.ANAGRAFICO))
                setPersonalCost(extraCost.getPrice());
            else if (extraCost.getType().equals(ExtraCostType.POSTALE))
                setPostalCost(extraCost.getPrice());
        }
    }

    public void saveCertification() {
        String selectedCertificationTab = SessionHelper.get("selectedTranscriptionTab") != null ?
                SessionHelper.get("selectedTranscriptionTab").toString() : "";
        boolean isNew = this.getCertificationDataEntity().isNew();
        setCertificationDataEntity(getTranscriptionAndCertificationHelper().saveCertification(getCertificationRequest(),
                getCertificationDataEntity(), getCadastralCost(), getMortgageCost(), getPersonalCost(), getPostalCost(),
                getCadastralFileName(), getCadastralDocument(), getCadastralDocumentContents(), getMapFileName(),
                getMapDocument(), getMapDocumentContents(), getSignedCertificationFileName(),
                getSignedCertificationDocument(), getSignedCertificationDocumentContents()));
        if (isNew) {
            SessionHelper.put("selectedMainTab" ,"tab2");
            if (ValidationHelper.isNullOrEmpty(getTranscriptionDataEntityId())) {
                if (!ValidationHelper.isNullOrEmpty(getTranscriptionCertificationPage()) && getTranscriptionCertificationPage()) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTIONCERTIFICATION,
                            getCertificationRequest().getId(), null, getCertificationDataEntity().getId());
                } else if (!ValidationHelper.isNullOrEmpty(getCertificationPage()) && getCertificationPage()) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.CERTIFICATION,
                            getCertificationRequest().getId(), null, getCertificationDataEntity().getId());
                }
            } else {
                if (!ValidationHelper.isNullOrEmpty(getTranscriptionCertificationPage()) && getTranscriptionCertificationPage()) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTIONCERTIFICATION,
                            getCertificationRequest().getId(), getTranscriptionDataEntityId(), getCertificationDataEntity().getId());
                } else if (!ValidationHelper.isNullOrEmpty(getCertificationPage()) && getCertificationPage()) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.CERTIFICATION,
                            getCertificationRequest().getId(), getTranscriptionDataEntityId(), getCertificationDataEntity().getId());
                }
            }
        }
        if(StringUtils.isNotBlank(selectedCertificationTab)){
            this.setActiveTabIndex(Integer.parseInt(selectedCertificationTab));
        }
        RequestContext.getCurrentInstance().update("form");
        executeJS("$('.tab').removeClass('selected');");
        executeJS("$('#tab2').addClass('selected')");
    }

    public void handleCadastralFileUpload(FileUploadEvent event) throws PersistenceBeanException {
        setCadastralFileName(event.getFile().getFileName());
        setCadastralDocumentContents(event.getFile().getContents());
    }

    public void downloadCadastralDocument() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralFileName()) && !ValidationHelper.isNullOrEmpty(getCadastralDocumentContents())) {
            FileHelper.sendFile(getCadastralFileName(), getCadastralDocumentContents());
        }
    }

    public void deleteCadastralDocument() {
        setCadastralFileName(null);
        setCadastralDocumentContents(null);
    }
    
    /*public void handleMapFileUpload(FileUploadEvent event) throws PersistenceBeanException {
        setMapFileName(event.getFile().getFileName());
        setMapDocumentContents(event.getFile().getContents());
    }*/

    public void handleMapFileUpload(FileUploadEvent event)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException, IOException {
        setAbstractMapFileName(event.getFile().getFileName());
        setAbstractMapDocumentContents(event.getFile().getContents());
        boolean isNew = this.getCertificationDataEntity().isNew();
        if(isNew) {
            DaoManager.save(getCertificationDataEntity(), true);
        }
        String path = FileHelper.getTranscriptionDocumentSavePath(getCertificationDataEntity().getId(), "map", "certification");
        File filePath = new File(path);
        FileHelper.writeFileToFolder(abstractMapFileName, filePath, abstractMapDocumentContents);
        Document document = new Document();
        document.setTitle(FileHelper.getFileNameWOExtension(abstractMapFileName));
        document.setPath(path + abstractMapFileName);
        document.setTypeId(DocumentType.ESTRATTO_MAPPA.getId());
        document.setRequest(getCertificationRequest());
        document.setDate(new Date());
        DaoManager.save(document, true);
        loadAbstractMapDocumentList();
    }

    private void loadAbstractMapDocumentList() throws PersistenceBeanException, IllegalAccessException {
        setAbstractMapDocumentList(DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request", getCertificationRequest()),
                Restrictions.eq("typeId", DocumentType.ESTRATTO_MAPPA.getId())
        }));
    }

    public void downloadMapDocument() {
        if (!ValidationHelper.isNullOrEmpty(getMapFileName()) && !ValidationHelper.isNullOrEmpty(getMapDocumentContents())) {
            FileHelper.sendFile(getMapFileName(), getMapDocumentContents());
        }
    }

    public void deleteMapDocument() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        getCertificationDataEntity().setMapDocument(null);
        DaoManager.save(getCertificationDataEntity(), true);
        DaoManager.getSession().clear();
        setMapFileName(null);
        setMapDocumentContents(null);
        setMapDocument(null);
        setMapFileExists(Boolean.FALSE);
        loadCertificationData();
    }

    public void deleteAbstractMapDocument() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException  {
        DaoManager.remove(Document.class, getAbstractMapDocumentId(), true);
        loadAbstractMapDocumentList();
    }

    public void handleSignedCertificationFileUpload(FileUploadEvent event) throws PersistenceBeanException {
        setSignedCertificationFileName(event.getFile().getFileName());
        setSignedCertificationDocumentContents(event.getFile().getContents());
    }

    public void downloadSignedCertificationDocument() {
        if (!ValidationHelper.isNullOrEmpty(getSignedCertificationFileName()) && !ValidationHelper.isNullOrEmpty(getSignedCertificationDocumentContents())) {
            FileHelper.sendFile(getSignedCertificationFileName(), getSignedCertificationDocumentContents());
        }
    }

    public void deleteSignedCertificationDocument() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        setSignedCertificationFileName(null);
        setSignedCertificationDocumentContents(null);
    }

    public final void onTabChange(final TabChangeEvent event) throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException, IOException {
        TabView tv = (TabView) event.getComponent();
        if (!ValidationHelper.isNullOrEmpty(tv)) {
            this.activeTabIndex = tv.getActiveIndex();
        }
        if(this.activeTabIndex == 0){
            setTranscriptionAmount();
        }
        SessionHelper.put("selectedTranscriptionTab", this.getActiveTabIndex());
    }

    public void loadTabs() {
        if((getCertificationPage() != null && getCertificationPage())
                || (getRenewalPage() != null && getRenewalPage()))
            setTranscriptionTab(false);
    }

    public void updateTabs() {
        setTranscriptionTab(!getTranscriptionTab());
    }

    public void mergeFiles() throws IOException, HibernateException, PersistenceBeanException, IllegalAccessException {
        if(!ValidationHelper.isNullOrEmpty(getCertificationRequest())
                && (!ValidationHelper.isNullOrEmpty(getAbstractMapDocumentList())
                || !ValidationHelper.isNullOrEmpty(getSignedCertificationDocument()))) {
            PDFMergerUtility obj = new PDFMergerUtility();
            if(!ValidationHelper.isNullOrEmpty(getCertificationDataEntity().getMapDocument())) {
                document = getCertificationDataEntity().getMapDocument();
                document.setDate(new Date());
                DaoManager.save(document, true);
            } else {
                document = new Document();
                document.setDate(new Date());
                document.setRequest(getCertificationRequest());
                DaoManager.save(document, true);
            }

            fileName = generatePdfName(getCertificationRequest());

            String pathToFile = FileHelper.getTranscriptionDocumentSavePath(getCertificationDataEntity().getId(), "map", "certification") + fileName + ".pdf";
            Path pdfFilePath = Paths.get(pathToFile);
            if (Files.notExists(pdfFilePath.getParent()))
                Files.createDirectories(pdfFilePath.getParent());
            obj.setDestinationFileName(pathToFile);

            if(!ValidationHelper.isNullOrEmpty(getCertificationProcessedDocument())) {
                File file1 = new File(getCertificationProcessedDocument().getPath());
                obj.addSource(file1);
            }

            if(!ValidationHelper.isNullOrEmpty(getAbstractMapDocumentList())) {
                for (Document mapDocument : getAbstractMapDocumentList()) {
                    File file1 = new File(mapDocument.getPath());
                    obj.addSource(file1);
                }
            }

            try {
                obj.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
                document.setPath(pathToFile);
                document.setTitle(fileName);
                document.setTypeId(DocumentType.CERTIFICATION_MERGED.getId());
                DaoManager.save(document, true);
                getCertificationDataEntity().setMapDocument(document);
                DaoManager.save(getCertificationDataEntity(), true);

                setMapFileName(FileHelper.getFileName(getCertificationDataEntity().getMapDocument().getPath()));
                setMapDocumentContents(FileHelper.loadContentByPath(getCertificationDataEntity().getMapDocument().getPath()));
                setMapDocument(getCertificationDataEntity().getMapDocument());
                setMapFileExists(Boolean.TRUE);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void downloadMergeFiles() {
        FileHelper.sendFile(fileName + ".pdf", FileHelper.loadContentByPath(document.getPath()));
    }

    public void handleCertificationProcessedFileUpload(FileUploadEvent event) throws PersistenceBeanException, IOException {
        setCertificationProcessedFileName(event.getFile().getFileName());
        setCertificationProcessedDocumentContents(event.getFile().getContents());

        boolean isNew = this.getCertificationDataEntity().isNew();
        if(isNew) {
            DaoManager.save(getCertificationDataEntity(), true);
        }
        String path = FileHelper.getApplicationProperties().getProperty("requestReportSavePath")
                + File.separator + getCertificationRequest().getId() + File.separator;
        File filePath = new File(path);
        FileHelper.writeFileToFolder(getCertificationProcessedFileName(), filePath, getCertificationProcessedDocumentContents());
        Document document = new Document();
        document.setTitle(FileHelper.getFileNameWOExtension(getCertificationProcessedFileName()));
        document.setPath(path + getCertificationProcessedFileName());
        document.setTypeId(DocumentType.REQUEST_REPORT.getId());
        document.setRequest(getCertificationRequest());
        document.setDate(new Date());
        document.setSelectedForEmail(Boolean.TRUE);
        DaoManager.save(document, true);
    }

    public void downloadCertificationProcessedDocument() {
        if (!ValidationHelper.isNullOrEmpty(getCertificationProcessedFileName()) && !ValidationHelper.isNullOrEmpty(getCertificationProcessedDocumentContents())) {
            FileHelper.sendFile(getCertificationProcessedFileName(), getCertificationProcessedDocumentContents());
        }
    }

    public void deleteCertificationProcessedDocument() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        setCertificationProcessedFileName(null);
        setCertificationProcessedDocumentContents(null);
    }

    public void updateImportF24Anticipated() {
        if(getTranscriptionDataEntity().getAnticipatedF24() != null && getTranscriptionDataEntity().getAnticipatedF24()){
            if(getTranscriptionDataEntity().getImportF24Anticipated() == null){
                Double totalImport = sumOfImport();
                if(totalImport > 0.0)
                    getTranscriptionDataEntity().setImportF24Anticipated(totalImport);
            }
        }else {
            getTranscriptionDataEntity().setImportF24Anticipated(null);
        }

    }

    private String generatePdfName(Request request) throws HibernateException, IllegalAccessException, PersistenceBeanException {
        String separator = "-";
        String spaceVal = "\\s";
        StringJoiner joiner = new StringJoiner(separator);
        String prefix = "";

        if (!ValidationHelper.isNullOrEmpty(request.getDistraintFormality())) {
            StringBuilder sb = new StringBuilder();
            sb.append("Certificazione notarile_");

            List<Subject> subjects = DaoManager.load(Subject.class, new CriteriaAlias[]{
                            new CriteriaAlias("sectionC", "sc", JoinType.INNER_JOIN)},
                    new Criterion[]{Restrictions.eq("sc.formality", request.getDistraintFormality()),
                            Restrictions.eq("sc.sectionCType", SectionCType.CONTRO.getName())});

            if (subjects != null && subjects.size() == 1) {
                Subject s = subjects.get(0);
                if (s.getTypeIsPhysicalPerson()) {
                    sb.append(s.getSurname().toUpperCase());
                } else if (SubjectType.LEGAL_PERSON.getId().equals(s.getTypeId())) {
                    sb.append(s.getBusinessName().toUpperCase());
                }
            } else {
                subjects.stream().forEach(s -> {
                    String fullName = "";
                    if (s.getTypeIsPhysicalPerson()) {
                        fullName = s.getSurname().toUpperCase();
                    } else if (SubjectType.LEGAL_PERSON.getId().equals(s.getTypeId())) {
                        fullName = s.getBusinessName().toUpperCase();
                    }
                    if (!ValidationHelper.isNullOrEmpty(fullName)) {
                        if (sb.length() > 24)
                            sb.append("-");
                        sb.append(fullName);
                    }
                });
            }
            joiner.add(sb.toString());
        } else if (!ValidationHelper.isNullOrEmpty(request.getSubject())) {
            if (request.isPhysicalPerson()) {
                if (!ValidationHelper.isNullOrEmpty(request.getSubject().getSurname())) {
                    joiner.add(request.getSubject().getSurname()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
                if (!ValidationHelper.isNullOrEmpty(request.getSubject().getName())) {
                    joiner.add(request.getSubject().getName()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
                if (!ValidationHelper.isNullOrEmpty(request.getSubject().getBirthCity())) {
                    joiner.add(request.getSubject().getBirthCity().getDescription()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
                if (!ValidationHelper.isNullOrEmpty(request.getSubject().getBirthDate())) {
                    joiner.add(DateTimeHelper.toFormatedString(request.getSubject().getBirthDate(),
                            DateTimeHelper.getXmlDatePattert()));
                }
            } else {
                if (!ValidationHelper.isNullOrEmpty(request.getSubject().getBusinessName())) {
                    joiner.add(request.getSubject().getBusinessName()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
                if (!ValidationHelper.isNullOrEmpty(request.getSubject().getNumberVAT())) {
                    joiner.add(request.getSubject().getNumberVAT()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
            }
        }

        joiner.add("Cons");
        if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())) {
            joiner.add(request.getAggregationLandChargesRegistry().getName()
                    .toUpperCase().replaceAll(spaceVal, separator));
        }

        if (joiner.toString().toUpperCase().startsWith("CON.") || joiner.toString().equalsIgnoreCase("CON")) {
            prefix = "-";
        }
        return prefix + joiner.toString().replaceAll("[^\\w\\s\\-_.]", "");
    }

    public void handleOtherDocumentUpload(FileUploadEvent event) {
        setOtherDocumentContents(event.getFile().getContents());
        setOtherFileNameTemp(event.getFile().getFileName());
    }

    public void saveOtherDocument() throws HibernateException, PersistenceBeanException, IOException, IllegalAccessException {

        if(ValidationHelper.isNullOrEmpty(getOtherDocumentContents()))
            return;

        boolean isNew = this.getCertificationDataEntity().isNew();
        if(isNew) {
            DaoManager.save(getCertificationDataEntity(), true);
        }
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
        document.setTypeId(DocumentType.OTHER.getId());
        document.setNote(getOtherDocumentNote());
        Request request = null;
        if(!ValidationHelper.isNullOrEmpty(getCertificationRequest()))
            request = getCertificationRequest();
        if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest()))
            request = getTranscriptionRequest();
        document.setRequest(request);
        document.setDate(new Date());
        DaoManager.save(document, true);
        setOtherDocumentName(null);
        setOtherDocumentNote(null);
        setOtherFileNameTemp(null);
        setOtherDocumentContents(null);
        loadOtherDocumentList();
    }

    private void loadOtherDocumentList() throws PersistenceBeanException, IllegalAccessException {
        Request request = null;
        if(!ValidationHelper.isNullOrEmpty(getCertificationRequest()))
            request = getCertificationRequest();
        if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest()))
            request = getTranscriptionRequest();
        setOtherDocuments(new ArrayList<>());
        List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request", request),
                Restrictions.eq("typeId", DocumentType.OTHER.getId())});
        if(!ValidationHelper.isNullOrEmpty(documents)) {
            getOtherDocuments().addAll(documents);
        }
    }

    public void deleteOtherDocument() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        DaoManager.remove(Document.class, getOtherDocumentId(), true);
        loadOtherDocumentList();
    }
    
    public void deleteOtherTempDocument() {
    	setOtherDocumentContents(null);
        setOtherFileNameTemp(null);
    }
    
    public void generateCertificationReportSpese(Boolean isPdf) {
		try {
			log.info("Inside generate Certification REPORT SPESE");

			ExcelDataWrapper excelDataWrapper = getTranscriptionAndCertificationHelper()
					.populateExcelDataWrapper(certificationRequest);
			CertificationExpense certificationExpense = new ExcelTemplateHelper()
					.createExpenseReportCertification(excelDataWrapper, certificationRequest);

			InputStream input = null;
	        try {
	        	log.info("Trying to read CERTIFICATION_REPORT_SPESE_TEMPLATE from current dir...");
	        	input = new FileInputStream(new File("./templates/CERTIFICATION_REPORT_SPESE_TEMPLATE.xls"));
	            log.info("CERTIFICATION_REPORT_SPESE_TEMPLATE has been read from current dir...");
	        } catch (Exception e) {
	        	log.info("Trying to read CERTIFICATION_REPORT_SPESE_TEMPLATE by resource stream...");
	        	input = this.getClass().getResourceAsStream("/templates/CERTIFICATION_REPORT_SPESE_TEMPLATE.xls");
	        }
			
			String tmpFileNameSuffix = "REPORT SPESE";
			String tempDir = FileHelper.getLocalTempDir();
			tempDir += File.separator + UUID.randomUUID();
			FileUtils.forceMkdir(new File(tempDir));
			String path = tempDir + File.separator + tmpFileNameSuffix + ".xls";
			try (OutputStream output = new FileOutputStream(path)) {
				Context context = new Context();
				context.putVar("certificationExpense", certificationExpense);
				JxlsHelper.getInstance().processTemplate(input, output, context);
			}

			File file = new File(path);
			if(isPdf) {
				String sofficeCommand = ApplicationSettingsHolder.getInstance()
						.getByKey(ApplicationSettingsKeys.SOFFICE_COMMAND).getValue().trim();

				VisureManageHelper.sendPDFfromXLSFile(file, sofficeCommand, tempDir, path);
				log.info("Certification Pdf REPORT SPESE generated");
			} else {
				VisureManageHelper.sendXLSFile(tmpFileNameSuffix + ".xls", file, tempDir, path);
				log.info("Certification Excel REPORT SPESE generated");
			}
			log.info("Leaving generate Certification REPORT SPESE");
		} catch (Exception e) {
			LogHelper.log(log, e);
		}
	}

    public void preCheckDocument() throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        Request request = (getTranscriptionPage() != null && getTranscriptionPage()) ? getTranscriptionRequest()
                : getCertificationRequest();
        if (ValidationHelper.isNullOrEmpty(request.getMail()) ||
                ValidationHelper.isNullOrEmpty(request.getMail().getClientInvoice())) {
            executeJS("PF('invoiceMissingBillingClientDialogWV').show();");
            return;
        }else if (!ValidationHelper.isNullOrEmpty(request.getMail()) &&
                !ValidationHelper.isNullOrEmpty(request.getMail().getNdg())) {
            List<Invoice> invoices = DaoManager.load(Invoice.class, new Criterion[]{Restrictions.eq("ndg",
                    request.getMail().getNdg())});
            if (!ValidationHelper.isNullOrEmpty(invoices)) {
                String message = ResourcesHelper.getString("invoiceAlreadyCreated");
                message = message + " "+request.getMail().getNdg() + " in data "+invoices.get(0).getDateString();
                setInvoiceAlreadyCreated(message);
                RequestContext.getCurrentInstance().update("@widgetVar(invoiceAlreadyCreatedDialogWV)");
                executeJS("PF('invoiceAlreadyCreatedDialogWV').show();");
                return;
            }
        }
        preCheckDocumentOtherRequests();
    }

    public void preCheckDocumentOtherRequests() throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        Request request = (getTranscriptionPage() != null && getTranscriptionPage()) ? getTranscriptionRequest()
                : getCertificationRequest();
        if (!ValidationHelper.isNullOrEmpty(request.getMail()) &&
                !ValidationHelper.isNullOrEmpty(request.getMail().getNdg())) {
            List<Request> requests = DaoManager.load(Request.class, new Criterion[]{Restrictions.and(Restrictions.eq("ndg", request.getMail().getNdg()),
                    Restrictions.eq("stateId", RequestState.EVADED.getId()),
                    Restrictions.isNotNull("mail"),
                    Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted")),
                    Restrictions.ne("mail", request.getMail()) )});
            List<Request> requestListForInvoice = new ArrayList<>();
            if (!ValidationHelper.isNullOrEmpty(requests)) {
                requestListForInvoice.addAll(requests);
                List<Request> requestListEntity = request.getMail().getRequests().stream()
                        .filter(x -> x.isDeletedRequest() && ValidationHelper.isNullOrEmpty(x.getInvoice())
                                && !ValidationHelper.isNullOrEmpty(x.getStateId())
                                && (RequestState.EVADED.getId().equals(x.getStateId())))
                        .collect(Collectors.toList());
                if (!ValidationHelper.isNullOrEmpty(requestListEntity)) {
                    requestListForInvoice.addAll(requestListEntity);
                }
                if (!ValidationHelper.isNullOrEmpty(requestListForInvoice)) {
                    setOtherRequestsConsideredForInvoice(new ArrayList<>());
                    getOtherRequestsConsideredForInvoice().addAll(requestListForInvoice);
                }
                String message = ResourcesHelper.getString("otherRequestsExistsForInvoice");
                setOtherRequestsExistsForInvoice(message);
                RequestContext.getCurrentInstance().update("@widgetVar(otherRequestsExistsForInvoiceDialogWV)");
                executeJS("PF('otherRequestsExistsForInvoiceDialogWV').show();");
                return;
            }
        }
        openInvoiceDalog(false);
    }

    public void openInvoiceDalog(boolean otherRequests) throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        setRequestsConsideredForInvoice(null);
        if (otherRequests) {
            setRequestsConsideredForInvoice(new ArrayList<>());
            List<Request> otherRequestListForInvoice = getOtherRequestsConsideredForInvoice();
            if (!ValidationHelper.isNullOrEmpty(otherRequestListForInvoice)) {
                otherRequestListForInvoice.stream().forEach(r -> {
                    r.setSelectedForInvoice(true);
                });
                getRequestsConsideredForInvoice().addAll(otherRequestListForInvoice);
            }
        } else {
            setRequestsConsideredForInvoice(new ArrayList<>());
            Request requestForInvoice = getTranscriptionRequest();
            requestForInvoice.setSelectedForInvoice(true);
            getRequestsConsideredForInvoice().add(requestForInvoice);
        }
        executeJS("PF('mailManagerViewRequestsForInvoiceDlg').show();");
    }

    public void createInvoice() throws Exception {
        setMaxInvoiceNumber();
        List<Request> selectedRequestList = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getRequestsConsideredForInvoice())) {
            selectedRequestList = getRequestsConsideredForInvoice().stream()
                    .filter(r -> r.isSelectedForInvoice())
                    .collect(Collectors.toList());
        }
        Request transcriptionRequest = (getTranscriptionPage() != null && getTranscriptionPage()) ? getTranscriptionRequest()
                : getCertificationRequest();
        Invoice invoice = new Invoice();
        if(!ValidationHelper.isNullOrEmpty(transcriptionRequest.getMail()) &&
                !ValidationHelper.isNullOrEmpty(transcriptionRequest.getMail().getClientInvoice()))
            invoice.setClient(transcriptionRequest.getMail().getClientInvoice());
        else
            throw new Exception("Client invoice is null, Can't create invoice");

        setSelectedInvoiceClient(invoice.getClient());
        if(!ValidationHelper.isNullOrEmpty(getSelectedInvoiceClient()))
            setSelectedInvoiceClientId(getSelectedInvoiceClient().getId());
        invoice.setDate(getInvoiceDate());
        invoice.setDate(new Date());
        invoice.setStatus(InvoiceStatus.DRAFT);
        if(!ValidationHelper.isNullOrEmpty(transcriptionRequest.getMail()))
            invoice.setEmailFrom(transcriptionRequest.getMail());
        List<RequestPriceListModel> requestPriceListModels = InvoiceHelper.groupingItemsByTaxRate(selectedRequestList);
        setSelectedInvoiceItems(InvoiceHelper.getInvoiceItems(requestPriceListModels, getCausal(), selectedRequestList));
        setInvoicedRequests(selectedRequestList);
        invoice.setTotalGrossAmount(getTotalGrossAmount());
        setTempInvoice(invoice);
        loadInvoiceDialogData(invoice);
        executeJS("PF('invoiceDialogBillingWV').show();");
        requestPriceListModels.stream().forEach(rp -> {
            log.info(rp.getTaxRate() + "   " + rp.getTotalCost());
            if (ValidationHelper.isNullOrEmpty(rp.getTaxRate())) {
                setInvoiceErrorMessage(ResourcesHelper.getString("nullTaxRateMessage"));
                executeJS("PF('nullTaxRateErrorDialogWV').show();");
                RequestContext.getCurrentInstance().update("nullTaxRateErrorDialog");
            }
        });

        double totalCost = 0d;
        for(Request request: selectedRequestList) {
            if(ValidationHelper.isNullOrEmpty(request.getTotalCost()))
                totalCost += 0d;
            else
                totalCost += Double.parseDouble(request.getTotalCost().replaceAll(",", "."));
            if (!ValidationHelper.isNullOrEmpty(request.getService())) {
                List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", request.getId())});
                Double nationalCost = 0d;
                for (ExtraCost cost : extraCost) {
                    if(ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                        nationalCost = cost.getPrice();
                        break;
                    }
                }
                totalCost += nationalCost;
            }
        }
        BigDecimal tot = BigDecimal.valueOf(totalCost);
        tot = tot.setScale(2, RoundingMode.HALF_UP);
        totalCost = tot.doubleValue();
        log.info("request total cost :: "+totalCost + ", total invoice :: "+getAllTotalLine().doubleValue());
        if(totalCost != getAllTotalLine().doubleValue()) {
            if(!ValidationHelper.isNullOrEmpty(getInvoiceErrorMessage())) {
                setInvoiceErrorMessage(getInvoiceErrorMessage() + "<br /><br />" + ResourcesHelper.getString("invoiceTotalNotMatchMessage"));
            } else {
                setInvoiceErrorMessage(ResourcesHelper.getString("invoiceTotalNotMatchMessage"));
            }
            executeJS("PF('nullTaxRateErrorDialogWV').show();");
            RequestContext.getCurrentInstance().update("nullTaxRateErrorDialog");
        }
    }

    public void setMaxInvoiceNumber() throws HibernateException {
        LocalDate currentdate = LocalDate.now();
        int currentYear = currentdate.getYear();

        Long lastInvoiceNumber = 0l;
        try {
            lastInvoiceNumber = (Long) DaoManager.getMax(Invoice.class, "number",
                    new Criterion[]{
                            Restrictions.and( Restrictions.sqlRestriction("year(this_.date) = "
                                    + DateTimeHelper.getYearOfNow()))
                    });
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        if (lastInvoiceNumber == null){
            ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.NEXT_INVOICE_NUMBER
                    ,null);
            lastInvoiceNumber = 0l;
        }
        String nextNumber = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.NEXT_INVOICE_NUMBER).getValue();
        Long nextInvoiceNumber = 0l;
        if(StringUtils.isNotBlank(nextNumber)) {
            nextInvoiceNumber = Long.parseLong(nextNumber.trim());
        }
        if(nextInvoiceNumber > lastInvoiceNumber)
            lastInvoiceNumber = nextInvoiceNumber;
        else {
            lastInvoiceNumber += 1;
        }
        String invoiceNumber = lastInvoiceNumber + "-" + currentYear + "-FE";
        setInvoiceNumber(invoiceNumber);
        setNumber(lastInvoiceNumber);
    }

    public String getCausal() {
        Request request = (getTranscriptionPage() != null && getTranscriptionPage()) ? getTranscriptionRequest()
                : getCertificationRequest();
        String causal = "";
        WLGInbox mail = request.getMail();
        if (!ValidationHelper.isNullOrEmpty(mail)) {
            String reference = "";
            if (!ValidationHelper.isNullOrEmpty(mail.getReferenceRequest()))
                reference = "Rif. " + mail.getReferenceRequest();
            String ndg = "";
            if (!ValidationHelper.isNullOrEmpty(mail.getNdg()))
                ndg = "NDG: " + mail.getNdg();
            String uffico = "";
            if (!ValidationHelper.isNullOrEmpty(mail.getOffice()) && !ValidationHelper.isNullOrEmpty(mail.getOffice().getDescription()))
                uffico = "UFFICIO: " + mail.getOffice().getDescription();
            String gestore = "";
            if (!ValidationHelper.isNullOrEmpty(mail.getManagers())) {
                List<Client> managers = mail.getManagers();
                String managerNames = "";
                for (Client client : managers) {
                    if (!ValidationHelper.isNullOrEmpty(client.getClientName())) {
                        if (!managerNames.isEmpty())
                            managerNames = managerNames + ", ";
                        managerNames = managerNames + client.getClientName();
                    }
                }
                if (!managerNames.isEmpty())
                    gestore = "GESTORE: " + managerNames;
            }
            String fiduciario = "";
            if (StringUtils.isNotBlank(mail.getFiduciary()))
                fiduciario = "FIDUCIARIO: " + mail.getFiduciary();
            else if (!ValidationHelper.isNullOrEmpty(mail.getClientFiduciary())
                    && !ValidationHelper.isNullOrEmpty(mail.getClientFiduciary().getClientName()))
                fiduciario = "FIDUCIARIO: " + mail.getClientFiduciary().getClientName();
            causal = reference +
                    (!reference.isEmpty() && !ndg.isEmpty() ? " - " : "") +
                    ndg +
                    ((!reference.isEmpty() || !ndg.isEmpty()) && !uffico.isEmpty() ? " - " : "") +
                    uffico +
                    ((!reference.isEmpty() || !ndg.isEmpty() || !uffico.isEmpty()) && !gestore.isEmpty() ? " - " : "") +
                    gestore +
                    ((!reference.isEmpty() || !ndg.isEmpty() || !uffico.isEmpty() || !gestore.isEmpty()) && !fiduciario.isEmpty() ? " - " : "") +
                    fiduciario;
        }
        return causal;
    }

    public Double getTotalGrossAmount() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        Double totalGrossAmount = 0D;
        if (!ValidationHelper.isNullOrEmpty(getGoodsServicesFields())) {
            for (GoodsServicesFieldWrapper wrapper : getGoodsServicesFields()) {
                if (!ValidationHelper.isNullOrEmpty(wrapper.getTotalLine())) {
                    totalGrossAmount += wrapper.getTotalLine();
                    if (!ValidationHelper.isNullOrEmpty(wrapper.getSelectedTaxRateId())) {
                        TaxRate taxrate = DaoManager.get(TaxRate.class, wrapper.getSelectedTaxRateId());
                        if (!ValidationHelper.isNullOrEmpty(taxrate.getPercentage())) {
                            totalGrossAmount += (wrapper.getTotalLine() * (taxrate.getPercentage().doubleValue() / 100));
                        }
                    }
                }
            }
            BigDecimal tot = BigDecimal.valueOf(totalGrossAmount);
            tot = tot.setScale(2, RoundingMode.HALF_UP);
            totalGrossAmount = tot.doubleValue();
        }
        return totalGrossAmount;
    }

    public Double getAllTotalLine() {
        Double total = 0D;
        if (!ValidationHelper.isNullOrEmpty(getGoodsServicesFields())) {
            total = getGoodsServicesFields().stream().collect(
                    Collectors.summingDouble(GoodsServicesFieldWrapper::getTotalLine));
            BigDecimal tot = BigDecimal.valueOf(total);
            tot = tot.setScale(2, RoundingMode.HALF_UP);
            total = tot.doubleValue();
        }
        return total;
    }

    public void loadInvoiceDialogData(Invoice invoiceDb) throws IllegalAccessException, PersistenceBeanException, HibernateException, InstantiationException {
        setInvoiceErrorMessage(null);
        setShowRequestTab(true);
        setActiveTabIndex(0);
        setDeletedGoodsServicesFields(new ArrayList<>());
        if (!ValidationHelper.isNullOrEmpty(invoiceDb) && !ValidationHelper.isNullOrEmpty(invoiceDb.getId()))
            setInvoicedRequests(DaoManager.load(Request.class, new Criterion[]{Restrictions.eq("invoice", invoiceDb)}));
        if (!invoiceDb.isNew()) {
            List<PaymentInvoice> paymentInvoicesList = DaoManager.load(PaymentInvoice.class,
                    new Criterion[]{Restrictions.eq("invoice", invoiceDb)}, new Order[]{
                            Order.desc("date")});
            setPaymentInvoices(paymentInvoicesList);
            double totalImport = 0.0;
            for (PaymentInvoice paymentInvoice : paymentInvoicesList) {
                totalImport = totalImport + paymentInvoice.getPaymentImport().doubleValue();
            }
        }
        List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted")),
                Restrictions.or(
                        Restrictions.eq("brexa", Boolean.FALSE),
                        Restrictions.isNull("brexa"))});
        setInvoiceClients(ComboboxHelper.fillList(clients.stream()
                .filter(c -> (
                                (ValidationHelper.isNullOrEmpty(c.getManager()) || !c.getManager()) &&
                                        (ValidationHelper.isNullOrEmpty(c.getFiduciary()) || !c.getFiduciary())
                        )
                ).sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList()), Boolean.TRUE));
        if(!ValidationHelper.isNullOrEmpty(getSelectedInvoiceClient()) &&
            ValidationHelper.isNullOrEmpty(getInvoiceClients()
                    .stream()
                    .filter(c -> c.getValue().toString().equals(getSelectedInvoiceClient().getStrId()))
                    .findFirst().orElse(null))){
            getInvoiceClients().add(new SelectItem(getSelectedInvoiceClient().getId(), getSelectedInvoiceClient().toString()));
        }
        //setMaxInvoiceNumber();
        docTypes = new ArrayList<>();
        docTypes.add(new SelectItem("FE", "FATTURA"));
        setDocumentType("FE");
        competence = new Date();
        setVatCollectabilityList(ComboboxHelper.fillList(VatCollectability.class,
                false, false));
        if(!ValidationHelper.isNullOrEmpty(invoiceDb.getClient()))
            paymentTypes = ComboboxHelper.fillList(invoiceDb.getClient().getPaymentTypeList(), Boolean.FALSE);
        setGoodsServicesFields(new ArrayList<>());
        setInvoiceDate(invoiceDb.getDate());
        setSelectedInvoice(invoiceDb);
        if(!ValidationHelper.isNullOrEmpty(invoiceDb.getClient())){
            setSelectedClientId(invoiceDb.getClient().getId());
            if (invoiceDb.getClient().getSplitPayment() != null && invoiceDb.getClient().getSplitPayment())
                setVatCollectabilityId(VatCollectability.SPLIT_PAYMENT.getId());
        }
        setSelectedInvoiceClient(invoiceDb.getClient());
        if(!ValidationHelper.isNullOrEmpty(invoiceDb.getClient()))
            setSelectedInvoiceClientId(invoiceDb.getClient().getId());
        if (!ValidationHelper.isNullOrEmpty(invoiceDb.getVatCollectability()))
            setVatCollectabilityId(invoiceDb.getVatCollectability().getId());
        if (!ValidationHelper.isNullOrEmpty(invoiceDb.getPaymentType()))
            setSelectedPaymentTypeId(invoiceDb.getPaymentType().getId());
        if (!ValidationHelper.isNullOrEmpty(invoiceDb.getNotes()))
            setInvoiceNote(invoiceDb.getNotes());
        if (invoiceDb.getStatus() !=  null && invoiceDb.getStatus().equals(InvoiceStatus.DELIVERED)) {
            setInvoiceSentStatus(true);
        }
        if (!ValidationHelper.isNullOrEmpty(invoiceDb.getNumber()))
            setNumber(invoiceDb.getNumber());
        if (!ValidationHelper.isNullOrEmpty(invoiceDb.getInvoiceNumber()))
            setInvoiceNumber(invoiceDb.getInvoiceNumber());
        if (!invoiceDb.isNew()) {
            setSelectedInvoiceItems(DaoManager.load(InvoiceItem.class,
                    new Criterion[]{Restrictions.eq("invoice", invoiceDb)}));
        }
        int counter = 1;
        List<GoodsServicesFieldWrapper> wrapperList = new ArrayList<>();

        for (InvoiceItem invoiceItem : getSelectedInvoiceItems()) {

            GoodsServicesFieldWrapper wrapper = createGoodsServicesFieldWrapper();
            wrapper.setCounter(counter);
            if (invoiceItem.getId() != null) {
                wrapper.setInvoiceItemId(invoiceItem.getId());
            }
            wrapper.setInvoiceTotalCost(invoiceItem.getInvoiceTotalCost());
            wrapper.setSelectedTaxRateId(invoiceItem.getTaxRate().getId());
            if(!ValidationHelper.isNullOrEmpty(invoiceItem.getAmount()))
                wrapper.setInvoiceItemAmount(invoiceItem.getAmount());
            double totalcost = !(ValidationHelper.isNullOrEmpty(invoiceItem.getInvoiceTotalCost())) ? invoiceItem.getInvoiceTotalCost().doubleValue() : 0.0;
            double amount = !(ValidationHelper.isNullOrEmpty(invoiceItem.getAmount())) ? invoiceItem.getAmount().doubleValue() : 0.0;
            double totalLine;
            if (amount != 0.0) {
                totalLine = totalcost * amount;
            } else {
                totalLine = totalcost;
            }
            wrapper.setTotalLine(totalLine);
            if (!ValidationHelper.isNullOrEmpty(invoiceItem.getDescription()))
                wrapper.setDescription(invoiceItem.getDescription());
            wrapperList.add(wrapper);
            counter = counter + 1;
        }
        setGoodsServicesFields(wrapperList);
        loadExpenseDocument(getSelectedInvoice());
        loadDraftEmail();
        setInvoiceNote(getCausal());
        if (!ValidationHelper.isNullOrEmpty(invoiceDb.getNotes()))
            setInvoiceNote(invoiceDb.getNotes());

        // }
        if (!ValidationHelper.isNullOrEmpty(invoiceDb.getInvoiceNumber())) {
            String invoiceNo = "Fattura: " + invoiceDb.getInvoiceNumber();
            String invoiceDate = "";
            if (!ValidationHelper.isNullOrEmpty(invoiceDb.getDateString()))
                invoiceDate = invoiceDb.getDateString();
            String reference = "";
            if (!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getMail().getReferenceRequest()))
                reference = "- Rif. " + getTranscriptionRequest().getMail().getReferenceRequest() + " ";
            String ndg = "";
            if (!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getMail().getNdg()))
                ndg = "NDG: " + getTranscriptionRequest().getMail().getNdg();
            String emailSubject = invoiceNo + " " +
                    invoiceDate + " " +
                    reference +
                    (!reference.isEmpty() && !ndg.isEmpty() ? " - " : "") +
                    ndg;
            setEmailSubject(emailSubject);
            if (!ValidationHelper.isNullOrEmpty(invoiceDb.getEmail()) && !ValidationHelper.isNullOrEmpty(invoiceDb.getEmail().getEmailSubject()))
                setEmailSubject(invoiceDb.getEmail().getEmailSubject());
        }
        setClientProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
        getClientProvinces().add(new SelectItem(Province.FOREIGN_COUNTRY_ID, Province.FOREIGN_COUNTRY));
        setClientAddressCities(ComboboxHelper.fillList(new ArrayList<City>(), true));
        getInvoiceClientData(getSelectedInvoiceClient());
        if (!ValidationHelper.isNullOrEmpty(invoiceDb) && !ValidationHelper.isNullOrEmpty(invoiceDb.getStatus()) && invoiceDb.getStatus().equals(InvoiceStatus.DRAFT))
            setInvoiceSaveAsDraft(Boolean.TRUE);
    }

    private void getInvoiceClientData(Client client) throws HibernateException, IllegalAccessException, PersistenceBeanException {
        setClientAddressStreet(client != null ? client.getAddressStreet() : "");
        setClientAddressPostalCode(client != null ? client.getAddressPostalCode() : "");

        if (!ValidationHelper.isNullOrEmpty(client) && !ValidationHelper.isNullOrEmpty(client.getAddressProvinceId())) {
            setClientAddressProvinceId(client.getAddressProvinceId().getId());
            handleAddressProvinceChange();
        }
        if (!ValidationHelper.isNullOrEmpty(client) && !ValidationHelper.isNullOrEmpty(client.getAddressCityId())) {
            setClientAddressCityId(client.getAddressCityId().getId());
        }
        setClientNumberVAT(client != null ? client.getNumberVAT() : "");
        setClientFiscalCode(client != null ? client.getFiscalCode() : "");
        setClientMailPEC(client != null ? client.getMailPEC() : "");
        setClientAddressSDI(client != null ? client.getAddressSDI() : "");
    }

    public void handleAddressProvinceChange() throws HibernateException, PersistenceBeanException, IllegalAccessException {
        setClientAddressCities(ComboboxHelper.fillList(City.class, Order.asc("description"),
                new Criterion[]{Restrictions.eq("province.id", getClientAddressProvinceId()),
                        Restrictions.eq("external", Boolean.TRUE)}));
    }

    public void loadDraftEmail() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        Invoice invoice =  DaoManager.get(Invoice.class, new Criterion[]{
                Restrictions.eq("number", getNumber()),
                Restrictions.and( Restrictions.sqlRestriction("year(this_.date) = "
                        + DateTimeHelper.getYearOfNow()))
        });
        if(!ValidationHelper.isNullOrEmpty(invoice)) {
            if (!ValidationHelper.isNullOrEmpty(invoice.getEmail())) {
                WLGInbox inbox = DaoManager.get(WLGInbox.class, invoice.getEmail().getId());

                if (!ValidationHelper.isNullOrEmpty(inbox.getEmailFrom()))
                    setEmailFrom(inbox.getEmailFrom());
                if (!ValidationHelper.isNullOrEmpty(inbox.getEmailTo()))
                    setEmailTo(inbox.getEmailTo());
                if (!ValidationHelper.isNullOrEmpty(inbox.getEmailCC()))
                    setEmailCC(inbox.getEmailCC());

                if (!ValidationHelper.isNullOrEmpty(getEmailFrom()))
                    setSendFrom(Arrays.asList(getEmailFrom().split(",")));

                if (!ValidationHelper.isNullOrEmpty(getEmailTo()))
                    setSendTo(Arrays.asList(getEmailTo().split(",")));

                if (!ValidationHelper.isNullOrEmpty(getEmailCC()))
                    setSendCC(Arrays.asList(getEmailCC().split(",")));

                if (!ValidationHelper.isNullOrEmpty(inbox.getEmailBodyToEditor()))
                    setEmailBodyToEditor(inbox.getEmailBodyToEditor());

                if (!ValidationHelper.isNullOrEmpty(inbox.getEmailSubject()))
                    setEmailSubject(inbox.getEmailSubject());
            } else {
                if (!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom())) {

                    WLGInbox inbox = DaoManager.get(WLGInbox.class, invoice.getEmailFrom().getId());
                    List<Client> managers = inbox.getManagers();
                    List<ClientEmail> allEmailList = new ArrayList<>();
                    CollectionUtils.emptyIfNull(managers).stream().forEach(manager -> {
                        if (!ValidationHelper.isNullOrEmpty(manager.getEmails()))
                            allEmailList.addAll(manager.getEmails());
                    });
                    sendTo = new LinkedList<>();
                    CollectionUtils.emptyIfNull(allEmailList).stream().forEach(email -> {
                        if (!ValidationHelper.isNullOrEmpty(email.getEmail()))
                            sendTo.addAll(MailHelper.parseMailAddress(email.getEmail()));
                    });
                }
                setEmailBodyToEditor(invoiceHelper.createInvoiceEmailDefaultBody(invoice));
            }
        }
    }

    private GoodsServicesFieldWrapper createGoodsServicesFieldWrapper() throws IllegalAccessException, PersistenceBeanException {
        GoodsServicesFieldWrapper wrapper = new GoodsServicesFieldWrapper();
        ums = new ArrayList<>();
        ums.add(new SelectItem("pz", "PZ"));
        wrapper.setUms(ums);
        wrapper.setInvoiceItemAmount(1.0d);
        wrapper.setVatAmounts(ComboboxHelper.fillList(TaxRate.class, Order.asc("description"), new CriteriaAlias[]{}, new Criterion[]{
                Restrictions.eq("use", Boolean.TRUE)
        }, true, false));
        wrapper.setTotalLine(0D);
        return wrapper;
    }

    public void loadInvoiceDataAfterError() throws IllegalAccessException, HibernateException, InstantiationException, PersistenceBeanException {
        Invoice invoice = getTempInvoice();
        setSelectedInvoiceClient(invoice.getClient());
        if(!ValidationHelper.isNullOrEmpty(getSelectedInvoiceClient()))
            setSelectedInvoiceClientId(getSelectedInvoiceClient().getId());
        loadInvoiceDialogData(invoice);
        executeJS("PF('invoiceDialogBillingWV').show();");
        RequestContext.getCurrentInstance().update("invoiceDialogBilling");
    }

    public Double getTotalVat() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        Double total = 0D;
        if (!ValidationHelper.isNullOrEmpty(getGoodsServicesFields())) {
            for (GoodsServicesFieldWrapper wrapper : getGoodsServicesFields()) {
                if (!ValidationHelper.isNullOrEmpty(wrapper.getTotalLine())) {
                    if (!ValidationHelper.isNullOrEmpty(wrapper.getSelectedTaxRateId())) {
                        TaxRate taxrate = DaoManager.get(TaxRate.class, wrapper.getSelectedTaxRateId());
                        if (!ValidationHelper.isNullOrEmpty(taxrate.getPercentage())) {
                            total += wrapper.getTotalLine().doubleValue() * (taxrate.getPercentage().doubleValue() / 100);
                        }
                    }
                }
            }
            BigDecimal tot = BigDecimal.valueOf(total);
            tot = tot.setScale(2, RoundingMode.HALF_UP);
            total = tot.doubleValue();
        }
        return total;
    }

    public void saveInvoicePayment() throws HibernateException, PersistenceBeanException, IllegalAccessException, InstantiationException {
        PaymentInvoice paymentInvoice = new PaymentInvoice();
        if(!ValidationHelper.isNullOrEmpty(getSelectedPaymentInvoice())) {
            paymentInvoice = getSelectedPaymentInvoice();
        }
        paymentInvoice.setPaymentImport(getPaymentAmount());
        paymentInvoice.setDate(getPaymentDate());
        //paymentInvoice.setDescription(getPaymentDescription());
        if(!ValidationHelper.isNullOrEmpty(getSelectedPaymentOutcome())) {
            PaymentType paymentType = DaoManager.get(PaymentType.class, getSelectedPaymentOutcome());
            paymentInvoice.setDescription(paymentType.getDescription());
        }
        paymentInvoice.setInvoice(getSelectedInvoice());
        DaoManager.save(paymentInvoice, true);
        List<PaymentInvoice> paymentInvoicesList = DaoManager.load(PaymentInvoice.class, new Criterion[]{Restrictions.eq("invoice", getSelectedInvoice())}, new Order[]{
                Order.asc("date")});
        setPaymentInvoices(paymentInvoicesList);
    }

    public void createNewGoodsServicesFields() throws IllegalAccessException, PersistenceBeanException {
        GoodsServicesFieldWrapper wrapper = invoiceHelper.createGoodsServicesFieldWrapper();
        int size = getGoodsServicesFields().size();
        wrapper.setCounter(size + 1);
        getGoodsServicesFields().add(wrapper);
    }

    public void deleteGoodService(GoodsServicesFieldWrapper goodsServicesFieldWrapper) {
        if(getGoodsServicesFields() != null && goodsServicesFieldWrapper != null){
            if(getDeletedGoodsServicesFields() == null)
                setDeletedGoodsServicesFields(new ArrayList<>());
            getDeletedGoodsServicesFields().add(goodsServicesFieldWrapper);
            getGoodsServicesFields().remove(goodsServicesFieldWrapper);
        }
    }

    public void saveInvoiceInDraft() throws PersistenceBeanException, IllegalAccessException {
        cleanValidation();
        if (ValidationHelper.isNullOrEmpty(getInvoiceDate())) {
            addRequiredFieldException("form:date");
            setValidationFailed(true);
        }

        if (ValidationHelper.isNullOrEmpty(getSelectedInvoiceClientId())) {
            addRequiredFieldException("form:invoiceClient");
            setValidationFailed(true);
        }

        if (ValidationHelper.isNullOrEmpty(getSelectedPaymentTypeId())) {
            addRequiredFieldException("form:paymentType");
            setValidationFailed(true);
        }

        setInvoiceErrorMessage(ResourcesHelper.getString("invalidDataMsg"));

        if (ValidationHelper.isNullOrEmpty(getClientAddressSDI())) {
            setInvoiceErrorMessage(ResourcesHelper.getString("noSDIAddressMessage"));
            setValidationFailed(true);
        }

        if (!ValidationHelper.isNullOrEmpty(getClientNumberVAT())) {
            String vatNumber = getClientNumberVAT().trim();
            if (getClientNumberVAT().startsWith("IT")) {
                vatNumber = getClientNumberVAT().trim().substring(2);
            }
            if (vatNumber.length() != 11) {
                setInvoiceErrorMessage(ResourcesHelper.getString("invalidClientNumberVAT"));
                setValidationFailed(true);
            }
        } else {
            setInvoiceErrorMessage(ResourcesHelper.getString("invalidClientNumberVAT"));
            setValidationFailed(true);
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedInvoice())
                && !ValidationHelper.isNullOrEmpty(getSelectedInvoice().getId())) {
            if (DaoManager.getCount(Invoice.class, "id", new Criterion[]{
                    Restrictions.eq("number", getNumber()),
                    Restrictions.ne("id", getSelectedInvoice().isNew() ? 0L : getSelectedInvoice().getId()),
                    Restrictions.and( Restrictions.sqlRestriction("year(this_.date) = "
                            + DateTimeHelper.getYearOfNow()))
            }) > 0) {
                setInvoiceErrorMessage(ResourcesHelper.getValidation("invoiceWarning"));
                setValidationFailed(true);
            }
        }
        boolean isSpecialCharacters = false;
        for (GoodsServicesFieldWrapper goodsServicesFieldWrapper : getGoodsServicesFields()) {
            if (ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getInvoiceTotalCost())) {
                setValidationFailed(true);
            }

            if (ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getSelectedTaxRateId())) {
                setValidationFailed(true);
            }
            if (!getValidationFailed() && !isSpecialCharacters
                    && StringUtils.isNotBlank(goodsServicesFieldWrapper.getDescription())
                    && checkSpecialCharacters(goodsServicesFieldWrapper.getDescription())) {
                isSpecialCharacters = true;
                String description = goodsServicesFieldWrapper.getDescription();
                goodsServicesFieldWrapper.setDescription(description.replaceAll(InvoiceHelper.specialCharacters, ""));
            }
        }

        if (getValidationFailed()) {
            executeJS("PF('invoiceErrorDialogWV').show();");
            RequestContext.getCurrentInstance().update("invoiceErrorDialog");
            return;
        }

        if (!isSpecialCharacters && StringUtils.isNotBlank(getInvoiceNote()) && checkSpecialCharacters(getInvoiceNote())) {
            isSpecialCharacters = true;
            String invoiceNote = getInvoiceNote();
            setInvoiceNote(invoiceNote.replaceAll(InvoiceHelper.specialCharacters, ""));
        }

        if (isSpecialCharacters) {
            executeJS("PF('invoiceDialogBillingWV').hide();");
            executeJS("PF('invoiceSpecialCharactersDialogWV').show();");
            RequestContext.getCurrentInstance().update("invoiceSpecialCharactersDialog");
        } else
            saveInvoiceDialog();
    }

    public void saveInvoiceDialog() {
        try {
            Invoice invoice = saveInvoice(InvoiceStatus.DRAFT, true);
            saveExpenseDocument(invoice);
            setInvoiceSaveAsDraft(Boolean.TRUE);
            loadInvoiceDialogData(invoice);
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
        }
    }


    public Invoice saveInvoice(InvoiceStatus invoiceStatus, Boolean saveInvoiceNumber) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        Request request = (getTranscriptionPage() != null && getTranscriptionPage()) ? getTranscriptionRequest()
                : getCertificationRequest();
        WLGInbox mail = request.getMail();
        if (getSelectedInvoice() == null) {
            setSelectedInvoice(new Invoice());
        }
        getSelectedInvoice().setDate(getInvoiceDate());
        if(!ValidationHelper.isNullOrEmpty(getSelectedInvoiceClient())){
            getSelectedInvoice().setClient(getSelectedInvoiceClient());
        }else
            getSelectedInvoice().setClient(null);

        getSelectedInvoice().setDocumentType(getDocumentType());
        if (!ValidationHelper.isNullOrEmpty(getSelectedPaymentTypeId()))
            getSelectedInvoice().setPaymentType(DaoManager.get(PaymentType.class, getSelectedPaymentTypeId()));

        if (!ValidationHelper.isNullOrEmpty(getVatCollectabilityId()))
            getSelectedInvoice().setVatCollectability(VatCollectability.getById(getVatCollectabilityId()));
        getSelectedInvoice().setNotes(getInvoiceNote().replaceAll("&", "E"));
        getSelectedInvoice().setStatus(invoiceStatus);

        getSelectedInvoice().setTotalGrossAmount(getTotalGrossAmount());
        if(!ValidationHelper.isNullOrEmpty(mail)){
            if (!ValidationHelper.isNullOrEmpty(mail.getNdg()))
                getSelectedInvoice().setNdg(mail.getNdg());
            if (!ValidationHelper.isNullOrEmpty(mail.getOffice()))
                getSelectedInvoice().setOffice(mail.getOffice());
            if (!ValidationHelper.isNullOrEmpty(mail.getReferenceRequest()))
                getSelectedInvoice().setPractice(mail.getReferenceRequest());
        }
        DaoManager.save(getSelectedInvoice(), true);
        if (saveInvoiceNumber) {
            getSelectedInvoice().setNumber(getNumber());
            getSelectedInvoice().setInvoiceNumber(getInvoiceNumber());
        }
        if (!ValidationHelper.isNullOrEmpty(mail) && !ValidationHelper.isNullOrEmpty(mail.getManagers())) {
            getSelectedInvoice().setManagers(new ArrayList<>(mail.getManagers()));
        }
        DaoManager.save(getSelectedInvoice(), true);
        setMaxInvoiceNumber();
        for (GoodsServicesFieldWrapper goodsServicesFieldWrapper : getGoodsServicesFields()) {
            if (!ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getInvoiceItemId())) {
                InvoiceItem invoiceItem = getSelectedInvoiceItems()
                        .stream().filter(inv ->
                                (inv.getId() != null
                                        && inv.getId().equals(goodsServicesFieldWrapper.getInvoiceItemId()))
                        ).findFirst().get();
                invoiceItem.setAmount(goodsServicesFieldWrapper.getInvoiceItemAmount());
                invoiceItem.setTaxRate(DaoManager.get(TaxRate.class, goodsServicesFieldWrapper.getSelectedTaxRateId()));
                invoiceItem.setDescription(goodsServicesFieldWrapper.getDescription().replaceAll("&","E"));
                invoiceItem.setInvoiceTotalCost(goodsServicesFieldWrapper.getInvoiceTotalCost());
                invoiceItem.setInvoice(getSelectedInvoice());
                DaoManager.save(invoiceItem, true);
            } else {
                InvoiceItem invoiceItem = new InvoiceItem();
                invoiceItem.setAmount(goodsServicesFieldWrapper.getInvoiceItemAmount());
                invoiceItem.setTaxRate(DaoManager.get(TaxRate.class, goodsServicesFieldWrapper.getSelectedTaxRateId()));
                invoiceItem.setDescription(goodsServicesFieldWrapper.getDescription().replaceAll("&","E"));
                invoiceItem.setInvoiceTotalCost(goodsServicesFieldWrapper.getInvoiceTotalCost());
                invoiceItem.setInvoice(getSelectedInvoice());
                invoiceItem.setSubject(!ValidationHelper.isNullOrEmpty(request.getSubject())
                        ? request.getSubject().toString() : null);
                DaoManager.save(invoiceItem, true);
            }
        }
        for (GoodsServicesFieldWrapper goodsServicesFieldWrapper : getDeletedGoodsServicesFields()) {
            if(goodsServicesFieldWrapper.getInvoiceItemId() != null){
                InvoiceItem invoiceItem = DaoManager.get(InvoiceItem.class,goodsServicesFieldWrapper.getInvoiceItemId());
                if(!ValidationHelper.isNullOrEmpty(invoiceItem)){
                    DaoManager.remove(invoiceItem, true);
                }
            }
        }
        CollectionUtils.emptyIfNull(getInvoicedRequests()).stream().forEach(r -> {
            try {
                r.setInvoice(getSelectedInvoice());
                DaoManager.save(r, true);
            } catch (PersistenceBeanException e) {
                log.error("error in saving invoice in request after creating invoice ", e);
            }
        });
        if (!ValidationHelper.isNullOrEmpty(getClientAddressStreet()))
            getSelectedInvoiceClient().setAddressStreet(getClientAddressStreet());
        if (!ValidationHelper.isNullOrEmpty(getClientAddressPostalCode()))
            getSelectedInvoiceClient().setAddressPostalCode(getClientAddressPostalCode());
        if (!ValidationHelper.isNullOrEmpty(getClientAddressProvinceId()))
            getSelectedInvoiceClient().setAddressProvinceId(DaoManager.get(Province.class, getClientAddressProvinceId()));
        if (!ValidationHelper.isNullOrEmpty(getClientAddressCityId()))
            getSelectedInvoiceClient().setAddressCityId(DaoManager.get(City.class, getClientAddressCityId()));
        if (!ValidationHelper.isNullOrEmpty(getClientNumberVAT()))
            getSelectedInvoiceClient().setNumberVAT(getClientNumberVAT());
        if (!ValidationHelper.isNullOrEmpty(getClientFiscalCode()))
            getSelectedInvoiceClient().setFiscalCode(getClientFiscalCode());
        if (!ValidationHelper.isNullOrEmpty(getClientMailPEC()))
            getSelectedInvoiceClient().setMailPEC(getClientMailPEC());
        if (!ValidationHelper.isNullOrEmpty(getClientAddressSDI()))
            getSelectedInvoiceClient().setAddressSDI(getClientAddressSDI());
        DaoManager.save(getSelectedInvoiceClient(), true);
        return getSelectedInvoice();
    }

    private boolean checkSpecialCharacters(String inputString){
        String foundCharacters = "";
        Matcher hasSpecial = InvoiceHelper.specialCharactersPattern.matcher(inputString);
        for (int i = 0; i < inputString.length(); i++) {
            if (hasSpecial.find()) {
                if(StringUtils.isBlank(foundCharacters))
                    foundCharacters = hasSpecial.group(0);
                else
                    foundCharacters += ", " + hasSpecial.group(0);
            }
        }
        if(StringUtils.isNotBlank(foundCharacters)){
            setInvoiceErrorMessage(String.format(
                    ResourcesHelper.getString("invoiceSpecialCharacters"),
                    foundCharacters));
            return true;
        }
        setInvoiceErrorMessage(null);
        return false;
    }

    public void sendInvoice() {
        cleanValidation();
        if (ValidationHelper.isNullOrEmpty(getInvoiceDate())) {
            addRequiredFieldException("form:date");
            setValidationFailed(true);
        }

        if (ValidationHelper.isNullOrEmpty(getSelectedPaymentTypeId())) {
            addRequiredFieldException("form:paymentType");
            setValidationFailed(true);
        }

        for (GoodsServicesFieldWrapper goodsServicesFieldWrapper : getGoodsServicesFields()) {
            if (ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getInvoiceTotalCost())) {
                setValidationFailed(true);
            }

            if (ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getSelectedTaxRateId())) {
                setValidationFailed(true);
            }
        }

        setInvoiceErrorMessage(ResourcesHelper.getString("invalidDataMsg"));

        if (ValidationHelper.isNullOrEmpty(getClientAddressSDI())) {
            setInvoiceErrorMessage(ResourcesHelper.getString("noSDIAddressMessage"));
            setValidationFailed(true);
        }

        if (!ValidationHelper.isNullOrEmpty(getClientNumberVAT())) {
            String vatNumber = getClientNumberVAT().trim();
            if(getClientNumberVAT().startsWith("IT")) {
                vatNumber = getClientNumberVAT().trim().substring(2);
            }
            if(vatNumber.length() != 11) {
                setInvoiceErrorMessage(ResourcesHelper.getString("invalidClientNumberVAT"));
                setValidationFailed(true);
            }
        } else {
            setInvoiceErrorMessage(ResourcesHelper.getString("invalidClientNumberVAT"));
            setValidationFailed(true);
        }

        if (getValidationFailed()) {
            executeJS("PF('invoiceErrorDialogWV').show();");
            RequestContext.getCurrentInstance().update("invoiceErrorDialog");
            return;
        }

        try {

            saveInvoiceInDraft();
            Invoice invoice =  DaoManager.get(Invoice.class, new Criterion[]{
                    Restrictions.eq("number", getNumber()),
                    Restrictions.and( Restrictions.sqlRestriction("year(this_.date) = "
                            + DateTimeHelper.getYearOfNow()))
            });
            List<InvoiceItem> invoiceItems = DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoice)});
            FatturaAPI fatturaAPI = new FatturaAPI();
            String xmlData = fatturaAPI.getDataForXML(invoice, invoiceItems);

            FatturaAPIResponse fatturaAPIResponse = fatturaAPI.callFatturaAPI(xmlData, log);

            if (fatturaAPIResponse != null && fatturaAPIResponse.getReturnCode() == 0) {
                invoice.setStatus(InvoiceStatus.DELIVERED);
                DaoManager.save(invoice, true);

                List<Request> selectedRequestList = new ArrayList<>();
                if (!ValidationHelper.isNullOrEmpty(getInvoicedRequests())) {
                    selectedRequestList = getInvoicedRequests().stream().filter(r -> !ValidationHelper.isNullOrEmpty(r.getInvoice()))
                            .collect(Collectors.toList());
                }
                CollectionUtils.emptyIfNull(selectedRequestList).stream().forEach(r -> {
                    try {
                        r.setStateId(RequestState.SENT_TO_SDI.getId());
                        DaoManager.save(r, true);
                    } catch (PersistenceBeanException e) {
                        log.error("error in saving request after sending invoice ", e);
                    }
                });

                setInvoiceSentStatus(true);
                /*executeJS("PF('invoiceDialogWV').hide();");*/
            } else {
                setApiError(ResourcesHelper.getString("sendInvoiceErrorMsg"));
                if (fatturaAPIResponse != null
                        && !ValidationHelper.isNullOrEmpty(fatturaAPIResponse.getDescription())) {

                    if (fatturaAPIResponse.getDescription().contains("already exists")) {
                        setApiError(ResourcesHelper.getString("sendInvoiceDuplicateMsg"));
                    }
                }
                RequestContext.getCurrentInstance().update("sendInvoiceErrorDialog");
                executeJS("PF('sendInvoiceErrorDialogWV').show();");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
            setApiError(ResourcesHelper.getString("sendInvoiceErrorMsg"));
            executeJS("PF('sendInvoiceErrorDialogWV').show();");
            return;
        }
        executeJS("PF('invoiceDialogBillingWV').hide();");
        redierctToBilling();
    }

    public void redierctToBilling() {
        RedirectHelper.goTo(PageTypes.BILLING_LIST);
    }

    public void openInvoiceDialog() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        Request request = (getTranscriptionPage() != null && getTranscriptionPage()) ? getTranscriptionRequest()
                : getCertificationRequest();
        if (!ValidationHelper.isNullOrEmpty(request)
                && !ValidationHelper.isNullOrEmpty(request.getInvoice())) {
            Invoice invoice = DaoManager.get(Invoice.class, request.getInvoice().getId());
            loadInvoiceDialogData(invoice);
            executeJS("PF('invoiceDialogBillingWV').show();");
        }
    }

    public void onItemSelectInvoiceClient() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        Client selectedClient = DaoManager.get(Client.class, getSelectedInvoiceClientId());
        setSelectedInvoiceClient(selectedClient);
        if (selectedClient.getSplitPayment() != null && selectedClient.getSplitPayment())
            setVatCollectabilityId(VatCollectability.SPLIT_PAYMENT.getId());
        else
            setVatCollectabilityId(null);
        if (selectedClient.getPaymentTypeList() != null && !selectedClient.getPaymentTypeList().isEmpty()) {
            setPaymentTypes(ComboboxHelper.fillList(selectedClient.getPaymentTypeList(), false));
        } else
            setPaymentTypes(ComboboxHelper.fillList(new ArrayList<>(), false));
        getInvoiceClientData(selectedClient);
    }
    
    
    public void handleExpenseFileUpload(FileUploadEvent event) throws PersistenceBeanException, IOException {
        setExpenseFileName(event.getFile().getFileName());
        setExpenseFileContents(event.getFile().getContents());
    }
    
    public void saveExpenseDocument(Invoice invoice) throws HibernateException, PersistenceBeanException, IllegalAccessException, InstantiationException {
    	if(!ValidationHelper.isNullOrEmpty(getExpenseFileName()) && getExpenseFileContents() != null) {
    		Document document = new Document();
    		Document documentDb = DaoManager.get(Document.class, new Criterion[] {
                    Restrictions.eq("invoiceNumber", invoice.getId()), 
                    Restrictions.eq("typeId", DocumentType.OTHER.getId())});
    		if(!ValidationHelper.isNullOrEmpty(documentDb)) {
    			document = documentDb;
    		}
            document.setInvoiceNumber(invoice.getId());
            StringBuilder sb = new StringBuilder();

            sb.append(FileHelper.getDocumentSavePath());
            sb.append(DateTimeHelper.ToFilePathString(new Date()));
            sb.append(getCurrentUser().getId());
            sb.append("\\");

            File filePath = new File(sb.toString());
            String fileName = getExpenseFileName();
            
            try {
				FileHelper.writeFileToFolder(fileName, filePath, getExpenseFileContents());
			} catch (IOException e) {
				e.printStackTrace();
			}
            document.setTitle(FileHelper.getFileNameWOExtension(fileName));
            document.setPath(sb + fileName);
            document.setTypeId(DocumentType.OTHER.getId());
            document.setDate(new Date());
            DaoManager.save(document, true);
    	}
    }
    
    public void loadExpenseDocument(Invoice invoice) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
    	Document expenseDocument = DaoManager.get(Document.class, new Criterion[] {
                Restrictions.eq("invoiceNumber", invoice.getId()), 
                Restrictions.eq("typeId", DocumentType.OTHER.getId())});
    	if (!ValidationHelper.isNullOrEmpty(expenseDocument)) {
            setExpenseFileName(FileHelper.getFileName(expenseDocument.getPath()));
            setExpenseFileContents(FileHelper.loadContentByPath(expenseDocument.getPath()));
            setExpenseDocument(expenseDocument);
        } else {
            setExpenseFileName(null);
            setExpenseFileContents(null);
            setExpenseDocument(null);
        }
    }
    
    public void deleteExpenseDocument() throws HibernateException, PersistenceBeanException {
    	if(!ValidationHelper.isNullOrEmpty(getExpenseDocument()) && !getExpenseDocument().isNew()) {
    		DaoManager.remove(getExpenseDocument(), true);
    	}
    	setExpenseFileName(null);
        setExpenseFileContents(null);
        setExpenseDocument(null);
    }

    public void downloadInvoicePdf() {
        try {
            String refrequest = "";
            WLGInbox baseMail = getTranscriptionRequest().getMail();
            if (!ValidationHelper.isNullOrEmpty(baseMail)) {
                List<Request> filteredRequests = DaoManager.load(Request.class,
                        new Criterion[]{
                                Restrictions.eq("invoice", getSelectedInvoice()),
                                Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                                        Restrictions.isNull("isDeleted"))
                        });

                if (!ValidationHelper.isNullOrEmpty(filteredRequests)) {
                    setInvoicedRequests(filteredRequests);
                }
                refrequest = baseMail.getReferenceRequest();
            }
            String landscapePDF = "";
            String tempDir  = FileHelper.getLocalTempDir() + File.separator + UUID.randomUUID();
            Date currentDate = new Date();
            String fileName = "Richieste_Invoice_" + DateTimeHelper.toFileDateWithMinutes(currentDate);
            if (!ValidationHelper.isNullOrEmpty(getInvoicedRequests())) {
                byte[] baos = getXlsBytes(refrequest, getInvoicedRequests().get(0));
                if (!ValidationHelper.isNullOrEmpty(baos)) {
                    FileUtils.forceMkdir(new File(tempDir));
                    File filePath = new File(tempDir);
                    String excelFile = FileHelper.writeFileToFolder(fileName + ".xls", filePath, baos);
                    String sofficeCommand =
                            ApplicationSettingsHolder.getInstance().getByKey(
                                    ApplicationSettingsKeys.SOFFICE_COMMAND).getValue().trim();
                    Process p = Runtime.getRuntime().exec(new String[] { sofficeCommand, "--headless",
                            "--convert-to", "pdf","--outdir", tempDir, excelFile });
                    p.waitFor();
                    String excelPdf = tempDir + File.separator + fileName + ".pdf";
                    PDDocument srcDoc = PDDocument.load(new File(excelPdf));
                    for (PDPage pdPage : srcDoc.getDocumentCatalog().getPages()) {
                        pdPage.setRotation(270);
                    }
                    landscapePDF = tempDir + File.separator + fileName + "_landscape.pdf";
                    srcDoc.save(landscapePDF);
                    srcDoc.close();
                    FileHelper.delete(excelFile);
                    FileHelper.delete(excelPdf);
                }
            }
            Invoice invoice =  DaoManager.get(Invoice.class, new Criterion[]{
                    Restrictions.eq("number", getNumber()),
                    Restrictions.and( Restrictions.sqlRestriction("year(this_.date) = "
                            + DateTimeHelper.getYearOfNow()))
            });
            String tempPdf = invoiceDialogBean.prepareInvoicePdf(DocumentType.COURTESY_INVOICE, invoice, tempDir);
            String sb = MailHelper.getDestinationPath() +
                    DateTimeHelper.ToFilePathString(new Date());

            fileName =  "FE-" + getNumber() + " " + invoice.getClient().getClientName();
            String filePathStr = sb + File.separator + fileName + ".pdf";
            Path pdfFilePath = Paths.get(filePathStr);
            if(Files.notExists(pdfFilePath.getParent()))
                Files.createDirectories(pdfFilePath.getParent());
            PDFMergerUtility obj = new PDFMergerUtility();
            obj.setDestinationFileName(filePathStr);
            File file1 = new File(tempPdf);
            obj.addSource(file1);
            if(!ValidationHelper.isNullOrEmpty(landscapePDF)){
                File file2 = new File(landscapePDF);
                obj.addSource(file2);
            }
            obj.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
            FileHelper.delete(tempDir);
            byte[] fileContent = FileHelper.loadContentByPath(filePathStr);
            if (fileContent != null) {
                InputStream stream = new ByteArrayInputStream(fileContent);
                invoicePDFFile = new DefaultStreamedContent(stream, FileHelper.getFileExtension(filePathStr),
                        fileName.toUpperCase() + ".pdf");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private byte[] getXlsBytes(String refrequest, Request invoiceRequest) {
        byte[] excelFile = null;
        try {
            ExcelDataWrapper excelDataWrapper = new ExcelDataWrapper();
            excelDataWrapper.setNdg(getTranscriptionRequest().getMail().getNdg());
            Document document = DaoManager.get(Document.class, new Criterion[]{
                    Restrictions.eq("mail.id", getTranscriptionRequest().getMail().getId())});

            if (ValidationHelper.isNullOrEmpty(document)) {
                document = new Document();
                document.setMail(getTranscriptionRequest().getMail());
                document.setTypeId(DocumentType.INVOICE_REPORT.getId());
                document.setReportNumber(SaveRequestDocumentsHelper.getLastInvoiceNumber() + 1);
            }
            excelDataWrapper.setReportn(document.getReportNumber());
            excelDataWrapper.setReferenceRequest(refrequest);

            if (!ValidationHelper.isNullOrEmpty(invoiceRequest)
                    && !ValidationHelper.isNullOrEmpty(invoiceRequest.getInvoice())) {
                excelDataWrapper.setInvoiceNumber(invoiceRequest.getInvoice().getInvoiceNumber());
                excelDataWrapper.setData((invoiceRequest.getInvoice().getDate() == null ?
                        DateTimeHelper.getNow() : invoiceRequest.getInvoice().getDate()));
            }

            if (!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getMail().getClientInvoice())) {
                excelDataWrapper.setClientInvoice(DaoManager.get(Client.class, getTranscriptionRequest().getMail().getClientInvoice().getId()));
            }

            if (!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getMail().getManagers())) {
                excelDataWrapper.setManagers(getTranscriptionRequest().getMail().getManagers());
            }

            excelDataWrapper.setFiduciary(getTranscriptionRequest().getMail().getFiduciary());
            if (!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getMail().getClientFiduciary())) {
                excelDataWrapper.setClientFiduciary(DaoManager.get(Client.class, getTranscriptionRequest().getMail().getClientFiduciary().getId()));
            }

            if (!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getMail().getOffice())) {
                excelDataWrapper.setOffice(getTranscriptionRequest().getMail().getOffice().getDescription());
            }

            if(!ValidationHelper.isNullOrEmpty(document.getNote())) {
                excelDataWrapper.setDocumentNote(document.getNote());
            }
            List<Request> filteredRequests =
                    emptyIfNull(getInvoicedRequests())
                            .stream()
                            .filter(r -> r.isDeletedRequest() &&
                                    (ValidationHelper.isNullOrEmpty(r.getService()) ||
                                            ((ValidationHelper.isNullOrEmpty(r.getService().getManageTranscription()) ||
                                                    !r.getService().getManageTranscription()) &&
                                                    (ValidationHelper.isNullOrEmpty(r.getService().getManageCertification()) ||
                                                            !r.getService().getManageTranscription()))
                                    )
                            )
                            .collect(Collectors.toList());
            List<Request> transcriptionRequests = getInvoicedRequests().stream()
                    .filter(r -> r.isDeletedRequest() && !ValidationHelper.isNullOrEmpty(r.getService())
                            && ((!ValidationHelper.isNullOrEmpty(r.getService().getManageTranscription())
                            && r.getService().getManageTranscription()) ||
                            (!ValidationHelper.isNullOrEmpty(r.getService().getManageCertification())
                                    && r.getService().getManageCertification())))
                    .collect(Collectors.toList());

            excelFile = new CreateExcelRequestsReportHelper(true)
                    .convertMailUserDataToExcel(filteredRequests, transcriptionRequests, document, excelDataWrapper);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return excelFile;
    }
}
