package it.nexera.ris.web.exceltemplate;

import java.util.List;

import it.nexera.ris.web.beans.wrappers.logic.TranscriptionWrapper;
import lombok.Data;

@Data
public class CertificationExpense {

	private String billingCustomer;
	private String fatturaN;
	private String data;
	private String office;
	private String manager;
	private String trust;
	private String ndg;
	private String reportn;
	private String referenceRequest;
	private String requestTitle;
    
    private List<Cost> costs;
    private String totalRights;
    private String totalExpenses;
    //private String totalStamp;
    private String totalFees;
    
    
}
