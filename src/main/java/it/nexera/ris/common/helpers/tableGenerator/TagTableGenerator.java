package it.nexera.ris.common.helpers.tableGenerator;

import it.nexera.ris.common.enums.NoteType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.web.beans.pages.RequestTextEditBean;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.PartedPairsByCityWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.*;

public abstract class TagTableGenerator {

    class TagTableWrapper {
        int allRowNum;
        int counter;
        String estateFormalityDef;
        String cityDesc;
        int cityRowNumber;
        List<String> descriptionRows;
        List<Pair<String, String>> pairs;
    }
    static final String FORMALITY_SD_HEADER = ResourcesHelper.getString("formalityListSDHeader");
    static final String TEXT_CENTER = "text-align: center; ";
    static final String TEXT_JUSTIFY = "text-align: justify; ";
    static final String TEXT_RIGHT = "text-align: right; ";
    static final String TEXT_TOP = "vertical-align: top;";
    static final String HEADER_STYLE = "border-top: 0; border-left: 0; border-right: 0;";
    static final String WIDE_LINE = "line-height: 1.6;";
    static final String STANDARD_TABLE_WIDTH = "width: 670px; ";
    static final String FULL_SIZE_TABLE_WIDTH = "width: 770px; ";
    static final String STANDARD_TABLE_COLUMN_1_WIDTH = "width: 28px; ";
    static final String STANDARD_TABLE_COLUMN_2_WIDTH = "width: 95px; ";
    static final String STANDARD_TABLE_COLUMN_3_WIDTH = "width: 96px; ";
    static final String STANDARD_TABLE_COLUMN_4_WIDTH = "width: 470px; ";
    static final String COLUMN_PADDING = "padding-left:6px;padding-right:6px;";
    static final String BORDER = "border: 1px solid black;";
    static final String BORDER_NONE = "border: 0px;";

    static final String TABLE_OPEN = "<table align=\"left\" style=\"display: contents;border-collapse: collapse; " + STANDARD_TABLE_WIDTH + "\">";
    // be careful with TABLE_OPEN_FULLSIZE - it is for Certificazione template
    static final String TABLE_OPEN_FULLSIZE = "<table align=\"left\" style=\" border-collapse: collapse; font-size:14px; " +
                                              "margin-top: -9px; font-family: 'Calibri'; " + FULL_SIZE_TABLE_WIDTH + "\">";
    static final String TABLE_CLOSE = "</table>";

    static final String ROW_OPEN = "<tr>";
    static final String ROW_CLOSE = "</tr>";

    static final String FOOTER = ResourcesHelper.getString("footerRealEstateRelationshipTable");
    static final String ESTATE_FORMALITY_HEADER = ResourcesHelper.getString("formalityListHeader");

    private StringJoiner joiner;

    private Request request;

    private List<TagTableWrapper> tagTableList;

    private List<PartedPairsByCityWrapper> partedPairsByCityWrapperList;

    TagTableGenerator(Request request) {
        this.request = request;
        this.joiner = new StringJoiner(ROW_CLOSE + ROW_OPEN,
                TABLE_OPEN + ROW_OPEN, ROW_CLOSE + TABLE_CLOSE);
    }

    TagTableGenerator(Request request, boolean fullSize) {
        this.request = request;
        this.joiner = new StringJoiner(ROW_CLOSE + ROW_OPEN,
                TABLE_OPEN_FULLSIZE + ROW_OPEN, ROW_CLOSE + TABLE_CLOSE);
    }

    abstract void addBeginning() throws PersistenceBeanException, IllegalAccessException;

    abstract void fillTagTableList()
            throws TypeFormalityNotConfigureException, PersistenceBeanException, IllegalAccessException;

    public String compileTable() throws PersistenceBeanException, IllegalAccessException, TypeFormalityNotConfigureException, InstantiationException {
        String beforeTableText = addBeforeTableText();
        addHeader();
        addBeginning();
        addBody();
        addRequestComment();
        addEstateFormality();
        addFinalCost();
        addFooter();
        return beforeTableText + getJoiner().toString();
    }

