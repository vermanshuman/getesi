package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import javax.persistence.*;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "import_f24")
@Getter
@Setter
public class ImportF24 extends IndexedEntity {

	private static final long serialVersionUID = 1327290158375950259L;

	@ManyToOne
    @JoinColumn(name = "type_formality_id")
    private TypeFormality typeFormaility;
	
	@Column(name = "code")
	private String code;
	
	@Column(name = "import")
	private Double f24Import;
	
	@Column(name = "include_number")
	private Boolean includeNumber;

	@OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "id_import_f24_pdf")
	private ImportF24Pdf importF24Pdf;
	
	@Column(name = "type")
	private String type;

	@Column(name = "penalty")
	private Boolean penalty;
	
	@Column(name = "percentage")
	private Double percentage;
}
