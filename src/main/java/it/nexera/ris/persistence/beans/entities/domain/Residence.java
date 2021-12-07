package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;

import javax.persistence.*;

@Entity
@Table(name = "residence")
public class Residence extends IndexedEntity {

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(name = "address")
    private String address;

    @Column(name = "cap")
    private String cap;

    public Residence() {
    }

    public Residence(Residence residence) {
        this.province = residence.province;
        this.city = residence.city;
        this.country = residence.country;
        this.address = residence.address;
        this.cap = residence.cap;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }
}
