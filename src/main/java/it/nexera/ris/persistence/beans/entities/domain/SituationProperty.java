package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "situation_property")
public class SituationProperty extends IndexedEntity {

    private static final long serialVersionUID = 6919102074560613032L;

    @ManyToOne
    @JoinColumn(name = "situation_id")
    private EstateSituation situation;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    @ManyToMany(mappedBy = "situationProperties")
    private List<DatafromProperty> datafromProperties;

    @Transient
    private Long orderNumber;

    public SituationProperty() {
    }

    public SituationProperty(EstateSituation situation, Property property) {
        this.situation = situation;
        this.property = property;
    }

    public void removeAllDatafromPropertiesAssociations() {
        if (!ValidationHelper.isNullOrEmpty(this.getDatafromProperties())) {
            for (DatafromProperty datafromProperty : this.getDatafromProperties()) {
                datafromProperty.getSituationProperties().remove(this);
            }
            this.getDatafromProperties().clear();
        }
    }

    public EstateSituation getSituation() {
        System.out.println("situation  " + situation.getId());
        return situation;
    }

    public void setSituation(EstateSituation situation) {
        this.situation = situation;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public List<DatafromProperty> getDatafromProperties() {
        return datafromProperties;
    }

    public void setDatafromProperties(List<DatafromProperty> datafromProperties) {
        this.datafromProperties = datafromProperties;
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Long orderNumber) {
        this.orderNumber = orderNumber;
    }
}
