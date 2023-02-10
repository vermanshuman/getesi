package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "tracking_position")
public class TrackingPosition extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = 1626591094467440121L;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "position_active")
    private String positionActive ;

    @Column(name = "position_vat")
    private String positionVat;

    @Column(name = "position_name")
    private String positionName;

    @Column(name = "position_legal_address")
    private String positionLegalAddress;

    @Column(name = "position_type")
    private String positionType;

    @Column(name = "notes", columnDefinition="varchar(400)")
    private String notes;
    
    @Column(name = "socio")
    private Boolean socio;
    
    @Column(name = "giuridic_nature")
    private String giuridicNature;
    
    @Column(name = "start_date")
    private Date startDate;
    
    @Column(name = "status_position")
    private String statusPosition;
    
    @Column(name = "code")
    private String code;
    
    @Column(name = "date_nomination")
    private Date dateNomination;
    
    @Column(name = "stopped")
    private Boolean stopped;

    @Column(name = "status_society")
    private String statusSociety;

    @Column(name = "participation_vat")
    private String participationVat;

    @Column(name = "participation_name")
    private String participationName;

    @Column(name = "participation_active")
    private String participationActive;

    @Column(name = "participation_status")
    private String participationStatus;

    @Column(name = "participation_socio")
    private Boolean participationSocio;
}
