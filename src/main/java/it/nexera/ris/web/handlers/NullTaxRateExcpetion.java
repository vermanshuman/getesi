package it.nexera.ris.web.handlers;

public class NullTaxRateExcpetion extends RuntimeException {

	private static final long serialVersionUID = 8218312694688535741L;
	
	public NullTaxRateExcpetion(String errorMessage) {
        super(errorMessage);
    }

}