    protected void addFinalCost() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        String prefix = ResourcesHelper.getString("requestCostOutput");

        addFormalityEmptyRow(1);

        CostCalculationHelper calculation = new CostCalculationHelper(getRequest());
        if ((getRequest().getCostButtonConfirmClicked() == null || !getRequest().getCostButtonConfirmClicked()) ||
                getRequest().getLastCostChanging()) {
            calculation.calculateAllCosts(false);
        }

        setRequest(DaoManager.get(Request.class, getRequest().getId()));

        if (!ValidationHelper.isNullOrEmpty(getRequest().getTotalCost())
                && !getRequest().getTotalCost().equals("0,00")
                && getRequest().getCostOutputCheck()) {

            getJoiner().add(getCell("", BORDER) +
                    getCellColspan("<b>" + prefix + getRequest().getTotalCost() + "</b>", 3, BORDER));
        }
        addFormalityEmptyRow(1);
    }

    void addRequestComment() {
        if (!ValidationHelper.isNullOrEmpty(getRequest().getComments())) {
            addEmptyRow(1);

            String note = "";
            if (getRequest().getComments().size() == 1) {
                note = ResourcesHelper.getString("requestCommentOne");
            } else {
                note = ResourcesHelper.getString("requestCommentMany");
            }

            StringBuffer sb = new StringBuffer();
            sb.append(getCell("", STANDARD_TABLE_COLUMN_1_WIDTH + BORDER));
            sb.append(getCell("", STANDARD_TABLE_COLUMN_2_WIDTH + BORDER));
            sb.append(getCell("", STANDARD_TABLE_COLUMN_3_WIDTH + BORDER));
            sb.append(getCell(note, TEXT_CENTER + "font-weight: bold;" + STANDARD_TABLE_COLUMN_4_WIDTH + BORDER));
            getJoiner().add(sb.toString());

            List<Comment> comments = getRequest().getComments();
            for (int i = 0; i < comments.size(); i++) {
                Comment comment = comments.get(i);
                addEmptyRow(1);
                if (getRequest().getComments().size() > 1) {
                    sb = new StringBuffer();
                    sb.append(getCell("", STANDARD_TABLE_COLUMN_1_WIDTH + BORDER));
                    sb.append(getCell("", STANDARD_TABLE_COLUMN_2_WIDTH + BORDER));
                    sb.append(getCell("", STANDARD_TABLE_COLUMN_3_WIDTH + BORDER));
                    sb.append(getCell(String.valueOf(Character.toUpperCase(Character.forDigit(10 + i, Character.MAX_RADIX))) + ")"
                            , TEXT_CENTER + "font-weight: bold;" + STANDARD_TABLE_COLUMN_4_WIDTH + BORDER));
                    getJoiner().add(sb.toString());
                }
                sb = new StringBuffer();
                sb.append(getCell("", STANDARD_TABLE_COLUMN_1_WIDTH + BORDER));
                sb.append(getCell("", STANDARD_TABLE_COLUMN_2_WIDTH + BORDER));
                sb.append(getCell("", STANDARD_TABLE_COLUMN_3_WIDTH + BORDER));
                sb.append(getCell(comment.getComment(), TEXT_JUSTIFY + STANDARD_TABLE_COLUMN_4_WIDTH + BORDER));
                getJoiner().add(sb.toString());
            }
        }

    }

    String addBeforeTableText() {
        return "";
    }

    void addEstateFormality() throws PersistenceBeanException, IllegalAccessException, TypeFormalityNotConfigureException {

        List<EstateFormality> shortUnsortedList = DaoManager.load(EstateFormality.class, new CriteriaAlias[]{
                new CriteriaAlias("requestFormalities", "request", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("accountable", true),
                Restrictions.eq("request.request.id", getRequest().getId()),
                Restrictions.isNotNull("landChargesRegistry")
        });
        if (ValidationHelper.isNullOrEmpty(shortUnsortedList)) {
            return;
        }
        addFormalityEmptyRow(2);
        getJoiner().add(getCell("", BORDER) +
                getCellColspan(ESTATE_FORMALITY_HEADER, 3, BORDER));

        TreeMap<String, List<EstateFormality>> groups =
                new TreeMap<>();

        for (EstateFormality f : shortUnsortedList) {
            String landRegistryName = f.getLandChargesRegistry().getName();

            groups.computeIfAbsent(landRegistryName, k -> new ArrayList<>());

            groups.get(landRegistryName).add(f);
        }

        List<EstateFormality> shortList = new ArrayList<>();

        for (List<EstateFormality> list : groups.values()) {
            list.sort(Comparator.comparing(EstateFormality::getDate));

            shortList.addAll(list);
        }

        long numOfLand = groups.size();

        for (int i = 0; i < shortList.size(); i++) {
            EstateFormality f = shortList.get(i);
            if (numOfLand != 1L && (i == 0 || !f.getLandChargesRegistry().getName()
                    .equals(shortList.get(i - 1).getLandChargesRegistry().getName()))) {
                getJoiner().add(getCell("", WIDE_LINE + BORDER) +
                        getCellColspan("<b>" + f.getLandChargesRegistry().getName() + "</b>",
                                3, WIDE_LINE + BORDER));

            }
            String s = (f.getTypeAct() != null ? f.getTypeAct().getType().getEditorValue() : "") + " "
                    + (f.getEstateFormalityType() != null ? f.getEstateFormalityType().toString() : "") + " "
                    + checkerNumRG(f.getNumRG(), f.getNumRP()) + checkerNumRP(f.getNumRP())
                    + " del " + checkData(DateTimeHelper.toFormatedString(f.getDate(), "dd.MM.yyyy")) + " "
                    + f.getChangedTypeAct();
            getJoiner().add(getCell("", WIDE_LINE + BORDER) +
                    getCellColspan(s, 3, WIDE_LINE + BORDER));
            if (f.getShowToggler()) {
                for (String annText : f.getExpansionText(NoteType.NOTE_TYPE_A)) {
                    getJoiner().add(getCell("", WIDE_LINE + BORDER) +
                            getCellColspan("&nbsp;&nbsp;&nbsp;<span style=\"font-size:14px;\">&bull;</span> "
                                    + annText, 3, WIDE_LINE + BORDER));
                }
            }
            if (!ValidationHelper.isNullOrEmpty(f.getCommunications())) {
                for (Communication com : f.getCommunications()) {
                    getJoiner().add(getCell("", WIDE_LINE + BORDER) +
                            getCellColspan("<nobr>&nbsp;&nbsp;&nbsp;<span style=\"font-size:14px;\">&bull;</span> "
                                    + f.getCommunicationRow(com) + "</nobr>", 3, WIDE_LINE + BORDER));
                }
            }
        }
        addFormalityEmptyRow(4);
    }

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
            if(wrapper.descriptionRows.size() > 0)
                sb.append(getCell(wrapper.descriptionRows.get(0), COLUMN_PADDING + STANDARD_TABLE_COLUMN_4_WIDTH +
                    "border-left: 1px solid black;border-right: 1px solid black;border-top: 1px solid black;"));
            getJoiner().add(sb.toString());
            for (int i = 1; i < wrapper.descriptionRows.size(); i++) {
                boolean last = (i == (wrapper.descriptionRows.size() - 1));
                sb = new StringBuffer();
                if (wrapper.cityRowNumber == i) {
                    sb.append(getCell(wrapper.cityDesc, TEXT_CENTER + TEXT_TOP + "font-weight: bold;"
                            + STANDARD_TABLE_COLUMN_3_WIDTH + (last ? "border-bottom: 1px solid black;" : "")));
                } else {
                    sb.append(getCell("", last ? "border-bottom: 1px solid black;" : ""));
                }
                sb.append(getCell(wrapper.descriptionRows.get(i), COLUMN_PADDING + STANDARD_TABLE_COLUMN_4_WIDTH +
                        "border-left: 1px solid black;border-right: 1px solid black;"
                        + (last ? "border-bottom: 1px solid black;" : "")));
                getJoiner().add(sb.toString());
            }
            notFirst = true;
        }
    }

    protected void addHeader() {
        String sb = getCell("N.", HEADER_STYLE + TEXT_CENTER + STANDARD_TABLE_COLUMN_1_WIDTH) +
                getCell("PROVEN.", HEADER_STYLE + TEXT_CENTER + STANDARD_TABLE_COLUMN_2_WIDTH) +
                getCell("COMUNE", HEADER_STYLE + TEXT_CENTER + STANDARD_TABLE_COLUMN_3_WIDTH) +
                getCell("OGGETTO", HEADER_STYLE + TEXT_CENTER + STANDARD_TABLE_COLUMN_4_WIDTH);
        getJoiner().add(sb);
    }

    protected void addFooter() {
        getJoiner().add(getCellColspan(FOOTER, 4, COLUMN_PADDING + BORDER));
    }

    private void addFormalityEmptyRow(int number) {
        for (int i = 0; i < number; i++) {
            getJoiner().add(getCell("", BORDER) +
                    getCellColspan("", 3, BORDER));
        }
    }

    void addEmptyRow(int number) {
        for (int i = 0; i < number; i++) {
            getJoiner().add(getCell("", BORDER) +
                    getCell("", BORDER) +
                    getCell("", BORDER) +
                    getCell("", BORDER));
        }
    }

    String getCell(String data, String style) {
        return String.format("<td style=\"%s\">%s</td>",
                style, data == null ? "" : data);
    }

    private String getCellColspan(String data, int colspan, String style) {
        return String.format("<td style=\"%s\" colspan=\"%s\">%s</td>",
                style, colspan, data == null ? "" : data);
    }

    String getCellRowspan(String data, int rowspan, String style) {
        return String.format("<td style=\"%s\" rowspan=\"%s\">%s</td>",
                style, rowspan, data == null ? "" : data);
    }

    String checkerNumRG(Integer value, String check) {
        String result = ValidationHelper.isNullOrEmpty(value) ? "" : value.toString();
        if (!ValidationHelper.isNullOrEmpty(check) && !ValidationHelper.isNullOrEmpty(result)) {
            return result + "/";
        } else if (ValidationHelper.isNullOrEmpty(check) && !ValidationHelper.isNullOrEmpty(result)) {
            return result;
        } else {
            return "";
        }
    }

    public void addSalesDevelopmentFormalities() throws HibernateException, IllegalAccessException, PersistenceBeanException, TypeFormalityNotConfigureException {
        if(!getRequest().getSalesDevelopment()) {
            return;
        }
        List<EstateSituation> estateSituations = DaoManager.load(EstateSituation.class, new Criterion[]{
                Restrictions.eq("request.id", getRequest().getId()),
                Restrictions.eq("salesDevelopment", Boolean.TRUE)
        });

        if (ValidationHelper.isNullOrEmpty(estateSituations)) {
            return;
        }
        estateSituations.sort(new RequestTextEditBean.SortByInnerEstateFormalityDate());
        List<Formality> formalities = estateSituations.get(0).getFormalityList();
        if (ValidationHelper.isNullOrEmpty(formalities)) {
            return;
        }
        formalities.sort(Comparator.comparing(Formality::getComparedDate)
                .thenComparing(Formality::getGeneralRegister)
                .thenComparing(Formality::getParticularRegister));
        setTagTableList(new ArrayList<>());
        int counter = 1;
        fillPartedPairsByCityWrapper(formalities, Boolean.FALSE);

        for (PartedPairsByCityWrapper partedPairsByCityWrapper : getPartedPairsByCityWrapperList()) {
            for (List<Pair<String, String>> pairList : partedPairsByCityWrapper.getPatredList()) {
                TagTableWrapper wrapper = new TagTableWrapper();
                wrapper.counter = counter;
                wrapper.cityDesc = partedPairsByCityWrapper.getFormality().getFirstPropertyAlienatedTable() ;
                wrapper.descriptionRows = new ArrayList<>();
                wrapper.pairs = new ArrayList<>();
                String formalityBlock = "";
                StringBuffer sb = new StringBuffer();
                if(!ValidationHelper.isNullOrEmpty(partedPairsByCityWrapper.getFormality().getType())){
                    sb.append(partedPairsByCityWrapper.getFormality().getType());
                    sb.append(" ");
                }
                sb.append("N.RI ");
                if(!ValidationHelper.isNullOrEmpty(partedPairsByCityWrapper.getFormality().getGeneralRegister())){
                    sb.append(partedPairsByCityWrapper.getFormality().getGeneralRegister());
                    sb.append("/");
                }
                if(!ValidationHelper.isNullOrEmpty(partedPairsByCityWrapper.getFormality().getParticularRegister())){
                    sb.append(partedPairsByCityWrapper.getFormality().getParticularRegister());
                    sb.append(" ");
                }
                sb.append("del ");
                if(!ValidationHelper.isNullOrEmpty(partedPairsByCityWrapper.getFormality().getPresentationDate())){
                    sb.append( DateTimeHelper.toString(partedPairsByCityWrapper.getFormality().getPresentationDate()));
                    sb.append(" ");
                }
                if(sb.length() > 0){
                    String prefix =  sb.toString().toUpperCase() + "</div>";
                    sb.setLength(0);
                    sb.append("<div style=\"text-align: justify;font-weight: bold;\">");
                    sb.append(prefix);
                    sb.append("</div>");
                }
                String  data = partedPairsByCityWrapper.getFormality().getSubjectAlienatedTable(getRequest());
                if (!ValidationHelper.isNullOrEmpty(data)) {
                    sb.append(data);
                }
                formalityBlock = sb.toString();
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

        if (ValidationHelper.isNullOrEmpty(getTagTableList())) {
            return;
        }
        boolean notFirst = false;
        for (TagTableWrapper wrapper : getTagTableList()) {
            if (notFirst) {
                addEmptyRow(1);
            } else {
                addEmptyRow(2);
                getJoiner().add(getCellColspan("***", 4, BORDER + TEXT_CENTER + "font-weight: bold;"));
                getJoiner().add(
                        getCellColspan(FORMALITY_SD_HEADER, 4, BORDER + TEXT_CENTER + "font-weight: bold;"));
                addEmptyRow(1);
            }
            StringBuffer sb = new StringBuffer();
            sb.append(getCellRowspan("", wrapper.allRowNum,
                    TEXT_CENTER + TEXT_TOP + STANDARD_TABLE_COLUMN_1_WIDTH + BORDER));
            sb.append(getCellRowspan(wrapper.estateFormalityDef, wrapper.allRowNum,
                    TEXT_CENTER + TEXT_TOP + STANDARD_TABLE_COLUMN_2_WIDTH + BORDER));
            sb.append(getCell(wrapper.counter + ")", "vertical-align: text-top;text-align: right;"));

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
        fillPartedPairsByCityWrapper(formalities, Boolean.FALSE);
    }

    protected void fillPartedPairsByCityWrapper(List<Formality> formalities, Boolean addCommercialAndOmi)
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

    String checkerNumRP(String value) {
        return ValidationHelper.isNullOrEmpty(value) ? "" : value;
    }

    String checkerDate(Date value) {
        return ValidationHelper.isNullOrEmpty(value) ? "" : DateTimeHelper.toString(value);
    }

    private String checkData(String value) {
        return ValidationHelper.isNullOrEmpty(value) ? "" : value;
    }

    public StringJoiner getJoiner() {
        return joiner;
    }

    public void setJoiner(StringJoiner joiner) {
        this.joiner = joiner;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public List<TagTableWrapper> getTagTableList() {
        return tagTableList;
    }

    public void setTagTableList(List<TagTableWrapper> tagTableList) {
        this.tagTableList = tagTableList;
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
