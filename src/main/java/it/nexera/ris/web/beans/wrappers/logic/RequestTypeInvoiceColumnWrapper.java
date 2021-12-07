package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;

import org.primefaces.model.DualListModel;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;

public class RequestTypeInvoiceColumnWrapper implements Serializable{

	private static final long serialVersionUID = -8125554174267786336L;

	private DualListModel<InvoiceColumnWrapper> invoiceColumns;

    private RequestType requestType;

    public RequestTypeInvoiceColumnWrapper() {
	}
    
    public RequestTypeInvoiceColumnWrapper(DualListModel<InvoiceColumnWrapper> invoiceColumns, 
            RequestType requestType) {
        this.invoiceColumns = invoiceColumns;
        this.requestType = requestType;
    }

	
	@Override
	public String toString() {
		return getRequestType().toString();
	}

    public DualListModel<InvoiceColumnWrapper> getInvoiceColumns() {
        return invoiceColumns;
    }

    public void setInvoiceColumns(DualListModel<InvoiceColumnWrapper> invoiceColumns) {
        this.invoiceColumns = invoiceColumns;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }
}
