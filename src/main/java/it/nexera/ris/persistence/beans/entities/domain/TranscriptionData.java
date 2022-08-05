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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_document_id")
    private Document courierDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_document_id")
    private Document entryDocument;
}
