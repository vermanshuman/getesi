package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "estate_location")
public class EstateLocation extends IndexedEntity {

    private static final long serialVersionUID = 863893897426501718L;

    @Column(name = "description")
    private String description;

    @Column(name = "common_code")
    private String commonCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "estate_formality_id")
    private EstateFormality estateFormality;

    public boolean isEmpty() {
        if (!ValidationHelper.isNullOrEmpty(description)) return false;
        if (!ValidationHelper.isNullOrEmpty(commonCode)) return false;
        return true;
    }

    @Override
    public String toString() {
        return description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommonCode() {
        return commonCode;
    }

    public void setCommonCode(String commonCode) {
        this.commonCode = commonCode;
    }

    public EstateFormality getEstateFormality() {
        return estateFormality;
    }

    public void setEstateFormality(EstateFormality estateFormality) {
        this.estateFormality = estateFormality;
    }
}
