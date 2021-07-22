package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.logic.RelationshipGroupingWrapper;
import it.nexera.ris.web.beans.wrappers.logic.TemplateEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TemplatePdfTableHelper {

    private static final String PROPERTY_CADASTRAL_CATEGORY_CODE_FOR_LAND_PROPERTY_BLOCK = "R";

    public transient final static Log log = LogFactory.getLog(TemplatePdfTableHelper.class);

    public static String makeRealEstateReportNote() {
        return passImage("/request_estate.png", "width=624 height=47");
    }

    public static String getSubjectRegistry(Request request) {
        if (ValidationHelper.isNullOrEmpty(request) || ValidationHelper.isNullOrEmpty(request.getSubject())) {
            return "";
        }

        String city = "";
        String province = "";
        Subject subject = request.getSubject();

        if (subject.getForeignCountry() != null &&
                subject.getForeignCountry()) {
            if (!ValidationHelper.isNullOrEmpty(subject.getCountry())) {
                city = subject.getCountry().getDescription();
            }
            province = "EE";
        } else {
            city = subject.getBirthCity() == null ? "" :
                    subject.getBirthCity().getDescription();
            province = subject.getBirthProvince() == null ? "" :
                    subject.getBirthProvince().getCode();
        }

        if (subject.getTypeIsPhysicalPerson()) {
            return String.format("%s %s<br/>%s%s (%s) il %s<br/>C.F. %s",
                    subject.getSurname() == null ? "" : subject.getSurname().toUpperCase(),
                    subject.getName() == null ? "" : subject.getName().toUpperCase(),
                    SexTypes.MALE.getId().equals(subject.getSex()) ? "nato a " : "nata a ",
                    city,
                    province,
                    subject.getBirthDate() == null ? "" :
                            DateTimeHelper.toStringDateWithDots(subject.getBirthDate()),
                    subject.getFiscalCode() == null ? "" : subject.getFiscalCode());
        } else {
            return String.format("%s&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;P.iva %s<br/>con sede in %s (%s)",
                    subject.getBusinessName() == null ? "" : subject.getBusinessName().toUpperCase(),
                    subject.getNumberVAT() == null ? "" : subject.getNumberVAT(),
                    city,
                    province);
        }
    }

    public static String getEstateFormalityConservationDate(Request request)
            throws PersistenceBeanException, IllegalAccessException {
        Date group = (Date) DaoManager.getMin(RequestConservatory.class, "conservatoryDate",
                new CriteriaAlias[]{}, new Criterion[]{
                        Restrictions.eq("request.id", request.getId())
                });
        if (group != null) {
            return DateTimeHelper.toString(group);
        } else return "";
    }

    public static List<String> groupPropertiesByQuoteTypeList(List<Property> propertyList, Subject subject,
                                                              boolean filterRelationship,
                                                              boolean showCadastralIncome,
                                                              boolean showAgriculturalIncome) {
        for (Property property : propertyList) {
            if (!ValidationHelper.isNullOrEmpty(property.getCategoryCode()) && !RealEstateType.LAND.getShortValue().equals(property.getCategoryCode())) {
                property.setType(RealEstateType.BUILDING.getId());
            }
        }
        Map<List<RelationshipGroupingWrapper>, List<Property>> re = new HashMap<>();
        wrapProperties(propertyList, subject, filterRelationship, re);
        List<String> joiner = new ArrayList<>();
        for (Iterator<Map.Entry<List<RelationshipGroupingWrapper>, List<Property>>> iterator = re.entrySet().iterator();
             iterator.hasNext(); ) {
            Map.Entry<List<RelationshipGroupingWrapper>, List<Property>> entry = iterator.next();
            constructTableText(joiner, iterator, entry, true, showCadastralIncome, showAgriculturalIncome);
        }
        return joiner;
    }

    public static List<Pair<String, String>> groupPropertiesByQuoteTypeListLikePairs(List<Property> propertyList, Subject subject,
                                                                                     List<Subject> presumableSubjects,
                                                                                     boolean filterRelationship, Formality formality,
                                                                                     boolean showCadastralIncome,
                                                                                     boolean showAgriculturalIncome) {

        return groupPropertiesByQuoteTypeListLikePairs(propertyList, subject, presumableSubjects, filterRelationship, formality, showCadastralIncome, showAgriculturalIncome, Boolean.FALSE);
    }
    public static List<Pair<String, String>> groupPropertiesByQuoteTypeListLikePairs(List<Property> propertyList, Subject subject,
                                                                                     List<Subject> presumableSubjects,
                                                                                     boolean filterRelationship, Formality formality,
                                                                                     boolean showCadastralIncome,
                                                                                     boolean showAgriculturalIncome, Boolean addCommercialAndOmi) {
        for (Property property : propertyList) {
            if (!ValidationHelper.isNullOrEmpty(property.getCategoryCode())
                    && !RealEstateType.LAND.getShortValue().equals(property.getCategoryCode())) {
                property.setType(RealEstateType.BUILDING.getId());
            }
        }
        Map<List<RelationshipGroupingWrapper>, List<Property>> re = new HashMap<>();
        wrapPropertiesAliente(propertyList, subject, presumableSubjects, filterRelationship, re, formality);

        Map<List<RelationshipGroupingWrapper>, List<Property>> sortedMap = sortByValue(re);
        String city = "";
        List<Pair<String, String>> result = new ArrayList<>();

        for (Iterator<Map.Entry<List<RelationshipGroupingWrapper>, List<Property>>> iterator = sortedMap.entrySet().iterator();
             iterator.hasNext(); ) {
            List<String> joiner = new ArrayList<>();
            Map.Entry<List<RelationshipGroupingWrapper>, List<Property>> entry = iterator.next();
            constructTableText(joiner, iterator, entry, addCommercialAndOmi, showCadastralIncome, showAgriculturalIncome);
            String currentCity = entry.getValue().stream().map(p -> p.getCity().getDescription()).findFirst().get();

            for (int i = 0; i < joiner.size(); i++) {
                String s = joiner.get(i);

                if (city.isEmpty() && s.contains("-")) {
                    city = currentCity;

                    result.add(new Pair<>(city, s));
                } else if (!city.equals(currentCity) && s.contains("-")) {
                    city = currentCity;
                    result.add(new Pair<>(city, s));
                } else if (((joiner.size() - 1) == i) && "".equals(s)) {
                    //don't print an empty line in the end of block
                    // when it's one formality, but different cities
                } else {
                    result.add(new Pair<>("", s));
                }
            }
        }
        return result;
    }

    private static void constructTableText(List<String> joiner, Iterator<Map.Entry<List<RelationshipGroupingWrapper>,
            List<Property>>> iterator, Map.Entry<List<RelationshipGroupingWrapper>, List<Property>> entry, boolean addCommercialAndOmi, 
            boolean showCadastralIncome,boolean showAgriculturalIncome) {
        joiner.add(entry.getKey().stream()
                .map(p -> String.format("DIRITTI PARI A %s %s %s",
                        p.getQuote(), p.getManagedPropertyType(), p.getExpectedRegimeFormat()))
                .collect(Collectors.joining("<br/>", "<div align=\"center\"><b>", "</b></div>")));

        List<Property> freeProperty = entry.getValue();
        String landData = freeProperty.stream().filter(p ->
                PROPERTY_CADASTRAL_CATEGORY_CODE_FOR_LAND_PROPERTY_BLOCK.equals(p.getCategoryCode())
                        && p.isLandDataAreExistAndNotEmpty()).map(x ->
                x.getAllFields(addCommercialAndOmi, true)).collect(Collectors.joining("<br />"));

        
        freeProperty = freeProperty.stream().filter(p -> !PROPERTY_CADASTRAL_CATEGORY_CODE_FOR_LAND_PROPERTY_BLOCK.equals(p.getCategoryCode())
                || !p.isLandDataAreExistAndNotEmpty()).collect(Collectors.toList());
        String bilding = freeProperty.stream().filter(p -> p.getType().equals(RealEstateType.BUILDING.getId()))
                .map(x -> x.getAllFields(addCommercialAndOmi, false)).collect(Collectors.joining("<br />"));
        if (!ValidationHelper.isNullOrEmpty(bilding)) {
            joiner.add("");
            joiner.add(bilding);
        }
        if (!ValidationHelper.isNullOrEmpty(landData)) {
            joiner.add("");
            joiner.add(landData);
        }

        Map<String, List<Property>> map = new HashMap<>();
        List<Property> properties = freeProperty.stream()
                .filter(p -> p.getType() != null)
                .filter(p -> p.getType()
                .equals(RealEstateType.LAND.getId())).collect(Collectors.toList());

        for (Property property : properties) {
            if (map.containsKey(property.getSheets())) {
                map.get(property.getSheets()).add(property);
            } else {
                map.put(property.getSheets(), new ArrayList<>());
                map.get(property.getSheets()).add(property);
            }
        }
        for (Map.Entry<String, List<Property>> stringListEntry : map.entrySet()) {
            String land = landPropertyBlock(stringListEntry.getValue(), showCadastralIncome, showAgriculturalIncome);
            if (!ValidationHelper.isNullOrEmpty(land)) {
                joiner.add("");
                joiner.add(land);
            }
        }
        if (iterator.hasNext()) {
            joiner.add("");
        }
    }

    public static void wrapProperties(List<Property> propertyList, Subject subject, boolean filterRelationship,
                                      Map<List<RelationshipGroupingWrapper>, List<Property>> re) {
        wrapProperties(propertyList, subject, filterRelationship, re, null);
    }

    public static void wrapProperties(List<Property> propertyList, Subject subject, boolean filterRelationship,
                                      Map<List<RelationshipGroupingWrapper>, List<Property>> re, Formality formality) {
        for (Property property : propertyList) {
            List<RelationshipGroupingWrapper> pairs = new LinkedList<>();
            List<Relationship> relationshipList = getRelationships(subject, formality, property);
            wrapRelationshipProperty(subject, filterRelationship, re, property, pairs, relationshipList);
        }
    }

    public static void wrapPropertiesAliente(List<Property> propertyList, Subject subject, List<Subject> presumableSubjects,
                                             boolean filterRelationship,
                                             Map<List<RelationshipGroupingWrapper>, List<Property>> re, Formality formality) {
        for (Property property : propertyList) {
            List<RelationshipGroupingWrapper> pairs = new LinkedList<>();
            List<Relationship> relationshipList = new ArrayList<>();
            for (Subject sub : presumableSubjects) {
                relationshipList.addAll(getRelationships(sub, formality, property));
            }
            wrapRelationshipProperty(subject, filterRelationship, re, property, pairs, relationshipList);
        }
    }

    private static void wrapRelationshipProperty(Subject subject, boolean filterRelationship,
                                                 Map<List<RelationshipGroupingWrapper>, List<Property>> re,
                                                 Property property, List<RelationshipGroupingWrapper> pairs,
                                                 List<Relationship> relationshipList) {
        if (filterRelationship) {
            relationshipList = relationshipList.stream()
                    .filter(r -> r.getRelationshipTypeId().equals(RelationshipType.MANUAL_ENTRY.getId()))
                    .collect(Collectors.toList());
        }
        if (ValidationHelper.isNullOrEmpty(relationshipList) && !ValidationHelper.isNullOrEmpty(subject)) {
            relationshipList = property.getRelationships().stream()
                    .filter(r -> r.getSubject() != null)
                    .filter(r -> r.getSubject().getId().equals(subject.getId())
                            && r.getRelationshipTypeId().equals(RelationshipType.CADASTRAL_DOCUMENT.getId()))
                    .collect(Collectors.toList());
        }
        for (Relationship relationship : relationshipList) {
            if (relationship.getQuote() != null && relationship.getPropertyType() != null) {
                RelationshipGroupingWrapper relationshipGroupingWrapper = new RelationshipGroupingWrapper(
                        relationship.getQuote() == null ? "" : relationship.getQuote(),
                        relationship.getPropertyType().equalsIgnoreCase("Proprieta`")
                                || relationship.getPropertyType().equalsIgnoreCase("Proprieta'")
                                ? "DI PIENA PROPRIETA'" : "DI " + relationship.getPropertyType().toUpperCase(),
                        relationship.getRegime() == null ? "" : relationship.getRegime(),
                        relationship.getProperty().getCity());
                if (pairs.stream().noneMatch(p -> p.equals(relationshipGroupingWrapper))) {
                    pairs.add(relationshipGroupingWrapper);
                }
            }
        }
        List<RelationshipGroupingWrapper> fromMap = re.keySet().stream()
                .filter(l -> l.size() == pairs.size()).filter(pairs::containsAll).findAny().orElse(null);
        if (fromMap == null) {
            fromMap = pairs;
        }
        re.computeIfAbsent(fromMap, k -> new ArrayList<>()).add(property);
    }


    private static List<Relationship> getRelationships(Subject subject, Formality formality, Property property) {
        if (!ValidationHelper.isNullOrEmpty(formality) && !ValidationHelper.isNullOrEmpty(subject)) {
            return property.getRelationships().stream()
                    .filter(r -> r.getSubject().getId().equals(subject.getId()))
                    .filter(r -> r.getFormality().getId().equals(formality.getId()))
                    .collect(Collectors.toList());
        } else if (!ValidationHelper.isNullOrEmpty(subject)) {
            return property.getRelationships().stream()
                    .filter(r -> r.getSubject() != null)
                    .filter(r -> r.getSubject().getId().equals(subject.getId()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<Relationship>();
    }

    private static String landPropertyBlock(List<Property> propertyList,boolean showCadastralIncome,
            boolean showAgriculturalIncome) {
        List<Property> landProperties = ValidationHelper.isNullOrEmpty(propertyList) ? null :
                propertyList.stream().filter(p -> ValidationHelper.isNullOrEmpty(p.getCategory())
                        || !PROPERTY_CADASTRAL_CATEGORY_CODE_FOR_LAND_PROPERTY_BLOCK.equals(p.getCategory().getCode()))
                        .collect(Collectors.toList());
        if (ValidationHelper.isNullOrEmpty(landProperties)) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        str.append("<div style=\"white-space:normal;\">- terreno<br/><table style=\"border:none;margin-left: 0;" +
                "padding-left:0;margin-right:auto;padding-right:auto; width:auto;\">");
        List<Pair<CadastralData, Property>> dataList = new ArrayList<>();
        for (Property property : landProperties) {
            for (CadastralData cadastralData : property.getCadastralData()) {
                dataList.add(new Pair<>(cadastralData, property));
            }
        }
        dataList.sort(Comparator.comparing(p -> p.getFirst().getSheet()));
        for (int i = 0; i < dataList.size(); i++) {
            CadastralData data = dataList.get(i).getFirst();
            Property property = dataList.get(i).getSecond();
            str.append("<tr>").append("<td style=\"border:none;\">");
            if (i == 0) {
                str.append("<span style=\"font-weight:normal;\">").append("In&nbsp;catasto").append("</span>");
            } else {
                str.append("<span style=\"font-weight:normal;\">").append("</span>");
            }
            str.append("</td>").append("<td style=\"border:none;\">");
            if (i == 0 || !data.getSheet().equals(dataList.get(i - 1).getFirst().getSheet())) {
                str.append("<span style=\"font-weight:bold;\">").append("&nbsp;foglio&nbsp;").append(data.getSheet())
                        .append("</span>");
            }
            str.append("</td>").append("<td style=\"border:none;\">");
            str.append("<span style=\"font-weight:bold;\">").append("&nbsp;p.lla&nbsp;").append(data.getParticle())
                    .append("</span>");
            str.append("</td>").append("<td style=\"border:none;\">");
            str.append("<span style=\"font-weight:normal;\">");

            optimizePropertyParameters(property);
            str.append("&nbsp;mq&nbsp;");
            String landMQ= property.getTagLandMQ();
            if(landMQ.endsWith(".00") || landMQ.endsWith(".0"))
                landMQ = landMQ.substring(0, landMQ.lastIndexOf("."));
            if(!landMQ.contains(".") && !landMQ.contains(",")){
                landMQ = GeneralFunctionsHelper.formatDoubleString(landMQ);
            }
            str.append(landMQ);
            str.append("</span>");
            str.append("</td>");
            str.append("</tr>");
            if (showCadastralIncome || showAgriculturalIncome){
                if(!ValidationHelper.isNullOrEmpty(property.getAgriculturalIncome())|| 
                        !ValidationHelper.isNullOrEmpty(property.getCadastralIncome())) {
                    str.append("<tr>");
                    str.append("<td style=\"border:none;\">");
                    str.append("</td>");
                    str.append("<td colspan=\"3\" style=\"border:none;\">");
                  
                    if (showAgriculturalIncome) {
                        str.append("<span>");
                        str.append("(Red. agr. € ").append(property.getAgriculturalIncome());
                        if(!showCadastralIncome)
                            str.append(")");
                        else
                            str.append(",");
                        str.append("</span>");
                    }
                    if (showCadastralIncome) {
                        str.append("<span>");
                         if(!showAgriculturalIncome)
                            str.append("(");
                         else
                             str.append("&nbsp;&nbsp;");
                         str.append("Red. dom. € ").append(property.getCadastralIncome());
                          str.append(")");
                        str.append("</span>");
                    }
                 
                    str.append("</tr>");
                }
               
            }
        }
        str.append("</table></div>");
        str.append(propertyList.stream().filter(p -> !ValidationHelper.isNullOrEmpty(p.getComment())
                && !p.getComment().equals(ResourcesHelper.getString("propertyCommentDefaultValue")))
                .map(Property::getComment).collect(Collectors.joining("<br/>", "<i>", "</i>")));
        
        return str.toString();
        
    }

    private static void optimizePropertyParameters(Property property) {
        Integer centiares = ValidationHelper.isNullOrEmpty(property.getCentiares()) ? 0 : property.getCentiares().intValue();
        Integer ares = ValidationHelper.isNullOrEmpty(property.getAres()) ? 0 : property.getAres().intValue();
        Integer hectares = ValidationHelper.isNullOrEmpty(property.getHectares()) ? 0 : property.getHectares().intValue();

        Integer temp;
        if (centiares >= 100) {
            temp = centiares % 100;
            ares += ((centiares - temp) / 100);
            centiares = temp;
        }
        if (ares >= 100) {
            temp = ares % 100;
            hectares += ((ares - temp) / 100);
            ares = temp;
        }

        property.setCentiares(Double.valueOf(centiares));
        property.setAres(Double.valueOf(ares));
        property.setHectares(Double.valueOf(hectares));
    }

    private static String passExternalImage(String url, String additionalStyle) {
        try {
            return "<img src=\"" + url + "\"" + additionalStyle + ">";
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    private static String passImage(String projectUrl, String additionalStyle) {
        try {
            String resourcesPath = "/resources/images/";
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            String url = new URL(request.getScheme(),
                    request.getServerName(), request.getServerPort(),
                    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
                            + resourcesPath + projectUrl).toString();
            return "<img src=\"" + url + "\"" + additionalStyle + ">";
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    public static String makeSubjectCadastralTable(Subject subject) {
        if (subject != null) {
            try {
                Set<Relationship> relationshipsSet = loadCadastralRelationships(subject);

                if (!ValidationHelper.isNullOrEmpty(relationshipsSet)) {
                    StringBuilder rows = new StringBuilder();

                    rows.append(createSubjectCadastralTableHead());

                    for (Relationship r : relationshipsSet) {
                        rows.append(createSubjectCadastralTableRow(r));
                    }

                    rows.append("</table>");

                    return rows.toString();
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return "";
    }

    public static Set<Relationship> loadCadastralRelationships(Subject subject)
            throws PersistenceBeanException, IllegalAccessException {
        List<Relationship> relationshipsList = DaoManager.load(Relationship.class, new Criterion[]{
                Restrictions.eq("subject.id", subject.getId()),
                Restrictions.eq("relationshipTypeId",
                        RelationshipType.CADASTRAL_DOCUMENT.getId())
        });

        return relationshipsList.stream()
                .filter(distinctByKey(Relationship::getTableId))
                .collect(Collectors.toSet());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static String makeSubjectFormalityTable(Subject subject) {
        if (subject != null) {
            try {
                List<Formality> formalities = loadFormalitiesBySubject(subject);

                if (!ValidationHelper.isNullOrEmpty(formalities)) {
                    StringBuffer rows = new StringBuffer();

                    rows.append(createSubjectFormalityTableHead());

                    formalities.stream().distinct().forEach(f -> rows.append(createSubjectFormalityTableRow(f)));

                    rows.append("</table>");

                    return rows.toString();
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return "";
    }

    public static String makeSubjectLandRegisterTable(Subject subject) {
        if (subject != null) {
            try {
                StringBuilder rows = new StringBuilder();
                rows.append("<p style=\"text-align: center; font-size: 20px; font-family: Times New Roman; font-weight: bold; margin: 0\">");
                rows.append(ResourcesHelper.getString("requestTextEditNationalResearch"));
                rows.append(ResourcesHelper.getString("requestTextEditUpdatedSituation"));
                long resultTime = subject.getCreateDate().getTime() - 604_800_000L; // creation date minus 7 days in milliseconds
                String dateStr = DateTimeHelper.toString(new Date(resultTime));
                rows.append(dateStr).append("</b></br>");
                rows.append(ResourcesHelper.getString("reqeustTextEditResearchData"));
                rows.append(ResourcesHelper.getString("reqeustTextEditRestrictedSearch"));
                rows.append(ResourcesHelper.getString("reqeustTextEditLandRegistry"));

                if (subject.getTypeId().equals(2L)) {
                    rows.append(ResourcesHelper.getString("reqeustTextEditJuridicalName"));
                    rows.append(subject.getBusinessName().toUpperCase()).append("</b></br>");
                    rows.append(ResourcesHelper.getString("reqeustTextEditVatNumber"))
                            .append(subject.getNumberVAT()).append("</b></br>");
                } else {
                    rows.append(ResourcesHelper.getString("requestTextEditSurname"))
                            .append(subject.getSurname().toUpperCase()).append("</b>&nbsp;&nbsp;&nbsp;&nbsp;");
                    rows.append(ResourcesHelper.getString("reqeustTextEditName"))
                            .append(subject.getName().toUpperCase()).append("</b></br>");
                    rows.append(ResourcesHelper.getString("requestTextEditDateOfBirth"));
                    StringBuilder dateBuilder = new StringBuilder();
                    dateStr = DateTimeHelper.toString(subject.getBirthDate());
                    dateBuilder.append(" <b>").append(dateStr).append("</b>");
                    rows.append(dateBuilder.toString()).append("</br>");
                }

                rows.append(ResourcesHelper.getString("requestTextEditNamesake"));
                rows.append(ResourcesHelper.getString("requestTextEditListOfNameSake"));
                return rows.toString();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return "";
    }

    public static List<Formality> loadFormalitiesBySubject(Subject subject)
            throws PersistenceBeanException, IllegalAccessException {
        List<Long> documentIds = DaoManager.loadField(
                Relationship.class, "tableId", Long.class, new Criterion[]{
                        Restrictions.eq("subject.id", subject.getId()),
                        Restrictions.eq("relationshipTypeId", 2L)
                });

        if (ValidationHelper.isNullOrEmpty(documentIds)) {
            documentIds = Collections.singletonList(0L);
        }

        return DaoManager.load(Formality.class, new Criterion[]{
                Restrictions.in("document.id", documentIds)
        });
    }

    private static String createSubjectCadastralTableRow(Relationship relationship) {
        StringBuilder sb = new StringBuilder();

        sb.append("<tr>");

        appendTd(relationship.getDocumentTitle(), sb);
        appendTd(relationship.getDocumentPath(), sb);
        appendTd(DateTimeHelper.toString(relationship.getCreateDate()), sb);

        sb.append("</tr>");

        return sb.toString();
    }

    private static String createSubjectFormalityTableRow(Formality formality) {
        StringBuilder sb = new StringBuilder();

        sb.append("<tr>");

        appendTd(formality.getProvincialOfficeName(), sb);
        appendTd(formality.getType(), sb);
        appendTd(formality.getInspectionDateStr(), sb);
        appendTd(formality.getParticularRegister(), sb);
        appendTd(formality.getGeneralRegister(), sb);
        appendTd(formality.getSpeciesStr(), sb);
        appendTd(formality.getForAgainst(), sb);
        appendTd(formality.getDocumentTitle(), sb);
        appendTd(formality.getDocumentPath(), sb);

        sb.append("</tr>");

        return sb.toString();
    }

    private static String createSubjectCadastralTableHead() {
        StringBuilder sb = new StringBuilder();

        sb.append("<table width=\"100%\" align=\"center\" style=\"border-collapse: collapse;\">");
        sb.append("<tr>");
        appendTd(ResourcesHelper.getEnum("documentGenerationTagsSUBJECT_S_CADASTRAL1"), sb);
        appendTd(ResourcesHelper.getEnum("documentGenerationTagsSUBJECT_S_CADASTRAL2"), sb);
        appendTd(ResourcesHelper.getEnum("documentGenerationTagsSUBJECT_S_CADASTRAL3"), sb);

        sb.append("</tr>");

        return sb.toString();
    }

    private static String createSubjectFormalityTableHead() {
        StringBuilder sb = new StringBuilder();

        sb.append("<table width=\"100%\" align=\"center\" style=\"border-collapse: collapse;\">");
        sb.append("<tr>");
        appendTd(ResourcesHelper.getEnum("documentGenerationTagsSUBJECT_S_FORMALITIES1"), sb);
        appendTd(ResourcesHelper.getEnum("documentGenerationTagsSUBJECT_S_FORMALITIES2"), sb);
        appendTd(ResourcesHelper.getEnum("documentGenerationTagsSUBJECT_S_FORMALITIES3"), sb);
        appendTd(ResourcesHelper.getEnum("documentGenerationTagsSUBJECT_S_FORMALITIES4"), sb);
        appendTd(ResourcesHelper.getEnum("documentGenerationTagsSUBJECT_S_FORMALITIES5"), sb);
        appendTd(ResourcesHelper.getEnum("documentGenerationTagsSUBJECT_S_FORMALITIES6"), sb);
        appendTd(ResourcesHelper.getEnum("documentGenerationTagsSUBJECT_S_FORMALITIES7"), sb);
        appendTd(ResourcesHelper.getEnum("documentGenerationTagsSUBJECT_S_FORMALITIES8"), sb);
        appendTd(ResourcesHelper.getEnum("documentGenerationTagsSUBJECT_S_FORMALITIES9"), sb);
        sb.append("</tr>");

        return sb.toString();
    }

    private static String makePropertyBlock(Set<RequestPrintProperty> requestPrintProperties) {
        StringBuffer sb = new StringBuffer();

        for (RequestPrintProperty rpp : requestPrintProperties) {
            String html = PrintPDFHelper.readWorkingListFile("selectedPropertyBlock", "Tag");

            for (PropertyBlockGenerationTags tag : PropertyBlockGenerationTags.values()) {
                String valueTag = "";
                try {
                    Method method = Property.class.getMethod(tag.getGetMethod());
                    valueTag = TemplateEntity.correctMethodInvoking(method, rpp.getProperty());
                } catch (Exception e) {
                }

                html = html.replaceAll(tag.getTag(), valueTag);
            }

            sb.append(html);
        }

        return sb.toString();
    }

    private static String makeFormalityBlock(Set<Formality> formalities) {
        StringBuffer sb = new StringBuffer();

        for (Formality formality : formalities) {
            String html = PrintPDFHelper.readWorkingListFile("selectedFormalityBlock", "Tag");

            for (FormalityBlockGenerationTags tag : FormalityBlockGenerationTags.values()) {
                String valueTag = "";
                try {
                    Method method = Formality.class.getMethod(tag.getGetMethod());
                    valueTag = TemplateEntity.correctMethodInvoking(method, formality);
                } catch (Exception e) {
                }

                html = html.replaceAll(tag.getTag(), valueTag);
            }

            sb.append(html);
        }

        return sb.toString();
    }

    private static Relationship loadRelationshipBySubAndProp(Long subjectId, Long propertyId) {
        try {
            List<Relationship> rs = DaoManager.load(Relationship.class, new Criterion[]{
                    Restrictions.eq("subject.id", subjectId),
                    Restrictions.eq("property.id", propertyId)
            });

            if (!ValidationHelper.isNullOrEmpty(rs)) {
                if (rs.size() > 1) {
                    LogHelper.log(log, "More then 1 Relationship with subject.id = "
                            + subjectId + " and property.id = "
                            + propertyId);
                }

                return rs.get(0);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return null;
    }

    private static void appendTd(String data, StringBuilder str) {
        str.append(
                "<td style=\"border: 1px solid black;\">");
        str.append(data == null ? "" : data);
        str.append("</td>");
    }

    public static Map<List<RelationshipGroupingWrapper>, List<Property>> sortByValue(Map<List<RelationshipGroupingWrapper>, List<Property>> map) {
        List<Map.Entry<List<RelationshipGroupingWrapper>, List<Property>>> list = new ArrayList<>(map.entrySet());
        list.sort(new SortMapByCity());

        Map<List<RelationshipGroupingWrapper>, List<Property>> result = new LinkedHashMap<>();
        for (Map.Entry<List<RelationshipGroupingWrapper>, List<Property>> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static class SortMapByCity implements Comparator<Map.Entry<List<RelationshipGroupingWrapper>, List<Property>>> {
        @Override
        public int compare(Map.Entry<List<RelationshipGroupingWrapper>, List<Property>> o1, Map.Entry<List<RelationshipGroupingWrapper>, List<Property>> o2) {
            return o1.getValue().get(0).getCity().getDescription().compareTo(o2.getValue().get(0).getCity().getDescription());
        }
    }

    public static String getInitTextRegistryOrTable(Request request) {
        String defaultText = ResourcesHelper.getString("init_text_registry_or_table_default");
        AggregationLandChargesRegistry alcr = request.getAggregationLandChargesRegistry();
        boolean bReplaceWithConservatory = alcr.getLandChargesRegistries()
        		.stream()
        		.filter(lcr -> !ValidationHelper.isNullOrEmpty(lcr.getType()))
        		.allMatch(x->x.getType().equals(LandChargesRegistryType.CONSERVATORY));
        boolean bReplaceWithTavolare = alcr.getLandChargesRegistries()
        		.stream()
        		.filter(lcr -> !ValidationHelper.isNullOrEmpty(lcr.getType()))
        		.anyMatch(lcr->lcr.getType().equals(LandChargesRegistryType.TAVOLARE));
        if(bReplaceWithTavolare) {
            defaultText = ResourcesHelper.getString("init_text_registry_or_table_tavolare");
        }
        if(bReplaceWithConservatory) {
            defaultText = ResourcesHelper.getString("init_text_registry_or_table_conservatory");
        }

        return defaultText;


    }
}