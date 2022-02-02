package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;

public class RequestTypeFilterWrapper implements Serializable {

    private static final long serialVersionUID = 5622227676149662187L;

    private Long id;

    private String value;

    private Boolean selected;

    private RequestType requestType;

    public RequestTypeFilterWrapper(RequestType requestType) {
        this.requestType = requestType;
        this.id = requestType.getId();
        this.value = requestType.toString();
    }

    public RequestTypeFilterWrapper(Boolean selected, RequestType requestType) {
        this.selected = selected;
        this.requestType = requestType;
        this.id = requestType.getId();
        this.value = requestType.toString();
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

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }
}
