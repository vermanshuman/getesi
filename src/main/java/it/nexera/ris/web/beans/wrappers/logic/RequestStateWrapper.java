package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.RequestState;

import java.io.Serializable;

public class RequestStateWrapper implements Serializable {

    private static final long serialVersionUID = 8537903316367693957L;

    private Long id;

    private String value;

    private Boolean selected;

    private RequestState state;

    public RequestStateWrapper(RequestState state) {
        this.state = state;
        this.id = state.getId();
        this.value = state.toString();
    }

    public RequestStateWrapper(Boolean selected, RequestState state) {
        this.selected = selected;
        this.state = state;
        this.id = state.getId();
        this.value = state.toString();
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

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) {
        this.state = state;
    }

}
