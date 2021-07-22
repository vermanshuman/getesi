package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ClientTitleType;
import it.nexera.ris.common.enums.ClientType;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@javax.persistence.Entity
@Table(name = "notary")
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

    public String toString() {
    	String r = name;
    	
    	if (!ValidationHelper.isNullOrEmpty(city))
    		r += " - " + city.toUpperCase();
    	
    	return r;
    }
    
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getOfficeAddress() {
		return officeAddress;
	}

	public void setOfficeAddress(String officeAddress) {
		this.officeAddress = officeAddress;
	}

	public String getInscriptions() {
		return inscriptions;
	}

	public void setInscriptions(String inscriptions) {
		this.inscriptions = inscriptions;
	}

}