package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

import java.util.Arrays;
import java.util.List;

public enum DocumentType {
    FORMALITY(1l, ""),
    CADASTRAL(3l, "Cadastral"),
    ESTATE_FORMALITY(5L, "SyntheticFormality"),
    REQUEST_REPORT(6L, ""),
    INDIRECT_CADASTRAL(7L, ""),
    ALLEGATI(8L, ""),
    INDIRECT_CADASTRAL_REQUEST(9L, ""),
    INVOICE_REPORT(11L, ""),
    INVOICE(12L, ""),
    COURTESY_INVOICE(13L, ""),
    OTHER(2l, "");

    private Long id;

    private String folderName;

    private DocumentType(Long id, String folderName) {
        this.id = id;
        this.folderName = folderName;
    }

    public static DocumentType getById(Long id) {
        DocumentType document = null;

        for (DocumentType type : DocumentType.values()) {
            if (type.getId().equals(id)) {
                document = type;

                break;
            }
        }

        return document;
    }

    public static List<Long> getOnlyEditorDocumentType() {
        return Arrays.asList(DocumentType.FORMALITY.getId(),
                DocumentType.CADASTRAL.getId(),
                DocumentType.ESTATE_FORMALITY.getId(),
                DocumentType.REQUEST_REPORT.getId());
    }

    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public Long getId() {
        return id;
    }

    public String getFolderName() {
        return folderName;
    }
}
