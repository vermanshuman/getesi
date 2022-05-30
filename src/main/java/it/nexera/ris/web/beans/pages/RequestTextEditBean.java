package it.nexera.ris.web.beans.pages;

import it.nexera.ris.api.FatturaAPI;
import it.nexera.ris.api.FatturaAPIResponse;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.omi.OMIHelper;
import it.nexera.ris.common.helpers.tableGenerator.CertificazioneTableGenerator;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.view.FormalityView;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.base.AccessBean;
import it.nexera.ris.web.beans.wrappers.logic.RelationshipGroupingWrapper;
import it.nexera.ris.web.beans.wrappers.logic.TemplateEntity;
import it.nexera.ris.web.beans.wrappers.logic.editInTable.*;
import it.nexera.ris.web.common.EntityLazyListModel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.xml.serialize.XMLSerializer;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.jsoup.Jsoup;
import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ManagedBean
@ViewScoped
public class RequestTextEditBean extends EntityEditPageBean<RequestPrint> {

    private Request examRequest;

    private Long requestId;

    private Long pdfZoomValue;

    private Long selectedTemplateId;

    private String editText;

    private List<SelectItem> templates;

    private LazyDataModel<Request> lazyModel;

    private List<EstateSituationEditInTableWrapper> estateSituations;

    private List<EstateSituationEditInTableWrapper> otherEstateSituations;

    private List<FormalityEditInTableWrapper> estateSituationFormalities;

    private Boolean showEstateTable;

    private List<SelectItem> categories;

    private List<SelectItem> estateFormalityTypes;

    private Comment comment;

    private RequestTextEditCommentType currentComment;

    private Long selectedId;

    private String quote1;

    private String quote2;

    private String requestCostForced;

    private String requestNumberActUpdate;

    private PropertyTypeEnum propertyType;

    private List<SelectItem> propertyTypeList;

    private PropertyEditInTableWrapper currentProperty;

    private List<Document> requestDocuments;

    private List<RequestConservatory> requestConservatoryList;

    private Date conservatoryMinDate;

    private Date conservatoryMaxDate;

    private CostManipulationHelper costManipulationHelper;

    private String typeFormalityNotConfigureMessage;

    private Long lastAppliedTemplate;

    private RelationshipEditInTableWrapper editRelationship;

    private RelationshipEditInTableWrapper deleteRelationship;

    private String numberActDifference;

    private static final Long MODEL_ID_CERTIFICAZIONE = 4L;
    private static final Long REAL_ESTATE_TEMPLATE_ID = 10L;

    private List<Subject> subjectsToSelect;

    private Subject selectedSubjectToGenerate;

    private Boolean calledByApplicaButton;

    private Date requestEndDate;

    private Date requestEndDateMax;

    private String datafromPropertyText;

    private int estateSituationId;

    private String requestCommentCertification;

    private Long previousStateId;

    private Boolean dateConfimed;

    private Boolean hideExtraCost = Boolean.FALSE;

    private List<Document> otherDocuments;

    private List<String> estateFormalitySalesMessage;

    private Boolean showSalesSection;

    private List<SalesEstateSituationEditTableWrapper> salesOtherEstateSituations;

    @Getter
    @Setter
    private Long selectedFormalityId;

    @Getter
    @Setter
    private boolean formalityDetailsRendered;

    @Getter
    @Setter
    private List<SalesEstateSituationEditTableWrapper> gravamiEstateSituations;

    @Getter
    @Setter
    private List<Document> requestNonSaleDocuments;

    @Getter
    @Setter
    private List<Document> requestSaleDocuments;

    private boolean multipleCreate;

    // private MenuModel topMenuModel;

//    private int activeMenuTabNum;

    private List<InputCard> inputCardList;

    private String invoiceNumber;

    private Double invoiceItemAmount;

   // private Double invoiceItemVat;

    private Double invoiceTotalCost;

    private List<SelectItem> vatAmounts;

    private List<SelectItem> docTypes;

    private Date competence;

    private List<SelectItem> ums;

    private Long vatCollectabilityId;

    private List<SelectItem> vatCollectabilityList;

    private List<SelectItem> paymentTypes;

    private Long selectedPaymentTypeId;

    private Date invoiceDate;

    private boolean invoiceSentStatus;

    private String invoiceNote;

    private String documentType;

    private String apiError;

    private Boolean billinRequest;

    private Boolean showRequestCost = Boolean.TRUE;

    private Long selectedTaxRateId;

    @Getter
    @Setter
    private int activeTabIndex;

    @Getter
    @Setter
    private List<PaymentInvoice> paymentInvoices;

    @Getter
    @Setter
    private Double amountToBeCollected;

    @Getter
    @Setter
    private Double totalPayments;

    @Getter
    @Setter
    private boolean showRegime;

    @Getter
    @Setter
    private List<SelectItem> regimes;

    @Getter
    @Setter
    private Long selectedRegime;


    private PropertyEditInTableWrapper propertyEditInTableWrapper;

    private EstateSituationEditInTableWrapper currentEstateSituation;

    private List<RelationshipEditInTableWrapper> estateSituationRelationshipList;

    private String estateQuote1;

    private String estateQuote2;

    private PropertyTypeEnum estatePropertyType;

    private Long estateSelectedRegime;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        setActiveTabIndex(0);
        setShowSalesSection(Boolean.FALSE);
        String parameter = getRequestParameter(RedirectHelper.ID_PARAMETER);
        setEntityId(null);
        setShowEstateTable(false);
        fillTemplates();
        fillZoom();
        filterTableFromPanel();
        if (!ValidationHelper.isNullOrEmpty(parameter)) {
            Long requestId = Long.parseLong(parameter);
            setRequestId(requestId);
            setExamRequest(DaoManager.get(Request.class, requestId));
            List<RequestPrint> requestPrints = DaoManager.load(RequestPrint.class,
                    new CriteriaAlias[]{new CriteriaAlias("request", "rq", JoinType.INNER_JOIN)},
                    new Criterion[]{Restrictions.eq("rq.id", requestId)});
            if (!ValidationHelper.isNullOrEmpty(requestPrints)) {
                this.setEntityId(requestPrints.get(0).getId());
                this.setEditText(requestPrints.get(0).getRequest().getRequestPrint().getBodyContent());
                if (getEntity().getTemplate() != null) {
                    this.setSelectedTemplateId(getEntity().getTemplate().getId());
                }
            }
        }
        if ((ValidationHelper.isNullOrEmpty(getEntity().getNeedValidateCadastral())
                || getEntity().getNeedValidateCadastral()) && !ValidationHelper.isNullOrEmpty(getExamRequest()) &&
                !EstateSituationHelper.isValidFormalityCadastral(getExamRequest().getId())) {
            addException(ResourcesHelper.getString("requestTextFormalityValidation"), true);
        }
        setCategories(ComboboxHelper.fillList(CadastralCategory.class));
        setEstateFormalityTypes(ComboboxHelper.fillList(EstateFormalityType.class, false));
        setPropertyTypeList(ComboboxHelper.fillList(PropertyTypeEnum.class, false, false));

        setCostManipulationHelper(new CostManipulationHelper());
        getCostManipulationHelper().setMortgageTypeList(ComboboxHelper.fillList(MortgageType.class, false, false));

