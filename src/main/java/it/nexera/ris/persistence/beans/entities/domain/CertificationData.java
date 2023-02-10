package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "certification_data")
public class CertificationData extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -6617736448504128338L;

    private Date expiration;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "cadastral_document_id")
    private Document cadastralDocument;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "unified_document_id")
    private Document mapDocument;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "signed_certification_document_id")
    private Document signedCertificationDocument;

    @Column(name = "sending_courier")
    private Boolean sendingCourier;

    @Column(name = "sending_courier_date")
    private Date sendingCourierDate;

    @Column(name = "sending_certification")
    private Boolean sendingCertification;

    @Column(name = "sending_certification_date")
    private Date sendingCertificationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @Column(name = "email_sent_date")
    private Date emailSentDate;
    
    @Column(name = "courier_email_date")
    private Date courierEmailDate;
}
