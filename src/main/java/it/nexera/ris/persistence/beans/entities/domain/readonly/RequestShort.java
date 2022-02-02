package it.nexera.ris.persistence.beans.entities.domain.readonly;

import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "request")
public class RequestShort extends IndexedEntity {

    private static final long serialVersionUID = -5178936510302251225L;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_type_id")
    private RequestType requestType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aggregation_land_char_reg_id")
    private AggregationLandChargesRegistry aggregationLandChargesRegistry;

    @ManyToMany
    @JoinTable(name = "request_service", joinColumns = {
            @JoinColumn(name = "request_id", table = "request")
    }, inverseJoinColumns = {
            @JoinColumn(name = "service_id", table = "dic_service")
    })
    private List<Service> multipleServices;
    
    public String getCreateDateStr() {
        return DateTimeHelper.toString(getCreateDate());
    }

    public String getClientName() {
        return getClient() == null ? "" : getClient().toString();
    }

    public String getServiceName() {
        return getService() == null ? "" : getService().toString();
    }

    public String getAggregationLandChargesRegistryName() {
        return getAggregationLandChargesRegistry() == null ? "" : getAggregationLandChargesRegistry().toString();
    }
}
