package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "datafrom_property")
public class DatafromProperty extends IndexedEntity {

    private static final long serialVersionUID = -2961478399220174241L;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(name = "text")
    private String text;

    @ManyToMany
    @JoinTable(name = "situation_property_datafrom", joinColumns = {
            @JoinColumn(name = "datafrom_property_id", table = "datafrom_property")
    }, inverseJoinColumns = {
            @JoinColumn(name = "situation_property_id", table = "situation_property")
    })
    private List<SituationProperty> situationProperties;

    @Transient
    private boolean associated;

    public boolean isAssociated() {
        return associated;
    }

    public void setAssociated(boolean associated) {
        this.associated = associated;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<SituationProperty> getSituationProperties() {
        return situationProperties;
    }

    public void setSituationProperties(List<SituationProperty> situationProperties) {
        this.situationProperties = situationProperties;
    }
}
