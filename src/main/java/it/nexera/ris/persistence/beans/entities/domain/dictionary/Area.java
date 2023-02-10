package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.ITreeNode;
import it.nexera.ris.persistence.beans.entities.domain.Client;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "dic_area")
public class Area extends Dictionary implements ITreeNode {

    private static final long serialVersionUID = -6693752517580879441L;

    @OneToMany(mappedBy = "area", cascade = CascadeType.REMOVE)
    private List<Office> offices;

    @OneToMany(mappedBy = "area")
    private List<Client> clients;

    @ManyToMany(mappedBy = "areas")
    private List<Client> clientsForManyAreas;

    @Column(name = "external_brexa")
    private Boolean externalBrexa;

    @Override
    public boolean isChild(IEntity parent) {
        return false;
    }

    @Override
    public String getIcon() {
        return "/resources/images/area.png";
    }

    public List<Office> getOffices() {
        return offices;
    }

    public void setOffices(List<Office> offices) {
        this.offices = offices;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public List<Client> getClientsForManyAreas() {
        return clientsForManyAreas;
    }

    public void setClientsForManyAreas(List<Client> clientsForManyAreas) {
        this.clientsForManyAreas = clientsForManyAreas;
    }

    public Boolean getExternalBrexa() {
        return externalBrexa;
    }

    public void setExternalBrexa(Boolean externalBrexa) {
        this.externalBrexa = externalBrexa;
    }
}
