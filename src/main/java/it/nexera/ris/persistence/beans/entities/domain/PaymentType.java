package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "payment_type")
@Getter
@Setter
public class PaymentType extends IndexedEntity implements Serializable {

	private static final long serialVersionUID = -203805995863279495L;

	@Column(name = "code")
    private String code;	
	
    @Column(name = "description")
    private String description;

    @Column(name = "beneficiary")
    private String beneficiary;

    @Column(name = "istitution_name")
    private String istitutionName;

    @Column(name = "iban")
    private String iban;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public String toString() {
        return description;
    }

}
