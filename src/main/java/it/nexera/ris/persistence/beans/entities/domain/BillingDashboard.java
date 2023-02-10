package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "billing_dashboard_data")
@NoArgsConstructor
@AllArgsConstructor

public class BillingDashboard extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -2353719548403091899L;

    private String revenue;

    private String unsolved;

    private Integer unLockedInvoicesCount;

    private String unLockedInvoicesTooltip;

    private Double monthJanFebAmount;

    private Double monthMarAprAmount;

    private Double monthMayJunAmount;

    private Double monthJulAugAmount;

    private Double monthSepOctAmount;

    private Double monthNovDecAmount;

    @Column(length = 65535,columnDefinition="Text")
    private String barChartData;
//    @OneToMany(mappedBy = "billingDashboard")
//    private List<BillingTurnoverData> billingTurnoverData;
//
//    @OneToMany(mappedBy = "billingDashboard")
//    private List<BillingTurnoverCustomerData> billingTurnoverPerCustomerData;
}
