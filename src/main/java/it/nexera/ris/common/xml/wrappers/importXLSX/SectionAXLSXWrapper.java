package it.nexera.ris.common.xml.wrappers.importXLSX;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.SectionA;

public class SectionAXLSXWrapper {

    private FormalityXLSXWrapper formality;

    public SectionA toEntity(Formality formality) {
        SectionA sectionA = formality.getSectionA();
        if(ValidationHelper.isNullOrEmpty(sectionA)) {
            sectionA = new SectionA();
        }
        return sectionA;
    }

    public FormalityXLSXWrapper getFormality() {
        return formality;
    }

    public void setFormality(FormalityXLSXWrapper formality) {
        this.formality = formality;
    }
}
