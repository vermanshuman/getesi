package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;

import javax.persistence.*;

@Entity
@Table(name = "client_service_info")
public class ClientServiceInfo extends IndexedEntity {

    private static final long serialVersionUID = -2493732219863519502L;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @Column(name = "days_to_expire")
    private Integer daysToExpire;
    
    @Column(name = "single_evasione_file")
    private Boolean singleEvasionFile;
    
    @Column(name = "visible")
    private Boolean visible;

    public ClientServiceInfo() {
    }

    public ClientServiceInfo(Client client, Service service, Integer daysToExpire) {
        this.client = client;
        this.service = service;
        this.daysToExpire = daysToExpire;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Integer getDaysToExpire() {
        return daysToExpire;
    }

    public void setDaysToExpire(Integer daysToExpire) {
        this.daysToExpire = daysToExpire;
    }

	public Boolean getSingleEvasionFile() {
		return singleEvasionFile;
	}

	public void setSingleEvasionFile(Boolean singleEvasionFile) {
		this.singleEvasionFile = singleEvasionFile;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}
    
}
