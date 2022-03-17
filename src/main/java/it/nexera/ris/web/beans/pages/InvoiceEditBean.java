package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.xml.wrappers.ConservatoriaSelectItem;
import it.nexera.ris.common.xml.wrappers.RequestViewWrapper;
import it.nexera.ris.common.xml.wrappers.RequestWrapper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.base.AccessBean;

import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean(name = "invoiceEditBean")
@ViewScoped
public class InvoiceEditBean extends EntityEditPageBean<Invoice> implements Serializable {

    private static final long serialVersionUID = -669341430296239089L;

    private List<SelectItem> clients;
    
    private Long selectedClientId;
    
    private List<SelectItem> paymentTypes;
    
    private List<SelectItem> vatAmounts;
    
    private Long selectedPaymentTypeId;
    
    private Boolean splitPayment;
    
    private Date date;
    
    private String notes;
   
    private List<InvoiceItem> invoiceItems;
    
    private String invoiceItemSubject;
    
    private String invoiceItemDescription;
    
    private Double invoiceItemAmount;
    
    //private TaxRate invoiceItemVat;
    
    private InvoiceItem selectedItem;
    
    private List<InvoiceItem> scheduledForDeletion;

	private Long selectedTaxRateId;

    private String fiscalCode;

	@Override
	public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException {

		try {
			scheduledForDeletion = new ArrayList<>();

			clients = ComboboxHelper.fillList(Client.class);
			paymentTypes = ComboboxHelper.fillList(new PaymentType[] { });

			setVatAmounts(ComboboxHelper.fillList(TaxRate.class, Order.asc("description"), new CriteriaAlias[]{}, new Criterion[]{
					Restrictions.eq("use", Boolean.TRUE)
			}, true, false));

			selectedClientId = null;
			selectedPaymentTypeId = null;

			if(!getEntity().isNew()) {
				setSplitPayment(getEntity().getSplitPayment());
				setNotes(getEntity().getNotes());
				setDate(getEntity().getDate());
				setFiscalCode(getEntity().getFiscalCode());

				selectedClientId = getEntity().getClient().getId();
				selectedPaymentTypeId = getEntity().getPaymentType().getId();

				invoiceItems = DaoManager.load(InvoiceItem.class,
						new Criterion[]{
								Restrictions.eq("invoice.id", getEntity().getId())
						});

				fillPaymentTypeList();
			} else {
				invoiceItems = new ArrayList<>();
			}
		}

		catch(Exception e) {
			log.error(e);
		}
	}

	public String getFiscalCode() {
		return fiscalCode;
	}

	public void setFiscalCode(String fiscalCode) {
		this.fiscalCode = fiscalCode;
	}

	public List<SelectItem> getClients() {
		return clients;
	}

	public void setClients(List<SelectItem> clients) {
		this.clients = clients;
	}

	public Long getSelectedClientId() {
		return selectedClientId;
	}

	public void setSelectedClientId(Long selectedClientId) {
		this.selectedClientId = selectedClientId;
	}

	public List<SelectItem> getPaymentTypes() {
		return paymentTypes;
	}

