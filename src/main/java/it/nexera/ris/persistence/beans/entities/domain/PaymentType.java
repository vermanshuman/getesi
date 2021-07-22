package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "payment_type")
public class PaymentType extends IndexedEntity implements Serializable {

	private static final long serialVersionUID = -203805995863279495L;

	@Column(name = "code")
    private String code;	
	
    @Column(name = "description")
    private String description;
    
    public String toString() {
    	return description;
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
}
