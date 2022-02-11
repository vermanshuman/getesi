package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "dic_aggregation_land_char_reg")
public class AggregationLandChargesRegistry extends IndexedEntity {

    private static final long serialVersionUID = 1533180820445303590L;

    @Column(name = "name")
    private String name;

    @ManyToMany
    @JoinTable(name = "aggregation_land", joinColumns =
            {@JoinColumn(name = "aggregation_id", table = "dic_aggregation_land_char_reg")}, inverseJoinColumns =
            {@JoinColumn(name = "land_id", table = "dic_land_charges_registry")})
    private List<LandChargesRegistry> landChargesRegistries;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "national")
    private Boolean national;

    @Override
    public String toString() {
        return getName();
    }

    public String getLandChargesRegistriesStr() {
        StringBuffer sb = new StringBuffer();

        if (!ValidationHelper.isNullOrEmpty(getLandChargesRegistries())) {
            for (LandChargesRegistry landReg : getLandChargesRegistries()) {
                sb.append(landReg);
                sb.append(" + ");
            }

            sb = new StringBuffer(sb.substring(0, sb.length() - 3));
        }

        return sb.toString();
    }

    public int getNumberOfVisualizedLandChargesRegistries() {
        int result = 0;

        for (LandChargesRegistry registry : getLandChargesRegistries()) {
            if (!ValidationHelper.isNullOrEmpty(registry.getVisualize()) && registry.getVisualize()) {
                result += 1;
            }
        }
        return result;
    }

    public List<Long> getAggregationLandChargesRegistersIds(){
        List<Long> chargesRegistryIds;
        if (!ValidationHelper.isNullOrEmpty(getLandChargesRegistries())) {
            chargesRegistryIds = getLandChargesRegistries().stream()
                    .map(LandChargesRegistry::getId).collect(Collectors.toList());
        } else {
            chargesRegistryIds = Collections.singletonList(0L);
        }
        return chargesRegistryIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LandChargesRegistry> getLandChargesRegistries() {
        return landChargesRegistries;
    }

    public void setLandChargesRegistries(
            List<LandChargesRegistry> landChargesRegistries) {
        this.landChargesRegistries = landChargesRegistries;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Boolean getNational() {
        return national;
    }

    public void setNational(Boolean national) {
        this.national = national;
    }
}
