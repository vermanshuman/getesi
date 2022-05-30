package it.nexera.ris.web.beans.pages;

import it.nexera.ris.api.FatturaAPI;
import it.nexera.ris.api.FatturaAPIResponse;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.create.xls.CreateExcelRequestsReportHelper;
import it.nexera.ris.common.xml.wrappers.SelectItemWrapper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityViewPageBean;
import it.nexera.ris.web.beans.base.AccessBean;
import it.nexera.ris.web.beans.wrappers.GoodsServicesFieldWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ExcelDataWrapper;
import it.nexera.ris.web.beans.wrappers.logic.FileWrapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@Setter
@Getter
@ManagedBean(name = "mailManagerViewBean")
@ViewScoped
public class MailManagerViewBean extends EntityViewPageBean<WLGInbox> implements Serializable {

    private static final long serialVersionUID = 6592167416646133302L;

    private Integer downloadFileIndex;

    private List<FileWrapper> attachedFiles;

    private AtomicLong fileIndex;

    private List<SelectItem> mailTypes;

    private Long selectedTypeId;

    private List<SelectItem> availableStates;

    private Long selectedStateId;

    private Long entityEditId;

    private String mailPdf;

    private boolean onlyView;

    private Long tablePage;

    private boolean relatedToClient;

    private boolean generatePdf;

    private List<SelectItem> invoiceClients;

    private List<SelectItem> clients;

    private List<SelectItem> fiduciaryClientsList;

    private Long selectedClientId;

    private Long selectedClientInvoiceId;

    private Long selectedClientFiduciaryId;

    private String emailBody;

    private String referencePractice;

    private String ndg;

    private String cdr;

    private List<SelectItemWrapper<Client>> clientManagers;

    private List<SelectItemWrapper<Client>> selectedClientManagers;

    private SelectItemWrapperConverter<Client> clientSelectItemWrapperConverter;

    private boolean createClient;

    private String documentType;

    @ManagedProperty(value = "#{clientCreateBean}")
    private ClientCreate clientCreateBean;

    private Boolean newClientButtonClicked;

    private List<SelectItem> notManagerOrFiduciaryClients;

    private Long selectedNotManagerOrFiduciaryClientId;

    private boolean isMultipleCreateRedirect;

    private boolean confirmButtonIsClicked;

    private List<SelectItem> officeList;

    private Long selectedOfficeId;

    private String fieldsNotSetDialogMessage;

    private Long clientTypeId;

    private List<SelectItem> clientTypes;

    // Fields for Invoice Dialog

    private Request examRequest;

    private boolean multipleCreate;

    // private MenuModel topMenuModel;

    // private int activeMenuTabNum;

    private List<InputCard> inputCardList;

    private String invoiceNumber;

    private Double invoiceItemAmount;

    // private Double invoiceItemVat;

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

    private String apiError;

    private boolean sendInvoice;

    private Boolean billinRequest;

    private int activeTabIndex;

    private List<PaymentInvoice> paymentInvoices;

    private Double amountToBeCollected;

    private Double totalPayments;

    private Long number;

    private List<Request> invoicedRequests;

    private List<FileWrapper> invoiceEmailAttachedFiles;

    private Integer requestType;

    private List<GoodsServicesFieldWrapper> goodsServicesFields;

    private boolean printPdf;

    private Long selectedInvoiceClientId;

    private Client selectedInvoiceClient;

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

    private boolean showRequestTab;

    private Boolean showCreateFatturaButton;

    private Long baseMailId;

    private WLGExport excelInvoice;

    private WLGExport pdfInvoice;

    private StreamedContent invoicePDFFile;

    private Invoice selectedInvoice;

    private List<InvoiceItem> selectedInvoiceItems;

    private Double paymentAmount;

    private Date paymentDate;

    private String paymentDescription;

    private Boolean dataSaved;

    private WLGExport courtesyInvoicePdf;

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

    private List<Request> requestsConsideredForInvoice;

    private static final String MAIL_RERLY_FOOTER = ResourcesHelper.getString("emailReplyFooter");

    private Integer billingListPageTab;

    private Boolean documentTypeInvoice;

    private boolean addAttachment = false;

    private Boolean showButtons;

    private String invoiceAlreadyCreated;

    private String otherRequestsExistsForInvoice;

    private List<Request> otherRequestsConsideredForInvoice;

    @ManagedProperty(value="#{invoiceDialogBean}")
    private InvoiceDialogBean invoiceDialogBean;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getRequestParameter(RedirectHelper.BILLING_LIST))) {
            setBillingListPageTab(Integer.valueOf(getRequestParameter(RedirectHelper.BILLING_LIST)));
        }

        setActiveTabIndex(0);
        setOnlyView(Boolean.parseBoolean(getRequestParameter(RedirectHelper.ONLY_VIEW)));
        SessionHelper.removeObject("isFromMailView");
        String tablePage = getRequestParameter(RedirectHelper.TABLE_PAGE);
        if (!ValidationHelper.isNullOrEmpty(tablePage)) {
            setTablePage(Long.parseLong(tablePage));
        }
        this.setClientTypes(ComboboxHelper.fillList(ClientType.class, Boolean.TRUE));

        loadAttachedFiles();
        checkRelatedToClient();
        if (getEntity().getRequestedReadConfirm() != null && getEntity().getRequestedReadConfirm()
                && (getEntity().getConfirmSent() == null || !getEntity().getConfirmSent())
                && getEntity().getServerId() != null && getEntity().getServerId()
                .equals(Long.parseLong(ApplicationSettingsHolder.getInstance()
                        .getByKey(ApplicationSettingsKeys.RECEIVED_SERVER_ID).getValue()))) {
            executeJS("PF('confirmReadDlgWV').show();");
        }
        setMailTypes(ComboboxHelper.fillList(WLGFolder.class, Order.asc("name"), new Criterion[]{
                Restrictions.or(
                        Restrictions.isNull("defaultFolder"),
                        Restrictions.ne("defaultFolder", true))
        }));
        setAvailableStates(new ArrayList<>());

        setClientSelectItemWrapperConverter(new SelectItemWrapperConverter<>(Client.class));

        for (MailManagerStatuses status : MailManagerStatuses.values()) {
            if (!MailManagerStatuses.CANCELED.equals(status) || getCurrentUser() != null && getCurrentUser().isAdmin()) {
                if (status.isShowAvailable()) {
                    getAvailableStates().add(new SelectItem(status.getId(), status.toString().toUpperCase()));
                }
            }
        }
        setEmailBody();

        Document document = DaoManager.get(Document.class, new Criterion[]{
                Restrictions.eq("mail.id", getEntity().getId())});

        if (!ValidationHelper.isNullOrEmpty(document) || !ValidationHelper.isNullOrEmpty(getEntity().getRequests())
                || (!ValidationHelper.isNullOrEmpty(getEntity().getRecievedInbox()) &&
                !ValidationHelper.isNullOrEmpty(getEntity().getRecievedInbox().getRequests()))) {
            setGeneratePdf(true);
        }

        if (!ValidationHelper.isNullOrEmpty(document) && !ValidationHelper.isNullOrEmpty(document.getTypeId()) &&
                document.getTypeId().equals(DocumentType.INVOICE_REPORT.getId()) && !ValidationHelper.isNullOrEmpty(document.getComplete())
            && document.getComplete()) {
            setDocumentTypeInvoice(true);
        }else
            setDocumentTypeInvoice(false);

        if (!ValidationHelper.isNullOrEmpty(getEntity().getReferenceRequest())) {
            setReferencePractice(getEntity().getReferenceRequest());
        }

        if (!ValidationHelper.isNullOrEmpty(getEntity().getNdg())) {
            setNdg(getEntity().getNdg());
        }

        if (!ValidationHelper.isNullOrEmpty(getEntity().getCdr())) {
            setCdr(getEntity().getCdr());
        }

        if (!ValidationHelper.isNullOrEmpty(getEntity().getClient()) && !ValidationHelper.isNullOrEmpty(getEntity().getClient().getTypeId())) {
            setClientTypeId(getEntity().getClient().getTypeId());
        }

        updateComboboxes();

        checkRelatedClientFields();

        if (!ValidationHelper.isNullOrEmpty(getEntity().getClient())) {
            setSelectedNotManagerOrFiduciaryClientId(getEntity().getClient().getId());
            SelectItemHelper.addItemToListIfItIsNotInIt(getNotManagerOrFiduciaryClients(), getEntity().getClient());
        }

        if (!ValidationHelper.isNullOrEmpty(getEntity().getClientInvoice())) {
            setSelectedClientInvoiceId(getEntity().getClientInvoice().getId());
        }

        //&& Boolean.TRUE.equals(getEntity().getClientFiduciary().getFiduciary())
        if (!ValidationHelper.isNullOrEmpty(getEntity().getClientFiduciary())) {
            setSelectedClientFiduciaryId(getEntity().getClientFiduciary().getId());
        }
        fillSelectedClientManagers();
        if (AccessBean.canCreateInPage(PageTypes.CLIENT_LIST)) {
            setCreateClient(true);
        }

        initOfficesList(true);
        if (!ValidationHelper.isNullOrEmpty(getSelectedClientManagers()) &&
                ValidationHelper.isNullOrEmpty(getClientManagers())) {
            setClientManagers(getSelectedClientManagers());
        }
        if (!ValidationHelper.isNullOrEmpty(getEntity().getOffice())) {
            setSelectedOfficeId(getEntity().getOffice().getId());
        }

        setNewClientButtonClicked(false);
        getEntity().reloadRequests();


        // Changes for invoice
