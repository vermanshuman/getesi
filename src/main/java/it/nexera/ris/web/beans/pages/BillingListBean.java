package it.nexera.ris.web.beans.pages;

import it.nexera.ris.api.FatturaAPI;
import it.nexera.ris.api.FatturaAPIResponse;
import it.nexera.ris.common.enums.InvoiceStatus;
import it.nexera.ris.common.enums.VatCollectability;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.logic.FileWrapper;
import it.nexera.ris.web.beans.wrappers.logic.GoodsServicesFieldWrapper;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.persistence.view.RequestView;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
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

    //private MenuModel topMenuModel;

    //private int activeMenuTabNum;

    private List<InputCard> inputCardList;

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

    private String invoiceNote;

    private String apiError;

    private boolean sendInvoice;

    private Boolean billinRequest;

    private String documentType;

    List<RequestView> filteredRequest;

    String invoiceErrorMessage;

    private boolean invoiceSentStatus;

    private Long selectedTaxRateId;
    
    private List<Request> invoicedRequests;
    
    private List<FileWrapper> invoiceEmailAttachedFiles;
    
    private boolean printPdf;
    
    private String mailPdf;
    
    private List<GoodsServicesFieldWrapper> goodsServicesFields;
    
    private List<SelectItem> invoiceClients;

    private Long selectedInvoiceClientId;
    
    private Client selectedInvoiceClient;
    

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
        
        setActiveTabIndex(0);
        
    }

    private void fillYears() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        List<Invoice> invoices = DaoManager.load(Invoice.class, new Criterion[] {Restrictions.or(Restrictions.isNotNull("date"))});
        Set<Integer> tempInvoices = new HashSet<>();
        for(Invoice invoice : invoices) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(invoice.getDate());
            int year = calendar.get(Calendar.YEAR);
            tempInvoices.add(year);
        }
        List<SelectItem> yearList = new ArrayList<SelectItem>();
        for(Integer year : tempInvoices) {
            yearList.add(new SelectItem(year));
        }
        setYears(yearList);
    }

    public void setQuadrimesterIdx(int startIdx, int endIdx) {
        quadrimesterStartIdx = startIdx;
        quadrimesterEndIdx = endIdx;
    }

    private double getRandomNumber(int min, int max) {
        return Math.random()*(max-min+1)+min;
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
        ChartSeries q1 = new ChartSeries();
        q1.setLabel("Q1");
        q1.set("1", 120);
        q1.set("2", 100);
        q1.set("3", 44);
        ChartSeries q2 = new ChartSeries();
        q2.setLabel("Q2");
        q2.set("4", 120);
        q2.set("5", 44);
        q2.set("6", 100);
        ChartSeries q3 = new ChartSeries();
        q3.setLabel("Q3");
        q3.set("7", 80);
        q3.set("8", 44);
        q3.set("9", 120);
        ChartSeries q4 = new ChartSeries();
        q4.setLabel("Q4");
        q4.set("10", 44);
        q4.set("12", 100);
        q4.set("12", 80);
        model.addSeries(q1);
        model.addSeries(q2);
        model.addSeries(q3);
        model.addSeries(q4);
        model.setTitle("Indice di redditività");
        model.setLegendPosition("ne");
        model.setSeriesColors("11773340");
        model.setShadow(false);
        Axis xAxis = model.getAxis(AxisType.X);
        xAxis.setLabel("");
        Axis yAxis = model.getAxis(AxisType.Y);
        //yAxis.setLabel("Sales");
        yAxis.setMin(0);
        yAxis.setMax(160);
        yAxis.setTickInterval("20.000");
        yAxis.setTickFormat("%'.3f");
    }

    public void filterTableFromPanel() throws IllegalAccessException, PersistenceBeanException {
        List<Criterion> restrictions = new ArrayList<>();
        List<Criterion> restrictionsLike = new ArrayList<>();

        if(!ValidationHelper.isNullOrEmpty(getFilterInvoiceNumber())) {
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
            restrictions.add(Restrictions.eq("managerId",  getManagerClientFilterid()));restrictions.add(Restrictions.eq("manager.id",  getManagerClientFilterid()));
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

        if(restrictionsLike.size() > 0) {
            if(restrictionsLike.size() > 1) {
                restrictions.add(Restrictions.or(restrictionsLike.toArray(new Criterion[restrictionsLike.size()])));
            }else {
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

    public void reset() throws NumberFormatException, HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException, IOException  {
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

    private void loadCompanies(List<Client> clients) throws HibernateException, IllegalAccessException, PersistenceBeanException {
        List<SelectItem> companies = new ArrayList<SelectItem>();
        for(Client client: clients) {
            if(client.getNameOfTheCompany() != null && !client.getNameOfTheCompany().isEmpty())
                companies.add(new SelectItem(client.getId(), client.getNameOfTheCompany()));
            else
                companies.add(new SelectItem(client.getId(), client.getNameProfessional()));
        }

        setCompanies(companies.stream()
                .sorted(Comparator.comparing(SelectItem::getLabel))
                .collect(Collectors.toList()));
    }
    
    public final void onTabChange(final TabChangeEvent event) {
        TabView tv = (TabView) event.getComponent();
        this.activeTabIndex = tv.getActiveIndex();
        //SessionHelper.put("activeTabIndex", activeTabIndex);
    }
    
    public void loadInvoiceDialogData() throws IllegalAccessException, PersistenceBeanException, HibernateException, InstantiationException  {
    	List<PaymentInvoice> paymentInvoicesList = DaoManager.load(PaymentInvoice.class, new Criterion[] {Restrictions.isNotNull("date")}, new Order[]{
                Order.desc("date")});
    	setPaymentInvoices(paymentInvoicesList);
    	double totalImport = 0.0;
    	for(PaymentInvoice paymentInvoice : paymentInvoicesList) {
    		totalImport = totalImport + paymentInvoice.getPaymentImport().doubleValue();
    	}
    	
    	docTypes = new ArrayList<>();
        docTypes.add(new SelectItem("FE", "FATTURA"));
        setDocumentType("FE");
        competence = new Date();
        setVatCollectabilityList(ComboboxHelper.fillList(VatCollectability.class,
                false, false));
        paymentTypes = ComboboxHelper.fillList(PaymentType.class);
        /*setInvoiceTotalCost(CollectionUtils.emptyIfNull(getFilteredRequest())
                .stream()
                .filter(r -> !ValidationHelper.isNullOrEmpty(r.getTotalCost()))
                .mapToDouble(r -> Double.parseDouble(r.getTotalCostDouble())).sum());*/
        /*ums = new ArrayList<>();
        ums.add(new SelectItem("pz", "pz"));

        vatAmounts = new ArrayList<>();
        vatAmounts.add(new SelectItem(0D, "0%"));
        vatAmounts.add(new SelectItem(4D, "4%"));
        vatAmounts.add(new SelectItem(10D, "10%"));
        vatAmounts.add(new SelectItem(22D, "22%"));*/
        
        
        List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"))});
        setInvoiceClients(ComboboxHelper.fillList(clients.stream()
                .filter(c -> (
                                (ValidationHelper.isNullOrEmpty(c.getManager()) || !c.getManager()) &&
                                        (ValidationHelper.isNullOrEmpty(c.getFiduciary()) || !c.getFiduciary())
                        )
                ).sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList()), Boolean.TRUE));
        
        Invoice invoice = DaoManager.get(Invoice.class, getNumber());
        if(!ValidationHelper.isNullOrEmpty(invoice)) {
        	setInvoiceDate(invoice.getDate());
        	setSelectedInvoiceClientId(invoice.getClient().getId());
        	setSelectedPaymentTypeId(invoice.getPaymentType().getId());
        	setVatCollectabilityId(invoice.getVatCollectability().getId());
        	setInvoiceNote(invoice.getNotes());
        	List<GoodsServicesFieldWrapper> wrapperList = new ArrayList<>();
        	List<InvoiceItem> invoiceItemsDb = DaoManager.load(InvoiceItem.class, new Criterion[]{Restrictions.eq("invoice", invoice)});
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
            	wrapperList.add(wrapper);
            }
            setGoodsServicesFields(wrapperList);
            setSameInvoiceNumber(invoice.getId());
            if(invoice.getStatus().equals(InvoiceStatus.DELIVERED)) {
            	setInvoiceSentStatus(true);
            }
        } else {
        	setGoodsServicesFields(new ArrayList<>());
            getGoodsServicesFields().add(createGoodsServicesFieldWrapper());
            setMaxInvoiceNumber();
        }
        
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
    
    /*public Double getTotalVat() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
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
    } */
    
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
    
   /* public Double getTotalGrossAmount() throws PersistenceBeanException, InstantiationException, IllegalAccessException {

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
    
    public Double getAllTotalLine() {
    	Double total = 0D;
    	if(!ValidationHelper.isNullOrEmpty(getGoodsServicesFields()))
    		total = getGoodsServicesFields().stream().collect(
				Collectors.summingDouble(GoodsServicesFieldWrapper::getTotalLine));
    	return total;
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
    
    public void onItemSelectInvoiceClient() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
    	setSelectedInvoiceClient(DaoManager.get(Client.class, getSelectedInvoiceClientId()));
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
        invoice.setClient(getSelectedInvoiceClient());
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
            }
        }catch(Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
            executeJS("PF('sendInvoiceErrorDialogWV').show();");
        }
    }

}