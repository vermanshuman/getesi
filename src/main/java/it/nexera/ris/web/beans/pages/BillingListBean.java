package it.nexera.ris.web.beans.pages;

import it.nexera.ris.api.FatturaAPI;
import it.nexera.ris.api.FatturaAPIResponse;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.create.xls.CreateExcelInvoicesReportHelper;
import it.nexera.ris.common.helpers.create.xls.CreateExcelRequestsReportHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.persistence.view.RequestView;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.GoodsServicesFieldWrapper;
import it.nexera.ris.web.beans.wrappers.logic.*;
import it.nexera.ris.web.common.EntityLazyListModel;
import it.nexera.ris.web.common.ListPaginator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

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
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@ManagedBean(name = "billingListBean")
@ViewScoped
@Getter
@Setter
public class BillingListBean extends EntityLazyListPageBean<Invoice>
        implements Serializable {

    private static transient final Log log = LogFactory.getLog(BillingListBean.class);

    private static final long serialVersionUID = -7955389068518829670L;

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
    private int activeTabIndex;

    private List<PaymentInvoice> paymentInvoices;

    private Double amountToBeCollected;

    private Double totalPayments;

    private Long number;

    private String invoiceNumber;

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

    private List<SelectItem> docTypes;

    private Date competence;

    private List<SelectItem> ums;

    private Long vatCollectabilityId;

    private List<SelectItem> vatCollectabilityList;

    private List<SelectItem> paymentTypes;

    private Long selectedPaymentTypeId;

    private Date invoiceDate;

    private String invoiceNote;

    private String apiError;

    private boolean sendInvoice;

    private Boolean billinRequest;

    private String documentType;

    List<RequestView> filteredRequest;

    String invoiceErrorMessage;

    private boolean invoiceSentStatus;

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

    private List<Request> invoicedRequests;

    private List<FileWrapper> invoiceEmailAttachedFiles;

    private boolean printPdf;

    private String mailPdf;

    private List<GoodsServicesFieldWrapper> goodsServicesFields;

    private List<SelectItem> invoiceClients;

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

    private Boolean showRequestTab;

    private StreamedContent invoicePDFFile;

    private Invoice selectedInvoice;

    private Double paymentAmount;

    private Date paymentDate;

    private String paymentDescription;

    private List<InvoiceItem> selectedInvoiceItems;

    private List<Criterion> invoiceTabCriteria;

    private InvoiceHelper invoiceHelper;

    private WLGExport excelInvoice;

    private WLGExport pdfInvoice;

    private WLGExport courtesyInvoicePdf;

    private Integer downloadFileIndex;

    private static final String MAIL_RERLY_FOOTER = ResourcesHelper.getString("emailReplyFooter");

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

    private boolean addAttachment = false;

    @ManagedProperty(value="#{invoiceDialogBean}")
    private InvoiceDialogBean invoiceDialogBean;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {

        if (!ValidationHelper.isNullOrEmpty(getRequestParameter(RedirectHelper.BILLING_LIST))) {
            openSubjectsToBeInvoicedTab();
        }
        invoiceHelper = new InvoiceHelper();
        List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"))});
        setClients(ComboboxHelper.fillList(clients.stream()
                .filter(c -> (
                                (ValidationHelper.isNullOrEmpty(c.getManager()) || !c.getManager()) &&
                                        (ValidationHelper.isNullOrEmpty(c.getFiduciary()) || !c.getFiduciary())
                        )
                ).sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList()), Boolean.TRUE));
        fillYears();

        filterTableFromPanel();

        setManagerClients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                Restrictions.eq("manager", Boolean.TRUE),
        }, Boolean.FALSE));

        setOffices(ComboboxHelper.fillList(Office.class, Boolean.TRUE));
        loadCompanies(clients);

        loadRequestPanel();
        setActiveTabIndex(0);
    }

    private void loadRequestPanel() throws PersistenceBeanException, IllegalAccessException {
        setStatesForSelect(new ArrayList<>());
        setPaginator(new ListPaginator(10, 1, 1, 1,
                "DESC", "createDate"));
        setStateWrappers(new ArrayList<>());
        setServicesForSelect(new ArrayList<>());
        setServiceWrappers(new ArrayList<>());
        setUsersForSelect(new ArrayList<>());
        setUserWrappers(new ArrayList<>());
        setRequestTypeWrappers(new ArrayList<>());
        setRequestTypesForSelect(new ArrayList<>());
        List<User> notExternalCategoryUsers = DaoManager.load(User.class
                , new Criterion[]{Restrictions.or(
                        Restrictions.eq("category", UserCategories.INTERNO),
                        Restrictions.isNull("category"))});

        notExternalCategoryUsers.forEach(u -> getUserWrappers().add(new UserFilterWrapper(u)));
        for (RequestState rs : RequestState.values()) {
            getStateWrappers().add(new RequestStateWrapper(RequestState.EVADED.equals(rs), rs));
        }

        List<RequestType> requestTypes = DaoManager.load(RequestType.class, new Criterion[]{Restrictions.isNotNull("name")});
        if (!ValidationHelper.isNullOrEmpty(requestTypes)) {
            Collections.sort(requestTypes, Comparator.comparing(object -> object.toString().toUpperCase()));
            requestTypes.forEach(r -> getRequestTypeWrappers().add(new RequestTypeFilterWrapper(r)));
        }

        List<Service> services = DaoManager.load(Service.class, new Criterion[]{Restrictions.isNotNull("name")});
        if (!ValidationHelper.isNullOrEmpty(services)) {
            Collections.sort(services, Comparator.comparing(object -> object.toString().toUpperCase()));
            services.forEach(s -> getServiceWrappers().add(new ServiceFilterWrapper(s)));
        }

        setFiduciaryClients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                Restrictions.eq("fiduciary", Boolean.TRUE),
        }, Boolean.FALSE));

        setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.TRUE));

        filterRequestTableFromPanel();
    }

    private void fillYears() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        List<Invoice> invoices = DaoManager.load(Invoice.class, new Criterion[]{Restrictions.or(Restrictions.isNotNull("date"))});
        Set<Integer> tempInvoices = new HashSet<>();
        for (Invoice invoice : invoices) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(invoice.getDate());
            int year = calendar.get(Calendar.YEAR);
            tempInvoices.add(year);
        }
        List<SelectItem> yearList = new ArrayList<SelectItem>();
        for (Integer year : tempInvoices) {
            yearList.add(new SelectItem(year));
        }
        setYears(yearList);
    }

    public void setQuadrimesterIdx(int startIdx, int endIdx) {
        quadrimesterStartIdx = startIdx;
        quadrimesterEndIdx = endIdx;
    }

    private double getRandomNumber(int min, int max) {
        return Math.random() * (max - min + 1) + min;
    }

    public List<Integer> getTestTurnoverPerMonth() {
        turnoverPerMonth.add(1);
        turnoverPerMonth.add(2);
        turnoverPerMonth.add(3);
        turnoverPerMonth.add(4);
        turnoverPerMonth.add(5);
        turnoverPerMonth.add(6);
        turnoverPerMonth.add(7);
        turnoverPerMonth.add(8);
        turnoverPerMonth.add(9);
        turnoverPerMonth.add(10);
        turnoverPerMonth.add(11);
        turnoverPerMonth.add(12);
        return turnoverPerMonth;
    }

    public List<String> getTestTurnoverPerCustomer() {
        turnoverPerCustomer.add("BCP");
        turnoverPerCustomer.add("Banca Sella");
        turnoverPerCustomer.add("Intrum");
        turnoverPerCustomer.add("Penelope SR");
        turnoverPerCustomer.add("BCP1");
        turnoverPerCustomer.add("Banca Sella1");
        turnoverPerCustomer.add("Intrum1");
        turnoverPerCustomer.add("Penelope SR1");
        turnoverPerCustomer.add("BCP2");
        turnoverPerCustomer.add("Banca Sella2");
        turnoverPerCustomer.add("Intrum2");
        turnoverPerCustomer.add("Penelope SR2");
        return turnoverPerCustomer;
    }

    public BillingListBean() {
        model = new BarChartModel();
        ChartSeries m1 = new ChartSeries();
        m1.setLabel("m1");
        m1.set("Jan", 120);
        m1.set("Feb", 20);
        m1.set("Mar", 100);
        m1.set("Apr", 50);
        m1.set("May", 60);
        m1.set("Jun", 80);
        m1.set("Jul", 90);
        m1.set("Aug", 100);
        m1.set("Sep", 70);
        m1.set("Oct", 30);
        m1.set("Nov", 90);
        m1.set("Dec", 100);
        model.addSeries(m1);
        model.setTitle("Indice di redditività");
        model.setLegendPosition("ne");
        model.setSeriesColors("DDDDDD60");
        model.setShadow(false);
        model.setExtender("customExtender");
        Axis xAxis = model.getAxis(AxisType.X);
        xAxis.setLabel("");
        Axis yAxis = model.getAxis(AxisType.Y);
        //   yAxis.setLabel("Sales");
        yAxis.setMin(0);
        yAxis.setMax(160);
        yAxis.setTickInterval("20.000");
        yAxis.setTickFormat("%'.3f");
    }

    public void filterTableFromPanel() throws IllegalAccessException, PersistenceBeanException {
        List<Criterion> restrictions = new ArrayList<>();
        List<Criterion> restrictionsLike = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getFilterInvoiceNumber())) {
            Criterion r = Restrictions.eq("number", getFilterInvoiceNumber());
            restrictionsLike.add(r);
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedCompanyId())) {
            restrictions.add(Restrictions.eq("client.id", getSelectedCompanyId()));
        }

        if (!ValidationHelper.isNullOrEmpty(getDateFrom())) {
            restrictions.add(Restrictions.ge("date", DateTimeHelper.getDayStart(getDateFrom())));
        }

        if (!ValidationHelper.isNullOrEmpty(getDateTo())) {
            restrictions.add(Restrictions.le("date", DateTimeHelper.getDayEnd(getDateTo())));
        }

        if (!ValidationHelper.isNullOrEmpty(getManagerClientFilterid())) {
            restrictions.add(Restrictions.eq("managerId", getManagerClientFilterid()));
            restrictions.add(Restrictions.eq("manager.id", getManagerClientFilterid()));
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedOfficeId())) {
            restrictions.add(Restrictions.eq("office.id", getSelectedOfficeId()));
        }

        if (!ValidationHelper.isNullOrEmpty(getFilterNotes())) {
            restrictions.add(Restrictions.eq("notes", getFilterNotes()));
        }

        if (!ValidationHelper.isNullOrEmpty(getFilterNdg())) {
            restrictions.add(Restrictions.eq("ndg", getFilterNdg()));
        }

        if (!ValidationHelper.isNullOrEmpty(getFilterPractice())) {
            restrictions.add(Restrictions.eq("practice", getFilterPractice()));
        }

        if (restrictionsLike.size() > 0) {
            if (restrictionsLike.size() > 1) {
                restrictions.add(Restrictions.or(restrictionsLike.toArray(new Criterion[restrictionsLike.size()])));
            } else {
                restrictions.add(restrictionsLike.get(0));
            }
        }
        setInvoiceTabCriteria(restrictions);
        loadList(Invoice.class, restrictions.toArray(new Criterion[0]), new Order[]{
                Order.desc("number")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException {
        setDateFrom(null);
        setDateTo(null);
        setSelectedClientId(null);
        setManagerClientFilterid(null);
        setSelectedOfficeId(null);
        setFilterNotes(null);
        setFilterNdg(null);
        setFilterPractice(null);
        setFilterAll(null);
        filterTableFromPanel();
    }

    public void reset() throws NumberFormatException, HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException, IOException {
        setDateFrom(null);
        setDateTo(null);
        setSelectedClientId(null);
        setManagerClientFilterid(null);
        setSelectedOfficeId(null);
        setFilterNotes(null);
        setFilterNdg(null);
        setFilterPractice(null);
        setServiceWrappers(new ArrayList<>());
        setRequestTypeWrappers(new ArrayList<>());
        this.onLoad();
    }

    public void resetSubject() throws NumberFormatException, HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException, IOException {

    }

    private void loadCompanies(List<Client> clients) throws HibernateException, IllegalAccessException, PersistenceBeanException {
        List<SelectItem> companies = new ArrayList<SelectItem>();
        for (Client client : clients) {
            if (client.getNameOfTheCompany() != null && !client.getNameOfTheCompany().isEmpty())
                companies.add(new SelectItem(client.getId(), client.getNameOfTheCompany()));
            else
                companies.add(new SelectItem(client.getId(), client.getNameProfessional()));
        }

        setCompanies(companies.stream()
                .sorted(Comparator.comparing(SelectItem::getLabel))
                .collect(Collectors.toList()));
    }

    public void filterRequestTableFromPanel() {

        List<Criterion> restrictions = RequestHelper.filterTableFromPanel(getDateFrom(), getDateTo(), getDateFromEvasion(),
                getDateToEvasion(), getSelectedSubjectClientId(), getRequestTypeWrappers(), getStateWrappers(), getUserWrappers(),
                getServiceWrappers(), null, getAggregationFilterId(), null, Boolean.FALSE);

        if (!ValidationHelper.isNullOrEmpty(getNameFilter())) {
            restrictions.add(Restrictions.or(
                    Restrictions.ilike("name", getNameFilter(), MatchMode.ANYWHERE),
                    Restrictions.ilike("code", getNameFilter().trim(), MatchMode.ANYWHERE)));
        }
//
//        if (!ValidationHelper.isNullOrEmpty(getSearchFiscalCode())) {
//            restrictions.add(Restrictions.ilike("code", getSearchFiscalCode().trim(), MatchMode.ANYWHERE));
//        }

        if (!ValidationHelper.isNullOrEmpty(getDateExpiration())) {
            restrictions.add(Restrictions.ge("expirationDate",
                    DateTimeHelper.getDayStart(getDateExpiration())));
            restrictions.add(Restrictions.le("expirationDate",
                    DateTimeHelper.getDayEnd(getDateExpiration())));
        }
//        List<Criterion> restrictions = new ArrayList<>();
//        List<Criterion> restrictionsLike = new ArrayList<>();
//
//        if (restrictionsLike.size() > 0) {
//            if (restrictionsLike.size() > 1) {
//                restrictions.add(Restrictions.or(restrictionsLike.toArray(new Criterion[restrictionsLike.size()])));
//            } else {
//                restrictions.add(restrictionsLike.get(0));
//            }
//        }
//        restrictions.add(Restrictions.eq("stateId", RequestState.EVADED.getId()));
        setRequestFilterRestrictions(restrictions);

        this.setLazySubjectModel(new EntityLazyListModel<>(RequestView.class, restrictions.toArray(new Criterion[0]),
                new Order[]{
                        Order.desc("createDate")
                }));

        getLazySubjectModel().load((getPaginator().getTablePage() - 1) * getPaginator().getRowsPerPage(), getPaginator().getRowsPerPage(),
                getPaginator().getTableSortColumn(),
                (getPaginator().getTableSortOrder() == null || getPaginator().getTableSortOrder().equalsIgnoreCase("DESC")
                        || getPaginator().getTableSortOrder().equalsIgnoreCase("UNSORTED")) ? SortOrder.DESCENDING : SortOrder.ASCENDING, new HashMap<>());

        Integer totalPages = (int) Math.ceil((getLazySubjectModel().getRowCount() * 1.0) / getPaginator().getRowsPerPage());
        if (totalPages == 0)
            totalPages = 1;

        getPaginator().setRowCount(getLazySubjectModel().getRowCount());
        getPaginator().setTotalPages(totalPages);
        getPaginator().setPage(getPaginator().getCurrentPageNumber());
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
        filterRequestTableFromPanel();
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
            filterRequestTableFromPanel();
        }
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

    public Integer getRequestTypeSelected() {
        int selected = 0;
        for (RequestTypeFilterWrapper requestTypeFilterWrapper : requestTypeWrappers) {
            if (requestTypeFilterWrapper.getSelected()) {
                selected++;
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

    public void setSelectedRequestTypes(List<RequestType> selectedRequestTypes) {
        this.selectedRequestTypes = selectedRequestTypes;
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

    public void openRequestMail() {
        RedirectHelper.goToMailManagerViewFromBillingList(getEntityEditId());
    }

    public void prepareToModify() {
        getStatesForSelect().add(SelectItemHelper.getNotSelected());
        getUsersForSelect().add(SelectItemHelper.getNotSelected());
        Arrays.asList(RequestState.values()).forEach(st -> getStatesForSelect()
                .add(new SelectItem(st.getId(), st.toString())));
        getUserWrappers().forEach(u -> getUsersForSelect().add(new SelectItem(u.getId(), u.getValue())));
        setShowPrintButton(true);
        getServicesForSelect().add(SelectItemHelper.getNotSelected());
        getServiceWrappers().forEach(s -> getServicesForSelect().add(new SelectItem(s.getId(), s.getValue())));
        getRequestTypesForSelect().add(SelectItemHelper.getNotSelected());
        getRequestTypeWrappers().forEach(r -> getRequestTypesForSelect().add(new SelectItem(r.getId(), r.getValue())));
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

    public void loadInvoiceDialogData(Invoice invoicedb) throws IllegalAccessException, PersistenceBeanException, HibernateException, InstantiationException  {
        setShowRequestTab(true);
        setActiveTabIndex(0);
        setInvoicedRequests(DaoManager.load(Request.class,
                new Criterion[]{
                        Restrictions.eq("invoice", invoicedb),
                        Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                                Restrictions.isNull("isDeleted"))
        }));
        if(!ValidationHelper.isNullOrEmpty(invoicedb) && !invoicedb.isNew()){
            List<PaymentInvoice> paymentInvoicesList = DaoManager.load(PaymentInvoice.class, new Criterion[] {Restrictions.eq("invoice", invoicedb)}, new Order[]{
                    Order.asc("date")});
            setPaymentInvoices(paymentInvoicesList);
            double totalImport = 0.0;
            for(PaymentInvoice paymentInvoice : paymentInvoicesList) {
                totalImport = totalImport + paymentInvoice.getPaymentImport().doubleValue();
            }
        }
        docTypes = new ArrayList<>();
        docTypes.add(new SelectItem("FE", "FATTURA"));
        setDocumentType("FE");
        competence = new Date();
        setVatCollectabilityList(ComboboxHelper.fillList(VatCollectability.class,
                false, false));
        //paymentTypes = ComboboxHelper.fillList(PaymentType.class);
        setPaymentTypes(ComboboxHelper.fillList(new ArrayList<>(), false));
        List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"))});
        setInvoiceClients(ComboboxHelper.fillList(clients.stream()
                .filter(c -> (
                                (ValidationHelper.isNullOrEmpty(c.getManager()) || !c.getManager()) &&
                                        (ValidationHelper.isNullOrEmpty(c.getFiduciary()) || !c.getFiduciary())
                        )
                ).sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList()), Boolean.TRUE));
        setSelectedInvoiceClientId(null);
        setSelectedInvoiceClient(null);
        setSelectedInvoice(invoicedb);
        if(!ValidationHelper.isNullOrEmpty(invoicedb)) {
            Invoice invoice = DaoManager.get(Invoice.class, invoicedb.getId());
            setSelectedInvoice(invoice);
            if(!ValidationHelper.isNullOrEmpty(invoice)) {
                paymentTypes = ComboboxHelper.fillList(invoice.getClient().getPaymentTypeList(), Boolean.FALSE);
                setInvoiceDate(invoice.getDate());
                setSelectedInvoiceClientId(invoice.getClient().getId());
                setSelectedInvoiceClient(invoice.getClient());
                if(!ValidationHelper.isNullOrEmpty(invoice.getPaymentType())){
                    setSelectedPaymentTypeId(invoice.getPaymentType().getId());
                }
                if(!ValidationHelper.isNullOrEmpty(invoice.getVatCollectability())){
                    setVatCollectabilityId(invoice.getVatCollectability().getId());
                }
                setInvoiceNote(invoice.getNotes());
                if(!ValidationHelper.isNullOrEmpty(invoice.getNumber()))
                    setNumber(invoice.getNumber());
                if(!ValidationHelper.isNullOrEmpty(invoice.getInvoiceNumber()))
                    setInvoiceNumber(invoice.getInvoiceNumber());
                List<GoodsServicesFieldWrapper> wrapperList = new ArrayList<>();
                int counter = 1;
                List<InvoiceItem> items = DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoice)});
                for (InvoiceItem invoiceItem : items) {
                    GoodsServicesFieldWrapper wrapper = invoiceHelper.createGoodsServicesFieldWrapper();//createGoodsServicesFieldWrapper();
                    wrapper.setCounter(counter);
                    wrapper.setInvoiceItemId(invoiceItem.getId());
                    wrapper.setInvoiceTotalCost(invoiceItem.getInvoiceTotalCost());
                    wrapper.setSelectedTaxRateId(invoiceItem.getTaxRate().getId());
                    wrapper.setInvoiceItemAmount(ValidationHelper.isNullOrEmpty(invoiceItem.getAmount()) ? 0.0 : invoiceItem.getAmount());
                    double totalcost = !(ValidationHelper.isNullOrEmpty(invoiceItem.getInvoiceTotalCost())) ? invoiceItem.getInvoiceTotalCost().doubleValue() : 0.0;
                    double amount = !(ValidationHelper.isNullOrEmpty(invoiceItem.getAmount())) ? invoiceItem.getAmount().doubleValue() : 0.0;
                    double totalLine = 0d;
                    if(amount != 0.0) {
                        totalLine = totalcost * amount;
                    } else {
                        totalLine = totalcost;
                    }
                    wrapper.setTotalLine(totalLine);
                    if(!ValidationHelper.isNullOrEmpty(invoiceItem.getDescription()))
                        wrapper.setDescription(invoiceItem.getDescription());
                    wrapperList.add(wrapper);
                    counter = counter + 1;
                }
                setGoodsServicesFields(wrapperList);
                setSameInvoiceNumber(invoice.getId());
                if(!ValidationHelper.isNullOrEmpty(invoice.getStatus()) && invoice.getStatus().equals(InvoiceStatus.DELIVERED)) {
                    setInvoiceSentStatus(true);
                }
            }
        } else {
            setGoodsServicesFields(new ArrayList<>());
            createNewGoodsServicesFields();
            setMaxInvoiceNumber();
            setInvoiceDate(new Date());
        }
        setClientProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
        getClientProvinces().add(new SelectItem(Province.FOREIGN_COUNTRY_ID, Province.FOREIGN_COUNTRY));
        setClientAddressCities(ComboboxHelper.fillList(new ArrayList<City>(), true));
        getInvoiceClientData(getSelectedInvoiceClient());
        loadDraftEmail();
    }

    public void loadInvoiceDialogDataEdit(Invoice invoice) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setNumber(invoice.getId());
        setSelectedInvoiceItems(DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoice)}));
        loadInvoiceDialogData(invoice);
        //executeJS("PF('invoiceDialogBillingWV').show();");
    }

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
        String invoiceNumber = (lastInvoiceNumber + 1) + "-" + currentYear + "-FE";
        setInvoiceNumber(invoiceNumber);
        setNumber(lastInvoiceNumber + 1);
    }

    public void setSameInvoiceNumber(Long lastInvoiceNumber) {
        LocalDate currentdate = LocalDate.now();
        int currentYear = currentdate.getYear();
        String invoiceNumber = (lastInvoiceNumber) + "-" + currentYear + "-FE";
        setInvoiceNumber(invoiceNumber);
        setNumber(lastInvoiceNumber);
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

    public Double getTotalGrossAmount() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        return invoiceHelper.getTotalGrossAmount(getGoodsServicesFields());
//        Double totalGrossAmount = 0D;
//        if(!ValidationHelper.isNullOrEmpty(getGoodsServicesFields())) {
//            for(GoodsServicesFieldWrapper wrapper: getGoodsServicesFields()) {
//                if(!ValidationHelper.isNullOrEmpty(wrapper.getTotalLine())){
//                    totalGrossAmount += wrapper.getTotalLine();
//                    if(!ValidationHelper.isNullOrEmpty(wrapper.getSelectedTaxRateId())){
//                        TaxRate taxrate = DaoManager.get(TaxRate.class, wrapper.getSelectedTaxRateId());
//                        if(!ValidationHelper.isNullOrEmpty(taxrate.getPercentage())){
//                            totalGrossAmount += (wrapper.getTotalLine() * (taxrate.getPercentage().doubleValue()/100));
//                        }
//                    }
//                }
//            }
//            BigDecimal tot = BigDecimal.valueOf(totalGrossAmount);
//            tot = tot.setScale(2, RoundingMode.HALF_UP);
//            totalGrossAmount = tot.doubleValue();
//        }
//        return totalGrossAmount;
    }

    public Double getAllTotalLine() {
        return invoiceHelper.getAllTotalLine(getGoodsServicesFields());
//        Double total = 0D;
//        if(!ValidationHelper.isNullOrEmpty(getGoodsServicesFields())) {
//            total = getGoodsServicesFields().stream().collect(
//                    Collectors.summingDouble(GoodsServicesFieldWrapper::getTotalLine));
//            BigDecimal tot = BigDecimal.valueOf(total);
//            tot = tot.setScale(2, RoundingMode.HALF_UP);
//            total = tot.doubleValue();
//        }
//        return total;
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
        GoodsServicesFieldWrapper wrapper = invoiceHelper.createGoodsServicesFieldWrapper();
        int size = getGoodsServicesFields().size();
        wrapper.setCounter(size + 1);
        getGoodsServicesFields().add(wrapper);
    }

    public void saveInvoiceInDraft() {
        cleanValidation();
        if(ValidationHelper.isNullOrEmpty(getInvoiceDate())){
            addRequiredFieldException("form:date");
            setValidationFailed(true);
        }

        if(ValidationHelper.isNullOrEmpty(getSelectedInvoiceClientId())){
            addRequiredFieldException("form:invoiceClient");
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

            if(ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getSelectedTaxRateId())){
                setValidationFailed(true);
            }
        }

        if (getValidationFailed()){
            executeJS("PF('invoiceErrorDialogWV').show();");
            return;
        }

        try {
            Invoice invoice = saveInvoice(InvoiceStatus.DRAFT, true);
            loadInvoiceDialogData(invoice);
        }catch(Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
        }
        executeJS("PF('invoiceDialogBillingWV').hide();");
        closeInvoiceDialog();

    }

    public void onItemSelectInvoiceClient() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        Client selectedClient = DaoManager.get(Client.class, getSelectedInvoiceClientId());
        setSelectedInvoiceClient(selectedClient);
        System.out.println(selectedClient.getSplitPayment());
        if(selectedClient.getSplitPayment() != null && selectedClient.getSplitPayment())
            setVatCollectabilityId(VatCollectability.SPLIT_PAYMENT.getId());
        else
            setVatCollectabilityId(null);
        if(selectedClient.getPaymentTypeList() != null && !selectedClient.getPaymentTypeList().isEmpty()) {
            setPaymentTypes(ComboboxHelper.fillList(selectedClient.getPaymentTypeList(), false));
        } else
            setPaymentTypes(ComboboxHelper.fillList(new ArrayList<>(), false));
        getInvoiceClientData(selectedClient);
    }

    public Invoice saveInvoice(InvoiceStatus invoiceStatus, Boolean saveInvoiceNumber) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        //Invoice invoice = DaoManager.get(Invoice.class, getNumber());
        if (getSelectedInvoice() == null) {
            setSelectedInvoice(new Invoice());
        }

        getSelectedInvoice().setDate(getInvoiceDate());
        getSelectedInvoice().setClient(getSelectedInvoiceClient());
        getSelectedInvoice().setDocumentType(getDocumentType());
        if(!ValidationHelper.isNullOrEmpty(getSelectedPaymentTypeId()))
            getSelectedInvoice().setPaymentType(DaoManager.get(PaymentType.class, getSelectedPaymentTypeId()));

        if(!ValidationHelper.isNullOrEmpty(getVatCollectabilityId()))
            getSelectedInvoice().setVatCollectability(VatCollectability.getById(getVatCollectabilityId()));
        getSelectedInvoice().setNotes(getInvoiceNote());
        getSelectedInvoice().setStatus(invoiceStatus);
        if(saveInvoiceNumber) {
            getSelectedInvoice().setNumber(getNumber());
            getSelectedInvoice().setInvoiceNumber(getInvoiceNumber());
        }
        getSelectedInvoice().setTotalGrossAmount(getTotalGrossAmount());
        DaoManager.save(getSelectedInvoice(), true);
        for(GoodsServicesFieldWrapper goodsServicesFieldWrapper : getGoodsServicesFields()) {
            if (!ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getInvoiceItemId())) {
                InvoiceItem invoiceItem = getSelectedInvoiceItems()
                        .stream().filter(inv ->
                                (inv.getId() != null
                                        && inv.getId().equals(goodsServicesFieldWrapper.getInvoiceItemId()))
                        ).findFirst().get();

                //  InvoiceItem invoiceItem = DaoManager.get(InvoiceItem.class, goodsServicesFieldWrapper.getInvoiceItemId());
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
                DaoManager.save(invoiceItem, true);
            }
        }
        if(!ValidationHelper.isNullOrEmpty(getClientAddressStreet()))
            getSelectedInvoiceClient().setAddressStreet(getClientAddressStreet());
        if(!ValidationHelper.isNullOrEmpty(getClientAddressPostalCode()))
            getSelectedInvoiceClient().setAddressPostalCode(getClientAddressPostalCode());
        if(!ValidationHelper.isNullOrEmpty(getClientAddressProvinceId()))
            getSelectedInvoiceClient().setAddressProvinceId(DaoManager.get(Province.class, getClientAddressProvinceId()));
        if(!ValidationHelper.isNullOrEmpty(getClientAddressCityId()))
            getSelectedInvoiceClient().setAddressCityId(DaoManager.get(City.class, getClientAddressCityId()));
        if(!ValidationHelper.isNullOrEmpty(getClientNumberVAT()))
            getSelectedInvoiceClient().setNumberVAT(getClientNumberVAT());
        if(!ValidationHelper.isNullOrEmpty(getClientFiscalCode()))
            getSelectedInvoiceClient().setFiscalCode(getClientFiscalCode());
        if(!ValidationHelper.isNullOrEmpty(getClientMailPEC()))
            getSelectedInvoiceClient().setMailPEC(getClientMailPEC());
        if(!ValidationHelper.isNullOrEmpty(getClientAddressSDI()))
            getSelectedInvoiceClient().setAddressSDI(getClientAddressSDI());
        DaoManager.save(getSelectedInvoiceClient(), true);
        return getSelectedInvoice();
    }

    public void sendInvoice() {
        cleanValidation();

        if(ValidationHelper.isNullOrEmpty(getInvoiceDate())){
            addRequiredFieldException("form:date");
            setValidationFailed(true);
        }

        if(ValidationHelper.isNullOrEmpty(getSelectedInvoiceClientId())){
            addRequiredFieldException("form:invoiceClient");
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

            if(ValidationHelper.isNullOrEmpty(goodsServicesFieldWrapper.getSelectedTaxRateId())){
                setValidationFailed(true);
            }
        }

        if (getValidationFailed()){
            executeJS("PF('invoiceErrorDialogWV').show();");
            return;
        }

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
                setInvoiceSentStatus(true);
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
                return;
            }
        }catch(Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
            setApiError(ResourcesHelper.getString("sendInvoiceErrorMsg"));
            executeJS("PF('sendInvoiceErrorDialogWV').show();");
            return;
        }
        executeJS("PF('invoiceDialogBillingWV').hide();");
        closeInvoiceDialog();
    }

    public void sendInvoice(Invoice invoiceDb) {
        try {
            Invoice invoice = DaoManager.get(Invoice.class, invoiceDb.getId());
            List<InvoiceItem> invoiceItems = DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoice)});
            FatturaAPI fatturaAPI = new FatturaAPI();
            String xmlData = fatturaAPI.getDataForXML(invoice, invoiceItems);
            log.info("Mailmanager XMLDATA: " + xmlData);
            FatturaAPIResponse fatturaAPIResponse = fatturaAPI.callFatturaAPI(xmlData, log);

            if (fatturaAPIResponse != null && fatturaAPIResponse.getReturnCode() != -1) {
                invoice.setStatus(InvoiceStatus.DELIVERED);
                DaoManager.save(invoice, true);
                setInvoiceSentStatus(true);
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
                return;
            }
        }catch(Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
            setApiError(ResourcesHelper.getString("sendInvoiceErrorMsg"));
            executeJS("PF('sendInvoiceErrorDialogWV').show();");
        }
    }

    public void loadRequestsExcel() throws PersistenceBeanException,
            IllegalAccessException, IOException, InstantiationException {
        setAllRequestViewsToModify(DaoManager.load(RequestView.class, getRequestFilterRestrictions().toArray(new Criterion[0])));
        List<Long> requestIdList = getAllRequestViewsToModify().stream().map(RequestView::getId).collect(Collectors.toList());
        if (!ValidationHelper.isNullOrEmpty(requestIdList)) {
            List<Request> requests = DaoManager.load(
                    Request.class, new Criterion[]{
                            Restrictions.in("id", requestIdList),
                            Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                                    Restrictions.isNull("isDeleted"))
                    });
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
            throws IOException, PersistenceBeanException {
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
                getRequestFilterRestrictions().toArray(new Criterion[0])));

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



    public void modifyRequests()
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        filterRequestTableFromPanel();
        setAllRequestViewsToModify(
                DaoManager.load(RequestView.class, getRequestFilterRestrictions().toArray(new Criterion[0])));
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

    public void downloadInvoice(){

    }

    public void manageRequest() {
        RedirectHelper.goTo(PageTypes.REQUEST_EDIT, getSelectedRequestId());
    }

    public void openRequestSubject() {
        RedirectHelper.goToOnlyView(PageTypes.SUBJECT, getSelectedRequestId());
    }

    public void downloadPdfFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        try {
            Request request = DaoManager.get(Request.class, getDownloadRequestId());
            String body = RequestHelper.getPdfRequestBody(request);
            FileHelper.sendFile("richiesta-" + request.getStrId() + ".pdf",
                    PrintPDFHelper.convertToPDF(null, body, null,
                            DocumentType.ESTATE_FORMALITY));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void editExcelData() throws PersistenceBeanException, IllegalAccessException {
        setAllRequestViewsToModify(DaoManager.load(RequestView.class, getRequestFilterRestrictions().toArray(new Criterion[0])));
        List<Long> requestIdList = getAllRequestViewsToModify().stream().map(RequestView::getId).collect(Collectors.toList());
        if(!ValidationHelper.isNullOrEmpty(requestIdList)){
            SessionHelper.put("selectedRequestIds", requestIdList);
            SessionHelper.put("selectedRequestClientId", getSelectedSubjectClientId());
        }
        RedirectHelper.goTo(PageTypes.EXCEL_DATA,null, true);
    }

    public void createInvoice() throws IllegalAccessException, PersistenceBeanException, HibernateException, InstantiationException {
        setMaxInvoiceNumber();
        List<RequestView> allRequestViews = DaoManager.load(RequestView.class,
                getRequestFilterRestrictions().toArray(new Criterion[0]));
        List<Long> requestIdList = allRequestViews.stream().map(RequestView::getId).collect(Collectors.toList());
        if(!ValidationHelper.isNullOrEmpty(allRequestViews)){
            List<Request> filteredRequests = DaoManager.load(Request.class, new Criterion[]{
                    Restrictions.in("id", requestIdList)
            });

            Invoice invoice = new Invoice();
            invoice.setClient(DaoManager.get(Client.class,getSelectedSubjectClientId()));
            invoice.setDate(getInvoiceDate());
            invoice.setDate(new Date());
            invoice.setStatus(InvoiceStatus.DRAFT);
            setSelectedInvoiceItems(InvoiceHelper.groupingItemsByTaxRate(filteredRequests, ""));
            loadInvoiceDialogData(invoice);
            executeJS("PF('invoiceDialogBillingWV').show();");
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

    public void clearFiltraPanel() {
        setDateFrom(null);
        setDateTo(null);
        setDateFromEvasion(null);
        setDateToEvasion(null);
        setDateExpiration(null);
        setSelectedRequestTypes(null);
        setSelectedServices(null);
        setManagerClientFilterid(null);
        setFiduciaryClientFilterId(null);
        setAggregationFilterId(null);
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

    public String prepareInvoicePdf() throws
            IOException, PersistenceBeanException, IllegalAccessException, InstantiationException {
        Invoice invoice = DaoManager.get(Invoice.class, getNumber());
        String body = invoiceDialogBean.getPdfRequestBody(invoice);
        log.info("Invoice PDF body" + body);
        byte [] fileContent  = PrintPDFHelper.convertToPDF(null, body, null,
                DocumentType.INVOICE);

        if (fileContent != null) {
            String fileName = "FE-" + getNumber() + " "
                    + getSelectedInvoice().getClient().getClientName();
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

    public void downloadInvoicePdf() {
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
                if(!ValidationHelper.isNullOrEmpty(items) && items.size() > 0)
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

                String tempPdf = prepareInvoicePdf();
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
            executeJS(
                    "$('.tab').removeClass('selected');"+
                            "$('#tab2').addClass('selected');"+
                            "$('.hide').hide();"+
                            "$('#content_tab2').show();");
        } catch (Exception e) {
            LogHelper.log(log, e);
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

    public WLGInbox saveMail(Long mailManagerStatus) throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException  {
        String emailsFrom = DaoManager.loadField(WLGServer.class, "login",
                String.class, new Criterion[]{Restrictions.eq("id", Long.parseLong(
                        ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.SENT_SERVER_ID)
                                .getValue()))}).get(0);
        Invoice invoice = DaoManager.get(Invoice.class, getNumber());
        WLGInbox inbox = new WLGInbox();
        if(!ValidationHelper.isNullOrEmpty(invoice.getEmail())) {
            inbox = DaoManager.get(WLGInbox.class, invoice.getEmail().getId());
        }
        inbox.setEmailFrom(emailsFrom);
        inbox.setEmailTo(getEmailTo());
        inbox.setEmailCC(getEmailCC());
        inbox.setEmailSubject(getEmailSubject());
        inbox.setEmailBody(MailHelper.htmlToText(getEmailBodyToEditor()));
        inbox.setEmailBodyHtml(getEmailBodyToEditor());
        inbox.setClient(getSelectedInvoiceClient());
        if(inbox.isNew())
            log.info("setting new mail state to :: "+MailManagerStatuses.findById(mailManagerStatus));
        else
            log.info("setting mail :: "+inbox.getId() + " state to :: "+MailManagerStatuses.findById(mailManagerStatus)
                    + " by user:: "+ UserHolder.getInstance().getCurrentUser().getId());
        inbox.setState(mailManagerStatus);
        inbox.setSendDate(new Date());
        inbox.setReceiveDate(new Date());
        DaoManager.save(inbox, true);
        invoice.setEmail(inbox);
        DaoManager.save(invoice, true);
        //saveFiles(true);
        saveFiles(inbox, true);
        loadInvoiceDialogData(invoice);
        return inbox;
    }

    public void loadDraftEmail() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        Invoice invoice = DaoManager.get(Invoice.class, getNumber());
        if (!ValidationHelper.isNullOrEmpty(invoice) && !ValidationHelper.isNullOrEmpty(invoice.getEmail())) {
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
            if (!ValidationHelper.isNullOrEmpty(invoice) &&
                    !ValidationHelper.isNullOrEmpty(invoice.getEmailFrom())) {
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
                    if(!ValidationHelper.isNullOrEmpty(manager.getEmails()))
                        allEmailList.addAll(manager.getEmails());
                });
                sendTo = new LinkedList<>();
                CollectionUtils.emptyIfNull(allEmailList).stream().forEach(email -> {
                    if(!ValidationHelper.isNullOrEmpty(email.getEmail()))
                        sendTo.addAll(MailHelper.parseMailAddress(email.getEmail()));
                });
//                WLGInbox baseMail = DaoManager.get(WLGInbox.class, invoice.getEmailFrom().getId());
//                String sendDate;
//                if (invoice.getEmailFrom().getSendDate() == null) {
//                    sendDate = "";
//                } else if (baseMail.getReceived()) {
//                    sendDate = DateTimeHelper.toFormatedStringLocal(invoice.getEmailFrom().getSendDate(),
//                            DateTimeHelper.getMySQLDateTimePattern(), null);
//                } else {
//                    sendDate = DateTimeHelper.ToMySqlStringWithSeconds(invoice.getEmailFrom().getSendDate());
//                }
//                StringBuilder html = new StringBuilder();
//
//                String emailPrefix = String.format(MAIL_RERLY_FOOTER,
//                        prepareEmailAddress(invoice.getEmailFrom().getEmailFrom()),
//                        sendDate,
//                        prepareEmailAddress(invoice.getEmailFrom().getEmailTo()),
//                        invoice.getEmailFrom().getEmailCC(),
//                        invoice.getEmailFrom().getEmailSubject());
//                html.append(emailPrefix);
//                html.append(invoice.getEmailFrom().getEmailBody());
//                html.append(invoice.getEmailFrom().getEmailPostfix());
//                setEmailBodyToEditor(html.toString());
            }
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

        try {
            Invoice invoice = DaoManager.get(Invoice.class, getNumber());
            WLGInbox wlgInbox = saveMail(MailManagerStatuses.NEW.getId());
            MailHelper.sendMail(wlgInbox,(addAttachment ? getInvoiceEmailAttachedFiles() : null), null);
            log.info("Mail is sent");
            if (!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom())) {
                wlgInbox.setRecievedInbox(DaoManager.get(WLGInbox.class, invoice.getEmailFrom().getId()));
                DaoManager.save(wlgInbox, true);
            }
            if(!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom()) && !ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getManagers())) {
                wlgInbox.setManagers(new ArrayList<>(invoice.getEmailFrom().getManagers()));
                DaoManager.save(wlgInbox, true);
            }
            wlgInbox.setServerId(Long.parseLong(ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.SENT_SERVER_ID).getValue()));
            DaoManager.save(wlgInbox, true);
            List<Request> selectedRequestList = new ArrayList<>();
            List<Request> requests = DaoManager.load(Request.class, new Criterion[]{
                    Restrictions.eq("invoice", invoice),
                    Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted"))
            });
            if (!ValidationHelper.isNullOrEmpty(requests)) {
                selectedRequestList = requests.stream().filter(r -> r.isSelectedForInvoice())
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
            executeJS("PF('invoiceDialogBillingWV').hide();");
        } catch (Exception e) {
            log.info("Mail is not sent");
            LogHelper.log(log, e);
            executeJS("showNotSendMsg();");
            return;
        }
    }

    public void loadInvoiceDialogDataPayment(Invoice invoice) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setActiveTabIndex(1);
        List<Request> requests = DaoManager.load(Request.class, new Criterion[]{
                Restrictions.eq("invoice", invoice),
                Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted"))
        });
        if(requests != null && !requests.isEmpty()) {
            setActiveTabIndex(2);
        }
        setNumber(invoice.getId());
        loadInvoiceDialogData(invoice);

    }

    public void saveInvoicePayment() throws HibernateException, PersistenceBeanException, IllegalAccessException {
        PaymentInvoice paymentInvoice = new PaymentInvoice();
        paymentInvoice.setPaymentImport(getPaymentAmount());
        paymentInvoice.setDate(getPaymentDate());
        paymentInvoice.setDescription(getPaymentDescription());
        paymentInvoice.setInvoice(getSelectedInvoice());
        DaoManager.save(paymentInvoice, true);
        List<PaymentInvoice> paymentInvoicesList = DaoManager.load(PaymentInvoice.class, new Criterion[] {Restrictions.eq("invoice", getSelectedInvoice())}, new Order[]{
                Order.asc("date")});
        setPaymentInvoices(paymentInvoicesList);
    }

    public void generateXlsWithInvoicesReport() {
        try {
            CreateExcelInvoicesReportHelper helper = new CreateExcelInvoicesReportHelper();
            FileHelper.sendFile("InvoicesReport-" + DateTimeHelper.toStringDateWithDots(new Date()) + ".xls",
                    helper.createInvoiceExcel(DaoManager.load(Invoice.class, getInvoiceTabCriteria().toArray(new Criterion[0]),
                            new Order[]{
                                    Order.desc("number")})));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void attachInvoiceData(Invoice invoice) throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {
        setInvoiceEmailAttachedFiles(new ArrayList<>());
        String refrequest = "";
        String ndg = "";
        // WLGInbox baseMail = DaoManager.get(WLGInbox.class, getBaseMailId());
        if(ValidationHelper.isNullOrEmpty(invoice.getEmailFrom())) {
            invoiceDialogBean.attachCourtesyInvoicePdf(invoice);
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
        invoiceDialogBean.attachCourtesyInvoicePdf(invoice);
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

    public void fillAttachedFiles(WLGInbox inbox) throws PersistenceBeanException, IOException, InstantiationException, IllegalAccessException {
        setInvoiceEmailAttachedFiles(new ArrayList<>());
        List<WLGExport> exportList = DaoManager.load(WLGExport.class, new Criterion[]{
                Restrictions.eq("inbox", inbox)
        });
        for (WLGExport export : exportList) {
            addAttachedFile(export);
        }
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

    private String prepareEmailAddress(String email) {
        return email.replace("<", "&lt;").replace(">", "&gt;");
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

    public String createInvoiceEmailDefaultBody(Invoice invoice) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if(!ValidationHelper.isNullOrEmpty(invoice)) {
            if(ValidationHelper.isNullOrEmpty(invoice.getEmail()) && !ValidationHelper.isNullOrEmpty(invoice.getEmailFrom())) {
                String dearCustomer = ResourcesHelper.getString("dearCustomer");
                String attachedCopyMessage = ResourcesHelper.getString("attachedCopyMessage");
                String thanksMessage = ResourcesHelper.getString("thanksMessage");
                String reference = "";
                String ndg = "";
                if (!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getNdg()))
                    ndg = "NDG: " + invoice.getEmailFrom().getNdg();
                if (!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getReferenceRequest()))
                    reference = "RIF: " +invoice.getEmailFrom().getReferenceRequest() + " ";
                String requestType = "";
                List<Request> requests = DaoManager.load(Request.class, new Criterion[]{
                        Restrictions.eq("invoice", invoice),
                        Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                                Restrictions.isNull("isDeleted"))
                });
                Set<String> requestTypeSet = new HashSet<>();
                requests.stream().forEach(request -> {
                    requestTypeSet.add(request.getRequestType().getName());
                });
                for(String request: requestTypeSet){
                    requestType = requestType + request + " ";
                }
                requestType = "REQUEST: "+requestType;

                String emailBody = dearCustomer + ",</br></br>"
                        + attachedCopyMessage + "</br></br>"
                        + (ndg.isEmpty() ? "" : ndg + "</br>")
                        + (reference.isEmpty() ? "" : reference + "</br>")
                        + (requestType.isEmpty() ? "" : requestType + "</br></br>")
                        + thanksMessage;

                return emailBody;
            } else if(ValidationHelper.isNullOrEmpty(invoice.getEmail())) {
                String dearCustomer = ResourcesHelper.getString("dearCustomer");
                String attachedMessage = ResourcesHelper.getString("attachedMessage");
                String thanksMessage = ResourcesHelper.getString("thanksMessage");

                String emailBody = dearCustomer + ",</br></br>"
                        + attachedMessage + "</br></br>"
                        + thanksMessage;

                return emailBody;
            }
        }
        return "";
    }


    private void openSubjectsToBeInvoicedTab() {
        executeJS("$('.tab').removeClass('selected'); $('#content_tab4').addClass('selected'); $('.hide').hide(); $('#content_tab4').show();");
    }

    public boolean isAddAttachment() {
        return addAttachment;
    }

    public void setAddAttachment(boolean addAttachment) {
        this.addAttachment = addAttachment;
    }
}