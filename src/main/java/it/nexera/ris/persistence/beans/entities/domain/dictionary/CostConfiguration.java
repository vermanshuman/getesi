package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "dic_cost_configuration")
@Getter
@Setter
public class CostConfiguration extends IndexedEntity {

    private static final long serialVersionUID = 4353731225922784701L;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "type_id")
    private Long typeId;

    @ManyToMany(mappedBy = "costConfigurations")
    private List<Service> services;

    @ManyToMany(mappedBy = "serviceCostUnauthorizedQuoteList")
    private List<Service> serviceCostUnauthorizedQuoteServices;
    
    @Column(name = "urgency")
    private Boolean urgency;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (this.getId() == null) {
            return obj == this;
        }

        CostConfiguration costEntity = (CostConfiguration) obj;

        return this.getId().equals(costEntity.getId());
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
