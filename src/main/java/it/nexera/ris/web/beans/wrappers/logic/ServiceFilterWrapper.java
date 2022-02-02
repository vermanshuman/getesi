package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;

public class ServiceFilterWrapper implements Serializable {

    private static final long serialVersionUID = 5622227676149662187L;

    private Long id;

    private String value;

    private Boolean selected;

    private Service service;

    public ServiceFilterWrapper(Service service) {
        this.service = service;
        this.id = service.getId();
        this.value = service.toString();
    }

    public ServiceFilterWrapper(Boolean selected, Service service) {
        this.selected = selected;
        this.service = service;
        this.id = service.getId();
        this.value = service.toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getSelected() {
        return selected == null ? Boolean.FALSE : selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
