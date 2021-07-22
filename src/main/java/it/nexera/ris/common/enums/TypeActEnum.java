package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

import java.util.Arrays;

public enum TypeActEnum {

    TYPE_A("A", "ANN"),
    TYPE_I("I", "IS"),
    TYPE_PS("PS", "PS"),
    TYPE_T("T", "TR"),
    TYPE_PA("PA", "PA");

    private String xmlValue;

    private String editorValue;

    TypeActEnum(String xmlValue, String editorValue) {
        this.xmlValue = xmlValue;
        this.editorValue = editorValue;
    }

    public static TypeActEnum getByStr(String value) {
        return Arrays.stream(TypeActEnum.values()).filter(act -> act.getXmlValue().equalsIgnoreCase(value))
                .findFirst().orElse(null);
    }

    public String getEditorValue() {
        return editorValue;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public String getXmlValue() {
        return xmlValue;
    }
}
