package it.nexera.ris.persistence.beans.entities.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

@Entity
@Table(name = "situation_real_formality_property")
public class EstateSituationFormalityProperty extends IndexedEntity {


	private static final long serialVersionUID = -341502387961235834L;

	@ManyToOne
	@JoinColumn(name = "situation_id")
	private EstateSituation estateSituations;

	@ManyToOne
	@JoinColumn(name = "property_id")
	private Property property;
	
	@ManyToOne
	@JoinColumn(name = "formality_id")
	private Formality formality;


	public EstateSituation getEstateSituations() {
		return estateSituations;
	}

	public void setEstateSituations(EstateSituation estateSituations) {
		this.estateSituations = estateSituations;
	}

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public Formality getFormality() {
		return formality;
	}

	public void setFormality(Formality formality) {
		this.formality = formality;
	}

}
