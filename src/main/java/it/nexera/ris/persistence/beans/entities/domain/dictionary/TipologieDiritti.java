package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

@Entity
@Table(name = "relationship_type")
public class TipologieDiritti extends IndexedEntity {

    private static final long serialVersionUID = 4401488640720926480L;

    @Column(name = "nome")
    private String name;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
    
 
    @Override
    public String toString() {
        return this.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Boolean getIsDeleted() {
        return isDeleted == null ? Boolean.FALSE : isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

}
