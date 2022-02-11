package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "country")
public class CountryList {

    @XmlElement(name = "data_entry", type = Country.class)
    private List<Country> countries;

    public CountryList() {
        super();
    }

    public List<Country> getCountries() {
        return countries;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }

}
