package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "section_d")
public class SectionD extends IndexedEntity {

    private static final long serialVersionUID = 2821593198340675449L;

    @ManyToOne
    @JoinColumn(name = "formality_id")
    private Formality formality;

    @Column(name = "additional_information")
    private String additionalInformation;

    public Formality getFormality() {
        return formality;
    }

    public void setFormality(Formality formality) {
        this.formality = formality;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}
