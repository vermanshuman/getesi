package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeActNotConfigureException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.EstateFormality;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeAct;
import org.apache.commons.lang3.text.WordUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class EstateFormalityXMLWrapper
        extends BaseXMLWrapper<EstateFormality> {

    private String procedureType;

    private String description;

    private String date;

    private String numRP;

    private String numRPB;

    private String numRG;

    private String repertoire;

    private String actCode;

    private String speciesAct;

    private String noteType;

    private String provenance;

    private String titleDate;

    private String denominationPU;

    private String rifAnno;

    private LandChargesRegistry chargesRegistry;

    @Override
    public EstateFormality toEntity() throws InstantiationException, IllegalAccessException, PersistenceBeanException {
        return null;
    }

    public EstateFormality toEntityWithException(Session session)
            throws InstantiationException, IllegalAccessException, PersistenceBeanException, TypeActNotConfigureException {
        EstateFormality estateFormality = new EstateFormality();

        estateFormality.setId(getId());
        estateFormality.setCreateUserId(getCreateUserId());
        estateFormality.setUpdateUserId(getUpdateUserId());
        estateFormality.setCreateDate(getCreateDate());
        estateFormality.setUpdateDate(getUpdateDate());
        estateFormality.setVersion(getVersion());
        estateFormality.setDate(DateTimeHelper.fromXMLStringDate(getDate()));
        estateFormality.setTypeAct(ValidationHelper.isNullOrEmpty(getActCode()) ? null
                : ConnectionManager.get(TypeAct.class, new Criterion[]{
                Restrictions.eq("code", "" + Integer.parseInt(getActCode())),
                Restrictions.eq("type", TypeActEnum.getByStr(getNoteType()))
        }, session));
        if (estateFormality.getTypeAct() == null) {
        	TypeActNotConfigureException ex =
        		new TypeActNotConfigureException("Type act not found");

        	ex.setActCode("" + Integer.parseInt(getActCode()));
        	ex.setNoteType(getNoteType());
        	ex.setDescription(getDescription());

            throw ex;
        }
        estateFormality.setDenominationPU(getDenominationPU());
        estateFormality.setDescription(getDescription());
        estateFormality.setNumRG(ValidationHelper.isNullOrEmpty(getNumRG()) ? null : Integer.parseInt(getNumRG()));
        estateFormality.setNumRP(getNumRP());
        estateFormality.setNumRPB(ValidationHelper.isNullOrEmpty(getNumRPB()) ? null : Integer.parseInt(getNumRPB()));
        estateFormality.setRepertoire(getRepertoire());
        estateFormality.setTitleDate(DateTimeHelper.fromXMLStringDate(getTitleDate()));
        estateFormality.setSpeciesAct(getSpeciesAct());
        estateFormality.setEstateFormalityType(ValidationHelper.isNullOrEmpty(getProcedureType()) ? null
                : EstateFormalityType.getEnumByCode(getProcedureType()));
        estateFormality.setProvenance(ValidationHelper.isNullOrEmpty(getProvenance()) ? null : Provenance.getEnumByString(getProvenance()));
        estateFormality.setComment(String.format(ResourcesHelper.getString("estateFormalityCommentFormat"),
                estateFormality.getTypeAct() != null ? estateFormality.getTypeAct().getTextInVisura() : "",
                getDenominationPU(), DateTimeHelper.toStringDateWithDots(estateFormality.getTitleDate()), getRepertoire()));
        estateFormality.setLandChargesRegistry(getChargesRegistry());
        if(!ValidationHelper.isNullOrEmpty(getRifAnno())){
            try {
                estateFormality.setReferenceYear(Integer.parseInt(getRifAnno()));
            } catch (NumberFormatException e) {
                LogHelper.log(log, e);
            }
        }
        return estateFormality;
    }

    @Override
    public void setField(XMLElements element, String value) {
        if (!ValidationHelper.isNullOrEmpty(value)) {
            switch ((EstateFormalityXMLElements) element) {
                case PROVENANCE:
                    setProvenance(value);
                    break;

                case PROCEDURE_TYPE:
                    setProcedureType(value);
                    break;

                case DATE:
                    setDate(value);
                    break;

                case NUM_RPB:
                    setNumRPB(value);
                    break;

                case NUM_RP:
                    setNumRP(value);
                    break;

                case NUM_RG:
                    setNumRG(value);
                    break;

                case REPERTOIRE:
                    setRepertoire(value);
                    break;

                case ACT_CODE:
                    setActCode(value);
                    break;

                case SPECIES_ACT:
                    setSpeciesAct(value);
                    break;

                case NOTE_TYPE:
                    setNoteType(value);
                    break;

                case TITLE_DATE:
                    setTitleDate(value);
                    break;

                case DENOMINATION_PU:
                    setDenominationPU(WordUtils.capitalizeFully(value));
                    break;

                case DESCRIPTION:
                    setDescription(value);
                    break;

                case RIF_ANNO:
                    setRifAnno(value);
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

    public String getRepertoire() {
        return repertoire;
    }

    public void setRepertoire(String repertoire) {
        this.repertoire = repertoire;
    }

    public String getSpeciesAct() {
        return speciesAct;
    }

    public void setSpeciesAct(String speciesAct) {
        this.speciesAct = speciesAct;
    }

    public String getTitleDate() {
        return titleDate;
    }

    public void setTitleDate(String titleDate) {
        this.titleDate = titleDate;
    }

    public String getDenominationPU() {
        return denominationPU;
    }

    public void setDenominationPU(String denominationPU) {
        this.denominationPU = denominationPU;
    }

    public String getProcedureType() {
        return procedureType;
    }

    public void setProcedureType(String procedureType) {
        this.procedureType = procedureType;
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

    public String getNumRG() {
        return numRG;
    }

    public void setNumRG(String numRG) {
        this.numRG = numRG;
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

    public String getProvenance() {
        return provenance;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }

    public LandChargesRegistry getChargesRegistry() {
        return chargesRegistry;
    }

    public void setChargesRegistry(LandChargesRegistry chargesRegistry) {
        this.chargesRegistry = chargesRegistry;
    }

    public String getRifAnno() {
        return rifAnno;
    }

    public void setRifAnno(String rifAnno) {
        this.rifAnno = rifAnno;
    }
}
