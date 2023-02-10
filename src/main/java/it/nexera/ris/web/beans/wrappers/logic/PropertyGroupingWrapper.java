package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;

import java.util.Objects;

public class PropertyGroupingWrapper {

    private City city;

    private String sectionCity;

    public PropertyGroupingWrapper(Property property) {
        this.city = property.getCity();
        this.sectionCity = property.getSectionCity();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyGroupingWrapper that = (PropertyGroupingWrapper) o;
        return Objects.equals(city, that.city) &&
                Objects.equals(sectionCity, that.sectionCity);
    }

    @Override
    public int hashCode() {

        return Objects.hash(city, sectionCity);
    }
    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getSectionCity() {
        return sectionCity;
    }

    public void setSectionCity(String sectionCity) {
        this.sectionCity = sectionCity;
    }
}