package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "dic_cost_configuration")
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public List<Service> getServiceCostUnauthorizedQuoteServices() {
        return serviceCostUnauthorizedQuoteServices;
    }

    public void setServiceCostUnauthorizedQuoteServices(List<Service> serviceCostUnauthorizedQuoteServices) {
        this.serviceCostUnauthorizedQuoteServices = serviceCostUnauthorizedQuoteServices;
    }

    public Boolean getUrgency() {
        return urgency;
    }

    public void setUrgency(Boolean urgency) {
        this.urgency = urgency;
    }
}
