package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;

import it.nexera.ris.common.enums.BillingTypeFields;
import it.nexera.ris.persistence.beans.entities.domain.ClientInvoiceManageColumn;

public class InvoiceColumnWrapper implements Serializable{

	private static final long serialVersionUID = -8125554174267786336L;

	private ClientInvoiceManageColumn field;

    private Boolean selected;

    public InvoiceColumnWrapper() {
	}
    
    public InvoiceColumnWrapper(ClientInvoiceManageColumn field) {
        this.selected = true;
        this.field = field;
    }

    public InvoiceColumnWrapper(BillingTypeFields field) {
        ClientInvoiceManageColumn invoiceField = new ClientInvoiceManageColumn();
        invoiceField.setField(field);
        this.field = invoiceField;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

	public ClientInvoiceManageColumn getField() {
		return field;
	}

	public void setField(ClientInvoiceManageColumn field) {
		this.field = field;
	}
	
	@Override
	public String toString() {
		return getField().getField().toString();
	}
}
