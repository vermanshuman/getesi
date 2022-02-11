package it.nexera.ris.common.xml.wrappers.importXLSX;

import it.nexera.ris.persistence.beans.entities.domain.SectionC;

import java.util.ArrayList;
import java.util.List;

public class SectionCXLSXWrapper {

    private FormalityXLSXWrapper formality;

    private String sectionCType;

    private SubjectXLSXWrapper subject;

    public List<SectionC> toEntity() {
        List<SectionC> sectionCList = new ArrayList<>();
        if (getSectionCType().equalsIgnoreCase("F/C")) {
            SectionC sectionC = new SectionC();
            sectionC.setSectionCType("A favore");
            sectionCList.add(sectionC);
            sectionC = new SectionC();
            sectionC.setSectionCType("Contro");
            sectionCList.add(sectionC);
        } else {
            SectionC sectionC = new SectionC();
            sectionC.setSectionCType(getSectionCType().equalsIgnoreCase("F") ? "A favore" : "Contro");
            sectionCList.add(sectionC);
        }

        return sectionCList;
    }

    public FormalityXLSXWrapper getFormality() {
        return formality;
    }

    public void setFormality(FormalityXLSXWrapper formality) {
        this.formality = formality;
    }

    public String getSectionCType() {
        return sectionCType;
    }

    public void setSectionCType(String sectionCType) {
        this.sectionCType = sectionCType;
    }

    public SubjectXLSXWrapper getSubject() {
        return subject;
    }

    public void setSubject(SubjectXLSXWrapper subject) {
        this.subject = subject;
    }
}
