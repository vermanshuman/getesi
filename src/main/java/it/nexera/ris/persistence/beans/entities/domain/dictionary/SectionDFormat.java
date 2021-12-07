package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

@Entity
@Table(name = "section_d_format")
public class SectionDFormat extends IndexedEntity {

    private static final long serialVersionUID = 1606680898694670503L;

    private String name;
    
    @Column(name="text", columnDefinition="TEXT")
    private String text;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted == null ? Boolean.FALSE : isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    @Override
    public String toString() {
        return name;
    }
}