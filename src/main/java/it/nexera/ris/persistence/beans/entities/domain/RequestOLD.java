package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(name = "requestOLD")
@Data
public class RequestOLD implements Serializable {

    private static final long serialVersionUID = -4283281481282464162L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "fiscalcode_vat")
    private String fiscalCodeVat;

    @Column(name = "client")
    private String client;

    @OneToOne
    @JoinColumn(name = "id_land_registry")
    private LandChargesRegistry landChargesRegistry;

    @Column(name = "type")
    private String type;

    @Column(name = "evasion_date")
    private Date evasionDate;

    @Column(name = "request_date")
    private Date requestDate;

    @Column(name = "num_formality")
    private Long numFormality;

    public String getRequestDateString() {
        return DateTimeHelper.toString(getRequestDate());
    }
}