package it.nexera.ris.web.beans.pages;

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
import it.nexera.ris.persistence.beans.entities.domain.readonly.WLGInboxShort;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.persistence.view.RequestView;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.logic.RequestStateWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RequestTypeFilterWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ServiceFilterWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserFilterWrapper;
import it.nexera.ris.web.common.EntityLazyListModel;
import it.nexera.ris.web.common.ListPaginator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
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

@ManagedBean(name = "requestListBean")
@ViewScoped
public class RequestListBean extends EntityLazyListPageBean<RequestView>
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

    private List<RequestView> allRequestViewsToModify;

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
        if (ValidationHelper.isNullOrEmpty(SessionHelper.get("loadRequestFilters"))) {
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
                        Restrictions.isNull("deleted"))
        }).stream()
                .filter(c -> (
                                (ValidationHelper.isNullOrEmpty(c.getManager()) || !c.getManager()) &&
                                        (ValidationHelper.isNullOrEmpty(c.getFiduciary()) || !c.getFiduciary())
                        )
                ).sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList()), Boolean.TRUE));

        setRequestTypes(ComboboxHelper.fillList(RequestType.class, Boolean.FALSE));
        if (getRequestTypes().size() > 0) {
            Collections.sort(getRequestTypes(), new Comparator<SelectItem>() {
                @Override
                public int compare(final SelectItem object1, final SelectItem object2) {
                    return object1.getLabel().toUpperCase().compareTo(object2.getLabel().toUpperCase());
                }
            });
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
                        Restrictions.isNull("category")), Restrictions.eq("status", UserStatuses.ACTIVE)});

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
        filterTableFromPanel();
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

    public void prepareToModify() {
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
    }

    public void modifyRequests()
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        filterTableFromPanel();
        setAllRequestViewsToModify(
                DaoManager.load(RequestView.class, getFilterRestrictions().toArray(new Criterion[0])));
        List<Long> requestIdList = getAllRequestViewsToModify().stream()
                .map(RequestView::getId).collect(Collectors.toList());
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
        setAllRequestViewsToModify(DaoManager.load(RequestView.class, getFilterRestrictions().toArray(new Criterion[0])));
        List<Long> requestIdList = getAllRequestViewsToModify().stream().map(RequestView::getId).collect(Collectors.toList());
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
        setAllRequestViewsToModify(DaoManager.load(RequestView.class,
                getFilterRestrictions().toArray(new Criterion[0])));
        List<Long> requestIdList = getAllRequestViewsToModify().stream()
                .map(RequestView::getId).collect(Collectors.toList());
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
        updateFilterValueInSession();
        String filterState = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("searchClicked");

        List<Criterion> restrictions = RequestHelper.filterTableFromPanel(getDateFrom(), getDateTo(), getDateFromEvasion(),
                getDateToEvasion(), getSelectedClientId(), getRequestTypeWrappers(), getStateWrappers(), getUserWrappers(),
                getServiceWrappers(), getSelectedUserType(), getAggregationFilterId(), getSelectedServiceType(), Boolean.FALSE);

        if((ValidationHelper.isNullOrEmpty(filterState) || !Boolean.parseBoolean(filterState)) && ValidationHelper.isNullOrEmpty(getSelectedStates()))
            restrictions.add(Restrictions.or(Restrictions.eq("stateId", RequestState.INSERTED.getId()),
                    Restrictions.eq("stateId", RequestState.IN_WORK.getId()), Restrictions.eq("stateId", RequestState.TO_BE_SENT.getId())));

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

        if (!ValidationHelper.isNullOrEmpty(getFiduciaryClientFilterId())) {
//            restrictions.add(Restrictions.eq("fiduciaryId",
//                    getFiduciaryClientFilterId()));
        }


        if (!ValidationHelper.isNullOrEmpty(getManagerClientFilterid())) {
//            restrictions.add(Restrictions.eq("managerId",
//                    getManagerClientFilterid()));
        }

        setFilterRestrictions(restrictions);
