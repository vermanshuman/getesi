package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.Invoice;
import it.nexera.ris.persistence.beans.entities.domain.InvoiceItem;
import it.nexera.ris.persistence.beans.entities.domain.PaymentType;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.cloud.FattureInCloud;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ManagedBean(name = "invoiceListBean")
@ViewScoped
public class InvoiceListBean extends EntityLazyListPageBean<Invoice>
        implements Serializable {

    private static final long serialVersionUID = -5590180358388236956L;

    private List<SelectItem> clients;
    private List<SelectItem> paymentTypes;
    
    private Date dateFrom;
    private Date dateTo;
    private Long selectedClientId;
    private Long selectedPaymentTypeId;
    private Boolean splitPayment;
    private Boolean sent;
	private String apiError;

	public List<SelectItem> getClients() {
		return clients;
	}

	public void setClients(List<SelectItem> clients) {
		this.clients = clients;
	}

	public List<SelectItem> getPaymentTypes() {
		return paymentTypes;
	}

	public void setPaymentTypes(List<SelectItem> paymentTypes) {
		this.paymentTypes = paymentTypes;
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

	public Long getSelectedPaymentTypeId() {
		return selectedPaymentTypeId;
	}

	public void setSelectedPaymentTypeId(Long selectedPaymentTypeId) {
		this.selectedPaymentTypeId = selectedPaymentTypeId;
	}

	public Boolean getSplitPayment() {
		return splitPayment;
	}

	public void setSplitPayment(Boolean splitPayment) {
		this.splitPayment = splitPayment;
	}

	public Boolean getSent() {
		return sent;
	}

	public void setSent(Boolean sent) {
		this.sent = sent;
	}

	public String getApiError() {
		return apiError;
	}

	public void setApiError(String apiError) {
		this.apiError = apiError;
	}

	/* (non-Javadoc)
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
    	setClients(ComboboxHelper.fillList(Client.class));
    	paymentTypes = ComboboxHelper.fillList(PaymentType.class);
    	
        this.loadList(Invoice.class, new Order[]{ });
    }

    public boolean getCanDelete(Invoice invoice){
    	return true;
    }
    
    public void createNewInvoice() {
        RedirectHelper.goTo(PageTypes.INVOICE_EDIT);
    }
    
    public void manageRequest() {
        RedirectHelper.goTo(PageTypes.INVOICE_EDIT, getEntityEditId());
    }
    
    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(dateFrom)) {
            restrictions.add(Restrictions.ge("createDate",
                    DateTimeHelper.getDayStart(dateFrom)));
        }

        if (!ValidationHelper.isNullOrEmpty(dateTo)) {
            restrictions.add(Restrictions.le("createDate",
                    DateTimeHelper.getDayEnd(dateTo)));
        }
        
        if (!ValidationHelper.isNullOrEmpty(selectedClientId)) {
            restrictions.add(Restrictions.eq("client.id",
                    selectedClientId));
        }
        
        if (!ValidationHelper.isNullOrEmpty(selectedPaymentTypeId)) {
            restrictions.add(Restrictions.eq("paymentType.id",
                    selectedPaymentTypeId));
        }
        
        if (!ValidationHelper.isNullOrEmpty(splitPayment)) {
            restrictions.add(Restrictions.eq("splitPayment",
                    splitPayment));
        }
        
        if (!ValidationHelper.isNullOrEmpty(sent)) {
            restrictions.add(Restrictions.eq("sent",
                    sent));
        }


        loadList(Invoice.class, restrictions.toArray(new Criterion[0]),
                new Order[]{Order.desc("createDate")});
    }
    
    public void sendInvoice() {
    	
    	try {
    		//log.info("SENDING INVOICE ID " + getEntityEditId());
    	
    		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    		
    		FattureInCloud cloud = new FattureInCloud("506840" 
    				, "e43c9ff59b726d6c81218559164376f1");
    	
    		Invoice invoice = DaoManager.get(Invoice.class, getEntityEditId());
    	
    		JSONObject invoiceObj = new JSONObject();
    		
    		invoiceObj.put("nome", invoice.getClient().toString());
    		invoiceObj.put("data", dateFormat.format(invoice.getDate()));
    		invoiceObj.put("note", invoice.getNotes());
    		invoiceObj.put("split_payment", invoice.getSplitPayment());
    		
        	JSONArray articleArray = new JSONArray();
        	
        	List<InvoiceItem> items =     			
        			 DaoManager.load(InvoiceItem.class, 
        					new Criterion[]{
        							Restrictions.eq("invoice.id", getEntityEditId())
        					});
        	
        	for(InvoiceItem item : items) {
        		JSONObject article = new JSONObject();
        		
        		article.put("nome", item.getSubject());
        		article.put("descrizione", item.getDescription());
        		article.put("prezzo_lordo", item.getGrossAmount());
        		article.put("prezzo_netto", item.getAmount());
				if(!ValidationHelper.isNullOrEmpty(item.getTaxRate()) && !ValidationHelper.isNullOrEmpty(item.getTaxRate().getPercentage()))
					article.put("cod_iva", item.getTaxRate().getPercentage());
        		
        		articleArray.put(article);
        	}
        	
        	invoiceObj.put("lista_articoli", articleArray);
        	
        	JSONArray paymentArray = new JSONArray();
        	
        	JSONObject payment = new JSONObject();
        	
        	payment.put("data_scadenza", dateFormat.format(invoice.getDate()));
        	payment.put("importo", "auto");
        	payment.put("metodo", invoice.getPaymentType().toString());
        	payment.put("data_saldo", dateFormat.format(invoice.getDate()));
        	
        	paymentArray.put(payment);
        	
        	invoiceObj.put("lista_pagamenti", paymentArray);
        	
        	//log.info("INVOICEOBJ =" + invoiceObj);
        	
    		if(invoice.getSent() == null || !invoice.getSent()) {
    			JSONObject response = cloud.newInvoice(invoiceObj);
    		
    			//log.info("NEW INVOICE RESPONSE = " + response);
    			
    			if (FattureInCloud.wasSuccessful(response)) {
    				invoice.setCloudId(response.getLong("new_id"));
        			invoice.setSent(true);
        			DaoManager.save(invoice, true);
        			executeJS("PF('sendInvoiceOkDialogWV').show();");
    			} else 
    				executeJS("PF('sendInvoiceErrorDialogWV').show();");
    			
    		} else {
    			invoiceObj.put("id", invoice.getCloudId());
    			
    			JSONObject response = cloud.editInvoice(invoiceObj);
    			
    			//log.info("EDIT INVOICE RESPONSE = " + response);
    			
    			if(!FattureInCloud.wasSuccessful(response))
    				executeJS("PF('editInvoiceEditErrorDialogWV').show();");
    			else
        			executeJS("PF('editInvoiceEditOkDialogWV').show();");
    			
    		}
    		
    	}
    	
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    }
    
    @Override
    public void deleteEntity() throws HibernateException,
    PersistenceBeanException, InstantiationException,
    IllegalAccessException, IOException {
    	Long id = this.getEntityDeleteId();
    	
    	if (id == null)
    		return;
    		
		FattureInCloud cloud = new FattureInCloud("506840" 
				, "e43c9ff59b726d6c81218559164376f1");
	
    	Invoice invoice = DaoManager.get(Invoice.class, id);
    	
    	if (invoice.getSent() != null && invoice.getSent()) {
    		JSONObject object = new JSONObject();
    		
    		object.put("id", invoice.getCloudId());
    		
    		JSONObject response = cloud.removeInvoice(object);
    		
    		log.info("REMOVE INVOICE RESPONSE = " + response);
    		
			if (FattureInCloud.wasSuccessful(response))
    			executeJS("PF('removeInvoiceOkDialogWV').show();");
			else {
				executeJS("PF('removeInvoiceErrorDialogWV').show();");
				return;
			}
    	}
    	
    	List<InvoiceItem> items =     			
   			 DaoManager.load(InvoiceItem.class, 
   					new Criterion[]{
   							Restrictions.eq("invoice.id", id)
   					});
    	
    	for(InvoiceItem item : items)
    		DaoManager.remove(item, true);
    	
    	super.deleteEntity();
    }
}
