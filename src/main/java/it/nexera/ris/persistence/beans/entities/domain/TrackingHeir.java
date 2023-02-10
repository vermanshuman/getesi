package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.TrackingHeirType;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "tracking_heir")
public class TrackingHeir extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -192116614117875783L;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "fiscal_code")
    private String fiscalCode;

    private String address;

    @Column(name = "indirizzo_civico")
    private String civicAddress;

    private String city;

    private String province;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name="notes", columnDefinition="varchar(400)")
    private String notes;
    
    @Column(name = "address_status")
    private String addressStatus;
    
    @Column(name = "accepted_heritage")
    private Boolean acceptedHeritage;
    
    @Column(name = "type")
    private TrackingHeirType type;

    private String relationship;

}