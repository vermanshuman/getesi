package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.Invoice;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
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

    @Transient
    private String dateString;

    public String getDateString() {
        if(getDate() != null) {
            return DateTimeHelper.toFormatedStringLocal(getDate(),
                    DateTimeHelper.getDatePattern(), null);
        }
        return dateString;
    }
}