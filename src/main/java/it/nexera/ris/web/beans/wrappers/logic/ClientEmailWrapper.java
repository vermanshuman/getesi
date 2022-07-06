package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.persistence.beans.entities.domain.ClientEmail;

import java.io.Serializable;

public class ClientEmailWrapper implements Serializable {

    private static final long serialVersionUID = 9057511524874361233L;

    private ClientEmail clientEmail;

    private Boolean selected;

    private Boolean edit;

    private String previousEmail;

    private Boolean delete;

    private Long id;

    public ClientEmailWrapper(ClientEmail clientEmail) {
        this.clientEmail = clientEmail;
        this.previousEmail = clientEmail.getEmail();
    }

    public ClientEmailWrapper(ClientEmail clientEmail, Long id) {
        this.clientEmail = clientEmail;
        this.previousEmail = clientEmail.getEmail();
        this.id = id;
    }

    public ClientEmail getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(ClientEmail clientEmail) {
        this.clientEmail = clientEmail;
    }

    public Boolean getSelected() {
        return selected == null ? Boolean.FALSE : selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Boolean getEdit() {
        return edit == null ? Boolean.FALSE : edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public String getPreviousEmail() {
        return previousEmail;
    }

    public void setPreviousEmail(String previousEmail) {
        this.previousEmail = previousEmail;
    }

    public Boolean getDelete() {
        return delete == null ? Boolean.FALSE : delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
