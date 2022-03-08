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
import it.nexera.ris.web.beans.wrappers.logic.ExcelRequestWrapper;
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
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

    private  List<ExcelTableWrapper> dataTable;

    private Date excelDate;

    private Request examRequest;

    private CostManipulationHelper costManipulationHelper;

    private Boolean hideExtraCost = Boolean.FALSE;

    private Long requestId;

    private String costNote;

    private Boolean showRequestCost = Boolean.TRUE;

    @Override
    protected void onConstruct() {
        try {
            setCostManipulationHelper(new CostManipulationHelper());
            String idParameter = getRequestParameter(RedirectHelper.ID_PARAMETER);
            log.info("onConstruct ID " + idParameter);
            if(!ValidationHelper.isNullOrEmpty(idParameter)){
                setMailId(Long.valueOf(idParameter.trim()));
            }
        } catch (Exception e){
            LogHelper.log(log, e);
        }
        loadPage();
    }

    private void loadPage() {
        try {
            dataTable = new ArrayList<>();
            getCostManipulationHelper().setEditable(true);
            setCostManipulationHelper(new CostManipulationHelper());
            List<Client> clientList = DaoManager.load(Client.class, new Criterion[]{
                    Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                            Restrictions.isNull("deleted"))})
                    .stream().sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList());


            WLGInbox mail = DaoManager.get(WLGInbox.class, getMailId());
            setMail(mail);
            setClientSelectItemWrapperConverter(new SelectItemWrapperConverter<>(Client.class));
            updateComboboxes();

            if (!ValidationHelper.isNullOrEmpty(mail.getClient())) {
                setSelectedNotManagerOrFiduciaryClientId(mail.getClient().getId());
                SelectItemHelper.addItemToListIfItIsNotInIt(getNotManagerOrFiduciaryClients(),mail.getClient());
            }

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

                List<Office> offices = DaoManager.get(Client.class, getSelectedNotManagerOrFiduciaryClientId()).getOffices();
                if (!ValidationHelper.isNullOrEmpty(offices)) {
                    setOfficeList(ComboboxHelper.fillList(offices.stream()
                            .sorted(Comparator.comparing(Dictionary::getDescription))
                            .collect(Collectors.toList()), true));
                } else {
                    setOfficeList(Collections.singletonList(SelectItemHelper.getNotSelected()));
                }
            }else {
                setInvoiceClients(ComboboxHelper.fillList(clientList, true));
                setOfficeList(ComboboxHelper.fillList(Office.class, Order.asc("description")));
            }
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
            }else {
                setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(clientList.stream()
                        .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
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
            initOfficesList();
            if(!ValidationHelper.isNullOrEmpty(getSelectedClientManagers()) &&
                    ValidationHelper.isNullOrEmpty(getClientManagers())) {
                setClientManagers(getSelectedClientManagers());
            }

            setReferenceRequest(getMail().getReferenceRequest());

            if(!ValidationHelper.isNullOrEmpty(getMail().getRecievedInbox()) &&
                    !ValidationHelper.isNullOrEmpty(getMail().getRecievedInbox().getRequests())) {
                prepareTables(getMail().getRecievedInbox().getRequests());
            }else if (!ValidationHelper.isNullOrEmpty(getMail().getRequests())) {
                prepareTables(getMail().getRequests());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

    }

    public ExcelRequestWrapper reportCopy(Request request) throws CloneNotSupportedException {
        ExcelRequestWrapper newRequestWrapper = new ExcelRequestWrapper();
        newRequestWrapper.setTempId(request.getTempId());
        newRequestWrapper.setClient(request.getClient());
        newRequestWrapper.setClient(request.getClient());
        newRequestWrapper.setTempId(UUID.randomUUID().toString());
        newRequestWrapper.setEstateFormalityList(request.getEstateFormalityList());
        newRequestWrapper.setEvasionDate(request.getEvasionDate());
        newRequestWrapper.setBillingClient(request.getBillingClient());
        newRequestWrapper.setService(request.getService());
        if (!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
            newRequestWrapper.setMultipleServices(request.getMultipleServices());
        }
        newRequestWrapper.setDocumentsRequest(request.getDocumentsRequest());
        newRequestWrapper.setCreateDate(request.getCreateDate());
        newRequestWrapper.setSubject(request.getSubject());
        newRequestWrapper.setFiscalCodeVATNamber(request.getFiscalCodeVATNamber());
        newRequestWrapper.setServiceName(request.getServiceName());
        newRequestWrapper.setAggregationLandChargesRegistryName(request.getAggregationLandChargesRegistryName());
        newRequestWrapper.setNumberActOrSumOfEstateFormalitiesAndOther(request.getNumberActOrSumOfEstateFormalitiesAndOther());
        newRequestWrapper.setCostCadastral(request.getCostCadastral());
        newRequestWrapper.setCostNote(request.getCostNote());
        newRequestWrapper.setCdr(request.getCdr());
        newRequestWrapper.setNdg(request.getNdg());
        newRequestWrapper.setRequestExcelUserName(request.getRequestExcelUserName());
        newRequestWrapper.setPosition(request.getPosition());
        newRequestWrapper.setUserOfficeId(request.getUserOfficeId());
        newRequestWrapper.setRequest(request);

        return newRequestWrapper;
    }
    private void prepareTables(List<Request> requests) throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {

        Map<RequestType, List<Request>> sortedRequests = new HashMap<>();
        setRequests(requests.stream().filter(Request::isDeletedRequest).collect(Collectors.toList()));
        sortRequestsByType(getRequests(), sortedRequests);
        Client requestClient = getRequests().get(0).getClient();
        for (Map.Entry<RequestType, List<Request>> entry : sortedRequests.entrySet()) {
            List<String> columns = new ArrayList<>();
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
                columns.addAll(Arrays.asList(CreateExcelRequestsReportHelper.getRequestsEvasionColumns()));
            }

            ExcelTableWrapper excelTableWrapper = new ExcelTableWrapper();
            excelTableWrapper.setRequestName(entry.getKey().getName());
            excelTableWrapper.setColumnNames(columns);

            excelTableWrapper.setOriginalRequests(entry.getValue());
            //excelTableWrapper.setRequests(entry.getValue());
            excelTableWrapper.setRequests(new ArrayList<>());
            excelTableWrapper.setRequestWrappers(new ArrayList<>());
            Map<String, String> columnValues = new HashMap<>();
            Map<String, String> footerValues = new HashMap<>();
            CreateExcelRequestsReportHelper createExcelRequestsReportHelper = new CreateExcelRequestsReportHelper(true);
            int colIndex = -1;
            List<Request> newRequests = new ArrayList<>();
            List<ExcelRequestWrapper> newRequestWrappers = new ArrayList<>();
            for(Request request : entry.getValue()) {
                if (!ValidationHelper.isNullOrEmpty(request.getService())) {
                    excelTableWrapper.getRequests().add(request);
                    ExcelRequestWrapper excelRequestWrapper = new ExcelRequestWrapper();
                    excelRequestWrapper.setRequestId(request.getId());
                    excelRequestWrapper.setTempId(request.getTempId());
                    excelTableWrapper.getRequestWrappers().add(excelRequestWrapper);
                    addColumnValues(request, columnValues,createExcelRequestsReportHelper);
                    List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                            Restrictions.eq("requestId", request.getId())});
                    Double result = 0d;
                    for (ExtraCost cost : extraCost) {
                        if(ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                            result = cost.getPrice();
                            try {
                                ExcelRequestWrapper newRequestWrapper = reportCopy(request);
                                newRequestWrapper.setRequestId(request.getId());
                                newRequestWrappers.add(newRequestWrapper);
                                addColumnValues(newRequestWrapper, request.getService(), columnValues,
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
                                ExcelRequestWrapper newRequestWrapper = reportCopy(request);
                                newRequestWrapper.setTempId(UUID.randomUUID().toString());
                                newRequestWrapper.setRequestId(request.getId());
                                newRequestWrapper.setEstateFormalityList(request.getEstateFormalityList());
                                newRequestWrappers.add(newRequestWrapper);
                                addColumnValues(newRequestWrapper, request.getService(), columnValues,
                                        createExcelRequestsReportHelper,-1,result);
                            } catch (CloneNotSupportedException e) {
                                LogHelper.log(log, e);
                            }
                        }
                    }
                    for (Service service : request.getMultipleServices()) {
                        try {
                            ExcelRequestWrapper newRequestWrapper = reportCopy(request);
                            newRequestWrapper.setTempId(UUID.randomUUID().toString());
                            newRequestWrapper.setRequestId(request.getId());
                            newRequestWrappers.add(newRequestWrapper);
                            addColumnValues(newRequestWrapper, service, columnValues,
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
            if(!ValidationHelper.isNullOrEmpty(newRequestWrappers)) {
                excelTableWrapper.getRequestWrappers().addAll(newRequestWrappers);
            }
            footerValues.put(ResourcesHelper.getString("requestedDate"), BillingTypeFields.EXCEL_TOTAL.toString().toUpperCase());
            
            colIndex = getIndex(BillingTypeFields.EXCEL_MORTGAGE_EXPENSES.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            DecimalFormat formatter = (DecimalFormat) nf;
            formatter.applyPattern("#,###.00");
            double result = 0.0;
            if(colIndex > -1){
                result = createExcelRequestsReportHelper.getSumOfCostEstateFormalityService(excelTableWrapper.getOriginalRequests());
                footerValues.put(BillingTypeFields.EXCEL_MORTGAGE_EXPENSES.toString(), result > 0 ? formatter.format(result) : "0.0");
            }

            colIndex = getIndex(BillingTypeFields.EXCEL_CATASTAL_EXPENSES.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
            if (colIndex > -1) {
                result = getSumOfCostCadastral(excelTableWrapper.getOriginalRequests());
                footerValues.put(BillingTypeFields.EXCEL_CATASTAL_EXPENSES.toString(), result > 0 ? formatter.format(result) : "0.0");
            }

            colIndex = getIndex(BillingTypeFields.EXCEL_COMPENSATION.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
            if (colIndex > -1) {
                result = createExcelRequestsReportHelper.getSumOfCostPayServices(excelTableWrapper.getOriginalRequests());
                footerValues.put(BillingTypeFields.EXCEL_COMPENSATION.toString(), result > 0 ? formatter.format(result) : "0.0");
            }

            colIndex = getIndex(BillingTypeFields.EXCEL_TOTAL.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
            if (colIndex > -1) {
                result = createExcelRequestsReportHelper.getSumOfCostTotalServices(excelTableWrapper.getOriginalRequests());
                footerValues.put(BillingTypeFields.EXCEL_TOTAL.toString(), result > 0 ? formatter.format(result) : "0.0");
            }

            excelTableWrapper.setColumnValues(columnValues);
            excelTableWrapper.setFooterValues(footerValues);
            getDataTable().add(excelTableWrapper);
        }
    }

    private void addColumnValues(Request request,
                                 Map<String, String> columnValues, CreateExcelRequestsReportHelper createExcelRequestsReportHelper) throws IllegalAccessException, PersistenceBeanException, InstantiationException{

        int colIndex = getIndex(BillingTypeFields.EXCEL_DATE.toString() , CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_DATE.toString(),request), DateTimeHelper.toString(request.getEvasionDate()));
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_NAME.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_NAME.toString(),request),
                    request.getSubject() != null ? (request.getSubject().getFullName() != null ? request.getSubject().getFullName().toUpperCase() : "") : "");
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_USER.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_USER.toString(),request),request.getRequestExcelUserName());
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_CODE.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_CODE.toString(),request), request.getFiscalCodeVATNamber());
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_OFFICE.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            Office office = DaoManager.get(Office.class, request.getUserOfficeId());
            if (!ValidationHelper.isNullOrEmpty(office)) {
                columnValues.put(getColumnName(BillingTypeFields.EXCEL_OFFICE.toString(),request), office.getCode() + " " + office.getDescription());
            }
        }
        colIndex = getIndex(BillingTypeFields.EXCEL_REQUEST_TYPE.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_REQUEST_TYPE.toString(),request), request.getServiceName().toUpperCase());
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_CONSERVATORIA.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_CONSERVATORIA.toString(),request),request.getAggregationLandChargesRegistryName());
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_FORMALITY.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_FORMALITY.toString(),request), String.valueOf(request.getNumberActOrSumOfEstateFormalitiesAndOther().longValue()));
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_MORTGAGE_EXPENSES.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_MORTGAGE_EXPENSES.toString(),request),
                    String.valueOf(getCostEstateFormalityAndExtraCostRelated(request)));
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_CATASTAL_EXPENSES.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_CATASTAL_EXPENSES.toString(),request), String.valueOf(getCostCadastralAndExtraCostRelated(request)));
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_COMPENSATION.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_COMPENSATION.toString(),request), ValidationHelper.isNullOrEmpty(request.getCostPay()) ? "0" : String.valueOf(request.getCostPay()));
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_TOTAL.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_TOTAL.toString(),request), ValidationHelper.isNullOrEmpty(request.getTotalCost()) ? "0" : request.getTotalCost().replaceAll(",", "."));
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_NOTE.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            if(ValidationHelper.isNullOrEmpty(request.getCostNote()))
                columnValues.put(getColumnName(BillingTypeFields.EXCEL_NOTE.toString(),request),createExcelRequestsReportHelper.getRequestExtraCostDistinctTypes(request));
            else
                columnValues.put(getColumnName(BillingTypeFields.EXCEL_NOTE.toString(),request),request.getCostNote());
        }
        colIndex = getIndex(BillingTypeFields.EXCEL_CDR.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_CDR.toString(),request),request.getCdr());
        }

        colIndex = getIndex(ResourcesHelper.getString("requestPrintFormalityPresentationDate"), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(ResourcesHelper.getString("requestPrintFormalityPresentationDate"),request), DateTimeHelper.toString(request.getEvasionDate()));
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_NDG.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_NDG.toString(),request),request.getNdg());
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_POSITION.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1 && !ValidationHelper.isNullOrEmpty(request.getPosition())) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_POSITION.toString(),request),request.getPosition());
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_STAMPS.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_STAMPS.toString(),request), String.valueOf(getExtraCostRelated(request, ExtraCostType.MARCA)));
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_POSTAL_EXPENSES.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_POSTAL_EXPENSES.toString(),request), String.valueOf(getExtraCostRelated(request, ExtraCostType.POSTALE)));
        }
    }

    private Boolean isBillingClient(ExcelRequestWrapper excelRequestWrapper) {
        if (!ValidationHelper.isNullOrEmpty(excelRequestWrapper.getBillingClient())) {
            return true;
        } else if (!ValidationHelper.isNullOrEmpty(excelRequestWrapper.getClient())
                && !ValidationHelper.isNullOrEmpty(excelRequestWrapper.getClient().getCostOutput())
                && excelRequestWrapper.getClient().getCostOutput()) {
            return false;
        }
        return null;
    }

    private boolean restrictionForPriceList(ExcelRequestWrapper request) {
        boolean result = false;

        if (!ValidationHelper.isNullOrEmpty(request.getDocumentsRequest())) {
            boolean isRequestHasDocumentWithSecondType = request.getDocumentsRequest().stream()
                    .anyMatch(x -> DocumentType.OTHER.getId().equals(x.getTypeId()));
            if (!ValidationHelper.isNullOrEmpty(request.getService())
                    && !ValidationHelper.isNullOrEmpty(request.getService().getUnauthorizedQuote())) {
                result = isRequestHasDocumentWithSecondType && request.getService().getUnauthorizedQuote();
            }
        }
        return result;
    }

    private void addColumnValues(ExcelRequestWrapper request, Service service,
                                 Map<String, String> columnValues,
                                 CreateExcelRequestsReportHelper createExcelRequestsReportHelper,
                                 int index, double extraCost) throws IllegalAccessException, PersistenceBeanException, InstantiationException{
        Boolean billingClient = isBillingClient(request);
        boolean restrictionForPriceList = restrictionForPriceList(request);
        int colIndex = getIndex(BillingTypeFields.EXCEL_DATE.toString() , CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(
                    getColumnName(BillingTypeFields.EXCEL_DATE.toString(),request,service), DateTimeHelper.toString(request.getEvasionDate()));
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_NAME.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(
                    getColumnName(BillingTypeFields.EXCEL_NAME.toString(),request,service),
                    request.getSubject() != null ? (request.getSubject().getFullName() != null ? request.getSubject().getFullName().toUpperCase() : "") : "");
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_USER.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_USER.toString(),request,service),request.getRequestExcelUserName());
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_CODE.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(
                    getColumnName(BillingTypeFields.EXCEL_CODE.toString(),request,service),
                    request.getFiscalCodeVATNamber());
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_OFFICE.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            Office office = DaoManager.get(Office.class, request.getUserOfficeId());
            if (!ValidationHelper.isNullOrEmpty(office)) {
                columnValues.put(getColumnName(BillingTypeFields.EXCEL_OFFICE.toString(),request,service),
                        office.getCode() + " " + office.getDescription());
            }
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_REQUEST_TYPE.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {

            String serviceName = request.getServiceName()!=null ? request.getServiceName().toUpperCase() : "";
            serviceName = service== null ? serviceName : service.toString().toUpperCase();
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_REQUEST_TYPE.toString(),request,service), serviceName);
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_CONSERVATORIA.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            if(index != -1) {
                columnValues.put(getColumnName(
                        BillingTypeFields.EXCEL_CONSERVATORIA.toString(),request,service),
                        request.getAggregationLandChargesRegistryName());
            }else {
                List<AggregationLandChargesRegistry> aggregationLandChargesRegistries =
                        DaoManager.load(AggregationLandChargesRegistry.class, new Criterion[]
                                {Restrictions.eq("national", Boolean.TRUE)});

                if(aggregationLandChargesRegistries.size() > 0) {
                    columnValues.put(getColumnName(
                            BillingTypeFields.EXCEL_CONSERVATORIA.toString(),request,service),
                            aggregationLandChargesRegistries.get(0).getName());
                }
            }

        }

        colIndex = getIndex(BillingTypeFields.EXCEL_FORMALITY.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1 && index != -1) {
            columnValues.put(getColumnName(
                    BillingTypeFields.EXCEL_FORMALITY.toString(),request,service),
                    String.valueOf(request.getNumberActOrSumOfEstateFormalitiesAndOther().longValue()));
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_MORTGAGE_EXPENSES.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            if(!ValidationHelper.isNullOrEmpty(service)) {
                Double cost = 0d;
                if(index != -1) {
                    cost = createExcelRequestsReportHelper.getCostEstateFormalityAndExtraCostRelated(
                            request.getRequest(),service,billingClient, restrictionForPriceList);
                }else {
                    cost += extraCost;
                }
                columnValues.put(getColumnName(BillingTypeFields.EXCEL_MORTGAGE_EXPENSES.toString(),request, service),
                        String.valueOf(cost));
            }
        }

        if(index != -1) {
            colIndex = getIndex(BillingTypeFields.EXCEL_CATASTAL_EXPENSES.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
            if (colIndex > -1) {
                columnValues.put(getColumnName(BillingTypeFields.EXCEL_CATASTAL_EXPENSES.toString(),request,service), String.valueOf(getCostCadastralAndExtraCostRelated(request.getRequest())));
            }
        }

        if(index != -1) {
            colIndex = getIndex(BillingTypeFields.EXCEL_COMPENSATION.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
            if (colIndex > -1) {
                if(!ValidationHelper.isNullOrEmpty(service)) {
                    columnValues.put(getColumnName(BillingTypeFields.EXCEL_COMPENSATION.toString(),request,service),
                            String.valueOf(
                                    createExcelRequestsReportHelper.getCostPay(
                                            request.getRequest(),service,billingClient, restrictionForPriceList)));
                }
            }
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_TOTAL.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            if(index != -1) {
                if(!ValidationHelper.isNullOrEmpty(service)) {
                    Double result = 0d;
                    if (!ValidationHelper.isNullOrEmpty(request.getCostCadastral())) {
                        result += request.getCostCadastral();
                    }
                    result += createExcelRequestsReportHelper.getCostExtra(request.getRequest(), service, billingClient, restrictionForPriceList);
                    result += createExcelRequestsReportHelper.getCostEstateFormality(request.getRequest(), service, billingClient, restrictionForPriceList);
                    result += createExcelRequestsReportHelper.getCostPay(request.getRequest(), service, billingClient, restrictionForPriceList);
                    result = (double) Math.round((result)* 100000d) / 100000d;
                    columnValues.put(getColumnName(BillingTypeFields.EXCEL_TOTAL.toString(),request,service), String.valueOf(result));
                }
            }else {
                columnValues.put(getColumnName(BillingTypeFields.EXCEL_TOTAL.toString(),request,service), String.valueOf(extraCost));
            }

        }

        colIndex = getIndex(BillingTypeFields.EXCEL_NOTE.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            if(index == 0) {
                if(extraCost > 0)
                    columnValues.put(getColumnName(BillingTypeFields.EXCEL_NOTE.toString(),request,service),"Costo aggiuntivo: " + extraCost);
            }else if(index != -1){
                if(ValidationHelper.isNullOrEmpty(request.getCostNote()))
                    columnValues.put(getColumnName(BillingTypeFields.EXCEL_NOTE.toString(),request,service),
                            createExcelRequestsReportHelper.getRequestExtraCostDistinctTypes(request.getRequest()));
                else
                    columnValues.put(getColumnName(BillingTypeFields.EXCEL_NOTE.toString(),request,service),request.getCostNote());
            }else {
                columnValues.put(getColumnName(BillingTypeFields.EXCEL_NOTE.toString(),request,service),"nazionale positiva");
            }
        }
        
        colIndex = getIndex(BillingTypeFields.EXCEL_CDR.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_CDR.toString(),request,service),request.getCdr());
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_NDG.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_NDG.toString(),request,service),request.getNdg());
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_POSITION.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1 && !ValidationHelper.isNullOrEmpty(request.getPosition())) {
            columnValues.put(getColumnName(BillingTypeFields.EXCEL_POSITION.toString(),request,service),request.getPosition());
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_STAMPS.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            if(index == 0) {
                Double extraCostSum = createExcelRequestsReportHelper.getRequestExtraCostSumByType(request.getRequestId(), ExtraCostType.MARCA);
                columnValues.put(getColumnName(BillingTypeFields.EXCEL_STAMPS.toString(),request,service),String.valueOf(extraCostSum));
            }
        }

        colIndex = getIndex(BillingTypeFields.EXCEL_POSTAL_EXPENSES.toString(), CreateExcelRequestsReportHelper.getRequestsEvasionColumns());
        if (colIndex > -1) {
            if(index == 0){
                Double extraCostSum = createExcelRequestsReportHelper.getRequestExtraCostSumByType(request.getRequestId(), ExtraCostType.POSTALE);
                columnValues.put(getColumnName(BillingTypeFields.EXCEL_POSTAL_EXPENSES.toString(),request,service),String.valueOf(extraCostSum));
            }
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

    private String getColumnName(String columnName, ExcelRequestWrapper request) {
        return columnName + "_" + request.getTempId();
    }

    private String getColumnName(String columnName, ExcelRequestWrapper request, Service service) {
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
            if(!ValidationHelper.isNullOrEmpty(getMail().getRecievedInbox()) &&
                    !ValidationHelper.isNullOrEmpty(getMail().getRecievedInbox().getRequests())) {
                recievedInboxRequests = getMail().getRecievedInbox().getRequests();
            }



            if (!ValidationHelper.isNullOrEmpty(getMail().getRequests()) ||
                    !ValidationHelper.isNullOrEmpty(recievedInboxRequests)) {
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
                    getMail().setClientInvoice(null);
                }

                if (!ValidationHelper.isNullOrEmpty(getSelectedOfficeId())) {
                    getMail().setOffice(DaoManager.get(Office.class, getSelectedOfficeId()));
                    excelDataWrapper.setOffice(getMail().getOffice().getDescription());
                } else {
                    getMail().setOffice(null);
                    excelDataWrapper.setOffice(null);
                }

                if (!ValidationHelper.isNullOrEmpty(getSelectedClientManagers())) {
                    getMail().setManagers(new ArrayList<>());
                    List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                            Restrictions.in("id", getSelectedClientManagers().stream()
                                    .map(SelectItemWrapper::getId).collect(Collectors.toList()))});
                    if (!ValidationHelper.isNullOrEmpty(clients)) {
                        getMail().setManagers(clients);
                    }
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
                List<Request> requests = !ValidationHelper.isNullOrEmpty(recievedInboxRequests) ? recievedInboxRequests : getMail().getRequests();
                List<Request> filteredRequests  = emptyIfNull(requests).stream().filter(r->r.isDeletedRequest()).collect(Collectors.toList());
                excelFile = new CreateExcelRequestsReportHelper(true).convertMailUserDataToExcel(filteredRequests, document,excelDataWrapper);
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

        if(!ValidationHelper.isNullOrEmpty(getMail().getClient()) &&
                !ValidationHelper.isNullOrEmpty(getMail().getClient().getTypeId())) {
            List<Client> notManagerOrFiduciaryClients = clientList.stream()
                    .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                    .collect(Collectors.toList());

            setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(notManagerOrFiduciaryClients.stream()
                    .filter(c -> Objects.nonNull(c))
                    .filter(c -> (c.getTypeId().equals(getMail().getClient().getTypeId())))
                    .collect(Collectors.toList()), true));
        }else {
            setNotManagerOrFiduciaryClients(ComboboxHelper.fillList(clientList.stream()
                    .filter(c -> Objects.nonNull(c))
                    .filter(c -> (c.getFiduciary() == null || !c.getFiduciary()) && (c.getManager() == null || !c.getManager()))
                    .collect(Collectors.toList()), true));
        }

        if (!ValidationHelper.isNullOrEmpty(getMail().getClient()) &&
                !ValidationHelper.isNullOrEmpty(getSelectedNotManagerOrFiduciaryClientId())) {
            SelectItemHelper.addItemToListIfItIsNotInIt(getNotManagerOrFiduciaryClients(), getMail().getClient());
        }
        if (!ValidationHelper.isNullOrEmpty(getExcelClientInvoiceId())) {
            SelectItemHelper.addItemToListIfItIsNotInIt(getInvoiceClients(), getMail().getClientInvoice());
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedClientFiduciaryId())) {
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
                return (ResourcesHelper.getString("excelDate"));
            case EXCEL_CONSERVATORIA:
                return (ResourcesHelper.getString("requestPrintCorservatoryName"));
            case EXCEL_NAME:
                return (ResourcesHelper.getString("excelName"));
            case EXCEL_CODE:
                return (ResourcesHelper.getString("excelCode"));
            case EXCEL_REQUEST_TYPE:
                return (ResourcesHelper.getString("excelRequestType"));
            case EXCEL_FORMALITY:
                return (ResourcesHelper.getString("excelFormality"));
            case EXCEL_MORTGAGE_EXPENSES:
                return (ResourcesHelper.getString("excelMortgageExpenses"));
            case EXCEL_CATASTAL_EXPENSES:
                return (ResourcesHelper.getString("excelCatastalExpenses"));
            case EXCEL_COMPENSATION:
                return (ResourcesHelper.getString("excelCompensation"));
            case EXCEL_TOTAL:
                return (ResourcesHelper.getString("excelTotal"));
            case EXCEL_NOTE:
                return (BillingTypeFields.EXCEL_NOTE.toString());
            case EXCEL_CDR:
                return (BillingTypeFields.EXCEL_CDR.toString());
            case EXCEL_NDG:
                return (BillingTypeFields.EXCEL_NDG.toString());
            case EXCEL_USER:
                return (ResourcesHelper.getString("excelUser"));
            case EXCEL_OFFICE:
                return (ResourcesHelper.getString("excelOffice"));
            case EXCEL_STAMPS:
                return (BillingTypeFields.EXCEL_STAMPS.toString());
            case EXCEL_POSTAL_EXPENSES:
                return (BillingTypeFields.EXCEL_POSTAL_EXPENSES.toString());
            case EXCEL_POSITION:
                return (BillingTypeFields.EXCEL_POSITION.toString());
            default:
                return null;
        }
    }

    public void saveRequestExtraCost() throws Exception {
        getCostManipulationHelper().setCostNote(getCostNote());
        //  DaoManager.refresh(getExamRequest());
        getCostManipulationHelper().saveRequestExtraCost(getExamRequest());
        CostCalculationHelper calculation = new CostCalculationHelper(getExamRequest());
        calculation.calculateAllCosts(true);
        loadPage();
    }

    public void updateCosts() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        getCostManipulationHelper().updateExamRequestParametersFromHelper(getExamRequest());
        getCostManipulationHelper().viewExtraCost(getExamRequest());

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

    public List<ExcelTableWrapper> getDataTable() {
        return dataTable;
    }

    public void setDataTable(List<ExcelTableWrapper> dataTable) {
        this.dataTable = dataTable;
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
        setCostNote(null);
        setCostManipulationHelper(new CostManipulationHelper());
        Request request =DaoManager.get(Request.class, getRequestId());
        if(!Hibernate.isInitialized(request.getRequestFormalities())){
            request.reloadRequestFormalities();
        }
        setExamRequest(request);
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
                costNote = ValidationHelper.isNullOrEmpty(costNote) ? new CreateExcelRequestsReportHelper().generateCorrectNote(getExamRequest()) : costNote.concat(" ").concat(new CreateExcelRequestsReportHelper().generateCorrectNote(getExamRequest()));
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }
        }else
            setCostNote(getExamRequest().getCostNote());
        getCostManipulationHelper().viewExtraCost(getExamRequest());
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


    public void deleteExtraCost(ExtraCost extraCostToDelete) {
        getCostManipulationHelper().getRequestExtraCosts().remove(extraCostToDelete);
        getCostManipulationHelper().setIncludeNationalCost(null);
    }

    public void addExtraCost(String extraCostValue) {
        getCostManipulationHelper().addExtraCost(extraCostValue, getRequestId());
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
}