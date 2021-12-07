package it.nexera.ris.common.enums;

import java.util.Arrays;

public enum PropertyTypeEnum {
    NUMBER_1("PIENA PROPRIETA'"),
    NUMBER_1S("PROPRIETA'  SUPERFICIARIA"),
    NUMBER_1T("PROPRIETA'  PER L'AREA"),
    NUMBER_2("NUDA PROPRIETA'"),
    NUMBER_2S("NUDA PROPRIETA' SUPERFICIARIA"),
    NUMBER_3("ABITAZIONE"),
    NUMBER_3S("ABITAZIONE SU PROPRIETA' SUPERFICIARIA"),
    NUMBER_4("DIRITTO DEL CONCEDENTE"),
    NUMBER_5("DIRITTO DELL'ENFITEUTA"),
    NUMBER_6("SUPERFICIE"),
    NUMBER_7("USO"),
    NUMBER_7S("USO SU PROPRIETA' SUPERFICIARIA"),
    NUMBER_8("USUFRUTTO"),
    NUMBER_8S("USUFRUTTO SU PROPRIETA' SUPERFICIARIA"),
    NUMBER_9("SERVITU'");

    private String description;

    PropertyTypeEnum(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    public static PropertyTypeEnum getByDescription(String description) {
        return Arrays.stream(PropertyTypeEnum.values()).filter(d -> d.getDescription().equals(description))
                .findAny().orElse(null);
    }

    public static boolean isPresent(String description) {
        return Arrays.stream(PropertyTypeEnum.values()).anyMatch(d -> d.getDescription().equals(description));
    }

    public String getDescription() {
        return description;
    }
}
