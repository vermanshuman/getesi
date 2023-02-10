package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "dic_aggregation_land_char_reg_reference")
public class AggregationLandChargesRegistryReference extends IndexedEntity {

    private static final long serialVersionUID = 3978217340924432662L;

    @Column(name = "name")
    private String name;

    @Column(name = "role")
    private String role;

    @Column(name = "email")
    private String email;

    @Column(name = "cell_phone")
    private String cellPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dic_aggregation_land_char_reg_id")
    private AggregationLandChargesRegistry aggregationLandChargesRegistry;
}