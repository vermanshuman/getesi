package it.nexera.ris.common.helpers.tableGenerator;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.persistence.beans.entities.domain.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;

public class CertificazioneTableGenerator extends InterlayerTableGenerator {

    private static final String CADASTRAL_CATEGORY_CODE = "T";
    private static final String MAGIC_SPACE_FIX = "<strong>&nbsp;</strong>";

    private String bodyText;
    private String bodyContent;
    private boolean isSingularProperty;
    @Getter
    @Setter
    private static Client requestClient;

    public CertificazioneTableGenerator(Request request) {
        super(request);
    }

    public CertificazioneTableGenerator(Request request, boolean fullSize, String text) {
        super(request, fullSize);
        setBodyText(text);
    }

    @Override
    void addBeginning() {

    }

    @Override
    void fillTagTableList() throws PersistenceBeanException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        if(!ValidationHelper.isNullOrEmpty(getRequest()) && !ValidationHelper.isNullOrEmpty(getRequest().getClient())){
            setRequestClient(getRequest().getClient());
        }
        Formality formality = getRequest().getDistraintFormality();

        if (!ValidationHelper.isNullOrEmpty(formality)) {

            sb.append(String.format("Procedura esecutiva immobiliare promossa da %s, " +
                            "<br/> Contro (Parte debitrice): <br/><b> %s </b> . " +
                            "<br/> In virtù di pignoramento immobiliare trascritto presso l'Agenzia delle Entrate " +
                            "- Ufficio Provinciale di %s  Territorio e Servizio di Pubblicità Immobiliare di <b> %s </b>" +
                            " - in data %s al n. %s Registro Generale e n. %s Registro Particolare.",
                    manageSubjectsPart(getRequest().getDistraintFormality(), SectionCType.A_FAVORE, false,
                            false, false, true, false),
                    manageSubjectsPart(getRequest().getDistraintFormality(), SectionCType.CONTRO, false,
                            true, false, true, false),
                    formality.getProvincialOfficeName(), formality.getReclamePropertyService(),
                    DateTimeHelper.toFormatedString(formality.getPresentationDate(), DateTimeHelper.getMonthWordDatePattert(), Locale.ITALY),
                    formality.getGeneralRegister(), formality.getParticularRegister()));

            if (!ValidationHelper.isNullOrEmpty(getRequest().getNotary())) {
                sb.append(String.format(
                        "<br/> Il sottoscritto, <b> Avv. %s</b>, Notaio in %s con studio alla %s, iscritto presso il %s, ",
                        getRequest().getNotary().getName(), getRequest().getNotary().getCity(),
                        getRequest().getNotary().getOfficeAddress(), getRequest().getNotary().getInscriptions()));
            }
            List<Property> properties = formality.getSectionB().stream().map(SectionB::getProperties)
                    .flatMap(List::stream).collect(Collectors.toList());
            this.isSingularProperty = !ValidationHelper.isNullOrEmpty(properties) && properties.size() == 1;
            sb.append(String.format("<br/> <br/> <center> <b> DICHIARA </b> </center> " +
                            " di aver effettuato presso l'Agenzia delle Entrate Uffici Provinciali di <b>%s</b> " +
                            " - Territorio (Servizi Catastali e di Pubblicità Immobiliare%s)" +
                            //" &nbsp;l'esame ultraventennale a tutto il %s della proprietà e disponibilità dei seguenti immobili: ",
                            " &nbsp;l'esame ultraventennale a tutto il %s della proprietà e disponibilità %s: ",
                            
                    formality.getProvincialOfficeName(), frmTxt(formality, " di <b>", "</b>",
                            x -> !ValidationHelper.isNullOrEmpty(x.getProvincialOffice())
                                    && !ValidationHelper.isNullOrEmpty(x.getReclamePropertyService())
                                    && x.getProvincialOffice().getId().equals(x.getReclamePropertyService().getId())
                                    || ValidationHelper.isNullOrEmpty(x.getReclamePropertyService()) ?
                                    "" : x.getReclamePropertyService().getName()),
                    DateTimeHelper.toFormatedString(formality.getPresentationDate(),
                            DateTimeHelper.getMonthWordDatePattert(), Locale.ITALY),this.isSingularProperty ? "del seguente immobile" : "dei seguenti immobili"));

            
            sb.append(String.format("<br/> <br/> <center> <b> DESCRIZIONE %s OGGETTO DI PIGNORAMENTO: </b> </center>" + "%s",
                    this.isSingularProperty ? "DELL'IMMOBILE" : "DEGLI IMMOBILI",
                    !ValidationHelper.isNullOrEmpty(properties) ? createNumberedPropertyList(properties, formality) : "<br/>"));
        } else {
            sb.append("<br/>");
        }
        sb.append(createCertificate());

