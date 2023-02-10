package it.nexera.ris.web.beans.wrappers;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificationListTurnoverWrapper implements Serializable {

	private static final long serialVersionUID = 3902356488895555463L;

	private Integer month;
	
	private double totalTax;
	
	private double nonTotalTax;
	
	private double totalIva;
	
	private double total;
	
	private String clientName;
	
	

}
