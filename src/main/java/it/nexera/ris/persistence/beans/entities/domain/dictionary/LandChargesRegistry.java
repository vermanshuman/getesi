package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import it.nexera.ris.common.enums.LandChargesRegistryType;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

@Entity
@Table(name = "dic_land_charges_registry")
public class LandChargesRegistry extends IndexedEntity {

    private static final long serialVersionUID = -2313765320505312349L;

    @Column(name = "name")
    private String name;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "description")
    private String description;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column
    @Enumerated(EnumType.STRING)
    private LandChargesRegistryType type;

    @ManyToMany
    @JoinTable(name = "land_char_reg_province", joinColumns =
            {
                    @JoinColumn(name = "land_id", table = "dic_land_charges_registry")
            }, inverseJoinColumns =
            {
                    @JoinColumn(name = "province_id", table = "dic_province")
            })
    private List<Province> provinces;

    @ManyToMany
    @JoinTable(name = "land_char_reg_city", joinColumns =
            {
                    @JoinColumn(name = "land_id", table = "dic_land_charges_registry")
            }, inverseJoinColumns =
            {
                    @JoinColumn(name = "city_id", table = "dic_city")
            })
    private List<City> cities;

    @ManyToMany(mappedBy = "landChargesRegistries")
    private List<AggregationLandChargesRegistry> aggregationLandChargesRegistries;

    @Column(name = "visualize")
    private Boolean visualize;

    @Override
    public String toString() {
        return this.getName();
    }

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

        LandChargesRegistry registryEntity = (LandChargesRegistry) obj;

        return this.getId().equals(registryEntity.getId());
    }

    public List<String> getCityDescriptions() {
        if (getCities() != null) {
            return getCities().stream().map(Object::toString)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<String>();
        }
    }

    public List<String> getProvinceDescriptions() {
        if (getProvinces() != null) {
            return getProvinces().stream().map(Object::toString)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<String>();
        }
    }

    public String getTypeDescription() {
        return type == null ? "" : type.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsDeleted() {
        return isDeleted == null ? Boolean.FALSE : isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LandChargesRegistryType getType() {
        return type;
    }

    public void setType(LandChargesRegistryType type) {
        this.type = type;
    }

    public List<Province> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<Province> provinces) {
        this.provinces = provinces;
    }

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }

    public List<AggregationLandChargesRegistry> getAggregationLandChargesRegistries() {
        return aggregationLandChargesRegistries;
    }

    public void setAggregationLandChargesRegistries(List<AggregationLandChargesRegistry> aggregationLandChargesRegistries) {
        this.aggregationLandChargesRegistries = aggregationLandChargesRegistries;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Boolean getVisualize() {
        return visualize;
    }

    public void setVisualize(Boolean visualize) {
        this.visualize = visualize;
    }
}