//        generateMenuModel();
        //setMaxInvoiceNumber();
        List<Request> requestList =
                getEntity().getRequests()
                        .stream()
                        .filter(x ->
                                ValidationHelper.isNullOrEmpty(x.getInvoice()) &&
                                        !ValidationHelper.isNullOrEmpty(x.getStateId()) &&
                                        (RequestState.EVADED.getId().equals(x.getStateId())
                                                || RequestState.SENT_TO_SDI.getId().equals(x.getStateId())))
                        .collect(Collectors.toList());
        setInvoicedRequests(requestList);

        // Double invoiceTotalCost = 0.0D;
        if (!ValidationHelper.isNullOrEmpty(requestList)) {
            setShowCreateFatturaButton(Boolean.TRUE);
            Request examRequest = DaoManager.get(Request.class, new CriteriaAlias[]{
                    new CriteriaAlias("client", "c", JoinType.LEFT_OUTER_JOIN),
                    new CriteriaAlias("c.addressCityId", "ac", JoinType.LEFT_OUTER_JOIN),
                    new CriteriaAlias("c.addressProvinceId", "ap", JoinType.LEFT_OUTER_JOIN),
            }, new Criterion[]{
                    Restrictions.eq("id", getInvoicedRequests().get(0).getId())
            });
            setExamRequest(examRequest);
        } else {
            setShowCreateFatturaButton(Boolean.FALSE);
        }
        if (!ValidationHelper.isNullOrEmpty(getExamRequest())
                && !ValidationHelper.isNullOrEmpty(getExamRequest().getInvoice())) {
            Invoice invoice = DaoManager.get(Invoice.class, getExamRequest().getInvoice().getId());
            loadInvoiceDialogData(invoice);
        }
        if (!ValidationHelper.isNullOrEmpty(getExamRequest()) &&
                getExamRequest().getStateId().equals(RequestState.SENT_TO_SDI.getId()))
            setInvoiceSentStatus(true);

        List<Request> requestListSentToSdi =
                getEntity().getRequests()
                        .stream()
                        .filter(x -> !ValidationHelper.isNullOrEmpty(x.getStateId()) &&
                                RequestState.SENT_TO_SDI.getId().equals(x.getStateId())).collect(Collectors.toList());
        if (!requestListSentToSdi.isEmpty())
            setSendInvoice(true);
        setBillinRequest(AccessBean.canViewPage(PageTypes.BILLING_LIST));
        setBaseMailId(getEntityId());
        setRequestsConsideredForInvoice(new ArrayList<>());
        List<Request> requestListForInvoice =
                getEntity().getRequests()
                        .stream()
                        .filter(x ->
                                x.isDeletedRequest() &&
                                        ValidationHelper.isNullOrEmpty(x.getInvoice()) &&
                                        !ValidationHelper.isNullOrEmpty(x.getStateId()) &&
                                        (RequestState.EVADED.getId().equals(x.getStateId())))
                        .collect(Collectors.toList());
        if(!ValidationHelper.isNullOrEmpty(requestListForInvoice)) {
            requestListForInvoice.stream().forEach(r -> {
                r.setSelectedForInvoice(true);
            });
            setRequestsConsideredForInvoice(requestListForInvoice);
        }
    }

    public void handleClientSelect() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        initOfficesList(true);
    }

    public void initOfficesList(boolean isClientSelected) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Client> clientList = DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"))})
                .stream().sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList());


        if (!ValidationHelper.isNullOrEmpty(getClientTypeId())) {
            List<Client> notManagerOrFiduciaryClients = clientList.stream()
                    .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                    .collect(Collectors.toList());

            setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(notManagerOrFiduciaryClients.stream()
                    .filter(c -> (c.getTypeId().equals(getClientTypeId())))
                    .collect(Collectors.toList()), true));
        } else {
            setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(clientList.stream()
                    .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                    .collect(Collectors.toList()), true));
        }
        if (getSelectedNotManagerOrFiduciaryClientId() != null) {
            setClientManagers(ComboboxHelper.fillWrapperList(emptyIfNull(clientList)
                    .stream()
                    .filter(c -> emptyIfNull(c.getReferenceClients()).stream()
                            .anyMatch(rc -> rc.getId().equals(getSelectedNotManagerOrFiduciaryClientId())))
                    .collect(Collectors.toList())));

            setFiduciaryClientsList(ComboboxHelper.fillList(clientList.stream()
                    .filter(c -> emptyIfNull(c.getReferenceClients()).stream()
                            .anyMatch(rc -> rc.getId().equals(getSelectedNotManagerOrFiduciaryClientId())))
                    .filter(c -> (c.getFiduciary() != null && c.getFiduciary()))
                    .collect(Collectors.toList()), true));

            List<Office> offices = DaoManager.get(Client.class, getSelectedNotManagerOrFiduciaryClientId()).getOffices();
            if (!ValidationHelper.isNullOrEmpty(offices)) {
                setOfficeList(ComboboxHelper.fillList(offices.stream()
                        .sorted(Comparator.comparing(Dictionary::getDescription))
                        .collect(Collectors.toList()), true));
            } else {
                setOfficeList(Collections.singletonList(SelectItemHelper.getNotSelected()));
            }
            // MailManagerView data - do not set automatically Fatturazione field
            List<Client> invoiceClients = new ArrayList<>();
            if (isClientSelected && !ValidationHelper.isNullOrEmpty(getNotManagerOrFiduciaryClients())) {
                for (Client client : clientList) {
                    if (!client.getId().equals(getSelectedNotManagerOrFiduciaryClientId())) {
                        continue;
                    }
                    invoiceClients.addAll(client.getBillingRecipientList());
                }
            }
            setInvoiceClients(ComboboxHelper.fillList(invoiceClients.stream()
                    .filter(distinctByKey(c -> c.getId()))
                    .collect(Collectors.toList()), true));
        } else {
            setOfficeList(ComboboxHelper.fillList(Office.class, Order.asc("description")));
            setClientManagers(ComboboxHelper.fillWrapperList(clientList.stream()
                    .filter(c -> c.getManager() != null && c.getManager())
                    .collect(Collectors.toList())));
        }
    }

    private void addClientToManagerListIfHeIsNotInIt() {

        for (SelectItemWrapper<Client> selectedClientManager : getSelectedClientManagers()) {

            if (getClientManagers().stream().noneMatch(x -> x.getId().equals(selectedClientManager.getId()))) {
                getClientManagers().add(selectedClientManager);
                getClientSelectItemWrapperConverter().getWrapperList().add(selectedClientManager);
            }
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedClientManagers())) {
            try {
                SelectItemWrapper<Client> selectedClientManager = getSelectedClientManagers().get(0);
                Client client = DaoManager.get(Client.class, new Criterion[]{
                        Restrictions.eq("id", selectedClientManager.getId())});
                if (!Hibernate.isInitialized(client.getReferenceClients()))
                    Hibernate.initialize(client.getReferenceClients());
                if (!ValidationHelper.isNullOrEmpty(client.getReferenceClients())
                        && client.getReferenceClients().size() == 1) {
                    setSelectedNotManagerOrFiduciaryClientId(client.getReferenceClients().get(0).getId());
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    private void fillSelectedClientManagers() {
        setSelectedClientManagers(new ArrayList<>());

        if (!ValidationHelper.isNullOrEmpty(getEntity().getManagers())) {
            for (Client item : getEntity().getManagers()) {
                SelectItemWrapper<Client> selectItem = new SelectItemWrapper<>(item);
                getSelectedClientManagers().add(selectItem);
            }
        }
        addClientToManagerListIfHeIsNotInIt();
    }

    public void updateComboboxes() throws PersistenceBeanException, IllegalAccessException {
        List<Client> clientList = DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"))})
                .stream().sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList());

        List<Client> invoiceClients = new ArrayList<Client>();

        for (Client client : clientList) {
            if (!client.getId().equals(getSelectedNotManagerOrFiduciaryClientId())) {
                continue;
            }
            if (!ValidationHelper.isNullOrEmpty(client.getBillingRecipientList())) {
                invoiceClients.addAll(client.getBillingRecipientList());
            }
        }
        setInvoiceClients(ComboboxHelper.fillList(invoiceClients, true));


        setFiduciaryClientsList(ComboboxHelper.fillList(clientList.stream()
                .filter(c -> c.getFiduciary() != null && c.getFiduciary()).collect(Collectors.toList()), true));

        if (!ValidationHelper.isNullOrEmpty(getClientTypeId())) {
            List<Client> notManagerOrFiduciaryClients = clientList.stream()
                    .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                    .collect(Collectors.toList());

            setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(notManagerOrFiduciaryClients.stream()
                    .filter(c -> (c.getTypeId().equals(getClientTypeId())))
                    .collect(Collectors.toList()), true));
        } else {
            setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(clientList.stream()
                    .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                    .collect(Collectors.toList()), true));
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedNotManagerOrFiduciaryClientId())) {
            SelectItemHelper.addItemToListIfItIsNotInIt(getNotManagerOrFiduciaryClients(), getEntity().getClient());
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedClientInvoiceId())) {
            SelectItemHelper.addItemToListIfItIsNotInIt(getInvoiceClients(), getEntity().getClientInvoice());
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedClientFiduciaryId())) {
            SelectItemHelper.addItemToListIfItIsNotInIt(getInvoiceClients(), getEntity().getClientFiduciary());
        }

        if (getSelectedNotManagerOrFiduciaryClientId() != null) {

            setClientManagers(ComboboxHelper.fillWrapperList(emptyIfNull(clientList)
                    .stream()
                    .filter(c -> emptyIfNull(c.getReferenceClients()).stream()
                            .anyMatch(rc -> rc.getId().equals(getSelectedNotManagerOrFiduciaryClientId())))
                    .collect(Collectors.toList())));

//            setClientManagers(ComboboxHelper.fillWrapperList(clientList.stream()
//                    .filter(c -> (c.getManager() != null && c.getManager()) || (c.getId().equals(getSelectedNotManagerOrFiduciaryClientId())))
//                    .collect(Collectors.toList())));
        } else {
            setClientManagers(ComboboxHelper.fillWrapperList(clientList.stream()
                    .filter(c -> c.getManager() != null && c.getManager()).collect(Collectors.toList())));
        }


