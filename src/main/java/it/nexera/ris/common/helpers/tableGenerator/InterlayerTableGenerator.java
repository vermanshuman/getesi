package it.nexera.ris.common.helpers.tableGenerator;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.helpers.EstateSituationHelper;
import it.nexera.ris.common.helpers.SubjectHelper;
import it.nexera.ris.common.helpers.TemplatePdfTableHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.PartedPairsByCityWrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class InterlayerTableGenerator extends TagTableGenerator {
    private List<PartedPairsByCityWrapper> partedPairsByCityWrapperList;

    InterlayerTableGenerator(Request request) {
        super(request);
    }

    public InterlayerTableGenerator(Request request, boolean fullSize) {
        super(request, fullSize);
    }

    @Override
    void addBody() throws PersistenceBeanException, IllegalAccessException, TypeFormalityNotConfigureException {
        fillTagTableList();
        if (ValidationHelper.isNullOrEmpty(getTagTableList())) {
            return;
        }
        boolean notFirst = false;
        for (TagTableWrapper wrapper : getTagTableList()) {
            if (notFirst) {
                addEmptyRow(1);
            }
            StringBuffer sb = new StringBuffer();
            sb.append(getCellRowspan("" + wrapper.counter, wrapper.allRowNum,
                    TEXT_CENTER + TEXT_TOP + STANDARD_TABLE_COLUMN_1_WIDTH + BORDER));
            sb.append(getCellRowspan(wrapper.estateFormalityDef, wrapper.allRowNum,
                    TEXT_CENTER + TEXT_TOP + STANDARD_TABLE_COLUMN_2_WIDTH + BORDER));
            sb.append(getCell("", "border-top: 1px solid black;"));
            sb.append(getCell(wrapper.pairs.get(0).getSecond(), COLUMN_PADDING + STANDARD_TABLE_COLUMN_4_WIDTH +
                    "border-left: 1px solid black;border-right: 1px solid black;border-top: 1px solid black;"));
            getJoiner().add(sb.toString());
            for (int i = 1; i < wrapper.pairs.size(); i++) {
                boolean last = (i == (wrapper.pairs.size() - 1));
                sb = new StringBuffer();
                Pair<String, String> cityProperty = wrapper.pairs.get(i);
                if (!ValidationHelper.isNullOrEmpty(cityProperty.getFirst())) {
                    sb.append(getCell(cityProperty.getFirst(), TEXT_CENTER + TEXT_TOP + "font-weight: bold;"
                            + STANDARD_TABLE_COLUMN_3_WIDTH + (last ? "border-bottom: 1px solid black;" : "")));
                } else {
                    sb.append(getCell("", last ? "border-bottom: 1px solid black;" : ""));
                }
                sb.append(getCell(cityProperty.getSecond(), COLUMN_PADDING + STANDARD_TABLE_COLUMN_4_WIDTH +
                        "border-left: 1px solid black;border-right: 1px solid black;"
                        + (last ? "border-bottom: 1px solid black;" : "")));
                getJoiner().add(sb.toString());
            }
            notFirst = true;
        }
    }

    protected void fillPartedPairsByCityWrapper(List<Formality> formalities)
            throws PersistenceBeanException, IllegalAccessException {
        List<Long> listIds = EstateSituationHelper.getIdSubjects(getRequest());
        List<Subject> presumableSubjects = EstateSituationHelper.getListSubjects(listIds);
        List<Subject> unsuitableSubjects = SubjectHelper.deleteUnsuitable(presumableSubjects, formalities);
        presumableSubjects.removeAll(unsuitableSubjects);
        presumableSubjects.add(getRequest().getSubject());
        for (Formality formality : formalities) {

            Boolean showCadastralIncome = Boolean.FALSE;
            Boolean showAgriculturalIncome = Boolean.FALSE;
            
            if(!ValidationHelper.isNullOrEmpty(getRequest().getClient()) &&
                    !ValidationHelper.isNullOrEmpty(getRequest().getClient().getShowCadastralIncome())){
                showCadastralIncome = getRequest().getClient().getShowCadastralIncome();
            }
            if(!ValidationHelper.isNullOrEmpty(getRequest().getClient()) &&
                    !ValidationHelper.isNullOrEmpty(getRequest().getClient().getShowAgriculturalIncome())){
                showAgriculturalIncome = getRequest().getClient().getShowAgriculturalIncome();
            }
            List<Property> properties = formality.loadPropertiesByRelationship(presumableSubjects);
            List<Pair<String, String>> tempPairs = TemplatePdfTableHelper.groupPropertiesByQuoteTypeListLikePairs(properties,
                    getRequest().getSubject(), presumableSubjects, false, formality, showCadastralIncome, showAgriculturalIncome);

            PartedPairsByCityWrapper pairsByCityWrapper = new PartedPairsByCityWrapper(formality, tempPairs);
            pairsByCityWrapper.fillPatredList();

            getPartedPairsByCityWrapperList().add(pairsByCityWrapper);

        }

    }

    public List<PartedPairsByCityWrapper> getPartedPairsByCityWrapperList() {
        if (ValidationHelper.isNullOrEmpty(partedPairsByCityWrapperList)) {
            setPartedPairsByCityWrapperList(new ArrayList<>());
        }
        return partedPairsByCityWrapperList;
    }

    public void setPartedPairsByCityWrapperList(List<PartedPairsByCityWrapper> partedPairsByCityWrapperList) {
        this.partedPairsByCityWrapperList = partedPairsByCityWrapperList;
    }
}
