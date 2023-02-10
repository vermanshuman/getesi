package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import it.nexera.ris.persistence.beans.entities.domain.Request;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExcelTableTranscriptionWrapper implements Serializable {

	private static final long serialVersionUID = 2829398641405374950L;
	
	private String title;
	
	private List<Request> requests;
	
	private List<String> columnNames;
	
	private Map<String, String> columnValues;
	
	private Map<String, String> footerValues;
	
	private List<TranscriptionWrapper> transcriptionWrappers;
	
	private Long cancelRequestId;

}