//        if (!ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
//           setClientManagers(ComboboxHelper.fillWrapperList(clientList.stream()
//                      .filter(c -> c.getManager() != null && c.getManager() && c.getId().equals(getSelectedClientId()) )
//                      .collect(Collectors.toList())));
//         }else {
//           setClientManagers(ComboboxHelper.fillWrapperList(clientList.stream()
//                      .filter(c -> c.getManager() != null && c.getManager()  )
//                      .collect(Collectors.toList())));
//         }


        getClientSelectItemWrapperConverter().setWrapperList(new ArrayList<>(getClientManagers()));

        if (!ValidationHelper.isNullOrEmpty(getSelectedClientManagers())) {
            addClientToManagerListIfHeIsNotInIt();
        }
    }

    private void checkRelatedClientFields() throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getEntity().getClientInvoice())
                && ValidationHelper.isNullOrEmpty(getEntity().getClientFiduciary())
                && ValidationHelper.isNullOrEmpty(getEntity().getManagers())
                && ValidationHelper.isNullOrEmpty(getEntity().getClient())) {

            List<String> onlyEmails = MailHelper.getOnlyEmails(getEntity().getEmailTo());
            onlyEmails.addAll(MailHelper.getOnlyEmails(getEntity().getEmailFrom()));
            onlyEmails.addAll(MailHelper.getOnlyEmails(getEntity().getEmailCC()));

            if (!ValidationHelper.isNullOrEmpty(onlyEmails)) {
                List<Client> clientList = DaoManager.load(Client.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("emails", "email", JoinType.INNER_JOIN)},
                        new Criterion[]{
                                Restrictions.in("email.email", onlyEmails),
                                Restrictions.and(Restrictions.isNotNull("manager"), Restrictions.eq("manager", Boolean.TRUE))});

                if (ValidationHelper.isNullOrEmpty(clientList)) {
                    clientList = DaoManager.load(Client.class,
                            new CriteriaAlias[]{
                                    new CriteriaAlias("emails", "email", JoinType.INNER_JOIN)},
                            new Criterion[]{
                                    Restrictions.in("email.email", MailHelper.getOnlyEmails(getEntity().getEmailFrom())),
                                    Restrictions.and(Restrictions.isNotNull("manager"), Restrictions.eq("manager", Boolean.TRUE))});
                }
                getEntity().setManagers(new ArrayList<>());
                getEntity().getManagers().addAll(clientList);
            }

            onlyEmails.removeAll(MailHelper.getOnlyEmails(getEntity().getEmailCC()));

            if (!ValidationHelper.isNullOrEmpty(onlyEmails)) {
                List<Client> clientFiduciaryList = DaoManager.load(Client.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("emails", "email", JoinType.INNER_JOIN)},
                        new Criterion[]{Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                                Restrictions.isNull("deleted")),
                                Restrictions.in("email.email", onlyEmails),
                                Restrictions.eq("fiduciary", Boolean.TRUE)});

                if (!ValidationHelper.isNullOrEmpty(clientFiduciaryList)) {
                    getEntity().setClientFiduciary(clientFiduciaryList.get(0));
                }
            }

            if (!ValidationHelper.isNullOrEmpty(getEntity().getManagers())) {
                Optional<Client> clientInvoice = getEntity().getManagers().stream().filter(
                        client -> !ValidationHelper.isNullOrEmpty(client.getBillingRecipientList())).findFirst();
                clientInvoice.ifPresent(client -> getEntity().setClientInvoice(client.getBillingRecipientList().get(0)));

                if (!ValidationHelper.isNullOrEmpty(getEntity().getManagers().get(0).getClient())) {
                    getEntity().setClient(getEntity().getManagers().get(0).getClient());
                }
            }

            if (!ValidationHelper.isNullOrEmpty(getEntity().getClientFiduciary())
                    && !ValidationHelper.isNullOrEmpty(getEntity().getClientFiduciary().getOffice())) {
                getEntity().setOffice(getEntity().getClientFiduciary().getOffice());
                setSelectedOfficeId(getEntity().getOffice().getId());
            } else if (!ValidationHelper.isNullOrEmpty(getEntity().getManagers())) {
                for (Client manager : getEntity().getManagers()) {
                    if (!ValidationHelper.isNullOrEmpty(manager.getOffice())) {
                        getEntity().setOffice(manager.getOffice());
                        setSelectedOfficeId(getEntity().getOffice().getId());
                        break;
                    }
                }
            }

            // MailManagerView data - do not have to be saved in db until the user clicks button Salva
            // DaoManager.save(getEntity(), true);
            if (ValidationHelper.isNullOrEmpty(getSelectedNotManagerOrFiduciaryClientId())) {

                onlyEmails = MailHelper.getOnlyEmails(getEntity().getEmailFrom());
                if (!ValidationHelper.isNullOrEmpty(onlyEmails)) {
                    List<Client> clientList = DaoManager.load(Client.class,
                            new CriteriaAlias[]{
                                    new CriteriaAlias("emails", "email", JoinType.INNER_JOIN)},
                            new Criterion[]{
                                    Restrictions.ilike("email.email", getEntity().getEmailFrom()
                                            .replaceAll(".+<", "").replaceAll(">", ""), MatchMode.ANYWHERE),
                                    Restrictions.or(Restrictions.eq("manager", Boolean.FALSE),
                                            Restrictions.isNull("manager")),
                                    Restrictions.or(Restrictions.eq("fiduciary", Boolean.FALSE),
                                            Restrictions.isNull("fiduciary"))});

                    if (clientList != null && clientList.size() > 0) {
                        setSelectedNotManagerOrFiduciaryClientId(clientList.get(0).getId());
                    }
                }

            }
        }
    }

    private List<String> getEmailsFromString(String emailString) {
        List<String> result = new ArrayList<>();

        Pattern p = Pattern.compile("\\<(.*?)\\>");
        Matcher m = p.matcher(emailString);

        while (m.find()) {
            result.add(m.group(1));
        }
        return result;
    }

    public void setEmailBody() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEmailBody(getEntity().getEmailBody().replaceAll("<base.*?>", ""));
        if (MailManagerTypes.SENT == getEntity().getMailType() && getEntity().getEmailBodyHtml().contains("img")
                && getEntity().getEmailBodyHtml().contains("cid")) {
            org.jsoup.nodes.Document doc = Jsoup.parse(getEntity().getEmailBody());
            for (Element element : doc.select("img")) {
                String id = element.attr("imageID");
                if (!ValidationHelper.isNullOrEmpty(id)) {
                    WLGExport export = DaoManager.get(WLGExport.class, Long.parseLong(id));
                    byte[] bytes = FileHelper.loadContentByPath(export.getDestinationPath());
                    try {
                        String encodedFile = new String();
                        if (!ValidationHelper.isNullOrEmpty(bytes)) {
                            encodedFile = new String(Base64.encodeBase64(bytes), "UTF-8");
                        }
                        setEmailBody(getEmailBody().replaceAll("imageid=\"" + id + "\" src=\"(.*?)\"",
                                "src=\"data:image/png;base64, " + encodedFile + "\""));
                    } catch (UnsupportedEncodingException e) {
                        LogHelper.log(log, e);
                    }
                }
            }
        }
    }

    private void checkRelatedToClient() throws PersistenceBeanException, IllegalAccessException {
        if (getEntity().getMailType() == MailManagerTypes.RECEIVED) {
            List<Client> clients = DaoManager.load(Client.class, new CriteriaAlias[]{
                    new CriteriaAlias("emails", "email", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("email.email", getEntity().getEmailFrom()
                            .replaceAll(".+<", "").replaceAll(">", ""))
            });
            if (!ValidationHelper.isNullOrEmpty(clients)) {
                setRelatedToClient(true);
                setSelectedClientId(clients.get(0).getId());
            } else {
                setSelectedClientId(null);
            }
        }
    }

    public void prepareClients() throws PersistenceBeanException, IllegalAccessException {
        setClients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                Restrictions.or(
                        Restrictions.isNull("isDeleted"),
                        Restrictions.eq("isDeleted", false)
                )}));
        checkRelatedToClient();
    }

    public void confirmSelectedClient() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
            ClientEmail clientEmail = new ClientEmail();
            clientEmail.setEmail(getEntity().getEmailFrom()
                    .replaceAll(".+<", "")
                    .replaceAll(">", ""));
            clientEmail.setTypeId(EmailType.PERSONAL.getId());
            clientEmail.setClient(DaoManager.get(Client.class, getSelectedClientId()));
            DaoManager.save(clientEmail, true);
            checkRelatedToClient();
            onLoad();
        }
    }

    public void editEntity() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (getCanEdit()) {
            RedirectHelper.goToMailEdit(getEntity().getId(), MailEditType.EDIT);
        }
    }

    public void loadAttachedFiles() {
        setAttachedFiles(new ArrayList<>());
        setFileIndex(new AtomicLong());
        getEntity().setFiles(null);
        getEntity().setImgFiles(null);
        if (getEntity().getFiles() != null) {
            getEntity().getFiles().forEach(file -> {
                getAttachedFiles().add(new FileWrapper(getFileIndex().getAndIncrement(),
                        file.getFileName(), file.getDestinationPath()));
            });
        }
    }

    public void addImage() throws PersistenceBeanException, IllegalAccessException {
        for (WLGExport export : getEntity().getImgFiles()) {
            DaoManager.save(export, true);
        }
        loadAttachedFiles();
    }

    public void setConfirmMailRead() {
        try {
            WLGInbox mail = DaoManager.get(WLGInbox.class, getEntityId());
            mail.setConfirmSent(Boolean.TRUE);
            DaoManager.save(mail, true);
        } catch (PersistenceBeanException | IllegalAccessException | InstantiationException e) {
            LogHelper.log(log, e);
        }
    }

    public void sendConfirmMail() {
        if (!ValidationHelper.isNullOrEmpty(getEntityId())) {

            try {
                WLGInbox mail = DaoManager.get(WLGInbox.class, getEntityId());

                WLGInbox confirmMail = new WLGInbox();

                List<String> emailsFrom = DaoManager.loadField(WLGServer.class, "login", String.class,
                        new Criterion[]{Restrictions.eq("type", 15L)});
                if (!ValidationHelper.isNullOrEmpty(emailsFrom)) {
                    confirmMail.setEmailFrom(emailsFrom.get(0));
                }
                confirmMail.setEmailTo(mail.getEmailFrom());
                confirmMail.setEmailSubject(String.format(
                        ResourcesHelper.getString("confirmMailSubject"),
                        mail.getEmailSubject()));
                confirmMail.setEmailBody(String.format(
                        ResourcesHelper.getString("confirmMailBody"),
                        DateTimeHelper.toStringWithMinutes(new Date()),
                        mail.getEmailBody()));
                confirmMail.setEmailBodyHtml(String.format(
                        ResourcesHelper.getString("confirmMailBody"),
                        DateTimeHelper.toStringWithMinutes(new Date()),
                        mail.getEmailBodyHtml()));

                MailHelper.sendMail(confirmMail, null);

                mail.setConfirmSent(Boolean.TRUE);

                DaoManager.save(mail, true);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void downloadFile() {
        FileWrapper wrapper = getAttachedFiles().get(getDownloadFileIndex());
        if (!ValidationHelper.isNullOrEmpty(wrapper)) {
            if (ValidationHelper.isNullOrEmpty(wrapper.getFilePath())) {
                log.warn("File download error: Document is null");
                return;
            }

            File file = new File(wrapper.getFilePath());
            try {
                FileHelper.sendFile(wrapper.getFileName(), new FileInputStream(file), (int) file.length());
            } catch (FileNotFoundException e) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                        ResourcesHelper.getValidation("noDocumentOnServer"), "");
            }
        }
    }

    public boolean getCanDeleteMail() {
        try {
            return AccessBean.canDeleteInPage(PageTypes.MAIL_MANAGER_LIST);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public boolean isActual() {
        return !ValidationHelper.isNullOrEmpty(getEntity()) &&
                !ValidationHelper.isNullOrEmpty(getEntity().getState())
                && !getEntity().getState().equals(MailManagerStatuses.DELETED.getId())
                && !getEntity().getState().equals(MailManagerStatuses.CANCELED.getId());
    }

    public void changeMailState() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        changeMailsStatus(MailManagerStatuses.findById(getSelectedStateId()));
    }

    public void cancelMail() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        changeMailsStatus(MailManagerStatuses.CANCELED);
        if (isOnlyView()) RedirectHelper.goTo(PageTypes.MAIL_MANAGER_FOLDER);
        else RedirectHelper.goToSavePage(PageTypes.MAIL_MANAGER_LIST, null, getTablePage());
    }

    private void changeMailsStatus(MailManagerStatuses status)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (getEntity() != null) {
            getEntity().setUserChangedState(null);
            if (MailManagerStatuses.READ == status) {
                if (!getEntity().getRead()) {
                    ReadWLGInbox readWLGInbox = new ReadWLGInbox(getEntity().getId(), getCurrentUser().getId());
                    DaoManager.save(readWLGInbox, true);
                }
            } else if (MailManagerStatuses.NEW == status) {
                List<ReadWLGInbox> readWLGInboxList = DaoManager.load(ReadWLGInbox.class, new Criterion[]{
                        Restrictions.eq("mailId", getEntity().getId()),
                        Restrictions.eq("userId", getCurrentUser().getId())
                });
                if (!ValidationHelper.isNullOrEmpty(readWLGInboxList)) {
                    for (ReadWLGInbox read : readWLGInboxList) {
                        DaoManager.remove(read, true);
                    }
                }
            } else if (MailManagerStatuses.CANCELED == status) {
                getEntity().setPreviousState(getEntity().getState());
                getEntity().setUserChangedState(DaoManager.get(User.class, getCurrentUser().getId()));
            } else if (MailManagerStatuses.PARTIAL == status || MailManagerStatuses.MANAGED == status) {
                getEntity().setUserChangedState(DaoManager.get(User.class, getCurrentUser().getId()));
            }

            getEntity().setState(status.getId());
            log.info("setting mail :: " + getEntity().getId() + " state to :: " + MailManagerStatuses.findById(getEntity().getState())
                    + " by user:: " + getCurrentUser().getId());
            DaoManager.save(getEntity(), true);
        }
    }

    public void changeMailType() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (getSelectedTypeId() != null && !ValidationHelper.isNullOrEmpty(getEntity())) {
            WLGFolder folder = DaoManager.get(WLGFolder.class, getSelectedTypeId());
            MailManagerHelper.changeFolder(getEntity(), folder, getCurrentUser().getId());
        }
    }

    public void generateMailPdf() {
        setMailPdf(PrintPDFHelper.generatePDFOnEmail(getEntity(), "pdfView"));
    }

    public void clearPrintArea() {
        setMailPdf(null);
    }

    public void goTable() {
        SessionHelper.put("isFromMailView", Boolean.TRUE);
        if (!ValidationHelper.isNullOrEmpty(getBillingListPageTab())) {
            RedirectHelper.goToBillingListFromMailManagerView(getBillingListPageTab());
            return;
        }
        if (isOnlyView())
            RedirectHelper.goTo(PageTypes.MAIL_MANAGER_FOLDER);
        else
            RedirectHelper.goToSavePage(PageTypes.MAIL_MANAGER_LIST, null, getTablePage());
    }

    public void processManagedState(boolean redirectToCreateRequest) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        getEntity().setUser(DaoManager.get(User.class, getCurrentUser().getId()));
        DaoManager.save(getEntity(), true);

        if (redirectToCreateRequest) {
            RedirectHelper.goToCreateMultipleRequestFromMail(getEntity().getId(), false, isMultipleCreateRedirect(), getRequestType());
        }
    }

    public void checkFieldsBeforeProcessManagedStateCheck() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        List<String> notSetFields = new ArrayList<>();
        if (ValidationHelper.isNullOrEmpty(getEntity().getClient())) {
            notSetFields.add(ResourcesHelper.getString("client"));
        }
        if (ValidationHelper.isNullOrEmpty(getEntity().getClientInvoice())) {
            notSetFields.add(ResourcesHelper.getString("mailBilling"));
        }
        if (ValidationHelper.isNullOrEmpty(getEntity().getClientFiduciary())) {
            notSetFields.add(ResourcesHelper.getString("mailTrust"));
        }
        if (ValidationHelper.isNullOrEmpty(getEntity().getManagers())) {
            notSetFields.add(ResourcesHelper.getString("clientManager"));
        }

        getEntity().setNdg(getNdg());
        getEntity().setCdr(getCdr());
        getEntity().setReferenceRequest(getReferencePractice());
        if (notSetFields.size() == 0) {
            processManagedStateCheck();
        } else {
            setFieldsNotSetDialogMessage(
                    ResourcesHelper.getString("mailManagerViewFieldsNotSetDialogMessage")
                            + " " + String.join(", ", notSetFields));
            processManagedStateCheck();
        }
    }

    public void processManagedStateCheck() {
        setConfirmButtonIsClicked(Boolean.TRUE);
        setMultipleCreateRedirect(Boolean.TRUE);
        setRequestType(0);
        executeJS("PF('chooseSingleOrMultipleRequestCreateWV').show();");
//
//        saveReference(true);
    }

    public void generatePdfRequestCost() {
        try {
            Document document = DaoManager.get(Document.class, new Criterion[]{
                    Restrictions.eq("mail.id", getEntity().getId())});

            if (!ValidationHelper.isNullOrEmpty(document)) {
                FileHelper.sendFile(document.getTitle() + ".xls", FileHelper.loadContentByPath(document.getPath()));
            } else if (!ValidationHelper.isNullOrEmpty(getEntity().getRequests())) {
                List<Request> requests = getEntity().getRequests().stream().filter(Request::isDeletedRequest).collect(Collectors.toList());

                FileHelper.sendFile("costs" + ".xls", new CreateExcelRequestsReportHelper(true).convertMailDataToExcel(requests, document));

            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void openMailManagerEditor() throws PersistenceBeanException {
        try {
            if (!ValidationHelper.isNullOrEmpty(getEntity().getToBeSentRequests())) {
                List<Long> selectedIds = getEntity().getToBeSentRequests().stream()
                        .filter(r -> r.isSelected())
                        .map(Request::getId)
                        .collect(Collectors.toList());
                if (!ValidationHelper.isNullOrEmpty(selectedIds)) {
                    RedirectHelper.goToMailEditRequestToSent(getEntity().getRequests().get(0).getId(),
                            getEntity().getId(), MailEditType.REQUEST_REPLY_ALL, selectedIds);
                    return;
                }
            }
            RedirectHelper.goToMailEditRequestToSent(getEntity().getRequests().get(0).getId(),
                    getEntity().getId(), MailEditType.REQUEST_REPLY_ALL);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

    }

    public void saveReference(Boolean redirect) throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        if (isConfirmButtonIsClicked()) {
            if (!ValidationHelper.isNullOrEmpty(getSelectedClientInvoiceId())) {
                getEntity().setClientInvoice(DaoManager.get(Client.class, getSelectedClientInvoiceId()));
            } else {
                getEntity().setClientInvoice(null);
            }
            if (!ValidationHelper.isNullOrEmpty(getSelectedClientFiduciaryId())) {
                getEntity().setClientFiduciary(DaoManager.get(Client.class, getSelectedClientFiduciaryId()));
            } else {
                getEntity().setClientFiduciary(null);
            }
            if (!ValidationHelper.isNullOrEmpty(getSelectedOfficeId())) {
                getEntity().setOffice(DaoManager.get(Office.class, getSelectedOfficeId()));
            } else {
                getEntity().setOffice(null);
            }
            getEntity().setManagers(new ArrayList<>());
            if (!ValidationHelper.isNullOrEmpty(getSelectedClientManagers())) {
                getEntity().setManagers(new ArrayList<>());
                List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                        Restrictions.in("id", getSelectedClientManagers().stream()
                                .map(SelectItemWrapper::getId).collect(Collectors.toList()))});
                if (!ValidationHelper.isNullOrEmpty(clients)) {
                    getEntity().setManagers(clients);
                }
            }

            getEntity().setReferenceRequest(getReferencePractice());
            getEntity().setNdg(getNdg());
            getEntity().setCdr(getCdr());

            if (!ValidationHelper.isNullOrEmpty(getSelectedNotManagerOrFiduciaryClientId())) {
                getEntity().setClient(DaoManager.get(Client.class, getSelectedNotManagerOrFiduciaryClientId()));
            } else {
                List<String> onlyEmails = MailHelper.getOnlyEmails(getEntity().getEmailFrom());
                if (!ValidationHelper.isNullOrEmpty(onlyEmails)) {
                    List<Client> clientList = DaoManager.load(Client.class,
                            new CriteriaAlias[]{
                                    new CriteriaAlias("emails", "email", JoinType.INNER_JOIN)},
                            new Criterion[]{
                                    Restrictions.in("email.email", onlyEmails),
                                    Restrictions.or(Restrictions.eq("manager", Boolean.FALSE),
                                            Restrictions.isNull("manager")),
                                    Restrictions.or(Restrictions.eq("fiduciary", Boolean.FALSE),
                                            Restrictions.isNull("fiduciary"))});
                    if (clientList != null && clientList.size() > 0) {
                        getEntity().setClient(clientList.get(0));
                    } else {
                        getEntity().setClient(null);
                    }
                } else {
                    getEntity().setClient(null);
                }
            }
            DaoManager.save(getEntity(), true);
            setDataSaved(Boolean.TRUE);
//            if (!redirect) {
//                RequestContext.getCurrentInstance().execute("PF('saveConfirmationDialogWV').show();");
//            } else {
//                FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, ResourcesHelper.getString("mailManagerSave"), null);
//                FacesContext.getCurrentInstance().addMessage("", facesMessage);
//            }
        }
        if (redirect) {
            processManagedState(true);
        }
    }

    public void openNewMailToManager() throws PersistenceBeanException, IllegalAccessException {
        List<Long> selectedIds = null;
        if (!ValidationHelper.isNullOrEmpty(getEntity().getValidRequests())) {
            selectedIds = getEntity().getValidRequests().stream()
                    .filter(r -> r.isSelectedForMail())
                    .map(Request::getId).collect(Collectors.toList());
        }
        if (!ValidationHelper.isNullOrEmpty(getEntity())) {
            if (!ValidationHelper.isNullOrEmpty(selectedIds)) {
                SessionHelper.put("selectedIds", selectedIds);
            }
            RedirectHelper.goToMailEdit(getEntity().getId(), MailEditType.SEND_TO_MANAGER);
        }
    }

    public void forwardMail() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        RedirectHelper.goToMailEdit(getEntity().getId(), MailEditType.FORWARD);
    }

    public void replyMail() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        RedirectHelper.goToMailEdit(getEntity().getId(), MailEditType.REPLY);
    }

    public void replyToAllMail() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntity())) {
            log.info("before Redirect To MailEdit");
            RedirectHelper.goToMailEdit(getEntity().getId(), MailEditType.REPLY_TO_ALL);
        }
    }

    public void gotoEditRequest() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        RedirectHelper.goToCreateRequestFromMail(getEntity().getId(), getEntityEditId(), false, isMultipleCreateRedirect());
    }

    public void changeStatusNewClientButton() {
        setNewClientButtonClicked(!getNewClientButtonClicked());
        getClientCreateBean().setRenderImportant(false);
    }

    public void clientCreateSave() throws PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        getClientCreateBean().onSave();
        getClientCreateBean().setEntity(new Client());
        changeStatusNewClientButton();
        prepareClients();
    }

    public void editExcelData() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        RedirectHelper.goTo(PageTypes.EXCEL_DATA, getEntity().getId());
    }


