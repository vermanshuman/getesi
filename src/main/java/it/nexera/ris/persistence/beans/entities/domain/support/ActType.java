package it.nexera.ris.persistence.beans.entities.domain.support;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "act_type", uniqueConstraints = @UniqueConstraint(columnNames =
        {"type", "code"}))
public class ActType extends IndexedEntity {

    private static final long serialVersionUID = -961896923418770702L;

    @Column(length = 50)
    private String type;

    @Column(length = 3)
    private String code;

    @Column(length = 150)
    private String description;

    @Column(length = 10)
    private String bscode;

    @Override
    public String toString() {
        return this.getDescription();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
