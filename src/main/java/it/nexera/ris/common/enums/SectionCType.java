package it.nexera.ris.common.enums;

public enum SectionCType {
    A_FAVORE("A favore"), CONTRO("Contro"), DEBITORI_NON_DATORI_DI_IPOTECA("Debitori non datori di ipoteca");

    private String name;

    SectionCType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static SectionCType getByName(String currentName) {
        for (SectionCType type : SectionCType.values()) {
            if (type.getName().equals(currentName)) {
                return type;
            }
        }
        return null;
    }

    public static SectionCType getByEstateFormalityType(EstateFormalityType estateFormalityType) {
        switch (estateFormalityType) {
            case FORMALITY_TYPE_F:
                return A_FAVORE;
            case FORMALITY_TYPE_C:
                return CONTRO;
            default:
                return null;
        }
    }
}
