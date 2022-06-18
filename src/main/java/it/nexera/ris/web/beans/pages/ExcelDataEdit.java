package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.create.xls.CreateExcelRequestsReportHelper;
import it.nexera.ris.common.xml.wrappers.SelectItemWrapper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.BaseEntityPageBean;
import it.nexera.ris.web.beans.wrappers.logic.ExcelDataWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ExcelTableWrapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@Setter
@Getter
@ManagedBean
@ViewScoped
public class ExcelDataEdit extends BaseEntityPageBean {

    private Long excelClientInvoiceId;

    private Long excelReportN;

    private Long excelFatturaN;

    private String excelReportNDG;

    private List<SelectItem> invoiceClients;

    private Long mailId;

    private Document document;

    private WLGInbox mail;

    private List<SelectItem> notManagerOrFiduciaryClients;

    private Long selectedNotManagerOrFiduciaryClientId;

    private String fatturaDiRiferimento;

    private Long selectedOfficeId;

    private List<SelectItem> officeList;

    private List<SelectItemWrapper<Client>> clientManagers;

    private List<SelectItemWrapper<Client>> selectedClientManagers;

    private SelectItemWrapperConverter<Client> clientSelectItemWrapperConverter;

    private Long selectedClientFiduciaryId;

    private List<SelectItem> fiduciaryClientsList;

    private String referenceRequest;

    private List<Request> requests;

    private  List<ExcelTableWrapper> excelDataTable;

    private Date excelDate;

    private Request examRequest;

    private CostManipulationHelper costManipulationHelper;

    private Boolean hideExtraCost = Boolean.FALSE;

    private Long requestId;

    private String costNote;

    private Boolean showRequestCost = Boolean.TRUE;

    private List<Long> selectedRequestIds;

    private Client selectedRequestClient;

    private List<Request> selectedRequests;

    @ManagedProperty(value="#{invoiceDialogBean}")
    private InvoiceDialogBean invoiceDialogBean;

    private List<Request> requestsConsideredForInvoice;

