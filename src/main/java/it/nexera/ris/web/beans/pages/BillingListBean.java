package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.view.RequestView;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean(name = "billingListBean")
@ViewScoped
@Getter
@Setter
public class BillingListBean extends EntityLazyListPageBean<RequestView>
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

    private double getRandomNumber(int min, int max) {
        return Math.random()*(max-min+1)+min;
    }
}