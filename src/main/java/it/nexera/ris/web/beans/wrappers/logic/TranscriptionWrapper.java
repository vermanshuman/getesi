package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TranscriptionWrapper implements Serializable {

	private static final long serialVersionUID = 3680506611372648254L;
	
	private String costType;
	
	private BigDecimal f24;
	
	private BigDecimal rights;
	
	private BigDecimal expenses;
	
	private BigDecimal stamps;
	
	private BigDecimal fees;
	
	private Integer tempId;

	private Boolean transcriptionTab;

}
