package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;

import it.nexera.ris.common.enums.BillingTypeFields;
import it.nexera.ris.persistence.beans.entities.domain.ReportColumn;

public class ReportColumnWrapper implements Serializable{

	private static final long serialVersionUID = -1142940260780779304L;

	private ReportColumn field;

    private Boolean selected;

    public ReportColumnWrapper() {
	}
    
    public ReportColumnWrapper(ReportColumn field) {
        this.selected = true;
        this.field = field;
    }

    public ReportColumnWrapper(BillingTypeFields field) {
    	ReportColumn reportField = new ReportColumn();
    	reportField.setField(field);
        this.field = reportField;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

	public ReportColumn getField() {
		return field;
	}

	public void setField(ReportColumn field) {
		this.field = field;
	}
	
	@Override
	public String toString() {
		return getField().getField().toString();
	}
}
