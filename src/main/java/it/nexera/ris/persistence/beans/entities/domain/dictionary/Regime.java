package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

@Entity
@Table(name = "regime")
public class Regime extends IndexedEntity {

    private static final long serialVersionUID = -735851224880599219L;

    @Column(name = "code")
    private String code;
    
    @Column(name = "text")
    private String text;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
    
 
    public Boolean getIsDeleted() {
        return isDeleted == null ? Boolean.FALSE : isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    @Override
    public String toString() {
        return this.getText();
    }


}
