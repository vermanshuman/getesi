package it.nexera.ris.common.helpers.tableGenerator;

import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.TemplatePdfTableHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.EstateFormality;
import it.nexera.ris.persistence.beans.entities.domain.EstateSituation;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.web.beans.pages.RequestTextEditBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RealEstateRelationshipTableGenerator extends TagTableGenerator {

    public RealEstateRelationshipTableGenerator(Request request) {
        super(request);
    }

    private List<EstateSituation> loadList() throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.load(EstateSituation.class, new Criterion[]{
                Restrictions.eq("request.id", getRequest().getId()),
                Restrictions.or(Restrictions.eq("salesDevelopment", Boolean.FALSE)
                        , Restrictions.isNull("salesDevelopment"))
        });
    }

    @Override
    void addBeginning() {

    }

    @Override
    void fillTagTableList() throws TypeFormalityNotConfigureException, PersistenceBeanException, IllegalAccessException {
        List<EstateSituation> estateSituationList = loadList();
        if (ValidationHelper.isNullOrEmpty(estateSituationList)) {
            return;
        }
        setTagTableList(new ArrayList<>());
        int counter = 1;
        estateSituationList.sort(new RequestTextEditBean.SortByInnerEstateFormalityDate());
        for (EstateSituation estateSituation : estateSituationList) {
            TagTableWrapper wrapper = new TagTableWrapper();
            wrapper.counter = counter;
            wrapper.estateFormalityDef = generateEstateRelationshipFirstCol(estateSituation);
            wrapper.cityDesc = generateEstateSituationSecondCol(estateSituation);
            wrapper.descriptionRows = new ArrayList<>();
            String estateFormality = estateFormalityBlock(estateSituation);
            if (!ValidationHelper.isNullOrEmpty(estateFormality)) {
                wrapper.descriptionRows.add(estateFormality);
            }
            wrapper.cityRowNumber = wrapper.descriptionRows.size() + 2;

            Boolean showCadastralIncome = Boolean.FALSE;
            Boolean showAgriculturalIncome = Boolean.FALSE;
            if (!ValidationHelper.isNullOrEmpty(estateSituation.getRequest().getClient()) &&
                    !ValidationHelper.isNullOrEmpty(estateSituation.getRequest().getClient().getShowCadastralIncome())) {
                showCadastralIncome = estateSituation.getRequest().getClient().getShowCadastralIncome();
            }
            if (!ValidationHelper.isNullOrEmpty(estateSituation.getRequest().getClient()) &&
                    !ValidationHelper.isNullOrEmpty(estateSituation.getRequest().getClient().getShowAgriculturalIncome())) {
                showAgriculturalIncome = estateSituation.getRequest().getClient().getShowAgriculturalIncome();
            }

            List<String> property = TemplatePdfTableHelper.groupPropertiesByQuoteTypeList(estateSituation.getPropertyList(),
                    estateSituation.getRequest().getSubject(), true, showCadastralIncome, showAgriculturalIncome);
            if (!ValidationHelper.isNullOrEmpty(property)) {
                wrapper.descriptionRows.addAll(property);
            }
            if (estateSituation.getComment() != null && !estateSituation.getComment()
                    .equals(ResourcesHelper.getString("estateSituationCommentDefaultValue"))) {
                wrapper.descriptionRows.add("<br/><br/><div style=\"text-align: justify;\"><i>" + estateSituation.getComment() + "</i></div>");
            }
            String formality = formalityBlock(estateSituation);
            if (!ValidationHelper.isNullOrEmpty(formality)) {
                wrapper.descriptionRows.add(formality);
            }
            wrapper.allRowNum = wrapper.descriptionRows.size();
            getTagTableList().add(wrapper);
            counter++;
        }
    }

    private String generateEstateRelationshipFirstCol(EstateSituation situation) {
        if (!ValidationHelper.isNullOrEmpty(situation.getEstateFormalityList())) {
            return situation.getEstateFormalityList().stream()
                    .sorted(Comparator.comparing(EstateFormality::getDate))
                    .map(form -> checkerNumRG(form.getNumRG(), form.getNumRP()) +
                            checkerNumRP(form.getNumRP()) + "<br/>" +
                            checkerDate(form.getDate()) + "<br/>")
                    .collect(Collectors.joining());
        }
        return "";
    }

    private String formalityBlock(EstateSituation situation) throws TypeFormalityNotConfigureException {
        String headerFormat = situation.getPropertyList().size() == 1
                ? ResourcesHelper.getString("estateSituationTableDescriptionFormatOne")
                : ResourcesHelper.getString("estateSituationTableDescriptionFormat");
        String formalityStr = "";
        if (!ValidationHelper.isNullOrEmpty(situation.getFormalityList())) {
            List<Formality> toSort = new ArrayList<>(situation.getFormalityList());
            toSort.sort(Comparator.comparing(Formality::getComparedDate)
                    .thenComparing(Formality::getGeneralRegister)
                    .thenComparing(Formality::getParticularRegister));
            StringJoiner joiner = new StringJoiner("<br/>");
            for (Formality formality : toSort) {
                String allFields = formality.getAllFields();
                joiner.add(allFields);
            }
            formalityStr = joiner.toString();
        }
        return !ValidationHelper.isNullOrEmpty(formalityStr) ? String.format(headerFormat, formalityStr) : "";
    }


    private String estateFormalityBlock(EstateSituation situation) {

        if (ValidationHelper.isNullOrEmpty(situation.getEstateFormalityList())
                && !ValidationHelper.isNullOrEmpty(situation.getCommentInit())) {
            return situation.getCommentInit() + "<br/><br/>";
        } else if (ValidationHelper.isNullOrEmpty(situation.getEstateFormalityList())) {
            return "";
        }
        String commentInit = "";
        if (!ValidationHelper.isNullOrEmpty(situation.getCommentInit())) {
            commentInit = situation.getCommentInit() + "<br/><br/>";
        }

        String firstPart;
        if (situation.getEstateFormalityList().size() == 1) {
            firstPart = String.format(ResourcesHelper.getString("estateFormalityReportFormatOne"),
                    getRepCommaString(situation.getEstateFormalityList().get(0).getCommentForTag()));
        } else {
            List<EstateFormality> estateFormalityList = situation.getEstateFormalityList();
            String listFormat = ResourcesHelper.getString("estateFormalityReportFormatListString");
            List<EstateFormality> sortedEstateFormality =
                    estateFormalityList.stream().sorted((f1, f2) -> (f1.getDate().compareTo(f2.getDate()))).collect(Collectors.toList());
            firstPart = IntStream.range(0, sortedEstateFormality.size())
                    .mapToObj(i -> String.format(listFormat, (char) (i + (int) 'a'),
                            getRepCommaString(sortedEstateFormality.get(i).getCommentForTag())))
                    .collect(Collectors.joining("",
                            ResourcesHelper.getString("estateFormalityReportFormat"), ""));
        }
        String endingFormat = SexTypes.MALE.getId().equals(situation.getRequest().getSubject().getSex()) ?
                ResourcesHelper.getString("estateFormalityReportFormatEndingM") :
                ResourcesHelper.getString("estateFormalityReportFormatEndingF");
        return String.format(endingFormat, commentInit, firstPart, situation.getRequest().getSubject().getFullNameCapitalize());
    }

    private String getRepCommaString(String str) {
        Pattern pat = Pattern.compile("(.+rep\\. [a-zA-Z0-9_\\/]+)()(.*)");
        Matcher matcher = pat.matcher(str);
        if (matcher.find()) {
            return matcher.replaceFirst("$1,$3");
        }
        return str;
    }

    private String generateEstateSituationSecondCol(EstateSituation situation) {
        if (!ValidationHelper.isNullOrEmpty(situation.getPropertyList())
                && !ValidationHelper.isNullOrEmpty(situation.getPropertyList().get(0))
                && !ValidationHelper.isNullOrEmpty(situation.getPropertyList().get(0).getCity())) {
            return situation.getPropertyList().get(0).getCity().getDescription();
        }
        return "";
    }
}
