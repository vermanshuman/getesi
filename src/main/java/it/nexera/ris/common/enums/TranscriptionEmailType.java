package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum TranscriptionEmailType {
	NOTE("note"),
	DUPLO("duplo"),
	CERTIFICATION("certification"),
	NOTARY_CERTIFICATION("notary_certification"),
	CLIENT_EMAIL("client_email"),
	COURIER_CLIENT_EMAIL("courier_client_email");
	
	private String type;       

    private TranscriptionEmailType(String s) {
    	type = s;
    }

	public String getType() {
		return type;
	}

	public String toString() {
		return type;
	}
}