//    private void generateMenuModel() {
//        setTopMenuModel(new DefaultMenuModel());
//        if (isMultipleCreate()) {
//            addMenuItem(ResourcesHelper.getString("requestTextEditDataTab"));
//        } else {
//            addMenuItem(ResourcesHelper.getString("requestTextEditDataTab"));
//            if (!ValidationHelper.isNullOrEmpty(getInputCardList())) {
//                getInputCardList()
//                        .forEach(card -> addMenuItem(card.getName().toUpperCase()));
//            }
//        }
//    }

//    private void addMenuItem(String value) {
//        DefaultMenuItem menuItem = new DefaultMenuItem(value);
//
//        menuItem.setCommand("#{mailManagerViewBean.goToTab(" +
//                getTopMenuModel().getElements().size() + ")}");
//        menuItem.setUpdate("form");
//
//        getTopMenuModel().addElement(menuItem);
//    }

    public void setMaxInvoiceNumber(Long invoiceId) throws HibernateException {
        LocalDate currentdate = LocalDate.now();
        int currentYear = currentdate.getYear();

        Long lastInvoiceNumber = 0l;
        try {
            lastInvoiceNumber = (Long) DaoManager.getMax(Invoice.class, "id",
                    new Criterion[]{});
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        if (lastInvoiceNumber == null)
            lastInvoiceNumber = 0l;
        String invoiceNumber = (invoiceId) + "-" + currentYear + "-FE";
        setInvoiceNumber(invoiceNumber);
        setNumber(invoiceId);
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

//    public void sendInvoice() {
//        cleanValidation();
//        if(ValidationHelper.isNullOrEmpty(getSelectedPaymentTypeId())){
//            addRequiredFieldException("form:paymentType");
//            setValidationFailed(true);
//        }
//
//        if(ValidationHelper.isNullOrEmpty(getInvoiceItemAmount())){
//            addRequiredFieldException("form:quantita");
//            setValidationFailed(true);
//        }
//
//        if(ValidationHelper.isNullOrEmpty(getSelectedTaxRateId())){
//            addRequiredFieldException("form:invoiceVat");
//            setValidationFailed(true);
//        }
//
//        if (getValidationFailed()){
//            executeJS("PF('invoiceErrorDialogWV').show();");
//            return;
//        }
//
//        try {
//            Invoice invoice = new Invoice();
//            invoice.setClient(getExamRequest().getClient());
//            invoice.setDate(getInvoiceDate());
//            invoice.setDocumentType(getDocumentType());
//            if(!ValidationHelper.isNullOrEmpty(getSelectedPaymentTypeId()))
//                invoice.setPaymentType(DaoManager.get(PaymentType.class, getSelectedPaymentTypeId()));
//
//            if(!ValidationHelper.isNullOrEmpty(getVatCollectabilityId()))
//                invoice.setVatCollectability(VatCollectability.getById(getVatCollectabilityId()));
//            invoice.setNotes(getInvoiceNote());
//            InvoiceItem invoiceItem = new InvoiceItem();
//            if(!ValidationHelper.isNullOrEmpty(getExamRequest())
//                    && !ValidationHelper.isNullOrEmpty(getExamRequest().getSubject())){
//                invoiceItem.setSubject(getExamRequest().getSubject().toString());
//                invoiceItem.setAmount(getInvoiceItemAmount());
//                if(!ValidationHelper.isNullOrEmpty(getSelectedTaxRateId())){
//                    invoiceItem.setTaxRate(DaoManager.get(TaxRate.class, getSelectedTaxRateId()));
//                }
//                // invoiceItem.setVat(getInvoiceItemVat());
//                invoiceItem.setInvoiceTotalCost(getInvoiceTotalCost());
//            }
//            List<InvoiceItem> invoiceItems = new ArrayList<>();
//            invoiceItems.add(invoiceItem);
//            FatturaAPI fatturaAPI = new FatturaAPI();
//            String xmlData = fatturaAPI.getDataForXML(invoice, invoiceItems);
//            log.info("Mailmanager XMLDATA: " + xmlData);
//            FatturaAPIResponse fatturaAPIResponse = fatturaAPI.callFatturaAPI(xmlData, log);
//
//            if (fatturaAPIResponse != null && fatturaAPIResponse.getReturnCode() != -1) {
//                DaoManager.save(invoice, true);
//                invoiceItem.setInvoice(invoice);
//                DaoManager.save(invoiceItem,true);
//                CollectionUtils.emptyIfNull(getInvoiceRequests())
//                        .stream()
//                        .forEach(r -> {
//                            try {
//                                r.setStateId(RequestState.SENT_TO_SDI.getId());
//                                r.setInvoice(invoice);
//                                DaoManager.save(r, true);
//                            } catch (PersistenceBeanException e) {
//                                log.error("error in saving request after sending invoice ", e);
//                            }
//                        });
//
//                setInvoiceSentStatus(true);
//                executeJS("PF('invoiceDialogWV').hide();");
//            } else {
//                setApiError(ResourcesHelper.getString("sendInvoiceErrorMsg"));
//                if(fatturaAPIResponse != null
//                        && !ValidationHelper.isNullOrEmpty(fatturaAPIResponse.getDescription())){
//
//                    if(fatturaAPIResponse.getDescription().contains("already exists")) {
//                        setApiError(ResourcesHelper.getString("sendInvoiceDuplicateMsg"));
//                    }else
//                        setApiError(fatturaAPIResponse.getDescription());
//                }
//                executeJS("PF('sendInvoiceErrorDialogWV').show();");
//            }
//
//        }catch(Exception e) {
//            e.printStackTrace();
//            LogHelper.log(log, e);
//            executeJS("PF('sendInvoiceErrorDialogWV').show();");
//        }
//    }

    public final void onTabChange(final TabChangeEvent event) throws HibernateException,
            InstantiationException, IllegalAccessException, PersistenceBeanException, IOException {
        TabView tv = (TabView) event.getComponent();
        this.activeTabIndex = tv.getActiveIndex();
        //SessionHelper.put("activeTabIndex", activeTabIndex);
        if (activeTabIndex == 3) {
            Invoice invoice = DaoManager.get(Invoice.class, getNumber());
            if (ValidationHelper.isNullOrEmpty(invoice.getEmail())) {
                attachInvoiceData();
            } else {
                fillAttachedFiles(invoice.getEmail());
            }
        }
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
        if(!ValidationHelper.isNullOrEmpty(invoiceDb.getClient()))
            paymentTypes = ComboboxHelper.fillList(invoiceDb.getClient().getPaymentTypeList(), Boolean.TRUE);
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
            if(!ValidationHelper.isNullOrEmpty(getEntity().getManagers())) {
                List<Client> managers = getEntity().getManagers();
                String managerNames = "";
                for(Client client: managers) {
                    if(!ValidationHelper.isNullOrEmpty(client.getClientName())) {
                        if(!managerNames.isEmpty())
                            managerNames = managerNames + ", ";
                        managerNames = managerNames + client.getClientName();
                    }
                }
                if(!managerNames.isEmpty())
                    gestore = "GESTORE: " + managerNames;
            }
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

    public void createInvoice() throws Exception {
        List<Request> selectedRequestList = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getRequestsConsideredForInvoice())) {
            selectedRequestList = getRequestsConsideredForInvoice().stream()
                    .filter(r -> r.isSelectedForInvoice())
                    .collect(Collectors.toList());
        }
        Invoice invoice = new Invoice();
        if(!ValidationHelper.isNullOrEmpty(getEntity().getClientInvoice()))
            invoice.setClient(getEntity().getClientInvoice());
        else
            throw new Exception("Client invoice is null, Can't create invoice");

//        if(selectedRequestList.size() > 0) {
//            //invoice.setClient(selectedRequestList.get(0).getClient());
//
//        }

        setSelectedInvoiceClient(invoice.getClient());
        if(!ValidationHelper.isNullOrEmpty(getSelectedInvoiceClient()))
            setSelectedInvoiceClientId(getSelectedInvoiceClient().getId());
        invoice.setDate(getInvoiceDate());
        invoice.setDate(new Date());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setEmailFrom(getEntity());
        // DaoManager.save(invoice, true);

        setSelectedInvoiceItems(InvoiceHelper.groupingItemsByTaxRate(selectedRequestList, getCausal()));
      /*  for (InvoiceItem invoiceItem : invoiceItems) {
            invoiceItem.setInvoice(invoice);
            DaoManager.save(invoiceItem, true);
        }
        CollectionUtils.emptyIfNull(selectedRequestList).stream().forEach(r -> {
            try {
                r.setInvoice(invoice);
                DaoManager.save(r, true);
            } catch (PersistenceBeanException e) {
                log.error("error in saving invoice in request after creating invoice ", e);
            }
        });*/
        //   setShowCreateFatturaButton(Boolean.FALSE);
        setInvoicedRequests(selectedRequestList);
        invoice.setTotalGrossAmount(getTotalGrossAmount());
        // DaoManager.save(invoice, true);
        loadInvoiceDialogData(invoice);
        executeJS("PF('invoiceDialogBillingWV').show();");
    }

    private GoodsServicesFieldWrapper createGoodsServicesFieldWrapper() throws IllegalAccessException, PersistenceBeanException {
        GoodsServicesFieldWrapper wrapper = new GoodsServicesFieldWrapper();
        ums = new ArrayList<>();
        ums.add(new SelectItem("pz", "pz"));
        wrapper.setUms(ums);
        wrapper.setVatAmounts(ComboboxHelper.fillList(TaxRate.class, Order.asc("description"), new CriteriaAlias[]{}, new Criterion[]{
                Restrictions.eq("use", Boolean.TRUE)
        }, true, false));
        wrapper.setTotalLine(0D);
        return wrapper;
    }

    public void createNewGoodsServicesFields() throws IllegalAccessException, PersistenceBeanException {
        GoodsServicesFieldWrapper wrapper = createGoodsServicesFieldWrapper();
        int size = getGoodsServicesFields().size();
        wrapper.setCounter(size + 1);
        getGoodsServicesFields().add(wrapper);
    }

    public void saveInvoiceInDraft() {
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

        if (getValidationFailed()) {
            executeJS("PF('invoiceErrorDialogWV').show();");
            return;
        }
        try {
            saveInvoice(InvoiceStatus.DRAFT, true);
            loadInvoiceDialogData(getSelectedInvoice());
            setShowCreateFatturaButton(Boolean.FALSE);
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
        }
        executeJS("PF('invoiceDialogBillingWV').hide();");
        closeInvoiceDialog();
    }

    public Invoice saveInvoice(InvoiceStatus invoiceStatus, Boolean saveInvoiceNumber) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        //- Invoice invoice = DaoManager.get(Invoice.class, getNumber());
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
        getSelectedInvoice().setNotes(getInvoiceNote());
        getSelectedInvoice().setStatus(invoiceStatus);

        getSelectedInvoice().setTotalGrossAmount(getTotalGrossAmount());
        if (!ValidationHelper.isNullOrEmpty(getEntity().getNdg()))
        	getSelectedInvoice().setNdg(getEntity().getNdg());
        if (!ValidationHelper.isNullOrEmpty(getEntity().getOffice()))
        	getSelectedInvoice().setOffice(getEntity().getOffice());
        if (!ValidationHelper.isNullOrEmpty(getEntity().getReferenceRequest()))
        	getSelectedInvoice().setPractice(getEntity().getReferenceRequest());
        DaoManager.save(getSelectedInvoice(), true);
        setMaxInvoiceNumber(getSelectedInvoice().getId());
        if (saveInvoiceNumber) {
            getSelectedInvoice().setNumber(getNumber());
            getSelectedInvoice().setInvoiceNumber(getInvoiceNumber());
        }
        if (!ValidationHelper.isNullOrEmpty(getEntity().getManagers())) {
        	getSelectedInvoice().setManagers(new ArrayList<>(getEntity().getManagers()));
        }
        DaoManager.save(getSelectedInvoice(), true);
        for (GoodsServicesFieldWrapper goodsServicesFieldWrapper : getGoodsServicesFields()) {
            if (!ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getInvoiceItemId())) {
                InvoiceItem invoiceItem = getSelectedInvoiceItems()
                        .stream().filter(inv ->
                                (inv.getId() != null
                                        && inv.getId().equals(goodsServicesFieldWrapper.getInvoiceItemId()))
                        ).findFirst().get();
                // InvoiceItem invoiceItem = DaoManager.get(InvoiceItem.class, goodsServicesFieldWrapper.getInvoiceItemId());
                invoiceItem.setAmount(goodsServicesFieldWrapper.getInvoiceItemAmount());
                invoiceItem.setTaxRate(DaoManager.get(TaxRate.class, goodsServicesFieldWrapper.getSelectedTaxRateId()));
                invoiceItem.setDescription(goodsServicesFieldWrapper.getDescription());
                invoiceItem.setInvoiceTotalCost(goodsServicesFieldWrapper.getInvoiceTotalCost());
                invoiceItem.setInvoice(getSelectedInvoice());
                DaoManager.save(invoiceItem, true);
            } else {
                InvoiceItem invoiceItem = new InvoiceItem();
                invoiceItem.setAmount(goodsServicesFieldWrapper.getInvoiceItemAmount());
                invoiceItem.setTaxRate(DaoManager.get(TaxRate.class, goodsServicesFieldWrapper.getSelectedTaxRateId()));
                invoiceItem.setDescription(goodsServicesFieldWrapper.getDescription());
                invoiceItem.setInvoiceTotalCost(goodsServicesFieldWrapper.getInvoiceTotalCost());
                invoiceItem.setInvoice(getSelectedInvoice());
                invoiceItem.setSubject(getExamRequest().getSubject().toString());
                DaoManager.save(invoiceItem, true);
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

        if (getValidationFailed()) {
            executeJS("PF('invoiceErrorDialogWV').show();");
            return;
        }

        try {

            saveInvoiceInDraft();
            Invoice invoice = DaoManager.get(Invoice.class, getNumber());
            List<InvoiceItem> invoiceItems = DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoice)});
            FatturaAPI fatturaAPI = new FatturaAPI();
            String xmlData = fatturaAPI.getDataForXML(invoice, invoiceItems);
            log.info("Mailmanager XMLDATA: " + xmlData);
            FatturaAPIResponse fatturaAPIResponse = fatturaAPI.callFatturaAPI(xmlData, log);

            if (fatturaAPIResponse != null && fatturaAPIResponse.getReturnCode() != -1) {
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
                    } else
                        setApiError(fatturaAPIResponse.getDescription());
                }
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
        closeInvoiceDialog();
    }

