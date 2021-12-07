package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.ITreeNode;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "dic_sublevel")
public class Sublevel extends Dictionary implements ITreeNode {

    private static final long serialVersionUID = -1258980887899360107L;

    @ManyToOne
    @JoinColumn(name = "office_id")
    private Office office;

    @ManyToOne
    @JoinColumn(name = "previous_sublevel_id")
    private Sublevel previousSublevel;

    @OneToMany(mappedBy = "previousSublevel", cascade = CascadeType.REMOVE)
    private List<Sublevel> childSublevels;

    @Override
    public boolean isChild(IEntity parent) {
        if (parent instanceof Office && getOffice() != null) {
            return getOffice().getId().equals(parent.getId());
        } else if (parent instanceof Sublevel && getPreviousSublevel() != null) {
            return getPreviousSublevel().getId().equals(parent.getId());
        } else {
            return false;
        }
    }

    @Override
    public String getIcon() {
        return "/resources/images/sublevel.png";
    }

    public Office getOffice() {
        return office;
    }

    public Sublevel getPreviousSublevel() {
        return previousSublevel;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public void setPreviousSublevel(Sublevel previousSublevel) {
        this.previousSublevel = previousSublevel;
    }

    public List<Sublevel> getChildSublevels() {
        return childSublevels;
    }

    public void setChildSublevels(List<Sublevel> childSublevels) {
        this.childSublevels = childSublevels;
    }
}
