package it.nexera.ris.web.beans.wrappers;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingListTurnoverWrapper implements Serializable {

	private static final long serialVersionUID = -8013619826679821239L;
	
	private Integer month;
	
	private double totalTax;
	
	private double nonTotalTax;
	
	private double totalIva;
	
	private double total;
	
	private String clientName;
	
	

}
