package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;

public class BaseEnumWrapper implements Serializable {

    private static final long serialVersionUID = 1941569584235959815L;

    private Long id;

    private String value;

    private Boolean selected;

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
}
