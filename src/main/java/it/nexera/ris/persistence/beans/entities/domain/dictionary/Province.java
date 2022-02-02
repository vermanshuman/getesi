package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "dic_province")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "PROVINCE_SEQ", allocationSize = 1)
public class Province extends Dictionary {

    private static final long serialVersionUID = -7915205798272153294L;

    public static final String FOREIGN_COUNTRY = "STATO ESTERO";

    public static final Long FOREIGN_COUNTRY_ID = -1L;

    @Column(name = "client_instance")
    private Boolean clientInstance;

    @ManyToMany(mappedBy = "provinces", fetch = FetchType.LAZY)
    private List<LandChargesRegistry> landChargesRegistries;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (this.getId() == null) {
            return obj == this;
        }

        Province province = (Province) obj;

        return this.getId().equals(province.getId());
    }

    public Boolean getClientInstance() {
        return clientInstance;
    }

    public void setClientInstance(Boolean clientInstance) {
        this.clientInstance = clientInstance;
    }

    public List<LandChargesRegistry> getLandChargesRegistries() {
        return landChargesRegistries;
    }

    public void setLandChargesRegistries(List<LandChargesRegistry> landChargesRegistries) {
        this.landChargesRegistries = landChargesRegistries;
    }
}
