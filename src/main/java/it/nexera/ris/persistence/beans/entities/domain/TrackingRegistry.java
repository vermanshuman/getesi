package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "tracking_registry")
public class TrackingRegistry extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -7373474729473632027L;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private String address;

    @Column(name = "indirizzo_civico")
    private String civicAddress;

    @Column(name = "postal_code")
    private String postalCode;

    private String province;

    private String city;

    @Column(name="notes_residence", columnDefinition="varchar(4000)")
    private String notes;

    @Column(name = "deceased_date")
    private Date deceasedDate;

    @Column(name = "deceased_last_address")
    private String deceasedLastAddress;

    @Column(name = "deceased_indirizzo_civico")
    private String deceasedCivicAddress;

    @Column(name = "cf_confirmed")
    private Boolean cfConfirmed;
    
    @Column(name = "new_cf")
    private String newCf;
    
    @Column(name = "civil_status")
    private String civilStatus;
    
    @Column(name = "sex")
    private Long sex;
    
    @Column(name = "birth_date")
    private Date birthDate;
    
    @Column(name = "birth_city")
    private String birthCity;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "debt_position")
    private String debtPosition;
    
    @Column(name = "notes_subject", columnDefinition="varchar(4000)")
    private String notesSubject;
    
    @Column(name = "deceased_address_city")
    private String deceasedAddressCity;
    
    @Column(name = "deceased_address_province")
    private String deceasedAddressProvince;
    
    @Column(name = "deceased_address_cap")
    private String deceasedAddressCap;
    
    @Column(name = "deceased_address_status")
    private String deceasedAddressStatus;

    @Column(name = "deceased_address_notes", columnDefinition="varchar(4000)")
    private String deceasedAddressNotes;

    @Column(name = "notes_deceased", columnDefinition="varchar(4000)")
    private String notesDeceased;
    
    @Column(name = "business_name")
    private String businessName;
    
    @Column(name = "revoke_date_proceeding")
    private Date revokeDateProceeding;
    
    @Column(name = "close_date_proceeding")
    private Date closeDateProceeding;
    
    @Column(name = "insolvency_proceeding")
    private String insolvencyProceeding;
    
    @Column(name = "import")
    private BigDecimal importTracking;
    
    @Column(name = "code_ateco")
    private String codeAteco;
    
    @Column(name = "date_start")
    private Date dateStart;
    
    @Column(name = "date_end")
    private Date dateEnd;
    
    @Column(name = "date_creation")
    private Date dateCreation;
    
    @Column(name = "giuridic_nature")
    private String giuridicNature;
    
    @Column(name = "data_status")
    private String dataStatus;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "number_vat")
    private String numberVat;
    
    @Column(name = "protest")
    private String protest;
    
    @Column(name = "prejudicial")
    private String prejudicial;
    
    @Column(name = "condominium")
    private String condominium;
    
    @Column(name = "cciaa")
    private String cciaa;
    
    @Column(name = "social_amount")
    private BigDecimal socialAmount;
    
    @Column(name = "pec")
    private String pec;
    
    @Column(name = "domicile_address")
    private String domicileAddress;

    @Column(name = "domicile_indirizzo_civico")
    private String domicileCivicAddress;
    
    @Column(name = "domicile_cap")
    private String domicileCap;
    
    @Column(name = "domicile_city")
    private String domicileCity;
    
    @Column(name = "domicile_province")
    private String domicileProvince;

    @Column(name = "deceased_confirmed")
    private Boolean deceasedConfirmed;

    @Column(name = "address_city")
    private String addressCity;

    @Column(name = "address_province")
    private String addressProvince;

    @Column(name = "address_cap")
    private String addressCap;

    @Column(name = "address_status")
    private String addressStatus;

    private Boolean active;

    @Column(name = "domicile_status")
    private String domicileStatus;

    private Boolean untraceable;

    @Column(name = "residence_address")
    private String residenceAddress;

    @Column(name = "residence_indirizzo_civico")
    private String residenceCivicAddress;

    @Column(name = "residence_city")
    private String residenceCity;

    @Column(name = "residence_province")
    private String residenceProvince;

    @Column(name = "residence_cap")
    private String residenceCap;

    @Column(name = "residence_status")
    private String residenceStatus;

    @Column(name = "notes_domicile")
    private String notesDomicile;
}
