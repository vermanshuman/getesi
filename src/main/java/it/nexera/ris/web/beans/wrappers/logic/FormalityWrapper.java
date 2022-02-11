package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.SectionC;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FormalityWrapper implements Serializable {

    public transient final Log log = LogFactory
            .getLog(FormalityWrapper.class);

    private static final long serialVersionUID = -8276040990023629164L;

    private Formality formality;

    private String conservatoryName;

    private Boolean isSelected;

    private Subject subject;

    public FormalityWrapper(Formality formality, Subject subject) {
        this.formality = formality;
        this.subject = subject;

        if (formality.getReclamePropertyService() != null) {
            conservatoryName = formality.getReclamePropertyService().toString();
        } else if (formality.getProvincialOffice() != null) {
            conservatoryName = formality.getProvincialOffice().toString();
        }
    }

    public String getFavorAgainst() {
        List<Long> sectionCIds = new ArrayList<>();

        if (getSubject() != null && getSubject().getSectionC() != null) {
            sectionCIds.addAll(getSubject().getSectionC().stream()
                    .map(SectionC::getId).collect(Collectors.toList()));
        }

        try {
            List<Long> formalitySectionCIds = DaoManager.loadField(
                    Formality.class, "sectionC.id", Long.class,
                    new CriteriaAlias[]
                            {
                                    new CriteriaAlias("sectionC", "sectionC",
                                            JoinType.INNER_JOIN)
                            }, new Criterion[]
                            {});
            sectionCIds.addAll(formalitySectionCIds);
        } catch (Exception e1) {
            LogHelper.log(log, e1);
        }

        if (!ValidationHelper.isNullOrEmpty(sectionCIds)) {
            try {
                List<String> values = DaoManager.loadField(SectionC.class,
                        "sectionCType", String.class, new Criterion[]
                                {
                                        Restrictions.in("id", sectionCIds)
                                });

                Set<String> valuesSet = new HashSet<>(values);

                if (valuesSet.size() > 1) {
                    return "C";
                } else {
                    return "F/C";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return "";
    }

    public Formality getFormality() {
        return formality;
    }

    public void setFormality(Formality formality) {
        this.formality = formality;
    }

    public String getConservatoryName() {
        return conservatoryName;
    }

    public void setConservatoryName(String conservatoryName) {
        this.conservatoryName = conservatoryName;
    }

    public Boolean getIsSelected() {
        return isSelected == null ? Boolean.FALSE : isSelected;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

}
