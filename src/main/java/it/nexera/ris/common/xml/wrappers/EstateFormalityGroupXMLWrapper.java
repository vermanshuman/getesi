package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.common.enums.EstateFormalityGroupXMLElements;
import it.nexera.ris.common.enums.XMLElements;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.EstateFormalityGroup;

import java.util.Date;

public class EstateFormalityGroupXMLWrapper extends BaseXMLWrapper<EstateFormalityGroup> {

    private Date conservationDate;

    @Override
    public EstateFormalityGroup toEntity() throws InstantiationException, IllegalAccessException, PersistenceBeanException {
        EstateFormalityGroup group = new EstateFormalityGroup();

        group.setConservationDate(getConservationDate());

        return group;
    }

    @Override
    public void setField(XMLElements element, String value) {
        if (!ValidationHelper.isNullOrEmpty(value)) {
            switch ((EstateFormalityGroupXMLElements) element) {
                case CONSERVATION_DATE:
                    setConservationDate(DateTimeHelper.fromXMLString(value));
                    break;
                default:
                    break;
            }
        }
    }

    public Date getConservationDate() {
        return conservationDate;
    }

    public void setConservationDate(Date conservationDate) {
        this.conservationDate = conservationDate;
    }
}
