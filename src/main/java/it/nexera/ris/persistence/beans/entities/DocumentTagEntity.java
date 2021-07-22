package it.nexera.ris.persistence.beans.entities;

import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class DocumentTagEntity extends IndexedEntity {

    private static final long serialVersionUID = 586933551758505066L;

    public abstract Subject getSubject();

    public abstract Client getClient();

    public abstract AggregationLandChargesRegistry getAggregationLandChargesRegistry();

}
