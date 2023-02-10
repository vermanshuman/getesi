package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "tracking_bank_account")
public class TrackingBankAccount extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -9026481937608153847L;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private String checked;

    private String name;

    private String vat;

    private String address;

    @Column(name = "indirizzo_civico")
    private String civicAddress;

    @Column(name = "account_number")
    private String accountNumber;

    private String abi;

    private String cab;

    @Column(name="notes", columnDefinition="varchar(4000)")
    private String notes;
    
    @Column(name = "address_city")
    private String addressCity;
    
    @Column(name = "address_province")
    private String addressProvince;
    
    @Column(name = "address_cap")
    private String addressCap;
    
    private String active;

    @Column(name = "address_status")
    private String addressStatus;

    private String protests;
}