//    public List<String> completeMailFrom(String query) {
//        return completeField(query, "email_from");
//    }

    public List<String> completeDestinations(String query) {
        return completeField(query, "email_to");
    }

    public List<String> completeMailCC(String query) {
        return completeField(query, "email_cc");
    }

    private List<String> completeField(String query, String field) {
        try {
            List<String> filterList = new ArrayList<>();
            Session session = DaoManager.getSession();
            ((List<String>) session.createSQLQuery("SELECT DISTINCT " + field + " FROM wlg_inbox wlg " +
                    "WHERE " + field + " LIKE '%" + query + "%' AND " + "(" + field + " LIKE '%,%' " +
                    "OR NOT EXISTS(SELECT 1 FROM email_remove WHERE wlg." + field + " LIKE CONCAT('%', email, '%')))")
                    .list()).stream()
                    .map(MailHelper::parseMailAddress)
                    .flatMap(List::stream)
                    .filter(item -> item.toLowerCase().contains(query.toLowerCase()))
                    .filter(item -> !filterList.contains(item))
                    .filter(MailHelper::checkRemoveMailAddress)
                    .forEach(filterList::add);
            return filterList;
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e.getMessage());
        }
        return Collections.emptyList();
    }

    public void deleteEmailTo() throws PersistenceBeanException {
        String email = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("param");
        if (!ValidationHelper.isNullOrEmpty(email)) {
            deleteEmail(MailHelper.prepareEmailToSend(email));
            getSendTo().remove(getSendTo().size() - 1);
        }
    }

    public void deleteEmailCC() throws PersistenceBeanException {
        String email = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("param");
        if (!ValidationHelper.isNullOrEmpty(email)) {
            deleteEmail(MailHelper.prepareEmailToSend(email));
            getSendCC().remove(getSendCC().size() - 1);
        }
    }

