package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum NoteType {

    NOTE_TYPE_T(1L, "T"),
    NOTE_TYPE_I(2L, "I"),
    NOTE_TYPE_A(3L, "A");

    private Long id;

    private String shortCode;

    private NoteType(Long id, String shortCode) {
        this.id = id;
        this.shortCode = shortCode;
    }

    public static NoteType findById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (NoteType noteType : NoteType.values()) {
                if (noteType.getId().equals(id)) {
                    return noteType;
                }
            }
        }

        return null;
    }

    public static NoteType getEnumByString(String code) {
        for (NoteType e : NoteType.values()) {
            if (code.equals(e.getShortCode())) return e;
        }
        return null;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public Long getId() {
        return id;
    }

    public String getShortCode() {
        return shortCode;
    }
}