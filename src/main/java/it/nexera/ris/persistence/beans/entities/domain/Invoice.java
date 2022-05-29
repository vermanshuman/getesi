package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.InvoiceStatus;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

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
}