	public void setPaymentTypes(List<SelectItem> paymentTypes) {
		this.paymentTypes = paymentTypes;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public List<InvoiceItem> getInvoiceItems() {
		return invoiceItems;
	}

	public void setInvoiceItems(List<InvoiceItem> invoiceItems) {
		this.invoiceItems = invoiceItems;
	}

	public String getInvoiceItemSubject() {
		return invoiceItemSubject;
	}

	public void setInvoiceItemSubject(String invoiceItemSubject) {
		this.invoiceItemSubject = invoiceItemSubject;
	}

	public String getInvoiceItemDescription() {
		return invoiceItemDescription;
	}

	public void setInvoiceItemDescription(String invoiceItemDescription) {
		this.invoiceItemDescription = invoiceItemDescription;
	}

	public Double getInvoiceItemAmount() {
		return invoiceItemAmount;
	}

	public void setInvoiceItemAmount(Double invoiceItemAmount) {
		this.invoiceItemAmount = invoiceItemAmount;
	}
	

	public InvoiceItem getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(InvoiceItem selectedItem) {
		this.selectedItem = selectedItem;
	}
	
	public List<SelectItem> getVatAmounts() {
		return vatAmounts;
	}

	public void setVatAmounts(List<SelectItem> vatAmounts) {
		this.vatAmounts = vatAmounts;
	}

	public Double getTotalAmount() {
		Double totalAmount = 0D;
		
		for(InvoiceItem item : invoiceItems)
			totalAmount += item.getAmount();
		
		return totalAmount;
	}
	
	public Double getTotalVat() {
		Double totalVat = 0D;
		
		for(InvoiceItem item : invoiceItems)
			totalVat += item.getVatAmount();
		
		return totalVat;
	}
	
	public Double getTotalGrossAmount() {
		return getTotalAmount() + getTotalVat();
	}

	public void showNewInvoiceItemDialog() {
		selectedItem = null;
		showEditInvoiceItemDialog();
	}

	public void showEditInvoiceItemDialog() {
		
		try {						
			if (selectedItem != null) {				
				invoiceItemSubject = selectedItem.getSubject();
				invoiceItemDescription = selectedItem.getDescription();
				invoiceItemAmount = selectedItem.getAmount();
				if(!ValidationHelper.isNullOrEmpty(selectedItem.getTaxRate()))
					setSelectedTaxRateId(selectedItem.getTaxRate().getId());
				// invoiceItemVat = selectedItem.getVat();
			} else {
				invoiceItemSubject = "";
				invoiceItemDescription = "";
				invoiceItemAmount = 0D;
				setSelectedTaxRateId(null);
				//  invoiceItemVat = 0D;
			}
		}
		
		catch(Exception e) {
			log.error(e);
		}
	}
	
	public void completeEditInvoiceItemDialog() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
		if (ValidationHelper.isNullOrEmpty(invoiceItemSubject) ||
			ValidationHelper.isNullOrEmpty(invoiceItemDescription) ||
			invoiceItemAmount < 0 ) {
			executeJS("PF('itemErrorDialogWV').show();");
		} else
		{		
			InvoiceItem item = (selectedItem != null) ?
				selectedItem : new InvoiceItem();				
			
			item.setSubject(invoiceItemSubject);
			item.setDescription(invoiceItemDescription);
			item.setAmount(invoiceItemAmount);
			if(!ValidationHelper.isNullOrEmpty(getSelectedTaxRateId())){
				item.setTaxRate(DaoManager.get(TaxRate.class, getSelectedTaxRateId()));
			}

			// item.setVat(invoiceItemVat);
			
			if (selectedItem == null)
				invoiceItems.add(item);
			
			executeJS("PF('itemDialogWV').hide();");
		}
	}
	
	public void deleteInvoiceItem() {
		if (!selectedItem.isNew())
			scheduledForDeletion.add(selectedItem);
		
		invoiceItems.remove(selectedItem);
	}

	@Override
	public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
		if(ValidationHelper.isNullOrEmpty(selectedClientId))
            setValidationFailed(true);
		
		if(ValidationHelper.isNullOrEmpty(selectedPaymentTypeId))
			setValidationFailed(true);
		
		if(ValidationHelper.isNullOrEmpty(date))
			setValidationFailed(true);
		
		if(ValidationHelper.isNullOrEmpty(fiscalCode))
			setValidationFailed(true);
		
		if (getValidationFailed())
			executeJS("PF('invoiceErrorDialogWV').show();");
	}

	@Override
	public void onSave() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		getEntity().setClient(DaoManager.get(Client.class, selectedClientId));
		getEntity().setPaymentType(DaoManager.get(PaymentType.class, selectedPaymentTypeId));
		getEntity().setNotes(notes);
		getEntity().setSplitPayment(splitPayment);
		getEntity().setDate(date);
		getEntity().setFiscalCode(fiscalCode);
		
		DaoManager.save(getEntity());
		
		for(InvoiceItem item : scheduledForDeletion)
			DaoManager.remove(item);
		
		for(InvoiceItem item : invoiceItems) {
			item.setInvoice(getEntity());
			
			DaoManager.save(item);
		}	
    }
	
	private void fillPaymentTypeList() {
		try {
			if (!ValidationHelper.isNullOrEmpty(selectedClientId)) {
				Client client = DaoManager.get(Client.class, selectedClientId);
				
				PaymentType[] paymentTypeList = client.getPaymentTypeList().toArray(new PaymentType[0]);
				
				paymentTypes = ComboboxHelper.fillList(paymentTypeList);
				
				if (!ValidationHelper.isNullOrEmpty(paymentTypeList)) {
					selectedPaymentTypeId = paymentTypeList[0].getId();
				}
				else
					selectedPaymentTypeId = null;
			}
		}
		
		catch(Exception e) {
			log.error(e);
		}	
	}
	
	public void onClientChange() {
		fillPaymentTypeList();
	}

	public Long getSelectedTaxRateId() {
		return selectedTaxRateId;
	}

	public void setSelectedTaxRateId(Long selectedTaxRateId) {
		this.selectedTaxRateId = selectedTaxRateId;
	}
}
