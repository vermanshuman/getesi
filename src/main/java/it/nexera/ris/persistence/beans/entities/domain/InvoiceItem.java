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

	@ManyToOne
	@JoinColumn(name = "tax_rate_id")
	private TaxRate taxRate;

	@Column(name = "total_cost")
	private Double invoiceTotalCost;

	@Transient
	private Double vat;

//	@Transient
//	private String uuid;

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

	public Double getVatAmount() {

		Double vat = 0.0;
		if(!ValidationHelper.isNullOrEmpty(getTaxRate())
				&& !ValidationHelper.isNullOrEmpty(getTaxRate().getPercentage())) {
			vat = getTaxRate().getPercentage().doubleValue();
		}
		return getAmount() * (vat / 100);
	}
	
	public Double getGrossAmount() {
		return getAmount() + getVatAmount();
	}

	public Double getInvoiceTotalCost() {
		return invoiceTotalCost;
	}

	public void setInvoiceTotalCost(Double invoiceTotalCost) {
		this.invoiceTotalCost = invoiceTotalCost;
	}

	public TaxRate getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(TaxRate taxRate) {
		this.taxRate = taxRate;
	}

	public Double getVat() {
		if(!ValidationHelper.isNullOrEmpty(getTaxRate()) && !ValidationHelper.isNullOrEmpty(getTaxRate().getPercentage()))
			return getTaxRate().getPercentage().doubleValue();
		return 0.0;
	}

//	public String getUuid() {
//		return uuid;
//	}
//
//	public void setUuid(String uuid) {
//		this.uuid = uuid;
//	}
}
