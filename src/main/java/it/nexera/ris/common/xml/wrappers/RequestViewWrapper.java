package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.common.enums.ManageTypeFields;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.InputCardManageField;

import java.util.List;

public class RequestViewWrapper {

    private boolean oneElement;

    private int lineNum;

    private List<InputCardManageField> fields;

    private InputCardManageField field;

    public boolean isOneElement() {
        return oneElement;
    }

    public void setOneElement(boolean oneElement) {
        this.oneElement = oneElement;
    }

    public boolean getLineEven() {
        return lineNum % 2 == 0;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public List<InputCardManageField> getFields() {
        return fields;
    }

    public void setFields(List<InputCardManageField> fields) {
        this.fields = fields;
    }

    public InputCardManageField getField() {
        return field;
    }

    public void setField(InputCardManageField field) {
        this.field = field;
    }

    public boolean isAttachedDocumentsBlock() {
        if (!ValidationHelper.isNullOrEmpty(getFields()) && !ValidationHelper.isNullOrEmpty(getFields().get(0))) {
            if (getFields().get(0).getField().getEnumPropsWrapper().getId()
                    .equals(ManageTypeFields.ATTACHED_DOCUMENTS.getEnumPropsWrapper().getId())) {
                return true;
            }
        }
        return false;
    }
}
