package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.create.xls.CreateExcelRequestsReportHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.persistence.view.RequestView;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.BaseEntityPageBean;
import it.nexera.ris.web.beans.wrappers.GoodsServicesFieldWrapper;
import it.nexera.ris.web.beans.wrappers.logic.*;
import it.nexera.ris.web.common.ListPaginator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.chart.BarChartModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@ManagedBean(name = "invoiceDialogBean")
@ViewScoped
@Getter
@Setter
public class InvoiceDialogBean extends BaseEntityPageBean implements Serializable {

    private static final long serialVersionUID = 8295276742594934264L;

    private Long number;

    private String invoiceNumber;

    private Boolean showRequestTab;

    private int activeTabIndex;

    private List<FileWrapper> invoiceEmailAttachedFiles;

    private WLGExport courtesyInvoicePdf;

    private List<Request> invoicedRequests;

    private WLGExport excelInvoice;

    private WLGExport pdfInvoice;

    private Date invoiceDate;

    private boolean invoiceSentStatus;

    private List<SelectItem> invoiceClients;

    private Long selectedInvoiceClientId;

    private Client selectedInvoiceClient;

    private String documentType;

    private List<SelectItem> docTypes;

    private String clientAddressStreet;

    private String clientAddressPostalCode;

    private Long clientAddressCityId;

    private Long clientAddressProvinceId;

    private String clientNumberVAT;

    private String clientFiscalCode;

    private String clientMailPEC;

    private String clientAddressSDI;

    private List<SelectItem> clientProvinces;

    private List<SelectItem> clientAddressCities;

    private List<SelectItem> cities;

    private Long vatCollectabilityId;

    private List<SelectItem> vatCollectabilityList;

    private List<SelectItem> paymentTypes;

    private Long selectedPaymentTypeId;

    private String invoiceNote;

    private List<GoodsServicesFieldWrapper> goodsServicesFields;

    private InvoiceHelper invoiceHelper;

    private Double paymentAmount;

    private String paymentDescription;

    private List<InvoiceItem> selectedInvoiceItems;

    private List<Criterion> invoiceTabCriteria;

    private Integer downloadFileIndex;

    private static final String MAIL_RERLY_FOOTER = ResourcesHelper.getString("emailReplyFooter");

    private List<SelectItem> clients;

    private Long selectedClientId;

    private List<SelectItem> years;

    private Integer selectedYear;

    private Double monthJanFebAmount = getRandomNumber(10, 50);

    private Double monthMarAprAmount = getRandomNumber(100, 150);

    private Double monthMayJunAmount = getRandomNumber(200, 250);

    private Double monthJulAugAmount = getRandomNumber(100, 150);

    private Double monthSepOctAmount = getRandomNumber(200, 250);

    private Double monthNovDecAmount = getRandomNumber(50, 100);

    private List<Integer> turnoverPerMonth = new ArrayList<>();

    private List<String> turnoverPerCustomer = new ArrayList<>();

    public String[] months = new String[]{"Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"};

    private int quadrimesterStartIdx = 0;

    private int quadrimesterEndIdx = 3;

    private BarChartModel model;

    private List<Invoice> invoices;

    private Long filterInvoiceNumber;

    private Date dateFrom;

    private Date dateTo;

    private String filterAll;

    private List<SelectItem> managerClients;

    private Long managerClientFilterid;

    private List<SelectItem> offices;

    private Long selectedOfficeId;

    private String filterNotes;

    private String filterNdg;

    private String filterPractice;

    private List<SelectItem> companies;

    private Long selectedCompanyId;

    private List<PaymentInvoice> paymentInvoices;

    private Double amountToBeCollected;

    private Double totalPayments;

    private Request examRequest;

    private boolean multipleCreate;

    private String nameFilter;

    private String searchFiscalCode;

    private Long selectedSubjectClientId;

    private List<RequestStateWrapper> stateWrappers;

    private List<RequestState> selectedStates;

    private Long selectedRequestType;

    private List<SelectItem> requestTypes;

    private List<SelectItem> requestTypesForSelect;

    private List<RequestType> selectedRequestTypes;

    private Integer requestType;

    private List<RequestTypeFilterWrapper> requestTypeWrappers;

    private List<Service> selectedServices;

    private List<ServiceFilterWrapper> serviceWrappers;

    private ServiceFilterWrapper selectedServiceForFilter;

    private List<SelectItem> servicesForSelect;

    private Long fiduciaryClientFilterId;

    private Long subjectManagerClientFilterId;

    private List<SelectItem> fiduciaryClients;

    private List<SelectItem> landAggregations;

    private Long aggregationFilterId;

    private Date dateExpiration;

    private Date subjectDateFrom;

    private Date subjectDateTo;

    private Date dateFromEvasion;

    private Date dateToEvasion;

    private LazyDataModel<Property> lazySubjectModel;

    private ListPaginator paginator;

    private List<Document> requestDocuments;

    private Boolean showPrintButton;

    private List<InputCard> inputCardList;

    private Double invoiceItemAmount;

    private Double invoiceTotalCost;

    private List<SelectItem> vatAmounts;

    private Date competence;

    private List<SelectItem> ums;

    private String apiError;

    private boolean sendInvoice;

    private Boolean billinRequest;


    List<RequestView> filteredRequest;

    String invoiceErrorMessage;


    private Boolean createTotalCostSumDocumentRecord;

    private List<RequestView> allRequestViewsToModify;

    private List<Criterion> requestFilterRestrictions;

    private Long selectedState;

    private List<SelectItem> statesForSelect;

    private Long selectedUser;

    private List<SelectItem> usersForSelect;

    private List<UserFilterWrapper> userWrappers;

    private RequestTypeFilterWrapper selectedRequestTypeForFilter;

    private Long selectedRequestId;

    private Long downloadRequestId;

    private UserFilterWrapper selectedUserForFilter;

    private boolean printPdf;

    private String mailPdf;

    private String changeVar;

    private List<String> sendTo;

    private List<String> sendCC;

    private List<String> sendFrom;

    private String emailTo;

    private String emailCC;

    private String emailFrom;

    private String emailSubject;

    private String emailBodyToEditor;

    private static final String DELIM = ", ";

    private StreamedContent invoicePDFFile;

    private Invoice selectedInvoice;

    private Date paymentDate;

    private WLGInbox entity;

