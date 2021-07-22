package it.nexera.ris.common.xml.wrappers.importXLSX;

import it.nexera.ris.common.enums.LandChargesRegistryType;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.*;
import java.util.stream.Collectors;

public class FormalityXLSXWrapper {

    private DocumentXLSXWrapper document;

    private String type;

    private Double particularRegister;

    private Date presentationDate;

    private String conservatory;

    private SectionAXLSXWrapper sectionA;

    private List<SectionCXLSXWrapper> sectionCList;

    private String sectionATextWrap;

    public FormalityXLSXWrapper() {
        this.sectionCList = new ArrayList<>();
    }

    public Formality toEntity(Session session) throws IllegalAccessException, InstantiationException {
        Formality formality = new Formality();
        formality.setType(getType());
        formality.setSectionAText(getSectionATextWrap());
        if (!ValidationHelper.isNullOrEmpty(getParticularRegister())) {
            formality.setParticularRegister(Integer.toString(getParticularRegister().intValue()));
        }
        formality.setPresentationDate(getPresentationDate());


        if(!ValidationHelper.isNullOrEmpty(getConservatory())) {
            char lastChar = getConservatory().charAt(getConservatory().length() - 1);
            if(Character.isDigit(lastChar)) {
                setConservatory(getConservatory().replaceAll(String.valueOf(lastChar),(" " + lastChar)));
            }
        }

        formality.setReclamePropertyService(ConnectionManager.get(LandChargesRegistry.class, new Criterion[]{
                Restrictions.eq("name", getConservatory()),
                Restrictions.eq("type", LandChargesRegistryType.CONSERVATORY)
        }, session));

        return formality;
    }

    public Formality toEntityExists(Session session) throws InstantiationException, IllegalAccessException {
        List<Long> documentIds = this.getDocument().getEntity(session).stream().map(IndexedEntity::getId)
                .collect(Collectors.toList());

        List<Formality> formalityList = ConnectionManager.load(Formality.class, new Criterion[]{
                Restrictions.eq("particularRegister", String.valueOf(getParticularRegister().intValue())),
                Restrictions.eq("type", getType()),
                Restrictions.in("document.id", documentIds),
        }, session);

        if(!ValidationHelper.isNullOrEmpty(formalityList)) {
            return formalityList.get(0);
        }
        return toEntity(session);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormalityXLSXWrapper that = (FormalityXLSXWrapper) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(particularRegister, that.particularRegister) &&
                Objects.equals(presentationDate, that.presentationDate) &&
                Objects.equals(conservatory, that.conservatory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, particularRegister, presentationDate, conservatory);
    }

    public DocumentXLSXWrapper getDocument() {
        return document;
    }

    public void setDocument(DocumentXLSXWrapper document) {
        this.document = document;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getParticularRegister() {
        return particularRegister;
    }

    public void setParticularRegister(Double particularRegister) {
        this.particularRegister = particularRegister;
    }

    public Date getPresentationDate() {
        return presentationDate;
    }

    public void setPresentationDate(Date presentationDate) {
        this.presentationDate = presentationDate;
    }

    public String getConservatory() {
        return conservatory;
    }

    public void setConservatory(String conservatory) {
        this.conservatory = conservatory;
    }

    public SectionAXLSXWrapper getSectionA() {
        return sectionA;
    }

    public void setSectionA(SectionAXLSXWrapper sectionA) {
        this.sectionA = sectionA;
    }

    public List<SectionCXLSXWrapper> getSectionCList() {
        return sectionCList;
    }

    public void setSectionCList(List<SectionCXLSXWrapper> sectionCList) {
        this.sectionCList = sectionCList;
    }

    public String getSectionATextWrap() {
        return sectionATextWrap;
    }

    public void setSectionATextWrap(String sectionATextWrap) {
        this.sectionATextWrap = sectionATextWrap;
    }
}
