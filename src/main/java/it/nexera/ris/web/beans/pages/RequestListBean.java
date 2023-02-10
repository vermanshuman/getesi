package it.nexera.ris.web.beans.pages;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.nexera.ris.api.ApiFacade;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.create.xls.CreateExcelRequestsReportHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.persistence.view.RequestSubjectView;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.AZRequestWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RequestStateWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RequestTypeFilterWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ServiceFilterWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserFilterWrapper;
import it.nexera.ris.web.common.EntityLazyListModel;
import it.nexera.ris.web.common.ListPaginator;
import it.nexera.ris.web.dto.AZSendRequestDTO;
import it.nexera.ris.web.dto.AZSendRequestResponseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;
import org.primefaces.model.SortOrder;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@ManagedBean(name = "requestListBean")
@ViewScoped
public class RequestListBean extends EntityLazyListPageBean<RequestSubjectView>
        implements Serializable {

    private static transient final Log log = LogFactory.getLog(RequestListBean.class);

    private static final long serialVersionUID = -5590180358388236956L;

    private Date dateFrom;

    private Date dateTo;

    private Date dateFromEvasion;

    private Date dateToEvasion;

    private Date dateExpiration;

    private Long selectedClientId;

    private List<SelectItem> clients;

    private Long selectedRequestType;

    private List<SelectItem> requestTypes;

    private Long selectedServiceType;

    private List<SelectItem> serviceTypes;

    private String searchLastName;

    private String searchFiscalCode;

    private String searchCreateUser;

    private Long downloadRequestId;

    private Long selectedTemplateId;

    private List<SelectItem> templates;

    private Request downloadRequest;

    private List<RequestStateWrapper> stateWrappers;

    private RequestStateWrapper selectedStateForFilter;

    private List<UserFilterWrapper> userWrappers;

    private UserFilterWrapper selectedUserForFilter;

    private List<Document> requestDocuments;

    private Boolean showPrintButton;

    private Long selectedState;

    private List<SelectItem> statesForSelect;

    private Long selectedUser;

    private List<SelectItem> usersForSelect;

    private List<Criterion> filterRestrictions;

    private List<RequestSubjectView> allRequestSubjectViewsToModify;

    private String selectedUserType;

    private List<SelectItem> userTypes;

    private List<SelectItem> landAggregations;

    private Long aggregationFilterId;

    private byte[] anomalyRequestsFile;

    private Boolean createTotalCostSumDocumentRecord;

    private List<SelectItem> fiduciaryClients;

    private Long fiduciaryClientFilterId;

    private List<SelectItem> managerClients;

    private Long managerClientFilterid;

    private List<RequestStateWrapper> selectedRequestStates;

    private List<ServiceFilterWrapper> serviceWrappers;

    private ServiceFilterWrapper selectedServiceForFilter;

    private List<SelectItem> servicesForSelect;

    private List<RequestTypeFilterWrapper> requestTypeWrappers;

    private RequestTypeFilterWrapper selectedRequestTypeForFilter;

    private List<SelectItem> requestTypesForSelect;

    private List<SelectItem> cities;

    private Integer expirationDays;

    private List<RequestState> selectedStates;

    private List<RequestType> selectedRequestTypes;

    private List<Service> selectedServices;

    private Integer rowsPerPage;

    private Integer pageNumber;

    private Integer requestType;

    @Getter
    @Setter
    private ListPaginator paginator;

    private boolean resetSettingPanel = true;

    String filterState = null;

    @Getter
    @Setter
    private List<Request> sendToBrexaRequests;

    @Getter
    @Setter
    private Long selectedAZRequestId;

    @Getter
    @Setter
    private List<AZRequestWrapper> azServices;

    @Getter
    @Setter
    private List<AZRequestWrapper> selectedAZServices;

    @Getter
    @Setter
    private EntityLazyListModel brexaLazyModel;

    @Getter
    @Setter
    private String azApiErrorMessage;

    private boolean selectAllSendToBrexaRequests = false;

    @Getter
    @Setter
    private TranscriptionAndCertificationHelper transcriptionAndCertificationHelper;

    @Getter
    @Setter
    private List<AZRequestWrapper> sendToAzRequests;

    @Getter
    @Setter
    private List<AZRequestWrapper> selectedAZRequests;

    @Getter
    @Setter
    private List<SelectItem> suppliers;

    @Getter
    @Setter
    private Long selectedSupplierId;

    @Getter
    @Setter
    private Boolean disableAllRowCheck;

    @Getter
    @Setter
    private Boolean azCheckTop;

    @Getter
    @Setter
    private Boolean azCheckBottom;
    
    @Getter
    @Setter
    private String cancelRequestComment;
    
    @Getter
    @Setter
    private Long cancelRequestId;

    private static final String KEY_CLIENT_ID = "KEY_CLIENT_ID_SESSION_KEY_NOT_COPY";
    private static final String KEY_STATES = "KEY_STATES_SESSION_KEY_NOT_COPY";
    private static final String KEY_REQUEST_TYPE = "KEY_REQUEST_TYPE_SESSION_KEY_NOT_COPY";
    private static final String KEY_SERVICES = "KEY_SERVICES_SESSION_KEY_NOT_COPY";
    private static final String KEY_CLIENT_MANAGER_ID = "KEY_CLIENT_MANAGER_ID_SESSION_KEY_NOT_COPY";
    private static final String KEY_CLIENT_FIDUCIARY_ID = "KEY_CLIENT_FIDUCIARY_ID_SESSION_KEY_NOT_COPY";
    private static final String KEY_AGGREAGATION = "KEY_AGGREAGATION_SESSION_KEY_NOT_COPY";
    private static final String KEY_DATE_EXPIRATION = "KEY_DATE_EXPIRATION_SESSION_KEY_NOT_COPY";
    private static final String KEY_DATE_FROM_REQ = "KEY_DATE_FROM_REQ_SESSION_KEY_NOT_COPY";
    private static final String KEY_DATE_TO_REQ = "KEY_DATE_TO_REQ_SESSION_KEY_NOT_COPY";
    private static final String KEY_DATE_FROM_EVASION = "KEY_DATE_FROM_EVASION_SESSION_KEY_NOT_COPY";
    private static final String KEY_DATE_TO_EVASION = "KEY_DATE_TO_EVASION_SESSION_KEY_NOT_COPY";
    private static final String KEY_NOMINATIVO = "KEY_NOMINATIVO_SESSION_KEY_NOT_COPY";
    private static final String KEY_CF = "KEY_CF_SESSION_KEY_NOT_COPY";
    private static final String KEY_ROWS_PER_PAGE = "KEY_ROWS_PER_PAGE_SESSION_KEY_NOT_COPY";
    private static final String KEY_PAGE_NUMBER = "KEY_PAGE_NUMBER_SESSION_KEY_NOT_COPY";

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {

        Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();
        RequestEditBean requestEditBean = (RequestEditBean) viewMap.get("requestEditBean");
        if (ValidationHelper.isNullOrEmpty(SessionHelper.get("loadRequestFilters"))
                && ValidationHelper.isNullOrEmpty(requestEditBean)) {
            clearFilterValueFromSession();
        }

        setPaginator(new ListPaginator(10, 1, 1, 1,
                "DESC", "createDate"));

        setSearchLastName((String) SessionHelper.get("searchLastName"));
        setSearchFiscalCode((String) SessionHelper.get("searchFiscalCode"));
        setSearchCreateUser((String) SessionHelper.get("searchCreateUser"));
        setStateWrappers(new ArrayList<>());
        setUserWrappers(new ArrayList<>());
        setStatesForSelect(new ArrayList<>());
        setUsersForSelect(new ArrayList<>());
        setServiceWrappers(new ArrayList<>());
        setServicesForSelect(new ArrayList<>());
        setRequestTypeWrappers(new ArrayList<>());
        setRequestTypesForSelect(new ArrayList<>());

        setClients(ComboboxHelper.fillList(DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"),
                        Restrictions.or(
                                Restrictions.eq("brexa", Boolean.FALSE),
                                Restrictions.isNull("brexa")))
        }).stream()
                .filter(c -> (
                                (ValidationHelper.isNullOrEmpty(c.getManager()) || !c.getManager()) &&
                                        (ValidationHelper.isNullOrEmpty(c.getFiduciary()) || !c.getFiduciary())
                        )
                ).sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList()), Boolean.TRUE));

        setRequestTypes(ComboboxHelper.fillList(RequestType.class, Boolean.FALSE));
        if (getRequestTypes().size() > 0) {
            Collections.sort(getRequestTypes(), (object1, object2) -> object1.getLabel().toUpperCase().compareTo(object2.getLabel().toUpperCase()));
        }

        setServiceTypes(ComboboxHelper.fillList(Service.class, Boolean.TRUE));
        if (getServiceTypes().size() > 0) {
            Collections.sort(getServiceTypes(), new Comparator<SelectItem>() {
                @Override
                public int compare(final SelectItem object1, final SelectItem object2) {
                    return object1.getLabel().toUpperCase().compareTo(object2.getLabel().toUpperCase());
                }
            });
        }
        setUserTypes(ComboboxHelper.fillList(UserCategories.class, Boolean.FALSE));
        setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.TRUE));

        setFiduciaryClients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                Restrictions.eq("fiduciary", Boolean.TRUE),
        }, Boolean.FALSE));

        setManagerClients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                Restrictions.eq("manager", Boolean.TRUE),
        }, Boolean.FALSE));


        List<User> notExternalCategoryUsers = DaoManager.load(User.class
                , new Criterion[]{Restrictions.or(
                        Restrictions.eq("category", UserCategories.INTERNO),
                        Restrictions.isNull("category")), Restrictions.eq("status", UserStatuses.ACTIVE),
                        Restrictions.isNotNull("getesi"),
                        Restrictions.eq("getesi", Boolean.TRUE)});

        notExternalCategoryUsers.forEach(u -> getUserWrappers().add(new UserFilterWrapper(u)));

        List<Service> services = DaoManager.load(Service.class, new Criterion[]{Restrictions.isNotNull("name")});
        if (!ValidationHelper.isNullOrEmpty(services)) {
            Collections.sort(services, new Comparator<Service>() {
                @Override
                public int compare(final Service object1, final Service object2) {
                    return object1.toString().toUpperCase().compareTo(object2.toString().toUpperCase());
                }
            });
            services.forEach(s -> getServiceWrappers().add(new ServiceFilterWrapper(s)));
        }


        if (!ValidationHelper.isNullOrEmpty(getSearchLastName()) || !ValidationHelper.isNullOrEmpty(getSearchFiscalCode())
                || !ValidationHelper.isNullOrEmpty(getSearchCreateUser())) {
            setSelectedAllStatesOnPanel(true);
        }

        Long dueRequestTypeId = (Long) SessionHelper.get("dueRequestTypeId");

        if (!ValidationHelper.isNullOrEmpty(dueRequestTypeId)) {
            SessionHelper.removeObject("dueRequestTypeId");
            List<RequestType> requestTypes = DaoManager.load(RequestType.class, new Criterion[]{Restrictions.isNotNull("name")});
            if (!ValidationHelper.isNullOrEmpty(requestTypes)) {
                Collections.sort(requestTypes, new Comparator<RequestType>() {
                    @Override
                    public int compare(final RequestType object1, final RequestType object2) {
                        return object1.toString().toUpperCase().compareTo(object2.toString().toUpperCase());
                    }
                });
                requestTypes.forEach(r -> {
                    getRequestTypeWrappers().add(new RequestTypeFilterWrapper(r.getId().equals(dueRequestTypeId), r));
                });
            }
            for (RequestState rs : RequestState.values()) {
                getStateWrappers().add(new RequestStateWrapper(!RequestState.EVADED.equals(rs), rs));
            }
            Integer expirationDays = (Integer) SessionHelper.get("expirationDays");
            if (!ValidationHelper.isNullOrEmpty(expirationDays)) {
                SessionHelper.removeObject("expirationDays");
                setExpirationDays(expirationDays);
            }
        } else {
            setExpirationDays(null);
            Arrays.asList(RequestState.values()).forEach(st -> getStateWrappers()
                    .add(new RequestStateWrapper(false, st)));

            List<RequestType> requestTypes = DaoManager.load(RequestType.class, new Criterion[]{Restrictions.isNotNull("name")});
            if (!ValidationHelper.isNullOrEmpty(requestTypes)) {
                Collections.sort(requestTypes, Comparator.comparing(object -> object.toString().toUpperCase()));
                requestTypes.forEach(r -> getRequestTypeWrappers().add(new RequestTypeFilterWrapper(r)));
            }

        }
        String filterStateBy = (String) SessionHelper.get("REQUEST_LIST_FILTER_BY");
        if (!ValidationHelper.isNullOrEmpty(filterStateBy)) {
            getStateWrappers().forEach(r -> {
                if (r.getState().equals(RequestState.valueOf(filterStateBy))) {
                    r.setSelected(Boolean.TRUE);
                } else {
                    r.setSelected(Boolean.FALSE);
                }
            });
            SessionHelper.removeObject("REQUEST_LIST_FILTER_BY");
        }
        loadFilterValueFromSession();
        if(StringUtils.isNotBlank(getSearchLastName()) || StringUtils.isNotBlank(getSearchFiscalCode())
                || StringUtils.isNotBlank(getSearchCreateUser())){
            filterState = "true";
        }
        filterTableFromPanel();
        setTranscriptionAndCertificationHelper(new TranscriptionAndCertificationHelper());
        this.setSuppliers(ComboboxHelper.fillList(Supplier.class, Order.asc("name"),
                Restrictions.eq("getesi", Boolean.TRUE)));
    }

    public void loadRequestDocuments() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request.id", getEntityEditId()),
                Restrictions.eq("selectedForEmail", true)
                //,Restrictions.ne("typeId", DocumentType.FORMALITY.getId())
        });

        List<Document> formalities = DaoManager.load(Document.class, new CriteriaAlias[]{
                new CriteriaAlias("formality", "f", JoinType.INNER_JOIN),
                new CriteriaAlias("f.requestList", "r_f", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("r_f.id", getEntityEditId()),
                Restrictions.eq("request.id", getEntityEditId()),
                Restrictions.eq("selectedForEmail", true)
        });

        if (!ValidationHelper.isNullOrEmpty(formalities)) {
            for (Document temp : formalities) {
                if (!documents.contains(temp)) {
                    documents.add(temp);
                }
            }
        }
        setRequestDocuments(documents);
    }


    public void loadAllegatiDocuments() throws PersistenceBeanException, IllegalAccessException {
        List<Document> documents = getAllegatiDocuments(getEntityEditId());
        setRequestDocuments(documents);
    }

    private List<Document> getAllegatiDocuments(Long requestId)
            throws PersistenceBeanException, IllegalAccessException {
        List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request.id", requestId),
                Restrictions.eq("typeId", DocumentType.ALLEGATI.getId())
        });

        List<Document> formalities = DaoManager.load(Document.class, new CriteriaAlias[]{
                new CriteriaAlias("formality", "f", JoinType.INNER_JOIN),
                new CriteriaAlias("f.requestList", "r_f", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("r_f.id", requestId),
                Restrictions.eq("typeId", DocumentType.ALLEGATI.getId())
        });

        if (!ValidationHelper.isNullOrEmpty(formalities)) {
            for (Document temp : formalities) {
                if (!documents.contains(temp)) {
                    documents.add(temp);
                }
            }
        }
        return documents;
    }

    public void downloadPdfFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        try {

            Request request = DaoManager.get(Request.class, downloadRequestId);

            String body = RequestHelper.getPdfRequestBody(request);
            updateFilterValueInSession();

            FileHelper.sendFile("richiesta-" + request.getStrId() + ".pdf",
                    PrintPDFHelper.convertToPDF(null, body, null,
                            DocumentType.ESTATE_FORMALITY));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void prepareToModify() throws IllegalAccessException, InstantiationException, PersistenceBeanException {
        setShowPrintButton(true);
        getStatesForSelect().add(SelectItemHelper.getNotSelected());
        getUsersForSelect().add(SelectItemHelper.getNotSelected());
        getServicesForSelect().add(SelectItemHelper.getNotSelected());
        getRequestTypesForSelect().add(SelectItemHelper.getNotSelected());
        Arrays.asList(RequestState.values()).forEach(st -> getStatesForSelect()
                .add(new SelectItem(st.getId(), st.toString())));
        getUserWrappers().forEach(u -> getUsersForSelect().add(new SelectItem(u.getId(), u.getValue())));
        getServiceWrappers().forEach(s -> getServicesForSelect().add(new SelectItem(s.getId(), s.getValue())));
        getRequestTypeWrappers().forEach(r -> getRequestTypesForSelect().add(new SelectItem(r.getId(), r.getValue())));
        filterState = "TRUE";
        filterTableFromPanel();

    }

    public void modifyRequests()
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        filterTableFromPanel();
        setAllRequestSubjectViewsToModify(
                DaoManager.load(RequestSubjectView.class, getFilterRestrictions().toArray(new Criterion[0])));
        List<Long> requestIdList = getAllRequestSubjectViewsToModify().stream()
                .map(RequestSubjectView::getId).collect(Collectors.toList());
        if (!ValidationHelper.isNullOrEmpty(getSelectedState())) {
            for (Long id : requestIdList) {
                RequestHelper.updateState(id, getSelectedState());
            }
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedUser())) {
            for (Long id : requestIdList) {
                RequestHelper.updateUser(id, getSelectedUser());
            }
        }
    }

    public void loadRequestsExcel() throws PersistenceBeanException,
            IllegalAccessException, IOException, InstantiationException {
        //filterTableFromPanel();
        setAllRequestSubjectViewsToModify(DaoManager.load(RequestSubjectView.class, getFilterRestrictions().toArray(new Criterion[0])));
        List<Long> requestIdList = getAllRequestSubjectViewsToModify().stream().map(RequestSubjectView::getId).collect(Collectors.toList());
        if (!ValidationHelper.isNullOrEmpty(requestIdList)) {
            List<Request> requests = DaoManager.load(Request.class, new Criterion[]{Restrictions.in("id", requestIdList)});
            String fileName = "evasioni" + ".xls";

            boolean isItInvoiceReport = !ValidationHelper.isNullOrEmpty(getCreateTotalCostSumDocumentRecord())
                    && getCreateTotalCostSumDocumentRecord();

            byte[] generatedExcel;
            if (!ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
                generatedExcel = new CreateExcelRequestsReportHelper(isItInvoiceReport)
                        .convertFilteredRequestsToExcel(requests, getSelectedClientId());
            } else {
                generatedExcel = new CreateExcelRequestsReportHelper(isItInvoiceReport)
                        .convertFilteredRequestsToExcel(requests);
            }

            if (isItInvoiceReport) {
                fileName = createTotalCostSumDocumentRecord(requests, generatedExcel);
            }

            FileHelper.sendFile(fileName, generatedExcel);
        }
    }

    private String createTotalCostSumDocumentRecord(List<Request> requests, byte[] generatedExcel)
            throws IOException, PersistenceBeanException, IllegalAccessException {
        long invoiceNumber = SaveRequestDocumentsHelper.getLastInvoiceNumber() + 1;
        String fileName = "evasioni_" + invoiceNumber + ".xls";

        String path = FileHelper.writeFileToFolder(fileName,
                new File(FileHelper.getApplicationProperties().getProperty("requestReportSavePath")), generatedExcel);

        Document totalCostSumDocument = new Document();
        double totalCostSum = requests.stream().filter(x -> !ValidationHelper.isNullOrEmpty(x.getTotalCost()))
                .map(x -> Double.parseDouble(x.getTotalCostDouble())).reduce(0.0, Double::sum);
        totalCostSumDocument.setCost(String.format("%.2f", totalCostSum));
        totalCostSumDocument.setTypeId(DocumentType.INVOICE_REPORT.getId());
        totalCostSumDocument.setInvoiceNumber(invoiceNumber);
        totalCostSumDocument.setPath(path);
        totalCostSumDocument.setDate(new Date());
        totalCostSumDocument.setTitle(fileName.substring(0, fileName.indexOf(".xls")));

        DaoManager.save(totalCostSumDocument, true);

        return fileName;
    }

    public void loadRequestsPdf() throws PersistenceBeanException, IllegalAccessException {
        setAllRequestSubjectViewsToModify(DaoManager.load(RequestSubjectView.class,
                getFilterRestrictions().toArray(new Criterion[0])));
        List<Long> requestIdList = getAllRequestSubjectViewsToModify().stream()
                .map(RequestSubjectView::getId).collect(Collectors.toList());
        try {
            Map<String, byte[]> files = new HashMap<>();
            Integer fileCounter = 1;
            for (Long requestId : requestIdList) {
                Request request = DaoManager.get(Request.class, requestId);

                List<Document> documents = getAllegatiDocuments(requestId);
                for (Document document : documents) {
                    try {
                        if (!ValidationHelper.isNullOrEmpty(document)) {
                            File file = new File(document.getPath());
                            if (!ValidationHelper.isNullOrEmpty(document.getTitle())) {
                                String title = prepareDocumentTitle(document);
                                FileInputStream inputFile = new FileInputStream(file);
                                byte[] data = new byte[(int) file.length()];
                                inputFile.read(data);
                                files.put(fileCounter++ + "_" + title, data);
                                if (inputFile != null) {
                                    inputFile.close();
                                }
                            }
                        }
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                }

                String body = RequestHelper.getPdfRequestBody(request);
                files.put(fileCounter++ + "_richiesta-" + request.getStrId() + ".pdf",
                        PrintPDFHelper.convertToPDF(null, body, null,
                                DocumentType.ESTATE_FORMALITY));
            }
            FileHelper.downloadFiles(files);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void downloadRequestFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
            Document document = DaoManager.get(Document.class, getEntityEditId());
            switch (DocumentType.getById(document.getTypeId())) {
                case CADASTRAL:
                case ESTATE_FORMALITY: {
                    String projectUrl = this.getRequest().getHeader("referer");
                    projectUrl = projectUrl.substring(0, projectUrl.indexOf(this.getCurrentPage().getPagesContext())) + "/";
                    PrintPDFHelper.generatePDFOnDocument(document.getId(), projectUrl);
                }
                break;
                case FORMALITY:
                case REQUEST_REPORT:
                case ALLEGATI:
                case ATTACHMENT_C:
                case SINGLE_FULFILMENT_FILE:
                case OTHER: {
                    File file = new File(document.getPath());
                    if (!ValidationHelper.isNullOrEmpty(document)) {
                        String title = prepareDocumentTitle(document);
                        try {
                            FileHelper.sendFile(title,
                                    new FileInputStream(file), (int) file.length());
                        } catch (Exception e) {
                            LogHelper.log(log, e);
                        }
                    }
                }
                break;
            }
        }
    }

    private String prepareDocumentTitle(Document document) {
        String path = !ValidationHelper.isNullOrEmpty(document.getPath()) ? document.getPath() : "";
        if (!ValidationHelper.isNullOrEmpty(document.getTitle())) {
            String title = document.getTitle();
            if (title.contains(".")) {
                int point = title.lastIndexOf(".");
                return title.substring(0, point) + path.substring(path.lastIndexOf("."));
            } else {
                return title + path.substring(path.lastIndexOf("."));
            }
        } else {
            return path.substring(path.lastIndexOf("\\") + 1);
        }
    }

    public void createNewRequest() {
        RedirectHelper.goTo(PageTypes.REQUEST_EDIT);
    }

    public void createNewMultipleRequest() {
        RedirectHelper.goToMultiple(PageTypes.REQUEST_EDIT);
    }

    public void manageRequest() {

        SessionHelper.put("searchLastName", getSearchLastName());
        SessionHelper.put("searchFiscalCode", getSearchFiscalCode());
        SessionHelper.put("searchCreateUser", getSearchCreateUser());
        updateFilterValueInSession();
        RedirectHelper.goTo(PageTypes.REQUEST_EDIT, getEntityEditId());
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        filterTableFromPanel(null);
    }

    public void filterTableFromPanel(String searchClicked) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        updateFilterValueInSession();
        List<Criterion> restrictions = RequestHelper.filterTableFromPanel(getDateFrom(), getDateTo(), getDateFromEvasion(),
                getDateToEvasion(), getSelectedClientId(), getRequestTypeWrappers(), getStateWrappers(), getUserWrappers(),
                getServiceWrappers(), getSelectedUserType(), getAggregationFilterId(), getSelectedServiceType(), Boolean.FALSE);

        if ((filterState == null || !Boolean.parseBoolean(filterState)) && ValidationHelper.isNullOrEmpty(getSelectedStates()))
            restrictions.add(Restrictions.or(Restrictions.eq("stateId", RequestState.INSERTED.getId()),
                    Restrictions.eq("stateId", RequestState.IN_WORK.getId()),
                    Restrictions.eq("stateId", RequestState.TO_BE_SENT.getId()),
                    Restrictions.eq("stateId", RequestState.PROFILED.getId()),
                    Restrictions.eq("stateId", RequestState.EST_TO_SENT.getId())));

        if (!ValidationHelper.isNullOrEmpty(getSearchLastName())) {
            restrictions.add(
                    Restrictions.or(
                            Restrictions.sqlRestriction("replace(replace(replace(name, '.', ''),'\\'',''), '  ', ' ') like '%"
                                    + getSearchLastName().replaceAll("\\.", "")
                                    .replaceAll("\\s+", " ")
                                    .replaceAll("'", "").trim() + "%'"),
                            Restrictions.sqlRestriction("replace(replace(replace(reverse_name, '.', ''),'\\'',''), '  ', ' ') like '%"
                                    + getSearchLastName().replaceAll("\\.", "")
                                    .replaceAll("\\s+", " ")
                                    .replaceAll("'", "").trim() + "%'")
                    )
            );
        }

        if (!ValidationHelper.isNullOrEmpty(getSearchFiscalCode())) {
            restrictions.add(Restrictions.ilike("code", getSearchFiscalCode().trim(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(getDateExpiration())) {
            restrictions.add(Restrictions.ge("expirationDate",
                    DateTimeHelper.getDayStart(getDateExpiration())));
            restrictions.add(Restrictions.le("expirationDate",
                    DateTimeHelper.getDayEnd(getDateExpiration())));
        }

        if (!ValidationHelper.isNullOrEmpty(getExpirationDays())) {
            Date dueDate = DateTimeHelper.addDays(DateTimeHelper.getNow(), getExpirationDays());
            restrictions.add(getExpirationDays() == 0 ? Restrictions.le("expirationDate", dueDate) :
                    Restrictions.ge("expirationDate", dueDate));
        }

        if (getCurrentUser().isExternal()) {
            restrictions.add(Restrictions.eq("createUserId", getCurrentUser().getId()));
        }

        if (!ValidationHelper.isNullOrEmpty(getSearchCreateUser())) {
            List<Long> longList = getUserListIds(getSearchCreateUser());
            if (!ValidationHelper.isNullOrEmpty(longList)) {
                restrictions.add(Restrictions.in("createUserId", longList));
            }
        }

        if (PageTypes.NOTARIAL_CERTIFICATION_LIST.equals(getCurrentPage())) {
            restrictions.add(Restrictions.isNotNull("distraintFormalityId"));
            if (!getCanList() && getCanListCreatedByUser()) {
                restrictions.add(Restrictions.eq("createUserId",
                        UserHolder.getInstance().getCurrentUser().getId()));
            }
        }

//        restrictions.add(Restrictions.or(Restrictions.eq("managedBy", 1),
//                Restrictions.isNull("managedBy"), Restrictions.eq("managedBy", 0)));

        setFilterRestrictions(restrictions);
        EntityLazyListModel<RequestSubjectView> model = new EntityLazyListModel<>(RequestSubjectView.class, restrictions.toArray(new Criterion[0]),
                new Order[]{
                        Order.desc("createDate")
                });
        // loadSendToBrexaTable(model);
        this.setLazyModel(model);
        this.setBrexaLazyModel(model);
        Arrays.stream(restrictions.toArray(new Criterion[0]))
                .forEach(r -> LogHelper.debugInfo(log, "Restriction(RequestList) : " + r));
        getLazyModel().load((getPaginator().getTablePage() - 1) * getPaginator().getRowsPerPage(), getPaginator().getRowsPerPage(),
                getPaginator().getTableSortColumn(),
                (getPaginator().getTableSortOrder() == null || getPaginator().getTableSortOrder().equalsIgnoreCase("DESC")
                        || getPaginator().getTableSortOrder().equalsIgnoreCase("UNSORTED")) ? SortOrder.DESCENDING : SortOrder.ASCENDING, new HashMap<>());

        Integer totalPages = (int) Math.ceil((getLazyModel().getRowCount() * 1.0) / getPaginator().getRowsPerPage());
        if (totalPages == 0)
            totalPages = 1;

        getPaginator().setRowCount(getLazyModel().getRowCount());
        getPaginator().setTotalPages(totalPages);
        getPaginator().setPage(getPaginator().getCurrentPageNumber());
    }

    public void loadSendToBrexaTable() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        List<RequestSubjectView> list = getBrexaLazyModel().loadList(getPaginator().getTableSortColumn(),
                (getPaginator().getTableSortOrder() == null || getPaginator().getTableSortOrder().equalsIgnoreCase("DESC")
                        || getPaginator().getTableSortOrder().equalsIgnoreCase("UNSORTED")) ? SortOrder.DESCENDING : SortOrder.ASCENDING, new HashMap<>());

        setSendToBrexaRequests(new ArrayList<>());
        for (RequestSubjectView RequestSubjectView : list) {
            if (!ValidationHelper.isNullOrEmpty(RequestSubjectView.getId())) {
                Request request = DaoManager.get(Request.class, RequestSubjectView.getId());
                request.setSelectedForVisibleExternal(!ValidationHelper.isNullOrEmpty(RequestSubjectView.getVisibleExternal()) ? RequestSubjectView.getVisibleExternal() : Boolean.FALSE);
                if (!ValidationHelper.isNullOrEmpty(request.getRequestType()) &&
                        !ValidationHelper.isNullOrEmpty(request.getRequestType().getSendBrexa())
                        && request.getRequestType().getSendBrexa() && (request.getStateId() == null || !request.getStateId().equals(RequestState.EXTERNAL.getId()))) {
                    getSendToBrexaRequests().add(request);
                }
            }
        }
        RequestContext.getCurrentInstance().update("sendToBrexaRequestsDialog");
    }

    public void setVisibleExternalStatus() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {
        for (Request request : getSendToBrexaRequests()) {
            if (request.isSelectedForVisibleExternal()) {
                RequestManagedBy managedByGetesi = new RequestManagedBy();
                managedByGetesi.setManagedBy(1);
                managedByGetesi.setRequest(request);
                DaoManager.save(managedByGetesi, true);

                request.setVisibleExternal(Boolean.TRUE);
                request.setStateId(RequestState.EXTERNAL.getId());
                request.setCertificationStateId(RequestState.EXTERNAL.getId());
                User brexaUser = DaoManager.get(User.class, new Criterion[]{Restrictions.eq("firstName", "Brexa"), Restrictions.eq("lastName", "Brexa")});
                if (!ValidationHelper.isNullOrEmpty(brexaUser))
                    request.setUser(brexaUser);
                DaoManager.save(request, true);

                RequestManagedBy managedByBrexa = new RequestManagedBy();
                managedByBrexa.setManagedBy(2);
                managedByBrexa.setRequest(request);
                DaoManager.save(managedByBrexa, true);
            }
        }
        if (filterState != null && Boolean.parseBoolean(filterState))
            filterTableFromPanel("TRUE");
        else
            filterTableFromPanel();
    }

    public void loadFilterData() throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getCities()))
            populateCities(getFilterRestrictions());
    }

    private void populateCities(List<Criterion> restrictions)
            throws PersistenceBeanException, IllegalAccessException {
        List<RequestSubjectView> requestList = DaoManager.load(RequestSubjectView.class, restrictions.toArray(new Criterion[0]));

        List<Long> cityIds = new ArrayList<>();


        for (RequestSubjectView request : requestList) {
            if (!ValidationHelper.isNullOrEmpty(request.getCityId()) && !cityIds.contains(request.getCityId())) {
                cityIds.add(request.getCityId());
            }
        }
        if (!ValidationHelper.isNullOrEmpty(cityIds)) {
            setCities(ComboboxHelper.fillList(City.class,
                    Order.asc("description"),
                    new Criterion[]{Restrictions.isNotNull("province.id")
                            , Restrictions.eq("external", Boolean.TRUE), Restrictions.in("id", cityIds)}, Boolean.FALSE));
        }
    }

    public void verifyRequests() throws PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        filterTableFromPanel();
        List<Criterion> criterionList = getFilterRestrictions();
        criterionList.addAll(Arrays.asList(
                Restrictions.or(
                        Restrictions.isNull("serviceIsUpdate"),
                        Restrictions.eq("serviceIsUpdate", Boolean.FALSE)
                ), Restrictions.isNotNull("numberActUpdate")));

        List<RequestSubjectView> RequestSubjectViews = DaoManager.load(RequestSubjectView.class, criterionList.toArray(new Criterion[0]));
        List<Long> requestIdList = RequestSubjectViews.stream().map(RequestSubjectView::getId).collect(Collectors.toList());
        if (!ValidationHelper.isNullOrEmpty(requestIdList)) {
            List<Request> anomalyRequests = DaoManager.load(Request.class, new Criterion[]{Restrictions.in("id", requestIdList)});
            anomalyRequests = anomalyRequests.stream().filter(r -> !r.getNumberActUpdate().equals(
                    Double.valueOf(r.getSumOfEstateFormalitiesAndCommunicationsAndSuccess()))).collect(Collectors.toList());
            if (!ValidationHelper.isNullOrEmpty(anomalyRequests)) {
                setAnomalyRequestsFile(new CreateExcelRequestsReportHelper().convertFilteredRequestsToExcel(anomalyRequests));
                return;
            }
        }
        executeJS("PF('nonAnomalyRequestFound').show()");
    }

    public void loadExcelWithAnomalyRequests() {
        if (!ValidationHelper.isNullOrEmpty(getAnomalyRequestsFile())) {
            String fileName = "Anomaly-requests" + ".xls";
            FileHelper.sendFile(fileName, getAnomalyRequestsFile());
            setAnomalyRequestsFile(null);
        }
    }

    private List<Long> getUserListIds(String searchCreateUser) throws PersistenceBeanException, IllegalAccessException {
        List<Long> longList = new ArrayList<>();
        List<User> users = DaoManager.load(User.class, new Criterion[]{
                Restrictions.or(
                        Restrictions.like("firstName", searchCreateUser),
                        Restrictions.like("lastName", searchCreateUser),
                        Restrictions.like("businessName", searchCreateUser),
                        Restrictions.isNotNull("getesi"),
                        Restrictions.eq("getesi", Boolean.TRUE)
                )
        });
        if (!ValidationHelper.isNullOrEmpty(users)) {
            for (User user : users) {
                longList.add(user.getId());
            }
        }
        return longList;
    }

    @Override
    protected void deleteEntityInternal(Long id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        Request request = DaoManager.get(Request.class, id);
        if (getCurrentUser().isExternal() && !request.getStateId().equals(RequestState.INSERTED.getId())) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    ResourcesHelper.getValidation("warning"),
                    ResourcesHelper.getValidation("cannotRemoveRequest"));
        } else {
            request.setIsDeleted(Boolean.TRUE);
            DaoManager.save(request);
        }
    }

    public void selectStateForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getStateWrappers())) {
            for (RequestStateWrapper wkrsw : this.getStateWrappers()) {
                if (wkrsw.getId()
                        .equals(this.getSelectedStateForFilter().getId())) {
                    wkrsw.setSelected(!wkrsw.getSelected().booleanValue());
                    break;
                }
            }
        }
    }

    public void selectUserForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getUserWrappers())) {
            for (UserFilterWrapper wkrsw : this.getUserWrappers()) {
                if (wkrsw.getId().equals(this.getSelectedUserForFilter().getId())) {
                    wkrsw.setSelected(!wkrsw.getSelected().booleanValue());
                    break;
                }
            }
        }
    }

    public void selectServiceForFilter() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(this.getServiceWrappers())) {
            for (ServiceFilterWrapper wkrsw : this.getServiceWrappers()) {
                if (wkrsw.getId().equals(this.getSelectedServiceForFilter().getId())) {
                    wkrsw.setSelected(!wkrsw.getSelected().booleanValue());
                    break;
                }
            }
        }
        selectServiceCheck();
    }

    public void selectServiceCheck() throws HibernateException, IllegalAccessException, PersistenceBeanException {

        if (!ValidationHelper.isNullOrEmpty(this.getServiceWrappers())) {

            List<ServiceFilterWrapper> filterWrappers =
                    this.getServiceWrappers().stream().
                            filter(sw -> Objects.nonNull(
                                    sw.getService().getServiceReferenceType()))
                            .filter(distinctByKey(sw -> sw.getService().getServiceReferenceType()))
                            .collect(Collectors.toList());

            if (ValidationHelper.isNullOrEmpty(filterWrappers)) {
                setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.FALSE));
            } else {
                boolean isConservatory = false;
                boolean isComuni = false;
                for (ServiceFilterWrapper wkrsw : filterWrappers) {
                    if (wkrsw.getSelected()) {
                        if (wkrsw.getService().getServiceReferenceType() == ServiceReferenceTypes.COMMON) {
                            isComuni = true;
                        } else {
                            isConservatory = true;
                        }
                    }
                }
                if (isComuni && isConservatory) {
                    setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.FALSE));
                    getLandAggregations().addAll(getCities());
                } else if (isComuni) {
                    setLandAggregations(getCities());
                } else {
                    setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.FALSE));
                }
            }
        } else {
            setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.FALSE));
        }
    }

    public void selectRequestTypeForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getRequestTypeWrappers())) {
            for (RequestTypeFilterWrapper wkrsw : this.getRequestTypeWrappers()) {
                if (wkrsw.getId().equals(this.getSelectedRequestTypeForFilter().getId())) {
                    wkrsw.setSelected(!wkrsw.getSelected().booleanValue());
                    break;
                }
            }
        }
    }

    private void fillTemplates(Request rec) {
        if (rec != null) {
            try {
                this.setTemplates(GeneralFunctionsHelper.fillTemplates(
                        DocumentGenerationPlaces.REQUEST_MANAGEMENT,
                        rec.getType(), null, DaoManager.getSession()));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void generate() throws PersistenceBeanException, IllegalAccessException {
        if (getDownloadRequest() != null) {
            GeneralFunctionsHelper.showReport(getDownloadRequest(),
                    getSelectedTemplateId(), getCurrentUser(),
                    false, DaoManager.getSession());
            this.setDownloadRequest(null);
        }
    }

    public boolean getSelectedAllStatesOnPanel() {
        if (this.getStateWrappers() != null) {
            for (RequestStateWrapper wlrsw : this.getStateWrappers()) {
                if (!wlrsw.getSelected().booleanValue()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAllStatesOnPanel(boolean selectedAllStatesOnPanel) {
        if (this.getStateWrappers() != null) {
            for (RequestStateWrapper wlrsw : this.getStateWrappers()) {
                wlrsw.setSelected(selectedAllStatesOnPanel);
            }
        }
    }

    public boolean getSelectedAllUsersOnPanel() {
        if (this.getUserWrappers() != null) {
            for (UserFilterWrapper wlrsw : this.getUserWrappers()) {
                if (!wlrsw.getSelected().booleanValue()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAllUsersOnPanel(boolean selectedAllStatesOnPanel) {
        if (this.getUserWrappers() != null) {
            for (UserFilterWrapper wlrsw : this.getUserWrappers()) {
                wlrsw.setSelected(selectedAllStatesOnPanel);
            }
        }
    }

    public boolean getSelectedAllServicesOnPanel() {
        if (this.getServiceWrappers() != null) {
            for (ServiceFilterWrapper wlrsw : this.getServiceWrappers()) {
                if (!wlrsw.getSelected().booleanValue()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAllServicesOnPanel(boolean selectedAllServicesOnPanel) {
        if (this.getServiceWrappers() != null) {
            for (ServiceFilterWrapper wlrsw : this.getServiceWrappers()) {
                wlrsw.setSelected(selectedAllServicesOnPanel);
            }
        }
    }

    public boolean getSelectedAllRequestTypesOnPanel() {
        if (this.getRequestTypeWrappers() != null) {
            for (RequestTypeFilterWrapper wlrsw : this.getRequestTypeWrappers()) {
                if (!wlrsw.getSelected().booleanValue()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAllRequestTypesOnPanel(boolean selectedAllRequestTypesOnPanel) {
        if (this.getRequestTypeWrappers() != null) {
            for (RequestTypeFilterWrapper wlrsw : this.getRequestTypeWrappers()) {
                wlrsw.setSelected(selectedAllRequestTypesOnPanel);
            }
        }
    }


    public void openRequestEditor() {
        RedirectHelper.goTo(PageTypes.REQUEST_TEXT_EDIT, getEntityEditId());
    }

    public void openRequestMail() {
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_VIEW, getEntityEditId());
    }

    public void openRequestMailNew() {
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_VIEW, getEntityEditId(), true);
    }

    public void openRequestSubject() {
        updateFilterValueInSession();
        RedirectHelper.goToOnlyView(PageTypes.SUBJECT, getEntityEditId());
    }

    private void updateFilterValueInSession() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
            SessionHelper.put(KEY_CLIENT_ID, getSelectedClientId());
        } else
            SessionHelper.removeObject(KEY_CLIENT_ID);
        if (!ValidationHelper.isNullOrEmpty(getStateWrappers())) {
            SessionHelper.put(KEY_STATES, getStateWrappers());
        } else
            SessionHelper.removeObject(KEY_STATES);

        if (!ValidationHelper.isNullOrEmpty(getRequestTypeWrappers())) {
            SessionHelper.put(KEY_REQUEST_TYPE, getRequestTypeWrappers());
        } else
            SessionHelper.removeObject(KEY_REQUEST_TYPE);
        if (!ValidationHelper.isNullOrEmpty(getServiceWrappers())) {
            SessionHelper.put(KEY_SERVICES, getServiceWrappers());
        } else
            SessionHelper.removeObject(KEY_SERVICES);
        if (!ValidationHelper.isNullOrEmpty(getManagerClientFilterid())) {
            SessionHelper.put(KEY_CLIENT_MANAGER_ID, getManagerClientFilterid());
        } else
            SessionHelper.removeObject(KEY_CLIENT_MANAGER_ID);
        if (!ValidationHelper.isNullOrEmpty(getFiduciaryClientFilterId())) {
            SessionHelper.put(KEY_CLIENT_FIDUCIARY_ID, getFiduciaryClientFilterId());
        } else
            SessionHelper.removeObject(KEY_CLIENT_FIDUCIARY_ID);
        if (!ValidationHelper.isNullOrEmpty(getAggregationFilterId())) {
            SessionHelper.put(KEY_AGGREAGATION, getAggregationFilterId());
        } else
            SessionHelper.removeObject(KEY_AGGREAGATION);
        if (!ValidationHelper.isNullOrEmpty(getDateExpiration())) {
            SessionHelper.put(KEY_DATE_EXPIRATION, getDateExpiration());
        } else
            SessionHelper.removeObject(KEY_DATE_EXPIRATION);
        if (!ValidationHelper.isNullOrEmpty(getDateFrom())) {
            SessionHelper.put(KEY_DATE_FROM_REQ, getDateFrom());
        } else
            SessionHelper.removeObject(KEY_DATE_FROM_REQ);
        if (!ValidationHelper.isNullOrEmpty(getDateTo())) {
            SessionHelper.put(KEY_DATE_TO_REQ, getDateTo());
        } else
            SessionHelper.removeObject(KEY_DATE_TO_REQ);
        if (!ValidationHelper.isNullOrEmpty(getDateFromEvasion())) {
            SessionHelper.put(KEY_DATE_FROM_EVASION, getDateFromEvasion());
        } else
            SessionHelper.removeObject(KEY_DATE_FROM_EVASION);
        if (!ValidationHelper.isNullOrEmpty(getDateToEvasion())) {
            SessionHelper.put(KEY_DATE_TO_EVASION, getDateToEvasion());
        } else
            SessionHelper.removeObject(KEY_DATE_TO_EVASION);
        if (!ValidationHelper.isNullOrEmpty(getSearchLastName())) {
            SessionHelper.put(KEY_NOMINATIVO, getSearchLastName());
        } else
            SessionHelper.removeObject(KEY_NOMINATIVO);
        if (!ValidationHelper.isNullOrEmpty(getSearchFiscalCode())) {
            SessionHelper.put(KEY_CF, getSearchFiscalCode());
        } else
            SessionHelper.removeObject(KEY_CF);
        if (!ValidationHelper.isNullOrEmpty(getRowsPerPage())) {
            SessionHelper.put(KEY_ROWS_PER_PAGE, getRowsPerPage());
        } else
            SessionHelper.removeObject(KEY_ROWS_PER_PAGE);

        if (!ValidationHelper.isNullOrEmpty(getPaginator().getCurrentPageNumber())) {
            SessionHelper.put(KEY_PAGE_NUMBER, getPaginator().getCurrentPageNumber());
        } else
            SessionHelper.removeObject(KEY_PAGE_NUMBER);
    }

    private void loadFilterValueFromSession() {
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_CLIENT_ID))) {
            setSelectedClientId((Long) SessionHelper.get(KEY_CLIENT_ID));
        } else {
            setSelectedClientId(null);
        }
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_STATES))) {
            setStateWrappers((List<RequestStateWrapper>) SessionHelper.get(KEY_STATES));
        }

        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_REQUEST_TYPE))) {
            setRequestTypeWrappers((List<RequestTypeFilterWrapper>) SessionHelper.get(KEY_REQUEST_TYPE));
        }
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_SERVICES))) {
            setServiceWrappers((List<ServiceFilterWrapper>) SessionHelper.get(KEY_SERVICES));
        }
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_CLIENT_MANAGER_ID))) {
            setManagerClientFilterid((Long) SessionHelper.get(KEY_CLIENT_MANAGER_ID));
        } else {
            setManagerClientFilterid(null);
        }
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_CLIENT_FIDUCIARY_ID))) {
            setFiduciaryClientFilterId((Long) SessionHelper.get(KEY_CLIENT_FIDUCIARY_ID));
        } else {
            setFiduciaryClientFilterId(null);
        }
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_AGGREAGATION))) {
            setAggregationFilterId((Long) SessionHelper.get(KEY_AGGREAGATION));
        } else {
            setAggregationFilterId(null);
        }
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_DATE_EXPIRATION))) {
            setDateExpiration((Date) SessionHelper.get(KEY_DATE_EXPIRATION));
        } else {
            setDateExpiration(null);
        }
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_DATE_FROM_REQ))) {
            setDateFrom((Date) SessionHelper.get(KEY_DATE_FROM_REQ));
        } else {
            setDateFrom(null);
        }
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_DATE_TO_REQ))) {
            setDateTo((Date) SessionHelper.get(KEY_DATE_TO_REQ));
        } else {
            setDateTo(null);
        }
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_DATE_FROM_EVASION))) {
            setDateFromEvasion((Date) SessionHelper.get(KEY_DATE_FROM_EVASION));
        } else {
            setDateFromEvasion(null);
        }
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_DATE_TO_EVASION))) {
            setDateToEvasion((Date) SessionHelper.get(KEY_DATE_TO_EVASION));
        } else {
            setDateToEvasion(null);
        }

        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_NOMINATIVO))) {
            setSearchLastName((String) SessionHelper.get(KEY_NOMINATIVO));
        } else {
            setSearchLastName(null);
        }

        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_CF))) {
            setSearchFiscalCode((String) SessionHelper.get(KEY_CF));
        } else {
            setSearchFiscalCode(null);
        }

        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_ROWS_PER_PAGE))) {
            setRowsPerPage((Integer) SessionHelper.get(KEY_ROWS_PER_PAGE));
        } else {
            setRowsPerPage(10);
        }
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_PAGE_NUMBER))) {
            getPaginator().setCurrentPageNumber((Integer) SessionHelper.get(KEY_PAGE_NUMBER));
        } else {
            getPaginator().setCurrentPageNumber(0);
        }
        getPaginator().setTablePage(getPaginator().getCurrentPageNumber());
