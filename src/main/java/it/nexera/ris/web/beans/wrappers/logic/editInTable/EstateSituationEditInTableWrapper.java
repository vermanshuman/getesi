package it.nexera.ris.web.beans.wrappers.logic.editInTable;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.EstateSituation;

import java.util.List;
import java.util.stream.Collectors;

public class EstateSituationEditInTableWrapper {

    private Long estateSituationId;

    private List<EstateFormalityEditInTableWrapper> estateFormalityList;

    private List<PropertyEditInTableWrapper> propertyList;

    public BaseEditInTableWrapper comment;

    public BaseEditInTableWrapper commentInit;

    private Boolean regime;

    public EstateSituationEditInTableWrapper(EstateSituation situation) {
        this.estateFormalityList = situation.getEstateFormalityList().stream()
                .map(EstateFormalityEditInTableWrapper::new).collect(Collectors.toList());
        this.propertyList = situation.getPropertyList().stream()
                .map(p -> new PropertyEditInTableWrapper(p, situation)).collect(Collectors.toList());
        this.comment = new BaseEditInTableWrapper(situation.getId(),
                !ValidationHelper.isNullOrEmpty(situation.getRequest().getDistraintFormality())
                        ? situation.getCommentWithoutInitialize() : situation.getComment());
        this.commentInit = new BaseEditInTableWrapper(situation.getId(), situation.getCommentInit());
        this.estateSituationId = situation.getId();
    }

    public void save() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        for (EstateFormalityEditInTableWrapper formality : getEstateFormalityList()) {
            formality.save();
        }
        for (PropertyEditInTableWrapper property : getPropertyList()) {
            property.save();
        }
        if(getComment().isEdited()){
            EstateSituation situation = DaoManager.get(EstateSituation.class, getComment().getId());
            situation.setComment(getComment().getComment());
            DaoManager.save(situation, true);
        }
        if(getCommentInit().isEdited()){
            EstateSituation situation = DaoManager.get(EstateSituation.class, getCommentInit().getId());
            situation.setCommentInit(getCommentInit().getComment());
            DaoManager.save(situation, true);
        }
    }

    public List<EstateFormalityEditInTableWrapper> getEstateFormalityList() {
        return estateFormalityList;
    }

    public void setEstateFormalityList(List<EstateFormalityEditInTableWrapper> estateFormalityList) {
        this.estateFormalityList = estateFormalityList;
    }

    public List<PropertyEditInTableWrapper> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<PropertyEditInTableWrapper> propertyList) {
        this.propertyList = propertyList;
    }

    public BaseEditInTableWrapper getComment() {
        return comment;
    }

    public void setComment(BaseEditInTableWrapper comment) {
        this.comment = comment;
    }

    public BaseEditInTableWrapper getCommentInit() {
        return commentInit;
    }

    public void setCommentInit(BaseEditInTableWrapper commentInit) {
        this.commentInit = commentInit;
    }

    public Long getEstateSituationId() {
        return estateSituationId;
    }

    public void setEstateSituationId(Long estateSituationId) {
        this.estateSituationId = estateSituationId;
    }

    public Boolean getRegime() {
        return regime;
    }

    public void setRegime(Boolean regime) {
        this.regime = regime;
    }
}
