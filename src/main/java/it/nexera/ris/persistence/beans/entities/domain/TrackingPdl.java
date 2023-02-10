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
@Table(name = "tracking_pdl")
public class TrackingPdl extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = 1405511044563822284L;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "job_status")
    private String jobStatus ;

    private String employer ;

    @Column(name = "employer_vat")
    private String employerVat;

    @Column(name = "employer_legal_address")
    private String employerLegalAddress;

    @Column(name = "employer_legal_indirizzo_civico")
    private String employerLegalCivicAddress;

    @Column(name = "job_start_date")
    private Date jobStartDate;

    @Column(name = "contract_type")
    private String contractType;

    @Column(name = "net_amount")
    private BigDecimal netAmount;

    @Column(name = "employer_type")
    private String employerType;

    @Column(name = "notes", columnDefinition="varchar(400)")
    private String notes;

    @Column(name = "retirement_type")
    private String retirementType;

    @Column(name = "retirement_start_date")
    private Date retirementStartDate;

    @Column(name = "retirement_name")
    private String retirementName;

    @Column(name = "retirement_vat")
    private String retirementVat;

    @Column(name = "retirement_fiscal_code")
    private String retirementFiscalCode;

    @Column(name = "retirement_legal_address")
    private String retirementLegalAddress;

    @Column(name = "retirement_legal_indirizzo_civico")
    private String retirementLegalCivicAddress;

    @Column(name = "retirement_headquarter")
    private String retirementHeadquarter;

    @Column(name = "retirement_headquarter_indirizzo_civico")
    private String retirementHeadquarterCivicAddress;

    @Column(name = "retirement_amount")
    private BigDecimal retirementAmount;

    @Column(name = "retirement_notes", columnDefinition="varchar(400)")
    private String retirementNotes;
    
    @Column(name = "gross_amount")
    private BigDecimal grossAmount;
    
    @Column(name = "last_date")
    private Date lastDate;
    
    @Column(name = "employer_legal_address_city")
    private String employerLegalAddressCity;
    
    @Column(name = "employer_legal_address_province")
    private String employerLegalAddressProvince;
    
    @Column(name = "employer_legal_address_cap")
    private String employerLegalAddressCap;
    
    @Column(name = "cell_legal_address")
    private String cellLegalAddress;
    
    @Column(name = "cell2_legal_address")
    private String cell2LegalAddress;
    
    @Column(name = "fax_legal_address")
    private String faxLegalAddress;
    
    @Column(name = "employer_headquarter")
    private String employerHeadquarter;

    @Column(name = "employer_headquarter_indirizzo_civico")
    private String employerHeadquarterCivicAddress;

    @Column(name = "employer_headquarter_city")
    private String employerHeadquarterCity;
    
    @Column(name = "employer_headquarter_province")
    private String employerHeadquarterProvince;
    
    @Column(name = "employer_headquarter_cap")
    private String employerHeadquarterCap;
    
    @Column(name = "cell_headquarter")
    private String cellHeadquarter;
    
    @Column(name = "fax_headquarter")
    private String faxHeadquarter;
    
    @Column(name = "employee")
    private String employee;
    
    @Column(name = "expiration_contract")
    private String expirationContract;
    
    @Column(name = "salary")
    private BigDecimal salary;
    
    @Column(name = "unemployed")
    private String unemployed;
    
    @Column(name = "professional")
    private String professional;
    
    @Column(name = "function")
    private String function;

    @Column(name = "retirement_transfer_note")
    private String retirementTransferNote;

    @Column(name = "retirement_transfer_progress")
    private Boolean retirementTransferProgress;

    @Column(name = "retirement_transfer_amount")
    private BigDecimal retirementTransferAmount;

    @Column(name = "retirement_transfer_end")
    private Date retirementTransferEnd;

    @Column(name = "work_transfer_note")
    private String workTransferNote;

    @Column(name = "work_transfer_progress")
    private Boolean workTransferProgress;

    @Column(name = "work_transfer_amount")
    private BigDecimal workTransferAmount;

    @Column(name = "work_transfer_end")
    private Date workTransferEnd;

    @Column(name = "distraint_retirement_note")
    private String distraintRetirementNote;

    @Column(name = "distraint_retirement_progress")
    private Boolean distraintRetirementProgress;

    @Column(name = "distraint_retirement_amount")
    private BigDecimal distraintRetirementAmount;

    @Column(name = "distraint_retirement_end")
    private Date distraintRetirementEnd;

    @Column(name = "distraint_work_note")
    private String distraintWorkNote;

    @Column(name = "distraint_work_progress")
    private Boolean distraintWorkProgress;

    @Column(name = "distraint_work_amount")
    private BigDecimal distraintWorkAmount;

    @Column(name = "distraint_work_end")
    private Date distraintWorkEnd;

    @Column(name = "employer_legal_address_status")
    private String employerLegalAddressStatus;

    @Column(name = "employer_headquarter_status")
    private String employerHeadquarterStatus;

    @Column(name = "employer_fiscal_code")
    private String employerFiscalCode;

    private Boolean retired;

    @Column(name = "retirement_legal_address_city")
    private String retirementLegalAddressCity;

    @Column(name = "retirement_legal_address_province")
    private String retirementLegalAddressProvince;

    @Column(name = "retirement_legal_address_cap")
    private String retirementLegalAddressCap;

    @Column(name = "retirement_legal_address_status")
    private String retirementLegalAddressStatus;

    @Column(name = "retirement_headquarter_city")
    private String retirementHeadquarterCity;

    @Column(name = "retirement_headquarter_province")
    private String retirementHeadquarterProvince;

    @Column(name = "retirement_headquarter_cap")
    private String retirementHeadquarterCap;

    @Column(name = "retirement_headquarter_status")
    private String retirementHeadquarterStatus;

    private String type;

}
