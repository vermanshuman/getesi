package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.ITreeNode;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.WLGInbox;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "dic_office")
public class Office extends Dictionary implements ITreeNode {

    private static final long serialVersionUID = -2232071408269490677L;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    @OneToMany(mappedBy = "office", cascade = CascadeType.REMOVE)
    private List<Sublevel> sublevels;

    @OneToMany(mappedBy = "office")
    private List<Client> clients;

    @OneToMany(mappedBy = "office")
    private List<WLGInbox> wlgInboxList;

    @ManyToMany(mappedBy = "offices")
    private List<Client> clientsForManyOffices;

    @Override
    public boolean isChild(IEntity parent) {
        return getArea().getId().equals(parent.getId());
    }

    @Override
    public String getIcon() {
        return "/resources/images/office.png";
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public List<Sublevel> getSublevels() {
        return sublevels;
    }

    public void setSublevels(List<Sublevel> sublevels) {
        this.sublevels = sublevels;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public List<Client> getClientsForManyOffices() {
        return clientsForManyOffices;
    }

    public void setClientsForManyOffices(List<Client> clientsForManyOffices) {
        this.clientsForManyOffices = clientsForManyOffices;
    }

    public List<WLGInbox> getWlgInboxList() {
        return wlgInboxList;
    }

    public void setWlgInboxList(List<WLGInbox> wlgInboxList) {
        this.wlgInboxList = wlgInboxList;
    }
}
