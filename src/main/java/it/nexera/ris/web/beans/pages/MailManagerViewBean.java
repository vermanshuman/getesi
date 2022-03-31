package it.nexera.ris.web.beans.pages;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import it.nexera.ris.api.FatturaAPI;
import it.nexera.ris.api.FatturaAPIResponse;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.persistence.beans.entities.domain.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MailHelper;
import it.nexera.ris.common.helpers.MailManagerHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.PrintPDFHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.SelectItemHelper;
import it.nexera.ris.common.helpers.SelectItemWrapperConverter;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.create.xls.CreateExcelRequestsReportHelper;
import it.nexera.ris.common.xml.wrappers.SelectItemWrapper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityViewPageBean;
import it.nexera.ris.web.beans.base.AccessBean;
import it.nexera.ris.web.beans.wrappers.logic.FileWrapper;
import it.nexera.ris.web.beans.wrappers.logic.GoodsServicesFieldWrapper;
import it.nexera.ris.web.common.RequestPriceListModel;
import lombok.Getter;
import lombok.Setter;

import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;

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

    //private MenuModel topMenuModel;

    //private int activeMenuTabNum;

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

    private List<Request> invoiceRequests;

    private String apiError;

    private boolean sendInvoice;

    private Boolean billinRequest;

    private Long selectedTaxRateId;
    
    private int activeTabIndex;
    
    private List<PaymentInvoice> paymentInvoices;
    
    private Double amountToBeCollected;
    
    private Double totalPayments;
    
    private Long number;
    
    private List<Request> invoicedRequests;
    
    private List<FileWrapper> invoiceEmailAttachedFiles;
    
    private List<GoodsServicesFieldWrapper> goodsServicesFields;
    
    private boolean printPdf;
    
    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {
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
            SelectItemHelper.addItemToListIfItIsNotInIt(getNotManagerOrFiduciaryClients(),getEntity().getClient());
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

        initOfficesList();
        if(!ValidationHelper.isNullOrEmpty(getSelectedClientManagers()) &&
                ValidationHelper.isNullOrEmpty(getClientManagers())) {
            setClientManagers(getSelectedClientManagers());
        }
        if (!ValidationHelper.isNullOrEmpty(getEntity().getOffice())) {
            setSelectedOfficeId(getEntity().getOffice().getId());
        }

        setNewClientButtonClicked(false);
        getEntity().reloadRequests();


        // Changes for invoice
        //generateMenuModel();
        setMaxInvoiceNumber();

        List<Request> requestList =
                getEntity().getRequests()
                        .stream()
                        .filter(x -> !ValidationHelper.isNullOrEmpty(x.getStateId()) &&
                                RequestState.EVADED.getId().equals(x.getStateId())).collect(Collectors.toList());

        setInvoiceRequests(requestList);
        //Double invoiceTotalCost = 0.0D;
        if (!ValidationHelper.isNullOrEmpty(requestList)) {
            Request examRequest = DaoManager.get(Request.class, new CriteriaAlias[]{
                    new CriteriaAlias("client", "c", JoinType.LEFT_OUTER_JOIN),
                    new CriteriaAlias("c.addressCityId", "ac", JoinType.LEFT_OUTER_JOIN),
                    new CriteriaAlias("c.addressProvinceId", "ap", JoinType.LEFT_OUTER_JOIN),
            }, new Criterion[]{
                    Restrictions.eq("id", getInvoiceRequests().get(0).getId())
            });
            setExamRequest(examRequest);

            /*invoiceTotalCost = requestList
                    .stream()
                    .filter(r -> !ValidationHelper.isNullOrEmpty(r.getTotalCost()))
                    .mapToDouble(r -> Double.parseDouble(r.getTotalCostDouble())).sum();*/
        }
        /*setInvoiceTotalCost(invoiceTotalCost);
        setVatAmounts(ComboboxHelper.fillList(TaxRate.class, Order.asc("description"), new CriteriaAlias[]{}, new Criterion[]{
                Restrictions.eq("use", Boolean.TRUE)
        }, true, false));*/

        docTypes = new ArrayList<>();
        docTypes.add(new SelectItem("FE", "FATTURA"));
        setDocumentType("FE");
        competence = new Date();

        /*ums = new ArrayList<>();
        ums.add(new SelectItem("pz", "pz"));*/
        setVatCollectabilityList(ComboboxHelper.fillList(VatCollectability.class,
                false, false));
        paymentTypes = ComboboxHelper.fillList(PaymentType.class);

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
        if(!ValidationHelper.isNullOrEmpty(getExamRequest()) &&
                getExamRequest().getStateId().equals(RequestState.SENT_TO_SDI.getId()))
            setInvoiceSentStatus(true);

        List<Request> requestListSentToSdi =
                getEntity().getRequests()
                        .stream()
                        .filter(x -> !ValidationHelper.isNullOrEmpty(x.getStateId()) &&
                                RequestState.SENT_TO_SDI.getId().equals(x.getStateId())).collect(Collectors.toList());
        if(!requestListSentToSdi.isEmpty())
            setSendInvoice(true);
        setBillinRequest(AccessBean.canViewPage(PageTypes.BILLING_LIST));
    }

    public void initOfficesList() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Client> clientList = DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"))})
                .stream().sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList());


        if(!ValidationHelper.isNullOrEmpty(getClientTypeId())) {
            List<Client> notManagerOrFiduciaryClients = clientList.stream()
                    .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                    .collect(Collectors.toList());

            setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(notManagerOrFiduciaryClients.stream()
                    .filter(c -> (c.getTypeId().equals(getClientTypeId())))
                    .collect(Collectors.toList()), true));
        }else {
            setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(clientList.stream()
                    .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                    .collect(Collectors.toList()), true));
        }

        if (getSelectedNotManagerOrFiduciaryClientId() != null) {
//        	setClientManagers(ComboboxHelper.fillWrapperList(
//        			clientList.stream()
//        			.filter(c -> (c.getManager() != null
//        			&& c.getManager())  &&  (c.getClient() != null
//        			&& c.getClient().getId().equals(getSelectedNotManagerOrFiduciaryClientId())))
//        			.collect(Collectors.toList())
//        			));

            setClientManagers(ComboboxHelper.fillWrapperList( emptyIfNull(clientList)
                    .stream()
                    .filter(c -> emptyIfNull(c.getReferenceClients()).stream()
                            .anyMatch(rc-> rc.getId().equals(getSelectedNotManagerOrFiduciaryClientId())))
                    .collect(Collectors.toList())));

            setFiduciaryClientsList(ComboboxHelper.fillList(clientList.stream()
                    .filter(c -> emptyIfNull(c.getReferenceClients()).stream()
                            .anyMatch(rc-> rc.getId().equals(getSelectedNotManagerOrFiduciaryClientId())))
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
            List<Client> invoiceClients = new ArrayList<Client>();
            for(Client client : clientList) {
                if(!client.getId().equals(getSelectedNotManagerOrFiduciaryClientId())) {
                    continue;
                }
                invoiceClients.addAll(client.getBillingRecipientList());
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

        if(!ValidationHelper.isNullOrEmpty(getSelectedClientManagers())){
            try {
                SelectItemWrapper<Client> selectedClientManager = getSelectedClientManagers().get(0);
                Client client = DaoManager.get(Client.class, new Criterion[]{
                        Restrictions.eq("id", selectedClientManager.getId())});
                if(!Hibernate.isInitialized(client.getReferenceClients()))
                    Hibernate.initialize(client.getReferenceClients());
                if(!ValidationHelper.isNullOrEmpty(client.getReferenceClients())
                        && client.getReferenceClients().size() == 1){
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

        for(Client client : clientList) {
            if(!client.getId().equals(getSelectedNotManagerOrFiduciaryClientId())) {
                continue;
            }
            if (!ValidationHelper.isNullOrEmpty(client.getBillingRecipientList())) {
                invoiceClients.addAll(client.getBillingRecipientList());
            }
        }
        setInvoiceClients(ComboboxHelper.fillList(invoiceClients, true));


        setFiduciaryClientsList(ComboboxHelper.fillList(clientList.stream()
                .filter(c -> c.getFiduciary() != null && c.getFiduciary()).collect(Collectors.toList()), true));

        if(!ValidationHelper.isNullOrEmpty(getClientTypeId())) {
            List<Client> notManagerOrFiduciaryClients = clientList.stream()
                    .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                    .collect(Collectors.toList());

            setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(notManagerOrFiduciaryClients.stream()
                    .filter(c -> (c.getTypeId().equals(getClientTypeId())))
                    .collect(Collectors.toList()), true));
        }else {
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

            setClientManagers(ComboboxHelper.fillWrapperList( emptyIfNull(clientList)
                    .stream()
                    .filter(c -> emptyIfNull(c.getReferenceClients()).stream()
                            .anyMatch(rc-> rc.getId().equals(getSelectedNotManagerOrFiduciaryClientId())))
                    .collect(Collectors.toList())));

//            setClientManagers(ComboboxHelper.fillWrapperList(clientList.stream()
//                    .filter(c -> (c.getManager() != null && c.getManager()) || (c.getId().equals(getSelectedNotManagerOrFiduciaryClientId())))
//                    .collect(Collectors.toList())));
        }else {
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
                                Restrictions.and(Restrictions.isNotNull("manager"),Restrictions.eq("manager", Boolean.TRUE))});

                if (ValidationHelper.isNullOrEmpty(clientList)) {
                    clientList = DaoManager.load(Client.class,
                            new CriteriaAlias[]{
                                    new CriteriaAlias("emails", "email", JoinType.INNER_JOIN)},
                            new Criterion[]{
                                    Restrictions.in("email.email", MailHelper.getOnlyEmails(getEntity().getEmailFrom())),
                                    Restrictions.and(Restrictions.isNotNull("manager"),Restrictions.eq("manager", Boolean.TRUE))});
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

            DaoManager.save(getEntity(), true);
            if(ValidationHelper.isNullOrEmpty(getSelectedNotManagerOrFiduciaryClientId())) {

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

                    if(clientList != null && clientList.size() > 0) {
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
            } else if (MailManagerStatuses.PARTIAL == status || MailManagerStatuses.MANAGED == status) {
                getEntity().setUserChangedState(DaoManager.get(User.class, getCurrentUser().getId()));
            }

            getEntity().setState(status.getId());
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
        if (isOnlyView()) RedirectHelper.goTo(PageTypes.MAIL_MANAGER_FOLDER);
        else RedirectHelper.goToSavePage(PageTypes.MAIL_MANAGER_LIST, null, getTablePage());
    }

    public void processManagedState(boolean redirectToCreateRequest) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        getEntity().setUser(DaoManager.get(User.class, getCurrentUser().getId()));
        DaoManager.save(getEntity(), true);

        if (redirectToCreateRequest) {
            RedirectHelper.goToCreateRequestFromMail(getEntity().getId(), false, isMultipleCreateRedirect());
        }
    }

    public void checkFieldsBeforeProcessManagedStateCheck() {
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

        if (notSetFields.size() == 0) {
            processManagedStateCheck();
        } else {
            setFieldsNotSetDialogMessage(
                    ResourcesHelper.getString("mailManagerViewFieldsNotSetDialogMessage")
                            + " " + String.join(", ", notSetFields));
            executeJS("PF('fieldsNotSetDialogWV').show();");
        }
    }

    public void processManagedStateCheck() {
        setConfirmButtonIsClicked(Boolean.TRUE);
        executeJS("PF('chooseSingleOrMultipleRequestCreateWV').show();");
//        if (ValidationHelper.isNullOrEmpty(getEntity().getRequests())) {
//            executeJS("PF('enterPracticeReferenceWV').show();");
//        } else {
//            executeJS("PF('chooseSingleOrMultipleRequestCreateWV').show();");
//        }
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
                if(!ValidationHelper.isNullOrEmpty(selectedIds)){
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
                    if(clientList != null && clientList.size() > 0) {
                        getEntity().setClient(clientList.get(0));
                    }else {
                        getEntity().setClient(null);
                    }
                }else {
                    getEntity().setClient(null);
                }
            }
            DaoManager.save(getEntity(), true);
            if(!redirect){
                RequestContext.getCurrentInstance().execute("PF('saveConfirmationDialogWV').show();");
            }else{
                FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, ResourcesHelper.getString("mailManagerSave"), null);
                FacesContext.getCurrentInstance().addMessage("", facesMessage);
            }
        }
        if (redirect) {
            processManagedState(true);
        }
    }

    public void openNewMailToManager() throws PersistenceBeanException, IllegalAccessException {
        List<Long> selectedIds = null;
        if(!ValidationHelper.isNullOrEmpty(getEntity().getValidRequests())){
            selectedIds = getEntity().getValidRequests().stream()
                    .filter(r -> r.isSelectedForMail())
                    .map(Request::getId).collect(Collectors.toList());
        }
        if (!ValidationHelper.isNullOrEmpty(getEntity())) {
            if(!ValidationHelper.isNullOrEmpty(selectedIds)){
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
        RedirectHelper.goTo(PageTypes.EXCEL_DATA,getEntity().getId() );
    }


    /*private void generateMenuModel() {
        setTopMenuModel(new DefaultMenuModel());
        if (isMultipleCreate()) {
            addMenuItem(ResourcesHelper.getString("requestTextEditDataTab"));
        } else {
            addMenuItem(ResourcesHelper.getString("requestTextEditDataTab"));
            if (!ValidationHelper.isNullOrEmpty(getInputCardList())) {
                getInputCardList()
                        .forEach(card -> addMenuItem(card.getName().toUpperCase()));
            }
        }
    }*/

   /* private void addMenuItem(String value) {
        DefaultMenuItem menuItem = new DefaultMenuItem(value);

        menuItem.setCommand("#{mailManagerViewBean.goToTab(" +
                getTopMenuModel().getElements().size() + ")}");
        menuItem.setUpdate("form");

        getTopMenuModel().addElement(menuItem);
    }*/

    public void setMaxInvoiceNumber() throws HibernateException {
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
        String invoiceNumber = (lastInvoiceNumber) + "-" + currentYear + "-FE";
        setInvoiceNumber(invoiceNumber);
        setNumber(lastInvoiceNumber);
    }
    
    public Double getAllTotalLine() {
    	Double total = getGoodsServicesFields().stream().collect(
				Collectors.summingDouble(GoodsServicesFieldWrapper::getTotalLine));
    	return total;
    }

    /*public Double getTotalGrossAmount() throws PersistenceBeanException, InstantiationException, IllegalAccessException {

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
    }*/
    
    public Double getTotalGrossAmount() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        Double totalGrossAmount = 0D;
        for(GoodsServicesFieldWrapper wrapper: getGoodsServicesFields()) {
	        if(!ValidationHelper.isNullOrEmpty(wrapper.getTotalLine())){
	            totalGrossAmount += wrapper.getTotalLine();
	            if(!ValidationHelper.isNullOrEmpty(wrapper.getSelectedTaxRateId())){
	                TaxRate taxrate = DaoManager.get(TaxRate.class, wrapper.getSelectedTaxRateId());
	                if(!ValidationHelper.isNullOrEmpty(taxrate.getPercentage())){
	                    totalGrossAmount += (wrapper.getTotalLine() * (taxrate.getPercentage().doubleValue()/100));
	                }
	            }
	        }
        }
        return totalGrossAmount;
    }

   /* public Double getTotalVat() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
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
    }*/
    
    public Double getTotalVat() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        Double total = 0D;
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
        return total;
    }

    /*public void sendInvoice() {
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
            invoice.setClient(getExamRequest().getClient());
            invoice.setDate(getInvoiceDate());
            invoice.setDocumentType(getDocumentType());
            if(!ValidationHelper.isNullOrEmpty(getSelectedPaymentTypeId()))
                invoice.setPaymentType(DaoManager.get(PaymentType.class, getSelectedPaymentTypeId()));

            if(!ValidationHelper.isNullOrEmpty(getVatCollectabilityId()))
                invoice.setVatCollectability(VatCollectability.getById(getVatCollectabilityId()));
            invoice.setNotes(getInvoiceNote());
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
            log.info("Mailmanager XMLDATA: " + xmlData);
            FatturaAPIResponse fatturaAPIResponse = fatturaAPI.callFatturaAPI(xmlData, log);

            if (fatturaAPIResponse != null && fatturaAPIResponse.getReturnCode() != -1) {
                DaoManager.save(invoice, true);
                invoiceItem.setInvoice(invoice);
                DaoManager.save(invoiceItem,true);
                CollectionUtils.emptyIfNull(getInvoiceRequests())
                        .stream()
                        .forEach(r -> {
                            try {
                                r.setStateId(RequestState.SENT_TO_SDI.getId());
                                r.setInvoice(invoice);
                                DaoManager.save(r, true);
                            } catch (PersistenceBeanException e) {
                                log.error("error in saving request after sending invoice ", e);
                            }
                        });

                setInvoiceSentStatus(true);
                executeJS("PF('invoiceDialogWV').hide();");
            } else {
                setApiError(ResourcesHelper.getString("sendInvoiceErrorMsg"));
                if(fatturaAPIResponse != null
                        && !ValidationHelper.isNullOrEmpty(fatturaAPIResponse.getDescription())){

                    if(fatturaAPIResponse.getDescription().contains("already exists")) {
                        setApiError(ResourcesHelper.getString("sendInvoiceDuplicateMsg"));
                    }else
                        setApiError(fatturaAPIResponse.getDescription());
                }
                executeJS("PF('sendInvoiceErrorDialogWV').show();");
            }

        }catch(Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
            executeJS("PF('sendInvoiceErrorDialogWV').show();");
        }
    }*/
    
    
    
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
    	setMaxInvoiceNumber();
    	docTypes = new ArrayList<>();
        docTypes.add(new SelectItem("FE", "FATTURA"));
        setDocumentType("FE");
        competence = new Date();
        setVatCollectabilityList(ComboboxHelper.fillList(VatCollectability.class,
                false, false));
        paymentTypes = ComboboxHelper.fillList(getExamRequest().getClient().getPaymentTypeList(), Boolean.TRUE);
       
        setGoodsServicesFields(new ArrayList<>());
        List<Invoice> invoices = DaoManager.load(Invoice.class, new Criterion[]{}, new Order[] {Order.desc("id")});
        Invoice invoiceDb = invoices.get(0);
        setInvoiceDate(invoiceDb.getDate());
        setSelectedClientId(invoiceDb.getClient().getId());
        if(!ValidationHelper.isNullOrEmpty(invoiceDb.getVatCollectability()))
        	setVatCollectabilityId(invoiceDb.getVatCollectability().getId());
        if(!ValidationHelper.isNullOrEmpty(invoiceDb.getPaymentType()))
        	setSelectedPaymentTypeId(invoiceDb.getPaymentType().getId());
        if(!ValidationHelper.isNullOrEmpty(invoiceDb.getNotes()))
        	setInvoiceNote(invoiceDb.getNotes());
        if(invoiceDb.getStatus().equals(InvoiceStatus.DELIVERED)) {
        	setInvoiceSentStatus(true);
        }
        List<InvoiceItem> invoiceItemsDb = DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoiceDb)});
        for(InvoiceItem invoiceItem: invoiceItemsDb) {
        	GoodsServicesFieldWrapper wrapper = createGoodsServicesFieldWrapper();
        	wrapper.setInvoiceItemId(invoiceItem.getId());
        	wrapper.setInvoiceTotalCost(invoiceItem.getInvoiceTotalCost());
        	wrapper.setSelectedTaxRateId(invoiceItem.getTaxRate().getId());
        	wrapper.setInvoiceItemAmount(ValidationHelper.isNullOrEmpty(invoiceItem.getAmount()) ? 0.0 : invoiceItem.getAmount());
        	double totalcost = !(ValidationHelper.isNullOrEmpty(invoiceItem.getInvoiceTotalCost())) ? invoiceItem.getInvoiceTotalCost().doubleValue() : 0.0;
        	double amount = !(ValidationHelper.isNullOrEmpty(invoiceItem.getAmount())) ? invoiceItem.getAmount().doubleValue() : 0.0;
        	double totalLine = totalcost + amount;
        	wrapper.setTotalLine(totalLine);
        	if(!ValidationHelper.isNullOrEmpty(invoiceItem.getDescription()))
        		wrapper.setDescription(invoiceItem.getDescription());
        	getGoodsServicesFields().add(wrapper);
        }
        loadInvoiceEmailAttachedFiles();
    }
    
    public void createInvoice() throws IllegalAccessException, PersistenceBeanException, HibernateException, InstantiationException {
    	List<Request> selectedRequestList = new ArrayList<>();
        if(!ValidationHelper.isNullOrEmpty(getEntity().getValidRequests())){
        	selectedRequestList = getEntity().getValidRequests().stream()
                    .filter(r -> r.isSelectedForInvoice())
                    .collect(Collectors.toList());
        }
        
        /*Double invoiceTotalCost = selectedRequestList
                .stream()
                .filter(r -> !ValidationHelper.isNullOrEmpty(r.getTotalCost()))
                .mapToDouble(r -> Double.parseDouble(r.getTotalCostDouble())).sum();
        setInvoiceTotalCost(invoiceTotalCost);*/
        Invoice invoice = new Invoice();
        invoice.setClient(getExamRequest().getClient());
        invoice.setDate(new Date());
        invoice.setStatus(InvoiceStatus.DRAFT);
        
        DaoManager.save(invoice, true);
        
        List<InvoiceItem> invoiceItems = groupingItemsByTaxRate(selectedRequestList);
        for(InvoiceItem invoiceItem: invoiceItems) {
        	invoiceItem.setInvoice(invoice);
            DaoManager.save(invoiceItem,true);
        }
        setInvoicedRequests(selectedRequestList);
        
        loadInvoiceDialogData();
        executeJS("PF('invoiceDialogBillingWV').show();");
    }
    
    private List<InvoiceItem> groupingItemsByTaxRate(List<Request> selectedRequestList) throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
    	Map<Long, List<PriceList>> priceListMap = new HashMap<Long, List<PriceList>>();
        for(Request request: selectedRequestList) {
        	List<PriceList> priceList = new ArrayList<PriceList>();
            if(!ValidationHelper.isNullOrEmpty(request.getService())) {
                priceList = DaoManager.load(PriceList.class, 
                        new Criterion[]{
                                Restrictions.eq("client", request.getClient() != null ? request.getClient() : request.getBillingClient()),
                                Restrictions.eq("isNegative", true),
                                Restrictions.eq("service", request.getService())});
                
            }else if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                priceList = DaoManager.load(PriceList.class, 
                        new Criterion[]{
                                Restrictions.eq("client", request.getClient() != null ? request.getClient() : request.getBillingClient()),
                                Restrictions.eq("isNegative", true),
                                Restrictions.in("service", request.getMultipleServices())});
            }
            priceListMap.put(request.getId(), priceList);
        }
        
        List<RequestPriceListModel> requestPriceListModels = new ArrayList<>();
        for(Map.Entry<Long, List<PriceList>> entry : priceListMap.entrySet()) {
        	Long requestId = entry.getKey();
        	Request request = DaoManager.get(Request.class, requestId);
        	String totalCostStr = request.getTotalCost() != null ? request.getTotalCost() : "0.0";
        	if(totalCostStr.contains(",")) {
        		totalCostStr = totalCostStr.replace(",", ".");
        	}
        	Double totalCost = Double.parseDouble(totalCostStr);
        	List<PriceList> prices = entry.getValue();
        	
        	for(PriceList priceList : prices) {
	        	RequestPriceListModel requestPriceListModel = new RequestPriceListModel();
	        	requestPriceListModel.setRequestId(requestId);
	        	requestPriceListModel.setRequest(request);
	        	requestPriceListModel.setTotalCost(totalCost);
	        	requestPriceListModel.setClient(priceList.getClient());
	        	requestPriceListModel.setService(priceList.getService());
	        	requestPriceListModel.setTaxRate(priceList.getTaxRate());
	        	requestPriceListModels.add(requestPriceListModel);
        	}
        }
        
        Map<Request, Map<TaxRate, Double>> map = requestPriceListModels.stream().collect(
				Collectors.groupingBy(RequestPriceListModel::getRequest, 
						Collectors.groupingBy(RequestPriceListModel::getTaxRate, 
								Collectors.summingDouble(RequestPriceListModel::getTotalCost))));
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        for(Map.Entry<Request, Map<TaxRate, Double>> entry : map.entrySet()) {
        	Request request = entry.getKey();
        	Map<TaxRate, Double> taxRateMap = entry.getValue();
        	for(Map.Entry<TaxRate, Double> taxRateEntry : taxRateMap.entrySet()) {
        		TaxRate taxRate = taxRateEntry.getKey();
        		InvoiceItem invoiceItem = new InvoiceItem();
        		invoiceItem.setSubject(request.getSubject().toString());
        		invoiceItem.setTaxRate(taxRate);
        		invoiceItem.setInvoiceTotalCost(taxRateEntry.getValue().doubleValue());
        		invoiceItems.add(invoiceItem);
        	}
        }
        return invoiceItems;
    }
    
    public void loadInvoiceEmailAttachedFiles() {
    	FileWrapper wrapper1 = new FileWrapper(1l, "test.pdf", "D:/");
    	FileWrapper wrapper2 = new FileWrapper(2l, "test.pdf", "D:/");
    	FileWrapper wrapper3 = new FileWrapper(3l, "test.pdf", "D:/");
    	FileWrapper wrapper4 = new FileWrapper(4l, "test.pdf", "D:/");
    	List<FileWrapper> files = new ArrayList<>();
    	files.add(wrapper1);
    	files.add(wrapper2);
    	files.add(wrapper3);
    	files.add(wrapper4);
        setInvoiceEmailAttachedFiles(files);
        /*setFileIndex(new AtomicLong());
        getEntity().setFiles(null);
        getEntity().setImgFiles(null);
        if (getEntity().getFiles() != null) {
            getEntity().getFiles().forEach(file -> {
                getAttachedFiles().add(new FileWrapper(getFileIndex().getAndIncrement(),
                        file.getFileName(), file.getDestinationPath()));
            });
        }*/
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
    	getGoodsServicesFields().add(wrapper);
    }
    
    public void saveInvoiceInDraft() {
    	cleanValidation();
    	if(ValidationHelper.isNullOrEmpty(getInvoiceDate())){
            addRequiredFieldException("form:date");
            setValidationFailed(true);
        }
    	
        if(ValidationHelper.isNullOrEmpty(getSelectedPaymentTypeId())){
            addRequiredFieldException("form:paymentType");
            setValidationFailed(true);
        }

        for(GoodsServicesFieldWrapper goodsServicesFieldWrapper : getGoodsServicesFields()) {
        	if(ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getInvoiceTotalCost())){
	            setValidationFailed(true);
	        }
        	
        	if(ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getInvoiceItemAmount())){
	            setValidationFailed(true);
	        }
	
	        if(ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getSelectedTaxRateId())){
	            setValidationFailed(true);
	        }
        }

        if (getValidationFailed()){
            executeJS("PF('invoiceErrorDialogWV').show();");
            return;
        }
        
        try {
        	saveInvoice(InvoiceStatus.DRAFT, false);
        	loadInvoiceDialogData();
        }catch(Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
            executeJS("PF('sendInvoiceErrorDialogWV').show();");
        }
    }
    
    public void confirmInvoice() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
    	cleanValidation();
    	if(ValidationHelper.isNullOrEmpty(getNumber())){
            addRequiredFieldException("form:number");
            setValidationFailed(true);
        }
    	
    	if(ValidationHelper.isNullOrEmpty(getInvoiceDate())){
            addRequiredFieldException("form:date");
            setValidationFailed(true);
        }
    	
        if(ValidationHelper.isNullOrEmpty(getSelectedPaymentTypeId())){
            addRequiredFieldException("form:paymentType");
            setValidationFailed(true);
        }
        
        for(GoodsServicesFieldWrapper goodsServicesFieldWrapper : getGoodsServicesFields()) {
        	if(ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getInvoiceTotalCost())){
	            setValidationFailed(true);
	        }
        	
        	if(ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getInvoiceItemAmount())){
	            setValidationFailed(true);
	        }
	
	        if(ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getSelectedTaxRateId())){
	            setValidationFailed(true);
	        }
        }

        if (getValidationFailed()){
            executeJS("PF('invoiceErrorDialogWV').show();");
            return;
        }
        
        try {
            saveInvoice(InvoiceStatus.TOSEND, true);
            loadInvoiceDialogData();
            executeJS("PF('invoiceConfirmWV').show();");
        }catch(Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
            executeJS("PF('sendInvoiceErrorDialogWV').show();");
        }
    	
    }
    
    public void saveInvoice(InvoiceStatus invoiceStatus, Boolean saveInvoiceNumber) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
    	Invoice invoice = DaoManager.get(Invoice.class, getNumber());
        if(ValidationHelper.isNullOrEmpty(invoice)) {
        	invoice = new Invoice();
        }
        invoice.setDate(getInvoiceDate());
        invoice.setClient(getExamRequest().getClient());
        invoice.setDocumentType(getDocumentType());
        if(!ValidationHelper.isNullOrEmpty(getSelectedPaymentTypeId()))
            invoice.setPaymentType(DaoManager.get(PaymentType.class, getSelectedPaymentTypeId()));

        if(!ValidationHelper.isNullOrEmpty(getVatCollectabilityId()))
            invoice.setVatCollectability(VatCollectability.getById(getVatCollectabilityId()));
        invoice.setNotes(getInvoiceNote());
        invoice.setStatus(invoiceStatus);
        if(saveInvoiceNumber) {
        	invoice.setNumber(getNumber());
	    	invoice.setInvoiceNumber(getInvoiceNumber());
        }
        DaoManager.save(invoice, true);
        for(GoodsServicesFieldWrapper goodsServicesFieldWrapper : getGoodsServicesFields()) {
        	if(!ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getInvoiceItemId())) {
            	InvoiceItem invoiceItem = DaoManager.get(InvoiceItem.class, goodsServicesFieldWrapper.getInvoiceItemId());
                invoiceItem.setAmount(goodsServicesFieldWrapper.getInvoiceItemAmount());
                invoiceItem.setTaxRate(DaoManager.get(TaxRate.class, goodsServicesFieldWrapper.getSelectedTaxRateId()));
                invoiceItem.setDescription(goodsServicesFieldWrapper.getDescription());
                invoiceItem.setInvoiceTotalCost(goodsServicesFieldWrapper.getInvoiceTotalCost());
                DaoManager.save(invoiceItem, true);
            } else {
            	InvoiceItem invoiceItem = new InvoiceItem();
            	invoiceItem.setAmount(goodsServicesFieldWrapper.getInvoiceItemAmount());
                invoiceItem.setTaxRate(DaoManager.get(TaxRate.class, goodsServicesFieldWrapper.getSelectedTaxRateId()));
                invoiceItem.setDescription(goodsServicesFieldWrapper.getDescription());
                invoiceItem.setInvoiceTotalCost(goodsServicesFieldWrapper.getInvoiceTotalCost());
                invoiceItem.setInvoice(invoice);
                invoiceItem.setSubject(getExamRequest().getSubject().toString());
                DaoManager.save(invoiceItem, true);
            }
        }
    }
    
    public void sendInvoice() {
        try {
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
				if (!ValidationHelper.isNullOrEmpty(getEntity().getValidRequests())) {
					selectedRequestList = getEntity().getValidRequests().stream().filter(r -> r.isSelectedForInvoice())
							.collect(Collectors.toList());
				}
				CollectionUtils.emptyIfNull(selectedRequestList).stream().forEach(r -> {
					try {
						r.setStateId(RequestState.SENT_TO_SDI.getId());
						r.setInvoice(invoice);
						DaoManager.save(r, true);
					} catch (PersistenceBeanException e) {
						log.error("error in saving request after sending invoice ", e);
					}
				});

                setInvoiceSentStatus(true);
                /*executeJS("PF('invoiceDialogWV').hide();");*/
            } else {
                setApiError(ResourcesHelper.getString("sendInvoiceErrorMsg"));
                if(fatturaAPIResponse != null
                        && !ValidationHelper.isNullOrEmpty(fatturaAPIResponse.getDescription())){

                    if(fatturaAPIResponse.getDescription().contains("already exists")) {
                        setApiError(ResourcesHelper.getString("sendInvoiceDuplicateMsg"));
                    }else
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
    
    public Long getClientTypeId() {
        return clientTypeId;
    }

    public void setClientTypeId(Long clientTypeId) {
        this.clientTypeId = clientTypeId;
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

    public List<Request> getInvoiceRequests() {
        return invoiceRequests;
    }

    public void setInvoiceRequests(List<Request> invoiceRequests) {
        this.invoiceRequests = invoiceRequests;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Double getInvoiceTotalCost() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        return invoiceTotalCost;
    }


    public void newInvoiceMail() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntity())) {
            RedirectHelper.goToMailEdit(getEntity().getId(), MailEditType.SEND_INVOICE);
        }
    }

    public Long getSelectedTaxRateId() {
        return selectedTaxRateId;
    }

    public void setSelectedTaxRateId(Long selectedTaxRateId) {
        this.selectedTaxRateId = selectedTaxRateId;
    }
}