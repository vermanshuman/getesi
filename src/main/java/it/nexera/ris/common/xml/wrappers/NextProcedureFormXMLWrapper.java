package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.common.enums.NextProcedureFormXMLElements;
import it.nexera.ris.common.enums.NoteType;
import it.nexera.ris.common.enums.XMLElements;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.EstateFormalitySuccess;
import org.hibernate.HibernateException;

public class NextProcedureFormXMLWrapper
        extends BaseXMLWrapper<EstateFormalitySuccess> {

    private String description;

    private String date;

    private String numRP;

    private String numRPB;

    private String actCode;

    private String noteType;

    private String year;

    @Override
    public EstateFormalitySuccess toEntity()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        EstateFormalitySuccess nextProcedureForm = new EstateFormalitySuccess();

        nextProcedureForm.setId(getId());
        nextProcedureForm.setCreateUserId(getCreateUserId());
        nextProcedureForm.setUpdateUserId(getUpdateUserId());
        nextProcedureForm.setCreateDate(getCreateDate());
        nextProcedureForm.setUpdateDate(getUpdateDate());
        nextProcedureForm.setVersion(getVersion());
        nextProcedureForm.setDate(DateTimeHelper.fromXMLStringDate(getDate()));
        nextProcedureForm.setActCode(ValidationHelper.isNullOrEmpty(getActCode()) ? null : Integer.parseInt(getActCode()));
        nextProcedureForm.setYear(DateTimeHelper.fromXMLStringYear(getYear()));
        nextProcedureForm.setNoteType(ValidationHelper.isNullOrEmpty(getNoteType()) ? null : NoteType.getEnumByString(getNoteType()));
        nextProcedureForm.setNumRP(getNumRP());
        nextProcedureForm.setNumRPB(ValidationHelper.isNullOrEmpty(getNumRPB()) ? null : Integer.parseInt(getNumRPB()));
        nextProcedureForm.setDescription(getDescription());

        return nextProcedureForm;
    }

    @Override
    public void setField(XMLElements element, String value) {
        if (!ValidationHelper.isNullOrEmpty(value)) {
            switch ((NextProcedureFormXMLElements)element) {
                case DATE:
                    setDate(value);
                    break;

                case NOTE_TYPE:
                    setNoteType(value);
                    break;

                case NUM_RPB:
                    setNumRPB(value);
                    break;

                case NUM_RP:
                    setNumRP(value);
                    break;

                case ACT_CODE:
                    setActCode(value);
                    break;

                case YEAR:
                    setYear(value);
                    break;

                case DESCRIPTION:
                    setDescription(value);
                    break;

                default:
                    break;

            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNumRP() {
        return numRP;
    }

    public void setNumRP(String numRP) {
        this.numRP = numRP;
    }

    public String getNumRPB() {
        return numRPB;
    }

    public void setNumRPB(String numRPB) {
        this.numRPB = numRPB;
    }

    public String getActCode() {
        return actCode;
    }

    public void setActCode(String actCode) {
        this.actCode = actCode;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