//    public void deleteEmailFrom() throws PersistenceBeanException {
//        String email = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("param");
//        if (!ValidationHelper.isNullOrEmpty(email)) {
//            deleteEmail(MailHelper.prepareEmailToSend(email));
//            getSendFrom().remove(getSendFrom().size() - 1);
//        }
//    }

    public void deleteEmail(String email) throws PersistenceBeanException {
        EmailRemove remove = new EmailRemove();
        remove.setEmail(email);
        DaoManager.save(remove, true);
    }

    public void updateFrom() {
        if (!ValidationHelper.isNullOrEmpty(getSendFrom())) {
            setEmailFrom(getSendFrom().stream()
                    .map(MailHelper::prepareEmailToSend).collect(Collectors.joining(DELIM)));
        } else {
            setEmailFrom(null);
        }
    }

    public void updateDestination() {
        if (!ValidationHelper.isNullOrEmpty(getSendTo())) {
            setEmailTo(getSendTo().stream()
                    .map(MailHelper::prepareEmailToSend).collect(Collectors.joining(DELIM)));
        } else {
            setEmailTo(null);
        }
    }

    public void updateCC() {
        if (!ValidationHelper.isNullOrEmpty(getSendCC())) {
            setEmailCC(getSendCC().stream()
                    .map(MailHelper::prepareEmailToSend).collect(Collectors.joining(DELIM)));
        } else {
            setEmailCC(null);
        }
    }

    public void saveMailInDraft() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        updateFrom();
        updateDestination();
        updateCC();

        saveMail(MailManagerStatuses.NEW.getId());
    }

    public WLGInbox saveMail(Long mailManagerStatus) throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {
        String emailsFrom = DaoManager.loadField(WLGServer.class, "login",
                String.class, new Criterion[]{Restrictions.eq("id", Long.parseLong(
                        ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.SENT_SERVER_ID)
                                .getValue()))}).get(0);
        Invoice invoice = DaoManager.get(Invoice.class, getNumber());
        WLGInbox inbox = new WLGInbox();
        if (!ValidationHelper.isNullOrEmpty(invoice.getEmail())) {
            inbox = DaoManager.get(WLGInbox.class, invoice.getEmail().getId());
        }
        inbox.setEmailFrom(emailsFrom);
        inbox.setEmailTo(getEmailTo());
        inbox.setEmailCC(getEmailCC());
        inbox.setEmailSubject(getEmailSubject());
        inbox.setEmailBody(MailHelper.htmlToText(getEmailBodyToEditor()));
        inbox.setEmailBodyHtml(getEmailBodyToEditor());
        inbox.setClient(getSelectedInvoiceClient());
        inbox.setClient(getExamRequest().getClient());
        if (inbox.isNew())
            log.info("setting new mail state to :: " + MailManagerStatuses.findById(mailManagerStatus));
        else
            log.info("setting mail :: " + inbox.getId() + " state to :: " + MailManagerStatuses.findById(mailManagerStatus)
                    + " by user:: " + UserHolder.getInstance().getCurrentUser().getId());
        inbox.setState(mailManagerStatus);
        inbox.setSendDate(new Date());
        inbox.setReceiveDate(new Date());
        DaoManager.save(inbox, true);
        invoice.setEmail(inbox);
        DaoManager.save(invoice, true);
        saveFiles(inbox, true);
