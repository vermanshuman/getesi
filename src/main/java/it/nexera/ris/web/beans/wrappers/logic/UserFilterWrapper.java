package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.persistence.beans.entities.domain.User;

import java.io.Serializable;

public class UserFilterWrapper implements Serializable {

    private static final long serialVersionUID = 5935647740923512056L;

    private Long id;

    private String value;

    private Boolean selected;

    private User user;

    public UserFilterWrapper(User user) {
        this.user = user;
        this.id = user.getId();
        this.value = user.toString();
    }

    public UserFilterWrapper(Boolean selected, User user) {
        this.selected = selected;
        this.user = user;
        this.id = user.getId();
        this.value = user.toString();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
