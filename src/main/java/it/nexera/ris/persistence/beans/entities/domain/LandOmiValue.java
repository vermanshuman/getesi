package it.nexera.ris.persistence.beans.entities.domain;

import javax.persistence.*;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.atmosphere.config.service.Get;

@Entity
@Table(name = "land_omi_value")
public class LandOmiValue extends IndexedEntity {
    private static final long serialVersionUID = 4946419315802333282L;

    @ManyToOne
    @JoinColumn(name = "land_omi_id")
    private LandOmi landOmi;

    @ManyToOne
    @JoinColumn(name = "land_culture_id")
    private LandCulture landCulture;

    @Column(name = "value")
    private Double value;

    public LandOmi getLandOmi() {
        return landOmi;
    }

    public void setLandOmi(LandOmi landOmi) {
        this.landOmi = landOmi;
    }

    public LandCulture getLandCulture() {
        return landCulture;
    }

    public void setLandCulture(LandCulture landCulture) {
        this.landCulture = landCulture;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public boolean isEmpty() {
        if (!ValidationHelper.isNullOrEmpty(value)) return false;
        return true;
    }
}