//        loadDraftEmail();
        loadInvoiceDialogData(invoice);
        return inbox;
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
            if (!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom())) {
//                appendReplyFooter();
//                String emailsFrom = DaoManager.loadField(WLGServer.class, "login",
//                        String.class, new Criterion[]{Restrictions.eq("id", Long.parseLong(
//                                ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.SENT_SERVER_ID)
//                                        .getValue()))}).get(0);
//                if (invoice.getEmailFrom().getEmailCC().contains(emailsFrom)) {
//                    sendTo = new LinkedList<>();
//                    sendTo.addAll(MailHelper.parseMailAddress(invoice.getEmailFrom().getEmailFrom()));
//                    sendTo.addAll(MailHelper.parseMailAddress(invoice.getEmailFrom().getEmailTo()));
//                    sendCC = new LinkedList<>();
//                    sendCC = MailHelper.parseMailAddress(invoice.getEmailFrom().getEmailCC()).stream()
//                            .filter(m -> !m.contains(emailsFrom)).collect(Collectors.toList());
//                } else {
//                    sendTo = MailHelper.parseMailAddress(invoice.getEmailFrom().getEmailFrom());
//                    sendCC = new LinkedList<>();
//                    sendCC.addAll(MailHelper.parseMailAddress(invoice.getEmailFrom().getEmailTo()).stream()
//                            .filter(m -> !m.contains(emailsFrom)).collect(Collectors.toList()));
//                    sendCC.addAll(MailHelper.parseMailAddress(invoice.getEmailFrom().getEmailCC()));
//                }
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
//            setEmailBodyToEditor(getEntity().getEmailBodyToEditor());
            setEmailBodyToEditor(createInvoiceEmailDefaultBody(invoice));
        }
    }

    public void sendMail() throws PersistenceBeanException, IllegalAccessException, HibernateException, InstantiationException {
        cleanValidation();
        if (getValidationFailed()) {
            return;
        }
        updateFrom();
        updateDestination();
        updateCC();

        WLGInbox wlgInbox = saveMail(MailManagerStatuses.NEW.getId());

        try {
            MailHelper.sendMail(wlgInbox,(addAttachment ? getInvoiceEmailAttachedFiles() : null), null);
            log.info("Mail is sent");
            if (!ValidationHelper.isNullOrEmpty(getBaseMailId())) {
                wlgInbox.setRecievedInbox(DaoManager.get(WLGInbox.class, getBaseMailId()));
                DaoManager.save(wlgInbox, true);
            }
            if (!ValidationHelper.isNullOrEmpty(getEntity().getManagers())) {
                wlgInbox.setManagers(new ArrayList<>(getEntity().getManagers()));
                DaoManager.save(wlgInbox, true);
            }
            wlgInbox.setServerId(Long.parseLong(ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.SENT_SERVER_ID).getValue()));
            DaoManager.save(wlgInbox, true);
            List<Request> selectedRequestList = new ArrayList<>();
            if (!ValidationHelper.isNullOrEmpty(getEntity().getValidRequests())) {
                selectedRequestList = getEntity().getValidRequests().stream().filter(r -> r.isSelectedForInvoice())
                        .collect(Collectors.toList());
            }
            CollectionUtils.emptyIfNull(selectedRequestList).stream().forEach(r -> {
                try {
                    r.setStateId(RequestState.INVOICED.getId());
                    DaoManager.save(r, true);
                } catch (PersistenceBeanException e) {
                    log.error("error in saving request after sending mail ", e);
                }
            });
        } catch (Exception e) {
            log.info("Mail is not sent");
            LogHelper.log(log, e);
            executeJS("showNotSendMsg();");
            return;
        }

    }

    public List<SelectItem> getClientTypes() {
        return clientTypes;
    }

    public void setClientTypes(List<SelectItem> clientTypes) {
        this.clientTypes = clientTypes;
    }

    public void assosiateCancel() {
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_VIEW, getEntityId());
    }

    private void attachInvoiceData() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {
        setInvoiceEmailAttachedFiles(new ArrayList<>());
        String refrequest = "";
        String ndg = "";
        WLGInbox baseMail = DaoManager.get(WLGInbox.class, getBaseMailId());
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
        Invoice invoice = DaoManager.get(Invoice.class, getNumber());
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
            //attachInvoicePdf(baos);
            attachInvoicePdf(invoice);
        }
        //invoiceDialogBean.attachCourtesyInvoicePdf(invoice);
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

    private void attachInvoicePdf(Invoice invoice) {
        try {
            Date currentDate = new Date();
            String fileName = "Richieste_Invoice_" + DateTimeHelper.toFileDateWithMinutes(currentDate);
            pdfInvoice = new WLGExport();
            pdfInvoice.setExportDate(currentDate);
            DaoManager.save(pdfInvoice, true);
            String sb = pdfInvoice.generateDestinationPath(fileName);
            Files.createDirectories(Paths.get(sb));
            String pdfFilePath = sb + File.separator + fileName + ".pdf";
            invoiceDialogBean.attachInvoicePdf(invoice, pdfFilePath);
            pdfInvoice.setDestinationPath(pdfFilePath);
            DaoManager.save(pdfInvoice, true);
            LogHelper.log(log, pdfInvoice.getId() + " " + pdfFilePath);
            addAttachedFile(pdfInvoice);
        }catch (Exception e) {
            LogHelper.log(log, e);
        }
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

    public void downloadInvoiceFile() {
        FileWrapper wrapper = getInvoiceEmailAttachedFiles().stream().filter(w -> w.getId().equals(getDownloadFileIndex().longValue()))
                .findAny().orElse(null);
        if (!ValidationHelper.isNullOrEmpty(wrapper)) {
            if (ValidationHelper.isNullOrEmpty(wrapper.getFilePath())) {
                log.warn("File download error: Document is null");
                return;
            }

            File file = new File(wrapper.getFilePath());
            try {
                FileHelper.sendFile(wrapper.getFileName(), new FileInputStream(file), (int) file.length());
            } catch (FileNotFoundException e) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                        ResourcesHelper.getValidation("noDocumentOnServer"), "");
            }
        }
    }

