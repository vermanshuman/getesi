package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class SubjectDifferenceWrapper extends BaseHelper {

    private Subject subject;

    private String difference;

    private Long numberProperty;

    private Long numberFormality;

    public SubjectDifferenceWrapper(Subject subject, Subject second) {
        this.subject = subject;
        fillDifference(second);
        List<Request> requestList = null;
        try {
            requestList = DaoManager.load(Request.class, new Criterion[]{
                    Restrictions.eq("subject.id", subject.getId())
            });
        } catch (IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
        if (!ValidationHelper.isNullOrEmpty(requestList)) {
            this.numberFormality = requestList.stream().map(Request::getEstateFormalityList).distinct().count();
            this.numberProperty = requestList.stream().map(Request::getPropertyList).distinct().count();
        }
    }

    public void fillDifference(Subject second) {
        String srt = "";
        if (getSubject().getTypeId().equals(SubjectType.PHYSICAL_PERSON.getId())) {
            if (getSubject().getFiscalCode() != null && second.getFiscalCode() != null
                    && !getSubject().getFiscalCode().equalsIgnoreCase(second.getFiscalCode())) {
                srt += differenceStr("documentGenerationTagsSUBJECT_MASTERY_FISCAL_CODE",
                        getSubject().getFiscalCode());
            }
            if (!getSubject().getName().equalsIgnoreCase(second.getName())) {
                srt += differenceStr("documentGenerationTagsSUBJECT_MASTERY_NAME", getSubject().getName());
            }
            if (!getSubject().getSurname().equalsIgnoreCase(second.getSurname())) {
                srt += differenceStr("documentGenerationTagsSUBJECT_MASTERY_SURNAME", getSubject().getSurname());
            }
            if (getSubject().getSex() != null && second.getSex() != null
                    && !getSubject().getSex().equals(second.getSex())) {
                srt += differenceStr("documentGenerationTagsSUBJECT_MASTERY_SEX_TYPE", getSubject().getSex());
            }
            if (getSubject().getBirthDate() != null && second.getBirthDate() != null
                    && !getSubject().getBirthDate().equals(second.getBirthDate())) {
                srt += differenceStr("documentGenerationTagsSUBJECT_MASTERY_BIRTHDAY", DateTimeHelper.toString(getSubject()
                        .getBirthDate()));
            }
        } else {
            if (!getSubject().getNumberVAT().equalsIgnoreCase(second.getNumberVAT())) {
                srt += differenceStr("documentGenerationTagsSUBJECT_MASTERY_IVA",
                        getSubject().getNumberVAT());
            }
            if (!getSubject().getOldNumberVAT().equalsIgnoreCase(second.getOldNumberVAT())) {
                srt += differenceStr("documentGenerationTagsSUBJECT_MASTERY_OLD_IVA",
                        getSubject().getOldNumberVAT());
            }
            if (!getSubject().getBusinessName().equalsIgnoreCase(second.getBusinessName())) {
                srt += differenceStr("documentGenerationTagsSUBJECT_MASTERY_JURIDICAL_NATIONS",
                        getSubject().getBusinessName());
            }
        }
        if (getSubject().getBirthProvince() != null && second.getBirthProvince() != null
                && !getSubject().getBirthProvince().equals(second.getBirthProvince())) {
            srt += differenceStr("documentGenerationTagsSUBJECT_MASTERY_PROVINCE",
                    getSubject().getBirthProvince());
        }
        if (getSubject().getBirthCity() != null && second.getBirthCity() != null
                && !getSubject().getBirthCity().equals(second.getBirthCity())) {
            srt += differenceStr("documentGenerationTagsSUBJECT_MASTERY_COMUNE", getSubject().getBirthCity());
        }
        setDifference(srt);
    }

    private String differenceStr(String titleId, Object value) {
        return String.format("%s = %s<br/>", ResourcesHelper.getEnum(titleId), value.toString());
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getDifference() {
        return difference;
    }

    public void setDifference(String difference) {
        this.difference = difference;
    }

    public Long getNumberProperty() {
        return numberProperty;
    }

    public void setNumberProperty(Long numberProperty) {
        this.numberProperty = numberProperty;
    }

    public Long getNumberFormality() {
        return numberFormality;
    }

    public void setNumberFormality(Long numberFormality) {
        this.numberFormality = numberFormality;
    }
}
