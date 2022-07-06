package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.persistence.beans.entities.domain.Document;

import java.util.Comparator;

public class DocumentTypeRequestReportComparator implements Comparator<Document> {
    @Override
    public int compare(Document a, Document b) {
        if (DocumentType.REQUEST_REPORT.getId().equals(a.getTypeId()) && !DocumentType.REQUEST_REPORT.getId().equals(b.getTypeId())) {
            return -1;
        } else if (DocumentType.REQUEST_REPORT.getId().equals(b.getTypeId()) && !DocumentType.REQUEST_REPORT.getId().equals(a.getTypeId())) {
            return 1;
        } else {
            return 0;
        }
    }
}