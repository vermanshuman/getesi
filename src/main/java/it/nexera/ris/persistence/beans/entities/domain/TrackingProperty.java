package it.nexera.ris.persistence.beans.entities.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "tracking_property")
public class TrackingProperty extends IndexedEntity implements Serializable {

	private static final long serialVersionUID = -5525190469948707734L;
	
	@Column(name = "notes", columnDefinition="varchar(4000)")
    private String notes;
	
	@Column(name = "pra")
	private Boolean pra;
	
	@Column(name = "protective_act")
    private String protectiveAct;
	
	@Column(name = "distraints")
    private String distraints;
	
	@Column(name = "import_prejudicial")
    private BigDecimal importPrejudicial;
	
	@Column(name = "prejudicial")
    private String prejudicial;
	
	@Column(name = "quote_value")
    private String quoteValue;
	
	@Column(name = "free_value")
    private String freeValue;
	
	@Column(name = "value_mortgage")
    private BigDecimal valueMortgage;
	
	@Column(name = "value_relationship")
    private String valueRelationship;
	
	@Column(name = "intentional_burden")
    private String intentionalBurden;
	
	@Column(name = "judicial_burden")
    private String judicialBurden;
	
	@Column(name = "property_value")
    private String propertyValue;
	
	@Column(name = "number_free_property")
    private Integer numberFreeProperty;
	
	@Column(name = "land_registry")
    private String landRegistry;
	
	@Column(name = "number_land")
    private Integer numberLand;
	
	@Column(name = "number_property")
    private Integer numberProperty;
	
	@Column(name = "property_owned")
    private String propertyOwned;
	
	@Column(name = "cadastral")
    private String cadastral;
	
	@Column(name = "ownership")
    private String ownership;
	
	@Column(name = "address")
    private String address;

	@Column(name = "indirizzo_civico")
	private String civicAddress;

	@Column(name = "address_city")
    private String addressCity;
	
	@Column(name = "address_province")
    private String addressProvince;
	
	@Column(name = "sheet")
    private String sheet;
	
	@Column(name = "particle")
    private String particle;
	
	@Column(name = "classification")
    private String classification;
	
	@Column(name = "consistency")
    private String consistency;
	
	@Column(name = "revenue")
    private String revenue;
	
	@Column(name = "partita")
    private String match;
	
	@Column(name = "id_cadastral")
    private String idCadastral;
	
	@Column(name = "inspection_date")
    private Date inspectionDate;
	
	@Column(name = "class")
    private String propertyClass;
	
	@Column(name = "repertoire")
    private String repertoire;
	
	@Column(name = "pubblic_official")
    private String pubblicOfficial;
	
    private String sub;
	
	@Column(name = "notes_land_registry", columnDefinition="varchar(4000)")
    private String notesLandRegistry;

	@Column(name = "address_cap")
	private String addressCap;

	@Column(name = "address_status")
	private String addressStatus;

	private String other;

	@ManyToOne
	@JoinColumn(name = "request_id")
	private Request request;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subject_id")
	private Subject subject;
}
