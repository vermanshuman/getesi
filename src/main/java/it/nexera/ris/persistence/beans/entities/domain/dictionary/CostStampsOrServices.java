package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "cost_stamps_services")
public class CostStampsOrServices extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -6796159970332492880L;

    @Column(name = "tax_stamp_cost")
    private Double taxStampCost;

    @Column(name = "services_stamp_cost")
    private Double servicesStampCost;
}
