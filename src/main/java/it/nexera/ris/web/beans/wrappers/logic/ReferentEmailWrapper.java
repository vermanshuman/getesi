package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.persistence.beans.entities.domain.Referent;

import java.io.Serializable;

public class ReferentEmailWrapper implements Serializable {

    private static final long serialVersionUID = 3351414554190071010L;

    private Referent referent;

    private Boolean selected;

    private Boolean deleted;

    public ReferentEmailWrapper(Referent referent) {
        this.referent = referent;
    }

    public Referent getReferent() {
        return referent;
    }

    public void setReferent(Referent referent) {
        this.referent = referent;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