        setBodyContent(sb.toString());
    }

    private String createNumberedPropertyList(List<Property> propertyList, Formality formality) {
        if (ValidationHelper.isNullOrEmpty(propertyList)) {
            return "";
        }
        int serialNumber = 1;
        String styledSpanToNowrapSpaces = "<span style='white-space: nowrap; display: inline-block'>";
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='margin-left: 40px'>") ;

        //To create this numbered list we cannot use the <ol> tag, because of the template grid(the text are misaligned with this tag)
        //So we create list using divs and these styles
        //class "col1" is responsible for the numbers in our list
        //class "col2" is responsible for property text in our list
        sb.append("<style>" +
                "div.grid div  { float: left; }\n" +
                "div.col1  { width: 5%; }\n" +
                "div.col2  { width: 95%; }\n" +
                "</style>");

        for (Property property : propertyList) {
            
            List<Relationship> relationships = property.getRelationships().stream()
                    .filter(x -> x.getSectionCType().equalsIgnoreCase(SectionCType.A_FAVORE.getName())
                            && x.getFormality().getId().equals(formality.getId()))
                    .filter(distinctByKey(Relationship::getQuote)).collect(Collectors.toList());
            
            for (Relationship relationship : relationships) {
                sb.append("<div class=\"grid\">");
                sb.append("<div class=\"col1\">");
                sb.append("<b>").append(serialNumber++).append(")").append("</b> ");
                sb.append("</div>");
                sb.append("<div class=\"col2\">");
                sb.append("<i>");
                sb.append(relationship.getQuote().toLowerCase()).append(" ");
                sb.append(relationship.getPropertyType().toLowerCase()).append(" ");
                sb.append("</i> ");
                if (RealEstateType.BUILDING.getId().equals(property.getType())) {
                    sb.append("FABBRICATO");
                } else {
                    sb.append("TERRENO");
                }
                sb.append(", in ").append(property.getCityDescription());
                sb.append(" ( ").append(property.getProvince().getCode()).append(" ) ");
                sb.append(frmTxt(property.getAddress(), WordUtils::capitalizeFully));
                sb.append(frmTxt(property.getFloor(), ", piano "));
                sb.append(frmTxt(property.getInterno(), ", interno "));

                sb.append("<br/>");
                sb.append(addCadastralDataToReport(property.getCadastralData()));
                if (property.getCategoryCode().equals(CADASTRAL_CATEGORY_CODE)) {
                    sb.append(getLandData(property));
                }
                if (!ValidationHelper.isNullOrEmpty(property.getCategory())
                        && !ValidationHelper.isNullOrEmpty(property.getConsistency())) {
                    sb.append(getVaniOrMqConsistency(property));
                }
                if(ValidationHelper.isNullOrEmpty(property.getType()) || 
                        RealEstateType.BUILDING.getId().equals(property.getType())){
                    sb.append(frmTxt(property.getCategory(), ", " + styledSpanToNowrapSpaces + "Cat. ", "</span>",
                            CadastralCategory::getCodeInVisura));    
                } else  if(RealEstateType.LAND.getId().equals(property.getType()) && 
                        !ValidationHelper.isNullOrEmpty(property.getQuality())){
                    sb.append(frmTxt(property.getQuality().toLowerCase(),  ", " + styledSpanToNowrapSpaces,"</span>"));
                }
                
                sb.append("</div>");
                sb.append("</div>");
            }
        }
        sb.append("&nbsp</div>");
        return sb.toString();
    }

    private String getVaniOrMqConsistency(Property property) {
        StringBuilder sb = new StringBuilder();
        if (property.getCategory().getCodeInVisura().startsWith("A")) {
            sb.append(", vani ");
            sb.append(property.getConsistency().replaceAll("vani|VANI", ""));
        } else {
            if(ValidationHelper.isNullOrEmpty(property.getType()) 
                    || !RealEstateType.LAND.getId().equals(property.getType())) {
                sb.append(", mq ");
                sb.append(property.getConsistency().replaceAll("mq|MQ", ""));
            }
        }
        return sb.toString();
    }

    private String getLandData(Property property) {
        StringBuilder sb = new StringBuilder();
        sb.append(", ");
        sb.append(frmTxt(property.getHectares(), "ettari ", " ",
                x -> !x.equals(0d) ? String.valueOf(x.intValue()) : ""));
        sb.append(frmTxt(property.getAres(), "are ", " ",
                x -> !x.equals(0d) ? String.valueOf(x.intValue()) : ""));
        sb.append(frmTxt(property.getCentiares(), "centiare ", " ",
                x -> !x.equals(0d) ? String.valueOf(x.intValue()) : ""));
        return sb.toString();
    }

    private String addCadastralDataToReport(List<CadastralData> cadastralDataList) {
        StringBuilder sb = new StringBuilder();
        if (!ValidationHelper.isNullOrEmpty(cadastralDataList)) {
            List<CadastralData> distinctCadastralData = 
                    cadastralDataList.stream().distinct()
                    .collect(Collectors.toList());
            
            for(int c=0; c < distinctCadastralData.size();c++) {
                CadastralData cadastralData = cadastralDataList.get(c);
                if(c > 0)
                    sb.append(" e ");
                else {
                    sb.append("Distinto in catasto");
                    if(!ValidationHelper.isNullOrEmpty(cadastralData.getSection())) {
                        sb.append(" alla sezione " + cadastralData.getSection());
                    }else
                        sb.append(" al");
                }
                    // al");
                String  data = getCadastralSheetParticleSub(cadastralData);
                sb.append(data);
                if(c > 0 && (c == cadastralDataList.size()-1)) {
                    sb.append(" (graffate) ");
                }
            }
        }
        return sb.toString();
    }

    private static String manageSubjectsPart(Formality formality, SectionCType cType, boolean addSituationData,
                                      boolean reportDateMonthWordFormat, boolean addRelationshipData,
                                      boolean useNewLineSeparator, boolean subjectCityCamelCase)
            throws PersistenceBeanException, IllegalAccessException {
        List<Subject> subjects = formality.getSectionC().stream()
                .filter(x -> cType.getName().equals(x.getSectionCType()))
                .sorted(Comparator.comparingInt(o -> -o.getSubject().size()))
                .map(SectionC::getSubject).flatMap(List::stream).distinct().collect(Collectors.toList());
        
        return getSubjectData(subjects, formality, addSituationData, cType, reportDateMonthWordFormat,
                addRelationshipData, useNewLineSeparator, subjectCityCamelCase);
    }

    private static String getSubjectData(List<Subject> subjects, Formality formality, boolean addSituationData,
                                  SectionCType cType, boolean reportDateMonthWordFormat, boolean addRelationshipData,
                                  boolean useNewLineSeparator, boolean subjectCityCamelCase)
            throws PersistenceBeanException, IllegalAccessException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < subjects.size(); i++) {
            Subject subject = subjects.get(i);

            if (i != 0 && useNewLineSeparator) {
                result.append("<br/>");
            }
            if (subject.getTypeIsPhysicalPerson()) {
                result.append(subject.getFullName().toUpperCase());
                result.append(SexTypes.MALE.getId().equals(subject.getSex()) ? " nato a" : " nata a");
                if (!ValidationHelper.isNullOrEmpty(subject.getBirthCity())) {
                    result.append(MAGIC_SPACE_FIX);
                    if (subjectCityCamelCase) {
                        result.append(WordUtils.capitalizeFully(subject.getBirthCityDescription()));
                    } else {
                        result.append(subject.getBirthCityDescription());
                    }
                    if (!ValidationHelper.isNullOrEmpty(subject.getBirthCity().getProvince())) {
                        result.append(" (").append(subject.getBirthCity().getProvince().getCode()).append(")");
                    }
                } else if (!ValidationHelper.isNullOrEmpty(subject.getCountry())) {
                    result.append(" ").append(subject.getCountry().getDescription());
                }
                result.append(" il ");
                result.append(reportDateMonthWordFormat ?
                        DateTimeHelper.toFormatedString(subject.getBirthDate(),
                                DateTimeHelper.getMonthWordDatePattert(), Locale.ITALY)
                        : DateTimeHelper.toString(subject.getBirthDate()));
                result.append(", codice fiscale ").append(subject.getFiscalCode());
                if((i == subjects.size() - 1)
                        && !ValidationHelper.isNullOrEmpty(cType)
                        && cType.equals(SectionCType.DEBITORI_NON_DATORI_DI_IPOTECA))
                    result.append(";");
            } else {
                result.append(subject.getBusinessName());
                result.append(" sede ");
                result.append(frmTxt(subject.getBirthCityDescription(), "", "",
                        frmTxt(subject.getCountry(), Dictionary::getDescription),
                        str -> subjectCityCamelCase ? WordUtils.capitalizeFully(str) : str));
                result.append(frmTxt(subject.getBirthProvince(), " (", ")", Dictionary::getCode));
                result.append(", codice fiscale ").append(subject.getNumberVAT());
                if((i == subjects.size() - 1)
                        && !ValidationHelper.isNullOrEmpty(cType) && cType.equals(SectionCType.DEBITORI_NON_DATORI_DI_IPOTECA))
                    result.append(";");
            }

            if (addRelationshipData) {
                result.append(getRelationshipData(formality, subject, cType));
            } else {
                if (addSituationData) {
                    result.append(getSituationData(formality, cType, subject));
                }
                if (i != subjects.size() - 1 && !useNewLineSeparator) {
                    result.append(", ");
                }
            }

        }
        return result.toString();
    }

    private static String getRelationshipData(Formality formality, Subject subject, SectionCType cType) {
        StringBuilder sb = new StringBuilder();

        if (!ValidationHelper.isNullOrEmpty(subject.getRelationshipList())) {
            Relationship relationship = subject.getRelationshipList().stream().filter(r ->
                    !ValidationHelper.isNullOrEmpty(r.getFormality()) && formality.getId()
                            .equals(r.getFormality().getId()) && cType.getName().equals(r.getSectionCType()))
                    .findFirst().orElse(null);

            sb.append(", ");
            sb.append(frmTxt(relationship, x -> StringUtils.lowerCase(x.getPropertyType())));
            sb.append(" per ");
            sb.append(frmTxt(relationship, Relationship::getQuote));
        }

        sb.append("; ");

        return sb.toString();
    }

    private static String getSituationData(Formality formality, SectionCType cType, Subject subject) throws PersistenceBeanException,
            IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        List<Long> propertyIds = formality.getEstateSituationFormalityProperties().stream()
                .map(EstateSituationFormalityProperty::getProperty).map(IndexedEntity::getId).collect(Collectors.toList());

        List<Relationship> relationships = DaoManager.load(Relationship.class, new Criterion[]{
                Restrictions.eq("formality.id", formality.getId()),
                Restrictions.eq("subject.id", subject.getId()),
                Restrictions.eq("sectionCType", cType.getName()),
                propertyIds.isEmpty() ? Restrictions.isNotNull("id") :
                        Restrictions.in("property.id", propertyIds)
        });

        for (Relationship relationship : getDistinctRelationships(relationships)) {
            sb.append(", <i>per la quota di ");
            sb.append(relationship.getQuote().toLowerCase());
            sb.append(" del diritto di ");
            sb.append(relationship.getPropertyType().toLowerCase());

            if(!ValidationHelper.isNullOrEmpty(relationship.getRegime())){

                boolean showRegime = false;
                if(!ValidationHelper.isNullOrEmpty(relationship.getRelationshipTypeId()) &&
                        !ValidationHelper.isNullOrEmpty(getRequestClient().getRegime()) &&
                        getRequestClient().getRegime()){
                    showRegime = true;
                }else if(ValidationHelper.isNullOrEmpty(relationship.getRelationshipTypeId())){
                    showRegime = true;
                }
                if(showRegime){
                    sb.append(" in regime di ");
                    sb.append(relationship.getRegime().toLowerCase());
                }
            }
            sb.append("</i>");
        }

        return sb.toString();
    }

    private static List<Relationship> getDistinctRelationships(List<Relationship> relationships) {
        List<Relationship> relationshipsDistinctByRegime = relationships.stream()
                .filter(distinctByKey(relationship -> relationship.getRegime() != null ? relationship.getRegime() : ""))
                .collect(Collectors.toList());
        List<Relationship> relationshipsDistinctByQuota = relationships.stream()
                .filter(distinctByKey(relationship -> relationship.getQuote() != null ? relationship.getQuote() : ""))
                .collect(Collectors.toList());
        List<Relationship> relationshipsDistinctByPropertyType = relationships.stream()
                .filter(distinctByKey(relationship -> relationship.getPropertyType() != null ? relationship.getPropertyType() : ""))
                .collect(Collectors.toList());
        List<Relationship> distinctRelationships = new ArrayList<>(relationshipsDistinctByPropertyType);
        distinctRelationships.addAll(relationshipsDistinctByQuota);
        distinctRelationships.addAll(relationshipsDistinctByRegime);
        return distinctRelationships.stream().distinct().collect(Collectors.toList());
    }

    private String getSubjectsListByDistraintFormalityAndSectionC(Request request)
            throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(request.getDistraintFormality()) &&
                !ValidationHelper.isNullOrEmpty(request.getDistraintFormality().getSectionC())) {
            return manageSubjectsPart(request.getDistraintFormality(), SectionCType.CONTRO, false,
                    true, true, false, false);
        }
        return "";
    }

    private String generateRequestCertificationComment()
            throws PersistenceBeanException, IllegalAccessException {
        return String.format("Alla data del %s " +
                        "gli immobili in oggetto risultano di proprietà di %s",
                DateTimeHelper.toFormatedString(getRequest().getDistraintFormality().getPresentationDate(),
                        DateTimeHelper.getMonthWordDatePattert(), Locale.ITALY),
                getSubjectsListByDistraintFormalityAndSectionC(getRequest()));
    }

    public static String getRequestCertificateComment(Request request)
            throws PersistenceBeanException, IllegalAccessException {
        return new CertificazioneTableGenerator(request).generateRequestCertificationComment();
    }

    private String createCertificate() throws PersistenceBeanException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();

        Date presentationDate = null;
        if (!ValidationHelper.isNullOrEmpty(getRequest())
                && !ValidationHelper.isNullOrEmpty(getRequest().getDistraintFormality())) {
            presentationDate = getRequest().getDistraintFormality().getPresentationDate();
        }

        sb.append(String.format(" <center> <b> CERTIFICA </b> </center> " +
                        //" Alla data del %s  gli immobili oggetto di relazione sono censiti presso" +
                        " Alla data del %s %s presso" +
                        " l'Agenzia delle Entrate - Ufficio Provinciale di %s" +
                        "  - Territorio Servizi Catastali nel modo che segue:%s",
                !ValidationHelper.isNullOrEmpty(presentationDate) ? getFormattedCertificateDate(presentationDate)
                        : getCadastralDateDocumentSubject(), this.isSingularProperty ? "l'immobile oggetto della relazione è censito" : "gli immobili oggetto di relazione sono censiti",getCadastralProvinceDocumentSubject(),
                getCadastralPropertyListDocumentSubject()));

        sb.append(frmTxt(getRequest().getCommentCertification(), "</br>"));

        sb.append(String.format("<br/> <br/> <center> <b> STORIA IPOTECARIA ULTRA-VENTENNALE </b> </center> " +
                " <center> <u> Da indagini ipotecarie risultano le seguenti formalità: </u> </center>" +
                "%s", getGroupedFormalitiesList(getGroupedBySituationPropertyNotPrejudicialFormalitiesMap())));

        List<Formality> formalityList = getRequest().getSituationEstateLocations().stream()
                .filter(es -> ValidationHelper.isNullOrEmpty(es.getOtherType()) || !es.getOtherType())
                .filter(es -> (ValidationHelper.isNullOrEmpty(es.getSalesDevelopment()) ||
                        !es.getSalesDevelopment()))
                .peek(es -> es.getFormalityList()
                        .forEach(f -> f.setShouldReportRelationships(
                                es.getReportRelationship() == null ? false : es.getReportRelationship())))
                .map(EstateSituation::getFormalityList).flatMap(List::stream)
              .filter(x -> x.isExistsByPrejudicialAndСode(true))
                //.sorted(getCetificaFormalityComparator())
                .distinct().collect(Collectors.toList());

        formalityList.sort(Comparator.comparing(Formality::getComparedDate)
                .thenComparing(Formality::getGeneralRegister)
                .thenComparing(Formality::getParticularRegister)
                .thenComparing(Formality::getComparedDeathDate));

        Collections.reverse(formalityList);
        sb.append(String.format("<br/> <br/> <center> <b> Il sottoscritto dott. %s </b> </center>" +
                        "<center> <b> CERTIFICA </b> </center>" +
                        "<b> CHE %s OGGETTO DELLA PRESENTE RELAZIONE %s DALLE SEGUENTI FORMALITA': </b>" +
                        " %s ", getRequest().getNotaryNameIfExists().toUpperCase(),
                this.isSingularProperty ? "L'IMMOBILE" : "GLI IMMOBILI",
                this.isSingularProperty ? "RISULTA GRAVATO" : "RISULTANO GRAVATI",
                getFormalityList(formalityList, true, false)));

       
        formalityList = getRequest().getSituationEstateLocations().stream()
                .filter(es -> !ValidationHelper.isNullOrEmpty(es.getOtherType()) && es.getOtherType())
                .peek(es -> es.getFormalityList()
                        .forEach(f -> f.setShouldReportRelationships(
                                ValidationHelper.isNullOrEmpty(es.getReportRelationship()) || es.getReportRelationship())))
                .map(EstateSituation::getFormalityList).flatMap(List::stream)
                .sorted(getFormalityComparator()).collect(Collectors.toList());

        if (!ValidationHelper.isNullOrEmpty(formalityList)) {
            sb.append(String.format("<br/><br/><center><b><i>Ulteriori formalità rilevate</i></b></center>" +
                    "%s", getFormalityList(formalityList, false, false)));
        }

        sb.append(String.format("<br/><b> %s, %s </b>" +
                        "<br/> <div align=\"right\">Notaio %s </div>", getRequest().getNotaryCityIfExists(),
                ValidationHelper.isNullOrEmpty(getRequest().getEndDate()) ?
                        DateTimeHelper.toFormatedString(new Date(), DateTimeHelper.getMonthWordDatePattert(), Locale.ITALY)
                        : DateTimeHelper.toFormatedString(getRequest().getEndDate(), DateTimeHelper.getMonthWordDatePattert(), Locale.ITALY),
                getRequest().getNotaryNameIfExists()));

        return sb.toString();
    }

    private String getGroupedFormalitiesList(Map<String, List<Formality>> groupedFormalities)
            throws PersistenceBeanException, IllegalAccessException {
        StringBuilder stringBuilder = new StringBuilder();
        long group = 0;
        for (Map.Entry<String, List<Formality>> entry : groupedFormalities.entrySet()) {
            if (group != 0) {
                stringBuilder.append("<br/>");
            }
            
            String key = entry.getKey();
            if(!ValidationHelper.isNullOrEmpty(key) && key.startsWith("order_")) {
                key = "";
            }
            stringBuilder.append("<b>Provenienza debitore immobile ").append(key.replaceAll("\\,", "\\) ")).append(")").append(": </b>");
            stringBuilder.append(getFormalityList(entry.getValue(), true, true));
            ++group;
        }
        return stringBuilder.toString();
    }

    private Map<String, List<Formality>> getGroupedBySituationPropertyNotPrejudicialFormalitiesMap() {
        Map<String, List<Formality>> groupedFormalities = new LinkedHashMap<>();
        long order = 0;
        for (SituationProperty situationProperty : getRequest().getSituationEstateLocations().stream()
                .map(EstateSituation::getSituationProperties).flatMap(List::stream)
                .sorted(Comparator.comparing(IndexedEntity::getId)).collect(Collectors.toList())) {
            situationProperty.setOrderNumber(++order);
        }
        List<EstateSituation> sortedSituations = getRequest().getSituationEstateLocations().stream()
                .filter(es -> ValidationHelper.isNullOrEmpty(es.getOtherType()) || !es.getOtherType())
                .filter(es -> (ValidationHelper.isNullOrEmpty(es.getSalesDevelopment()) ||
                        !es.getSalesDevelopment()))
                .sorted((o1, o2) -> ObjectUtils.compare(
                        o1.getSituationProperties().stream().map(SituationProperty::getOrderNumber)
                                .min(ObjectUtils::compare).orElse(null),
                        o2.getSituationProperties().stream().map(SituationProperty::getOrderNumber)
                                .min(ObjectUtils::compare).orElse(null)))
                .collect(Collectors.toList());

        List<Long> formalitiesFromOtherEstateSituations = new LinkedList<>();
        for (EstateSituation situationEstateLocation : sortedSituations) {
            String orders = situationEstateLocation.getSituationProperties().stream()
                    .sorted(Comparator.comparing(IndexedEntity::getId)).map(x -> x.getOrderNumber().toString())
                    .collect(Collectors.joining(","));
            
            if(ValidationHelper.isNullOrEmpty(orders)) {
                orders = "order_" + UUID.randomUUID();
            }
            
            List<Formality> formalities = situationEstateLocation.getFormalityList().stream()
                    .filter(x -> Objects.nonNull(x))
                    .filter(x -> x.isExistsByPrejudicialAndСode(false))
            //        .sorted(getCetificaFormalityComparator())
                    .peek(f -> f.setShouldReportRelationships(
                            ValidationHelper.isNullOrEmpty(situationEstateLocation.getReportRelationship())
                                    || situationEstateLocation.getReportRelationship()))
                    .collect(Collectors.toList());

            formalities.sort(Comparator.comparing(Formality::getComparedDate)
                    .thenComparing(Formality::getGeneralRegister)
                    .thenComparing(Formality::getParticularRegister)
            .thenComparing(Formality::getComparedDeathDate));

            Collections.reverse(formalities);
            if (!ValidationHelper.isNullOrEmpty(formalities)) {
                formalitiesFromOtherEstateSituations.addAll(formalities.stream().map(IndexedEntity::getId)
                        .collect(Collectors.toList()));
                groupedFormalities.put(orders, formalities);
            }
        }
        return groupedFormalities;
    }

    private Comparator<Formality> getFormalityComparator() {
        Comparator<Formality> registersComparator = (o1, o2) -> {
            int compare = 0;
            if (!ValidationHelper.isNullOrEmpty(o1.getGeneralRegister())
                    && !ValidationHelper.isNullOrEmpty(o2.getGeneralRegister())) {
                compare = Long.valueOf(o1.getGeneralRegister().replaceAll("[^0-9]",""))
                        .compareTo(Long.parseLong(o2.getGeneralRegister().replaceAll("[^0-9]","")));
            }
            if (compare == 0) {
                if (!ValidationHelper.isNullOrEmpty(o1.getParticularRegister())
                        && !ValidationHelper.isNullOrEmpty(o2.getParticularRegister())) {
                    compare = Long.valueOf(o1.getParticularRegister().replaceAll("[^0-9]",""))
                            .compareTo(Long.parseLong(o2.getParticularRegister().replaceAll("[^0-9]","")));
                }
            }
            return compare;
        };

        return (o1, o2) -> {
            int compare;
            if (!ValidationHelper.isNullOrEmpty(o2.getSectionA()) && !ValidationHelper.isNullOrEmpty(o2.getSectionA().getDeathDate())) {
                if (!ValidationHelper.isNullOrEmpty(o1.getSectionA().getDeathDate())) {
                    compare = ObjectUtils.compare(o2.getSectionA().getDeathDate(), o1.getSectionA().getDeathDate());
                } else {
                    compare = ObjectUtils.compare(o2.getSectionA().getDeathDate(), o1.getPresentationDate());
                }
            } else if (!ValidationHelper.isNullOrEmpty(o1.getSectionA()) && !ValidationHelper.isNullOrEmpty(o1.getSectionA().getDeathDate())) {
                compare = ObjectUtils.compare(o2.getPresentationDate(), o1.getSectionA().getDeathDate());
            } else {
                compare = ObjectUtils.compare(o2.getPresentationDate(), o1.getPresentationDate());
            }
            return compare == 0 ? registersComparator.compare(o2, o1) : compare;
        };
    }

    private String getFormalityList(List<Formality> formalityList, boolean shouldBeFirstFormalityFromNewLine,
                                    boolean firstRegisterIsGeneral)
            throws PersistenceBeanException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < formalityList.size(); i++) {
            
            Formality formality = formalityList.get(i);
            
            if (i != 0 || shouldBeFirstFormalityFromNewLine) {
                sb.append("<br/>");
            }
            sb.append(getTextTypeAndCertificationTextFormality(formality));
            
            sb.append(formality.getGeneralRegister());
            sb.append("/");
            sb.append(formality.getParticularRegister());
            
            sb.append(" in data ").append(DateTimeHelper.toFormatedString(formality.getPresentationDate(),
                    DateTimeHelper.getMonthWordDatePattert(), Locale.ITALY));
            sb.append(", a seguito di ");
            if (!ValidationHelper.isNullOrEmpty(formality.getSectionA()) && !ValidationHelper.isNullOrEmpty(formality.getSectionA().getPublicOfficialNotary())) {
                if(!ValidationHelper.isNullOrEmpty(formality.getSectionA().getTitleDescription())) {
                    sb.append(formality.getSectionA().getTitleDescription().toLowerCase());
                    sb.append(" ");
                }
                sb.append("per Notaio ");
                sb.append(WordUtils.capitalizeFully(formality.getSectionA().getPublicOfficialNotary()));
                sb.append(frmTxt(formality.getSectionA().getSeat(), " di ",
                        x -> x.indexOf('(') != -1 ? WordUtils.capitalizeFully(x.substring(0, x.indexOf('(')))
                                + x.substring(x.indexOf('(')) : WordUtils.capitalizeFully(x)));
            } else {
                if(!ValidationHelper.isNullOrEmpty(formality.getSectionA()) && 
                        !ValidationHelper.isNullOrEmpty(formality.getSectionA().getTitleDescription())) {
                    sb.append(formality.getSectionA().getTitleDescription().toLowerCase());
                    sb.append(" ");
                }
                sb.append("per ");
                if(!ValidationHelper.isNullOrEmpty(formality.getSectionA()) && 
                        !ValidationHelper.isNullOrEmpty(formality.getSectionA().getPublicOfficial())) {
                    sb.append(WordUtils.capitalizeFully(formality.getSectionA().getPublicOfficial()));    
                }
                if(!ValidationHelper.isNullOrEmpty(formality.getSectionA()) &&  
                        !ValidationHelper.isNullOrEmpty(formality.getSectionA().getSeat()) &&
                        !ValidationHelper.isNullOrEmpty(formality.getSectionA().getPublicOfficial())) {
                    String seat = formality.getSectionA().getSeat().split("\\(")[0].trim();
                    if(!formality.getSectionA().getPublicOfficial().toUpperCase().contains(seat.toUpperCase())) {
                        sb.append(" di "+formality.getSectionA().getSeat());
                    }
                }
            }
            
            if(!ValidationHelper.isNullOrEmpty(formality.getSectionA()) && 
                    !ValidationHelper.isNullOrEmpty(formality.getSectionA().getTitleDate())) {
                sb.append(" del ").append(DateTimeHelper.toFormatedString(formality.getSectionA().getTitleDate(),
                        DateTimeHelper.getMonthWordDatePattert(), Locale.ITALY));    
            }
            
            if(!ValidationHelper.isNullOrEmpty(formality.getSectionA()) && 
                    !ValidationHelper.isNullOrEmpty(formality.getSectionA().getNumberDirectory())) {
                sb.append(", numero di repertorio ").append(formality.getSectionA().getNumberDirectory());    
            }
           
            if(!ValidationHelper.isNullOrEmpty(formality.getSectionA()) &&  
                    !ValidationHelper.isNullOrEmpty(formality.getSectionA().getOtherParticularRegister())) {
                sb.append(", ");
                sb.append(ResourcesHelper.getString("formalityReferenceFormalities").toLowerCase());
                sb.append(" n. ");
                sb.append(formality.getSectionA().getOtherParticularRegister());
                sb.append(" del");
                if(!ValidationHelper.isNullOrEmpty(formality.getSectionA()) &&  
                        !ValidationHelper.isNullOrEmpty(formality.getSectionA().getOtherData())) {
                    sb.append(" ");
                    sb.append(DateTimeHelper.toFormatedString(
                            formality.getSectionA().getOtherData(), 
                            DateTimeHelper.getMonthWordDatePattert(), null));
                }
            }
            if(!ValidationHelper.isNullOrEmpty(formality.getSectionA()) &&  
                    !ValidationHelper.isNullOrEmpty(formality.getSectionA().getTotal())){
                sb.append(" - Importo totale ").append(formality.getSectionA().getTotal()).append(" - ");
            }
            if(!ValidationHelper.isNullOrEmpty(formality.getSectionA()) &&  
                    !ValidationHelper.isNullOrEmpty(formality.getSectionA().getCapital())){
                if(ValidationHelper.isNullOrEmpty(formality.getSectionA()) ||
                        ValidationHelper.isNullOrEmpty(formality.getSectionA().getTotal())){
                    sb.append(" - ");
                }
                sb.append("Importo Capitale ").append(formality.getSectionA().getCapital()).append(" - ");
            }
            if(!ValidationHelper.isNullOrEmpty(formality.getSectionA()) && 
                    !ValidationHelper.isNullOrEmpty(formality.getSectionA().getDuration())){
                sb.append(" durata ").append(formality.getSectionA().getDuration());
            }
            if (!ValidationHelper.isNullOrEmpty(formality.getSectionA()) && 
                    !ValidationHelper.isNullOrEmpty(getRequest().getDistraintFormality())
                    && !ValidationHelper.isNullOrEmpty(formality.getTextCertification())) {
                sb.append(", ").append(formality.getTextCertification());
            } else {
                if(formality.getSectionA() != null && formality.getSectionA().getDeathDate() != null) {
                    sb.append(" contro l'eredità di ").append(manageSubjectsPart(formality, SectionCType.CONTRO,
                            formality.isShouldReportRelationships(), true,
                            false, false, true));
                    
                    sb.append(" e deceduto addì ").append("<b>").append(DateTimeHelper.getFromattedDate(formality.getSectionA().getDeathDate())).append("</b>");
                    StringBuilder result = new StringBuilder();
                    List<Subject> subjects = formality.getSectionC().stream()
                            .filter(x -> SectionCType.CONTRO.equals(x.getSectionCType()))
                            .sorted(Comparator.comparingInt(o -> -o.getSubject().size()))
                            .map(SectionC::getSubject).flatMap(List::stream).distinct().collect(Collectors.toList());
                    for (int s = 0; s < subjects.size(); s++) {
                        Subject subject = subjects.get(s);
                        result.append(getRelationshipData(formality, subject, SectionCType.CONTRO));
                    }
                    sb.append(" ").append(result.toString());
                    sb.append(" , devolutasi per legge in favore di ");
                    sb.append(manageSubjectsPart(formality, SectionCType.A_FAVORE,
                            formality.isShouldReportRelationships(), true,
                            false, false, true));
                }else {
                    sb.append(", a favore di ").append(manageSubjectsPart(formality, SectionCType.A_FAVORE,
                            formality.isShouldReportRelationships(), true,
                            false, false, true));
                    sb.append(" e contro ").append(manageSubjectsPart(formality, SectionCType.CONTRO,
                            formality.isShouldReportRelationships(), true,
                            false, false, true));
                }
            }
            
            List<Subject> subjects = formality.getSectionC().stream()
                    .filter(x -> SectionCType.DEBITORI_NON_DATORI_DI_IPOTECA.getName().equals(x.getSectionCType()))
                    .sorted(Comparator.comparingInt(o -> -o.getSubject().size()))
                    .map(SectionC::getSubject).flatMap(List::stream).distinct().collect(Collectors.toList());

            String result = manageSubjectsPart(formality, SectionCType.DEBITORI_NON_DATORI_DI_IPOTECA,
                    formality.isShouldReportRelationships(), true,
                    false, false, true);
            
            if(!ValidationHelper.isNullOrEmpty(result) && (!this.isSingularProperty || subjects != null && subjects.size() > 1))
                sb.append("; Debitori non datori di ipoteca ").append(result);
            else if(!ValidationHelper.isNullOrEmpty(result))
                sb.append("; Debitore non datore di ipoteca ").append(result);
            
            if (!ValidationHelper.isNullOrEmpty(getRequest().getDistraintFormality()) &&
                    !ValidationHelper.isNullOrEmpty(formality.getDistraintComment())) {
                String comment = formality.getDistraintComment().replaceAll("(style=\".*?\")", "");
                sb.append(comment);
            }
        }
        return sb.toString();
    }

    public static String getSubjectsPartForFormalityTextCertification(Formality formality)
            throws PersistenceBeanException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        String result = "";
        if(formality.getSectionA() != null && formality.getSectionA().getDeathDate() != null) {
            StringBuilder db = new StringBuilder();
            db.append(" contro l'eredità di ").append(manageSubjectsPart(formality, SectionCType.CONTRO,
                    formality.isShouldReportRelationships(), true,
                    false, false, true));
            db.append(" e deceduto addì ").append("<b>").append(DateTimeHelper.getFromattedDate(formality.getSectionA().getDeathDate())).append("</b>");
            StringBuilder sresult = new StringBuilder();
            List<Subject> subjects = formality.getSectionC().stream()
                    .filter(x -> SectionCType.CONTRO.equals(x.getSectionCType()))
                    .sorted(Comparator.comparingInt(o -> -o.getSubject().size()))
                    .map(SectionC::getSubject).flatMap(List::stream).distinct().collect(Collectors.toList());
            for (int s = 0; s < subjects.size(); s++) {
                Subject subject = subjects.get(s);
                sresult.append(getRelationshipData(formality, subject, SectionCType.CONTRO));
            }
            db.append(" ").append(sresult.toString());
            db.append(" , devolutasi per legge in favore di ");
            db.append(manageSubjectsPart(formality, SectionCType.A_FAVORE,
                    formality.isShouldReportRelationships(), true,
                    false, false, true));
            
            result = db.toString();
        }else {
            result = "a favore di " + manageSubjectsPart(formality, SectionCType.A_FAVORE,
                    formality.isShouldReportRelationships(), true,
                    true, false, true)
            + " e contro " + manageSubjectsPart(formality, SectionCType.CONTRO,
                    formality.isShouldReportRelationships(), true,
                    true, false, true);
        }
        sb.append(result);
        List<Subject> subjects = formality.getSectionC().stream()
                .filter(x -> SectionCType.DEBITORI_NON_DATORI_DI_IPOTECA.equals(x.getSectionCType()))
                .sorted(Comparator.comparingInt(o -> -o.getSubject().size()))
                .map(SectionC::getSubject).flatMap(List::stream).distinct().collect(Collectors.toList());

        return sb.toString();
    }

    private String getTextTypeAndCertificationTextFormality(Formality formality) throws PersistenceBeanException,
            IllegalAccessException {
        StringBuilder result = new StringBuilder();

        if (!ValidationHelper.isNullOrEmpty(formality.getSectionA())
                && !ValidationHelper.isNullOrEmpty(formality.getSectionA().getDerivedFromCode())) {

            List<TypeFormality> typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                    Restrictions.eq("type", formality.getTypeEnum()),
                    Restrictions.eq("code", formality.getSectionA().getCodeWithoutFirstZero())});
            if(formality.getSectionA().getDerivedFromCode().trim().endsWith("00")) {
                switch (formality.getTypeEnum()) {
            	case TYPE_I:
            	    String mortgageSpecies = formality.getSectionA().getMortgageSpecies();
                    if(StringUtils.isNotBlank(mortgageSpecies) && Character.isDigit(mortgageSpecies.charAt(0))) {
                        String startDigits = mortgageSpecies.split("\\s+")[0];
                        mortgageSpecies = mortgageSpecies.replaceFirst(startDigits, "").trim();
                    }
                    String derivedFrom =  formality.getSectionA().getDerivedFrom();
                    if(StringUtils.isNotBlank(derivedFrom) && Character.isDigit(derivedFrom.charAt(0))) {
                        String startDigits = derivedFrom.split("\\s+")[0];
                        derivedFrom = derivedFrom.replaceFirst(startDigits, "").trim();
                    }
                    result.append("<b>");
                    if(StringUtils.isNotBlank(mortgageSpecies)) {
                        result.append(mortgageSpecies);    
                    }
                    if(StringUtils.isNotBlank(mortgageSpecies) && StringUtils.isNotBlank(derivedFrom)) {
                        result.append("-");
                    }
                    if(StringUtils.isNotBlank(derivedFrom)) {
                        result.append(derivedFrom);    
                    }
                    result.append("</b>").append(", "); 
            		break;
            	case TYPE_T:
            	   
            	    
            	    String conventionSpecies = formality.getSectionA().getConventionSpecies();
            	    if(StringUtils.isNotBlank(conventionSpecies) && Character.isDigit(conventionSpecies.charAt(0))) {
                        String startDigits = conventionSpecies.split("\\s+")[0];
                        conventionSpecies = conventionSpecies.replaceFirst(startDigits, "").trim();
                    }
            		String conventionDescription =  formality.getSectionA().getConventionDescription();
            		if(StringUtils.isNotBlank(conventionDescription) && Character.isDigit(conventionDescription.charAt(0))) {
            			String startDigits = conventionDescription.split("\\s+")[0];
            			conventionDescription = conventionDescription.replaceFirst(startDigits, "").trim();
            		}
            		result.append("<b>");
            		if(StringUtils.isNotBlank(conventionSpecies)) {
            		    result.append(conventionSpecies);    
            		}
            		if(StringUtils.isNotBlank(conventionSpecies) && StringUtils.isNotBlank(conventionDescription)) {
            		    result.append("-");
            		}
            		if(StringUtils.isNotBlank(conventionDescription)) {
                        result.append(conventionDescription);    
                    }
            		result.append("</b>").append(", ");	
            		
            		break;
            	case TYPE_A:
            	    
            	    String annotationType  = formality.getSectionA().getAnnotationType();
                    if(StringUtils.isNotBlank(annotationType) && Character.isDigit(annotationType.charAt(0))) {
                        String startDigits = annotationType.split("\\s+")[0];
                        conventionSpecies = annotationType.replaceFirst(startDigits, "").trim();
                    }
                    String annotationDescription  =  formality.getSectionA().getAnnotationDescription();
                    if(StringUtils.isNotBlank(annotationDescription) && Character.isDigit(annotationDescription.charAt(0))) {
                        String startDigits = annotationDescription.split("\\s+")[0];
                        annotationDescription = annotationDescription.replaceFirst(startDigits, "").trim();
                    }
                    result.append("<b>");
                    if(StringUtils.isNotBlank(annotationType)) {
                        result.append(annotationType);    
                    }
                    if(StringUtils.isNotBlank(annotationType) && StringUtils.isNotBlank(annotationDescription)) {
                        result.append("-");
                    }
                    if(StringUtils.isNotBlank(annotationDescription)) {
                        result.append(annotationDescription);    
                    }
                    result.append("</b>").append(", "); 
            		break;
            	}
           }else {
           		result.append("<b></u>").append(typeFormalities.get(0).getTextInVisura()).append("</u></b>").append(", ");	
           }
            result.append(frmTxt(typeFormalities.get(0).getCertificationText()));
        }

        return result.toString();
    }


    private DocumentSubject getMinCadastralDocumentSubject() {
        if (!ValidationHelper.isNullOrEmpty(getRequest().getSubject())
                && !ValidationHelper.isNullOrEmpty(getRequest().getSubject().getDocumentSubjectList())) {

            Optional<DocumentSubject> minDocumentSubject = getRequest().getSubject().getDocumentSubjectList().stream()
                    .filter(x -> DocumentType.CADASTRAL.equals(x.getType()))
                    .min(Comparator.comparing(DocumentSubject::getDate));

            return minDocumentSubject.orElse(null);
        }
        return null;
    }

    private DocumentSubject getNewestCadastralDocumentSubject(Request request)
            throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(request.getDocumentsRequest())) {
            Document document = request.getDocumentsRequest().stream()
                    .filter(d -> DocumentType.CADASTRAL.getId().equals(d.getTypeId()))
                    .max(Comparator.comparing(Document::getDate)).orElse(null);

            if (!ValidationHelper.isNullOrEmpty(document)) {
                List<DocumentSubject> documentSubjects = DaoManager.load(DocumentSubject.class, new Criterion[]{
                        Restrictions.eq("document.id", document.getId()),
                        Restrictions.eq("type", DocumentType.CADASTRAL)
                }, new Order[]{
                        Order.desc("date")
                });
                if (!ValidationHelper.isNullOrEmpty(documentSubjects)) {
                    return documentSubjects.get(0);
                }
            }
        }
        return null;
    }

    private String getCadastralDateDocumentSubject() throws PersistenceBeanException, IllegalAccessException {
        String result = "";
        DocumentSubject documentSubject = getNewestCadastralDocumentSubject(getRequest());
        if (!ValidationHelper.isNullOrEmpty(documentSubject)) {
            result = DateTimeHelper.toString(documentSubject.getDate());
        }
        return result;
    }

    private String getCadastralProvinceDocumentSubject() throws PersistenceBeanException, IllegalAccessException {
        String result = "";
        DocumentSubject documentSubject = getNewestCadastralDocumentSubject(getRequest());
        if (!ValidationHelper.isNullOrEmpty(documentSubject)) {
            result = documentSubject.getProvince().getDescription().toUpperCase();
        }
        return result;
    }

    private String getCadastralPropertyListDocumentSubject() throws PersistenceBeanException, IllegalAccessException {
        StringBuilder result = new StringBuilder();
        if (!ValidationHelper.isNullOrEmpty(getRequest().getSituationEstateLocations())) {
            int propertyListNumber = 1;
            List<EstateSituation> situationEstateLocations = getRequest().getSituationEstateLocations();
            for (EstateSituation estateSituation : situationEstateLocations) {
                if(!ValidationHelper.isNullOrEmpty(estateSituation.getSalesDevelopment()) && estateSituation.getSalesDevelopment())
                    continue;
                List<Property> propertyList = estateSituation.getPropertyList();
                sortPropertiesByDistraintActIdProperties(propertyList);

                if (!ValidationHelper.isNullOrEmpty(propertyList)) {
                    result.append(getCollectedNumberedPropertyList(propertyList, propertyListNumber));
                    propertyListNumber += propertyList.size();
                }
                result.append(frmTxt(estateSituation.getCommentWithoutInitialize(), "<br/><i>", "</i>",
                        x -> x.replaceAll("<.+?>", "")));
            }
        }
        return result.toString();
    }

    private void sortPropertiesByDistraintActIdProperties(List<Property> propertyList) {
        if(!ValidationHelper.isNullOrEmpty(getRequest().getDistraintFormality())){
            List<Long> cadastralDataIdsFromDistrFormProps = getRequest().getDistraintFormality().getSectionB().stream()
                    .map(SectionB::getProperties).flatMap(List::stream)
                    .filter(x->!ValidationHelper.isNullOrEmpty(x.getCadastralData()))
                    .map(x->x.getCadastralData().get(0).getId()).collect(Collectors.toList());

            if(!cadastralDataIdsFromDistrFormProps.isEmpty()){
                propertyList.sort((left, right) -> {
                    if(ValidationHelper.isNullOrEmpty(left.getCadastralData())
                            && ValidationHelper.isNullOrEmpty(right.getCadastralData())){
                        return 0;
                    }else if(ValidationHelper.isNullOrEmpty(left.getCadastralData())){
                        return 1;
                    }else if(ValidationHelper.isNullOrEmpty(right.getCadastralData())){
                        return -1;
                    }else {
                        return Integer.compare(
                                cadastralDataIdsFromDistrFormProps.indexOf(left.getCadastralData().get(0).getId()),
                                cadastralDataIdsFromDistrFormProps.indexOf(right.getCadastralData().get(0).getId()));
                    }
                });
            }
        }
    }

    private String getCollectedNumberedPropertyList(List<Property> propertyList, int startNumber) throws PersistenceBeanException,
            IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < propertyList.size(); i++) {
            sb.append("<br/>");
            Property property = propertyList.get(i);
            sb.append("<b>").append(startNumber++).append(")").append("</b>&nbsp;");
            if (RealEstateType.LAND.getId().equals(property.getType())) {
                sb.append("\"Catasto Terreni");
            } else {
                sb.append("\"Catasto Fabbricati");
            }
            sb.append(" del Comune di ");
            
            sb.append(frmTxt(property.getCityDescription())).append(", ");
            sb.append(frmTxt(WordUtils.capitalizeFully(property.getAddress()), "", " ").trim());
            sb.append(frmTxt(property.getFloor(),", piano "));
            sb.append(frmTxt(property.getInterno(), ", interno "));
            sb.append(frmTxt(property.getScala(), ", scala "));

            //In some cases, a new line is added after the property number
            //The problem is with the <b> tag and indented text
            //To fix this, we need to add this bold space.
            sb.append(MAGIC_SPACE_FIX);

            sb.append("in ditta ");

            sb.append(getSubjectsRelationshipData(property));

            addCadastralDataToReportCertificated(sb, property.getCadastralData());
            if(!ValidationHelper.isNullOrEmpty(property.getArea()) && !property.getArea().equals("0")) {
            	 sb.append(frmTxt(property.getArea(), ", zona censuaria "));
            }
           
            if(ValidationHelper.isNullOrEmpty(property.getType()) || 
                    RealEstateType.BUILDING.getId().equals(property.getType())){
                sb.append(frmTxt(property.getCategory(), ", categoria ", "", CadastralCategory::getCodeInVisura));    
            }else  if(RealEstateType.LAND.getId().equals(property.getType()) && 
                    !ValidationHelper.isNullOrEmpty(property.getQuality())){
                sb.append(frmTxt(property.getQuality().toLowerCase(), ", "));
            }
            sb.append(frmTxt(property.getClassRealEstate(), ", classe "));
            sb.append(addConsistencyProperty(property));

            if (RealEstateType.LAND.getId().equals(property.getType())) {
                if (!ValidationHelper.isNullOrEmpty(property.getHectares())
                        || !ValidationHelper.isNullOrEmpty(property.getAres())
                        || !ValidationHelper.isNullOrEmpty(property.getCentiares())) {
                    sb.append(", superficie");
                    sb.append(frmTxt(property.getHectares(), " ha ", "", x -> String.valueOf(x.intValue())));
                    sb.append(frmTxt(property.getAres(), " are ", "", x -> String.valueOf(x.intValue())));
                    sb.append(frmTxt(property.getCentiares(), " ca ", "", x -> String.valueOf(x.intValue())));
                }
                sb.append(frmTxt(property.getCadastralIncome(), ", Reddito Dominicale Euro "));
                sb.append(frmTxt(property.getAgriculturalIncome(), " - Reddito Agrario Euro "));
            }

            sb.append(frmTxt(property.getCadastralArea(), ", superficie catastale mq ", x -> String.valueOf(x.intValue())));

            sb.append(frmTxt(property.getExclusedArea(), "( totale escluse aree scoperte mq ", ")",
                    x -> String.valueOf(x.intValue())));

            sb.append(frmTxt(property.getRevenue(), " Rendita Catastale Euro "));
            sb.append("\". ");

            addDatafromPropertyText(sb, property);

        }
        return sb.toString();
    }

    private void addDatafromPropertyText(StringBuilder sb, Property property) {
        for (EstateSituation situation : getRequest().getSituationEstateLocations()) {
            if(!ValidationHelper.isNullOrEmpty(situation.getSalesDevelopment()) && situation.getSalesDevelopment())
                continue;
            for (DatafromProperty datafromProperty :
                    emptyIfNull(property.getAssociatedDatafromPropertiesWithEstateSituationById(situation.getId()))) {
            	DatafromProperty dfp = null;
				try {
					dfp = DaoManager.get(DatafromProperty.class, datafromProperty.getId());
				}catch (Exception e) {
					e.printStackTrace();
				} 
				if(dfp != null) {
					sb.append("<br/>");
	                sb.append("<i>").append(datafromProperty.getText()).append("</i>");	
				}
            }
        }
    }

    private String addConsistencyProperty(Property property) {
        StringBuilder sb = new StringBuilder();
        if (!ValidationHelper.isNullOrEmpty(property.getCategory())
                && !ValidationHelper.isNullOrEmpty(property.getCategory().getCodeInVisura())) {
            if (property.getCategory().getCodeInVisura().trim().startsWith("A")) {
                sb.append(frmTxt(property.getConsistency(), ", vani ", "",
                        x -> x.replaceAll("vani|VANI", "").trim()));
            } else {
                sb.append(frmTxt(property.getConsistency(), ", consistenza mq ", "",
                        x -> x.replaceAll("mq|MQ", "").trim()));
            }
        }
        return sb.toString();
    }

    private void addCadastralDataToReportCertificated(StringBuilder sb, List<CadastralData> cadastralDataList) {
        if (!ValidationHelper.isNullOrEmpty(cadastralDataList)) {
            for(int c=0; c < cadastralDataList.size();c++) {
                CadastralData cadastralData = cadastralDataList.get(c);
                if(c > 0)
                    sb.append(" e ");
                sb.append(frmTxt(cadastralData.getSection(), " SEZ. "));
                sb.append(getCadastralSheetParticleSub(cadastralData));
                if(c > 0 && (c == cadastralDataList.size()-1)) {
                    sb.append(" (graffate) ");
                }
            }
        }
    }

    private String getCadastralSheetParticleSub(CadastralData cadastralData) {
        return (frmTxt(cadastralData.getSheet(), " foglio ")) +
                (frmTxt(cadastralData.getParticle(), " particella ")) +
                (frmTxt(cadastralData.getSub(), " sub  "));
    }

    private String getSubjectsRelationshipData(Property property) throws PersistenceBeanException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();

        if (!ValidationHelper.isNullOrEmpty(property.getRelationships())) {
            List<Relationship> relationships = property.getRelationships();
            List<Long> documentsIds = getRequest().getDocumentsRequest().stream()
                    .filter(x -> x.getTypeId().equals(DocumentType.CADASTRAL.getId()))
                    .map(IndexedEntity::getId).collect(Collectors.toList());
            for (Relationship relationship : relationships) {
                boolean reportDataCondition = !ValidationHelper.isNullOrEmpty(relationship.getSubject())
                        && !ValidationHelper.isNullOrEmpty(relationship.getTableId())
                        && !ValidationHelper.isNullOrEmpty(documentsIds)
                        && documentsIds.contains(relationship.getTableId());

                if (reportDataCondition) {
                    sb.append(" ").append(getSubjectData(Collections.singletonList(relationship.getSubject()),
                            null, false, null, false,
                            false, true, false));
                    sb.append(", ").append("<i>").append(relationship.getPropertyType());
                    if(!ValidationHelper.isNullOrEmpty(relationship.getQuote())) {
                        sb.append(" per ").append(relationship.getQuote());
                    }
                    boolean showRegime = false;
                    if(!ValidationHelper.isNullOrEmpty(relationship.getRelationshipTypeId()) &&
                            !ValidationHelper.isNullOrEmpty(getRequestClient().getRegime()) &&
                            getRequestClient().getRegime()){
                        showRegime = true;
                    }else if(ValidationHelper.isNullOrEmpty(relationship.getRelationshipTypeId())){
                        showRegime = true;
                    }
                    if(showRegime) {
                        sb.append(frmTxt(relationship.getRegime(), " in regime di ", "",
                                x -> x.replaceAll("in regime di ", "")));
                    }

                    sb.append(";").append("</i>");
                }
            }
        }
        return sb.toString();
    }

    private String getFormattedCertificateDate(Date certificateDate) {
        return DateTimeHelper.toFormatedString(certificateDate,
                DateTimeHelper.getMonthWordDatePattert(), Locale.ITALY);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private static <T> String frmTxt(T value) {
        return frmTxt(value, "", "", "", Object::toString);
    }

    private static <T> String frmTxt(T value, String prefix) {
        return frmTxt(value, prefix, "", "", Object::toString);
    }

    private static <T> String frmTxt(T value, String prefix, Function<T, String> operator) {
        return frmTxt(value, prefix, "", "", operator);
    }

    private static <T> String frmTxt(T value, Function<T, String> operator) {
        return frmTxt(value, "", "", "", operator);
    }

    private static <T> String frmTxt(T value, String prefix, String postfix) {
        return frmTxt(value, prefix, postfix, "", Object::toString);
    }

    private static <T> String frmTxt(T value, String prefix, String postfix, Function<T, String> operator) {
        return frmTxt(value, prefix, postfix, "", operator);
    }

    private static <T> String frmTxt(T value, String prefix, String postfix, String badResult,
                              Function<T, String> operator) {
        if (ValidationHelper.isNullOrEmpty(value)) {
            return badResult;
        }
        String result = operator.apply(value);
        if (ValidationHelper.isNullOrEmpty(result)) {
            return badResult;
        }
        return prefix + result + postfix;
    }

    @Override
    void addBody() throws PersistenceBeanException, IllegalAccessException {
        fillTagTableList();
        getJoiner().add(getCell("", BORDER_NONE + " width: 10% !important;") +
                getCell(getBodyText().replaceAll(DocumentGenerationTags.CERTIFICAZIONE_TABLE.getTag(), getBodyContent()),
                        BORDER_NONE + TEXT_JUSTIFY + "line-height:34px; width: 64% !important;") +
                getCell("", BORDER_NONE)
        );
    }

    @Override
    void addRequestComment() {
    }


    @Override
    protected void addHeader() {
    }

    @Override
    void addEstateFormality() {

    }

    @Override
    protected void addFooter() {

    }

    @Override
    protected void addFinalCost() {

    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }
}
