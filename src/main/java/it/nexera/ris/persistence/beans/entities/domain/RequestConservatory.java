package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "request_conservatory")
public class RequestConservatory extends IndexedEntity {

    private static final long serialVersionUID = 4382910765647651776L;

    @Column(name = "conservatory_date")
    private Date conservatoryDate;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne
    @JoinColumn(name = "conservatory_id")
    private LandChargesRegistry registry;

    public Date getConservatoryDate() {
        return conservatoryDate;
    }

    public void setConservatoryDate(Date conservatoryDate) {
        this.conservatoryDate = conservatoryDate;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public LandChargesRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(LandChargesRegistry registry) {
        this.registry = registry;
    }

    public String getConservatoryDateStr() {
        return DateTimeHelper.toString(getConservatoryDate());
    }
}
