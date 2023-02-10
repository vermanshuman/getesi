package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false, exclude = {"comprMin", "comprMax"})
@Data
@Entity
@Table(name = "omi_value", indexes = {@Index(columnList = "zone,city_cfis,category_code,state")})
public class OmiValue extends IndexedEntity {

    private static final long serialVersionUID = 9124134189291520890L;

    @Column(name = "zone")
    private String zone;

    @Column(name = "city_cfis")
    private String cityCfis;

    @Column(name = "city_description")
    private String cityDescription;

    @Column(name = "category_code")
    private Long categoryCode;

    @Column(name = "compr_min")
    private Long comprMin;

    @Column(name = "compr_max")
    private Long comprMax;

    @Column(name = "state")
    private String state;

    private Boolean manual;

    @Transient
    private Double minValue;

    @Transient
    private Double maxValue;

}