    @Override
    protected void onConstruct() {
        setMailId(null);
        setSelectedRequestIds(null);
        if(!ValidationHelper.isNullOrEmpty(getRequestParameter(RedirectHelper.ID_PARAMETER))){
            setMailId(Long.valueOf(getRequestParameter(RedirectHelper.ID_PARAMETER)));
        }else if(!ValidationHelper.isNullOrEmpty(SessionHelper.get("selectedRequestIds"))) {
            List<Long> selectedRequestIds =(List<Long>)SessionHelper.get("selectedRequestIds");
            SessionHelper.removeObject("selectedRequestIds");
            if(!ValidationHelper.isNullOrEmpty(SessionHelper.get("selectedRequestClientId"))) {
                Long clientId = (Long)SessionHelper.get("selectedRequestClientId");
                try {
                    setSelectedRequestClient(DaoManager.get(Client.class, clientId));
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
            SessionHelper.removeObject("selectedRequestClientId");
            setSelectedRequestIds(selectedRequestIds);
        }
        loadPage();
    }

    private void loadPage() {
        try {

            setExcelDataTable(new ArrayList<>());

            setCostManipulationHelper(new CostManipulationHelper());
            getCostManipulationHelper().setEditable(true);
            setCostManipulationHelper(new CostManipulationHelper());
            List<Client> clientList = DaoManager.load(Client.class, new Criterion[]{
                    Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                            Restrictions.isNull("deleted"))})
                    .stream().sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList());


            if(!ValidationHelper.isNullOrEmpty(getMailId())){
                WLGInbox mail = DaoManager.get(WLGInbox.class, getMailId());
                setMail(mail);
            }
            setClientSelectItemWrapperConverter(new SelectItemWrapperConverter<>(Client.class));
            updateComboboxes();

            if(!ValidationHelper.isNullOrEmpty(getMail()) && !ValidationHelper.isNullOrEmpty(getMail().getClient())){
                setSelectedNotManagerOrFiduciaryClientId(getMail().getClient().getId());
                SelectItemHelper.addItemToListIfItIsNotInIt(getNotManagerOrFiduciaryClients(),mail.getClient());
            }

//            else  if(!ValidationHelper.isNullOrEmpty(getSelectedRequestClient())){
//                setSelectedNotManagerOrFiduciaryClientId(getSelectedRequestClient().getId());
//                SelectItemHelper.addItemToListIfItIsNotInIt(getNotManagerOrFiduciaryClients(),getSelectedRequestClient());
//            }

            if (!ValidationHelper.isNullOrEmpty(getMail())) {
                Document document = DaoManager.get(Document.class, new Criterion[]{
                        Restrictions.eq("mail.id", getMailId())});

                if(ValidationHelper.isNullOrEmpty(document)) {
                    document = new Document();
                    document.setMail(getMail());
                    document.setTypeId(DocumentType.INVOICE_REPORT.getId());
                    document.setReportNumber(SaveRequestDocumentsHelper.getLastInvoiceNumber() + 1);
                    DaoManager.save(document, true);
                }
                setDocument(document);
                if(!ValidationHelper.isNullOrEmpty(document))
                    setExcelFatturaN(document.getInvoiceNumber());

                setExcelReportN(document.getReportNumber());

                setExcelDate((document == null || document.getInvoiceDate() == null ?
                        DateTimeHelper.getNow(): document.getInvoiceDate()));
            }
            if (getSelectedNotManagerOrFiduciaryClientId() != null) {
                List<Client> invoiceClients = new ArrayList<Client>();
                for(Client client : clientList) {
                    if(!client.getId().equals(getSelectedNotManagerOrFiduciaryClientId())) {
                        continue;
                    }
                    if (!ValidationHelper.isNullOrEmpty(client.getBillingRecipientList())) {
                        invoiceClients.addAll(client.getBillingRecipientList());
                    }
                }
                setInvoiceClients(ComboboxHelper.fillList(invoiceClients.stream()
                        .filter(distinctByKey(c -> c.getId()))
                        .collect(Collectors.toList()), true));

                if(!ValidationHelper.isNullOrEmpty(getSelectedNotManagerOrFiduciaryClientId())){
                    List<Office> offices = DaoManager.get(Client.class, getSelectedNotManagerOrFiduciaryClientId()).getOffices();
                    if (!ValidationHelper.isNullOrEmpty(offices)) {
                        setOfficeList(ComboboxHelper.fillList(offices.stream()
                                .sorted(Comparator.comparing(Dictionary::getDescription))
                                .collect(Collectors.toList()), true));
                    } else {
                        setOfficeList(Collections.singletonList(SelectItemHelper.getNotSelected()));
                    }
                }
            }else {
                setInvoiceClients(ComboboxHelper.fillList(clientList, true));
                setOfficeList(ComboboxHelper.fillList(Office.class, Order.asc("description")));
            }

            if(!ValidationHelper.isNullOrEmpty(getMail())){
                setExcelReportNDG(getMail().getNdg());
            }

            if(!ValidationHelper.isNullOrEmpty(getMail())){
                setExcelReportNDG(getMail().getNdg());
                if(!ValidationHelper.isNullOrEmpty(getMail().getClient()) &&
                        !ValidationHelper.isNullOrEmpty(getMail().getClient().getTypeId())) {
                    List<Client> notManagerOrFiduciaryClients = clientList.stream()
                            .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                            .collect(Collectors.toList());

                    setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(notManagerOrFiduciaryClients.stream()
                            .filter(c -> Objects.nonNull(c))
                            .filter(c -> (c.getTypeId().equals(getMail().getClient().getTypeId())))
                            .collect(Collectors.toList()), true));
                }
                if (!ValidationHelper.isNullOrEmpty(getMail().getClientInvoice())) {
                    setExcelClientInvoiceId(getMail().getClientInvoice().getId());
                    SelectItemHelper.addItemToListIfItIsNotInIt(getInvoiceClients(), getMail().getClientInvoice());
                }

                if (!ValidationHelper.isNullOrEmpty(getMail().getOffice())) {
                    setSelectedOfficeId(getMail().getOffice().getId());
                }

                if (!ValidationHelper.isNullOrEmpty(getMail().getClientFiduciary())) {
                    setSelectedClientFiduciaryId(getMail().getClientFiduciary().getId());
                }
                fillSelectedClientManagers();
                setReferenceRequest(getMail().getReferenceRequest());
            }else if(!ValidationHelper.isNullOrEmpty(getSelectedRequestClient())){
                List<Client> notManagerOrFiduciaryClients = clientList.stream()
                        .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                        .collect(Collectors.toList());

                setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(notManagerOrFiduciaryClients.stream()
                        .filter(c -> Objects.nonNull(c))
                        .filter(c -> (c.getTypeId().equals(getSelectedRequestClient().getTypeId())))
                        .collect(Collectors.toList()), true));
            }else {
                setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(clientList.stream()
                        .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                        .collect(Collectors.toList()), true));
            }
            initOfficesList();
            if(!ValidationHelper.isNullOrEmpty(getSelectedClientManagers()) &&
                    ValidationHelper.isNullOrEmpty(getClientManagers())) {
                setClientManagers(getSelectedClientManagers());
            }
            setSelectedRequests(null);

            if(!ValidationHelper.isNullOrEmpty(getMail())){
                if(!ValidationHelper.isNullOrEmpty(getMail().getRecievedInbox()) &&
                        !ValidationHelper.isNullOrEmpty(getMail().getRecievedInbox().getRequests())) {
                    prepareTables(getMail().getRecievedInbox().getRequests());
                    setSelectedRequests(getMail().getRecievedInbox().getRequests());
                }else if (!ValidationHelper.isNullOrEmpty(getMail().getRequests())) {
                    setSelectedRequests(getMail().getRequests());
                    prepareTables(getMail().getRequests());
                }
            } else if(!ValidationHelper.isNullOrEmpty(getSelectedRequestIds())){
                setSelectedRequests(DaoManager.load(Request.class, new Criterion[]{
                        Restrictions.in("id", getSelectedRequestIds())
                }));
                prepareTables(getSelectedRequests());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void prepareTables(List<Request> requests) throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {

        Map<RequestType, List<Request>> sortedRequests = new HashMap<>();
        setRequests(requests.stream().filter(Request::isDeletedRequest).collect(Collectors.toList()));
        sortRequestsByType(getRequests(), sortedRequests);
        //Client requestClient = getRequests().get(0).getClient();
        setExcelDataTable(new ArrayList<>());
        
        for (Map.Entry<RequestType, List<Request>> entry : sortedRequests.entrySet()) {
            List<String> columns = new ArrayList<>();
            Client requestClient = entry.getValue().get(0).getClient();
            List<ClientInvoiceManageColumn> clientInvoiceManageColumns = DaoManager.load(ClientInvoiceManageColumn.class,
                    new CriteriaAlias[]{new CriteriaAlias("client", "client", JoinType.INNER_JOIN),
                            new CriteriaAlias("requestType", "requestType", JoinType.INNER_JOIN)},
                    new Criterion[]{
                            Restrictions.and(Restrictions.eq("client.id", requestClient.getId())
                                    ,Restrictions.eq("requestType.id",entry.getKey().getId()))
                    },Order.asc("position"));

            if (!ValidationHelper.isNullOrEmpty(clientInvoiceManageColumns)) {
                for (ClientInvoiceManageColumn column : clientInvoiceManageColumns) {
                    columns.add(getColumnNameByField(column.getField()));
                }
            }else {
                columns.addAll(Arrays.asList(CreateExcelRequestsReportHelper.getRequestsColumns()));
            }
            CreateExcelRequestsReportHelper.setRequestsColumns(columns.toArray(new String[0]));
            ExcelTableWrapper excelTableWrapper = new ExcelTableWrapper();
            excelTableWrapper.setRequestName(entry.getKey().getName());
            excelTableWrapper.setColumnNames(columns);
            excelTableWrapper.setOriginalRequests(entry.getValue());
            excelTableWrapper.setRequests(new ArrayList<>());
            Map<String, String> columnValues = new HashMap<>();
            Map<String, String> footerValues = new HashMap<>();
            CreateExcelRequestsReportHelper createExcelRequestsReportHelper = new CreateExcelRequestsReportHelper(true);
            int colIndex = -1;
            List<Request> newRequests = new ArrayList<>();
            for(Request request : entry.getValue()) {
                if (!ValidationHelper.isNullOrEmpty(request.getService())) {
                    excelTableWrapper.getRequests().add(request);
                    addColumnValues(request, columnValues,createExcelRequestsReportHelper);
                    List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                            Restrictions.eq("requestId", request.getId())});
                    Double result;
                    for (ExtraCost cost : extraCost) {
                        if(ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                            result = cost.getPrice();
                            try {
                                Request newRequest = request.reportCopy();
                                newRequest.setTempId(UUID.randomUUID().toString());
                                newRequest.setEstateFormalityList(request.getEstateFormalityList());
                                newRequest.setEvasionDate(request.getEvasionDate());
                                newRequests.add(newRequest);
                                addColumnValues(newRequest, request.getService(), columnValues,
                                        createExcelRequestsReportHelper,-1,result);
                            } catch (CloneNotSupportedException e) {
                                LogHelper.log(log, e);
                            }
                        }
                    }
                }else if (!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                    int index = 0;
                    Double result = 0d;
                    List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                            Restrictions.eq("requestId", request.getId())});
                    for (ExtraCost cost : extraCost) {
                        result += cost.getPrice();
                        if(ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                            try {
                                Request newRequest = request.copy();
                                newRequest.setTempId(UUID.randomUUID().toString());
                                newRequest.setEstateFormalityList(request.getEstateFormalityList());
                                newRequest.setMultipleRequestId(request.getId());
                                newRequests.add(newRequest);
                                addColumnValues(newRequest, request.getService(), columnValues,
                                        createExcelRequestsReportHelper,-1,result);
                            } catch (CloneNotSupportedException e) {
                                LogHelper.log(log, e);
                            }
                        }
                    }
                    for (Service service : request.getMultipleServices()) {
                        try {
                            Request newRequest = request.copy();
                            newRequest.setTempId(UUID.randomUUID().toString());
                            newRequest.setMultipleRequestId(request.getId());
                            newRequests.add(newRequest);
                            addColumnValues(newRequest, service, columnValues,
                                    createExcelRequestsReportHelper,index++,result);
                        } catch (CloneNotSupportedException e) {
                            LogHelper.log(log, e);
                        }
                    }

                }
            }
            if(!ValidationHelper.isNullOrEmpty(newRequests)) {
                excelTableWrapper.getRequests().addAll(newRequests);
            }
            footerValues.put(ResourcesHelper.getString("requestedDate"), ResourcesHelper.getString("formalityTotal").toUpperCase());
            colIndex = getIndex(ResourcesHelper.getString("mortgageRights"), CreateExcelRequestsReportHelper.getRequestsColumns());

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            DecimalFormat formatter = (DecimalFormat) nf;
            formatter.applyPattern("#,###.00");
            double result = 0.0;
            if(colIndex > -1){
                result = createExcelRequestsReportHelper.getSumOfCostEstateFormalityService(excelTableWrapper.getOriginalRequests());
                footerValues.put(ResourcesHelper.getString("mortgageRights"), result > 0 ? formatter.format(result) : "0.0");
            }
            colIndex = getIndex(ResourcesHelper.getString("landRegistryRights"), CreateExcelRequestsReportHelper.getRequestsColumns());
            if (colIndex > -1) {
                result = getSumOfCostCadastral(excelTableWrapper.getOriginalRequests());
                footerValues.put(ResourcesHelper.getString("landRegistryRights"), result > 0 ? formatter.format(result) : "0.0");
            }

            colIndex = getIndex(ResourcesHelper.getString("excelStamps"), CreateExcelRequestsReportHelper.getRequestsColumns());
            if (colIndex > -1) {
                result = getSumOfExtraCost(excelTableWrapper.getOriginalRequests(), ExtraCostType.MARCA);
                footerValues.put(ResourcesHelper.getString("excelStamps"), result > 0 ? formatter.format(result) : "0.0");
            }

            colIndex = getIndex(ResourcesHelper.getString("excelPostalExpenses"), CreateExcelRequestsReportHelper.getRequestsColumns());
            if (colIndex > -1) {
                result = getSumOfExtraCost(excelTableWrapper.getOriginalRequests(), ExtraCostType.POSTALE);
                footerValues.put(ResourcesHelper.getString("excelPostalExpenses"), result > 0 ? formatter.format(result) : "0.0");
            }

            colIndex = getIndex(ResourcesHelper.getString("compensation"), CreateExcelRequestsReportHelper.getRequestsColumns());
            if (colIndex > -1) {
                result = createExcelRequestsReportHelper.getSumOfCostPayServices(excelTableWrapper.getOriginalRequests());
                footerValues.put(ResourcesHelper.getString("compensation"), result > 0 ? formatter.format(result) : "0.0");
            }

            colIndex = getIndex(ResourcesHelper.getString("formalityTotal"), CreateExcelRequestsReportHelper.getRequestsColumns());
            if (colIndex > -1) {
                result = createExcelRequestsReportHelper.getSumOfCostTotalServices(excelTableWrapper.getOriginalRequests());
                footerValues.put(ResourcesHelper.getString("formalityTotal"), result > 0 ? formatter.format(result) : "0.0");
            }
            excelTableWrapper.setColumnValues(columnValues);
            excelTableWrapper.setFooterValues(footerValues);
            getExcelDataTable().add(excelTableWrapper);
        }
    }

    private void addColumnValues(Request request,
                                 Map<String, String> columnValues, CreateExcelRequestsReportHelper createExcelRequestsReportHelper) throws IllegalAccessException, PersistenceBeanException, InstantiationException{

        int colIndex = getIndex(ResourcesHelper.getString("requestedDate") , CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("requestedDate"),request), request.getCreateDateStr());
        }

        colIndex = getIndex(ResourcesHelper.getString("nominative"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("nominative"),request), request.getSubject() != null ? (request.getSubject().getFullName() != null ? request.getSubject().getFullName().toUpperCase() : "") : "");
        }
        colIndex = getIndex(ResourcesHelper.getString("codFiscIva"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("codFiscIva"),request), request.getFiscalCodeVATNamber());
        }
        colIndex = getIndex(ResourcesHelper.getString("permissionRequest"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("permissionRequest"),request), request.getServiceName().toUpperCase());
        }
        colIndex = getIndex(ResourcesHelper.getString("requestListCorservatoryName"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("requestListCorservatoryName"),request),request.getAggregationLandChargesRegistryName());
        }
        colIndex = getIndex(ResourcesHelper.getString("requestPrintFormalityPresentationDate"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("requestPrintFormalityPresentationDate"),request), DateTimeHelper.toString(request.getEvasionDate()));
        }
        colIndex = getIndex(ResourcesHelper.getString("excelForm"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            Long val = request.getNumberActOrSumOfEstateFormalitiesAndOther().longValue() + createExcelRequestsReportHelper.getRequestExtraCostValue(request).longValue();
            columnValues.put(getColumnName(ResourcesHelper.getString("excelForm"),request), String.valueOf(val));
        }

        colIndex = getIndex(ResourcesHelper.getString("mortgageRights"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("mortgageRights"),request),
                    String.valueOf(getCostEstateFormalityAndExtraCostRelated(request)));
        }
        colIndex = getIndex(ResourcesHelper.getString("landRegistryRights"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("landRegistryRights"),request), String.valueOf(getCostCadastralAndExtraCostRelated(request)));
        }

        colIndex = getIndex(ResourcesHelper.getString("excelStamps"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("excelStamps"),request), String.valueOf(getExtraCostRelated(request, ExtraCostType.MARCA)));
        }

        colIndex = getIndex(ResourcesHelper.getString("excelPostalExpenses"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("excelPostalExpenses"),request), String.valueOf(getExtraCostRelated(request, ExtraCostType.POSTALE)));
        }

        colIndex = getIndex(ResourcesHelper.getString("compensation"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("compensation"),request), ValidationHelper.isNullOrEmpty(request.getCostPay()) ? "0" : String.valueOf(request.getCostPay()));
        }
        colIndex = getIndex(ResourcesHelper.getString("formalityTotal"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("formalityTotal"),request), ValidationHelper.isNullOrEmpty(request.getTotalCost()) ? "0" : request.getTotalCost().replaceAll(",", "."));
        }
        colIndex = getIndex(ResourcesHelper.getString("excelNote"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            if(ValidationHelper.isNullOrEmpty(request.getCostNote())){
                String note = "";
                boolean isAdded = Boolean.FALSE;
                List<Document> requestDocuments = DaoManager.load(Document.class,
                        new CriteriaAlias[]{new CriteriaAlias("request", "request", JoinType.INNER_JOIN)},
                        new Criterion[]{Restrictions.and(Restrictions.eq("request.id", request.getId()),
                                Restrictions.eq("typeId", 2L))});
                if (!ValidationHelper.isNullOrEmpty(requestDocuments)) {
                    if(request.getService() !=null
                            && request.getService().getUnauthorizedQuote()!=null && request.getService().getUnauthorizedQuote()){
                        note = "Preventivo non autorizzato";
                        isAdded = Boolean.TRUE;
                    }
                }
                if(!isAdded && request.getAuthorizedQuote()!=null &&  request.getAuthorizedQuote()){
                    note = "Preventivo autorizzato";
                }
                if(!isAdded && request.getUnauthorizedQuote() != null
                        &&  request.getUnauthorizedQuote()){
                    note = "Preventivo non autorizzato";
                }
                String requestNote = createExcelRequestsReportHelper.generateCorrectNote(request);
                requestNote = requestNote.replaceAll("(?i)<br\\p{javaSpaceChar}*(?:/>|>)", "\n");
                note = note.trim().isEmpty() ? requestNote : note.concat(" ").concat(requestNote);
                columnValues.put(getColumnName(ResourcesHelper.getString("excelNote"),request),note);
            }
            else
                columnValues.put(getColumnName(ResourcesHelper.getString("excelNote"),request),request.getCostNote());
        }
        colIndex = getIndex(ResourcesHelper.getString("excelCDR"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("excelCDR"),request),request.getCdr());
        }

        colIndex = getIndex(ResourcesHelper.getString("excelNDG"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("excelNDG"),request),request.getNdg());
        }

        colIndex = getIndex(ResourcesHelper.getString("excelUser"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("excelUser"),request),request.getRequestExcelUserName());
        }
        colIndex = getIndex(ResourcesHelper.getString("excelPosition"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1 && !ValidationHelper.isNullOrEmpty(request.getPosition())) {
            columnValues.put(getColumnName(ResourcesHelper.getString("excelPosition"),request),request.getPosition());
        }

        colIndex = getIndex(ResourcesHelper.getString("excelStamps"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            Double result =  getExtraCostRelated(request.getId(), ExtraCostType.MARCA);
            result = (double) Math.round((result)* 100000d) / 100000d;
            columnValues.put(getColumnName(ResourcesHelper.getString("excelStamps"),request), "\u20AC" + result);
        }

        colIndex = getIndex(ResourcesHelper.getString("excelPostalExpenses"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            Double result =  getExtraCostRelated(request.getId(), ExtraCostType.POSTALE);
            result = (double) Math.round((result)* 100000d) / 100000d;
            columnValues.put(getColumnName(ResourcesHelper.getString("excelPostalExpenses"),request),"\u20AC" + result);
        }
        request.setSelectedTemplateId(null);
        RequestPrint requestPrint = DaoManager.get(RequestPrint.class,
                new CriteriaAlias[]{new CriteriaAlias("request", "rq", JoinType.INNER_JOIN)},
                new Criterion[]{Restrictions.eq("rq.id", request.getId())});
        if (requestPrint != null) {
            if (requestPrint.getTemplate() != null) {
                request.setSelectedTemplateId(requestPrint.getTemplate().getId());
            }
        }
        boolean isCostMismatch = createExcelRequestsReportHelper.checkTotalCostSpecialColumn( request);
        if(isCostMismatch){
            String value = columnValues.get(getColumnName(ResourcesHelper.getString("excelNote"),request));
            String html = "<span style=\"color:Orange\">Anomalia costi</span>";
            if(!ValidationHelper.isNullOrEmpty(value)){
                html  = value + "<br/>" + html;
            }
            columnValues.put(getColumnName(ResourcesHelper.getString("excelNote"),request),html);
        }
    }

    private void addColumnValues(Request request, Service service,
                                 Map<String, String> columnValues,
                                 CreateExcelRequestsReportHelper createExcelRequestsReportHelper,
                                 int index, double extraCost) throws IllegalAccessException, PersistenceBeanException, InstantiationException{
        Boolean billingClient = createExcelRequestsReportHelper.isBillingClient(request);
        boolean restrictionForPriceList = createExcelRequestsReportHelper.restrictionForPriceList(request);

        int colIndex = getIndex(ResourcesHelper.getString("requestedDate") , CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(
                    getColumnName(ResourcesHelper.getString("requestedDate"),request,service),
                    request.getCreateDateStr());
        }

        colIndex = getIndex(ResourcesHelper.getString("nominative"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(
                    getColumnName(ResourcesHelper.getString("nominative"),request,service),
                    request.getSubject() != null ? (request.getSubject().getFullName() != null ? request.getSubject().getFullName().toUpperCase() : "") : "");
        }
        colIndex = getIndex(ResourcesHelper.getString("codFiscIva"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(
                    getColumnName(ResourcesHelper.getString("codFiscIva"),request,service),
                    request.getFiscalCodeVATNamber());
        }
        colIndex = getIndex(ResourcesHelper.getString("permissionRequest"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            String serviceName = request.getServiceName()!=null?request.getServiceName().toUpperCase():"";
            serviceName = service== null ? serviceName : service.toString().toUpperCase();
            columnValues.put(
                    getColumnName(ResourcesHelper.getString("permissionRequest"),request,service),
                    serviceName);
        }

        colIndex = getIndex(ResourcesHelper.getString("requestListCorservatoryName"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            if(index != -1) {
                columnValues.put(getColumnName(
                        ResourcesHelper.getString("requestListCorservatoryName"),request,service),
                        request.getAggregationLandChargesRegistryName());
            }else {
                List<AggregationLandChargesRegistry> aggregationLandChargesRegistries =
                        DaoManager.load(AggregationLandChargesRegistry.class, new Criterion[]
                                {Restrictions.eq("national", Boolean.TRUE)});

                if(aggregationLandChargesRegistries.size() > 0) {
                    columnValues.put(getColumnName(
                            ResourcesHelper.getString("requestListCorservatoryName"),request,service),
                            aggregationLandChargesRegistries.get(0).getName());
                }
            }

        }
        colIndex = getIndex(ResourcesHelper.getString("requestPrintFormalityPresentationDate"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(
                    ResourcesHelper.getString("requestPrintFormalityPresentationDate"),request,service),
                    DateTimeHelper.toString(request.getEvasionDate()));
        }
        colIndex = getIndex(ResourcesHelper.getString("excelForm"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1 && index != -1) {
            columnValues.put(getColumnName(
                    ResourcesHelper.getString("excelForm"),request,service),
                    String.valueOf(request.getNumberActOrSumOfEstateFormalitiesAndOther().longValue()));
        }

        colIndex = getIndex(ResourcesHelper.getString("mortgageRights"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            if(!ValidationHelper.isNullOrEmpty(service)) {
                Double cost = 0d;
                if(index != -1) {
                    cost = createExcelRequestsReportHelper.getCostEstateFormalityAndExtraCostRelated(
                            request,service,billingClient, restrictionForPriceList);
                }else {
                    cost += extraCost;
                }
                columnValues.put(getColumnName(ResourcesHelper.getString("mortgageRights"),request, service),
                        String.valueOf(cost));
            }
        }

        if(index != -1) {
            colIndex = getIndex(ResourcesHelper.getString("landRegistryRights"), CreateExcelRequestsReportHelper.getRequestsColumns());
            if (colIndex > -1) {
                columnValues.put(getColumnName(ResourcesHelper.getString("landRegistryRights"),request,service), String.valueOf(getCostCadastralAndExtraCostRelated(request)));
            }
        }

        if(index != -1) {
            colIndex = getIndex(ResourcesHelper.getString("compensation"), CreateExcelRequestsReportHelper.getRequestsColumns());
            if (colIndex > -1) {
                if(!ValidationHelper.isNullOrEmpty(service)) {
                    columnValues.put(getColumnName(ResourcesHelper.getString("compensation"),request,service),
                            String.valueOf(
                                    createExcelRequestsReportHelper.getCostPay(
                                            request,service,billingClient, restrictionForPriceList)));
                }
            }
        }

        colIndex = getIndex(ResourcesHelper.getString("formalityTotal"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            if(index != -1) {
                if(!ValidationHelper.isNullOrEmpty(service)) {
                    Double result = 0d;
                    if (!ValidationHelper.isNullOrEmpty(request.getCostCadastral())) {
                        result += request.getCostCadastral();
                    }
                    result += createExcelRequestsReportHelper.getCostExtra(request, service, billingClient, restrictionForPriceList);
                    result += createExcelRequestsReportHelper.getCostEstateFormality(request, service, billingClient, restrictionForPriceList);
                    result += createExcelRequestsReportHelper.getCostPay(request, service, billingClient, restrictionForPriceList);
                    result = (double) Math.round((result)* 100000d) / 100000d;
                    columnValues.put(getColumnName(ResourcesHelper.getString("formalityTotal"),request,service), String.valueOf(result));
                }
            }else {
                columnValues.put(getColumnName(ResourcesHelper.getString("formalityTotal"),request,service), String.valueOf(extraCost));
            }

        }
        colIndex = getIndex(ResourcesHelper.getString("excelNote"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            if(index == 0) {
                if(extraCost > 0)
                    columnValues.put(getColumnName(ResourcesHelper.getString("excelNote"),request,service),"Costo aggiuntivo: " + extraCost);
            }else if(index != -1){
                if(ValidationHelper.isNullOrEmpty(request.getCostNote())) {
                    String note = "";
                    boolean isAdded = Boolean.FALSE;
                    List<Document> requestDocuments = DaoManager.load(Document.class,
                            new CriteriaAlias[]{new CriteriaAlias("request", "request", JoinType.INNER_JOIN)},
                            new Criterion[]{Restrictions.and(Restrictions.eq("request.id", request.getId()),
                                    Restrictions.eq("typeId", 2L))});
                    if (!ValidationHelper.isNullOrEmpty(requestDocuments)) {
                        if(request.getService() !=null
                                && request.getService().getUnauthorizedQuote()!=null && request.getService().getUnauthorizedQuote()){
                            note = "Preventivo non autorizzato";
                            isAdded = Boolean.TRUE;
                        }
                    }
                    if(!isAdded && request.getAuthorizedQuote()!=null &&  request.getAuthorizedQuote()){
                        note = "Preventivo autorizzato";
                    }
                    if(!isAdded && request.getUnauthorizedQuote() != null
                            &&  request.getUnauthorizedQuote()){
                        note = "Preventivo non autorizzato";
                    }

                    String requestNote = createExcelRequestsReportHelper.generateCorrectNote(request);
                    requestNote = requestNote.replaceAll("(?i)<br\\p{javaSpaceChar}*(?:/>|>)", "\n");
                    note = note.trim().isEmpty() ? requestNote : note.concat(" ").concat(requestNote);
                    columnValues.put(getColumnName(ResourcesHelper.getString("excelNote"),request,service),note);
                } else
                    columnValues.put(getColumnName(ResourcesHelper.getString("excelNote"),request,service),request.getCostNote());
            }else {
                columnValues.put(getColumnName(ResourcesHelper.getString("excelNote"),request,service),"nazionale positiva");
            }
        }
        colIndex = getIndex(ResourcesHelper.getString("excelCDR"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("excelCDR"),request,service),request.getCdr());
        }

        colIndex = getIndex(ResourcesHelper.getString("excelNDG"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("excelNDG"),request,service),request.getNdg());
        }

        colIndex = getIndex(ResourcesHelper.getString("excelUser"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("excelUser"),request,service),request.getRequestExcelUserName());
        }

        colIndex = getIndex(ResourcesHelper.getString("excelPosition"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1 && !ValidationHelper.isNullOrEmpty(request.getPosition())) {
            columnValues.put(getColumnName(ResourcesHelper.getString("excelPosition"),request,service),request.getPosition());
        }

        colIndex = getIndex(ResourcesHelper.getString("excelStamps"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1 && index != -1) {

            Double result = getExtraCostRelated(!ValidationHelper.isNullOrEmpty(request.getId()) ? request.getId() : request.getReferenceId(), ExtraCostType.MARCA);
            result = (double) Math.round((result)* 100000d) / 100000d;

            columnValues.put(getColumnName(ResourcesHelper.getString("excelStamps"),request, service),
                    "\u20AC" + result);
        }

        colIndex = getIndex(ResourcesHelper.getString("excelPostalExpenses"), CreateExcelRequestsReportHelper.getRequestsColumns());
        if (colIndex > -1  && index != -1) {
            Double result = getExtraCostRelated(!ValidationHelper.isNullOrEmpty(request.getId()) ? request.getId() : request.getReferenceId(), ExtraCostType.POSTALE);
            result = (double) Math.round((result)* 100000d) / 100000d;
            columnValues.put(getColumnName(ResourcesHelper.getString("excelPostalExpenses"),request, service),
                    "\u20AC" + result);
        }

        Request referenceRequest = DaoManager.get(Request.class, !ValidationHelper.isNullOrEmpty(request.getReferenceId()) ? request.getReferenceId() : request.getId());
        referenceRequest.setSelectedTemplateId(null);
        RequestPrint requestPrint = DaoManager.get(RequestPrint.class,
                new CriteriaAlias[]{new CriteriaAlias("request", "rq", JoinType.INNER_JOIN)},
                new Criterion[]{Restrictions.eq("rq.id", referenceRequest.getId())});
        if (requestPrint != null) {
            if (requestPrint.getTemplate() != null) {
                referenceRequest.setSelectedTemplateId(requestPrint.getTemplate().getId());
            }
        }

        boolean isCostMismatch = createExcelRequestsReportHelper.checkTotalCostSpecialColumn( referenceRequest);
        if(isCostMismatch){
            String value = columnValues.get(getColumnName(ResourcesHelper.getString("excelNote"),request,service));
            String html = "<span style=\"color:Orange\">Anomalia costi</span>";
            if(!ValidationHelper.isNullOrEmpty(value)){
                html  = value + "<br/>" + html;
            }
            columnValues.put(getColumnName(ResourcesHelper.getString("excelNote"),request),html);
        }
    }
    protected Double getSumOfCostTotal(List<Request> requests) {
        double result = 0d;
        for (Request request : requests) {
            if (!ValidationHelper.isNullOrEmpty(request.getTotalCost())) {
                result += Double.parseDouble(request.getTotalCostDouble());
            }
        }
        return result;
    }

    protected Double getSumOfCostPayServices(List<Request> requests
            , CreateExcelRequestsReportHelper createExcelRequestsReportHelper) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        Double result = 0d;
        for (Request request : requests) {
            if(!ValidationHelper.isNullOrEmpty(request.getService())) {
                if (!ValidationHelper.isNullOrEmpty(request.getCostPay())) {
                    result += request.getCostPay();
                }
            }else if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                Boolean billingClient = createExcelRequestsReportHelper.isBillingClient(request);
                boolean restrictionForPriceList = createExcelRequestsReportHelper.restrictionForPriceList(request);
                for(Service service : request.getMultipleServices()) {
                    result += createExcelRequestsReportHelper.getCostPay(request,service,billingClient, restrictionForPriceList);
                }

            }
        }
        return result;
    }
    public Double getSumOfCostCadastral(List<Request> requests) throws PersistenceBeanException, IllegalAccessException {
        Double result = 0d;
        for (Request request : requests) {
            if (!ValidationHelper.isNullOrEmpty(request.getCostCadastral())) {
                result += request.getCostCadastral();
            }
            List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", request.getId()),
                    Restrictions.eq("type", ExtraCostType.CATASTO)});

            if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                for (ExtraCost cost : extraCosts) {
                    result += cost.getPrice();
                }
            }
        }
        return result;
    }
    public Double getSumOfCostEstateFormality(List<Request> requests
            , CreateExcelRequestsReportHelper createExcelRequestsReportHelper) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Double result = 0d;

        for (Request request : requests) {

            if(!ValidationHelper.isNullOrEmpty(request.getService())) {
                if (!ValidationHelper.isNullOrEmpty(request.getCostEstateFormality())) {
                    result += request.getCostEstateFormality();
                }
                List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", request.getId()),
                        Restrictions.eq("type", ExtraCostType.IPOTECARIO)});

                if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                    for (ExtraCost cost : extraCosts) {
                        result += cost.getPrice();
                    }
                }
            }else if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                Boolean billingClient = createExcelRequestsReportHelper.isBillingClient(request);
                boolean restrictionForPriceList = createExcelRequestsReportHelper.restrictionForPriceList(request);
                for(Service service : request.getMultipleServices()) {
                    result += createExcelRequestsReportHelper.getCostEstateFormalityAndExtraCostRelated(
                            request,service,billingClient, restrictionForPriceList);
                }
            }
        }
        return result;
    }

    public Double getSumOfExtraCost(List<Request> requests, ExtraCostType extraCostType) throws PersistenceBeanException, IllegalAccessException {
        Double result = 0d;
        for (Request request : requests) {
            List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", request.getId()),
                    Restrictions.eq("type", extraCostType)});

            if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                for (ExtraCost cost : extraCosts) {
                    result += cost.getPrice();
                }
            }
        }
        return result;
    }

    protected Double getExtraCostRelated(Request request, ExtraCostType extraCostType) throws PersistenceBeanException, IllegalAccessException {
        Double result = 0d;

        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId()),
                Restrictions.eq("type", extraCostType)});

        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            for (ExtraCost cost : extraCosts) {
                result += cost.getPrice();
            }
        }
        return result.equals(0d) ? result : Math.round(result * 100000d) / 100000d;
    }

    private String getColumnName(String columnName, Request request) {
        return columnName + "_" + request.getTempId();
    }

    private String getColumnName(String columnName, Request request, Service service) {
        return columnName + "_" + request.getTempId() ;
    }

    protected Double getCostEstateFormalityAndExtraCostRelated(Request request) throws PersistenceBeanException, IllegalAccessException {
        Double result = 0d;

        if (!ValidationHelper.isNullOrEmpty(request.getCostEstateFormality())) {
            result += request.getCostEstateFormality();
        }

        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId()),
                Restrictions.eq("type", ExtraCostType.IPOTECARIO)});

        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            for (ExtraCost cost : extraCosts) {
                result += cost.getPrice();
            }
        }

        return result.equals(0d) ? result : Math.round(result * 100000d) / 100000d;
    }

    protected Double getCostCadastralAndExtraCostRelated(Request request) throws PersistenceBeanException, IllegalAccessException {
        Double result = 0d;

        if (!ValidationHelper.isNullOrEmpty(request.getCostCadastral())) {
            result += request.getCostCadastral();
        }

        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId()),
                Restrictions.eq("type", ExtraCostType.CATASTO)});

        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            for (ExtraCost cost : extraCosts) {
                result += cost.getPrice();
            }
        }
        return result.equals(0d) ? result : Math.round(result * 100000d) / 100000d;
    }

    private void addClientToManagerListIfHeIsNotInIt() {
        for (SelectItemWrapper<Client> selectedClientManager : getSelectedClientManagers()) {
            if (getClientManagers().stream().noneMatch(x -> x.getId().equals(selectedClientManager.getId()))) {
                getClientManagers().add(selectedClientManager);
                getClientSelectItemWrapperConverter().getWrapperList().add(selectedClientManager);
            }
        }
    }

    private void fillSelectedClientManagers() {
        setSelectedClientManagers(new ArrayList<>());

        if (!ValidationHelper.isNullOrEmpty(getMail().getManagers())) {
            for (Client item : getMail().getManagers()) {
                SelectItemWrapper<Client> selectItem = new SelectItemWrapper<>(item);
                getSelectedClientManagers().add(selectItem);
            }
        }
        addClientToManagerListIfHeIsNotInIt();
    }

    private byte[] getXlsBytes() {
        byte[] excelFile = null;
        try {

            ExcelDataWrapper excelDataWrapper = new ExcelDataWrapper();

            excelDataWrapper.setNdg(getExcelReportNDG());
            excelDataWrapper.setReportn(getExcelReportN());
            excelDataWrapper.setFatturaDiRiferimento(getFatturaDiRiferimento());
            excelDataWrapper.setReferenceRequest(getReferenceRequest());
            excelDataWrapper.setFatturan(getExcelFatturaN());
            excelDataWrapper.setData(getExcelDate());
            if (!ValidationHelper.isNullOrEmpty(getExcelClientInvoiceId())) {
                excelDataWrapper.setClientInvoice(DaoManager.get(Client.class, getExcelClientInvoiceId()));
            }

            if (!ValidationHelper.isNullOrEmpty(getSelectedClientManagers())) {
                excelDataWrapper.setManagers(new ArrayList<>());
                List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                        Restrictions.in("id", getSelectedClientManagers().stream()
                                .map(SelectItemWrapper::getId).collect(Collectors.toList()))});
                if (!ValidationHelper.isNullOrEmpty(clients)) {
                    excelDataWrapper.setManagers(clients);
                }
            }
            if (!ValidationHelper.isNullOrEmpty(getSelectedClientFiduciaryId())) {
                excelDataWrapper.setClientFiduciary(DaoManager.get(Client.class, getSelectedClientFiduciaryId()));
            }

            List<Request> recievedInboxRequests = null;
            if(!ValidationHelper.isNullOrEmpty(getMail()) && !ValidationHelper.isNullOrEmpty(getMail().getRecievedInbox()) &&
                    !ValidationHelper.isNullOrEmpty(getMail().getRecievedInbox().getRequests())) {
                recievedInboxRequests = getMail().getRecievedInbox().getRequests();
            }
            if ((!ValidationHelper.isNullOrEmpty(getMail()) && !ValidationHelper.isNullOrEmpty(getMail().getRequests())) ||
                    !ValidationHelper.isNullOrEmpty(recievedInboxRequests)
                    || !ValidationHelper.isNullOrEmpty(getSelectedRequests())) {
                if(!ValidationHelper.isNullOrEmpty(getMail())){
                    getMail().setNdg(excelDataWrapper.getNdg());
                    getMail().setReferenceRequest(excelDataWrapper.getReferenceRequest());
                    getMail().setClient(DaoManager.get(Client.class, getSelectedNotManagerOrFiduciaryClientId()));

                    if (!ValidationHelper.isNullOrEmpty(getSelectedNotManagerOrFiduciaryClientId())) {
                        getMail().setClient(DaoManager.get(Client.class, getSelectedNotManagerOrFiduciaryClientId()));
                    } else {
                        List<String> onlyEmails = MailHelper.getOnlyEmails(getMail().getEmailFrom());
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
                                getMail().setClient(clientList.get(0));
                            }else {
                                getMail().setClient(null);
                            }
                        }else {
                            getMail().setClient(null);
                        }
                    }
                    if (!ValidationHelper.isNullOrEmpty(getExcelClientInvoiceId())) {
                        getMail().setClientInvoice(DaoManager.get(Client.class, getExcelClientInvoiceId()));
                    } else {
                        //getMail().setClientInvoice(null);
                    }
                }

                if (!ValidationHelper.isNullOrEmpty(getSelectedOfficeId())) {
                    if(!ValidationHelper.isNullOrEmpty(getMail()))
                        getMail().setOffice(DaoManager.get(Office.class, getSelectedOfficeId()));
                    excelDataWrapper.setOffice(getMail().getOffice().getDescription());
                } else {
                    if(!ValidationHelper.isNullOrEmpty(getMail()))
                        getMail().setOffice(null);
                    excelDataWrapper.setOffice(null);
                }

                if (!ValidationHelper.isNullOrEmpty(getMail())){
                    if (!ValidationHelper.isNullOrEmpty(getSelectedClientManagers())) {
                        getMail().setManagers(new ArrayList<>());
                        List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                                Restrictions.in("id", getSelectedClientManagers().stream()
                                        .map(SelectItemWrapper::getId).collect(Collectors.toList()))});
                        if (!ValidationHelper.isNullOrEmpty(clients)) {
                            getMail().setManagers(clients);
                        }
                    }else {
                        getMail().setManagers(null);
                    }

                    if (!ValidationHelper.isNullOrEmpty(getSelectedClientFiduciaryId())) {
                        getMail().setClientFiduciary(DaoManager.get(Client.class, getSelectedClientFiduciaryId()));
                    } else {
                        getMail().setClientFiduciary(null);
                    }

                    DaoManager.save(getMail(), true);

                    Document document = DaoManager.get(Document.class, new Criterion[]{
                            Restrictions.eq("mail.id", getMailId())});
                    if(!ValidationHelper.isNullOrEmpty(document)) {
                        document.setInvoiceNumber(excelDataWrapper.getFatturan());
                        document.setInvoiceDate(excelDataWrapper.getData());
                        DaoManager.save(document, true);
                    }
                }
                List<Request> requests = null;

                if(!ValidationHelper.isNullOrEmpty(recievedInboxRequests)){
                    requests = recievedInboxRequests;
                }else if(!ValidationHelper.isNullOrEmpty(getMail())){
                    requests = getMail().getRequests();
                }else if(!ValidationHelper.isNullOrEmpty(getSelectedRequests())){
                    requests = getSelectedRequests();
                }
                List<Request> filteredRequests  = emptyIfNull(requests).stream().filter(
                        r->r.isDeletedRequest()).collect(Collectors.toList());
                Collections.sort(filteredRequests, Comparator.comparing(r -> r.getSubject().getId()));
                excelFile = new CreateExcelRequestsReportHelper(true).convertMailUserDataToExcel(
                        filteredRequests, document,excelDataWrapper);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return excelFile;
    }

    public void generateXlsRequestCost() {
        try {
            log.info("Inside generateXlsRequestCost");
            byte[] excelFile = getXlsBytes();
            if(!ValidationHelper.isNullOrEmpty(excelFile)) {
                FileHelper.sendFile("costs.xls", excelFile);
            }
            log.info("Leaving generateXlsRequestCost");

            Optional<Request> evadedRequest = getSelectedRequests()
                    .stream().filter(r ->
                            r.isDeletedRequest() && !ValidationHelper.isNullOrEmpty(r.getStateId())
                                    && !r.getStateId().equals(RequestState.EVADED.getId()))
                    .findFirst();

            if(evadedRequest == null || !evadedRequest.isPresent()){
                if(!ValidationHelper.isNullOrEmpty(getDocument())
                        && (ValidationHelper.isNullOrEmpty(getDocument().getComplete()) || !getDocument().getComplete()) ) {
                    getDocument().setComplete(true);
                    DaoManager.save(getDocument(), true);
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void generatePdfRequestCost() {
        try {
            log.info("Inside generatePdfRequestCost");
            byte[] excelFile = getXlsBytes();
            if(ValidationHelper.isNullOrEmpty(excelFile)) {
                excelFile = FileHelper.loadContentByPath(document.getPath());
            }
            if(!ValidationHelper.isNullOrEmpty(excelFile)) {
                String tmpFileNameSuffix = "costs";
                String sofficeCommand =
                        ApplicationSettingsHolder.getInstance().getByKey(
                                ApplicationSettingsKeys.SOFFICE_COMMAND).getValue().trim();
                String tempDir = FileHelper.getLocalTempDir();
                tempDir  += File.separator + UUID.randomUUID();
                FileUtils.forceMkdir(new File(tempDir));

                FileHelper.writeFileToFolder(tmpFileNameSuffix + ".xls", new File(tempDir), excelFile);

                String path = tempDir + File.separator + tmpFileNameSuffix + ".xls";

                File file = new File(path);

                VisureManageHelper.sendPDFfromXLSFile(file, sofficeCommand,tempDir,path);
            }
            log.info("Leaving generatePdfRequestCost");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void initOfficesList() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Client> clientList = DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"))})
                .stream().sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList());

        if (getSelectedNotManagerOrFiduciaryClientId() != null) {
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

            if(!ValidationHelper.isNullOrEmpty(getSelectedNotManagerOrFiduciaryClientId())){
                List<Office> offices = DaoManager.get(Client.class, getSelectedNotManagerOrFiduciaryClientId()).getOffices();
                if (!ValidationHelper.isNullOrEmpty(offices)) {
                    setOfficeList(ComboboxHelper.fillList(offices.stream()
                            .sorted(Comparator.comparing(Dictionary::getDescription))
                            .collect(Collectors.toList()), true));
                } else {
                    setOfficeList(Collections.singletonList(SelectItemHelper.getNotSelected()));
                }
            }

            List<Client> invoiceClients = new ArrayList<Client>();
            for(Client client : clientList) {
                if(!client.getId().equals(getSelectedNotManagerOrFiduciaryClientId())) {
                    continue;
                }
                if (!ValidationHelper.isNullOrEmpty(client.getBillingRecipientList())) {
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

    public void updateComboboxes() throws PersistenceBeanException, IllegalAccessException {
        List<Client> clientList = DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"))})
                .stream().sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList());

        List<Client> invoiceClients = new ArrayList<>();
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

        Client selectedClient = null;
        if(!ValidationHelper.isNullOrEmpty(getMail()) && !ValidationHelper.isNullOrEmpty(getMail().getClient())){
            selectedClient = getMail().getClient();
        }else if(!ValidationHelper.isNullOrEmpty(getSelectedRequestClient()))
            selectedClient = getSelectedRequestClient();

        if(!ValidationHelper.isNullOrEmpty(selectedClient) && !ValidationHelper.isNullOrEmpty(selectedClient.getTypeId())) {
            List<Client> notManagerOrFiduciaryClients = clientList.stream()
                    .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                    .collect(Collectors.toList());
            if(!ValidationHelper.isNullOrEmpty(getMail())){
                setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(notManagerOrFiduciaryClients.stream()
                        .filter(c -> Objects.nonNull(c))
                        .filter(c -> (c.getTypeId().equals(getMail().getClient().getTypeId())))
                        .collect(Collectors.toList()), true));
            }else {
                if(!ValidationHelper.isNullOrEmpty(getMail())){
                    setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(notManagerOrFiduciaryClients.stream()
                            .filter(c -> Objects.nonNull(c))
                            .filter(c -> (c.getTypeId().equals(getSelectedRequestClient().getTypeId())))
                            .collect(Collectors.toList()), true));
                }
            }
        }else {
            setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(clientList.stream()
                    .filter(c -> Objects.nonNull(c))
                    .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                    .collect(Collectors.toList()), true));
        }
        if (!ValidationHelper.isNullOrEmpty(selectedClient) &&
                !ValidationHelper.isNullOrEmpty(getSelectedNotManagerOrFiduciaryClientId())) {
            SelectItemHelper.addItemToListIfItIsNotInIt(getNotManagerOrFiduciaryClients(), selectedClient);
        }
        if (!ValidationHelper.isNullOrEmpty(getMail()) && !ValidationHelper.isNullOrEmpty(getExcelClientInvoiceId())) {
            SelectItemHelper.addItemToListIfItIsNotInIt(getInvoiceClients(), getMail().getClientInvoice());
        }
        if (!ValidationHelper.isNullOrEmpty(getMail()) && !ValidationHelper.isNullOrEmpty(getSelectedClientFiduciaryId())) {
            SelectItemHelper.addItemToListIfItIsNotInIt(getInvoiceClients(), getMail().getClientFiduciary());
        }
        if (getSelectedNotManagerOrFiduciaryClientId() != null) {
            setClientManagers(ComboboxHelper.fillWrapperList( emptyIfNull(clientList)
                    .stream()
                    .filter(c -> emptyIfNull(c.getReferenceClients()).stream()
                            .anyMatch(rc-> rc.getId().equals(getSelectedNotManagerOrFiduciaryClientId())))
                    .collect(Collectors.toList())));
        }else {
            setClientManagers(ComboboxHelper.fillWrapperList(clientList.stream()
                    .filter(c -> c.getManager() != null && c.getManager()).collect(Collectors.toList())));
        }

        getClientSelectItemWrapperConverter().setWrapperList(new ArrayList<>(getClientManagers()));
        if (!ValidationHelper.isNullOrEmpty(getSelectedClientManagers())) {
            addClientToManagerListIfHeIsNotInIt();
        }
    }

    protected void sortRequestsByType(List<Request> requests, Map<RequestType, List<Request>> sortedRequests) {
        Collections.sort(requests, Comparator.comparing(r -> r.getSubject().getId()));
        for (Request elem : requests) {
            elem.setTempId(UUID.randomUUID().toString());
            if (!sortedRequests.containsKey(elem.getRequestType())) {
                sortedRequests.put(elem.getRequestType(), new ArrayList<>());
                sortedRequests.get(elem.getRequestType()).add(elem);
            } else if (sortedRequests.containsKey(elem.getRequestType())) {
                sortedRequests.get(elem.getRequestType()).add(elem);
            }
        }
    }

    private String getColumnNameByField(BillingTypeFields field) {
        switch (field) {
            case EXCEL_DATE:
                return (ResourcesHelper.getString("requestPrintFormalityPresentationDate"));
            case EXCEL_CONSERVATORIA:
                return (ResourcesHelper.getString("requestListCorservatoryName"));
            case EXCEL_NAME:
                return (ResourcesHelper.getString("nominative"));
            case EXCEL_CODE:
                return (ResourcesHelper.getString("codFiscIva"));
            case EXCEL_REQUEST_TYPE:
                return (ResourcesHelper.getString("permissionRequest"));
            case EXCEL_FORMALITY:
                return (ResourcesHelper.getString("excelForm"));
            case EXCEL_MORTGAGE_EXPENSES:
                return (ResourcesHelper.getString("mortgageRights"));
            case EXCEL_CATASTAL_EXPENSES:
                return (ResourcesHelper.getString("landRegistryRights"));
            case EXCEL_COMPENSATION:
                return (ResourcesHelper.getString("compensation"));
            case EXCEL_TOTAL:
                return (ResourcesHelper.getString("formalityTotal"));
            case EXCEL_NOTE:
                return (ResourcesHelper.getString("excelNote"));
            case EXCEL_CDR:
                return (ResourcesHelper.getString("excelCDR"));
            case EXCEL_NDG:
                return (ResourcesHelper.getString("excelNDG"));
            case EXCEL_USER:
                return (ResourcesHelper.getString("excelUser"));
            case EXCEL_OFFICE:
                return (ResourcesHelper.getString("excelOffice"));
            case EXCEL_STAMPS:
                return (ResourcesHelper.getString("excelStamps"));
            case EXCEL_POSTAL_EXPENSES:
                return (ResourcesHelper.getString("excelPostalExpenses"));
            default:
                return null;
        }
    }

    public void saveRequestExtraCost() throws Exception {
        getCostManipulationHelper().setCostNote(getCostNote());
        DaoManager.refresh(getExamRequest());
        getCostManipulationHelper().saveRequestExtraCost(getExamRequest());
        CostCalculationHelper calculation = new CostCalculationHelper(getExamRequest());
        calculation.calculateAllCosts(true);
        loadPage();
    }

    public void updateCosts() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        getCostManipulationHelper().updateExamRequestParametersFromHelper(getExamRequest());
        boolean reCalculate = true;
        if(getExamRequest().getCostButtonConfirmClicked() != null && getExamRequest().getCostButtonConfirmClicked()){
            reCalculate = false;
        }
        getCostManipulationHelper().viewExtraCost(getExamRequest(), reCalculate);
    }

    public void updateButtonPanel() {
    }

    protected Double getExtraCostRelated(Long requestId, ExtraCostType extraCostType) throws PersistenceBeanException, IllegalAccessException {
        Double result = 0d;

        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", requestId),
                Restrictions.eq("type", extraCostType)});

        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            for (ExtraCost cost : extraCosts) {
                result += cost.getPrice();
            }
        }
        return result.equals(0d) ? result : Math.round(result * 100000d) / 100000d;
    }

    private int getIndex(String columnName, String[] columns) {
        return Arrays.asList(columns).indexOf(columnName);
    }

    public void goBack() {
        RedirectHelper.goToMailViewFromClient(getMailId());
    }

    public Long getExcelClientInvoiceId() {
        return excelClientInvoiceId;
    }

    public void setExcelClientInvoiceId(Long excelClientInvoiceId) {
        this.excelClientInvoiceId = excelClientInvoiceId;
    }

    public Long getExcelReportN() {
        return excelReportN;
    }

    public void setExcelReportN(Long excelReportN) {
        this.excelReportN = excelReportN;
    }

    public String getExcelReportNDG() {
        return excelReportNDG;
    }

    public void setExcelReportNDG(String excelReportNDG) {
        this.excelReportNDG = excelReportNDG;
    }

    public List<SelectItem> getInvoiceClients() {
        return invoiceClients;
    }

    public void setInvoiceClients(List<SelectItem> invoiceClients) {
        this.invoiceClients = invoiceClients;
    }

    public Long getMailId() {
        return mailId;
    }

    public void setMailId(Long mailId) {
        this.mailId = mailId;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public WLGInbox getMail() {
        return mail;
    }

    public void setMail(WLGInbox mail) {
        this.mail = mail;
    }

    public List<SelectItem> getNotManagerOrFiduciaryClients() {
        return notManagerOrFiduciaryClients;
    }

    public void setNotManagerOrFiduciaryClients(List<SelectItem> notManagerOrFiduciaryClients) {
        this.notManagerOrFiduciaryClients = notManagerOrFiduciaryClients;
    }

    public Long getSelectedNotManagerOrFiduciaryClientId() {
        return selectedNotManagerOrFiduciaryClientId;
    }

    public void setSelectedNotManagerOrFiduciaryClientId(Long selectedNotManagerOrFiduciaryClientId) {
        this.selectedNotManagerOrFiduciaryClientId = selectedNotManagerOrFiduciaryClientId;
    }

    public String getFatturaDiRiferimento() {
        return fatturaDiRiferimento;
    }

    public void setFatturaDiRiferimento(String fatturaDiRiferimento) {
        this.fatturaDiRiferimento = fatturaDiRiferimento;
    }

    public Long getSelectedOfficeId() {
        return selectedOfficeId;
    }

    public void setSelectedOfficeId(Long selectedOfficeId) {
        this.selectedOfficeId = selectedOfficeId;
    }

    public List<SelectItem> getOfficeList() {
        return officeList;
    }

    public void setOfficeList(List<SelectItem> officeList) {
        this.officeList = officeList;
    }

    public List<SelectItemWrapper<Client>> getSelectedClientManagers() {
        return selectedClientManagers;
    }

    public void setSelectedClientManagers(List<SelectItemWrapper<Client>> selectedClientManagers) {
        this.selectedClientManagers = selectedClientManagers;
    }

    public List<SelectItemWrapper<Client>> getClientManagers() {
        return clientManagers;
    }

    public SelectItemWrapperConverter<Client> getClientSelectItemWrapperConverter() {
        return clientSelectItemWrapperConverter;
    }

    public void setClientManagers(List<SelectItemWrapper<Client>> clientManagers) {
        this.clientManagers = clientManagers;
    }

    public void setClientSelectItemWrapperConverter(SelectItemWrapperConverter<Client> clientSelectItemWrapperConverter) {
        this.clientSelectItemWrapperConverter = clientSelectItemWrapperConverter;
    }

    public Long getSelectedClientFiduciaryId() {
        return selectedClientFiduciaryId;
    }

    public void setSelectedClientFiduciaryId(Long selectedClientFiduciaryId) {
        this.selectedClientFiduciaryId = selectedClientFiduciaryId;
    }

    public List<SelectItem> getFiduciaryClientsList() {
        return fiduciaryClientsList;
    }

    public void setFiduciaryClientsList(List<SelectItem> fiduciaryClientsList) {
        this.fiduciaryClientsList = fiduciaryClientsList;
    }

    public String getReferenceRequest() {
        return referenceRequest;
    }

    public void setReferenceRequest(String referenceRequest) {
        this.referenceRequest = referenceRequest;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    public Date getExcelDate() {
        return excelDate;
    }

    public void setExcelDate(Date excelDate) {
        this.excelDate = excelDate;
    }

    public Long getExcelFatturaN() {
        return excelFatturaN;
    }

    public void setExcelFatturaN(Long excelFatturaN) {
        this.excelFatturaN = excelFatturaN;
    }

    public void viewExtraCost() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        viewExtraCost(false);
    }

    public void viewExtraCost(boolean recalculate) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        System.out.println(">>>>>>>>>>> " + getRequestId());
        setCostNote(null);
        setCostManipulationHelper(new CostManipulationHelper());
        Request request =  DaoManager.get(Request.class, getRequestId());
        request.setSelectedTemplateId(null);
        if(!Hibernate.isInitialized(request.getRequestFormalities())){
            request.reloadRequestFormalities();
        }
        setExamRequest(request);
        RequestPrint requestPrint = DaoManager.get(RequestPrint.class,
                new CriteriaAlias[]{new CriteriaAlias("request", "rq", JoinType.INNER_JOIN)},
                new Criterion[]{Restrictions.eq("rq.id", getRequestId())});
        if (requestPrint != null) {
            if (requestPrint.getTemplate() != null) {
                getExamRequest().setSelectedTemplateId(requestPrint.getTemplate().getId());
            }
        }
        getCostManipulationHelper().setMortgageTypeList(ComboboxHelper.fillList(MortgageType.class, false, false));
        if(ValidationHelper.isNullOrEmpty(getExamRequest().getCostNote())) {
            try {
                List<Document> requestDocuments = DaoManager.load(Document.class,
                        new CriteriaAlias[]{new CriteriaAlias("request", "request", JoinType.INNER_JOIN)},
                        new Criterion[]{Restrictions.and(Restrictions.eq("request.id", getExamRequest().getId()), Restrictions.eq("typeId", 2L))});
                boolean isAdded = Boolean.FALSE;
                if (!ValidationHelper.isNullOrEmpty(requestDocuments)) {
                    if(getExamRequest().getService() !=null
                            && getExamRequest().getService().getUnauthorizedQuote()!=null
                            && getExamRequest().getService().getUnauthorizedQuote()){
                        costNote = "Preventivo non autorizzato";
                        isAdded = Boolean.TRUE;
                    }
                }
                if(!isAdded && getExamRequest().getAuthorizedQuote()!= null
                        && getExamRequest().getAuthorizedQuote()){
                    costNote = "Preventivo autorizzato";
                }
                if(!isAdded && getExamRequest().getUnauthorizedQuote()!=null
                        && getExamRequest().getUnauthorizedQuote()){
                    costNote = "Preventivo non autorizzato";
                }
                costNote = ValidationHelper.isNullOrEmpty(costNote) ? new CreateExcelRequestsReportHelper().generateCorrectNote(getExamRequest()) : costNote.concat(" ").concat(new CreateExcelRequestsReportHelper().generateCorrectNote(getExamRequest()));
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }
        }else
            setCostNote(getExamRequest().getCostNote());

        getCostManipulationHelper().viewExtraCost(getExamRequest(), recalculate);
        setExcelDataTable(new ArrayList<>());
        if(!ValidationHelper.isNullOrEmpty(getMail()) && !ValidationHelper.isNullOrEmpty(getMail().getRecievedInbox()) &&
                !ValidationHelper.isNullOrEmpty(getMail().getRecievedInbox().getRequests())) {
            prepareTables(getMail().getRecievedInbox().getRequests());
        }else if (!ValidationHelper.isNullOrEmpty(getMail()) && !ValidationHelper.isNullOrEmpty(getMail().getRequests())) {
            prepareTables(getMail().getRequests());
        }
    }

    public void updateNationalCost() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {

        Request request = DaoManager.get(Request.class, new Criterion[]{
                Restrictions.eq("id", getExamRequest().getId())});

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

    public void createInvoice() throws Exception {
        setMaxInvoiceNumber();
        List<Request> selectedRequestList;
        if (!ValidationHelper.isNullOrEmpty(getRequestsConsideredForInvoice())) {
            selectedRequestList = getRequestsConsideredForInvoice().stream()
                    .filter(r -> r.isSelectedForInvoice())
                    .collect(Collectors.toList());

            Invoice invoice = new Invoice();
            if(selectedRequestList.size() > 0) {
                if(!ValidationHelper.isNullOrEmpty(getMail())
                        && !ValidationHelper.isNullOrEmpty(getMail().getClientInvoice()))
                    invoice.setClient(getMail().getClientInvoice());
                else
                    throw new Exception("Client invoice is null, Can't create invoice");
            }
            getInvoiceDialogBean().setSelectedInvoiceClient(invoice.getClient());
            invoice.setDate(new Date());
            invoice.setStatus(InvoiceStatus.DRAFT);
            invoice.setEmailFrom(getMail());
            getInvoiceDialogBean().setEntity(getMail());
            getInvoiceDialogBean().setSelectedInvoiceItems(InvoiceHelper.groupingItemsByTaxRate(selectedRequestList,
                    getInvoiceDialogBean().getCausal()));
            getInvoiceDialogBean().setInvoicedRequests(selectedRequestList);
            invoice.setTotalGrossAmount(getInvoiceDialogBean().getTotalGrossAmount());

            getInvoiceDialogBean().loadInvoiceDialogData(invoice);
            executeJS("PF('invoiceDialogExcelWV').show();");
        }
    }

    /*public void setMaxInvoiceNumber() throws HibernateException {
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
        String invoiceNumber = (lastInvoiceNumber + 1) + "-" + currentYear + "-FE";
        getInvoiceDialogBean().setInvoiceNumber(invoiceNumber);
        getInvoiceDialogBean().setNumber(lastInvoiceNumber + 1);
    }*/
    
    public void setMaxInvoiceNumber() throws HibernateException {
        LocalDate currentdate = LocalDate.now();
        int currentYear = currentdate.getYear();

        Long lastInvoiceNumber = 0l;
        try {
            lastInvoiceNumber = (Long) DaoManager.getMax(Invoice.class, "number",
                    new Criterion[]{});
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        if(lastInvoiceNumber == null)
            lastInvoiceNumber = 0l;
        String invoiceNumber = (lastInvoiceNumber + 1) + "-" + currentYear + "-FE";
        getInvoiceDialogBean().setInvoiceNumber(invoiceNumber);
        getInvoiceDialogBean().setNumber(lastInvoiceNumber + 1);
    }


    public void deleteExtraCost(ExtraCost extraCostToDelete) {
        getCostManipulationHelper().getRequestExtraCosts().remove(extraCostToDelete);
        getCostManipulationHelper().setIncludeNationalCost(null);
    }

    public void addExtraCost(String extraCostValue) {
        getCostManipulationHelper().addExtraCost(extraCostValue, getRequestId());
    }

    public void preCheckInvoice() throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {

        if (!ValidationHelper.isNullOrEmpty(getMail()) && ValidationHelper.isNullOrEmpty(getMail().getClientInvoice())) {
            executeJS("PF('invoiceMissingBillingClientDialogWV').show();");
            return;
        }
        prepareInvoiceData();
        executeJS("PF('mailManagerViewRequestsForInvoiceDlg').show();");
    }
    public void prepareInvoiceData() {
        setRequestsConsideredForInvoice(new ArrayList<>());
        List<Request> requestListForInvoice =
                getSelectedRequests()
                        .stream()
                        .filter(x ->
                                x.isDeletedRequest() && ValidationHelper.isNullOrEmpty(x.getInvoice()) &&
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

    public Request getExamRequest() {
        return examRequest;
    }

    public void setExamRequest(Request examRequest) {
        this.examRequest = examRequest;
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

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Boolean getShowRequestCost() {
        return showRequestCost;
    }

    public void setShowRequestCost(Boolean showRequestCost) {
        this.showRequestCost = showRequestCost;
    }

    public List<ExcelTableWrapper> getExcelDataTable() {
        return excelDataTable;
    }

    public void setExcelDataTable(List<ExcelTableWrapper> excelDataTable) {
        this.excelDataTable = excelDataTable;
    }
}