    @Override
    protected void onConstruct() {
        invoiceHelper = new InvoiceHelper();
        List<Client> clients = null;
        try {
            clients = DaoManager.load(Client.class, new Criterion[]{
                    Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                            Restrictions.isNull("deleted"))});
        } catch (PersistenceBeanException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        setClients(ComboboxHelper.fillList(clients.stream()
                .filter(c -> (
                                (ValidationHelper.isNullOrEmpty(c.getManager()) || !c.getManager()) &&
                                        (ValidationHelper.isNullOrEmpty(c.getFiduciary()) || !c.getFiduciary())
                        )
                ).sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList()), Boolean.TRUE));
    }

    public final void onTabChange(final TabChangeEvent event) throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException, IOException {
        TabView tv = (TabView) event.getComponent();
        if(!ValidationHelper.isNullOrEmpty(tv)){
            this.activeTabIndex = tv.getActiveIndex();
            if (activeTabIndex == 3) {
                Invoice invoice = DaoManager.get(Invoice.class, getNumber());
                if (ValidationHelper.isNullOrEmpty(invoice.getEmail())) {
                    attachInvoiceData(invoice);
                } else {
                    fillAttachedFiles(invoice.getEmail());
                }
            }
        }
    }

    private void attachInvoiceData(Invoice invoice) throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {
        setInvoiceEmailAttachedFiles(new ArrayList<>());
        String refrequest = "";
        String ndg = "";
        // WLGInbox baseMail = DaoManager.get(WLGInbox.class, getBaseMailId());
        if(ValidationHelper.isNullOrEmpty(invoice.getEmailFrom())) {
            attachCourtesyInvoicePdf(invoice);
            return;
        }
        WLGInbox baseMail = DaoManager.get(WLGInbox.class, invoice.getEmailFrom().getId());
        if (!ValidationHelper.isNullOrEmpty(baseMail)) {
            if (!ValidationHelper.isNullOrEmpty(baseMail)
                    && !ValidationHelper.isNullOrEmpty(baseMail.getRequests())) {
                setInvoicedRequests(baseMail.getRequests()
                        .stream()
                        .filter(r -> !ValidationHelper.isNullOrEmpty(r.getStateId()) &&
                                (r.getStateId().equals(RequestState.EVADED.getId()) || r.getStateId().equals(RequestState.SENT_TO_SDI.getId())))
                        .collect(Collectors.toList()));
            }
            refrequest = baseMail.getReferenceRequest();
            ndg = baseMail.getNdg();
        }
        if (ValidationHelper.isNullOrEmpty(getInvoicedRequests()))
            return;
        Request invoiceRequest = getInvoicedRequests().get(0);
        byte[] baos = getXlsBytes(refrequest, invoiceRequest, baseMail);
        if (!ValidationHelper.isNullOrEmpty(baos)) {
            excelInvoice = new WLGExport();
            Date currentDate = new Date();
            excelInvoice.setExportDate(currentDate);
            DaoManager.save(excelInvoice, true);
            String fileName = "Richieste_Invoice_" + DateTimeHelper.toFileDateWithMinutes(currentDate) + ".xls";
            String sb = excelInvoice.generateDestinationPath(fileName);
            File filePath = new File(sb);
            try {
                String str = FileHelper.writeFileToFolder(fileName,
                        filePath, baos);
                if (!new File(str).exists()) {
                    return;
                }
                LogHelper.log(log, excelInvoice.getId() + " " + str);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            DaoManager.save(excelInvoice, true);
            addAttachedFile(excelInvoice);
            attachInvoicePdf(baos);
        }
        attachCourtesyInvoicePdf(invoice);
    }


    private byte[] getXlsBytes(String refrequest, Request invoiceRequest, WLGInbox baseMail) {
        byte[] excelFile = null;
        try {
            ExcelDataWrapper excelDataWrapper = new ExcelDataWrapper();
            excelDataWrapper.setNdg(baseMail.getNdg());
            Document document = DaoManager.get(Document.class, new Criterion[]{
                    Restrictions.eq("mail.id", baseMail.getId())});

            if (ValidationHelper.isNullOrEmpty(document)) {
                document = new Document();
                document.setMail(baseMail);
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

            if (!ValidationHelper.isNullOrEmpty(baseMail.getClientInvoice())) {
                excelDataWrapper.setClientInvoice(DaoManager.get(Client.class, baseMail.getClientInvoice().getId()));
            }

            if (!ValidationHelper.isNullOrEmpty(baseMail.getManagers())) {
                excelDataWrapper.setManagers(baseMail.getManagers());
            }

            if (!ValidationHelper.isNullOrEmpty(baseMail.getClientFiduciary())) {
                excelDataWrapper.setClientFiduciary(DaoManager.get(Client.class, baseMail.getClientFiduciary().getId()));
            }

            if (!ValidationHelper.isNullOrEmpty(baseMail.getOffice())) {
                excelDataWrapper.setOffice(baseMail.getOffice().getDescription());
            }

            List<Request> filteredRequests = emptyIfNull(getInvoicedRequests()).stream().filter(r -> r.isDeletedRequest()).collect(Collectors.toList());
            excelFile = new CreateExcelRequestsReportHelper(true).convertMailUserDataToExcel(filteredRequests, document, excelDataWrapper);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return excelFile;
    }

    private void attachInvoicePdf(byte[] excelFile) {
        try {
            if(!ValidationHelper.isNullOrEmpty(excelFile)) {
                Date currentDate = new Date();
                String fileName = "Richieste_Invoice_" + DateTimeHelper.toFileDateWithMinutes(currentDate);
                String sofficeCommand =
                        ApplicationSettingsHolder.getInstance().getByKey(
                                ApplicationSettingsKeys.SOFFICE_COMMAND).getValue().trim();

                pdfInvoice = new WLGExport();
                pdfInvoice.setExportDate(currentDate);
                DaoManager.save(pdfInvoice, true);
                String sb = pdfInvoice.generateDestinationPath(fileName);
                File filePath = new File(sb);
                String str = "";
                try {
                    str = FileHelper.writeFileToFolder(fileName + ".xls",
                            filePath, excelFile);
                    if (!new File(str).exists()) {
                        return;
                    }
                    LogHelper.log(log, pdfInvoice.getId() + " " + str);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }

                Process p = Runtime.getRuntime().exec(new String[] { sofficeCommand, "--headless",
                        "--convert-to", "pdf","--outdir", sb, str });
                p.waitFor();

                String newPath = str.replaceFirst(".xls", ".pdf");
                FileHelper.delete(str);
                File convertedFile = new File(newPath);
                String pdfFileName = convertedFile.getName();
                pdfInvoice.generateDestinationPath(pdfFileName);
                DaoManager.save(pdfInvoice, true);
                LogHelper.log(log, pdfInvoice.getId() + " " + newPath);
                addAttachedFile(pdfInvoice);
            }
        }catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void fillAttachedFiles(WLGInbox inbox) throws PersistenceBeanException, IOException, InstantiationException, IllegalAccessException {
        setInvoiceEmailAttachedFiles(new ArrayList<>());
        List<WLGExport> exportList = DaoManager.load(WLGExport.class, new Criterion[]{
                Restrictions.eq("inbox", inbox)
        });
        for (WLGExport export : exportList) {
            addAttachedFile(export);
        }
    }

    private void addAttachedFile(WLGExport export) {
        if (export == null) {
            return;
        }
        if (getInvoiceEmailAttachedFiles() == null) {
            setInvoiceEmailAttachedFiles(new ArrayList<>());
        }
        if (new File(export.getDestinationPath()).exists()) {
            getInvoiceEmailAttachedFiles().add(new FileWrapper(export.getId(), export.getFileName(), export.getDestinationPath()));
        } else {
            LogHelper.log(log, "WARNING failed to attach file | no file on server: " + export.getDestinationPath());
        }

        invoiceEmailAttachedFiles = getInvoiceEmailAttachedFiles().stream().distinct().collect(Collectors.toList());
    }

    public Double getAllTotalLine() {
        return invoiceHelper.getAllTotalLine(getGoodsServicesFields());
    }

    public Double getTotalVat() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        Double total = 0D;
        if(!ValidationHelper.isNullOrEmpty(getGoodsServicesFields())) {
            for(GoodsServicesFieldWrapper wrapper: getGoodsServicesFields()) {
                if(!ValidationHelper.isNullOrEmpty(wrapper.getTotalLine())) {
                    if(!ValidationHelper.isNullOrEmpty(wrapper.getSelectedTaxRateId())) {
                        TaxRate taxrate = DaoManager.get(TaxRate.class, wrapper.getSelectedTaxRateId());
                        if(!ValidationHelper.isNullOrEmpty(taxrate.getPercentage())){
                            total += wrapper.getTotalLine().doubleValue() * (taxrate.getPercentage().doubleValue()/100);
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

    public void loadInvoiceDialogData(Invoice invoiceDb) throws IllegalAccessException, PersistenceBeanException, HibernateException, InstantiationException {
        setShowRequestTab(true);
        setActiveTabIndex(0);
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
                        Restrictions.isNull("deleted"))});
        setInvoiceClients(ComboboxHelper.fillList(clients.stream()
                .filter(c -> (
                                (ValidationHelper.isNullOrEmpty(c.getManager()) || !c.getManager()) &&
                                        (ValidationHelper.isNullOrEmpty(c.getFiduciary()) || !c.getFiduciary())
                        )
                ).sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList()), Boolean.TRUE));
        //setMaxInvoiceNumber();
        docTypes = new ArrayList<>();
        docTypes.add(new SelectItem("FE", "FATTURA"));
        setDocumentType("FE");
        competence = new Date();
        setVatCollectabilityList(ComboboxHelper.fillList(VatCollectability.class,
                false, false));
        paymentTypes = ComboboxHelper.fillList(invoiceDb.getClient().getPaymentTypeList(), Boolean.FALSE);
        setGoodsServicesFields(new ArrayList<>());
        setInvoiceDate(invoiceDb.getDate());
        setSelectedInvoice(invoiceDb);
        setSelectedClientId(invoiceDb.getClient().getId());
        setSelectedInvoiceClientId(invoiceDb.getClient().getId());
        setSelectedInvoiceClient(invoiceDb.getClient());
        if (invoiceDb.getClient().getSplitPayment() != null && invoiceDb.getClient().getSplitPayment())
            setVatCollectabilityId(VatCollectability.SPLIT_PAYMENT.getId());
        if (!ValidationHelper.isNullOrEmpty(invoiceDb.getVatCollectability()))
            setVatCollectabilityId(invoiceDb.getVatCollectability().getId());
        if (!ValidationHelper.isNullOrEmpty(invoiceDb.getPaymentType()))
            setSelectedPaymentTypeId(invoiceDb.getPaymentType().getId());
        if (!ValidationHelper.isNullOrEmpty(invoiceDb.getNotes()))
            setInvoiceNote(invoiceDb.getNotes());
        if (invoiceDb.getStatus().equals(InvoiceStatus.DELIVERED)) {
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
            wrapper.setInvoiceItemAmount(ValidationHelper.isNullOrEmpty(invoiceItem.getAmount()) ? 0.0 : invoiceItem.getAmount());
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
            if (!ValidationHelper.isNullOrEmpty(getEntity().getReferenceRequest()))
                reference = "- Rif. " + getEntity().getReferenceRequest() + " ";
            String ndg = "";
            if (!ValidationHelper.isNullOrEmpty(getEntity().getNdg()))
                ndg = "NDG: " + getEntity().getNdg();
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
    }

    private void getInvoiceClientData(Client client) throws HibernateException, IllegalAccessException, PersistenceBeanException {
        setClientAddressStreet(client != null ? client.getAddressStreet() : "");
        setClientAddressPostalCode(client != null ? client.getAddressPostalCode() : "");

        if(!ValidationHelper.isNullOrEmpty(client) && !ValidationHelper.isNullOrEmpty(client.getAddressProvinceId())) {
            setClientAddressProvinceId(client.getAddressProvinceId().getId());
            handleAddressProvinceChange();
        }
        if(!ValidationHelper.isNullOrEmpty(client) && !ValidationHelper.isNullOrEmpty(client.getAddressCityId())) {
            setClientAddressCityId(client.getAddressCityId().getId());
        }
        setClientNumberVAT(client != null ? client.getNumberVAT() : "");
        setClientFiscalCode(client != null ? client.getFiscalCode() : "");
        setClientMailPEC(client != null ? client.getMailPEC() : "");
        setClientAddressSDI(client != null ? client.getAddressSDI() : "");
    }

    public void handleAddressProvinceChange() throws HibernateException, PersistenceBeanException, IllegalAccessException {
        setClientAddressCities(ComboboxHelper.fillList(City.class, Order.asc("description"),
                new Criterion[] { Restrictions.eq("province.id", getClientAddressProvinceId()),
                        Restrictions.eq("external", Boolean.TRUE) }));
    }

    private String getCausal() {
        String causal = "";
        if (!ValidationHelper.isNullOrEmpty(getEntity())) {
            String reference = "";
            if(!ValidationHelper.isNullOrEmpty(getEntity().getReferenceRequest()))
                reference = "Rif. " + getEntity().getReferenceRequest();
            String ndg = "";
            if(!ValidationHelper.isNullOrEmpty(getEntity().getNdg()))
                ndg = "NDG: " + getEntity().getNdg();
            String uffico = "";
            if(!ValidationHelper.isNullOrEmpty(getEntity().getOffice()) && !ValidationHelper.isNullOrEmpty(getEntity().getOffice().getDescription()))
                uffico = "UFFICIO: " + getEntity().getOffice().getDescription();
            String gestore = "";
            if(!ValidationHelper.isNullOrEmpty(getEntity().getClient()) && !ValidationHelper.isNullOrEmpty(getEntity().getClient().getClientName()))
                gestore = "GESTORE: " + getEntity().getClient().getClientName();
            String fiduciario = "";
            if(!ValidationHelper.isNullOrEmpty(getEntity().getClientFiduciary()) && !ValidationHelper.isNullOrEmpty(getEntity().getClientFiduciary().getClientName()))
                fiduciario = "FIDUCIARIO: " + getEntity().getClientFiduciary().getClientName();
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

    public void loadDraftEmail() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        Invoice invoice = DaoManager.get(Invoice.class, getNumber());
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
            if(!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom())) {
                WLGInbox inbox = DaoManager.get(WLGInbox.class, invoice.getEmailFrom().getId());
                List<Client> managers = inbox.getManagers();
                List<ClientEmail> allEmailList = new ArrayList<>();
                CollectionUtils.emptyIfNull(managers).stream().forEach(manager -> {
                    if(!ValidationHelper.isNullOrEmpty(manager.getEmails()))
                        allEmailList.addAll(manager.getEmails());
                });
                sendTo = new LinkedList<>();
                CollectionUtils.emptyIfNull(allEmailList).stream().forEach(email -> {
                    if(!ValidationHelper.isNullOrEmpty(email.getEmail()))
                        sendTo.addAll(MailHelper.parseMailAddress(email.getEmail()));
                });
            }
        }
    }

    public Double getTotalGrossAmount() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        return invoiceHelper.getTotalGrossAmount(getGoodsServicesFields());
    }

    private double getRandomNumber(int min, int max) {
        return Math.random() * (max - min + 1) + min;
    }

    private GoodsServicesFieldWrapper createGoodsServicesFieldWrapper() throws IllegalAccessException, PersistenceBeanException {
        GoodsServicesFieldWrapper wrapper = new GoodsServicesFieldWrapper();
        ums = new ArrayList<>();
        ums.add(new SelectItem("pz", "PZ"));
        wrapper.setUms(ums);
        wrapper.setVatAmounts(ComboboxHelper.fillList(TaxRate.class, Order.asc("description"), new CriteriaAlias[]{}, new Criterion[]{
                Restrictions.eq("use", Boolean.TRUE)
        }, true, false, true));
        wrapper.setTotalLine(0D);
        return wrapper;
    }

    public void closeInvoiceDialog() {
        try {
            List<WLGExport> exports = DaoManager.load(WLGExport.class, new Criterion[]{
                    Restrictions.isNull("sourcePath")
            });
            if (!ValidationHelper.isNullOrEmpty(exports)) {
                for (WLGExport export : exports) {
                    FileHelper.delete(export.getDestinationPath());
                    DaoManager.remove(export, true);
                }
            }
            setInvoiceEmailAttachedFiles(new ArrayList<>());
            setActiveTabIndex(0);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static String getPdfRequestBody(Invoice invoice) throws PersistenceBeanException, IllegalAccessException {
        StringBuilder result = new StringBuilder();
        result.append("<div style=\"padding: 20px;\">");
        result.append("<h3>FATTURA ELETTRONICA</h3>");
        result.append("<table style=\"width: 720px;border-collapse: collapse;border: 1px solid lightgray;\">");
        result.append("<thead style=\"background-color: #E7E7E7;\">");
        result.append("<td style=\"border: 1px solid lightgray;padding: 10px;text-align: left;width: 50%;color: #888888;font-weight: bold;background-color: #E7E7E7;\">Mittente</td>");
        result.append("<td style=\"border: 1px solid lightgray;padding: 10px;text-align: left;width: 50%;color: #888888;font-weight: bold;background-color: #E7E7E7;\">Destinatario</td>");
        result.append("</thead>");
        result.append("<tbody>");
        result.append("<tr>");
        result.append("<td style=\"border: 2px solid lightgray;width: 50%;padding: 10px;line-height: 22px;\">");

        String comapnyName = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.COMPANY_NAME).getValue();
        if(ValidationHelper.isNullOrEmpty(comapnyName))
            comapnyName = "";
        result.append("<b>");
        result.append(comapnyName.toUpperCase());
        result.append("</b><br/>");
        String addressAppliant = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.ADDRESS).getValue();
        if(ValidationHelper.isNullOrEmpty(addressAppliant))
            addressAppliant = "";
        result.append(addressAppliant.toUpperCase());
        result.append("<br/>");
        String cityData = "";
        String provinceDescription = ApplicationSettingsHolder.getInstance().getByKey(
                ApplicationSettingsKeys.COMPANY_PROVINCE).getValue();
        if(!ValidationHelper.isNullOrEmpty(provinceDescription)) {
            List<Province> provinces = DaoManager.load(Province.class,
                    new Criterion[]{
                            Restrictions.eq("description", provinceDescription).ignoreCase()
                    });
            if(!ValidationHelper.isNullOrEmpty(provinces)){
                Province province = provinces.get(0);
                String cityString = ApplicationSettingsHolder.getInstance().getByKey(
                        ApplicationSettingsKeys.COMPANY_CITY).getValue();
                if(!ValidationHelper.isNullOrEmpty(cityString)){
                    List<String> cityDescriptions = Stream.of(cityString.split(",", -1))
                            .collect(Collectors.toList());

                    List<City> cities = DaoManager.load(City.class, new Criterion[]{
                            Restrictions.eq("province.id",province.getId()),
                            Restrictions.eq("external", Boolean.TRUE),
                            Restrictions.in("description", cityDescriptions)
                    },Order.asc("description"));
                    if(!ValidationHelper.isNullOrEmpty(cities)){
                        City city = cities.get(0);
                        if(!ValidationHelper.isNullOrEmpty(city)){
                            if(!ValidationHelper.isNullOrEmpty(city.getCap())){
                                cityData += city.getCap();
                            }
                            if(!ValidationHelper.isNullOrEmpty(city.getDescription())){
                                cityData += " - " + city.getDescription();
                            }

                            if(!ValidationHelper.isNullOrEmpty(province.getCode())){
                                cityData += " - " + province.getCode();
                            }
                            cityData += " - IT";
                        }
                    }
                }
            }
        }
        result.append(cityData);
        result.append("<br/>");
        String fiscalCodeAppliant = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.FISCAL_CODE).getValue();
        if(ValidationHelper.isNullOrEmpty(fiscalCodeAppliant))
            fiscalCodeAppliant = "";

        if(StringUtils.isNotBlank(fiscalCodeAppliant)){
            result.append("P.IVA: ");
        }
        result.append(fiscalCodeAppliant);
        result.append("<br/>");
        if(StringUtils.isNotBlank(fiscalCodeAppliant))
            result.append("Cod. Fiscale: ");
        result.append(fiscalCodeAppliant);
        result.append("<br/>");
        String emailFrom = "";
        List<String> emailsFrom = DaoManager.loadField(WLGServer.class, "login", String.class,
                new Criterion[]{Restrictions.eq("type", 15L)});
        if (!ValidationHelper.isNullOrEmpty(emailsFrom)) {
            emailFrom = emailsFrom.get(0);
        }
        result.append(emailFrom);
        result.append("<br/>");
        result.append("<br/>");
        result.append("<br/>");
        result.append("</td>");

        result.append("<td style=\"border: 2px solid lightgray;width: 50%;padding: 10px;line-height: 22px;\">");
        String clientName = "";
        String clientAddressStreet = "";
        String clientAddressHouseNumber = "";
        String clientPostal= "";
        String clientNumberVAT= "";
        String clientFiscalCode= "";
        String clientOffice= "";
        String clientPEC= "";
        if(!ValidationHelper.isNullOrEmpty(invoice)){
            if(!ValidationHelper.isNullOrEmpty(invoice.getClient())){
                if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getClientName())){
                    clientName = invoice.getClient().getClientName();
                }
                if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressStreet())){
                    clientAddressStreet = invoice.getClient().getAddressStreet();
                }
                if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressHouseNumber())){
                    clientAddressHouseNumber = invoice.getClient().getAddressHouseNumber();
                }
                if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressPostalCode())){
                    clientPostal = invoice.getClient().getAddressPostalCode();
                }
                if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressCityId()) &&
                        !ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressCityId().getDescription())){
                    clientPostal += " - " + invoice.getClient().getAddressCityId().getDescription();
                }
                if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressProvinceId()) &&
                        !ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressProvinceId().getCode())){
                    clientPostal += " - " + invoice.getClient().getAddressProvinceId().getCode();
                }
                clientPostal += " - IT";
                if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getNumberVAT())){
                    clientNumberVAT = invoice.getClient().getNumberVAT();
                }
                if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getFiscalCode())){
                    clientFiscalCode = invoice.getClient().getFiscalCode();
                }
                if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressSDI())){
                    clientOffice = invoice.getClient().getAddressSDI();
                }
                if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getMailPEC())){
                    clientPEC = invoice.getClient().getMailPEC();
                }
            }
        }
        result.append("<b>");
        result.append(clientName.toUpperCase());
        result.append("</b><br/>");
        result.append(clientAddressStreet);
        result.append("<br/>");
        result.append(clientAddressHouseNumber);
        result.append("<br/>");
        result.append(clientPostal);
        result.append("<br/>");
        if(StringUtils.isNotBlank(clientNumberVAT))
            result.append("P.IVA: ");
        result.append(clientNumberVAT);
        result.append("<br/>");
        if(StringUtils.isNotBlank(clientFiscalCode))
            result.append("Cod. Fiscale: ");
        result.append(clientFiscalCode);
        result.append("<br/>");
        if(StringUtils.isNotBlank(clientOffice))
            result.append("Codice Ufficio: ");
        result.append(clientOffice);
        result.append("<br/>");
        if(StringUtils.isNotBlank(clientPEC))
            result.append("PEC: ");
        result.append(clientPEC);
        result.append("<br/>");
        result.append("</td>");
        result.append("</tr>");
        result.append("</tbody>");
        result.append("</table>");

        result.append("<table style=\"width: 720px;border-collapse: collapse;border: 1px solid lightgray;margin-top: 20px;\">");
        result.append("<tbody>");
        result.append("<tr style=\"background-color: #E7E7E7;\">");
        result.append("<td colspan=\"4\" style=\"border: 1px solid lightgray;padding: 10px;text-align: left;width: 50%;color: #888888;background-color: #E7E7E7;font-weight: bold\">Dati Fattura</td>");
        result.append("</tr>");
        result.append("<tr style=\"background-color: #E7E7E7;\">");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 25%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Natura Documento</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 25%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Numero</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 25%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Data</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 25%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Importo Totale</td>");
        result.append("</tr>");
        result.append("<tr>");
        result.append("<td style=\"border: 2px solid lightgray;width: 25%;padding: 10px;line-height: 22px;\"><b>Fattura</b></td>");
        result.append("<td style=\"border: 2px solid lightgray;width: 25%;padding: 10px;line-height: 22px;\"><b>");
        result.append(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "");
        result.append("</b></td>");
        result.append("<td style=\"border: 2px solid lightgray;width: 25%;padding: 10px;line-height: 22px;\"><b>");
        result.append(invoice.getDate() != null ? DateTimeHelper.toFormatedString(invoice.getDate(), DateTimeHelper.getMySQLDatePattern()) : "");
        result.append("</b></td>");
        result.append("<td style=\"border: 2px solid lightgray;width: 25%;padding: 10px;line-height: 22px;\"><b>");
        result.append("EUR ");
        result.append(invoice.getTotalGrossAmount() != null ? invoice.getTotalGrossAmount().toString().replace(".", ",") : "");
        result.append("</b></td>");
        result.append("</tr>");
        result.append("</tbody>");
        result.append("<tr style=\"background-color: #E7E7E7;\">");
        result.append("<td colspan=\"4\" style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 25%;color: #478FCA;;background-color: #f3f3f3;\"><b>Causale</b></td>");
        result.append("</tr>");
        result.append("<tr>");
        result.append("<td colspan=\"4\" style=\"border: 2px solid lightgray;width: 25%;padding: 10px;line-height: 22px;\">");
        result.append(invoice.getNotes() != null ? invoice.getNotes() : "");
        result.append("</td>");
        result.append("</tr>");
        result.append("</table>");

        result.append("<table style=\"width: 720px;border-collapse: collapse;border: 1px solid lightgray;margin-top: 20px;\">");
        result.append("<tbody>");
        result.append("<tr>");
        result.append("<td colspan=\"2\" style=\"border: 1px solid lightgray;padding: 10px;text-align: left;width: 50%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Dati</td>");
        result.append("</tr>");
        result.append("<tr>");
        result.append("<td style=\"border: 2px solid lightgray;width: 25%;padding: 10px;line-height: 22px;\">");
        String ndg = "";
        String refrenceRequest = "";

        if(!ValidationHelper.isNullOrEmpty(invoice) && !ValidationHelper.isNullOrEmpty(invoice.getEmailFrom())){
            if(!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getNdg())){
                ndg = invoice.getEmailFrom().getNdg();
            }
            if(!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getReferenceRequest())){
                refrenceRequest = invoice.getEmailFrom().getReferenceRequest();
            }
        }
        if(StringUtils.isNotBlank(ndg))
            result.append("NDG: ");
        result.append(ndg);
        result.append("</td>");
        result.append("<td style=\"border: 2px solid lightgray;width: 25%;padding: 10px;line-height: 22px;\">");
        if(StringUtils.isNotBlank(refrenceRequest))
            result.append("Rif.: ");
        result.append(refrenceRequest);
        result.append("</td>");
        result.append("</tr>");
        result.append("</table>");

        result.append("<table style=\"width: 720px;border-collapse: collapse;border: 1px solid lightgray;margin-top: 20px;\">");
        result.append("<tbody>");
        result.append("<tr>");
        result.append("<td colspan=\"2\" style=\"border: 1px solid lightgray;padding: 10px;text-align: left;width: 50%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Dati cliente</td>");
        result.append("</tr>");
        result.append("<tr>");
        result.append("<td style=\"border: 2px solid lightgray;width: 25%;padding: 10px;line-height: 22px;\">");
        String mailClient = "";
        String mailTrust = "";
        String mailOffice = "";

        if(!ValidationHelper.isNullOrEmpty(invoice) && !ValidationHelper.isNullOrEmpty(invoice.getEmailFrom())){
            if(!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getManagers())){
                mailClient = invoice.getEmailFrom()
                        .getManagers().stream().map(Client::toString).collect(Collectors.joining(", "));
            }
            if(!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getClientFiduciary())){
                mailTrust = invoice.getEmailFrom().getClientFiduciary().toString();
            }
            if(!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getOffice()) &&
                    !ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getOffice().getDescription())){
                mailOffice = invoice.getEmailFrom().getOffice().toString();
            }
        }
        if(StringUtils.isNotBlank(mailClient))
            result.append("Gestore: ");
        result.append(mailClient);
        result.append("<br/>");
        if(StringUtils.isNotBlank(mailTrust))
            result.append("Fiduciario: ");
        result.append(mailTrust);
        result.append("</td>");
        result.append("<td style=\"border: 2px solid lightgray;width: 25%;padding: 10px;line-height: 22px;\">");
        if(StringUtils.isNotBlank(mailOffice))
            result.append("Ufficio: ");
        result.append(mailOffice);
        result.append("</td>");
        result.append("</tr>");
        result.append("</table>");

        result.append("<table style=\"width: 720px;border-collapse: collapse;border: 1px solid lightgray;margin-top: 20px;\">");
        result.append("<tbody>");
        result.append("<tr style=\"background-color: #E7E7E7;\">");
        result.append("<td colspan=\"6\" style=\"border: 1px solid lightgray;padding: 10px;text-align: left;width: 50%;color: #888888;background-color: #E7E7E7;font-weight: bold\">Dettaglio linee Fattura</td>");
        result.append("</tr>");
        result.append("<tr style=\"background-color: #E7E7E7;\">");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 35%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Descrizione</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 10%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">U.M.</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 10%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Q.tà</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 15%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Pr. Unitario</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 15%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Pr. Totale</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 15%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">IVA</td>");
        result.append("</tr>");
        if(!ValidationHelper.isNullOrEmpty(invoice)){
            List<InvoiceItem> items =
                    DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoice)});
            if(!ValidationHelper.isNullOrEmpty(items)){
                for(InvoiceItem item : items){
                    result.append("<tr>");
                    result.append("<td style=\"border: 2px solid lightgray;width: 35%;padding: 10px;line-height: 22px;\">");
                    if(!ValidationHelper.isNullOrEmpty(item.getDescription())){
                        result.append(item.getDescription());
                    }
                    result.append("</td>");
                    result.append("<td style=\"border: 2px solid lightgray;width: 10%;padding: 10px;line-height: 22px;\">");
                    result.append("pz");
                    result.append("</td>");
                    result.append("<td style=\"border: 2px solid lightgray;width: 10%;padding: 10px;line-height: 22px;\">");
                    if(!ValidationHelper.isNullOrEmpty(item.getAmount()))
                        result.append(item.getAmount());
                    result.append("</td>");
                    result.append("<td style=\"border: 2px solid lightgray;width: 15%;padding: 10px;line-height: 22px;\">");
                    if(!ValidationHelper.isNullOrEmpty(item.getInvoiceTotalCost()))
                        result.append(item.getInvoiceTotalCost());
                    result.append("</td>");
                    result.append("<td style=\"border: 2px solid lightgray;width: 15%;padding: 10px;line-height: 22px;\">");
                    if(!ValidationHelper.isNullOrEmpty(item.getAmount())
                            && !ValidationHelper.isNullOrEmpty(item.getInvoiceTotalCost())){
                        result.append(item.getInvoiceTotalCost() * item.getAmount());
                    }
                    result.append("</td>");
                    result.append("<td style=\"border: 2px solid lightgray;width: 15%;padding: 10px;line-height: 22px;\">");
                    if(!ValidationHelper.isNullOrEmpty(item.getTaxRate())
                            && !ValidationHelper.isNullOrEmpty(item.getTaxRate().getDescription())){
                        result.append(item.getTaxRate().getDescription());
                    }
                    result.append("</td>");
                    result.append("</tr>");
                }
            }
        }
        result.append("</table>");

        result.append("<table style=\"width: 720px;border-collapse: collapse;border: 1px solid lightgray;margin-top: 20px;\">");
        result.append("<tbody>");
        result.append("<tr style=\"background-color: #E7E7E7;\">");
        result.append("<td colspan=\"6\" style=\"border: 1px solid lightgray;padding: 10px;text-align: left;width: 50%;color: #888888;background-color: #E7E7E7;font-weight: bold\">Dati Riepilogo</td>");
        result.append("</tr>");
        result.append("<tr style=\"background-color: #E7E7E7;\">");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 35%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Natura/Esigibilità IVA</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 10%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Al. IVA</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 10%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Imponibile</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 15%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Imposta</td>");
        result.append("</tr>");
        if(!ValidationHelper.isNullOrEmpty(invoice)){
            List<InvoiceItem> items =
                    DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoice)});
            if(!ValidationHelper.isNullOrEmpty(items)){
                for(InvoiceItem item : items){
                    BigDecimal percentage = !ValidationHelper.isNullOrEmpty(item.getTaxRate()) && !ValidationHelper.isNullOrEmpty(item.getTaxRate().getPercentage()) ? item.getTaxRate().getPercentage() : BigDecimal.ZERO;
                    Double totalAmount = !ValidationHelper.isNullOrEmpty(item.getInvoiceTotalCost()) && !ValidationHelper.isNullOrEmpty(item.getAmount()) ? (item.getInvoiceTotalCost()*item.getAmount()) : 0;
                    result.append("<tr>");
                    result.append("<td style=\"border: 2px solid lightgray;width: 35%;padding: 10px;line-height: 22px;\">");
                    if(!ValidationHelper.isNullOrEmpty(item.getTaxRate()) && !ValidationHelper.isNullOrEmpty(item.getTaxRate().getDescription())){
                        result.append(item.getTaxRate().getDescription());
                    }
                    result.append("</td>");
                    result.append("<td style=\"border: 2px solid lightgray;width: 20%;padding: 10px;line-height: 22px;\">");
                    if(!percentage.equals(0))
                        result.append(percentage + " %");
                    result.append("</td>");
                    result.append("<td style=\"border: 2px solid lightgray;width: 25%;padding: 10px;line-height: 22px;\">");
                    if(!totalAmount.equals(0))
                        result.append(totalAmount);
                    result.append("</td>");
                    result.append("<td style=\"border: 2px solid lightgray;width: 20%;padding: 10px;line-height: 22px;\">");
                    if(!percentage.equals(0) &&!totalAmount.equals(0))
                        result.append(percentage.multiply(BigDecimal.valueOf(totalAmount)).divide(BigDecimal.valueOf(100)));
                    result.append("</td>");
                    result.append("</tr>");
                }
            }
        }
        result.append("</table>");

        // page-break-before: always;
        result.append("<table style=\"width: 720px;border-collapse: collapse;border: 1px solid lightgray;margin-top: 20px;\">");
        result.append("<tbody>");
        result.append("<tr style=\"background-color: #E7E7E7;\">");
        result.append("<td colspan=\"6\" style=\"border: 1px solid lightgray;padding: 10px;text-align: left;width: 50%;color: #888888;background-color: #E7E7E7;font-weight: bold\">Dati pagamento</td>");
        result.append("</tr>");
        result.append("<tr style=\"background-color: #E7E7E7;\">");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 20%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Condizioni Pagamento</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 10%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Modalità</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 15%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Data Scadenza</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 10%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Importo</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 15%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">Istituto</td>");
        result.append("<td style=\"border: 2px solid lightgray;padding: 10px;text-align: left;width: 30%;color: #478FCA;background-color: #f3f3f3;font-weight: bold\">IBAN</td>");
        result.append("</tr>");
        result.append("<tr>");
        result.append("<td colspan=\"6\" style=\"border: 1px solid lightgray;padding: 10px;text-align: left;font-weight: bold\">Pagamento completo</td>");
        result.append("</tr>");
        result.append("<tr>");
        result.append("<td style=\"border: 2px solid lightgray;width: 15%;padding: 10px;line-height: 22px;\"></td>");
        result.append("<td style=\"border: 2px solid lightgray;width: 15%;padding: 10px;line-height: 22px;\">");
        if(!ValidationHelper.isNullOrEmpty(invoice.getPaymentType()) && !ValidationHelper.isNullOrEmpty(invoice.getPaymentType().getDescription())) {
            result.append(invoice.getPaymentType().getDescription());
        }
        result.append("</td>");
        result.append("<td style=\"border: 2px solid lightgray;width: 15%;padding: 10px;line-height: 22px;\"></td>");
        result.append("<td style=\"border: 2px solid lightgray;width: 15%;padding: 10px;line-height: 22px;\">");
        if(!ValidationHelper.isNullOrEmpty(invoice.getTotalGrossAmount())) {
            result.append("EUR ");
            result.append(invoice.getTotalGrossAmount() != null ? invoice.getTotalGrossAmount().toString().replace(".", ",") : "");
        }
        result.append("</td>");
        result.append("<td style=\"border: 2px solid lightgray;width: 15%;padding: 10px;line-height: 22px;\">");
        if(!ValidationHelper.isNullOrEmpty(invoice.getPaymentType()) && !ValidationHelper.isNullOrEmpty(invoice.getPaymentType().getIstitutionName())) {
            result.append(invoice.getPaymentType().getIstitutionName());
        }
        result.append("</td>");
        result.append("<td style=\"border: 2px solid lightgray;width: 25%;padding: 10px;line-height: 22px;\">");
        if(!ValidationHelper.isNullOrEmpty(invoice.getPaymentType()) && !ValidationHelper.isNullOrEmpty(invoice.getPaymentType().getIban())) {
            result.append(invoice.getPaymentType().getIban());
        }
        result.append("</td>");
        result.append("</tr>");
        result.append("</tbody>");
        result.append("</table>");
        result.append("</div>");
        result.append("<pd4ml:footnote noref=\"true\">Copia della fattura elettronica disponibile nella Sua area riservata dell'Agenzia delle Entrate</pd4ml:footnote>");
        return result.toString();
    }

    public void attachCourtesyInvoicePdf(Invoice invoice) {
        try {
            String templatePath  = (new File(FileHelper.getRealPath(),
                    "resources" + File.separator + "layouts" + File.separator
                            + "Invoice" + File.separator + "InvoiceDocumentTemplate.docx")
                    .getAbsolutePath());

            Double imponibile = 0.0;
            Double totalIva = 0.0;
            Double ivaPercentage = 0.0;

            if(!ValidationHelper.isNullOrEmpty(invoice)) {
                List<InvoiceItem> items = DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoice)});
                for(InvoiceItem item : items) {
                    double total = 0.0;
                    double amount = 0.0;
                    double totalCost = 0.0;

                    if(item.getAmount() != null)
                        amount = item.getAmount();
                    if(item.getInvoiceTotalCost() != null)
                        totalCost = item.getInvoiceTotalCost();
                    if(amount != 0.0)
                        imponibile = imponibile + (amount * totalCost);
                    else
                        imponibile = imponibile + totalCost;
                    if(amount != 0.0)
                        total = amount * totalCost;
                    else
                        total = totalCost;
                    if(item.getVat() != null){
                        ivaPercentage = ivaPercentage + item.getVat();
                        totalIva = totalIva + ((item.getVat() * total)/100);
                    }
                }
                if(items.size() > 0)
                    ivaPercentage = ivaPercentage/ items.size();

                BigDecimal ivaPer = BigDecimal.valueOf(ivaPercentage);
                ivaPer = ivaPer.setScale(2, RoundingMode.HALF_UP);
                ivaPercentage = ivaPer.doubleValue();

                BigDecimal totIva = BigDecimal.valueOf(totalIva);
                totIva = totIva.setScale(2, RoundingMode.HALF_UP);
                totalIva = totIva.doubleValue();

                BigDecimal imponi = BigDecimal.valueOf(imponibile);
                imponi = imponi.setScale(2, RoundingMode.HALF_UP);
                imponibile = imponi.doubleValue();
                Date currentDate = new Date();
                String fileName = "Fattura_cortesia_"+getInvoiceNumber();

                String tempDir = FileHelper.getLocalTempDir();
                tempDir  += File.separator + UUID.randomUUID();
                FileUtils.forceMkdir(new File(tempDir));
                String tempDoc = tempDir +  File.separator +  fileName +".docx";

                try (XWPFDocument doc = new XWPFDocument(
                        Files.newInputStream(Paths.get(templatePath)))) {
                    for (XWPFParagraph p : doc.getParagraphs()) {
                        List<XWPFRun> runs = p.getRuns();
                        if (runs != null) {
                            for (XWPFRun r : runs) {
                                String text = r.getText(0);
                                String replace = "";
                                if (text != null && text.contains("inum")) {
                                    if(!ValidationHelper.isNullOrEmpty(invoice.getInvoiceNumber()))
                                        replace = invoice.getInvoiceNumber();
                                    text = text.replace("inum",replace );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("clientname")) {

                                    if(!ValidationHelper.isNullOrEmpty(invoice.getClient()))
                                        replace = invoice.getClient().toString();
                                    text = text.replace("clientname",replace );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("clientaddress")) {
                                    if(!ValidationHelper.isNullOrEmpty(invoice.getClient()) &&
                                            !ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressStreet()))
                                        replace = invoice.getClient().getAddressStreet();
                                    text = text.replace("clientaddress",replace );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("clientaddress2")) {
                                    if(!ValidationHelper.isNullOrEmpty(invoice.getClient()) &&
                                            !ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressPostalCode()))
                                        replace = invoice.getClient().getAddressPostalCode();

                                    if(!ValidationHelper.isNullOrEmpty(invoice.getClient()) &&
                                            !ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressProvinceId())){
                                        Province province = invoice.getClient().getAddressProvinceId();
                                        replace = province.getDescription() + "(" + province.getCode() + ")";
                                    }
                                    text = text.replace("clientaddress2",replace );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("clientpiva")) {
                                    if(!ValidationHelper.isNullOrEmpty(invoice.getClient()) &&
                                            !ValidationHelper.isNullOrEmpty(invoice.getClient().getFiscalCode()))
                                        replace = invoice.getClient().getFiscalCode();
                                    text = text.replace("clientpiva",replace );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("impon")) {
                                    text = text.replace("impon",imponibile.toString() );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("ivap")) {
                                    text = text.replace("ivap",ivaPercentage.toString() + "%" );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("ivaa")) {
                                    text = text.replace("ivaa",totalIva.toString());
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("totale")) {
                                    Double total = imponibile + totalIva;
                                    text = text.replace("totale",total.toString());
                                    r.setText(text, 0);
                                }
                            }
                        }
                    }

                    FileOutputStream out = new FileOutputStream(tempDoc);
                    doc.write(out);
                    out.close();
                }

                courtesyInvoicePdf = new WLGExport();
                courtesyInvoicePdf.setExportDate(currentDate);
                DaoManager.save(courtesyInvoicePdf, true);
                String sb = courtesyInvoicePdf.generateDestinationPath(fileName + ".pdf");
                File filePath = new File(sb);
                String sofficeCommand =
                        ApplicationSettingsHolder.getInstance().getByKey(
                                ApplicationSettingsKeys.SOFFICE_COMMAND).getValue().trim();
                Process p = Runtime.getRuntime().exec(new String[]{sofficeCommand, "--headless",
                        "--convert-to", "pdf", "--outdir", filePath.getAbsolutePath(), tempDoc});
                p.waitFor();
                FileHelper.delete(tempDoc);
                DaoManager.save(courtesyInvoicePdf, true);
                LogHelper.log(log, courtesyInvoicePdf.getId() + " " + filePath);
                addAttachedFile(courtesyInvoicePdf);
            }
        }catch(Exception e){
            LogHelper.log(log,e);
        }
    }

    private void attachInvoiceData(Long mailId) throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {
        setInvoiceEmailAttachedFiles(new ArrayList<>());
        String refrequest = "";
        String ndg = "";
        WLGInbox baseMail = DaoManager.get(WLGInbox.class, mailId);
        if (!ValidationHelper.isNullOrEmpty(baseMail)) {
            if (!ValidationHelper.isNullOrEmpty(baseMail)
                    && !ValidationHelper.isNullOrEmpty(baseMail.getRequests())) {
                setInvoicedRequests(baseMail.getRequests()
                        .stream()
                        .filter(r -> !ValidationHelper.isNullOrEmpty(r.getStateId()) &&
                                (r.getStateId().equals(RequestState.EVADED.getId()) || r.getStateId().equals(RequestState.SENT_TO_SDI.getId())))
                        .collect(Collectors.toList()));
            }
            refrequest = baseMail.getReferenceRequest();
            ndg = baseMail.getNdg();
        }
        if (ValidationHelper.isNullOrEmpty(getInvoicedRequests()))
            return;
        Request invoiceRequest = getInvoicedRequests().get(0);
        byte[] baos = getXlsBytes(refrequest, invoiceRequest);
        if (!ValidationHelper.isNullOrEmpty(baos)) {
            excelInvoice = new WLGExport();
            Date currentDate = new Date();
            excelInvoice.setExportDate(currentDate);
            DaoManager.save(excelInvoice, true);
            String fileName = "Richieste_Invoice_" + DateTimeHelper.toFileDateWithMinutes(currentDate) + ".xls";
            String sb = excelInvoice.generateDestinationPath(fileName);
            File filePath = new File(sb);
            try {
                String str = FileHelper.writeFileToFolder(fileName,
                        filePath, baos);
                if (!new File(str).exists()) {
                    return;
                }
                LogHelper.log(log, excelInvoice.getId() + " " + str);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            DaoManager.save(excelInvoice, true);
            addAttachedFile(excelInvoice);
            attachInvoicePdf(baos);
        }
        Invoice invoice = DaoManager.get(Invoice.class, getNumber());
        attachCourtesyInvoicePdf(invoice);
    }

    private byte[] getXlsBytes(String refrequest, Request invoiceRequest) {
        byte[] excelFile = null;
        try {
            ExcelDataWrapper excelDataWrapper = new ExcelDataWrapper();
            excelDataWrapper.setNdg(getEntity().getNdg());
            Document document = DaoManager.get(Document.class, new Criterion[]{
                    Restrictions.eq("mail.id", getEntity().getId())});

            if (ValidationHelper.isNullOrEmpty(document)) {
                document = new Document();
                document.setMail(getEntity());
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

            if (!ValidationHelper.isNullOrEmpty(getEntity().getClientInvoice())) {
                excelDataWrapper.setClientInvoice(DaoManager.get(Client.class, getEntity().getClientInvoice().getId()));
            }

            if (!ValidationHelper.isNullOrEmpty(getEntity().getManagers())) {
                excelDataWrapper.setManagers(getEntity().getManagers());
            }

            if (!ValidationHelper.isNullOrEmpty(getEntity().getClientFiduciary())) {
                excelDataWrapper.setClientFiduciary(DaoManager.get(Client.class, getEntity().getClientFiduciary().getId()));
            }

            if (!ValidationHelper.isNullOrEmpty(getEntity().getOffice())) {
                excelDataWrapper.setOffice(getEntity().getOffice().getDescription());
            }

            List<Request> filteredRequests = emptyIfNull(getInvoicedRequests()).stream().filter(r -> r.isDeletedRequest()).collect(Collectors.toList());
            excelFile = new CreateExcelRequestsReportHelper(true).convertMailUserDataToExcel(filteredRequests, document, excelDataWrapper);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return excelFile;
    }

    public void attachInvoicePdf(Invoice invoice, String pdfFilePath) {
        try {
            String templatePath  = (new File(FileHelper.getRealPath(),
                    "resources" + File.separator + "layouts" + File.separator
                            + "Invoice" + File.separator + "InvoiceDocumentTemplate.docx")
                    .getAbsolutePath());
            Double imponibile = 0.0;
            Double totalIva = 0.0;
            Double ivaPercentage = 0.0;

            if(!ValidationHelper.isNullOrEmpty(invoice)) {
                List<InvoiceItem> items = DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoice)});
                for(InvoiceItem item : items) {
                    double total = 0.0;
                    double amount = 0.0;
                    double totalCost = 0.0;

                    if(item.getAmount() != null)
                        amount = item.getAmount();
                    if(item.getInvoiceTotalCost() != null)
                        totalCost = item.getInvoiceTotalCost();
                    if(amount != 0.0)
                        imponibile = imponibile + (amount * totalCost);
                    else
                        imponibile = imponibile + totalCost;
                    if(amount != 0.0)
                        total = amount * totalCost;
                    else
                        total = totalCost;
                    if(item.getVat() != null){
                        ivaPercentage = ivaPercentage + item.getVat();
                        totalIva = totalIva + ((item.getVat() * total)/100);
                    }
                }
                if(!ValidationHelper.isNullOrEmpty(items) && items.size() > 0)
                    ivaPercentage = ivaPercentage/ items.size();
                BigDecimal ivaPer = BigDecimal.valueOf(ivaPercentage);
                ivaPer = ivaPer.setScale(2, RoundingMode.HALF_UP);
                ivaPercentage = ivaPer.doubleValue();

                BigDecimal totIva = BigDecimal.valueOf(totalIva);
                totIva = totIva.setScale(2, RoundingMode.HALF_UP);
                totalIva = totIva.doubleValue();
                String fileName = "FE-" + invoice.getNumber() + " " + invoice.getClient().getClientName();
//                Date currentDate = new Date();
//                String fileName = "Richieste_Invoice_"+DateTimeHelper.toFileDateWithMinutes(currentDate);
                //String fileName = "FE-" + getNumber() + " " + getSelectedInvoice().getClient().getClientName();
                String tempDir = FileHelper.getLocalTempDir();
                tempDir  += File.separator + UUID.randomUUID();
                FileUtils.forceMkdir(new File(tempDir));
                String tempDoc = tempDir +  File.separator +  fileName +"_temp.docx";

                try (XWPFDocument doc = new XWPFDocument(
                        Files.newInputStream(Paths.get(templatePath)))) {
                    for (XWPFParagraph p : doc.getParagraphs()) {
                        List<XWPFRun> runs = p.getRuns();
                        if (runs != null) {
                            for (XWPFRun r : runs) {
                                String text = r.getText(0);
                                String replace = "";
                                if (text != null && text.contains("inum")) {
                                    if(!ValidationHelper.isNullOrEmpty(invoice.getInvoiceNumber()))
                                        replace = invoice.getInvoiceNumber();
                                    text = text.replace("inum",replace );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("clientname")) {

                                    if(!ValidationHelper.isNullOrEmpty(invoice.getClient()))
                                        replace = invoice.getClient().toString();
                                    text = text.replace("clientname",replace );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("clientaddress")) {
                                    if(!ValidationHelper.isNullOrEmpty(invoice.getClient()) &&
                                            !ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressStreet()))
                                        replace = invoice.getClient().getAddressStreet();
                                    text = text.replace("clientaddress",replace );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("clientaddress2")) {
                                    if(!ValidationHelper.isNullOrEmpty(invoice.getClient()) &&
                                            !ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressPostalCode()))
                                        replace = invoice.getClient().getAddressPostalCode();

                                    if(!ValidationHelper.isNullOrEmpty(invoice.getClient()) &&
                                            !ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressProvinceId())){
                                        Province province = invoice.getClient().getAddressProvinceId();
                                        replace = province.getDescription() + "(" + province.getCode() + ")";
                                    }
                                    text = text.replace("clientaddress2",replace );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("clientpiva")) {
                                    if(!ValidationHelper.isNullOrEmpty(invoice.getClient()) &&
                                            !ValidationHelper.isNullOrEmpty(invoice.getClient().getFiscalCode()))
                                        replace = invoice.getClient().getFiscalCode();
                                    text = text.replace("clientpiva",replace );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("impon")) {
                                    text = text.replace("impon",imponibile.toString() );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("ivap")) {
                                    text = text.replace("ivap",ivaPercentage.toString() + "%" );
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("ivaa")) {
                                    text = text.replace("ivaa",totalIva.toString());
                                    r.setText(text, 0);
                                }else if (text != null && text.contains("totale")) {
                                    Double total = imponibile + totalIva;
                                    text = text.replace("totale",total.toString());
                                    r.setText(text, 0);
                                }
                            }
                        }
                    }

                    FileOutputStream out = new FileOutputStream(tempDoc);
                    doc.write(out);
                    out.close();
                }

                String sb = MailHelper.getDestinationPath() +
                        DateTimeHelper.ToFilePathString(new Date());
                File filePath = new File(sb);
                String sofficeCommand =
                        ApplicationSettingsHolder.getInstance().getByKey(
                                ApplicationSettingsKeys.SOFFICE_COMMAND).getValue().trim();
                Process p = Runtime.getRuntime().exec(new String[] { sofficeCommand, "--headless",
                        "--convert-to", "pdf","--outdir", filePath.getAbsolutePath(), tempDoc });
                p.waitFor();
                FileHelper.delete(tempDoc);

                String tempPdf = prepareInvoicePdf(invoice);
                String tempFilePathStr = sb + File.separator + fileName + "_temp.pdf";
                PDFMergerUtility obj = new PDFMergerUtility();
                obj.setDestinationFileName(pdfFilePath);
                File file1 = new File(tempPdf);
                File file2 = new File(tempFilePathStr);
                obj.addSource(file1);
                obj.addSource(file2);
                obj.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
                log.info("Merged pdf : " + pdfFilePath);
                FileHelper.delete(tempPdf);
                FileHelper.delete(tempFilePathStr);
            }
        }catch(Exception e){
            LogHelper.log(log,e);
        }
    }

    public String prepareInvoicePdf(Invoice invoice) throws
            IOException, PersistenceBeanException, IllegalAccessException {
        String body = getPdfRequestBody(invoice);
        log.info("Invoice PDF body" + body);
        byte [] fileContent  = PrintPDFHelper.convertToPDF(null, body, null,
                DocumentType.INVOICE);

        if (fileContent != null) {
            String fileName = "FE-" + invoice.getNumber() + " "
                    + invoice.getClient().getClientName();
            String tempDir = FileHelper.getLocalTempDir();
            tempDir  += File.separator + UUID.randomUUID();
            FileUtils.forceMkdir(new File(tempDir));
            String tempPdf = tempDir +  File.separator +  fileName +".pdf";
            InputStream stream = new ByteArrayInputStream(fileContent);
            File targetFile = new File(tempPdf);
            OutputStream outStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(outStream);
            log.info("Invoice PDF created : " + tempPdf);
            return tempPdf;
        }
        return "";
    }

    public String prepareInvoicePdf(DocumentType documentType, Invoice invoice, String tempDir) throws
            IOException, PersistenceBeanException, IllegalAccessException {
        String body = getPdfRequestBody(invoice);
        log.info("Invoice PDF body" + body);
        byte [] fileContent  = PrintPDFHelper.convertToPDF(null, body, null, documentType);

        if (fileContent != null) {
            String fileName = "FE-" + getNumber() + " "
                    + invoice.getClient().getClientName();
//            String tempDir = FileHelper.getLocalTempDir();
//            tempDir  += File.separator + UUID.randomUUID();
         //   FileUtils.forceMkdir(new File(tempDir));
            String tempPdf = tempDir +  File.separator +  fileName +".pdf";
            InputStream stream = new ByteArrayInputStream(fileContent);
            File targetFile = new File(tempPdf);
            OutputStream outStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(outStream);
            return tempPdf;
        }
        return "";
    }

    public void saveFiles(WLGInbox inbox, List<FileWrapper> invoiceEmailAttachedFiles, boolean transaction) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(invoiceEmailAttachedFiles)) {
            for (FileWrapper wrapper : invoiceEmailAttachedFiles) {
                if(!ValidationHelper.isNullOrEmpty(wrapper.getAddAttachment()) && wrapper.getAddAttachment()){
                    WLGExport export = DaoManager.get(WLGExport.class, new Criterion[]{
                            Restrictions.eq("id", wrapper.getId())
                    });
                    export.setExportDate(new Date());
                    export.setSourcePath(String.format("\\%s", inbox.getId()));
                    export.setInbox(inbox);
                    DaoManager.save(export, transaction);
                }else {
                    WLGExport export = DaoManager.get(WLGExport.class, new Criterion[]{
                            Restrictions.eq("id", wrapper.getId())
                    });
                    if(!ValidationHelper.isNullOrEmpty(export)){
                        DaoManager.refresh(export);
                    }
                }
            }
        }
    }

}