//        loadList(RequestView.class, restrictions.toArray(new Criterion[0]),
//                new Order[]{Order.desc("createDate")});
        this.setLazyModel(new EntityLazyListModel<>(RequestView.class, restrictions.toArray(new Criterion[0]),
                new Order[]{
                        Order.desc("createDate")
                }));

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

    public void loadFilterData() throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getCities()))
            populateCities(getFilterRestrictions());
    }

    private void populateCities(List<Criterion> restrictions)
            throws PersistenceBeanException, IllegalAccessException {
        List<RequestView> requestList = DaoManager.load(RequestView.class, restrictions.toArray(new Criterion[0]));

        List<Long> cityIds = new ArrayList<>();


        for (RequestView request : requestList) {
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

        List<RequestView> requestViews = DaoManager.load(RequestView.class, criterionList.toArray(new Criterion[0]));
        List<Long> requestIdList = requestViews.stream().map(RequestView::getId).collect(Collectors.toList());
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
                        Restrictions.like("businessName", searchCreateUser)
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
        }
        if (!ValidationHelper.isNullOrEmpty(getStateWrappers())) {
            SessionHelper.put(KEY_STATES, getStateWrappers());
        }
        if (!ValidationHelper.isNullOrEmpty(getRequestTypeWrappers())) {
            SessionHelper.put(KEY_REQUEST_TYPE, getRequestTypeWrappers());
        }
        if (!ValidationHelper.isNullOrEmpty(getServiceWrappers())) {
            SessionHelper.put(KEY_SERVICES, getServiceWrappers());
        }
        if (!ValidationHelper.isNullOrEmpty(getManagerClientFilterid())) {
            SessionHelper.put(KEY_CLIENT_MANAGER_ID, getManagerClientFilterid());
        }
        if (!ValidationHelper.isNullOrEmpty(getFiduciaryClientFilterId())) {
            SessionHelper.put(KEY_CLIENT_FIDUCIARY_ID, getFiduciaryClientFilterId());
        }
        if (!ValidationHelper.isNullOrEmpty(getAggregationFilterId())) {
            SessionHelper.put(KEY_AGGREAGATION, getAggregationFilterId());
        }
        if (!ValidationHelper.isNullOrEmpty(getDateExpiration())) {
            SessionHelper.put(KEY_DATE_EXPIRATION, getDateExpiration());
        }
        if (!ValidationHelper.isNullOrEmpty(getDateFrom())) {
            SessionHelper.put(KEY_DATE_FROM_REQ, getDateFrom());
        }
        if (!ValidationHelper.isNullOrEmpty(getDateTo())) {
            SessionHelper.put(KEY_DATE_TO_REQ, getDateTo());
        }
        if (!ValidationHelper.isNullOrEmpty(getDateFromEvasion())) {
            SessionHelper.put(KEY_DATE_FROM_EVASION, getDateFromEvasion());
        }
        if (!ValidationHelper.isNullOrEmpty(getDateToEvasion())) {
            SessionHelper.put(KEY_DATE_TO_EVASION, getDateToEvasion());
        }
        if (!ValidationHelper.isNullOrEmpty(getSearchLastName())) {
            SessionHelper.put(KEY_NOMINATIVO, getSearchLastName());
        }
        if (!ValidationHelper.isNullOrEmpty(getSearchFiscalCode())) {
            SessionHelper.put(KEY_CF, getSearchFiscalCode());
        }

        if (!ValidationHelper.isNullOrEmpty(getRowsPerPage())) {
            SessionHelper.put(KEY_ROWS_PER_PAGE, getRowsPerPage());
        }

        if (!ValidationHelper.isNullOrEmpty(getPaginator().getCurrentPageNumber())) {
            SessionHelper.put(KEY_PAGE_NUMBER, getPaginator().getCurrentPageNumber());
        }
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

    public List<RequestView> getAllRequestViewsToModify() {
        return allRequestViewsToModify;
    }

    public void setAllRequestViewsToModify(List<RequestView> allRequestViewsToModify) {
        this.allRequestViewsToModify = allRequestViewsToModify;
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
        filterTableFromPanel();
    }

    public void onPageChange() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
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
        TranscriptionData transcriptionData =  DaoManager.get(TranscriptionData.class,
                new CriteriaAlias[]{
                        new CriteriaAlias("request", "r", JoinType.INNER_JOIN)
                },
                new Criterion[]{Restrictions.eq("request.id", getEntityEditId())
        });
        updateFilterValueInSession();
        if(!ValidationHelper.isNullOrEmpty(transcriptionData)) {
        	RedirectHelper.goTo(PageTypes.TRANSCRIPTION_MANAGEMENT, getEntityEditId(), transcriptionData.getId());
        } else {
        	RedirectHelper.goTo(PageTypes.TRANSCRIPTION_MANAGEMENT, getEntityEditId(), null);
        }
    }
}