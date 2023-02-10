package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.persistence.beans.entities.domain.Request;
import java.io.Serializable;

public class RequestFilterWrapper implements Serializable {

    private static final long serialVersionUID = 5622227776149662187L;

    private Long id;

    private String value;

    private Boolean selected;

    private Request request;

    public RequestFilterWrapper(Request request) {
        this.request = request;
        this.id = request.getId();
        this.value = request.getService().getName();
    }

    public RequestFilterWrapper(Request request, String name) {
        this.request = request;
        this.id = request.getId();
        this.value = name;
    }

    public RequestFilterWrapper(Boolean selected, Request request) {
        this.selected = selected;
        this.request = request;
        this.id = request.getId();
        this.value = request.getService().getName();
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

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
