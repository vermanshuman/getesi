package it.nexera.ris.common.helpers.tableGenerator;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.PartedPairsByCityWrapper;
import org.hibernate.HibernateException;

import java.util.ArrayList;
import java.util.List;

public class DeceasedTableGenerator extends InterlayerTableGenerator {

    private static final String DECEADES_TABLE_HEADER = ResourcesHelper.getString("deceadesTableSubjectHeader");
    private static final String DECEADES_TABLE_TITLE = ResourcesHelper.getString("deceadesTableTitle");

    public DeceasedTableGenerator(Request request) {
        super(request);
    }

    @Override
    void addBeginning() {
        getJoiner().add(getCell("", BORDER) +
                getCell("", BORDER) +
                getCell("", BORDER) +
                getCell(DECEADES_TABLE_HEADER, COLUMN_PADDING + BORDER)
        );
        addEmptyRow(1);
        getJoiner().add(getCell("", BORDER) +
                getCell("", BORDER) +
                getCell("", BORDER) +
                getCell(DECEADES_TABLE_TITLE, "text-align:center;" + BORDER)
        );
        addEmptyRow(1);
    }

    @Override
    void fillTagTableList() throws PersistenceBeanException, IllegalAccessException {
        List<Formality> formalities = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getRequest().getSituationEstateLocations())) {
            formalities = getRequest().getSituationEstateLocations().get(0).getFormalityList();
        }
        if (ValidationHelper.isNullOrEmpty(formalities)) {
            return;
        }
        setTagTableList(new ArrayList<>());
        int counter = 1;

        fillPartedPairsByCityWrapper(formalities);

        for (PartedPairsByCityWrapper partedPairsByCityWrapper : getPartedPairsByCityWrapperList()) {
            for (List<Pair<String, String>> pairList : partedPairsByCityWrapper.getPatredList()) {
                TagTableWrapper wrapper = new TagTableWrapper();
                wrapper.counter = counter;
                wrapper.estateFormalityDef = partedPairsByCityWrapper.getFormality().getRegisterString();
                wrapper.cityDesc = partedPairsByCityWrapper.getFormality().getFirstPropertyAlienatedTable();
                wrapper.descriptionRows = new ArrayList<>();
                wrapper.pairs = new ArrayList<>();
                String formalityBlock = partedPairsByCityWrapper.getFormality().getSubjectDeceadesTable(getRequest());
                if (!ValidationHelper.isNullOrEmpty(formalityBlock)) {
                    wrapper.pairs.add(new Pair<>("", formalityBlock));
                }
                wrapper.cityRowNumber = wrapper.pairs.size() + 2;

                if (!ValidationHelper.isNullOrEmpty(pairList)) {
                    wrapper.pairs.addAll(pairList);
                }
                wrapper.allRowNum = wrapper.pairs.size();
                getTagTableList().add(wrapper);
                counter++;
            }
        }
    }
}
