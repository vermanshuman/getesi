package it.nexera.ris.common.helpers.tableGenerator;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.PartedPairsByCityWrapper;
import org.hibernate.HibernateException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AlienatedTableGenerator extends InterlayerTableGenerator {

    private static final String ALIENTED_TABLE_TITLE = ResourcesHelper.getString("alienatedTableTitle");

    public AlienatedTableGenerator(Request request) {
        super(request);
    }

    @Override
    void addBeginning() {

        long formalities = 0L;
        if (!ValidationHelper.isNullOrEmpty(getRequest().getSituationEstateLocations())) {
            EstateSituation estateSituation = getRequest().getSituationEstateLocations().stream()
                    .filter(es -> ValidationHelper.isNullOrEmpty(es.getSalesDevelopment()) || !es.getSalesDevelopment())
                    .findFirst()
                    .orElse(null);
            if(estateSituation != null)
                formalities = estateSituation.getFormalityList().size();
        }

        getJoiner().add(getCell("", BORDER) +
                getCell("", BORDER) +
                getCell("", BORDER) +
                (!ValidationHelper.isNullOrEmpty(getRequest().getSubject()) && getRequest().getSubject().getTypeIsPhysicalPerson() ?
                        getCell(ResourcesHelper.getString(!ValidationHelper.isNullOrEmpty(formalities) && formalities == 1L ?
                                "alienatedTableHeaderOne" : "alienatedTableHeader"), COLUMN_PADDING + BORDER) :
                        getCell(ResourcesHelper.getString(!ValidationHelper.isNullOrEmpty(formalities) && formalities == 1L ?
                                "alienatedTableHeaderOneLegal" : "alienatedTableHeaderLegal"), COLUMN_PADDING + BORDER))
        );
        addEmptyRow(1);
        getJoiner().add(getCell("", BORDER) +
                getCell("", BORDER) +
                getCell("", BORDER) +
                getCell(ALIENTED_TABLE_TITLE, "text-align:center;" + BORDER)
        );
        addEmptyRow(1);
    }

    @Override
    void fillTagTableList() throws PersistenceBeanException, IllegalAccessException {
        List<Formality> formalities = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getRequest().getSituationEstateLocations())) {
            EstateSituation estateSituation = getRequest().getSituationEstateLocations().stream()
                    .filter(es -> ValidationHelper.isNullOrEmpty(es.getSalesDevelopment()) || !es.getSalesDevelopment())
                    .findFirst()
                    .orElse(null);
            if(!ValidationHelper.isNullOrEmpty(estateSituation) && !ValidationHelper.isNullOrEmpty(estateSituation.getFormalityList())){
                formalities = new ArrayList<>(estateSituation.getFormalityList());
                formalities.sort(Comparator.comparing(Formality::getComparedDate)
                        .thenComparing(Formality::getGeneralRegister)
                        .thenComparing(Formality::getParticularRegister));
            }
        }
        if (ValidationHelper.isNullOrEmpty(formalities)) {
            return;
        }
        setTagTableList(new ArrayList<>());
        int counter = 1;



        fillPartedPairsByCity(formalities, Boolean.TRUE);

        for (PartedPairsByCityWrapper partedPairsByCityWrapper : getPartedPairsByCityWrapperList()) {
            for (List<Pair<String, String>> pairList : partedPairsByCityWrapper.getPatredList()) {

                TagTableWrapper wrapper = new TagTableWrapper();
                wrapper.counter = counter;
                wrapper.estateFormalityDef = partedPairsByCityWrapper.getFormality().getRegisterString();
                wrapper.cityDesc = partedPairsByCityWrapper.getFormality().getFirstPropertyAlienatedTable();
                wrapper.descriptionRows = new ArrayList<>();
                wrapper.pairs = new ArrayList<>();
                String formalityBlock = "";
                if (!ValidationHelper.isNullOrEmpty(partedPairsByCityWrapper.getFormality().getSubjectAlienatedTable(getRequest()))) {
                    formalityBlock = partedPairsByCityWrapper.getFormality().getSubjectAlienatedTable(getRequest());
                }
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

    private void fillPartedPairsByCity(List<Formality> formalities, Boolean addCommercialAndOmi )
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
                    getRequest().getSubject(), presumableSubjects, false, formality, showCadastralIncome, showAgriculturalIncome, addCommercialAndOmi);

            PartedPairsByCityWrapper pairsByCityWrapper = new PartedPairsByCityWrapper(formality, tempPairs);
            pairsByCityWrapper.fillPatredList();

            getPartedPairsByCityWrapperList().add(pairsByCityWrapper);

        }

    }
}
