package it.nexera.ris.web.beans.wrappers.logic.editInTable;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.EstateSituation;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class SalesEstateSituationEditInTableWrapper {

    private Long estateSituationId;

    private List<FormalityEditInTableWrapper> formalityList;

    private List<PropertyEditInTableWrapper> propertyList;

    public BaseEditInTableWrapper comment;

    public BaseEditInTableWrapper commentInit;

    public SalesEstateSituationEditInTableWrapper(EstateSituation situation) {
        this.formalityList = situation.getFormalityList().stream()
                .map(FormalityEditInTableWrapper::new).collect(Collectors.toList());
        this.propertyList = situation.getPropertyList().stream()
                .map(p -> new PropertyEditInTableWrapper(p, situation)).collect(Collectors.toList());
        this.comment = new BaseEditInTableWrapper(situation.getId(),
                !ValidationHelper.isNullOrEmpty(situation.getRequest().getDistraintFormality())
                        ? situation.getCommentWithoutInitialize() : situation.getComment());
        this.commentInit = new BaseEditInTableWrapper(situation.getId(), situation.getCommentInit());
        this.estateSituationId = situation.getId();
    }

    public void save() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        for (FormalityEditInTableWrapper formality : getFormalityList()) {
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

}