//        executeJS("if (PF('tableWV').getPaginator() != null ) " +
//                "PF('tableWV').getPaginator().setPage(" + getPageNumber() + ");");
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public Long getSelectedClientId() {
        return selectedClientId;
    }

    public void setSelectedClientId(Long selectedClientId) {
        this.selectedClientId = selectedClientId;
    }

    public List<SelectItem> getClients() {
        return clients;
    }

    public void setClients(List<SelectItem> clients) {
        this.clients = clients;
    }

    public Long getDownloadRequestId() {
        return downloadRequestId;
    }

    public void setDownloadRequestId(Long downloadRequestId) {
        this.downloadRequestId = downloadRequestId;
    }

    public Long getSelectedTemplateId() {
        return selectedTemplateId;
    }

    public void setSelectedTemplateId(Long selectedTemplateId) {
        this.selectedTemplateId = selectedTemplateId;
    }

    public List<SelectItem> getTemplates() {
        return templates;
    }

    public void setTemplates(List<SelectItem> templates) {
        this.templates = templates;
    }

    public Request getDownloadRequest() {
        return downloadRequest;
    }

    public void setDownloadRequest(Request downloadRequest) {
        this.downloadRequest = downloadRequest;
    }

    public List<RequestStateWrapper> getStateWrappers() {
        return stateWrappers;
    }

    public void setStateWrappers(List<RequestStateWrapper> stateWrappers) {
        this.stateWrappers = stateWrappers;
    }

    public RequestStateWrapper getSelectedStateForFilter() {
        return selectedStateForFilter;
    }

    public void setSelectedStateForFilter(
            RequestStateWrapper selectedStateForFilter) {
        this.selectedStateForFilter = selectedStateForFilter;
    }

    public List<UserFilterWrapper> getUserWrappers() {
        return userWrappers;
    }

    public void setUserWrappers(List<UserFilterWrapper> userWrappers) {
        this.userWrappers = userWrappers;
    }

    public UserFilterWrapper getSelectedUserForFilter() {
        return selectedUserForFilter;
    }

    public void setSelectedUserForFilter(
            UserFilterWrapper selectedUserForFilter) {
        this.selectedUserForFilter = selectedUserForFilter;
    }

    public List<SelectItem> getRequestTypes() {
        return requestTypes;
    }

    public void setRequestTypes(List<SelectItem> requestTypes) {
        this.requestTypes = requestTypes;
    }

    public Long getSelectedRequestType() {
        return selectedRequestType;
    }

    public void setSelectedRequestType(Long selectedRequestType) {
        this.selectedRequestType = selectedRequestType;
    }

    public Long getSelectedServiceType() {
        return selectedServiceType;
    }

    public void setSelectedServiceType(Long selectedServiceType) {
        this.selectedServiceType = selectedServiceType;
    }

    public List<SelectItem> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List<SelectItem> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public String getSearchLastName() {
        return searchLastName;
    }

    public void setSearchLastName(String searchLastName) {
        this.searchLastName = searchLastName;
    }

    public String getSearchFiscalCode() {
        return searchFiscalCode;
    }

    public void setSearchFiscalCode(String searchFiscalCode) {
        this.searchFiscalCode = searchFiscalCode;
    }

    public List<Document> getRequestDocuments() {
        return requestDocuments;
    }

    public void setRequestDocuments(List<Document> requestDocuments) {
        this.requestDocuments = requestDocuments;
    }

    public Date getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(Date dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getSearchCreateUser() {
        return searchCreateUser;
    }

    public void setSearchCreateUser(String searchCreateUser) {
        this.searchCreateUser = searchCreateUser;
    }

    public Boolean getShowPrintButton() {
        return showPrintButton;
    }

    public void setShowPrintButton(Boolean showPrintButton) {
        this.showPrintButton = showPrintButton;
    }

    public Long getSelectedState() {
        return selectedState;
    }

    public void setSelectedState(Long selectedState) {
        this.selectedState = selectedState;
    }

    public Long getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(Long selectedUser) {
        this.selectedUser = selectedUser;
    }

    public List<SelectItem> getStatesForSelect() {
        return statesForSelect;
    }

    public void setStatesForSelect(List<SelectItem> statesForSelect) {
        this.statesForSelect = statesForSelect;
    }

    public List<SelectItem> getUsersForSelect() {
        return usersForSelect;
    }

    public void setUsersForSelect(List<SelectItem> usersForSelect) {
        this.usersForSelect = usersForSelect;
    }

    public List<RequestSubjectView> getAllRequestSubjectViewsToModify() {
        return allRequestSubjectViewsToModify;
    }

    public void setAllRequestSubjectViewsToModify(List<RequestSubjectView> allRequestSubjectViewsToModify) {
        this.allRequestSubjectViewsToModify = allRequestSubjectViewsToModify;
    }

    public List<Criterion> getFilterRestrictions() {
        return filterRestrictions;
    }

    public void setFilterRestrictions(List<Criterion> filterRestrictions) {
        this.filterRestrictions = filterRestrictions;
    }

    public String getSelectedUserType() {
        return selectedUserType;
    }

    public void setSelectedUserType(String selectedUserType) {
        this.selectedUserType = selectedUserType;
    }

    public List<SelectItem> getUserTypes() {
        return userTypes;
    }

    public void setUserTypes(List<SelectItem> userTypes) {
        this.userTypes = userTypes;
    }

    public Date getDateFromEvasion() {
        return dateFromEvasion;
    }

    public void setDateFromEvasion(Date dateFromEvasion) {
        this.dateFromEvasion = dateFromEvasion;
    }

    public Date getDateToEvasion() {
        return dateToEvasion;
    }

    public void setDateToEvasion(Date dateToEvasion) {
        this.dateToEvasion = dateToEvasion;
    }

    public List<SelectItem> getLandAggregations() {
        return landAggregations;
    }

    public void setLandAggregations(List<SelectItem> landAggregations) {
        this.landAggregations = landAggregations;
    }

    public Long getAggregationFilterId() {
        return aggregationFilterId;
    }

    public void setAggregationFilterId(Long aggregationFilterId) {
        this.aggregationFilterId = aggregationFilterId;
    }

    public byte[] getAnomalyRequestsFile() {
        return anomalyRequestsFile;
    }

    public void setAnomalyRequestsFile(byte[] anomalyRequestsFile) {
        this.anomalyRequestsFile = anomalyRequestsFile;
    }

    public Boolean getCreateTotalCostSumDocumentRecord() {
        return createTotalCostSumDocumentRecord;
    }

    public void setCreateTotalCostSumDocumentRecord(Boolean createTotalCostSumDocumentRecord) {
        this.createTotalCostSumDocumentRecord = createTotalCostSumDocumentRecord;
    }

    public List<SelectItem> getFiduciaryClients() {
        return fiduciaryClients;
    }

    public List<SelectItem> getManagerClients() {
        return managerClients;
    }

    public void setFiduciaryClients(List<SelectItem> fiduciaryClients) {
        this.fiduciaryClients = fiduciaryClients;
    }

    public void setManagerClients(List<SelectItem> managerClients) {
        this.managerClients = managerClients;
    }

    public Long getFiduciaryClientFilterId() {
        return fiduciaryClientFilterId;
    }


    public void setFiduciaryClientFilterId(Long fiduciaryClientFilterId) {
        this.fiduciaryClientFilterId = fiduciaryClientFilterId;
    }

    public Long getManagerClientFilterid() {
        return managerClientFilterid;
    }

    public void setManagerClientFilterid(Long managerClientFilterid) {
        this.managerClientFilterid = managerClientFilterid;
    }

    public List<RequestStateWrapper> getSelectedRequestStates() {
        return selectedRequestStates;
    }

    public void setSelectedRequestStates(List<RequestStateWrapper> selectedRequestStates) {
        this.selectedRequestStates = selectedRequestStates;
    }

    public List<ServiceFilterWrapper> getServiceWrappers() {
        return serviceWrappers;
    }

    public void setServiceWrappers(List<ServiceFilterWrapper> serviceWrappers) {
        this.serviceWrappers = serviceWrappers;
    }

    public ServiceFilterWrapper getSelectedServiceForFilter() {
        return selectedServiceForFilter;
    }

    public void setSelectedServiceForFilter(ServiceFilterWrapper selectedServiceForFilter) {
        this.selectedServiceForFilter = selectedServiceForFilter;
    }

    public List<SelectItem> getServicesForSelect() {
        return servicesForSelect;
    }

    public void setServicesForSelect(List<SelectItem> servicesForSelect) {
        this.servicesForSelect = servicesForSelect;
    }

    public List<RequestTypeFilterWrapper> getRequestTypeWrappers() {
        return requestTypeWrappers;
    }

    public RequestTypeFilterWrapper getSelectedRequestTypeForFilter() {
        return selectedRequestTypeForFilter;
    }

    public List<SelectItem> getRequestTypesForSelect() {
        return requestTypesForSelect;
    }

    public void setRequestTypeWrappers(List<RequestTypeFilterWrapper> requestTypeWrappers) {
        this.requestTypeWrappers = requestTypeWrappers;
    }

    public void setSelectedRequestTypeForFilter(RequestTypeFilterWrapper selectedRequestTypeForFilter) {
        this.selectedRequestTypeForFilter = selectedRequestTypeForFilter;
    }

    public void setRequestTypesForSelect(List<SelectItem> requestTypesForSelect) {
        this.requestTypesForSelect = requestTypesForSelect;
    }

    public List<SelectItem> getCities() {
        return cities;
    }

    public void setCities(List<SelectItem> cities) {
        this.cities = cities;
    }

    public Integer getExpirationDays() {
        return expirationDays;
    }

    public void setExpirationDays(Integer expirationDays) {
        this.expirationDays = expirationDays;
    }

    public Integer getRequestTypeSelected() {
        int selected = 0;
        for (RequestTypeFilterWrapper requestTypeFilterWrapper : requestTypeWrappers) {
            if (requestTypeFilterWrapper.getSelected()) {
                selected++;
            }
        }
        return selected;
    }

    public Integer getStateSelected() {
        int selected = 0;
        for (RequestStateWrapper requestStateWrapper : stateWrappers) {
            if (requestStateWrapper.getSelected()) {
                selected++;
            }
        }
        return selected;
    }

    public Integer getServiceSelected() {
        int selected = 0;
        for (ServiceFilterWrapper serviceFilterWrapper : serviceWrappers) {
            if (serviceFilterWrapper.getSelected()) {
                selected++;
            }
        }
        return selected;
    }

    public void reset() throws PersistenceBeanException, IOException, InstantiationException, IllegalAccessException {
        clearFilterValueFromSession();
        setSelectedClientId(null);
        setManagerClientFilterid(null);
        setFiduciaryClientFilterId(null);
        setAggregationFilterId(null);
        setStateWrappers(new ArrayList<>());
        setUserWrappers(new ArrayList<>());
        setServiceWrappers(new ArrayList<>());
        setRequestTypeWrappers(new ArrayList<>());
        setShowPrintButton(null);
        this.onLoad();
    }

    public void setSelectedStates(List<RequestState> selectedStates) {
        this.selectedStates = selectedStates;
    }

    public List<RequestState> getSelectedStates() {
        List<RequestState> selected = new ArrayList<>();
        for (RequestStateWrapper requestStateWrapper : stateWrappers) {
            if (requestStateWrapper.getSelected()) {
                selected.add(requestStateWrapper.getState());
            }
        }
        return selected;
    }

    public List<RequestType> getSelectedRequestTypes() {
        List<RequestType> selected = new ArrayList<>();
        for (RequestTypeFilterWrapper requestTypeFilterWrapper : requestTypeWrappers) {
            if (requestTypeFilterWrapper.getSelected()) {
                selected.add(requestTypeFilterWrapper.getRequestType());
            }
        }
        return selected;
    }

    public String getItemIconStyleClass(Long requestTypeId) {
        String iconStyleClass = "";
        if (!ValidationHelper.isNullOrEmpty(requestTypeId)) {
            try {
                RequestType requestTypeDTO = DaoManager.get(RequestType.class, requestTypeId);
                iconStyleClass = requestTypeDTO.getIcon();
                if (iconStyleClass.startsWith("fa")) {
                    iconStyleClass = "fa " + iconStyleClass;
                }
            } catch (HibernateException | InstantiationException | IllegalAccessException | PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        }
        return iconStyleClass;
    }

    public void handleRowsChange() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        String rowsPerPage = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowsPerPageSelected");
        String pageNumber = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("pageNumber");
        if (!ValidationHelper.isNullOrEmpty(rowsPerPage))
            getPaginator().setRowsPerPage(Integer.parseInt(rowsPerPage));

        Integer totalPages = getPaginator().getRowCount() / getPaginator().getRowsPerPage();
        Integer pageEnd = 10;
        if (pageEnd < totalPages)
            pageEnd = totalPages;
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= pageEnd; i++) {
            builder.append("<a class=\"ui-paginator-page ui-state-default ui-corner-all page_" + i + "\"");
            builder.append("tabindex=\"0\" href=\"#\" onclick=\"changePage(" + i + ",event)\">" + i + "</a>");
        }
        getPaginator().setPaginatorString(builder.toString());
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("fromPageChange", "true");
        filterTableFromPanel();
    }

    public void onPageChange() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        onPageChange(true);
    }

    public void onPageChange(Boolean filter) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        String rowsPerPage = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowsPerPageSelected");
        String pageNumber = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("pageNumber");
        if (!ValidationHelper.isNullOrEmpty(rowsPerPage))
            getPaginator().setRowsPerPage(Integer.parseInt(rowsPerPage));
        if (!ValidationHelper.isNullOrEmpty(pageNumber)) {
            Integer currentPage = Integer.parseInt(pageNumber);
            getPaginator().setCurrentPageNumber(currentPage);
            StringBuilder builder = new StringBuilder();
            String cls = "ui-paginator-first ui-state-default ui-corner-all";
            if (currentPage == 1) {
                cls += " ui-state-disabled";
            }
            builder.append("<a href=\"#\" class=\"" + cls + "\"");
            builder.append(" tabindex=\"-1\" onclick=\"firstPage(event)\">\n");
            builder.append("<span class=\"ui-icon ui-icon-seek-first\">F</span>\n</a>\n");
            builder.append("<a href=\"#\" onclick=\"previousPage(event)\"");
            cls = "ui-paginator-prev ui-corner-all";
            if (currentPage == 1) {
                cls += " ui-state-disabled";
            }
            builder.append(" class=\"" + cls + "\"");
            builder.append(" tabindex=\"-1\">\n");
            builder.append("<span class=\"ui-icon ui-icon-seek-prev\">P</span>\n</a>\n");

            getPaginator().setPageNavigationStart(builder.toString());
            if (currentPage == getPaginator().getTotalPages()) {
                builder.setLength(0);
                builder.append("<a href=\"#\" class=\"ui-paginator-next ui-state-default ui-corner-all ui-state-disabled\"");
                builder.append(" tabindex=\"0\">\n");
                builder.append("<span class=\"ui-icon ui-icon-seek-next\">N</span>\n</a>\n");
                builder.append("<a href=\"#\"");
                builder.append(" class=\"ui-paginator-last ui-state-default ui-corner-all ui-state-disabled\" tabindex=\"-1\">\n");
                builder.append("<span class=\"ui-icon ui-icon-seek-end\">E</span>\n</a>\n");
                getPaginator().setPageNavigationEnd(builder.toString());
            } else {
                builder.setLength(0);
                builder.append("<a href=\"#\" class=\"ui-paginator-next ui-state-default ui-corner-all\"  onclick=\"nextPage(event)\"");
                builder.append(" tabindex=\"0\">\n");
                builder.append("<span class=\"ui-icon ui-icon-seek-next\">N</span>\n</a>\n");
                builder.append("<a href=\"#\"");
                builder.append(" class=\"ui-paginator-last ui-state-default ui-corner-all\" tabindex=\"-1\" onclick=\"lastPage(event)\">\n");
                builder.append("<span class=\"ui-icon ui-icon-seek-end\">E</span>\n</a>\n");
                getPaginator().setPageNavigationEnd(builder.toString());
            }
            getPaginator().setTablePage(currentPage);
            FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("fromPageChange", "true");
            if (filter)
                filterTableFromPanel();
        }
    }

    public void clearFiltraPanel() throws PersistenceBeanException, IllegalAccessException {
        loadFilterData();
        setSelectedClientId(null);
        setDateFrom(null);
        setDateTo(null);
        setDateFromEvasion(null);
        setDateToEvasion(null);
        setDateExpiration(null);
        setRequestTypes(null);
        setManagerClientFilterid(null);
        setFiduciaryClientFilterId(null);
        setAggregationFilterId(null);
        setSelectedServices(null);
        clearFilterValueFromSession();
        resetSettingPanel = false;
    }

    private void clearFilterValueFromSession() {
        SessionHelper.removeObject(KEY_CLIENT_ID);
        SessionHelper.removeObject(KEY_DATE_FROM_REQ);
        SessionHelper.removeObject(KEY_DATE_TO_REQ);
        SessionHelper.removeObject(KEY_DATE_FROM_EVASION);
        SessionHelper.removeObject(KEY_DATE_TO_EVASION);
        SessionHelper.removeObject(KEY_DATE_EXPIRATION);
        SessionHelper.removeObject(KEY_REQUEST_TYPE);
        SessionHelper.removeObject(KEY_CLIENT_MANAGER_ID);
        SessionHelper.removeObject(KEY_CLIENT_FIDUCIARY_ID);
        SessionHelper.removeObject(KEY_AGGREAGATION);
        SessionHelper.removeObject(KEY_SERVICES);
        SessionHelper.removeObject(KEY_CF);
        SessionHelper.removeObject(KEY_NOMINATIVO);
        SessionHelper.removeObject(KEY_STATES);
        SessionHelper.removeObject(KEY_PAGE_NUMBER);
        SessionHelper.removeObject("searchLastName");
        SessionHelper.removeObject("searchFiscalCode");
        SessionHelper.removeObject("searchCreateUser");
    }

    public void setSelectedRequestTypes(List<RequestType> selectedRequestTypes) {
        this.selectedRequestTypes = selectedRequestTypes;
    }

    public List<Service> getSelectedServices() {
        List<Service> selected = new ArrayList<>();
        for (ServiceFilterWrapper serviceFilterWrapper : serviceWrappers) {
            if (serviceFilterWrapper.getSelected()) {
                selected.add(serviceFilterWrapper.getService());
            }
        }
        return selected;
    }

    public void setSelectedServices(List<Service> selectedServices) {
        this.selectedServices = selectedServices;
    }

    public void createNewMultipleRequests() {
        // executeJS("PF('chooseSingleOrMultipleRequestCreateWV').show();");
        String queryParam = RedirectHelper.FROM_PARAMETER + "=RICHESTE_MULTIPLE";
        RedirectHelper.goToMultiple(PageTypes.REQUEST_EDIT, queryParam);
    }

    public void redirectToNewMultipleRequests() {
        String queryParam = RedirectHelper.FROM_PARAMETER + "=RICHESTE_MULTIPLE&" + RedirectHelper.REQUEST_TYPE_PARAM + "=" + getRequestType();
        RedirectHelper.goToMultiple(PageTypes.REQUEST_EDIT, queryParam);
    }

    public Integer getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(Integer rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getRequestType() {
        return requestType;
    }

    public void setRequestType(Integer requestType) {
        this.requestType = requestType;
    }

    public boolean isResetSettingPanel() {
        return resetSettingPanel;
    }

    public void setResetSettingPanel(boolean resetSettingPanel) {
        this.resetSettingPanel = resetSettingPanel;
    }

    public void openTranscriptionManagement() throws
            PersistenceBeanException, InstantiationException, IllegalAccessException {
        updateFilterValueInSession();
        getTranscriptionAndCertificationHelper().openTranscriptionManagement(getEntityEditId()); }

    public void setRequestVisibleExternal() throws
            PersistenceBeanException, InstantiationException, IllegalAccessException {
        Request request = DaoManager.get(Request.class,
                new Criterion[]{Restrictions.eq("id", getEntityEditId())
                });

        RequestManagedBy managedByGetesi = new RequestManagedBy();
        managedByGetesi.setManagedBy(1);
        managedByGetesi.setRequest(request);
        managedByGetesi.setCreateDate(new Date());
        DaoManager.save(managedByGetesi, true);

        request.setVisibleExternal(Boolean.TRUE);
        request.setStateId(RequestState.EXTERNAL.getId());
        request.setCertificationStateId(RequestState.EXTERNAL.getId());
        User brexaUser = DaoManager.get(User.class, new Criterion[]{Restrictions.eq("firstName", "Brexa"), Restrictions.eq("lastName", "Brexa")});
        if (!ValidationHelper.isNullOrEmpty(brexaUser))
            request.setUser(brexaUser);
        DaoManager.save(request, true);

        RequestManagedBy managedByBrexa = new RequestManagedBy();
        managedByBrexa.setManagedBy(2);
        managedByBrexa.setRequest(request);
        managedByBrexa.setCreateDate(new Date());
        DaoManager.save(managedByBrexa, true);

        filterTableFromPanel();
    }

    @SneakyThrows
    public void resetPage() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        onPageChange(false);
        filterTableFromPanel("true");
    }

    public void resetPageSearch(Boolean searchClicked) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (searchClicked != null && searchClicked)
            filterState = "TRUE";
        onPageChange(false);
        filterTableFromPanel("true");

    }
    public void updateAZRequests() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        updateAZRequests(Boolean.TRUE);
    }
    public void updateAZRequests(Boolean update) throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        setDisableAllRowCheck(Boolean.FALSE);
        cleanValidation();
        if(update)
            setSelectedSupplierId(null);
        Request request = DaoManager.get(Request.class, new CriteriaAlias[]{
                new CriteriaAlias("multipleServices", "m", JoinType.LEFT_OUTER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("id", getSelectedAZRequestId())});
        setAzServices(new ArrayList<>());
        if (!ValidationHelper.isNullOrEmpty(request)) {
            if (!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                for (int s = 0; s < request.getMultipleServices().size(); s++) {
                    Service service = request.getMultipleServices().get(s);
                    AZRequestWrapper azRequestWrapper = new AZRequestWrapper();
                    azRequestWrapper.setService(service);
                    azRequestWrapper.setRequestId(request.getId());
                    azRequestWrapper.setServiceId(service.getId());
                    azRequestWrapper.setMultiple(Boolean.TRUE);
                    if (getSelectedSupplierId() != null && getSelectedSupplierId() > 0 && (ValidationHelper.isNullOrEmpty(service.getSupplier())
                            || !getSelectedSupplierId().equals(service.getSupplier().getId()))) {
                        setDisableAllRowCheck(Boolean.TRUE);
                        azRequestWrapper.setDifferentSupplier(Boolean.TRUE);
                    }
                    getAzServices().add(azRequestWrapper);
                }
            } else if(!ValidationHelper.isNullOrEmpty(request.getService())){
                AZRequestWrapper azRequestWrapper = new AZRequestWrapper();
                azRequestWrapper.setService(request.getService());
                azRequestWrapper.setRequestId(request.getId());
                azRequestWrapper.setServiceId(request.getService().getId());
                if (getSelectedSupplierId() != null && getSelectedSupplierId() > 0
                        && (ValidationHelper.isNullOrEmpty(request.getService().getSupplier())
                        || !getSelectedSupplierId().equals(request.getService().getSupplier().getId()))) {
                    setDisableAllRowCheck(Boolean.TRUE);
                    azRequestWrapper.setDifferentSupplier(Boolean.TRUE);
                }
                getAzServices().add(azRequestWrapper);
            }
        }
    }

    public void sendToAZ() {
        log.info("In sendToAZ");
        setAzApiErrorMessage(null);
        String serviceParameter = null;
        this.cleanValidation();
        if(ValidationHelper.isNullOrEmpty(getSelectedSupplierId())){
            addFieldException("supplierList", "supplierMissing");
        }
        if (this.getValidationFailed()) {
            return;
        }
        try {
            List<AZRequestWrapper> selectedServices = null;
            if(!ValidationHelper.isNullOrEmpty(getAzServices()))
                selectedServices = getAzServices()
                    .stream().filter(s -> s.getSelected() != null && s.getSelected())
                    .collect(Collectors.toList());
            else if(!ValidationHelper.isNullOrEmpty(getSelectedAZRequests()))
                selectedServices = getSelectedAZRequests()
                        .stream().filter(s -> s.getSelected() != null && s.getSelected())
                        .collect(Collectors.toList());

            if (!ValidationHelper.isNullOrEmpty(selectedServices)) {
                List<Long> selectedServiceIds = selectedServices
                        .stream()
                        .map(AZRequestWrapper::getServiceId)
                        .collect(Collectors.toList());
                List<AggregationService> aggregationServices = DaoManager.load(AggregationService.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("services", "s", JoinType.LEFT_OUTER_JOIN)
                        },
                        new Criterion[]{
                                Restrictions.in("s.id", selectedServiceIds)});

                if (!ValidationHelper.isNullOrEmpty(aggregationServices))
                    for (Iterator<AggregationService> it = aggregationServices.iterator(); it.hasNext(); ) {
                        AggregationService as = it.next();
                        if (!ValidationHelper.isNullOrEmpty(as.getServices())) {
                            List<Long> aggreationServiceIds = as.getServices()
                                    .stream()
                                    .map(Service::getId)
                                    .collect(Collectors.toList());
                            Collections.sort(selectedServiceIds, Comparator.naturalOrder());
                            Collections.sort(aggreationServiceIds, Comparator.naturalOrder());

                            if (!Objects.equals(selectedServiceIds, aggreationServiceIds)) {
                                it.remove();
                            }
                        }
                    }
                if (aggregationServices != null && !aggregationServices.isEmpty()) {
                    AggregationService aggregationService = aggregationServices.get(0);
                    if (aggregationService.getAggregationAZ() != null
                            && StringUtils.isNotBlank(aggregationService.getAggregationAZ().getCode()))
                        serviceParameter = aggregationServices.get(0).getAggregationAZ().getCode();
                }
            }
            AZSendRequestDTO requestDTO = new AZSendRequestDTO();
            if (StringUtils.isNotBlank(serviceParameter))
                requestDTO.setService(serviceParameter);
            else {
                log.info("serviceParameter null");
                setAzApiErrorMessage(ResourcesHelper.getString("azAPISendRequestMissingServiceError"));
                setValidationFailed(true);
                executeJS("PF('azAPIErrorDialogWV').show();");
                RequestContext.getCurrentInstance().update("azAPIErrorDialog");
                return;
            }

            Request request = DaoManager.get(Request.class, new CriteriaAlias[]{
                    new CriteriaAlias("subject", "s", JoinType.LEFT_OUTER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("id", getSelectedAZRequestId())});

            if (!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getSubject())) {
                requestDTO.setFiscalCode(request.getSubject().getFiscalCodeVATNamber());
            }
            requestDTO.setCustomCode(Arrays.asList(getSelectedAZRequestId()));
            AZSendRequestResponseDTO responseDto = ApiFacade.SINGLETON.sendAZRequest(requestDTO);
            boolean apiFailed= false;
            if (responseDto != null && responseDto.getSuccess() != null && responseDto.getSuccess()) {
                if(responseDto.getData() != null && StringUtils.isNotBlank(responseDto.getData().getTicketid())){
                    log.info("Get Ticket data " + responseDto.getData().getTicketid());
                    if (getSelectedSupplierId() != null) {
                        for (AZRequestWrapper azRequestWrapper : selectedServices) {
                            if (azRequestWrapper.getMultiple() != null && azRequestWrapper.getMultiple()) {
                                Request azRequest = DaoManager.get(Request.class,
                                        new CriteriaAlias[]{
                                                new CriteriaAlias("requestServices", "rs", JoinType.INNER_JOIN)
                                        },
                                        new Criterion[]{Restrictions.eq("id", azRequestWrapper.getRequestId())
                                        });
                                List<RequestService> filteredServices = emptyIfNull(azRequest.getRequestServices())
                                        .stream()
                                        .filter(rs -> rs.getService().getId().equals(azRequestWrapper.getServiceId()))
                                        .collect(Collectors.toList());
                                filteredServices
                                        .stream()
                                        .forEach(rs -> {
                                            rs.setSupplier_id(getSelectedSupplierId());
                                            DaoManager.saveWeak(rs, true);
                                        });
                            }else {
                                Request azRequest = DaoManager.get(Request.class,
                                        new Criterion[]{Restrictions.eq("id", azRequestWrapper.getRequestId())});
                                azRequest.setSupplier(DaoManager.get(Supplier.class, getSelectedSupplierId()));
                                DaoManager.save(azRequest, true);
                            }
                        }
                    }
                }
            } else {
                if (responseDto != null) {
                    log.error("Send az response " + new ObjectMapper().writeValueAsString(responseDto));
                }
                apiFailed = true;
            }
            if(apiFailed){
                setAzApiErrorMessage(String.format(
                        ResourcesHelper.getString("azAPISendRequestError"), "GHJGHJ66A13F839R"));
                setValidationFailed(true);
                executeJS("PF('azAPIErrorDialogWV').show();");
                RequestContext.getCurrentInstance().update("azAPIErrorDialog");
            }else {
                executeJS("PF('sendToAZDialogWV').hide();");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getSelectedAllSendToBrexaRequests() {
        if (this.getSendToBrexaRequests() != null) {
            for (Request request : this.getSendToBrexaRequests()) {
                if (!request.isSelectedForVisibleExternal()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAllSendToBrexaRequests(boolean selectedAllSendToBrexaRequests) {
        selectAllSendToBrexaRequests = !selectAllSendToBrexaRequests;
        if (this.getSendToBrexaRequests() != null) {
            for (Request request : this.getSendToBrexaRequests()) {
                request.setSelectedForVisibleExternal(selectAllSendToBrexaRequests);
            }
        }
    }

    public void loadSendToAZTable() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        loadSendToAZTable(Boolean.TRUE);
    }

    public void loadSendToAZTable(Boolean update) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        cleanValidation();
         setSelectedAZRequests(null);
        if(update)
            setSelectedSupplierId(null);
        setSendToAzRequests(null);
        setDisableAllRowCheck(Boolean.FALSE);

        List<RequestSubjectView> list = getBrexaLazyModel().loadList(getPaginator().getTableSortColumn(),
                (getPaginator().getTableSortOrder() == null || getPaginator().getTableSortOrder().equalsIgnoreCase("DESC")
                        || getPaginator().getTableSortOrder().equalsIgnoreCase("UNSORTED")) ? SortOrder.DESCENDING : SortOrder.ASCENDING, new HashMap<>());

        setSendToAzRequests(new ArrayList<>());
        Integer index = 1;
        for (RequestSubjectView requestSubjectView : list) {
            Request request = DaoManager.get(Request.class, requestSubjectView.getId());

            if (!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                int span = request.getMultipleServices().size();
                for (int s = 0; s < request.getMultipleServices().size(); s++) {
                    Service service = request.getMultipleServices().get(s);
                    AZRequestWrapper azRequestWrapper = new AZRequestWrapper();
                    azRequestWrapper.setService(service);
                    azRequestWrapper.setIndex(s);
                    azRequestWrapper.setRequestId(request.getId());
                    azRequestWrapper.setId(index++);
                    azRequestWrapper.setSubject(request.getMailViewSubject());
                    azRequestWrapper.setMultiple(Boolean.TRUE);
                    azRequestWrapper.setSpan(span);
                    azRequestWrapper.setServiceId(service.getId());
                    if (getSelectedSupplierId() != null && getSelectedSupplierId() > 0 && (ValidationHelper.isNullOrEmpty(service.getSupplier())
                            || !getSelectedSupplierId().equals(service.getSupplier().getId()))) {
                        setDisableAllRowCheck(Boolean.TRUE);
                        azRequestWrapper.setDifferentSupplier(Boolean.TRUE);
                    }
                    getSendToAzRequests().add(azRequestWrapper);
                }
            } else if(!ValidationHelper.isNullOrEmpty(request.getService())){
                AZRequestWrapper azRequestWrapper = new AZRequestWrapper();
                azRequestWrapper.setService(request.getService());
                azRequestWrapper.setId(index++);
                azRequestWrapper.setSubject(request.getMailViewSubject());
                azRequestWrapper.setRequestId(request.getId());
                azRequestWrapper.setServiceId(request.getService().getId());
                if (getSelectedSupplierId() != null && getSelectedSupplierId() > 0
                        && (ValidationHelper.isNullOrEmpty(request.getService().getSupplier())
                        || !getSelectedSupplierId().equals(request.getService().getSupplier().getId()))) {
                    setDisableAllRowCheck(Boolean.TRUE);
                    azRequestWrapper.setDifferentSupplier(Boolean.TRUE);
                }
                getSendToAzRequests().add(azRequestWrapper);
            }
        }
        if(update)
            RequestContext.getCurrentInstance().update("sendToAZRequestsDialog");
    }

    public void sendMultipleToAZ() {
        log.info("In sendMultipleToAZ");
        setAzApiErrorMessage(null);
        setSelectedAZServices(new ArrayList<>());
        this.cleanValidation();
        if(ValidationHelper.isNullOrEmpty(getSelectedSupplierId())){
            addFieldException("supplierList", "supplierMissing");
        }
        if (this.getValidationFailed()) {
            RequestContext.getCurrentInstance().update("dialogValidationMessages");
            return;
        }

        boolean requestError = true;
        try {
            if (!ValidationHelper.isNullOrEmpty(getSendToAzRequests())) {
                Map<Long, List<AZRequestWrapper>> requestMap = new HashMap<>();
                for (AZRequestWrapper azRequestWrapper : getSendToAzRequests()) {
                    if (!requestMap.containsKey(azRequestWrapper.getRequestId()))
                        requestMap.put(azRequestWrapper.getRequestId(), new ArrayList<>());
                    requestMap.get(azRequestWrapper.getRequestId()).add(azRequestWrapper);

                }
                for (Map.Entry<Long, List<AZRequestWrapper>> entry : requestMap.entrySet()) {
                    setSelectedAZRequests(entry.getValue());
                    setSelectedAZRequestId(entry.getKey());
                    try {
                        sendToAZ();
                    } catch (Exception e) {
                        requestError = true;
                        e.printStackTrace();
                    }
                }
            }
            if(!requestError){
                RequestContext.getCurrentInstance().update("sendToAZRequestsTable");
                executeJS("PF('sendToAZDialogWV').hide();");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            e.printStackTrace();
        }
    }

    public void azTopChangeListener() {
        boolean checkAll = getAzCheckTop() != null && getAzCheckTop();
        setAzCheckBottom(checkAll);
        if(!ValidationHelper.isNullOrEmpty(getAzServices()))
            this.getAzServices()
                    .stream()
                    .forEach(s -> s.setSelected(checkAll));

        if(!ValidationHelper.isNullOrEmpty(getSendToAzRequests()))
            this.getSendToAzRequests()
                    .stream()
                    .forEach(s -> s.setSelected(checkAll));

    }

    public void azDownChangeListener() {
        boolean checkAll = getAzCheckBottom() != null && getAzCheckBottom();
        setAzCheckTop(checkAll);
        if(!ValidationHelper.isNullOrEmpty(getAzServices()))
            this.getAzServices()
                    .stream()
                    .forEach(s -> s.setSelected(checkAll));

        if(!ValidationHelper.isNullOrEmpty(getSendToAzRequests()))
            this.getSendToAzRequests()
                    .stream()
                    .forEach(s -> s.setSelected(checkAll));
    }

    public void azItemChangeListener() {
        AZRequestWrapper unselectedRequest = null;

        if(!ValidationHelper.isNullOrEmpty(getAzServices()))
            unselectedRequest = getAzServices()
                .stream().filter(s -> s.getSelected() == null || !s.getSelected())
                .findFirst().orElse(null);

        if(!ValidationHelper.isNullOrEmpty(getSendToAzRequests()))
            unselectedRequest = getSendToAzRequests()
                    .stream().filter(s -> s.getSelected() == null || !s.getSelected())
                    .findFirst().orElse(null);

        if (unselectedRequest != null) {
            setAzCheckBottom(false);
            setAzCheckTop(false);
        } else {
            setAzCheckBottom(true);
            setAzCheckTop(true);
        }
    }
    
    public void cancelRequest() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
    	if(!ValidationHelper.isNullOrEmpty(getCancelRequestId())) {
    		Request request = DaoManager.get(Request.class, getCancelRequestId());
    		if(!ValidationHelper.isNullOrEmpty(request)) {
    			if(!ValidationHelper.isNullOrEmpty(getCancelRequestComment())) {
    				request.setComment(getCancelRequestComment());
    			}
    			request.setStateId(RequestState.CANCELLED.getId());
    			DaoManager.save(request, true);
    		}
    	}
    }
}
