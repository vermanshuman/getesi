package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.TranscriptionData;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "import_f24_pdf")
@Getter
@Setter
public class ImportF24Pdf extends IndexedEntity {

	private static final long serialVersionUID = -2391147925796763119L;
	
	@Column(name = "code")
	private String code;
	
	@Column(name = "import")
	private Double f24Import;
	
	@Column(name = "f24_identification_number")
	private String f24IdentificationNumber;

	@Column(name = "reference_year")
	private Integer referenceYear;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transcription_data_id")
	private TranscriptionData transcriptionData;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "percentage")
	private Double percentage;

	@Transient
	private Long importF24Id;

	@Column(name = "import_f24_id")
	private Long idImportF24;

	@Column(name = "manual_f24")
	private Boolean manualF24;
}
