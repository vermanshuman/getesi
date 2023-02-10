package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ImportF24Pdf;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "transcription_data")
public class TranscriptionData extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -1185580832059922657L;

    @Column(name = "import_f24")
    private Double transcriptionAmount;

    @Column(name = "number_revenue_stamp")
    private Integer numberRevenueStamp;

    @Column(name = "number_service_stamp")
    private Integer numberServiceStamp ;

    @Column(name = "number_plico")
    private Integer packetNumber ;

    @Column(name = "envelop_stamp")
    private Boolean envelopStamp ;

    @Column(name = "user_phone")
    private Boolean userPhone ;

    private Date expiration;

    @Column(name = "courier_plico")
    private Boolean courierEnvelope ;

    @Column(name = "date_courier_plico")
    private Date courierEnvelopeDate;

    @Column(name = "expiration_note")
    private Date expirationNote;

    @Column(name = "expiration_duplo")
    private Date expirationDuplo;

    @Column(name = "courier_client")
    private Boolean courierClient ;

    @Column(name = "expiration_courier_client")
    private Date expirationCourierClient;

    @Column(name = "cadastral_assessment")
    private Double cadastralAssessment;

    @Column(name = "mortgage_investigation")
    private Double mortgageInvestigation;

    @Column(name = "personal_certificate")
    private Double personalCertificate;

    @Column(name = "postalCosts")
    private Double postalCosts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "courier_document_id")
    private Document courierDocument;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "entry_document_id")
    private Document entryDocument;
    
	@Column(name = "support_type")
	private Long supportType;

    @OneToMany(mappedBy = "transcriptionData", cascade = CascadeType.REFRESH)
    private List<ImportF24Pdf> importF24Pdfs;
    
    @Column(name = "payment_date")
    private Date paymentDate;
    
    @Column(name = "email_sent_date")
    private Date emailSentDate;

    @Column(name = "anticipated_f24")
    private Boolean anticipatedF24;

    @Column(name = "import_f24_anticipated")
    private Double importF24Anticipated;

    @Column(name = "text", columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "authorization_customer")
    private Boolean authorizationCustomer;
    
    @Column(name = "authorization_email")
    private Date authorizationEmail;

    @Column(name = "manual_f24")
    private Boolean manualF24;

    @Column(name = "duplo_email_date")
    private Date duploEmailDate;

    @Column(name = "courier_email_date")
    private Date courierEmailDate;

}
