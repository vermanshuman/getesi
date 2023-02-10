package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "billing_turnover_data")
@NoArgsConstructor
@AllArgsConstructor

public class BillingTurnoverData extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -6984701452357283791L;

    private Integer month;

    @Column(name = "total_tax")
    private double totalTax;

    @Column(name = "non_total_tax")
    private double nonTotalTax;

    @Column(name = "total_iva")
    private double totalIva;

    private double total;

    @Column(name = "client_name")
    private String clientName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_dashboard_data_id")
    private BillingDashboard billingDashboard;

}
