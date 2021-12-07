package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "old_property")
public class OldProperty extends IndexedEntity {

    private static final long serialVersionUID = -5689723184099450504L;

    @OneToOne
    @JoinColumn(name = "property_id")
    private Property property;

    @OneToOne
    @JoinColumn(name = "old_property_id")
    private Property oldProperty;

    @OneToOne
    @JoinColumn(name = "formality_id")
    private Formality formality;

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Property getOldProperty() {
        return oldProperty;
    }

    public void setOldProperty(Property oldProperty) {
        this.oldProperty = oldProperty;
    }

    public Formality getFormality() {
        return formality;
    }

    public void setFormality(Formality formality) {
        this.formality = formality;
    }
}
