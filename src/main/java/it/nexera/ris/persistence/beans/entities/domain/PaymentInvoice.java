package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "payment_invoice")
@Getter
@Setter
public class PaymentInvoice extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -2859048180366332022L;

    @Column(name = "import")
    private Double paymentImport;

    @Column(name = "date")
    private Date date;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

}