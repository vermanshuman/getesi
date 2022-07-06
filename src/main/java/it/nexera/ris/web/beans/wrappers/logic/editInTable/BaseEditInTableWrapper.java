package it.nexera.ris.web.beans.wrappers.logic.editInTable;

import it.nexera.ris.persistence.beans.entities.domain.Relationship;

public class BaseEditInTableWrapper {

    private Long id;

    private boolean edited;

    private String comment;

    public BaseEditInTableWrapper(Long id, String comment) {
        this.id = id;
        this.comment = comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        this.edited = true;
    }

    public Long getId() {
        return id;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public String getComment() {
        return comment;
    }
}
