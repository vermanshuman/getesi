package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "dic_aggregation_land_char_reg_note")
public class AggregationLandChargesRegistryNote extends IndexedEntity {

    private static final long serialVersionUID = 6813054584448975265L;

    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dic_aggregation_land_char_reg_id")
    private AggregationLandChargesRegistry aggregationLandChargesRegistry;
}