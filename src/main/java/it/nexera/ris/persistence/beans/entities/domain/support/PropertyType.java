package it.nexera.ris.persistence.beans.entities.domain.support;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "property_type")
public class PropertyType extends IndexedEntity {

    private static final long serialVersionUID = -1860536042524971848L;

    @Column(length = 10)
    private String code;

    @Column(length = 50)
    private String name;

    @Column(length = 150)
    private String description;

    @Column(length = 10)
    private String bscode;

    @Override
    public String toString() {
        return this.getDescription();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getBscode() {
        return bscode;
    }

    public void setBscode(String bscode) {
        this.bscode = bscode;
    }

}
