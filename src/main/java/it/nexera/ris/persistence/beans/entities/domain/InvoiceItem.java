package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "invoice_item")
public class InvoiceItem extends IndexedEntity implements Serializable {
	private static final long serialVersionUID = -4269406413536164945L;

	@ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;	
	
	@Column(name = "subject")
    private String subject;	
	
	@Column(name = "description")
    private String description;
	
	@Column(name = "amount")
    private Double amount;
	
	@Column(name = "vat")
	private Double vat;

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Double getVat() {
		return vat;
	}

	public void setVat(Double vat) {
		this.vat = vat;
	}	

	public Double getVatAmount() {
		if(!ValidationHelper.isNullOrEmpty(getAmount()) && !ValidationHelper.isNullOrEmpty(getVat()) &&
				getVat() > 0){
			return getAmount() * (getVat() / 100);
		}
		return 0.0D;
	}
	
	public Double getGrossAmount() {
		return getAmount() + getVatAmount();
	}
}
