package it.nexera.ris.web.beans.wrappers.logic.editInTable;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.tableGenerator.CertificazioneTableGenerator;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

@Getter
@Setter
public class FormalityEditInTableWrapper extends BaseEditInTableWrapper {

    private static Log log = LogFactory.getLog(FormalityEditInTableWrapper.class);

    private String generalRegister;

    private String particularRegister;

    private Date presentationDate;

    private String distraintComment;

    private String textCertification;

    public FormalityEditInTableWrapper(Formality formality) {
        super(formality.getId(), formality.getComment());
        this.generalRegister = formality.getGeneralRegister();
        this.particularRegister = formality.getParticularRegister();
        this.presentationDate = formality.getPresentationDate();
        this.distraintComment = formality.getDistraintComment();
        this.textCertification = formality.getTextCertification();
    }

    public void save() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (isEdited()) {
            Formality formality = DaoManager.get(Formality.class, getId());
            formality.setComment(getComment());
            formality.setDistraintComment(getDistraintComment());
            formality.setTextCertification(getTextCertification());
            DaoManager.save(formality, true);
        }
    }

    public String getTextCertificationStr(){
        if (ValidationHelper.isNullOrEmpty(getTextCertification())) {
            try {
                this.textCertification = CertificazioneTableGenerator
                        .getSubjectsPartForFormalityTextCertification(DaoManager.get(Formality.class,getId()));
            } catch (PersistenceBeanException | IllegalAccessException | InstantiationException e) {
                LogHelper.log(log, e);
            }
        }
        return getTextCertification();
    }

    public String getTextCertificationStr(Boolean shouldReportRelationships){
        if (ValidationHelper.isNullOrEmpty(getTextCertification())) {
            try {
                Formality formality = DaoManager.get(Formality.class,getId());
                formality.setShouldReportRelationships(shouldReportRelationships == null ? false : shouldReportRelationships);
                this.textCertification = CertificazioneTableGenerator
                        .getSubjectsPartForFormalityTextCertification(formality);
            } catch (PersistenceBeanException | IllegalAccessException | InstantiationException e) {
                LogHelper.log(log, e);
            }
        }
        return getTextCertification();
    }
    public void setDistraintComment(String distraintComment) {
        this.distraintComment = distraintComment;
        this.setEdited(true);
    }
}
