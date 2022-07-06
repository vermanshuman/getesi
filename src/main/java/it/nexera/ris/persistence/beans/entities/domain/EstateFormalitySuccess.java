package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.NoteType;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "estate_formality_success")
public class EstateFormalitySuccess extends IndexedEntity {

    private static final long serialVersionUID = 863893897426501718L;

    @Column(name = "description")
    private String description;

    @Column(name = "date")
    private Date date;

    @Column(name = "num_rp")
    private String numRP;

    @Column(name = "num_rpb")
    private Integer numRPB;

    @Column(name = "act_code")
    private Integer actCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type")
    private NoteType noteType;

    @Column(name = "year")
    private Date year;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "estate_formality_id")
    private EstateFormality estateFormality;

    public EstateFormalitySuccess() {
    }

    public EstateFormalitySuccess(EstateFormalitySuccess formalitySuccess) {
        this.description = formalitySuccess.getDescription();
        this.date = formalitySuccess.getDate();
        this.numRP = formalitySuccess.getNumRP();
        this.numRPB = formalitySuccess.getNumRPB();
        this.actCode = formalitySuccess.getActCode();
        this.noteType = formalitySuccess.getNoteType();
        this.year = formalitySuccess.getYear();
    }

    public boolean isEmpty() {
        if (!ValidationHelper.isNullOrEmpty(description)) return false;
        if (!ValidationHelper.isNullOrEmpty(date)) return false;
        if (!ValidationHelper.isNullOrEmpty(numRP)) return false;
        if (!ValidationHelper.isNullOrEmpty(numRPB)) return false;
        if (!ValidationHelper.isNullOrEmpty(actCode)) return false;
        if (!ValidationHelper.isNullOrEmpty(noteType)) return false;
        if (!ValidationHelper.isNullOrEmpty(year)) return false;
        return true;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstateFormalitySuccess estateFormalitySuccess = (EstateFormalitySuccess) o;
        if (actCode != null ? !actCode.equals(estateFormalitySuccess.getActCode()) : estateFormalitySuccess.actCode != null) return false;
        if (date != null ? !date.equals(estateFormalitySuccess.date) : estateFormalitySuccess.date != null) return false;
        if (noteType != null ? !noteType.equals(estateFormalitySuccess.noteType) : estateFormalitySuccess.noteType != null) return false;
        if (numRP != null ? !numRP.equals(estateFormalitySuccess.numRP) : estateFormalitySuccess.numRP != null) return false;
        if (estateFormality != null ? !estateFormality.getId().equals(estateFormalitySuccess.estateFormality != null ? estateFormalitySuccess.estateFormality.getId() : null) : (estateFormalitySuccess.estateFormality != null && estateFormalitySuccess.estateFormality.getId() != null)) return false;
        
        return numRPB != null ? !numRPB.equals(estateFormalitySuccess.numRPB) : estateFormalitySuccess.numRPB != null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNumRP() {
        return numRP;
    }

    public void setNumRP(String numRP) {
        this.numRP = numRP;
    }

    public Integer getNumRPB() {
        return numRPB;
    }

    public void setNumRPB(Integer numRPB) {
        this.numRPB = numRPB;
    }

    public Integer getActCode() {
        return actCode;
    }

    public void setActCode(Integer actCode) {
        this.actCode = actCode;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public void setNoteType(NoteType noteType) {
        this.noteType = noteType;
    }

    public EstateFormality getEstateFormality() {
        return estateFormality;
    }

    public void setEstateFormality(EstateFormality estateFormality) {
        this.estateFormality = estateFormality;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getYear() {
        return year;
    }

    public void setYear(Date year) {
        this.year = year;
    }
}
