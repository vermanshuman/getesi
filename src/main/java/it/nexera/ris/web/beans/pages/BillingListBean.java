package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
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

    public String[] months = new String[]{"Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"};

    private int quadrimesterStartIdx = 0;

    private int quadrimesterEndIdx = 3;

    private List<String> turnoverPerCustomer = new ArrayList<>();
    
    private List<Invoice> invoices;
    
    private Long filterInvoiceNumber;
    
    private Date dateFrom;

    private Date dateTo;
    
    private String filterAll;
    
    private List<SelectItem> managerClients;

    private Long managerClientFilterid;
    
    private List<SelectItem> landAggregations;

    private Long aggregationFilterId;
    
    private String filterNotes;
    
    private String filterNdg;
    
    private String filterPractice;
    
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {

        setClients(ComboboxHelper.fillList(DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"))
        }).stream()
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
        
        setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.TRUE));
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
    
    public void filterTableFromPanel() throws IllegalAccessException, PersistenceBeanException {
    	List<Criterion> restrictions = new ArrayList<>();
    	List<Criterion> restrictionsLike = new ArrayList<>();
    	
    	if(!ValidationHelper.isNullOrEmpty(getFilterInvoiceNumber())) {
            Criterion r = Restrictions.eq("number", getFilterInvoiceNumber());
            restrictionsLike.add(r);
        }
    	
    	if(!ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
            Criterion r = Restrictions.eq("client.id", getSelectedClientId());
            restrictionsLike.add(r);
        }
    	
    	if (!ValidationHelper.isNullOrEmpty(getDateFrom())) {
            restrictions.add(Restrictions.ge("date", DateTimeHelper.getDayStart(getDateFrom())));
        }

        if (!ValidationHelper.isNullOrEmpty(getDateTo())) {
            restrictions.add(Restrictions.le("date", DateTimeHelper.getDayEnd(getDateTo())));
        }
        
        if (!ValidationHelper.isNullOrEmpty(getManagerClientFilterid())) {
            restrictions.add(Restrictions.eq("managerId",  getManagerClientFilterid()));
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
    
    public void clearFiltraPanel() {
        setDateFrom(null);
        setDateTo(null);
        setSelectedClientId(null);
        setManagerClientFilterid(null);
        setAggregationFilterId(null);
        setFilterNotes(null);
        setFilterNdg(null);
        setFilterPractice(null);
        setFilterAll(null);
    }
    
    public void reset() throws NumberFormatException, HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException, IOException  {
    	setDateFrom(null);
        setDateTo(null);
    	setSelectedClientId(null);
        setManagerClientFilterid(null);
        setAggregationFilterId(null);
        setFilterNotes(null);
        setFilterNdg(null);
        setFilterPractice(null);
        this.onLoad();
    }
}