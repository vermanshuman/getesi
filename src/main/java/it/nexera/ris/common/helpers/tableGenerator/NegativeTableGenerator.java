package it.nexera.ris.common.helpers.tableGenerator;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.TemplatePdfTableHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

public class NegativeTableGenerator extends TagTableGenerator {

    public transient final Log log = LogFactory.getLog(NegativeTableGenerator.class);
    private boolean isNational;

    public NegativeTableGenerator(Request request) {
        super(request);
        this.isNational = request.getAggregationLandChargesRegistry() != null
                && request.getAggregationLandChargesRegistry().getName().equalsIgnoreCase("Nazionale");
    }

    @Override
    String addBeforeTableText() {
        String str = null;
        try {
            String group = TemplatePdfTableHelper.getEstateFormalityConservationDate(getRequest());
            if (isNational()) {
                str = String.format(ResourcesHelper.getString("negativeTableConservatoryNational"), group);
            } else {
                String richiesta = "";
                for (LandChargesRegistry registry : getRequest().getAggregationLandChargesRegistry().getLandChargesRegistries()) {
                    if (getRequest().getAggregationLandChargesRegistry().getName().equals("CASERTA - SMCV")) {
                        richiesta += getRequest().getAggregationLandChargesRegistry().getName();
                        break;
                    }
                    if (!ValidationHelper.isNullOrEmpty(richiesta) && registry.getVisualize()) {
                        richiesta += " - ";
                    }
                    if (registry.getVisualize()) {
                        richiesta += registry.getName();
                    }
                }
                str = String.format(ResourcesHelper.getString("negativeTableConservatoryNotNational"),
                        richiesta, group);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return str;
    }

    @Override
    void addBeginning() {
        String subjectText;
        if (getRequest().isPhysicalPerson()) {
            subjectText = isNational
                    ? ResourcesHelper.getString("negativeTableSubjectPhysicalNational")
                    : ResourcesHelper.getString("negativeTableSubjectPhysical");
        } else {
            subjectText = isNational
                    ? ResourcesHelper.getString("negativeTableSubjectLegalNational")
                    : ResourcesHelper.getString("negativeTableSubjectLegal");
        }
        getJoiner().add(getCell("", BORDER) +
                getCell("", BORDER) +
                getCell("", BORDER) +
                getCell(subjectText, COLUMN_PADDING + BORDER)
        );
    }

    @Override
    void fillTagTableList() {

    }

    @Override
    void addEstateFormality() {
        addEmptyRow(18);
    }

    public boolean isNational() {
        return isNational;
    }
}
