package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Relationship;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;

import java.util.Objects;

public class RelationshipGroupingWrapper {

    private String unitaNeg;

    private String propertyType;

    private String quote;

    private String regime;

    private City city;

    public RelationshipGroupingWrapper(Relationship relationship) {
        this.unitaNeg = relationship.getUnitaNeg();
        this.propertyType = relationship.getPropertyType();
        this.quote = relationship.getQuote();
        this.regime = relationship.getRegime();
        if(!ValidationHelper.isNullOrEmpty(relationship.getProperty())
                && !ValidationHelper.isNullOrEmpty(relationship.getProperty().getCity()))
        this.city = relationship.getProperty().getCity();
    }

    public RelationshipGroupingWrapper(String quote, String propertyType, String regime) {
        this.quote = quote;
        this.propertyType = propertyType;
        this.regime = regime;
    }

    public RelationshipGroupingWrapper(String quote, String propertyType, String regime, City city) {
        this.quote = quote;
        this.propertyType = propertyType;
        this.regime = regime;
        this.city = city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipGroupingWrapper that = (RelationshipGroupingWrapper) o;
        return Objects.equals(unitaNeg, that.unitaNeg) &&
                Objects.equals(propertyType, that.propertyType) &&
                Objects.equals(quote, that.quote) &&
                Objects.equals(regime, that.regime) &&
                Objects.equals(city, that.city);
    }

    @Override
    public int hashCode() {

        return Objects.hash(unitaNeg, propertyType, quote, regime, city);
    }

    public String getExpectedRegimeFormat() {
        String result = "";
        if (!ValidationHelper.isNullOrEmpty(getRegime())) {
            result = "<br/> (" + getRegime() + ")";
        }
        return result;
    }

    public String getManagedPropertyType() {
        String result = "";
        if (!ValidationHelper.isNullOrEmpty(getPropertyType())) {
            result = getPropertyType();

            if (getPropertyType().contains("DELL'ENFITEUTA")) {
                result = getPropertyType().replace("DELL'ENFITEUTA", "ENFITEUSI");
            } else if (getPropertyType().contains("DI DEL")) {
                result = getPropertyType().substring(getPropertyType().indexOf(" ")+1);
            }
        }
        return result;
    }

    public String getUnitaNeg() {
        return unitaNeg;
    }

    public void setUnitaNeg(String unitaNeg) {
        this.unitaNeg = unitaNeg;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getRegime() {
        return regime;
    }

    public void setRegime(String regime) {
        this.regime = regime;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
}