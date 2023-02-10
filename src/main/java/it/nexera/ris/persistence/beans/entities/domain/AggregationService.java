package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationAZ;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "aggregation_service")
public class AggregationService extends IndexedEntity {

    private static final long serialVersionUID = 5779283429773388004L;

    @ManyToMany(mappedBy = "aggregationServices")
    private List<Service> services;


    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "aggregation_az_id")
    private AggregationAZ aggregationAZ;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public void removeService(Service service) {
        this.getServices().remove(service);
        service.getAggregationServices().remove(this);
    }

    public void addService(Service service) {
        this.getServices().add(service);
        service.getAggregationServices().add(this);
    }
}
