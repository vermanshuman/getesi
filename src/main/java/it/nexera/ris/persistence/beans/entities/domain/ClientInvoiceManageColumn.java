package it.nexera.ris.persistence.beans.entities.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import it.nexera.ris.common.enums.BillingTypeFields;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;

@Entity
@Table(name = "client_invoice_column")
public class ClientInvoiceManageColumn extends IndexedEntity {

	private static final long serialVersionUID = 2823835467292352940L;
	

	private Integer position;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "billing_type_field")
    @Enumerated(EnumType.STRING)
    private BillingTypeFields field;
    
    @ManyToOne
    @JoinColumn(name = "request_type_id")
    private RequestType requestType;


	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public BillingTypeFields getField() {
		return field;
	}

	public void setField(BillingTypeFields field) {
		this.field = field;
	}

	@Override
	public String toString() {
		return this.getId() != null ? this.getStrId() : this.getField().toString();
	}

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }
}
