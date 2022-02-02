package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "land_omi")
public class LandOmi extends IndexedEntity {
    private static final long serialVersionUID = -9119413991672647922L;

    @Column(name = "year")
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    private Province province;

    @OneToMany(mappedBy = "landOmi", cascade = CascadeType.ALL)
    private List<LandOmiValue> omiValues;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "land_omi_city", joinColumns = {
            @JoinColumn(name = "land_omi_id", table = "land_omi")
    }, inverseJoinColumns = {
            @JoinColumn(name = "city_id", table = "dic_city")
    })
    private List<City> cities;
}