//    public String prepareInvoicePdf(DocumentType documentType) throws
//            IOException, PersistenceBeanException, IllegalAccessException, InstantiationException {
//        Invoice invoice = DaoManager.get(Invoice.class, getNumber());
//        String body = invoiceDialogBean.getPdfRequestBody(invoice);
//        log.info("Invoice PDF body" + body);
//        byte [] fileContent  = PrintPDFHelper.convertToPDF(null, body, null, documentType);
//
//        if (fileContent != null) {
//            String fileName = "FE-" + getNumber() + " "
//                    + getSelectedInvoice().getClient().getClientName();
//            String tempDir = FileHelper.getLocalTempDir();
//            tempDir  += File.separator + UUID.randomUUID();
//            FileUtils.forceMkdir(new File(tempDir));
//            String tempPdf = tempDir +  File.separator +  fileName +".pdf";
//            InputStream stream = new ByteArrayInputStream(fileContent);
//            File targetFile = new File(tempPdf);
//            OutputStream outStream = new FileOutputStream(targetFile);
//            byte[] buffer = new byte[8 * 1024];
//            int bytesRead;
//            while ((bytesRead = stream.read(buffer)) != -1) {
//                outStream.write(buffer, 0, bytesRead);
//            }
//            IOUtils.closeQuietly(stream);
//            IOUtils.closeQuietly(outStream);
//            return tempPdf;
//        }
//        return "";
//    }

    public void downloadInvoicePdf() {
        try {
            String refrequest = "";
            WLGInbox baseMail = DaoManager.get(WLGInbox.class, getBaseMailId());
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
            }
            String excelPdf = "";
            Date currentDate = new Date();
            String fileName = "Richieste_Invoice_" + DateTimeHelper.toFileDateWithMinutes(currentDate);
            if (!ValidationHelper.isNullOrEmpty(getInvoicedRequests())) {
                byte[] baos = getXlsBytes(refrequest, getInvoicedRequests().get(0));
                if (!ValidationHelper.isNullOrEmpty(baos)) {
                    String tempDir = FileHelper.getLocalTempDir();
                    tempDir  += File.separator + UUID.randomUUID();
                    FileUtils.forceMkdir(new File(tempDir));
                    File filePath = new File(tempDir);
                    String excelFile = FileHelper.writeFileToFolder(fileName + ".xls", filePath, baos);
                    String sofficeCommand =
                            ApplicationSettingsHolder.getInstance().getByKey(
                                    ApplicationSettingsKeys.SOFFICE_COMMAND).getValue().trim();
                    Process p = Runtime.getRuntime().exec(new String[] { sofficeCommand, "--headless",
                            "--convert-to", "pdf","--outdir", tempDir, excelFile });
                    p.waitFor();
                    excelPdf = tempDir + File.separator + fileName + ".pdf";
                    FileHelper.delete(excelFile);
                }
            }
            Invoice invoice = DaoManager.get(Invoice.class, getNumber());
            String tempPdf = invoiceDialogBean.prepareInvoicePdf(DocumentType.COURTESY_INVOICE, invoice);
            String sb = MailHelper.getDestinationPath() +
                    DateTimeHelper.ToFilePathString(new Date());

            String filePathStr = sb + File.separator + fileName + ".pdf";
            Path pdfFilePath = Paths.get(filePathStr);
            if(Files.notExists(pdfFilePath.getParent()))
                Files.createDirectories(pdfFilePath.getParent());
            PDFMergerUtility obj = new PDFMergerUtility();
            obj.setDestinationFileName(filePathStr);
            File file1 = new File(tempPdf);
            obj.addSource(file1);
            if(!ValidationHelper.isNullOrEmpty(excelPdf)){
                File file2 = new File(excelPdf);
                obj.addSource(file2);
            }
            obj.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
            FileHelper.delete(tempPdf);
            FileHelper.delete(excelPdf);
            byte[] fileContent = FileHelper.loadContentByPath(filePathStr);
            if (fileContent != null) {
                InputStream stream = new ByteArrayInputStream(fileContent);
                invoicePDFFile = new DefaultStreamedContent(stream, FileHelper.getFileExtension(filePathStr),
                        fileName + ".pdf");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void downloadInvoicePdf_old() {
        try {

            //String refrequest = "";
            //String ndg = "";
            String templatePath  = (new File(FileHelper.getRealPath(),
                    "resources" + File.separator + "layouts" + File.separator
                            + "Invoice" + File.separator + "InvoiceDocumentTemplate.docx")
                    .getAbsolutePath());

            Double imponibile = 0.0;
            Double totalIva = 0.0;
            Double ivaPercentage = 0.0;

            Invoice invoice = DaoManager.get(Invoice.class, getNumber());
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
                ivaPercentage = ivaPercentage/ items.size();
                BigDecimal ivaPer = BigDecimal.valueOf(ivaPercentage);
                ivaPer = ivaPer.setScale(2, RoundingMode.HALF_UP);
                ivaPercentage = ivaPer.doubleValue();

                BigDecimal totIva = BigDecimal.valueOf(totalIva);
                totIva = totIva.setScale(2, RoundingMode.HALF_UP);
                totalIva = totIva.doubleValue();

//                Date currentDate = new Date();
//                String fileName = "Richieste_Invoice_"+DateTimeHelper.toFileDateWithMinutes(currentDate);
                String fileName = "FE-" + getNumber() + " " + getSelectedInvoice().getClient().getClientName();
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
                                }/*else if (text != null && text.contains("refrequest")) {
	                                text = text.replace("refrequest",refrequest);
	                                r.setText(text, 0);
	                            }else if (text != null && text.contains("inboxndg")) {
	                                text = text.replace("inboxndg",ndg);
	                                r.setText(text, 0);
	                            }*/
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

                String tempPdf = invoiceDialogBean.prepareInvoicePdf(DocumentType.INVOICE, invoice);
                String tempFilePathStr = sb + File.separator + fileName + "_temp.pdf";
                String filePathStr = sb + File.separator + fileName + ".pdf";
                PDFMergerUtility obj = new PDFMergerUtility();
                obj.setDestinationFileName(filePathStr);
                File file1 = new File(tempPdf);
                File file2 = new File(tempFilePathStr);
                obj.addSource(file1);
                obj.addSource(file2);
                obj.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
                FileHelper.delete(tempPdf);
                FileHelper.delete(tempFilePathStr);
                byte[] fileContent = FileHelper.loadContentByPath(filePathStr);
                if (fileContent != null) {
                    InputStream stream = new ByteArrayInputStream(fileContent);
                    invoicePDFFile = new DefaultStreamedContent(stream, FileHelper.getFileExtension(filePathStr),
                            fileName + ".pdf");
                }
            }
        }catch(Exception e){
            LogHelper.log(log,e);
        }
    }

    private void saveFiles(WLGInbox inbox, boolean transaction) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getInvoiceEmailAttachedFiles())) {
            for (FileWrapper wrapper : getInvoiceEmailAttachedFiles()) {
                WLGExport export = DaoManager.get(WLGExport.class, new Criterion[]{
                        Restrictions.eq("id", wrapper.getId())
                });
                export.setExportDate(new Date());
                export.setSourcePath(String.format("\\%s", inbox.getId()));
                export.setInbox(inbox);
                DaoManager.save(export, transaction);
            }
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

    public void openInvoiceDialog() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {

        if (!ValidationHelper.isNullOrEmpty(getExamRequest())
                && !ValidationHelper.isNullOrEmpty(getExamRequest().getInvoice())) {
            Invoice invoice = DaoManager.get(Invoice.class, getExamRequest().getInvoice().getId());
            loadInvoiceDialogData(invoice);
            executeJS("PF('invoiceDialogBillingWV').show();");
        }
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


    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public void newInvoiceMail() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntity())) {
            RedirectHelper.goToMailEdit(getEntity().getId(), MailEditType.SEND_INVOICE);
        }
    }

    public void saveInvoicePayment() throws HibernateException, PersistenceBeanException, IllegalAccessException {
        PaymentInvoice paymentInvoice = new PaymentInvoice();
        paymentInvoice.setPaymentImport(getPaymentAmount());
        paymentInvoice.setDate(getPaymentDate());
        paymentInvoice.setDescription(getPaymentDescription());
        paymentInvoice.setInvoice(getSelectedInvoice());
        DaoManager.save(paymentInvoice, true);
        List<PaymentInvoice> paymentInvoicesList = DaoManager.load(PaymentInvoice.class, new Criterion[]{Restrictions.eq("invoice", getSelectedInvoice())}, new Order[]{
                Order.asc("date")});
        setPaymentInvoices(paymentInvoicesList);
    }

    public void attachCourtesyInvoicePdf() {
        try {
            //String refrequest = "";
            //String ndg = "";
            String templatePath = (new File(FileHelper.getRealPath(),
                    "resources" + File.separator + "layouts" + File.separator
                            + "Invoice" + File.separator + "InvoiceDocumentTemplate.docx")
                    .getAbsolutePath());

            Double imponibile = 0.0;
            Double totalIva = 0.0;
            Double ivaPercentage = 0.0;
            Invoice invoice = DaoManager.get(Invoice.class, getNumber());
            if (!ValidationHelper.isNullOrEmpty(invoice)) {
                List<InvoiceItem> items = DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoice)});
                for (InvoiceItem item : items) {
                    double total = 0.0;
                    double amount = 0.0;
                    double totalCost = 0.0;

                    if (item.getAmount() != null)
                        amount = item.getAmount();
                    if (item.getInvoiceTotalCost() != null)
                        totalCost = item.getInvoiceTotalCost();
                    if (amount != 0.0)
                        imponibile = imponibile + (amount * totalCost);
                    else
                        imponibile = imponibile + totalCost;
                    if (amount != 0.0)
                        total = amount * totalCost;
                    else
                        total = totalCost;
                    if (item.getVat() != null) {
                        ivaPercentage = ivaPercentage + item.getVat();
                        totalIva = totalIva + ((item.getVat() * total) / 100);
                    }
                }
                ivaPercentage = ivaPercentage / items.size();
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
                String fileName = "Fattura_cortesia_" + getInvoiceNumber();

                String tempDir = FileHelper.getLocalTempDir();
                tempDir += File.separator + UUID.randomUUID();
                FileUtils.forceMkdir(new File(tempDir));
                String tempDoc = tempDir + File.separator + fileName + ".docx";

                try (XWPFDocument doc = new XWPFDocument(
                        Files.newInputStream(Paths.get(templatePath)))) {
                    for (XWPFParagraph p : doc.getParagraphs()) {
                        List<XWPFRun> runs = p.getRuns();
                        if (runs != null) {
                            for (XWPFRun r : runs) {
                                String text = r.getText(0);
                                String replace = "";
                                if (text != null && text.contains("inum")) {
                                    if (!ValidationHelper.isNullOrEmpty(invoice.getInvoiceNumber()))
                                        replace = invoice.getInvoiceNumber();
                                    text = text.replace("inum", replace);
                                    r.setText(text, 0);
                                } else if (text != null && text.contains("clientname")) {

                                    if (!ValidationHelper.isNullOrEmpty(invoice.getClient()))
                                        replace = invoice.getClient().toString();
                                    text = text.replace("clientname", replace);
                                    r.setText(text, 0);
                                } else if (text != null && text.contains("clientaddress")) {
                                    if (!ValidationHelper.isNullOrEmpty(invoice.getClient()) &&
                                            !ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressStreet()))
                                        replace = invoice.getClient().getAddressStreet();
                                    text = text.replace("clientaddress", replace);
                                    r.setText(text, 0);
                                } else if (text != null && text.contains("clientaddress2")) {
                                    if (!ValidationHelper.isNullOrEmpty(invoice.getClient()) &&
                                            !ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressPostalCode()))
                                        replace = invoice.getClient().getAddressPostalCode();

                                    if (!ValidationHelper.isNullOrEmpty(invoice.getClient()) &&
                                            !ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressProvinceId())) {
                                        Province province = invoice.getClient().getAddressProvinceId();
                                        replace = province.getDescription() + "(" + province.getCode() + ")";
                                    }
                                    text = text.replace("clientaddress2", replace);
                                    r.setText(text, 0);
                                } else if (text != null && text.contains("clientpiva")) {
                                    if (!ValidationHelper.isNullOrEmpty(invoice.getClient()) &&
                                            !ValidationHelper.isNullOrEmpty(invoice.getClient().getFiscalCode()))
                                        replace = invoice.getClient().getFiscalCode();
                                    text = text.replace("clientpiva", replace);
                                    r.setText(text, 0);
                                } else if (text != null && text.contains("impon")) {
                                    text = text.replace("impon", imponibile.toString());
                                    r.setText(text, 0);
                                } else if (text != null && text.contains("ivap")) {
                                    text = text.replace("ivap", ivaPercentage.toString() + "%");
                                    r.setText(text, 0);
                                } else if (text != null && text.contains("ivaa")) {
                                    text = text.replace("ivaa", totalIva.toString());
                                    r.setText(text, 0);
                                } else if (text != null && text.contains("totale")) {
                                    Double total = imponibile + totalIva;
                                    text = text.replace("totale", total.toString());
                                    r.setText(text, 0);
                                }/*else if (text != null && text.contains("refrequest")) {
	                                text = text.replace("refrequest",refrequest);
	                                r.setText(text, 0);
	                            }else if (text != null && text.contains("inboxndg")) {
	                                text = text.replace("inboxndg",ndg);
	                                r.setText(text, 0);
	                            }*/
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
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void appendReplyFooter() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        WLGInbox baseMail = DaoManager.get(WLGInbox.class, getBaseMailId());
        String sendDate;
        if (getEntity().getSendDate() == null) {
            sendDate = "";
        } else if (baseMail.getReceived()) {
            sendDate = DateTimeHelper.toFormatedStringLocal(getEntity().getSendDate(),
                    DateTimeHelper.getMySQLDateTimePattern(), null);
        } else {
            sendDate = DateTimeHelper.ToMySqlStringWithSeconds(getEntity().getSendDate());
        }
        getEntity().setEmailPrefix(String.format(MAIL_RERLY_FOOTER,
                prepareEmailAddress(getEntity().getEmailFrom()),
                sendDate,
                prepareEmailAddress(getEntity().getEmailTo()),
                getEntity().getEmailCC(),
                getEntity().getEmailSubject()));
    }

    private String prepareEmailAddress(String email) {
        return email.replace("<", "&lt;").replace(">", "&gt;");
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

    public void checkDocument(boolean otherRequests) throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        setRequestsConsideredForInvoice(null);
        Document document = DaoManager.get(Document.class, new Criterion[] { Restrictions.eq("mail.id", getEntity().getId())});
        if (!ValidationHelper.isNullOrEmpty(document) && !ValidationHelper.isNullOrEmpty(document.getTypeId())
                && document.getTypeId().equals(DocumentType.INVOICE_REPORT.getId())) {
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
                List<Request> requestListForInvoice = getEntity().getRequests().stream()
                        .filter(x ->  x.isDeletedRequest() && ValidationHelper.isNullOrEmpty(x.getInvoice())
                                && !ValidationHelper.isNullOrEmpty(x.getStateId())
                                && (RequestState.EVADED.getId().equals(x.getStateId())))
                        .collect(Collectors.toList());
                if (!ValidationHelper.isNullOrEmpty(requestListForInvoice)) {
                    requestListForInvoice.stream().forEach(r -> {
                        r.setSelectedForInvoice(true);
                    });
                    getRequestsConsideredForInvoice().addAll(requestListForInvoice);
                }
            }
            executeJS("PF('mailManagerViewRequestsForInvoiceDlg').show();");
        } else {
            executeJS("PF('invoiceOpenErrorDialogWV').show();");
        }
    }


    public boolean isAddAttachment() {
        return addAttachment;
    }

    public void setAddAttachment(boolean addAttachment) {
        this.addAttachment = addAttachment;
    }

    public String createInvoiceEmailDefaultBody(Invoice invoice) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        String emailBody = "";
        if(!invoice.isNew()) {
            String dearCustomer = ResourcesHelper.getString("dearCustomer");
            String attachedCopyMessage = ResourcesHelper.getString("attachedCopyMessage");
            String thanksMessage = ResourcesHelper.getString("thanksMessage");
            String reference = "";
            String ndg = "";
            if (!ValidationHelper.isNullOrEmpty(getEntity().getNdg()))
                ndg = "NDG: " + getEntity().getNdg();
            if (!ValidationHelper.isNullOrEmpty(getEntity().getReferenceRequest()))
                reference = "RIF: " + getEntity().getReferenceRequest() + " ";
            String requestType = "";
            List<Request> requests = DaoManager.load(Request.class, new Criterion[]{Restrictions.eq("invoice", invoice)});
            Set<String> requestTypeSet = new HashSet<>();
            requests.stream().forEach(request -> {
                requestTypeSet.add(request.getRequestType().getName());
            });
            for(String request: requestTypeSet){
                requestType = requestType + request + " ";
            }
            requestType = "REQUEST: "+requestType;

            emailBody = dearCustomer + ",</br></br>"
                    + attachedCopyMessage + "</br></br>"
                    + (ndg.isEmpty() ? "" : ndg + "</br>")
                    + (reference.isEmpty() ? "" : reference + "</br>")
                    + (requestType.isEmpty() ? "" : requestType + "</br></br>")
                    + thanksMessage;
        }
        return emailBody;
    }

    public void loadButtons() {
        setShowButtons(Boolean.TRUE);
    }

    public void preCheckDocument() throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntity().getNdg())) {
            List<Invoice> invoices = DaoManager.load(Invoice.class, new Criterion[]{Restrictions.eq("ndg", getEntity().getNdg())});
            if (!ValidationHelper.isNullOrEmpty(invoices)) {
                String message = ResourcesHelper.getString("invoiceAlreadyCreated");
                message = message + " "+getEntity().getNdg() + " in data "+invoices.get(0).getDateString();
                setInvoiceAlreadyCreated(message);
                RequestContext.getCurrentInstance().update("@widgetVar(invoiceAlreadyCreatedDialogWV)");
                executeJS("PF('invoiceAlreadyCreatedDialogWV').show();");
                return;
            }
        }
        preCheckDocumentOtherRequests();
    }

    public void preCheckDocumentOtherRequests() throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntity().getNdg())) {
            List<Request> requests = DaoManager.load(Request.class, new Criterion[]{Restrictions.and(Restrictions.eq("ndg", getEntity().getNdg()),
                    Restrictions.eq("stateId", RequestState.EVADED.getId()),
                    Restrictions.isNotNull("mail"),
                    Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted")),
                    Restrictions.ne("mail", getEntity()) )});
            List<Request> requestListForInvoice = new ArrayList<>();
            if (!ValidationHelper.isNullOrEmpty(requests)) {
                requestListForInvoice.addAll(requests);
                List<Request> requestListEntity = getEntity().getRequests().stream()
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
        checkDocument(false);
    }
}