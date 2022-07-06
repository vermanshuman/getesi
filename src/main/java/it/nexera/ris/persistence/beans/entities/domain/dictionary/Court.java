package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

@Entity
@Table(name = "court")
public class Court extends IndexedEntity {

    private static final long serialVersionUID = -2454753551676642576L;

    @Column(name = "name")
    private String name;

    @Column(name = "fiscal_code")
    private String fiscalCode;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Override
    public String toString() {
        return this.getName();
    }

    public String getName() {
        return name;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public City getCity() {
        return city;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }


}
