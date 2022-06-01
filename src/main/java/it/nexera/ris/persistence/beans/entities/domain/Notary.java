package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "notary")
@Getter
@Setter
public class Notary extends IndexedEntity implements Serializable {

	private static final long serialVersionUID = 7229760713684989000L;

	@Column(name = "name")
    private String name;   
    
    @Column(name = "city")
    private String city;

    @Column(name = "office_address")
    private String officeAddress;

    @Column(name = "inscriptions")
    private String inscriptions;

	@Column(name = "is_deleted")
	private Boolean isDeleted;

    public String toString() {
    	String r = name;
    	
    	if (!ValidationHelper.isNullOrEmpty(city))
    		r += " - " + city.toUpperCase();
    	
    	return r;
    }
}