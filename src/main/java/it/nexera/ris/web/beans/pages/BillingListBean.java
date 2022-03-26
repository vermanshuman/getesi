package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.RequestState;
import it.nexera.ris.common.enums.VatCollectability;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.persistence.view.RequestView;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.logic.RequestStateWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RequestTypeFilterWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ServiceFilterWrapper;
import it.nexera.ris.web.common.EntityLazyListModel;
import it.nexera.ris.web.common.ListPaginator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    private List<RequestType> selectedRequestTypes;

    private List<RequestTypeFilterWrapper> requestTypeWrappers;

    private List<Service> selectedServices;

    private List<ServiceFilterWrapper> serviceWrappers;

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

    private Long selectedTaxRateId;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {

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
        setPaginator(new ListPaginator(10, 1, 1, 1,
                "DESC", "createDate"));
        setStateWrappers(new ArrayList<>());
        setRequestTypeWrappers(new ArrayList<>());
        setServiceWrappers(new ArrayList<>());

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
        model.setTitle("Accumulation of sales per quarter");
        model.setLegendPosition("ne");
        model.setSeriesColors("DDDDDD60");
        model.setShadow(false);
        model.setExtender("customExtender");
        Axis xAxis = model.getAxis(AxisType.X);
        xAxis.setLabel("");
        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setLabel("Sales");
        yAxis.setMin(0);
        yAxis.setMax(200);
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
        List<Criterion> restrictions = new ArrayList<>();
        List<Criterion> restrictionsLike = new ArrayList<>();

        if (restrictionsLike.size() > 0) {
            if (restrictionsLike.size() > 1) {
                restrictions.add(Restrictions.or(restrictionsLike.toArray(new Criterion[restrictionsLike.size()])));
            } else {
                restrictions.add(restrictionsLike.get(0));
            }
        }
        restrictions.add(Restrictions.eq("stateId", RequestState.EVADED.getId()));


        this.setLazySubjectModel(new EntityLazyListModel<>(RequestView.class, restrictions.toArray(new Criterion[0]),
                new Order[]{
                        Order.desc("createDate")
                }));

        getLazySubjectModel().load((getPaginator().getTablePage() - 1) * getPaginator().getRowsPerPage(), getPaginator().getRowsPerPage(),
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
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_VIEW, getEntityEditId());
    }

    public void prepareToModify() {
        setShowPrintButton(true);
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
        setMaxInvoiceNumber();
        docTypes = new ArrayList<>();
        docTypes.add(new SelectItem("FE", "FATTURA"));
        setDocumentType("FE");
        competence = new Date();
        setVatCollectabilityList(ComboboxHelper.fillList(VatCollectability.class,
                false, false));
        paymentTypes = ComboboxHelper.fillList(PaymentType.class);
        setInvoiceTotalCost(CollectionUtils.emptyIfNull(getFilteredRequest())
                .stream()
                .filter(r -> !ValidationHelper.isNullOrEmpty(r.getTotalCost()))
                .mapToDouble(r -> Double.parseDouble(r.getTotalCostDouble())).sum());
        ums = new ArrayList<>();
        ums.add(new SelectItem("pz", "pz"));

        vatAmounts = new ArrayList<>();
        vatAmounts.add(new SelectItem(0D, "0%"));
        vatAmounts.add(new SelectItem(4D, "4%"));
        vatAmounts.add(new SelectItem(10D, "10%"));
        vatAmounts.add(new SelectItem(22D, "22%"));
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
        return totalVat;
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
}