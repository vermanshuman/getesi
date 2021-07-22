package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.ManageTypeFields;
import it.nexera.ris.common.enums.ManageTypeFieldsState;
import it.nexera.ris.persistence.beans.entities.domain.InputCardManageField;

public class InputCardFieldWrapper {

    private InputCardManageField field;

    private Boolean selected;

    public InputCardFieldWrapper(InputCardManageField field) {
        this.selected = true;
        this.field = field;
    }

    public InputCardFieldWrapper(ManageTypeFields field) {
        InputCardManageField manageField = new InputCardManageField();
        manageField.setField(field);
        manageField.setState(ManageTypeFieldsState.HIDDEN);
        this.field = manageField;
    }

    public InputCardManageField getField() {
        return field;
    }

    public void setField(InputCardManageField field) {
        this.field = field;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
