package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum EstateFormalityType {

    FORMALITY_TYPE_C("C"),
    FORMALITY_TYPE_F("F"),
    FORMALITY_TYPE_E("E");

    private String xmlCode;

    private EstateFormalityType(String xmlCode) {
        this.xmlCode = xmlCode;
    }

    public static EstateFormalityType getEnumByCode(String code) {
        for (EstateFormalityType en : EstateFormalityType.values()) {
            if (code.equals(en.getXmlCode())) return en;
        }
        return null;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public String getXmlCode() {
        return xmlCode;
    }
}
