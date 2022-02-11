package it.nexera.ris.persistence.beans.entities.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;

@Entity
@Table(name = "report_formality_subject")
public class ReportFormalitySubject extends IndexedEntity {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4161646132624047707L;

	@Column(name = "type_formality_id")
    private Long typeFormalityId;

	@Column(name = "date")
    private Date date;
    
    @Column(name = "number")
    private String number;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "surname")
    private String surname;
    
    @Column(name = "birth_date")
    private Date birthDate;
    
    @Column(name = "business_name")
    private String businessName;
    
    @Column(name = "type_id")
    private Long typeId;
    
    @Column(name = "number_vat")
    private String numberVAT;
    
    @ManyToOne
    @JoinColumn(name = "birth_province_id")
    private Province birthProvince;

    @ManyToOne
    @JoinColumn(name = "birth_city_id")
    private City birthCity;
    
    @Column(name = "sex")
    private Long sex;
    
    public Province getBirthProvince() {
		return birthProvince;
	}

	public void setBirthProvince(Province birthProvince) {
		this.birthProvince = birthProvince;
	}

	public City getBirthCity() {
		return birthCity;
	}

	public void setBirthCity(City birthCity) {
		this.birthCity = birthCity;
	}

	public Long getSex() {
		return sex;
	}

	public void setSex(Long sex) {
		this.sex = sex;
	}

	public String getNumberVAT() {
		return numberVAT;
	}

	public void setNumberVAT(String numberVAT) {
		this.numberVAT = numberVAT;
	}

	public String getFiscalCode() {
		return fiscalCode;
	}

	public void setFiscalCode(String fiscalCode) {
		this.fiscalCode = fiscalCode;
	}

	@Column(name = "fiscal_code")
    private String fiscalCode;    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dic_land_charges_registry_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private LandChargesRegistry landChargesRegistry;
    
    public Long getTypeFormalityId() {
		return typeFormalityId;
	}

	public void setTypeFormalityId(Long typeFormalityId) {
		this.typeFormalityId = typeFormalityId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public LandChargesRegistry getLandChargesRegistry() {
		return landChargesRegistry;		
	}

	public void setLandChargesRegistry(LandChargesRegistry landChargesRegistry) {
		this.landChargesRegistry = landChargesRegistry;
	}
	
	public String getLandChargesRegistryName() {
		return (landChargesRegistry == null) ? "" :
			landChargesRegistry.getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public Long getTypeId() {
		return typeId;
	}

	public void setTypeId(Long typeId) {
		this.typeId = typeId;
	}
	
    public String getFullName() {
        if (SubjectType.PHYSICAL_PERSON.getId().equals(getTypeId())) {
            return String.format("%s %s", getSurname(), getName());
        } else if (SubjectType.LEGAL_PERSON.getId().equals(getTypeId())) {
            return getBusinessName();
        }

        return "";
    }
    
    public String getSubjectFiscalCode() {
    	return SubjectType.LEGAL_PERSON.getId().equals(getTypeId()) ?
    		getNumberVAT() : getFiscalCode();
    }
    
    public String getTypeFormality() {
        String value = getTypeFormalityId().toString();
        if(!ValidationHelper.isNullOrEmpty(getTypeFormalityId())) {
            switch (getTypeFormalityId().intValue()) {
            case 1:
                value = "Trascizione";
                break;

            case 2:
                value = "Iscrizione";
                break;

            case 4:
                value = "Annotamento";
                break;
            
            default:
                break;
            }
        }
        
        return value;
    }
}