        if (!ValidationHelper.isNullOrEmpty(getExamRequest())) {
            setConservatoryMaxDate(getExamRequest().getCreateDate());
        }
        Calendar c = Calendar.getInstance();
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getCreateDate())) {
            c.setTime(getExamRequest().getCreateDate());
            c.add(Calendar.DATE, -15);
            setConservatoryMinDate(c.getTime());
        }
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality())) {
            Date presentationDate = getExamRequest().getDistraintFormality().getPresentationDate();
            c.setTime(getExamRequest().getDistraintFormality().getPresentationDate());
            c.add(Calendar.DATE, 60);
            setRequestEndDateMax(c.getTime());
        }

        setComment(new Comment());

        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getCostForced())) {
            this.setRequestCostForced(String.valueOf(getExamRequest().getCostForced()));
        } else if (!ValidationHelper.isNullOrEmpty(getExamRequest().getTotalCost())) {
            this.setRequestCostForced(getExamRequest().getTotalCost());
        }

        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getNumberActUpdate())) {
            this.setRequestNumberActUpdate(String.valueOf(getExamRequest().getNumberActUpdate().intValue()));
        }

        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getClient()) &&
                !ValidationHelper.isNullOrEmpty(getExamRequest().getClient().getCostOutput())) {
            this.getCostManipulationHelper().setCostOutput(getExamRequest().getClient().getCostOutput());
        } else {
            this.getCostManipulationHelper().setCostOutput(false);
        }

        setRequestEndDate(getExamRequest().getEndDate());
        setRequestCommentCertification(getExamRequest().getCommentCertification());

        updateConservatoryList();

      //  generateMenuModel();
        setMaxInvoiceNumber();
        if(!ValidationHelper.isNullOrEmpty(getExamRequest().getTotalCostDouble()))
            setInvoiceTotalCost(Double.parseDouble(getExamRequest().getTotalCostDouble()));
        setVatAmounts(ComboboxHelper.fillList(TaxRate.class, Order.asc("description"), new CriteriaAlias[]{}, new Criterion[]{
                Restrictions.eq("use", Boolean.TRUE)
        }, true, false));

        docTypes = new ArrayList<>();
        docTypes.add(new SelectItem("FE", "FATTURA"));
        setDocumentType("FE");
        competence = new Date();

        ums = new ArrayList<SelectItem>();
        ums.add(new SelectItem("pz", "pz"));
        setVatCollectabilityList(ComboboxHelper.fillList(VatCollectability.class,
                false, false));
        paymentTypes = ComboboxHelper.fillList(PaymentType.class);
        if(ValidationHelper.isNullOrEmpty(getInvoiceItemAmount())){
            setInvoiceItemAmount(1.0D);
        }
        if(!ValidationHelper.isNullOrEmpty(getExamRequest())
                && !ValidationHelper.isNullOrEmpty(getExamRequest().getInvoice())){
            Invoice invoice = DaoManager.get(Invoice.class, getExamRequest().getInvoice().getId());
            String year = DateTimeHelper.toFormatedString(invoice.getDate(), DateTimeHelper.getXmlSecondDatePattertYear());
            setInvoiceNumber(invoice.getId() + "-" + year + "-FE");
            setInvoiceDate(invoice.getDate());
            setInvoiceNote(invoice.getNotes());
            if(!ValidationHelper.isNullOrEmpty(invoice.getVatCollectability()))
                setVatCollectabilityId(invoice.getVatCollectability().getId());
            setSelectedPaymentTypeId(invoice.getPaymentType().getId());
            List<InvoiceItem> invoiceItems = DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoice)});
            for(InvoiceItem invoiceItem : invoiceItems) {
                setInvoiceItemAmount(invoiceItem.getAmount());
                if(!ValidationHelper.isNullOrEmpty(invoiceItem.getTaxRate()))
                    setSelectedTaxRateId(invoiceItem.getTaxRate().getId());
              //  setInvoiceItemVat(invoiceItem.getVat());
            }
        }
        if(getExamRequest().getStateId().equals(RequestState.SENT_TO_SDI.getId()))
            setInvoiceSentStatus(true);

        setBillinRequest(AccessBean.canViewPage(PageTypes.BILLING_LIST));

    }

    public void onErrorClose() throws PersistenceBeanException {
        cleanValidation();
        getEntity().setNeedValidateCadastral(false);
        getEntity().setRequest(getExamRequest());
        DaoManager.save(getEntity(), true);
        getExamRequest().setRequestPrint(getEntity());
        DaoManager.save(getExamRequest(), true);
    }

    public void filterTableFromPanel() {
        setLazyModel(new EntityLazyListModel<>(Request.class, new Order[]{}));
    }

    public void print() throws PersistenceBeanException, IllegalAccessException {
        if (getEntity().isNew() && getEntity().getRequest() == null) {
            getEntity().setRequest(getExamRequest());
        }
        GeneralFunctionsHelper.showReport(getEntity().getRequest(), getSelectedTemplateId(),
                getCurrentUser(), false, getEditText(), DaoManager.getSession());
    }

    public void printSpecialPdf() throws PersistenceBeanException, IllegalAccessException {
        if (getEntity().isNew() && getEntity().getRequest() == null) {
            getEntity().setRequest(getExamRequest());
        }

        DaoManager.refresh(getEntity().getRequest());
        DaoManager.save(getEntity(), true);
        getExamRequest().setRequestPrint(getEntity());
        DaoManager.save(getExamRequest(), true);

        log.info("before fillRequestDocumentList");
        fillRequestDocumentList();
        log.info("after fillRequestDocumentList");

        log.info("before fillNonSaleDocumentList");
        fillNonSaleDocumentList();
        log.info("after fillNonSaleDocumentList");

        log.info("before fillSaleDocumentList");
        fillRequestDocumentSaleList();
        log.info("after fillSaleDocumentList");

        log.info("before fillOtherDocumentList");
        fillOtherDocumentList();
        log.info("after fillOtherDocumentList");
    }

    public void executeRequest() throws PersistenceBeanException, IllegalAccessException {
        if (getExamRequest() == null) {
            return;
        }
        boolean isPresent = false;

        if(ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality()) &&
                ValidationHelper.isNullOrEmpty(getExamRequest().getTranscriptionActId())) {
            isPresent = getExamRequest().getSituationEstateLocations().stream()
                    .map(EstateSituation::getPropertyList).flatMap(List::stream)
                    .filter(p -> p.getCategory() != null)
                    .filter(p -> checkCadastralCode(p.getCategory().getCode()))
                    .filter(p -> PropertyEntityHelper.getEstimateOMIRequestText(p).isEmpty())
                    .anyMatch(p -> PropertyEntityHelper.getEstimateLastCommercialValueRequestText(p).isEmpty());
        }

        if (isPresent) {
            executeJS("PF('categoryNotPresentWV').show();");
        } else {
            log.info("before Evadi richiesta open");
            printSpecialPdf();
            RequestContext.getCurrentInstance().update("requestDocumentTable");
            RequestContext.getCurrentInstance().update("requestDocumentNonSaleTable");
            RequestContext.getCurrentInstance().update("requestDocumentSaleTable");
            RequestContext.getCurrentInstance().update("otherDocumentTable");
            executeJS("PF('requestDocumentDlg').show();");
            setPreviousStateId(getExamRequest().getStateId());
            getExamRequest().setStateId(RequestState.TO_BE_SENT.getId());
            log.info("after Evadi richiesta open");
        }
    }

    private boolean checkCadastralCode(String code) {
        if (!ValidationHelper.isNullOrEmpty(code)) {
            Pattern p = Pattern.compile("(A[0-9]+)|(C[0-9]+)");
            Matcher matcher = p.matcher(code);
            return matcher.find();
        }
        return false;
    }

    private void fillRequestDocumentList() throws PersistenceBeanException, IllegalAccessException {
        RequestOutputTypes type = null;
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getService())) {
            type = getExamRequest().getService().getRequestOutputType();
        }else if(!ValidationHelper.isNullOrEmpty(getExamRequest().getMultipleServices())) {
            boolean isOnlyFile = Boolean.FALSE;
            boolean isOnlyEditor = Boolean.FALSE;
            boolean isAll = Boolean.FALSE;
            for(Service service : getExamRequest().getMultipleServices()) {
                if(!ValidationHelper.isNullOrEmpty(service.getRequestOutputType())) {
                    if(!isOnlyEditor && service.getRequestOutputType() == RequestOutputTypes.ONLY_EDITOR) {
                        isOnlyEditor = Boolean.TRUE;
                    }else if(!isOnlyFile && service.getRequestOutputType() == RequestOutputTypes.ONLY_FILE) {
                        isOnlyFile = Boolean.TRUE;
                    }else  if(!isAll && service.getRequestOutputType() == RequestOutputTypes.ALL) {
                        isAll = Boolean.TRUE;
                    }
                }
            }
            if(isAll || (isOnlyFile && isOnlyEditor)) {
                type = RequestOutputTypes.ALL;
            }else if(isOnlyFile) {
                type = RequestOutputTypes.ONLY_FILE;
            }else if(isOnlyEditor) {
                type = RequestOutputTypes.ONLY_EDITOR;
            }
        }else {
            type = RequestOutputTypes.ALL;
        }


        List<Document> requestDocuments = new ArrayList<>();

        if (type == RequestOutputTypes.ALL || type == RequestOutputTypes.ONLY_EDITOR || type == RequestOutputTypes.ONLY_FILE) {
            String fileName = generatePdfName();
            GeneralFunctionsHelper.saveReport(getEntity().getRequest(), getSelectedTemplateId(),
                    getCurrentUser(), false, getEditText(), fileName, DaoManager.getSession());

            List<Document> documentListToView = EstateSituationHelper.getDocuments(type, getExamRequest(), null);
            documentListToView.removeIf(d -> !ValidationHelper.isNullOrEmpty(d.getTypeId())
                    && d.getTypeId().equals(DocumentType.FORMALITY.getId()));

            if(!ValidationHelper.isNullOrEmpty(documentListToView)) {
                for (Document document : documentListToView) {
                    if (Objects.equals(document.getTypeId(), DocumentType.OTHER.getId())
                            || Objects.equals(document.getTypeId(), DocumentType.REQUEST_REPORT.getId())) {
                        document.setSelectedForDialogList(true);
                    } else {
                        document.setSelectedForDialogList(false);
                    }
                }
            }
            setRequestDocuments(documentListToView);
        }else if(!ValidationHelper.isNullOrEmpty(getEntity().getRequest().getTranscriptionActId())
                && type == RequestOutputTypes.XML) {

            String fileName = generateXMLFileName();
            List<Formality> forcedFormalities =  getEntity().getRequest().getFormalityForcedList();
            for(Formality forcedFormality : forcedFormalities) {
                if(ValidationHelper.isNullOrEmpty(forcedFormality.getDocument())) {
                    String pdfBody = FormalityHelper.getPdfBody(getEntity().getRequest().getTranscriptionActId());
                    Document requestDocument = GeneralFunctionsHelper.saveReport(getEntity().getRequest(), getSelectedTemplateId(),
                            getCurrentUser(), false, pdfBody, fileName, DaoManager.getSession(), DocumentType.FORMALITY.getId());

                    if (!Hibernate.isInitialized(requestDocument.getFormality())) {
                        requestDocument.setFormality(DaoManager.load(Formality.class, new Criterion[]{
                                Restrictions.eq("document.id", requestDocument.getId())
                        }));
                    }
                    forcedFormality.setDocument(requestDocument);
                    DaoManager.save(forcedFormality,true);
                }
            }
            org.jsoup.nodes.Document doc = Jsoup.parse(getEditText(), "UTF-8");
            try {
                String content = doc.text().trim().replaceAll("[^\\x00-\\x7F]", "");

                if(!ValidationHelper.isNullOrEmpty(content)) {
                    String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
                    String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.DTD).getValue();
                    System.setProperty("user.dir", new File(path).getParent());
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    InputSource is = new InputSource(new StringReader(content));
                    org.w3c.dom.Document document  = db.parse(is);
                    org.apache.xml.serialize.OutputFormat format = new org.apache.xml.serialize.OutputFormat(document);
                    format.setLineWidth(65);
                    format.setIndenting(true);
                    format.setIndent(2);
                    Writer out = new StringWriter();
                    XMLSerializer serializer = new XMLSerializer(out, format);
                    serializer.serialize(document);
                    String pathToFile = FileHelper.writeFileToFolder(fileName + ".xml",
                            new File(FileHelper.getApplicationProperties().getProperty("requestReportSavePath")
                                    + File.separator + getEntity().getRequest().getId()),
                            out.toString().getBytes());
                    Document xmlDocument = null;
                    List<Document> documentList = DaoManager.load(Document.class, new Criterion[]{
                            Restrictions.eq("typeId", DocumentType.REQUEST_REPORT.getId()),
                            Restrictions.eq("request.id", getEntity().getRequest().getId()),
                            Restrictions.eq("path", pathToFile),
                    });
                    System.setProperty("user.dir", currentPath);
                    if (!ValidationHelper.isNullOrEmpty(documentList)) {
                        xmlDocument = documentList.get(0);
                    }

                    if (xmlDocument == null)
                        xmlDocument = new Document();

                    xmlDocument.setTitle(fileName);
                    xmlDocument.setPath(pathToFile);
                    xmlDocument.setTypeId(DocumentType.REQUEST_REPORT.getId());
                    xmlDocument.setDate(new Date());
                    xmlDocument.setRequest(getEntity().getRequest());

                    DaoManager.save(xmlDocument,true);

                    DaoManager.refresh(getEntity().getRequest().getTranscriptionActId());
                    getEntity().getRequest().getTranscriptionActId().setDocument(xmlDocument);
                    DaoManager.save(getEntity().getRequest().getTranscriptionActId(),true);

                }
            } catch (IOException e) {
                LogHelper.log(log, e);
            } catch (ParserConfigurationException e) {
                LogHelper.log(log, e);
            } catch (SAXException e) {
                LogHelper.log(log, e);
            }
            for(Document requestDocument : requestDocuments) {
                DaoManager.refresh(getExamRequest());
                for (Formality formality : requestDocument.getFormality()) {
                    if (!getExamRequest().getFormalityPdfList().contains(formality)) {
                        getExamRequest().getFormalityPdfList().add(formality);
                    }
                }
                DaoManager.save(getExamRequest(), true);
            }

            List<Criterion> restrictions = new ArrayList<>();
            restrictions.add(Restrictions.eq("request.id", getExamRequest().getId()));
            List<Document> documentList = DaoManager.load(Document.class, restrictions.toArray(new Criterion[0]));
            documentList.forEach(document -> document.setSelectedForEmail(true));
            setRequestDocuments(documentList);
        }
    }

    private void fillNonSaleDocumentList() throws PersistenceBeanException, IllegalAccessException {
        RequestOutputTypes type = null;
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getService())) {
            type = getExamRequest().getService().getRequestOutputType();
        }else if(!ValidationHelper.isNullOrEmpty(getExamRequest().getMultipleServices())) {
            boolean isOnlyFile = Boolean.FALSE;
            boolean isOnlyEditor = Boolean.FALSE;
            boolean isAll = Boolean.FALSE;
            for(Service service : getExamRequest().getMultipleServices()) {
                if(!ValidationHelper.isNullOrEmpty(service.getRequestOutputType())) {
                    if(!isOnlyEditor && service.getRequestOutputType() == RequestOutputTypes.ONLY_EDITOR) {
                        isOnlyEditor = Boolean.TRUE;
                    }else if(!isOnlyFile && service.getRequestOutputType() == RequestOutputTypes.ONLY_FILE) {
                        isOnlyFile = Boolean.TRUE;
                    }else  if(!isAll && service.getRequestOutputType() == RequestOutputTypes.ALL) {
                        isAll = Boolean.TRUE;
                    }
                }
            }
            if(isAll || (isOnlyFile && isOnlyEditor)) {
                type = RequestOutputTypes.ALL;
            }else if(isOnlyFile) {
                type = RequestOutputTypes.ONLY_FILE;
            }else if(isOnlyEditor) {
                type = RequestOutputTypes.ONLY_EDITOR;
            }
        }else {
            type = RequestOutputTypes.ALL;
        }

        if (type == RequestOutputTypes.ALL || type == RequestOutputTypes.ONLY_EDITOR
                || type == RequestOutputTypes.ONLY_FILE) {
            String fileName = generatePdfName();
            GeneralFunctionsHelper.saveReport(getEntity().getRequest(), getSelectedTemplateId(),
                    getCurrentUser(), false, getEditText(), fileName, DaoManager.getSession());

            List<Document> documentListToView = EstateSituationHelper.getDocuments(type, getExamRequest(), Boolean.FALSE);
            if(!ValidationHelper.isNullOrEmpty(documentListToView)) {
                documentListToView.removeIf(d -> !ValidationHelper.isNullOrEmpty(d.getTypeId())
                        && !d.getTypeId().equals(DocumentType.FORMALITY.getId()));
            }
            if (!ValidationHelper.isNullOrEmpty(getExamRequest().getClient()) &&
                    !ValidationHelper.isNullOrEmpty(getExamRequest().getClient().getSendFormality()) &&
                    getExamRequest().getClient().getSendFormality()) {
                documentListToView.forEach(d -> d.setSelectedForDialogList(true));
            }
            setRequestNonSaleDocuments(documentListToView);
        }

    }

    private void fillRequestDocumentSaleList() throws PersistenceBeanException, IllegalAccessException {
        RequestOutputTypes type = null;
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getService())) {
            type = getExamRequest().getService().getRequestOutputType();
        }else if(!ValidationHelper.isNullOrEmpty(getExamRequest().getMultipleServices())) {
            boolean isOnlyFile = Boolean.FALSE;
            boolean isOnlyEditor = Boolean.FALSE;
            boolean isAll = Boolean.FALSE;
            for(Service service : getExamRequest().getMultipleServices()) {
                if(!ValidationHelper.isNullOrEmpty(service.getRequestOutputType())) {
                    if(!isOnlyEditor && service.getRequestOutputType() == RequestOutputTypes.ONLY_EDITOR) {
                        isOnlyEditor = Boolean.TRUE;
                    }else if(!isOnlyFile && service.getRequestOutputType() == RequestOutputTypes.ONLY_FILE) {
                        isOnlyFile = Boolean.TRUE;
                    }else  if(!isAll && service.getRequestOutputType() == RequestOutputTypes.ALL) {
                        isAll = Boolean.TRUE;
                    }
                }
            }
            if(isAll || (isOnlyFile && isOnlyEditor)) {
                type = RequestOutputTypes.ALL;
            }else if(isOnlyFile) {
                type = RequestOutputTypes.ONLY_FILE;
            }else if(isOnlyEditor) {
                type = RequestOutputTypes.ONLY_EDITOR;
            }
        }else {
            type = RequestOutputTypes.ALL;
        }

        if (type == RequestOutputTypes.ALL || type == RequestOutputTypes.ONLY_EDITOR || type == RequestOutputTypes.ONLY_FILE) {
            String fileName = generatePdfName();
            GeneralFunctionsHelper.saveReport(getEntity().getRequest(), getSelectedTemplateId(),
                    getCurrentUser(), false, getEditText(), fileName, DaoManager.getSession());

            List<Document> documentListToView = EstateSituationHelper.getDocuments(type, getExamRequest(), Boolean.TRUE);
            if(!ValidationHelper.isNullOrEmpty(documentListToView)) {
                documentListToView.removeIf(d ->
                        (!ValidationHelper.isNullOrEmpty(d.getTypeId())
                                && !d.getTypeId().equals(DocumentType.FORMALITY.getId())) ||
                                (!ValidationHelper.isNullOrEmpty(getRequestNonSaleDocuments())
                                        && getRequestNonSaleDocuments().contains(d)));
            }
            if (!ValidationHelper.isNullOrEmpty(getExamRequest().getClient()) &&
                    !ValidationHelper.isNullOrEmpty(getExamRequest().getClient().getSendSalesDevelopmentFormality()) &&
                    getExamRequest().getClient().getSendSalesDevelopmentFormality()) {
                documentListToView.forEach(d -> d.setSelectedForDialogList(true));
            }
            setRequestSaleDocuments(documentListToView);
        }
    }


    public void onConfirm() throws Exception {
        if (!ValidationHelper.isNullOrEmpty(getExamRequest()) && !ValidationHelper.isNullOrEmpty(getPreviousStateId())
                && getPreviousStateId().equals(RequestState.EVADED.getId())) {
            executeJS("PF('confirmRedirect').show();");
        }else {
            saveRequestDocumentsAndNotify(true);
        }
    }

    public void saveRequestDocumentsAndNotify(boolean isConfirmed) throws Exception {

        SaveRequestDocumentsHelper.saveRequestDocuments(getExamRequest(), getRequestDocuments(), isConfirmed);
        if(!ValidationHelper.isNullOrEmpty(getOtherDocuments())){
            SaveRequestDocumentsHelper.saveRequestDocuments(getExamRequest(), getOtherDocuments(), isConfirmed);
        }

        if (!ValidationHelper.isNullOrEmpty(getRequestNonSaleDocuments())) {
            SaveRequestDocumentsHelper.saveRequestDocuments(getExamRequest(), getRequestNonSaleDocuments(), isConfirmed);
        }

        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality())) {
            getExamRequest().setStateId(RequestState.TO_BE_SENT.getId());
            DaoManager.save(getExamRequest(), true);
        }

        SendNotificationHelper.checkAndSendNotification(getExamRequest());

        RedirectHelper.goTo(PageTypes.REQUEST_LIST);
    }

    private String generatePdfName() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        String separator = "-";
        String spaceVal = "\\s";
        StringJoiner joiner = new StringJoiner(separator);
        String prefix = "";

        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality())) {
            StringBuilder sb = new StringBuilder();
            sb.append("Certificazione notarile_");

            List<Subject> subjects = DaoManager.load(Subject.class, new CriteriaAlias[]{
                            new CriteriaAlias("sectionC", "sc", JoinType.INNER_JOIN)},
                    new Criterion[]{Restrictions.eq("sc.formality", getExamRequest().getDistraintFormality()),
                            Restrictions.eq("sc.sectionCType", SectionCType.CONTRO.getName())});

            if(subjects != null && subjects.size() == 1) {
                Subject s = subjects.get(0);
                if (s.getTypeIsPhysicalPerson()) {
                    sb.append(s.getSurname().toUpperCase());
                } else if (SubjectType.LEGAL_PERSON.getId().equals(s.getTypeId())) {
                    sb.append(s.getBusinessName().toUpperCase());
                }
            }else {
                subjects.stream().forEach(s->{
                    String fullName = "";
                    if (s.getTypeIsPhysicalPerson()) {
                        fullName = s.getSurname().toUpperCase();
                    } else if (SubjectType.LEGAL_PERSON.getId().equals(s.getTypeId())) {
                        fullName = s.getBusinessName().toUpperCase();
                    }
                    if (!ValidationHelper.isNullOrEmpty(fullName)) {
                        if(sb.length() > 24)
                            sb.append("-");
                        sb.append(fullName);
                    }
                });
            }
            joiner.add(sb.toString());
        }
        else if (!ValidationHelper.isNullOrEmpty(getExamRequest().getSubject())) {
            if (getExamRequest().isPhysicalPerson()) {
                if (!ValidationHelper.isNullOrEmpty(getExamRequest().getSubject().getSurname())) {
                    joiner.add(getExamRequest().getSubject().getSurname()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
                if (!ValidationHelper.isNullOrEmpty(getExamRequest().getSubject().getName())) {
                    joiner.add(getExamRequest().getSubject().getName()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
                if (!ValidationHelper.isNullOrEmpty(getExamRequest().getSubject().getBirthCity())) {
                    joiner.add(getExamRequest().getSubject().getBirthCity().getDescription()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
                if (!ValidationHelper.isNullOrEmpty(getExamRequest().getSubject().getBirthDate())) {
                    joiner.add(DateTimeHelper.toFormatedString(getExamRequest().getSubject().getBirthDate(),
                            DateTimeHelper.getXmlDatePattert()));
                }
            } else {
                if (!ValidationHelper.isNullOrEmpty(getExamRequest().getSubject().getBusinessName())) {
                    joiner.add(getExamRequest().getSubject().getBusinessName()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
                if (!ValidationHelper.isNullOrEmpty(getExamRequest().getSubject().getNumberVAT())) {
                    joiner.add(getExamRequest().getSubject().getNumberVAT()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
            }
        }

        joiner.add("Cons");
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getAggregationLandChargesRegistry())) {
            joiner.add(getExamRequest().getAggregationLandChargesRegistry().getName()
                    .toUpperCase().replaceAll(spaceVal, separator));
        }

        if (joiner.toString().toUpperCase().startsWith("CON.") || joiner.toString().equalsIgnoreCase("CON")) {
            prefix = "-";
        }
        return prefix + joiner.toString().replaceAll("[^\\w\\s\\-_.]", "");
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {

    }

    public void updateTemplate() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        log.info("start updateTemplate RequestTextEditBean");

        if(!ValidationHelper.isNullOrEmpty(getCalledByApplicaButton()) && getCalledByApplicaButton()) {
            setCalledByApplicaButton(false);
            setSelectedSubjectToGenerate(null);
        }

        setExamRequest(DaoManager.get(Request.class, getRequestId()));

        getExamRequest().setIncludeNationalCost(getCostManipulationHelper().getIncludeNationalCost());
        Long modalIdOfTemplate = 0L;

        if (!ValidationHelper.isNullOrEmpty(getSelectedTemplateId())) {
            DocumentTemplate template = DaoManager.get(DocumentTemplate.class, getSelectedTemplateId());
            modalIdOfTemplate = template.getModel().getId();
        }
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getService())
                && getExamRequest().getService().getIsUpdate() && modalIdOfTemplate.equals(2L)) {
            getExamRequest().setNumberActUpdate(0d);
        }
        DaoManager.refresh(getExamRequest());
        DaoManager.save(getExamRequest(), true);

        if (!ValidationHelper.isNullOrEmpty(getSelectedTemplateId()) && !ValidationHelper.isNullOrEmpty(getExamRequest())) {
            try {
                DocumentTemplate template = DaoManager.get(DocumentTemplate.class, getSelectedTemplateId());
                if (template != null) {
                    if ("DECEDUTO".equalsIgnoreCase(template.getName())
                            && (ValidationHelper.isNullOrEmpty(getExamRequest().getSubject())
                            || !getExamRequest().getSubject().getTypeIsPhysicalPerson())) {
                        executeJS("PF('nonPhysDlg').show();");
                        return;
                    }
                    if ("ALIENATO".equalsIgnoreCase(template.getName()) && (getSelectedSubjectToGenerate() == null)
                            && getExamRequest().getSubjectsRelatedToFormalityThroughSituation().size() > 1) {

                        setSubjectsToSelect(getExamRequest().getSubjectsRelatedToFormalityThroughSituation());
                        executeJS("PF('subjectSelectDlg').show();");
                        return;
                    }

                    String bodyContent = MODEL_ID_CERTIFICAZIONE.equals(modalIdOfTemplate) ?
                            template.getBodyContent() : template.getEscapedBodyContent();

                    setEditText(TemplateToPdfHelper.replaceTags(bodyContent,
                            new TemplateEntity(getExamRequest(), getCurrentUser())));

                    setLastAppliedTemplate(getSelectedTemplateId());
                }
            } catch (TypeFormalityNotConfigureException e) {
                setTypeFormalityNotConfigureMessage(String.format("The Codice %s and the Tipologia %s act not found",
                        e.getCode(), e.getType().toString()));
                executeJS("PF('typeError').show();");
            }
        }

        log.info("end updateTemplate RequestTextEditBean");
    }

    public void updateCost() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        getExamRequest().setIncludeNationalCost(getCostManipulationHelper().getIncludeNationalCost());
        Long modalIdOfTemplate = 0L;
        if (!ValidationHelper.isNullOrEmpty(getSelectedTemplateId())) {
            DocumentTemplate template = DaoManager.get(DocumentTemplate.class, getSelectedTemplateId());
            modalIdOfTemplate = template.getModel().getId();
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedTemplateId()) && !ValidationHelper.isNullOrEmpty(getExamRequest())) {
            try {
                DocumentTemplate template = DaoManager.get(DocumentTemplate.class, getSelectedTemplateId());
                if (template != null) {

                    String bodyContent = MODEL_ID_CERTIFICAZIONE.equals(modalIdOfTemplate) ?
                            template.getBodyContent() : template.getEscapedBodyContent();
                    TemplateToPdfHelper.replaceTags(bodyContent,
                            new TemplateEntity(getExamRequest(), getCurrentUser()));
                }
            } catch (TypeFormalityNotConfigureException e) {
            }
        }

    }

    public void updateTemplateWithSubjectSelected() throws Exception {
        if (getSelectedSubjectToGenerate() != null) {
            getExamRequest().setSubjectToGenerateAlienatedTemplate(getSelectedSubjectToGenerate());
        } else {
            getExamRequest().setSubjectToGenerateAlienatedTemplate(null);
            executeJS("PF('subjectSelectDlg').hide();");
        }
        updateTemplate();
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        if(ValidationHelper.isNullOrEmpty(getDateConfimed()) || !getDateConfimed()) {
            if (!isEndDateCorrectForDistraintFormality()) {
                executeJS("PF('endDateErrorMessageWV').show();");
                return;
            }
        }

        if (ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality())
                && !ValidationHelper.isNullOrEmpty(getExamRequest().getAggregationLandChargesRegistry())
                && ValidationHelper.isNullOrEmpty(getRequestConservatoryList().stream()
                .filter(x -> x.getConservatoryDate() != null)
                .map(RequestConservatory::getConservatoryDate)
                .collect(Collectors.toList()))) {
            executeJS("PF('noUpdateDateDialogIdDialogWV').show();");
            DaoManager.getSession().evict(getExamRequest());
            return;
        }
        updateRecords();
        getEntity().setRequest(getExamRequest());
        DaoManager.save(getEntity());
        getExamRequest().setRequestPrint(getEntity());
        DaoManager.save(getExamRequest());
        setDateConfimed(null);
    }

    public void checkFormalitiesYear() throws PersistenceBeanException, IllegalAccessException {
        String group = TemplatePdfTableHelper.getEstateFormalityConservationDate(getExamRequest());
        if (!ValidationHelper.isNullOrEmpty(group)) {
            List<Formality> formalities = new ArrayList<>();
            List<EstateSituation> estateSituationList = DaoManager.load(EstateSituation.class, new Criterion[]{
                    Restrictions.eq("request.id", getRequestId())
            });

            estateSituationList.stream()
                    .filter(es -> !ValidationHelper.isNullOrEmpty(es.getFormalityList()))
                    .forEach(es -> formalities.addAll(es.getFormalityList()));

            formalities.removeIf(f -> ValidationHelper.isNullOrEmpty(f.getPresentationDate()) ||
                    ValidationHelper.isNullOrEmpty(f.getSectionA())
                    || !ValidationHelper.isNullOrEmpty(f.getSectionA().getOtherData()));

            Optional<Formality> ifExist = formalities
                    .stream()
                    .filter(f ->  f.getPresentationDate().after(DateTimeHelper.minusYears(DateTimeHelper.getNow(), 20)))
                    .findFirst();

            if(ifExist.isPresent()){
                executeJS("PF('checkFormalityYearWV').show();");
                return;
            }
        }
        checkRelatedEstateFormalities();
    }

    public void checkRelatedEstateFormalities() throws PersistenceBeanException, IllegalAccessException {
        List<EstateFormality> estateFormalities = getExamRequest().getEstateFormalityList();
        if (getExamRequest().getEstateFormalityList().stream()
                .anyMatch(x -> x.getAccountable() == null || !x.getAccountable())) {
            executeJS("PF('checkEstateFormalityDialogWV').show();");
            return;
        }
        if(getExamRequest().getSalesDevelopment()
                && !ValidationHelper.isNullOrEmpty(estateFormalities)){

            List<EstateSituation> estateSituationList = DaoManager.load(EstateSituation.class, new Criterion[]{
                    Restrictions.eq("request.id", getRequestId()),
                    Restrictions.and(Restrictions.isNotNull("salesDevelopment"),
                            Restrictions.eq("salesDevelopment", Boolean.TRUE))
            });

            List<Formality> formalities = new ArrayList<>();
            if(estateFormalitySalesMessage == null)
                estateFormalitySalesMessage = new ArrayList<>();

            Calendar cal = Calendar.getInstance();
            Date today = cal.getTime();
            cal.add(Calendar.YEAR, -5);
            Date endYear = cal.getTime();

            estateSituationList.stream()
                    .filter(es -> !ValidationHelper.isNullOrEmpty(es.getFormalityList()))
                    .forEach(es -> formalities.addAll(es.getFormalityList()));

            estateFormalities.stream()
                    .filter(ef -> !ValidationHelper.isNullOrEmpty(ef.getEstateFormalityType())
                            && ef.getEstateFormalityType().equals(EstateFormalityType.FORMALITY_TYPE_C))
                    .filter(ef -> !ValidationHelper.isNullOrEmpty(ef.getNumRG()) &&
                            !ValidationHelper.isNullOrEmpty(ef.getNumRP()) &&
                            !ValidationHelper.isNullOrEmpty(ef.getDate()) &&
                            !(ef.getDate().after(today) || ef.getDate().before(endYear)))
                    .forEach(ef -> {
                        Formality formality = formalities.stream()
                                .filter(f ->
                                        f.getParticularRegister().equals(ef.getNumRP()) &&
                                                f.getGeneralRegister().equals(ef.getNumRG().toString()) &&
                                                f.getPresentationDate().compareTo(ef.getDate()) == 0
                                ).findFirst().orElse(null);
                        if(formality == null){
                            String text = String.format("%s - %s - %s", ef.getNumRG(), ef.getNumRP(),
                                    DateTimeHelper.toFormatedString(ef.getDate(),
                                            DateTimeHelper.getDatePattern()));
                            if(!estateFormalitySalesMessage.contains(text))
                                estateFormalitySalesMessage.add(text);
                        }
                    });

            if(!ValidationHelper.isNullOrEmpty(estateFormalitySalesMessage)){
                executeJS("PF('checkEstateFormalitySalesDialogWV').show();");
            }
        }
        checkForQuotaAndPropertyTypeIsSet();
    }

    public void checkDate() {
        setDateConfimed(Boolean.TRUE);
    }
    private boolean isEndDateCorrectForDistraintFormality() {
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality())) {
            if (ValidationHelper.isNullOrEmpty(getExamRequest().getEndDate())) {
                return false;
            } else {
                if (!ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality().getPresentationDate())) {
                    Instant endDate = getExamRequest().getEndDate().toInstant();
                    Instant presentationDate = getExamRequest().getDistraintFormality().getPresentationDate().toInstant();
                    return endDate.compareTo(presentationDate) > 0
                            && endDate.compareTo(presentationDate.plus(60, ChronoUnit.DAYS)) <= 0;
                }
            }
        }
        return true;
    }

    public void checkForQuotaAndPropertyTypeIsSet() {
        if (!REAL_ESTATE_TEMPLATE_ID.equals(getLastAppliedTemplate())) {
            this.pageSave();
            return;
        }

        int countOfLinesThatContainsQuotaAndProperty = findCountOfMatches("DIRITTI PARI A \\d+\\/\\d+ [a-zA-Z\\s]+.+?(?=<)",
                getEditText());
        int countOfAllTitles = findCountOfMatches("DIRITTI PARI A.*?(?=<)", getEditText());

        if (countOfAllTitles == countOfLinesThatContainsQuotaAndProperty) {
            this.pageSave();
        } else {
            executeJS("PF('saveConfirmation').show();");
        }
    }

    private int findCountOfMatches(String regex, String text) {
        int result = 0;

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            result++;
        }
        return result;
    }

    public void goBack() {
        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO, "",
                ResourcesHelper.getValidation("requestPrintSuccessSave"));
    }

    private void fillTemplates() {
        try {

            List<SelectItem> templates = GeneralFunctionsHelper.fillTemplates(
                    DocumentGenerationPlaces.REQUEST_PRINT, null, null, DaoManager.getSession());
            this.setTemplates(templates.stream().sorted(Comparator.comparing(SelectItem::getLabel)).collect(Collectors.toList()));

        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void updateRecords() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        this.getEntity().setBodyContent(getEditText());
        if (!ValidationHelper.isNullOrEmpty(getSelectedTemplateId())) {
            this.getEntity().setTemplate(DaoManager.get(DocumentTemplate.class, getSelectedTemplateId()));
        }
    }

    private void fillZoom() {
        this.setPdfZoomValue(125L);
    }

    public void openTableProcedure() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEstateSituationRelationshipList(new ArrayList<>());
        if (getShowEstateTable()) {
            setShowEstateTable(!getShowEstateTable());
            return;
        }
        log.info("Opennig Panello di controlo");
        setExamRequest(DaoManager.get(Request.class, getExamRequest().getId()));

        final List<EstateSituation> situations = getExamRequest().getSituationEstateLocations().stream()
                .sorted(new SortByInnerEstateFormalityDate()).collect(Collectors.toList());

        for (EstateSituation estateSituation : situations) {
            Map<List<RelationshipGroupingWrapper>, List<Property>> re = new HashMap<>();
            TemplatePdfTableHelper.wrapRequestProperties(estateSituation.getPropertyList(), estateSituation.getRequest().getSubject(),
                    true, re, getExamRequest());
            estateSituation.setPropertyList(re.values().stream().flatMap(List::stream).collect(Collectors.toList()));
        }

        setEstateSituations(situations.stream().map(EstateSituationEditInTableWrapper::new).collect(Collectors.toList()));

        List<Long> formalityIds = situations.stream().map(EstateSituation::getEstateFormalityList)
                .flatMap(List::stream).map(EstateFormality::getId).collect(Collectors.toList());
        if (ValidationHelper.isNullOrEmpty(formalityIds)) {
            formalityIds.add(0L);
        }
        List<Long> estateIds = situations.stream().map(EstateSituation::getPropertyList)
                .flatMap(List::stream).map(Property::getId).collect(Collectors.toList());

        EstateSituation situation = new EstateSituation();

        situation.setEstateFormalityList(DaoManager.load(EstateFormality.class, new CriteriaAlias[]{
                new CriteriaAlias("requestFormalities", "rf", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("rf.request.id", getExamRequest().getId()),
                Restrictions.not(Restrictions.in("id", formalityIds))
        }));

        situation.setPropertyList(getExamRequest().getPropertyList().stream()
                .filter(p -> !estateIds.contains(p.getId())).collect(Collectors.toList()));

        situation.setRequest(getExamRequest());
        setOtherEstateSituations(new LinkedList<>());
        getOtherEstateSituations().add(new EstateSituationEditInTableWrapper(situation));

        if(!ValidationHelper.isNullOrEmpty(situations) && getExamRequest().getSalesDevelopment()){
            setShowSalesSection(Boolean.TRUE);
            List<Long> salesFormalityIds = situations.stream()
                    .filter(es -> !ValidationHelper.isNullOrEmpty(es.getSalesDevelopment()) && es.getSalesDevelopment())
                    .map(EstateSituation::getFormalityList)
                    .flatMap(List::stream).map(Formality::getId).collect(Collectors.toList());
            if (ValidationHelper.isNullOrEmpty(salesFormalityIds)) {
                salesFormalityIds.add(0L);
            }
            List<Formality> salesFormalities = DaoManager.load(Formality.class, new Criterion[]{
                    Restrictions.in("id", salesFormalityIds)
            });

            EstateSituation salesSituation = new EstateSituation();
            salesSituation.setRequest(getExamRequest());
            setSalesOtherEstateSituations(new LinkedList<>());
            if(!ValidationHelper.isNullOrEmpty(salesFormalities)){
                List<Long> listIds = EstateSituationHelper.getIdSubjects(getExamRequest());
                List<Subject> presumableSubjects = EstateSituationHelper.getListSubjects(listIds);
                List<Subject> unsuitableSubjects = SubjectHelper.deleteUnsuitable(presumableSubjects, salesFormalities);
                presumableSubjects.removeAll(unsuitableSubjects);
                presumableSubjects.add(getExamRequest().getSubject());
                for (Formality formality : salesFormalities) {
                    List<Property> properties = formality.loadPropertiesByRelationship(presumableSubjects);
                    getSalesOtherEstateSituations().add(new SalesEstateSituationEditTableWrapper(formality,
                            properties, salesSituation));
                }
            }
        }
        setEstateSituationFormalities(getExamRequest().getSituationEstateLocations().stream()
                .peek(es -> es.getFormalityList().forEach(f -> f.setShouldReportRelationships(
                        es.getReportRelationship() == null ? false : es.getReportRelationship())))
                .map(EstateSituation::getFormalityList).flatMap(List::stream).distinct()
                .map(FormalityEditInTableWrapper::new).collect(Collectors.toList()));

        setShowEstateTable(!getShowEstateTable());
        updateConservatoryList();
        manageCostForced();
        log.info("End of opening 'Pannello di controllo'");
    }

    private void updateConservatoryList() throws PersistenceBeanException, IllegalAccessException {
        List<RequestConservatory> requestConservatoriesDB = DaoManager.load(RequestConservatory.class, new Criterion[]{
                Restrictions.eq("request.id", getExamRequest().getId())
        });
        setRequestConservatoryList(new ArrayList<>());
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getAggregationLandChargesRegistry())
                && !ValidationHelper.isNullOrEmpty(getExamRequest().getAggregationLandChargesRegistry().getLandChargesRegistries())) {
            for (LandChargesRegistry registry : getExamRequest().getAggregationLandChargesRegistry().getLandChargesRegistries()) {
                RequestConservatory rc = requestConservatoriesDB.stream().filter(rcDB -> rcDB.getRegistry().getId().equals(registry.getId())).findAny().orElse(null);
                if (rc == null) {
                    rc = new RequestConservatory();
                    rc.setRegistry(registry);
                    rc.setRequest(getExamRequest());
                }
                getRequestConservatoryList().add(rc);
            }
            if(!ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality())) {
                RequestConservatory rc = new RequestConservatory();
                rc.setRequest(getExamRequest());
                rc.setConservatoryDate(getExamRequest().getCertificationDate());
                getRequestConservatoryList().add(rc);
            }
        }
    }

    public static class SortByInnerEstateFormalityDate implements Comparator<EstateSituation> {
        @Override
        public int compare(EstateSituation o1, EstateSituation o2) {
            if (!ValidationHelper.isNullOrEmpty(o1.getEstateFormalityList()) && !ValidationHelper.isNullOrEmpty
                    (o2.getEstateFormalityList())) {
                o1.getEstateFormalityList().sort(Comparator.comparing(EstateFormality::getDate).reversed());
                o2.getEstateFormalityList().sort(Comparator.comparing(EstateFormality::getDate).reversed());
                return o1.getEstateFormalityList().get(0).getDate().compareTo(o2.getEstateFormalityList().get(0).getDate());
            } else if (!ValidationHelper.isNullOrEmpty(o1.getEstateFormalityList())) {
                return 1;
            } else if (!ValidationHelper.isNullOrEmpty(o2.getEstateFormalityList())) {
                return -1;
            }
            return 0;
        }
    }

    public void saveChanges() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality())) {
            if (ValidationHelper.isNullOrEmpty(getRequestEndDate())) {
                executeJS("PF('requestEndDateErrorWV').show();");
                return;
            }
        } else {
            Boolean needBlock = needBlockCadastralSave();
            if (needBlock != null) {
                if (needBlock) {
                    executeJS("PF('cadastralDateError').show();");
                    return;
                } else {
                    executeJS("PF('dateBeforeRequestCreateDateDialogWV').show();");
                }
            }
        }

        for (EstateSituationEditInTableWrapper situation : getEstateSituations()) {
            for (PropertyEditInTableWrapper propertyEditInTableWrapper : situation.getPropertyList()) {
                if(!ValidationHelper.isNullOrEmpty(situation.getEstateSituationRelationshipList())){
                    if(ValidationHelper.isNullOrEmpty(propertyEditInTableWrapper.getRelationshipList()))
                        propertyEditInTableWrapper.setRelationshipList(new ArrayList<>());
                    propertyEditInTableWrapper.getRelationshipList().addAll(situation.getEstateSituationRelationshipList());
                }
            }
            situation.save();
        }
        for (EstateSituationEditInTableWrapper situation : getOtherEstateSituations()) {
            situation.save();
        }
        for (FormalityEditInTableWrapper formality : getEstateSituationFormalities()) {
            formality.save();
        }
        for (SalesEstateSituationEditTableWrapper situation :
                CollectionUtils.emptyIfNull(getSalesOtherEstateSituations())) {
            situation.save();
        }
        for (RequestConservatory rc : getRequestConservatoryList()) {
            if (!ValidationHelper.isNullOrEmpty(rc.getConservatoryDate())
                    && !ValidationHelper.isNullOrEmpty(rc.getRegistry())) {
                DaoManager.refresh(rc.getRequest());
                DaoManager.save(rc, true);
            }
        }
        boolean requestChanged = false;

        manageCostForced();

        if (!String.valueOf(getExamRequest().getNumberActUpdate()).equals(getRequestNumberActUpdate())
                && !ValidationHelper.isNullOrEmpty(getRequestNumberActUpdate())) {
            try {
                Integer.parseInt(getRequestNumberActUpdate());
            } catch (NumberFormatException e) {
                if (!ValidationHelper.isNullOrEmpty(getExamRequest().getNumberActUpdate())) {
                    this.setRequestNumberActUpdate(String.valueOf(getExamRequest().getNumberActUpdate().intValue()));
                }
                return;
            }
            requestChanged = true;
            getExamRequest().setNumberActUpdate(Double.valueOf(getRequestNumberActUpdate()));
        }

        if (!Objects.equals(getRequestEndDate(), getExamRequest().getEndDate())) {
            getExamRequest().setEndDate(getRequestEndDate());
            requestChanged = true;
        }

        if (!Objects.equals(getExamRequest().getCommentCertification(), getRequestCommentCertification())) {
            getExamRequest().setCommentCertification(getRequestCommentCertification()
                    .replaceAll("<.+?>", "").replaceAll("&nbsp;", " ").trim());
            requestChanged = true;
        }

        if(!ValidationHelper.isNullOrEmpty(getRequestConservatoryList()) &&
                !ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality()) ) {

            for (RequestConservatory rc : getRequestConservatoryList()) {
                if(ValidationHelper.isNullOrEmpty(rc.getRegistry())) {
                    Date dataCertificazione = rc.getConservatoryDate();
                    if (!Objects.equals(dataCertificazione, getExamRequest().getCertificationDate())) {
                        requestChanged = true;
                        getExamRequest().setCertificationDate(dataCertificazione);
                    }
                }

            }
        }

        log.info("before saving request");
        if (requestChanged) {
            if (!Hibernate.isInitialized(getExamRequest().getRequestFormalities())) {
                getExamRequest().reloadRequestFormalities();
            }
            if (!Hibernate.isInitialized(getExamRequest().getRequestSubjects())) {
                getExamRequest().reloadRequestSubjects();
            }
            DaoManager.save(getExamRequest(), true);
        }
        log.info("after saving request");

        //DaoManager.getSession().clear();
        updateTemplate();
        log.info("finish saveChanges RequestTextEditBean");
    }

    private void manageCostForced() {

        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getTotalCost())
                && !getExamRequest().getTotalCost().equals(getRequestCostForced())) {
            try {
                if (!ValidationHelper.isNullOrEmpty(getRequestCostForced())) {
                    Double.parseDouble(getRequestCostForced().replaceAll(",", "."));
                }
                this.setRequestCostForced(getExamRequest().getTotalCost());
            } catch (NumberFormatException e) {
                if (!ValidationHelper.isNullOrEmpty(getExamRequest().getCostForced())) {
                    this.setRequestCostForced(String.valueOf(getExamRequest().getCostForced()));
                } else if (!ValidationHelper.isNullOrEmpty(getExamRequest().getTotalCost())) {
                    this.setRequestCostForced(getExamRequest().getTotalCost());
                }
            }
        }
    }

    private Boolean needBlockCadastralSave() {
        boolean needMessage = false;
        for (RequestConservatory rc : getRequestConservatoryList()) {
            if (!ValidationHelper.isNullOrEmpty(rc.getConservatoryDate())) {
                if (!needMessage && getConservatoryMinDate().after(rc.getConservatoryDate())) {
                    needMessage = true;
                }
                if (new Date().before(rc.getConservatoryDate())) {
                    return true;
                }
                Calendar c = Calendar.getInstance();
                c.setTime(rc.getConservatoryDate());
                if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                        c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    return true;
                }
            }
        }

        if (!needMessage) {
            return null;
        } else {
            return false;
        }
    }

    public void prepareComment() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        /*       Consumer<BaseEditInTableWrapper> unwrap = w -> getComment().setComment("<span style=\"font-size:14px;\">" +
                "<span style=\"font-family:Courier New,Courier,monospace;\">" +
                (w.getComment() == null ? "" : w.getComment()) + "</span></span>");*/

        Consumer<BaseEditInTableWrapper> unwrap = w -> getComment().setComment(w.getComment() == null ? "" : w.getComment());
        workWithTable(unwrap, false);
    }

    public void editOrCreateDatafromProperty() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        DatafromProperty datafromProperty;
        try {
            EstateSituationEditInTableWrapper estateSituationEditInTableWrapper = getEstateSituations().get(getEstateSituationId());
            PropertyEditInTableWrapper propertyEditInTableWrapper = estateSituationEditInTableWrapper.getPropertyList()
                    .get(Math.toIntExact(getSelectedId()));
            List<DatafromProperty> associatedDatafromProperties = propertyEditInTableWrapper
                    .getAssociatedWithEstateSituationDatafromProperties();
            LogHelper.debugInfo(log, "associatedDatafromProperties " + associatedDatafromProperties);
            LogHelper.debugInfo(log, "getEntityEditId() " + getEntityEditId());

            if (getEntityEditId() == null || Math.toIntExact(getEntityEditId()) == 0) {
                Property property = DaoManager.get(Property.class, propertyEditInTableWrapper.getId());
                SituationProperty situationProperty = property
                        .getSituationPropertyByEstateSituationId(estateSituationEditInTableWrapper.getEstateSituationId());

                datafromProperty = new DatafromProperty();
                datafromProperty.setProperty(property);
                //datafromProperty.setSituationProperties(new ArrayList<>());
                datafromProperty.setSituationProperties(Collections.singletonList(situationProperty));
                datafromProperty.setAssociated(true);

                situationProperty.getDatafromProperties().add(datafromProperty);

                associatedDatafromProperties.add(datafromProperty);
            } else {
                datafromProperty = associatedDatafromProperties.get(Math.toIntExact(getEntityEditId()));
            }
            datafromProperty.setText(getDatafromPropertyText().replaceAll("<.+?>", "")
                    .replaceAll("&nbsp;", " ").trim());
        }catch (Exception e) {
            LogHelper.log(log, e);
            throw e;
        }
    }

    public void deleteDatafromPropertyAssociation() {


        List<DatafromProperty> datafromPropertyList = getEstateSituations().get(getEstateSituationId()).getPropertyList()
                .get(Math.toIntExact(getSelectedId())).getAssociatedWithEstateSituationDatafromProperties();
        DatafromProperty datafromProperty = datafromPropertyList.get(Math.toIntExact(getEntityEditId()));
        datafromProperty.setAssociated(Boolean.FALSE);

        try {
            Property property = DaoManager.get(Property.class, getEstateSituations().get(getEstateSituationId()).getPropertyList().get(Math.toIntExact(getSelectedId())).getId());
            if(!ValidationHelper.isNullOrEmpty(getEstateSituationId())) {
                SituationProperty situationProperty = property.getSituationPropertyByEstateSituationId(
                        Long.valueOf(getEstateSituationId()));
                if(!ValidationHelper.isNullOrEmpty(situationProperty))
                    datafromProperty.getSituationProperties().removeIf(x -> x.getId().equals(situationProperty.getId()));

            }

            DaoManager.remove(datafromProperty, true);
            datafromPropertyList.remove(datafromProperty);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void changeComment() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        Consumer<BaseEditInTableWrapper> wrap = w -> w.setComment(getComment().getComment());
        workWithTable(wrap, true);
        setCurrentComment(null);
        setComment(new Comment());
        setSelectedId(null);
        RequestContext.getCurrentInstance().update("requestDocumentTable");
        RequestContext.getCurrentInstance().update("requestDocumentNonSaleTable");
        RequestContext.getCurrentInstance().update("requestDocumentSaleTable");
    }

    public void deleteComment(Comment comment) {
        getExamRequest().getComments().remove(comment);
        try {
            DaoManager.remove(comment, true);
        } catch (PersistenceBeanException e) {
            e.printStackTrace();
        }
    }

    public void compareNumberActsBeforeExtraCost() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getService()) && getExamRequest().getService().getIsUpdate()
                && ValidationHelper.isNullOrEmpty(getExamRequest().getNumberActUpdate())) {
            RequestContext.getCurrentInstance().update("inputEstateCost");
            executeJS("PF('estateFormalityCostDlg').show();");
        } else if ((ValidationHelper.isNullOrEmpty(getExamRequest().getCostButtonConfirmClicked())
                || !getExamRequest().getCostButtonConfirmClicked())
                && !ValidationHelper.isNull(getExamRequest().getClient().getMaxNumberAct()) &&
                getExamRequest().getNumberActOrSumOfEstateFormalitiesAndOther() > getExamRequest().getClient().getMaxNumberAct()) {
            setNumberActDifference(String.format(ResourcesHelper.getString("requestNumActGratedThenClientNumberAct"),
                    getExamRequest().getNumberActOrSumOfEstateFormalitiesAndOther().intValue(),
                    getExamRequest().getClient().getMaxNumberAct()));
            RequestContext.getCurrentInstance().update("requestNumberActDlg");
            executeJS("PF('requestNumberActGreaterThenClientNumberAct').show()");
        } else {
            this.viewExtraCost();
        }
    }

    public void editComment(Comment comment) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        setComment(comment);
    }

    public void saveRequestEstateFormalityCost() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        if (!Hibernate.isInitialized(getExamRequest().getRequestFormalities())) {
            getExamRequest().reloadRequestFormalities();
        }
        getCostManipulationHelper().saveRequestEstateFormalityCost(getExamRequest());
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getNumberActUpdate())) {
            compareNumberActsBeforeExtraCost();
        }
    }

    public void viewExtraCost() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        log.debug("Fecthing extra cost for request " + getExamRequest());
        boolean reCalculate = true;
        if(getExamRequest().getCostButtonConfirmClicked() != null && getExamRequest().getCostButtonConfirmClicked()){
            reCalculate = false;
        }
        viewExtraCost(reCalculate);
    }

    public void viewExtraCost(boolean recalculate) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        log.debug("Fecthing extra cost for request " + getExamRequest());
        if (!ValidationHelper.isNullOrEmpty(getRequestNumberActUpdate())) {
            getExamRequest().setNumberActUpdate(Double.valueOf(getRequestNumberActUpdate()));
        }
        getCostManipulationHelper().viewExtraCost(getExamRequest(), recalculate);
    }

    public void addExtraCost(String extraCostValue) {
        getCostManipulationHelper().addExtraCost(extraCostValue, getRequestId());
    }

    public void deleteExtraCost(ExtraCost extraCostToDelete) {
        getCostManipulationHelper().getRequestExtraCosts().remove(extraCostToDelete);
        getCostManipulationHelper().setIncludeNationalCost(null);
    }

    public void saveRequestExtraCost() throws Exception {
        getCostManipulationHelper().saveRequestExtraCost(getExamRequest());
        CostCalculationHelper calculation = new CostCalculationHelper(getExamRequest());
        calculation.calculateAllCosts(true);
        if(!ValidationHelper.isNullOrEmpty(getExamRequest().getRequestPrint())) {
            updateTemplate();
        }
//        else {
//            if(!ValidationHelper.isNullOrEmpty(getExamRequest().getClient()) &&
//                    !ValidationHelper.isNullOrEmpty(getExamRequest().getClient().getCostOutput()) &&
//                    getExamRequest().getClient().getCostOutput()){
//                executeJS("PF('reloadPageDialogWV').show()");
//            }
//        }
        //  getCostManipulationHelper().saveRequestExtraCost(getExamRequest());
        // updateTemplate();
        editExcelDataRequest();
    }

    public void editExcelDataRequest() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
            RedirectHelper.goToExcelDataRequest(getRequestId(), null, false);
    }

    public void reloadPage() throws IllegalAccessException, InstantiationException, PersistenceBeanException {
        updateTemplate();
    }

    private void workWithTable(Consumer<BaseEditInTableWrapper> funk, boolean wrap) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        switch (getCurrentComment()) {
            case SELECTED_ESTATE_FORMALITY:
                getEstateSituations().stream().map(EstateSituationEditInTableWrapper::getEstateFormalityList)
                        .flatMap(List::stream).filter(w -> w.getId().equals(getSelectedId())).findAny()
                        .ifPresent(funk);
                break;
            case SELECTED_PROPERTY:
                getEstateSituations().stream().map(EstateSituationEditInTableWrapper::getPropertyList)
                        .flatMap(List::stream).filter(w -> w.getId().equals(getSelectedId())).findAny()
                        .ifPresent(funk);
                break;
            case SELECTED_ESTATE_SITUATION:
                getEstateSituations().stream().map(EstateSituationEditInTableWrapper::getComment)
                        .filter(w -> w.getId().equals(getSelectedId())).findAny()
                        .ifPresent(funk);
                break;
            case SELECTED_ESTATE_SITUATION_INIT:
                getEstateSituations().stream().map(EstateSituationEditInTableWrapper::getCommentInit)
                        .filter(w -> w.getId().equals(getSelectedId())).findAny()
                        .ifPresent(funk);
                break;
            case NOT_SELECTED_ESTATE_FORMALITY:
                getOtherEstateSituations().stream().map(EstateSituationEditInTableWrapper::getEstateFormalityList)
                        .flatMap(List::stream).filter(w -> w.getId().equals(getSelectedId())).findAny()
                        .ifPresent(funk);
                break;
            case NOT_SELECTED_PROPERTY:
                getOtherEstateSituations().stream().map(EstateSituationEditInTableWrapper::getPropertyList)
                        .flatMap(List::stream).filter(w -> w.getId().equals(getSelectedId())).findAny()
                        .ifPresent(funk);
                break;
            case REQUEST:

                if (!ValidationHelper.isNullOrEmpty(this.getComment().getComment())) {
                    getComment().setRequest(getExamRequest());
                    DaoManager.refresh(getExamRequest());
                    DaoManager.save(getComment(), true);

                    if (ValidationHelper.isNullOrEmpty(getExamRequest().getComments())) {
                        getExamRequest().setComments(new ArrayList<>());
                    }

                    List<Comment> load = DaoManager.load(Comment.class
                            , new Criterion[]{Restrictions.eq("request.id", getExamRequest().getId())});

                    getExamRequest().setComments(load);

                }
                break;
            case SELECTED_FORMALITY:
                getEstateSituationFormalities().stream().filter(f -> f.getId().equals(getSelectedId())).findAny()
                        .ifPresent(funk);
                break;
            case FORMALITY_DISTRAINT_COMMENT:
                FormalityEditInTableWrapper wrapper = getEstateSituationFormalities().stream()
                        .filter(f -> f.getId().equals(getSelectedId())).findAny().orElse(null);
                if (wrapper != null) {
                    if (!wrap) {
                        getComment().setComment(wrapper.getDistraintComment());
                    } else {
                        wrapper.setDistraintComment(getComment().getComment());
                    }
                }
                break;
            case COMMENT_CERTIFICATION:
                if (!wrap) {
                    String comment = "";
                    if (ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality())
                            || !ValidationHelper.isNullOrEmpty(getRequestCommentCertification())) {
                        comment = getRequestCommentCertification();
                    } else if (!ValidationHelper.isNullOrEmpty(getExamRequest().getDistraintFormality())
                            && ValidationHelper.isNullOrEmpty(getRequestCommentCertification())) {
                        comment = CertificazioneTableGenerator.getRequestCertificateComment(getExamRequest());
                    }
                    getComment().setComment(comment);
                } else {
                    setRequestCommentCertification(getComment().getComment()
                            .replaceAll("<.+?>", ""));
                }
                break;
            case FORMALITY_COMMENT_CERTIFICATION:
                Optional<FormalityEditInTableWrapper> formalityEditInTableWrapper = getEstateSituationFormalities()
                        .stream().filter(x -> x.getId().equals(getSelectedId())).findFirst();
                if (!wrap) {
                    formalityEditInTableWrapper.ifPresent(x -> {
                        boolean setShouldReportRelationship = false;
                        for(EstateSituation es : getExamRequest().getSituationEstateLocations()) {
                            if(ValidationHelper.isNullOrEmpty(es.getOtherType()) || !es.getOtherType()) {
                                for(Formality f : es.getFormalityList()) {
                                    if(f.getId().equals(x.getId())) {
                                        setShouldReportRelationship = es.getReportRelationship() == null ? false : es.getReportRelationship();
                                    }
                                }
                                break;
                            }
                        }
                        getComment().setComment(x.getTextCertificationStr(setShouldReportRelationship));
                    });
                } else {
                    formalityEditInTableWrapper.ifPresent(x -> {
                        x.setTextCertification(getComment().getComment().replaceAll("<.+?>", ""));
                        x.setEdited(true);
                    });
                }
                break;
        }
    }

    public void prepareRelationships() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setSelectedRegime(null);
        setCurrentProperty(null);
        setEstateSelectedRegime(null);
        setCurrentEstateSituation(null);

        switch (getCurrentComment()) {
            case SELECTED_PROPERTY:
                setCurrentProperty(getEstateSituations().stream().map(EstateSituationEditInTableWrapper::getPropertyList)
                        .flatMap(List::stream).filter(w -> w.getId().equals(getSelectedId())).findAny()
                        .orElse(null));
                break;
            case NOT_SELECTED_PROPERTY:
                setCurrentProperty(getOtherEstateSituations().stream().map(EstateSituationEditInTableWrapper::getPropertyList)
                        .flatMap(List::stream).filter(w -> w.getId().equals(getSelectedId())).findAny()
                        .orElse(null));
                break;
            case SELECTED_ESTATE_SITUATION:
                setCurrentEstateSituation(getEstateSituations().stream()
                        .filter(w -> w.getEstateSituationId().equals(getSelectedId())).findAny()
                        .orElse(null));
                break;
            default:
                break;
        }

        if(!ValidationHelper.isNullOrEmpty(getCurrentProperty()))
            getCurrentProperty().prepareRelationship(getExamRequest().getSubject());
        else if(!ValidationHelper.isNullOrEmpty(getCurrentEstateSituation())){
            setEstateSituationRelationshipList(getCurrentEstateSituation().getEstateSituationRelationshipList());
        }

        setQuote1(null);
        setQuote2(null);
        setPropertyType(null);
        setEditRelationship(null);
        setDeleteRelationship(null);

        setEstateQuote1(null);
        setEstateQuote2(null);
        setEstatePropertyType(null);
    }

    public void editRelationShip() {
        this.setQuote1(this.getEditRelationship().getQuote1());
        this.setQuote2(this.getEditRelationship().getQuote2());
        this.setPropertyType(this.getEditRelationship().getType());
    }

    public void saveRelationship() {
        cleanValidation();
        if (ValidationHelper.isNullOrEmpty(getQuote1())
                || ValidationHelper.isNullOrEmpty(getQuote2())
                || Double.parseDouble(getQuote1().replaceAll(",", "."))
                / Double.parseDouble(getQuote2().replaceAll(",", ".")) > 1.0) {
            addRequiredFieldException("form:inputQuote1");
            addRequiredFieldException("form:inputQuote2");
            return;
        }
        if (ValidationHelper.isNullOrEmpty(getPropertyType())
                || !ValidationHelper.isNullOrEmpty(getCurrentProperty())
                && !ValidationHelper.isNullOrEmpty(getCurrentProperty().getRelationshipListToShow())
                && (ValidationHelper.isNullOrEmpty(getEditRelationship())
                && getCurrentProperty().getRelationshipListToShow().stream().anyMatch(r -> getPropertyType().equals(r.getType())))) {
            addRequiredFieldException("form:propertyType");
            return;
        }
        Regime regime = null;
        if(!ValidationHelper.isNullOrEmpty(getSelectedRegime())){
            try {
                regime = DaoManager.get(Regime.class, getSelectedRegime());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        if(!ValidationHelper.isNullOrEmpty(getEditRelationship())) {
            RelationshipEditInTableWrapper relationshipEditInTableWrapper =
                    getCurrentProperty().getRelationshipList().stream()
                            .filter(r -> r.getId().equals(getEditRelationship().getId()))
                            .findAny()
                            .orElse(null);
            relationshipEditInTableWrapper.setQuote1(getQuote1());
            relationshipEditInTableWrapper.setQuote2(getQuote2());
            relationshipEditInTableWrapper.setType(getPropertyType());
            relationshipEditInTableWrapper.setRegime(regime);
        }else {

            getCurrentProperty().getRelationshipList().add(new RelationshipEditInTableWrapper(
                    getQuote1(), getQuote2(), getPropertyType(), getExamRequest().getSubject(), regime));
        }
        setSelectedRegime(null);
        setCurrentComment(null);
        setComment(new Comment());
        setSelectedId(null);
        setQuote1(null);
        setQuote2(null);
        setPropertyType(null);
        setEditRelationship(null);
    }

    public void deleteRelationship() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        cleanValidation();
        if(!ValidationHelper.isNullOrEmpty(getDeleteRelationship())) {
            RelationshipEditInTableWrapper relationshipEditInTableWrapper =
                    getCurrentProperty().getRelationshipList().stream()
                            .filter(r -> r.getId().equals(getDeleteRelationship().getId()))
                            .findAny()
                            .orElse(null);
            relationshipEditInTableWrapper.setToDelete(Boolean.TRUE);
            Property property = DaoManager.get(Property.class, getCurrentProperty().getId());
            relationshipEditInTableWrapper.save(property);
        }
        setDeleteRelationship(null);
    }

    public void saveAllRelationship() {
        cleanValidation();
        if (ValidationHelper.isNullOrEmpty(getEstateQuote1())
                || ValidationHelper.isNullOrEmpty(getEstateQuote2())
                || Double.parseDouble(getEstateQuote1().replaceAll(",", "."))
                / Double.parseDouble(getEstateQuote2().replaceAll(",", ".")) > 1.0) {
            addRequiredFieldException("form:estateInputQuote1");
            addRequiredFieldException("form:estateInputQuote2");
            return;
        }
        if (ValidationHelper.isNullOrEmpty(getEstatePropertyType())
                || !ValidationHelper.isNullOrEmpty(getCurrentEstateSituation())
                && !ValidationHelper.isNullOrEmpty(getEstateSituationRelationshipList())
                && (ValidationHelper.isNullOrEmpty(getEditRelationship())
                && getEstateSituationRelationshipList().stream().anyMatch(r -> getEstatePropertyType().equals(r.getType())))) {
            addRequiredFieldException("form:estatePropertyType");
            return;
        }
        Regime regime = null;
        if(!ValidationHelper.isNullOrEmpty(getEstateSelectedRegime())){
            try {
                regime = DaoManager.get(Regime.class, getEstateSelectedRegime());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        if(!ValidationHelper.isNullOrEmpty(getEditRelationship())) {
            RelationshipEditInTableWrapper relationshipEditInTableWrapper =
                    getEstateSituationRelationshipList().stream()
                            .filter(r -> r.getId().equals(getEditRelationship().getId()))
                            .findAny()
                            .orElse(null);
            relationshipEditInTableWrapper.setQuote1(getEstateQuote1());
            relationshipEditInTableWrapper.setQuote2(getEstateQuote2());
            relationshipEditInTableWrapper.setType(getEstatePropertyType());
            relationshipEditInTableWrapper.setRegime(regime);
        }else {
            if(ValidationHelper.isNullOrEmpty(getEstateSituationRelationshipList())){
                setEstateSituationRelationshipList(new ArrayList<>());
            }
            getEstateSituationRelationshipList().add(new RelationshipEditInTableWrapper(
                    getEstateQuote1(), getEstateQuote2(), getEstatePropertyType(), getExamRequest().getSubject(), regime));
            getCurrentEstateSituation().setEstateSituationRelationshipList(getEstateSituationRelationshipList());
        }
        setEstateSelectedRegime(null);
        setCurrentComment(null);
        setComment(new Comment());
        setSelectedId(null);
        setEstateQuote1(null);
        setEstateQuote2(null);
        setEstatePropertyType(null);
        setEditRelationship(null);
    }

    public void editAllRelationShip() {
        this.setEstateQuote1(this.getEditRelationship().getQuote1());
        this.setEstateQuote2(this.getEditRelationship().getQuote2());
        this.setEstatePropertyType(this.getEditRelationship().getType());
        if(!ValidationHelper.isNullOrEmpty(this.getEditRelationship().getRegime())){
            this.setEstateSelectedRegime(this.getEditRelationship().getRegime().getId());
        }else
            this.setEstateSelectedRegime(null);
    }

    public void deleteAllRelationship() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        cleanValidation();
        if(!ValidationHelper.isNullOrEmpty(getDeleteRelationship())) {
            RelationshipEditInTableWrapper relationshipEditInTableWrapper =
                    getEstateSituationRelationshipList().stream()
                            .filter(r -> r.getId().equals(getDeleteRelationship().getId()))
                            .findAny()
                            .orElse(null);
            relationshipEditInTableWrapper.setToDelete(Boolean.TRUE);
            Property property = DaoManager.get(Property.class, getCurrentProperty().getId());
            relationshipEditInTableWrapper.save(property);
        }
        setDeleteRelationship(null);
    }


    public void openRequestEditor() {
        RedirectHelper.goTo(PageTypes.REQUEST_TEXT_EDIT, getEntityEditId(), true);
    }

    public void openMailManagerEditor(boolean doCheck) throws PersistenceBeanException {
        Request request = getEntity().getRequest();
        WLGInbox mail = (request == null) ? null :
                request.getMail();

        if (doCheck && mail != null && mail.getId() != null) {
            try {
                List<Request> requests =
                        DaoManager.load(Request.class, new Criterion[]{
                                Restrictions.ne("id", request.getId()),
                                Restrictions.eq("mail.id", mail.getId()),
                                Restrictions.or(
                                        Restrictions.eq("stateId", RequestState.INSERTED.getId()),
                                        Restrictions.eq("stateId", RequestState.IN_WORK.getId()),
                                        Restrictions.eq("stateId", RequestState.SUSPENDED.getId())
                                )
                        });

                if (!requests.isEmpty()) {
                    executeJS("PF('requestsStillBeingWorkedOnDialogWV').show();");
                    return;
                }
            } catch (Exception e) {
                log.info(e);
                return;
            }
        }

        getEntity().setEmailOpened(true);
        DaoManager.refresh(getEntity().getRequest());
        DaoManager.save(getEntity(), true);
        getExamRequest().setRequestPrint(getEntity());
        DaoManager.save(getExamRequest(), true);
        RedirectHelper.goToMailEditRequest(getRequestId(), getExamRequest().getMail() != null
                ? MailEditType.REQUEST_REPLY_ALL : MailEditType.REQUEST);
    }

    public void manageRequest() {
        RedirectHelper.goToOnlyView(PageTypes.REQUEST_EDIT, getExamRequest().getId());
    }

    public void setEntityEditId(Long entityEditId) {
        this.getViewState().put("entityEditId", entityEditId);
    }

    public Long getEntityEditId() {
        return (Long) this.getViewState().get("entityEditId");
    }

    public String getExternalRequestFrom() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        User createRequestUser = DaoManager.get(User.class, getExamRequest().getCreateUserId());
        String beginning = "RICHIESTA ESTERNA DA ";
        String body = createRequestUser.getFullname() + " (" + (createRequestUser.getClient() == null
                ? "" : createRequestUser.getClient().toString()) + ")";
        return beginning + body;
    }

    public boolean getCreatedByExternalUser() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getExamRequest())) {
            User user = DaoManager.get(User.class, getExamRequest().getCreateUserId());
            for (Role role : user.getRoles()) {
                if (RoleTypes.EXTERNAL.equals(role.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String generateXMLFileName() {
        StringBuilder buffer = new StringBuilder();
        if(!ValidationHelper.isNullOrEmpty(getEntity().getRequest().getTranscriptionActId().getTypeEnum())) {
            if(getEntity().getRequest().getTranscriptionActId().getTypeEnum().equals(TypeActEnum.TYPE_T)) {
                buffer.append("TR");
            }else if(getEntity().getRequest().getTranscriptionActId().getTypeEnum().equals(TypeActEnum.TYPE_I)){
                buffer.append("IS");
            }else
                buffer.append("ANN");

            buffer.append("_");
            if(!ValidationHelper.isNullOrEmpty(getEntity().getRequest().getTranscriptionActId().getSectionA())) {
                if(!ValidationHelper.isNullOrEmpty(getEntity().getRequest().getTranscriptionActId().getSectionA().getNumberDirectory())) {
                    buffer.append(getEntity().getRequest().getTranscriptionActId().getSectionA().getNumberDirectory());
                    buffer.append("_");
                }
                if(!ValidationHelper.isNullOrEmpty(getEntity().getRequest().getTranscriptionActId().getSectionA().getTitleDate())) {
                    buffer.append(DateTimeHelper.toFormatedString(getEntity().getRequest().getTranscriptionActId().getSectionA().getTitleDate(),
                            DateTimeHelper.getXmlDatePattert(), Locale.ITALY));
                    buffer.append("_");
                }
            }
            if(!ValidationHelper.isNullOrEmpty(getEntity().getRequest().getTranscriptionActId().getReclamePropertyService())) {
                buffer.append(getEntity().getRequest().getTranscriptionActId().getReclamePropertyService());
            }
        }

        return buffer.toString();
    }

    public void updateNationalCost() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {

        Request request = DaoManager.get(Request.class, new Criterion[]{
                Restrictions.eq("id", getRequestId())});

        if(!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry()) &&
                !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getNational()) &&
                request.getAggregationLandChargesRegistry().getNational()) {

            getCostManipulationHelper().setIncludeNationalCost(false);
            executeJS("PF('includeNationalCost2DialogWV').show();");
            return;

        }
        if(!ValidationHelper.isNullOrEmpty(getCostManipulationHelper().getIncludeNationalCost())
                && getCostManipulationHelper().getIncludeNationalCost()) {
            if(!ValidationHelper.isNullOrEmpty(request.getMail())) {
                List<Request> requestsWithSameMailId = DaoManager.load(Request.class,
                        new Criterion[] {Restrictions.and(Restrictions.eq("mail.id", request.getMail().getId()),
                                Restrictions.eq("subject.id", request.getSubject().getId()))
                        });
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
                List<Long> existingIds = ListUtils
                        .emptyIfNull(getRequestDocuments()).stream()
                        .map(Document::getId)
                        .collect(Collectors.toList());

                getRequestNonSaleDocuments()
                        .stream()
                        .map(Document::getId)
                        .forEach(existingIds::add);

                getRequestSaleDocuments()
                        .stream()
                        .map(Document::getId)
                        .forEach(existingIds::add);

                for(FormalityView formality: formalityPDFList) {
                    if(!ValidationHelper.isNullOrEmpty(formality.getDocumentId())
                            && !existingIds.contains(formality.getDocumentId())) {
                        Document otherDoc = DaoManager.get(Document.class, formality.getDocumentId());
                        if(!getOtherDocuments().contains(otherDoc)){
                            if (Objects.equals(otherDoc.getTypeId(), DocumentType.FORMALITY.getId())){
                                getOtherDocuments().add(otherDoc);
                            }

                        }
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void renderFormalityDetails() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        if(!ValidationHelper.isNullOrEmpty(getSelectedFormalityId())) {
            Formality formality = DaoManager.get(Formality.class, getSelectedFormalityId());


            if(!ValidationHelper.isNullOrEmpty(formality)){
                List<Formality> formalities = new ArrayList<>();
                formalities.add(formality);
                EstateSituation situation = new EstateSituation();
                situation.setRequest(getExamRequest());
                setGravamiEstateSituations(new LinkedList<>());
                if(!ValidationHelper.isNullOrEmpty(formalities)){
                    List<Long> listIds = EstateSituationHelper.getIdSubjects(getExamRequest());
                    List<Subject> presumableSubjects = EstateSituationHelper.getListSubjects(listIds);
                    List<Subject> unsuitableSubjects = SubjectHelper.deleteUnsuitable(presumableSubjects, formalities);
                    presumableSubjects.removeAll(unsuitableSubjects);
                    presumableSubjects.add(getExamRequest().getSubject());
                    for (Formality f : formalities) {
                        List<Property> properties = formality.loadPropertiesByRelationship(presumableSubjects);
                        getGravamiEstateSituations().add(new SalesEstateSituationEditTableWrapper(formality,
                                properties, situation));
                    }
                }
            }
        }
        setFormalityDetailsRendered(Boolean.TRUE);

    }

    public void reCalculateOMI() throws PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        for (EstateSituationEditInTableWrapper situation : getEstateSituations()) {
            for (PropertyEditInTableWrapper propertyEditInTableWrapper : situation.getPropertyList()) {
                if(!ValidationHelper.isNullOrEmpty(propertyEditInTableWrapper.isCalculateOmiValue())
                        && propertyEditInTableWrapper.isCalculateOmiValue()){
                    Property property = DaoManager.get(Property.class, propertyEditInTableWrapper.getId());
                    if(!ValidationHelper.isNullOrEmpty(property.getCity()) &&
                            !ValidationHelper.isNullOrEmpty(property.getCategoryCode())){
                        String code = OMIHelper.getCode(property.getCategoryCode());
                        List<OmiValue> omiValues = DaoManager.load(OmiValue.class, new Criterion[]{
                                Restrictions.eq("zone", property.getZone()),
                                Restrictions.eq("cityCfis", property.getCity().getCfis()),
                                Restrictions.eq("categoryCode", Long.parseLong(code))
                        });
                        if(ValidationHelper.isNullOrEmpty(omiValues)){
                            OmiValue omiValue = new OmiValue();
                            omiValue.setCategoryCode(Long.parseLong(OMIHelper.getCode(property.getCategoryCode())));
                            omiValue.setCityCfis(property.getCity().getCfis());
                            omiValue.setCityDescription(property.getCity().getDescription());
                            omiValue.setComprMin(propertyEditInTableWrapper.getMinimumValue());
                            omiValue.setComprMax(propertyEditInTableWrapper.getMaximumValue());
                            omiValue.setZone(property.getZone());
                            omiValue.setState("NORMALE");
                            DaoManager.save(omiValue, true);
                        }
                        propertyEditInTableWrapper.reCalculateOMI();
                    }


                }
            }
        }
    }

    public final void onTabChange(final TabChangeEvent event) {
        TabView tv = (TabView) event.getComponent();
        this.activeTabIndex = tv.getActiveIndex();
        //SessionHelper.put("activeTabIndex", activeTabIndex);
    }

    public void loadInvoiceDialogData() throws IllegalAccessException, PersistenceBeanException  {
        List<PaymentInvoice> paymentInvoicesList = DaoManager.load(PaymentInvoice.class, new Criterion[] {Restrictions.isNotNull("date")}, new Order[]{
                Order.desc("date")});
        setPaymentInvoices(paymentInvoicesList);
        double totalImport = 0.0;
        for(PaymentInvoice paymentInvoice : paymentInvoicesList) {
            totalImport = totalImport + paymentInvoice.getPaymentImport().doubleValue();
        }
    }

    public String getEditText() {
        return editText;
    }

    public void setEditText(String editText) {
        this.editText = editText;
    }

    public Long getSelectedTemplateId() {
        return selectedTemplateId;
    }

    public void setSelectedTemplateId(Long selectedTemplateId) {
        this.selectedTemplateId = selectedTemplateId;
    }

    public Long getPdfZoomValue() {
        return pdfZoomValue;
    }

    public void setPdfZoomValue(Long pdfZoomValue) {
        this.pdfZoomValue = pdfZoomValue;
    }

    public List<SelectItem> getTemplates() {
        return templates;
    }

    public void setTemplates(List<SelectItem> templates) {
        this.templates = templates;
    }

    public Request getExamRequest() {
        return examRequest;
    }

    public void setExamRequest(Request examRequest) {
        this.examRequest = examRequest;
    }

    public LazyDataModel<Request> getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(LazyDataModel<Request> lazyModel) {
        this.lazyModel = lazyModel;
    }

    public List<EstateSituationEditInTableWrapper> getEstateSituations() {
        return estateSituations;
    }

    public void setEstateSituations(List<EstateSituationEditInTableWrapper> estateSituations) {
        this.estateSituations = estateSituations;
    }

    public Boolean getShowEstateTable() {
        return showEstateTable;
    }

    public void setShowEstateTable(Boolean showEstateTable) {
        this.showEstateTable = showEstateTable;
    }

    public List<EstateSituationEditInTableWrapper> getOtherEstateSituations() {
        return otherEstateSituations;
    }

    public void setOtherEstateSituations(List<EstateSituationEditInTableWrapper> otherEstateSituations) {
        this.otherEstateSituations = otherEstateSituations;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public List<SelectItem> getCategories() {
        return categories;
    }

    public void setCategories(List<SelectItem> categories) {
        this.categories = categories;
    }

    public List<SelectItem> getEstateFormalityTypes() {
        return estateFormalityTypes;
    }

    public void setEstateFormalityTypes(List<SelectItem> estateFormalityTypes) {
        this.estateFormalityTypes = estateFormalityTypes;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }

    public String getQuote1() {
        return quote1;
    }

    public void setQuote1(String quote1) {
        this.quote1 = quote1;
    }

    public String getQuote2() {
        return quote2;
    }

    public void setQuote2(String quote2) {
        this.quote2 = quote2;
    }

    public PropertyTypeEnum getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyTypeEnum propertyType) {
        this.propertyType = propertyType;
    }

    public List<SelectItem> getPropertyTypeList() {
        return propertyTypeList;
    }

    public void setPropertyTypeList(List<SelectItem> propertyTypeList) {
        this.propertyTypeList = propertyTypeList;
    }

    public List<FormalityEditInTableWrapper> getEstateSituationFormalities() {
        return estateSituationFormalities;
    }

    public void setEstateSituationFormalities(List<FormalityEditInTableWrapper> estateSituationFormalities) {
        this.estateSituationFormalities = estateSituationFormalities;
    }

    public RequestTextEditCommentType getCurrentComment() {
        return currentComment;
    }

    public void setCurrentComment(RequestTextEditCommentType currentComment) {
        this.currentComment = currentComment;
    }

    public List<Document> getRequestDocuments() {
        return requestDocuments;
    }

    public void setRequestDocuments(List<Document> requestDocuments) {
        this.requestDocuments = requestDocuments;
    }

    public PropertyEditInTableWrapper getCurrentProperty() {
        return currentProperty;
    }

    public void setCurrentProperty(PropertyEditInTableWrapper currentProperty) {
        this.currentProperty = currentProperty;
    }

    public List<RequestConservatory> getRequestConservatoryList() {
        return requestConservatoryList;
    }

    public void setRequestConservatoryList(List<RequestConservatory> requestConservatoryList) {
        this.requestConservatoryList = requestConservatoryList;
    }

    public Date getConservatoryMinDate() {
        return conservatoryMinDate;
    }

    public void setConservatoryMinDate(Date conservatoryMinDate) {
        this.conservatoryMinDate = conservatoryMinDate;
    }

    public Date getConservatoryMaxDate() {
        return conservatoryMaxDate;
    }

    public void setConservatoryMaxDate(Date conservatoryMaxDate) {
        this.conservatoryMaxDate = conservatoryMaxDate;
    }

    public String getRequestCostForced() {
        return requestCostForced;
    }

    public void setRequestCostForced(String requestCostForced) {
        this.requestCostForced = requestCostForced;
    }

    public String getRequestNumberActUpdate() {
        return requestNumberActUpdate;
    }

    public void setRequestNumberActUpdate(String requestNumberActUpdate) {
        this.requestNumberActUpdate = requestNumberActUpdate;
    }

    public CostManipulationHelper getCostManipulationHelper() {
        return costManipulationHelper;
    }

    public void setCostManipulationHelper(CostManipulationHelper costManipulationHelper) {
        this.costManipulationHelper = costManipulationHelper;
    }

    public String getTypeFormalityNotConfigureMessage() {
        return typeFormalityNotConfigureMessage;
    }

    public void setTypeFormalityNotConfigureMessage(String typeFormalityNotConfigureMessage) {
        this.typeFormalityNotConfigureMessage = typeFormalityNotConfigureMessage;
    }

    public Long getLastAppliedTemplate() {
        return lastAppliedTemplate;
    }

    public void setLastAppliedTemplate(Long lastAppliedTemplate) {
        this.lastAppliedTemplate = lastAppliedTemplate;
    }

    public RelationshipEditInTableWrapper getEditRelationship() {
        return editRelationship;
    }

    public void setEditRelationship(RelationshipEditInTableWrapper editRelationship) {
        this.editRelationship = editRelationship;
    }

    public RelationshipEditInTableWrapper getDeleteRelationship() {
        return deleteRelationship;
    }

    public void setDeleteRelationship(RelationshipEditInTableWrapper deleteRelationship) {
        this.deleteRelationship = deleteRelationship;
    }

    public String getNumberActDifference() {
        return numberActDifference;
    }

    public void setNumberActDifference(String numberActDifference) {
        this.numberActDifference = numberActDifference;
    }

    public List<Subject> getSubjectsToSelect() {
        return subjectsToSelect;
    }

    public void setSubjectsToSelect(List<Subject> subjectsToSelect) {
        this.subjectsToSelect = subjectsToSelect;
    }

    public Subject getSelectedSubjectToGenerate() {
        return selectedSubjectToGenerate;
    }

    public void setSelectedSubjectToGenerate(Subject selectedSubjectToGenerate) {
        this.selectedSubjectToGenerate = selectedSubjectToGenerate;
    }

    public Boolean getCalledByApplicaButton() {
        return calledByApplicaButton;
    }

    public void setCalledByApplicaButton(Boolean calledByApplicaButton) {
        this.calledByApplicaButton = calledByApplicaButton;
    }

    public Date getRequestEndDate() {
        return requestEndDate;
    }

    public void setRequestEndDate(Date requestEndDate) {
        this.requestEndDate = requestEndDate;
    }

    public String getDatafromPropertyText() {
        return datafromPropertyText;
    }

    public void setDatafromPropertyText(String datafromPropertyText) {
        this.datafromPropertyText = datafromPropertyText;
    }

    public int getEstateSituationId() {
        return estateSituationId;
    }

    public void setEstateSituationId(int estateSituationId) {
        this.estateSituationId = estateSituationId;
    }

    public String getRequestCommentCertification() {
        return requestCommentCertification;
    }

    public void setRequestCommentCertification(String requestCommentCertification) {
        this.requestCommentCertification = requestCommentCertification;
    }

    public Date getRequestEndDateMax() {
        return requestEndDateMax;
    }

    public void setRequestEndDateMax(Date requestEndDateMax) {
        this.requestEndDateMax = requestEndDateMax;
    }

    public Long getPreviousStateId() {
        return previousStateId;
    }

    public void setPreviousStateId(Long previousStateId) {
        this.previousStateId = previousStateId;
    }

    public Boolean getDateConfimed() {
        return dateConfimed;
    }

    public void setDateConfimed(Boolean dateConfimed) {
        this.dateConfimed = dateConfimed;
    }

    public Boolean getHideExtraCost() {
        return hideExtraCost;
    }

    public List<Document> getOtherDocuments() {
        return otherDocuments;
    }

    public void setOtherDocuments(List<Document> otherDocuments) {
        this.otherDocuments = otherDocuments;
    }

    public List<String> getEstateFormalitySalesMessage() {
        return estateFormalitySalesMessage;
    }

    public void setEstateFormalitySalesMessage(List<String> estateFormalitySalesMessage) {
        this.estateFormalitySalesMessage = estateFormalitySalesMessage;
    }

    public Boolean getShowSalesSection() {
        return showSalesSection;
    }

    public void setShowSalesSection(Boolean showSalesSection) {
        this.showSalesSection = showSalesSection;
    }

    public List<SalesEstateSituationEditTableWrapper> getSalesOtherEstateSituations() {
        return salesOtherEstateSituations;
    }

    public void setSalesOtherEstateSituations(List<SalesEstateSituationEditTableWrapper> salesOtherEstateSituations) {
        this.salesOtherEstateSituations = salesOtherEstateSituations;
    }

//    private void addMenuItem(String value) {
//        DefaultMenuItem menuItem = new DefaultMenuItem(value);
//
//        menuItem.setCommand("#{requestTextEditBean.goToTab(" +
//                getTopMenuModel().getElements().size() + ")}");
//        menuItem.setUpdate("form");
//
//        getTopMenuModel().addElement(menuItem);
//    }


//    private void generateMenuModel() {
//        setTopMenuModel(new DefaultMenuModel());
//        if (isMultipleCreate()) {
//            addMenuItem(ResourcesHelper.getString("requestTextEditDataTab"));
////            addMenuItem(ResourcesHelper.getString("requestTextEditNoteTab"));
////            addMenuItem(ResourcesHelper.getString("requestTextEditPaymentsTab"));
////            addMenuItem(ResourcesHelper.getString("requestTextEditAttachmentsTab"));
////            addMenuItem(ResourcesHelper.getString("requestTextEditEmailTab"));
//        } else {
//            addMenuItem(ResourcesHelper.getString("requestTextEditDataTab"));
//            if (!ValidationHelper.isNullOrEmpty(getInputCardList())) {
//                getInputCardList()
//                        .forEach(card -> addMenuItem(card.getName().toUpperCase()));
//            }
////            addMenuItem(ResourcesHelper.getString("requestTextEditNoteTab"));
////            addMenuItem(ResourcesHelper.getString("requestTextEditPaymentsTab"));
////            addMenuItem(ResourcesHelper.getString("requestTextEditAttachmentsTab"));
////            addMenuItem(ResourcesHelper.getString("requestTextEditEmailTab"));
//        }
//    }

    public void setMaxInvoiceNumber() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        LocalDate currentdate = LocalDate.now();
        int currentYear = currentdate.getYear();

        Long lastInvoiceNumber = 0l;
        try {
            lastInvoiceNumber = (Long) DaoManager.getMax(Invoice.class, "id",
                    new Criterion[]{});
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        if(lastInvoiceNumber == null)
            lastInvoiceNumber = 0l;
        String invoiceNumber = (lastInvoiceNumber+1) + "-" + currentYear + "-FE";
        setInvoiceNumber(invoiceNumber);
    }

    public void sendInvoice() {
        cleanValidation();

        if(ValidationHelper.isNullOrEmpty(getSelectedPaymentTypeId())){
            addRequiredFieldException("form:paymentType");
            setValidationFailed(true);
        }

        if(ValidationHelper.isNullOrEmpty(getInvoiceItemAmount())){
            addRequiredFieldException("form:quantita");
            setValidationFailed(true);
        }

        if(ValidationHelper.isNullOrEmpty(getSelectedTaxRateId())){
            addRequiredFieldException("form:invoiceVat");
            setValidationFailed(true);
        }

        if (getValidationFailed()){
            executeJS("PF('invoiceErrorDialogWV').show();");
            return;
        }

        try {
            Invoice invoice = new Invoice();
            invoice.setDocumentType(getDocumentType());
            invoice.setClient(getExamRequest().getClient());
            invoice.setDate(getInvoiceDate());
            invoice.setInvoiceNumber(getInvoiceNumber());
            if(!ValidationHelper.isNullOrEmpty(getSelectedPaymentTypeId()))
                invoice.setPaymentType(DaoManager.get(PaymentType.class, getSelectedPaymentTypeId()));

            if(!ValidationHelper.isNullOrEmpty(getVatCollectabilityId()))
                invoice.setVatCollectability(VatCollectability.getById(getVatCollectabilityId()));
            invoice.setNotes(getInvoiceNote());
            invoice.setDocumentType(getDocumentType());
            InvoiceItem invoiceItem = new InvoiceItem();
            if(!ValidationHelper.isNullOrEmpty(getExamRequest())
                    && !ValidationHelper.isNullOrEmpty(getExamRequest().getSubject())){
                invoiceItem.setSubject(getExamRequest().getSubject().toString());
                invoiceItem.setAmount(getInvoiceItemAmount());
                if(!ValidationHelper.isNullOrEmpty(getSelectedTaxRateId())){
                    invoiceItem.setTaxRate(DaoManager.get(TaxRate.class, getSelectedTaxRateId()));
                }
                // invoiceItem.setVat(getInvoiceItemVat());
                invoiceItem.setInvoiceTotalCost(getInvoiceTotalCost());
            }
            List<InvoiceItem> invoiceItems = new ArrayList<>();
            invoiceItems.add(invoiceItem);
            FatturaAPI fatturaAPI = new FatturaAPI();
            String xmlData = fatturaAPI.getDataForXML(invoice, invoiceItems);
            log.info("XMLDATA: " + xmlData);
            FatturaAPIResponse fatturaAPIResponse = fatturaAPI.callFatturaAPI(xmlData, log);
            log.info("API Call Done : " + fatturaAPIResponse.getDescription() + " " + "Response Code: " + fatturaAPIResponse.getReturnCode());
            if (fatturaAPIResponse != null && fatturaAPIResponse.getReturnCode() != -1) {
                getExamRequest().setStateId(RequestState.SENT_TO_SDI.getId());
                getExamRequest().setInvoice(invoice);
                DaoManager.save(invoice, true);
                invoiceItem.setInvoice(invoice);
                DaoManager.save(invoiceItem,true);
                DaoManager.save(getExamRequest(), true);
                executeJS("PF('invoiceDialogWV').hide();");
            } else {
                setApiError(ResourcesHelper.getString("sendInvoiceErrorMsg"));
                if (fatturaAPIResponse != null
                        && !ValidationHelper.isNullOrEmpty(fatturaAPIResponse.getDescription())) {

                    if (fatturaAPIResponse.getDescription().contains("already exists")) {
                        setApiError(ResourcesHelper.getString("sendInvoiceDuplicateMsg"));
                    } else
                        setApiError(fatturaAPIResponse.getDescription());
                }
                executeJS("PF('sendInvoiceErrorDialogWV').show();");
            }

        }catch(Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
            executeJS("PF('sendInvoiceErrorDialogWV').show();");
        }
    }

//    public MenuModel getTopMenuModel() {
//        return topMenuModel;
//    }
//
//    public void setTopMenuModel(MenuModel topMenuModel) {
//        this.topMenuModel = topMenuModel;
//    }

    public List<InputCard> getInputCardList() {
        return inputCardList;
    }

    public void setInputCardList(List<InputCard> inputCardList) {
        this.inputCardList = inputCardList;
    }

//    public int getActiveMenuTabNum() {
//        return activeMenuTabNum;
//    }
//
//    public void setActiveMenuTabNum(int activeMenuTabNum) {
//        this.activeMenuTabNum = activeMenuTabNum;
//    }

    public boolean isMultipleCreate() {
        return multipleCreate;
    }

    public void setMultipleCreate(boolean multipleCreate) {
        this.multipleCreate = multipleCreate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Double getInvoiceItemAmount() {
        return invoiceItemAmount;
    }

    public void setInvoiceItemAmount(Double invoiceItemAmount) {
        this.invoiceItemAmount = invoiceItemAmount;
    }

//    public Double getInvoiceItemVat() {
//        return invoiceItemVat;
//    }

   // public void setInvoiceItemVat(Double invoiceItemVat) {
      //  this.invoiceItemVat = invoiceItemVat;
   // }

    public Double getInvoiceTotalCost() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        if(!ValidationHelper.isNullOrEmpty(getExamRequest())){
            Request invoiceRequest = DaoManager.get(Request.class, getExamRequest().getId());
            if(!ValidationHelper.isNullOrEmpty(invoiceRequest) &&
                    !ValidationHelper.isNullOrEmpty(invoiceRequest.getTotalCostDouble())){
                setInvoiceTotalCost(Double.parseDouble(invoiceRequest.getTotalCostDouble()));
            }
        }
        return invoiceTotalCost;
    }

    public void setInvoiceTotalCost(Double invoiceTotalCost) {
        this.invoiceTotalCost = invoiceTotalCost;
    }

    public List<SelectItem> getVatAmounts() {
        return vatAmounts;
    }

    public void setVatAmounts(List<SelectItem> vatAmounts) {
        this.vatAmounts = vatAmounts;
    }

    public Double getTotalGrossAmount() throws PersistenceBeanException, InstantiationException, IllegalAccessException {

        Double totalGrossAmount = 0D;

        if(!ValidationHelper.isNullOrEmpty(getInvoiceTotalCost())){
            totalGrossAmount += getInvoiceTotalCost();
            if(!ValidationHelper.isNullOrEmpty(getSelectedTaxRateId())){
                TaxRate taxrate = DaoManager.get(TaxRate.class, getSelectedTaxRateId());
                if(!ValidationHelper.isNullOrEmpty(taxrate.getPercentage())){
                    totalGrossAmount += (getInvoiceTotalCost() * (taxrate.getPercentage().doubleValue()/100));
                }
            }
//            if(!ValidationHelper.isNullOrEmpty(getInvoiceItemVat())){
//                totalGrossAmount += (getInvoiceTotalCost() * (getInvoiceItemVat()/100));
//            }
        }
        return totalGrossAmount;
    }

    public List<SelectItem> getDocTypes() {
        return docTypes;
    }

    public void setDocTypes(List<SelectItem> docTypes) {
        this.docTypes = docTypes;
    }

    public Date getCompetence() {
        return competence;
    }

    public void setCompetence(Date competence) {
        this.competence = competence;
    }

    public List<SelectItem> getUms() {
        return ums;
    }

    public void setUms(List<SelectItem> ums) {
        this.ums = ums;
    }

    public List<SelectItem> getVatCollectabilityList() {
        return vatCollectabilityList;
    }

    public void setVatCollectabilityList(List<SelectItem> vatCollectabilityList) {
        this.vatCollectabilityList = vatCollectabilityList;
    }

    public Long getVatCollectabilityId() {
        return vatCollectabilityId;
    }

    public void setVatCollectabilityId(Long vatCollectabilityId) {
        this.vatCollectabilityId = vatCollectabilityId;
    }

    public List<SelectItem> getPaymentTypes() {
        return paymentTypes;
    }

    public void setPaymentTypes(List<SelectItem> paymentTypes) {
        this.paymentTypes = paymentTypes;
    }

    public Long getSelectedPaymentTypeId() {
        return selectedPaymentTypeId;
    }

    public void setSelectedPaymentTypeId(Long selectedPaymentTypeId) {
        this.selectedPaymentTypeId = selectedPaymentTypeId;
    }

    public Double getTotalVat() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        Double totalVat = 0D;

        if(!ValidationHelper.isNullOrEmpty(getInvoiceTotalCost())){
            if(!ValidationHelper.isNullOrEmpty(getSelectedTaxRateId())){
                TaxRate taxrate = DaoManager.get(TaxRate.class, getSelectedTaxRateId());
                if(!ValidationHelper.isNullOrEmpty(taxrate.getPercentage())){
                    totalVat += getInvoiceTotalCost() * (taxrate.getPercentage().doubleValue()/100);
                }
            }
        }
//
//        if(!ValidationHelper.isNullOrEmpty(getInvoiceTotalCost()) &&
//                !ValidationHelper.isNullOrEmpty(getInvoiceItemVat()) && getInvoiceItemVat() > 0)
//            totalVat += getInvoiceTotalCost() * (getInvoiceItemVat()/100);
        return totalVat;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public boolean isInvoiceSentStatus() {
        return invoiceSentStatus;
    }

    public void setInvoiceSentStatus(boolean invoiceSentStatus) {
        this.invoiceSentStatus = invoiceSentStatus;
    }

    public String getInvoiceNote() {
        return invoiceNote;
    }

    public void setInvoiceNote(String invoiceNote) {
        this.invoiceNote = invoiceNote;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getApiError() {
        return apiError;
    }

    public void setApiError(String apiError) {
        this.apiError = apiError;
    }


    public Boolean getBillinRequest() {
        return billinRequest;
    }

    public void setBillinRequest(Boolean billinRequest) {
        this.billinRequest = billinRequest;
    }

    public Boolean getShowRequestCost() {
        return showRequestCost;
    }

    public void setShowRequestCost(Boolean showRequestCost) {
        this.showRequestCost = showRequestCost;
    }

    public Long getSelectedTaxRateId() {
        return selectedTaxRateId;
    }

    public void setSelectedTaxRateId(Long selectedTaxRateId) {
        this.selectedTaxRateId = selectedTaxRateId;
    }

    public EstateSituationEditInTableWrapper getCurrentEstateSituation() {
        return currentEstateSituation;
    }

    public void setCurrentEstateSituation(EstateSituationEditInTableWrapper currentEstateSituation) {
        this.currentEstateSituation = currentEstateSituation;
    }

    public List<RelationshipEditInTableWrapper> getEstateSituationRelationshipList() {
        return estateSituationRelationshipList;
    }

    public void setEstateSituationRelationshipList(List<RelationshipEditInTableWrapper> estateSituationRelationshipList) {
        this.estateSituationRelationshipList = estateSituationRelationshipList;
    }

    public String getEstateQuote1() {
        return estateQuote1;
    }

    public void setEstateQuote1(String estateQuote1) {
        this.estateQuote1 = estateQuote1;
    }

    public String getEstateQuote2() {
        return estateQuote2;
    }

    public void setEstateQuote2(String estateQuote2) {
        this.estateQuote2 = estateQuote2;
    }

    public PropertyTypeEnum getEstatePropertyType() {
        return estatePropertyType;
    }

    public void setEstatePropertyType(PropertyTypeEnum estatePropertyType) {
        this.estatePropertyType = estatePropertyType;
    }

    public Long getEstateSelectedRegime() {
        return estateSelectedRegime;
    }

    public void setEstateSelectedRegime(Long estateSelectedRegime) {
        this.estateSelectedRegime = estateSelectedRegime;
    }
}
