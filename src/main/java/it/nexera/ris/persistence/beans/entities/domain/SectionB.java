package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "section_b")
public class SectionB extends IndexedEntity {

    private static final long serialVersionUID = 4997265103599106505L;

    @Column(name = "bargaining_unit", length = 30)
    private String bargainingUnit;

    @ManyToOne
    @JoinColumn(name = "formality_id")
    private Formality formality;

    @ManyToMany
    @JoinTable(name = "property_section_b", joinColumns =
            {
                    @JoinColumn(name = "section_b_id", table = "section_b")
            }, inverseJoinColumns =
            {
                    @JoinColumn(name = "property_id", table = "property")
            })
    private List<Property> properties;

    public SectionB() {
    }

    public SectionB(String bargainingUnit) {
        this.bargainingUnit = bargainingUnit;
    }

    public String getBargainingUnit() {
        return bargainingUnit;
    }

    public void setBargainingUnit(String bargainingUnit) {
        this.bargainingUnit = bargainingUnit;
    }

    public Formality getFormality() {
        return formality;
    }

    public void setFormality(Formality formality) {
        this.formality = formality;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

}
