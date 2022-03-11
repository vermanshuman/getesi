package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.InvoiceStatus;
import it.nexera.ris.common.enums.VatCollectability;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.PaymentInvoice;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;


import javax.persistence.*;

import org.hibernate.criterion.Restrictions;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "invoice")
public class Invoice extends IndexedEntity implements Serializable {

	private static final long serialVersionUID = -203805995863279495L;
	
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;	
	
	@Column(name = "number")
    private Long number;	
	
	@Column(name = "date")
    private Date date;	
	
	@Column(name = "causal")
    private String notes;	
	
    @ManyToOne
    @JoinColumn(name = "payment_type_id")
    private PaymentType paymentType;		
	
    @Column(name = "split_payment")
    private Boolean splitPayment;

    @Column(name = "sent")
    private Boolean sent;
    
    @Column(name = "fiscalCode")
    private String fiscalCode;

    @Column(name = "cloud_id")
    private Long cloudId;

	@Column(name = "vat_collectability")
	private VatCollectability vatCollectability;

	@Column(name = "invoice_number")
	private String invoiceNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private InvoiceStatus status;

	@ManyToOne
	@JoinColumn(name = "manager_id")
	private Client manager;

	@ManyToOne
	@JoinColumn(name = "office_id")
	private Office office;

	@Column(name = "ndg")
	private String ndg;

	@Column(name = "pratica")
	private String practice;

	@ManyToOne
	@JoinColumn(name = "mail_id")
	private WLGInbox email;

	@Transient
	private String documentType;
	
	@Transient
	private Double totalAmount;
	
	@Transient
	private Double onBalance;
	
	@Transient
	private String dateString;

	public Long getCloudId() {
		return cloudId;
	}

	public void setCloudId(Long cloudId) {
		this.cloudId = cloudId;
	}

	public String getFiscalCode() {
		return fiscalCode;
	}

	public void setFiscalCode(String fiscalCode) {
		this.fiscalCode = fiscalCode;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public Long getNumber() {
		return number;
	}

	public void setNumber(Long number) {
		this.number = number;
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

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
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

	public VatCollectability getVatCollectability() {
		return vatCollectability;
	}

	public void setVatCollectability(VatCollectability vatCollectability) {
		this.vatCollectability = vatCollectability;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public InvoiceStatus getStatus() {
		return status;
	}

	public void setStatus(InvoiceStatus status) {
		this.status = status;
	}

	public Client getManager() {
		return manager;
	}

	public void setManager(Client manager) {
		this.manager = manager;
	}

	public Office getOffice() {
		return office;
	}

	public void setOffice(Office office) {
		this.office = office;
	}

	public String getNdg() {
		return ndg;
	}

	public void setNdg(String ndg) {
		this.ndg = ndg;
	}

	public String getPractice() {
		return practice;
	}

	public void setPractice(String practice) {
		this.practice = practice;
	}

	public WLGInbox getEmail() {
		return email;
	}

	public void setEmail(WLGInbox email) {
		this.email = email;
	}
	
	public Double getTotalAmount() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<InvoiceItem> invoiceItems = DaoManager.load(InvoiceItem.class, Restrictions.eq("invoice.id", this.getId()));
        double totalAmount = 0.0;
        for(InvoiceItem invoiceItem : invoiceItems) {
        	Double total = invoiceItem.getAmount().doubleValue() + invoiceItem.getVatAmount().doubleValue();
        	totalAmount = totalAmount + total;
        }
        return totalAmount;
    }
	
	public Double getOnBalance() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<PaymentInvoice> paymentInvoices = DaoManager.load(PaymentInvoice.class, Restrictions.eq("invoice.id", this.getId()));
        double paymentImportTotal = 0.0;
        for(PaymentInvoice invoiceItem : paymentInvoices) {
        	Double total = invoiceItem.getPaymentImport().doubleValue();
        	paymentImportTotal = paymentImportTotal + total;
        }
        Double onBalance = getTotalAmount().doubleValue() - paymentImportTotal;
        return onBalance;
    }

	public String getDateString() {
		if(getDate() != null) {
			return DateTimeHelper.toFormatedStringLocal(getDate(),
                    DateTimeHelper.getDatePattern(), null);
		}
		return dateString;
	}

	
	
	
}
