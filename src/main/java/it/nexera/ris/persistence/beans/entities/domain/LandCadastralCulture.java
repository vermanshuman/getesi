package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "land_cadastral_culture")
@Data
public class LandCadastralCulture extends IndexedEntity implements Serializable {
    private static final long serialVersionUID = -3426555567725332147L;

    private String description;

    @ManyToOne
    @JoinColumn(name = "land_culture_id")
    private LandCulture landCulture;
}
