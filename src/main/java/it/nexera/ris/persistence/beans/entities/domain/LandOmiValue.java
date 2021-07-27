package it.nexera.ris.persistence.beans.entities.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
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

    public boolean isEmpty() {
        if (!ValidationHelper.isNullOrEmpty(landCulture)) return false;
        if (!ValidationHelper.isNullOrEmpty(value)) return false;
        return true;
    }
}