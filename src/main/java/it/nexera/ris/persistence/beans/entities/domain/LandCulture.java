package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "land_culture")
@Getter
@Setter
public class LandCulture extends IndexedEntity implements Serializable {
    private static final long serialVersionUID = 7752749142252892509L;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "landCulture")
    private List<LandCadastralCulture> landCadastralCultures;

    private Boolean unavailable;

    @Override
    public String toString() {
        return name;
    }
}