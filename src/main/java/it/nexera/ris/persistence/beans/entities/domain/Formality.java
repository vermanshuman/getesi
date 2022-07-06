package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.FormalityStateType;
import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Entity
@Table(name = "formality")
public class Formality extends IndexedEntity {

    private static final long serialVersionUID = -4930211312003998016L;
    public transient final Log log = LogFactory.getLog(getClass());

    @ManyToOne
    @JoinColumn(name = "provincial_office_id")
    private LandChargesRegistry provincialOffice;

    @ManyToOne
    @JoinColumn(name = "reclame_property_service_id")
    private LandChargesRegistry reclamePropertyService;

    @Column(name = "general_register", length = 30)
    private String generalRegister;

    @Column(name = "particular_register", length = 30)
    private String particularRegister;

    @Column(name = "inspection_date")
    private Date inspectionDate;

    @Column(name = "number_Presentation")
    private String numberPresentation;

    @Column(name = "presentation_date")
    private Date presentationDate;

    @ManyToMany
    @JoinTable(name = "request_formality_forced", joinColumns = {
            @JoinColumn(name = "formality_id", table = "formality")
    }, inverseJoinColumns = {
            @JoinColumn(name = "request_id", table = "request")
    })
    private List<Request> requestForcedList;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @OneToOne(mappedBy = "formality", fetch = FetchType.EAGER)
    private SectionA sectionA;

    @OneToMany(mappedBy = "formality")
    private List<SectionB> sectionB;

    @OneToMany(mappedBy = "formality")
    private Set<SectionC> sectionC;

    @OneToMany(mappedBy = "formality")
    private List<SectionD> sectionD;

    @Column(name = "type")
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToMany(mappedBy = "formalityList")
    private List<EstateSituation> estateSituationList;

    @Column(name = "comment", columnDefinition = "LONGTEXT")
    private String comment;

    @ManyToMany(mappedBy = "formalityPdfList", fetch = FetchType.LAZY)
    private List<Request> requestList;

    @ManyToMany(mappedBy = "formalityExternalList", fetch = FetchType.LAZY)
    private List<Subject> subjectExternalList;

    //FormalityStateType enum
    @Column(name = "state")
    private Long state;

    @OneToMany(mappedBy = "distraintFormality", fetch = FetchType.LAZY)
    private List<Request> distraintRequests;

    @OneToMany(mappedBy = "formality", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<EstateSituationFormalityProperty> estateSituationFormalityProperties;

    @Column(name = "distraint_comment", columnDefinition = "LONGTEXT")
    private String distraintComment;
    

    @Column(name = "text_certification", columnDefinition = "TEXT")
    private String textCertification;

    @Transient
    private Boolean visible;

    @Transient
    private Subject currentSubject;

    @Transient
    private String sectionAText;

    @Transient
    private Set<Map.Entry<Integer, List<Property>>> sectionBMap;

    @Transient
    private boolean shouldReportRelationships;

    public String getActType() {
        if (!ValidationHelper.isNullOrEmpty(getDocument()) && DocumentType.INDIRECT_CADASTRAL_REQUEST.getId().equals(getDocument().getTypeId())) {
            return "VISURA CARTACEA";
        }
        if (!ValidationHelper.isNullOrEmpty(getSectionA())) {
            SectionA sectionA = getSectionA();
            if (!ValidationHelper.isNullOrEmpty(sectionA.getDerivedFrom())) {
                return sectionA.getDerivedFrom();
            } else if (!ValidationHelper.isNullOrEmpty(sectionA.getConventionDescription())) {
                return sectionA.getConventionDescription();
            } else if (!ValidationHelper.isNullOrEmpty(sectionA.getAnnotationDescription())) {
                return sectionA.getAnnotationDescription();
            }
        }
        return "";
    }

    public String getCurrentSubjectType() {
        return getCurrentSubjectType(getCurrentSubject());
    }

    public String getCurrentSubjectType(Subject subject) {
        if (!ValidationHelper.isNullOrEmpty(getSectionC())) {
            List<String> types = getSectionC().stream().filter(c -> c.getSubject().contains(subject))
                    .map(SectionC::getSectionCType).collect(Collectors.toList());
            StringJoiner result = new StringJoiner("/");
            if (types.contains("A favore")) {
                result.add("F");
            }
            if (types.contains("Contro")) {
                result.add("C");
            }
            return result.toString();
        }
        return "";
    }

    public String getSpecialSubjectType(Long id) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getSectionC()) && !ValidationHelper.isNullOrEmpty(id)) {
            Subject subject = DaoManager.get(Subject.class, id);
            List<String> types = getSectionC().stream().filter(c -> c.getSubject().contains(subject))
                    .map(SectionC::getSectionCType).collect(Collectors.toList());
            StringJoiner result = new StringJoiner(", ");
            if (types.contains("A favore")) {
                result.add("F");
            }
            if (types.contains("Contro")) {
                result.add("C");
            }
            return result.toString();
        }
        return "";
    }

    public String getDescriptionSectionA() {
        if (!ValidationHelper.isNullOrEmpty(getSectionA())) {
            return getSectionA().getTitleDescription();
        } else {
            return "";
        }
    }

    public String getTypeSectionC() {
        return getSectionC().stream().map(SectionC::getSectionCType).distinct().collect(Collectors.joining(", "));
    }

    public String getRegisterString() {
        return String.format("%s/%s<br/>%s", getGeneralRegister(), getParticularRegister(),
                DateTimeHelper.toString(getPresentationDate()));
    }

    public String getFirstPropertyAlienatedTable() throws PersistenceBeanException, IllegalAccessException {
        List<Property> properties = DaoManager.load(Property.class, new CriteriaAlias[]{
                new CriteriaAlias("sectionB", "sb", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("sb.formality.id", getId())
        });
        if (!ValidationHelper.isNullOrEmpty(properties) && !ValidationHelper.isNullOrEmpty(properties.get(0).getCity())) {
            return properties.get(0).getCity().getDescription().toUpperCase();
        } else return "";
    }

    public List<String> getCityPropertyTable() throws PersistenceBeanException, IllegalAccessException {
        List<Property> properties = DaoManager.load(Property.class, new CriteriaAlias[]{
                new CriteriaAlias("sectionB", "sb", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("sb.formality.id", getId())
        });
        List<String> cities = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(properties)) {
            for (Property property : properties) {
                if (!ValidationHelper.isNullOrEmpty(property.getCity())) {
                    cities.add(property.getCity().getDescription().toUpperCase());
                }
            }
        }
        return cities;
    }

    public String getSubjectAlienatedTable(Request request) throws PersistenceBeanException, IllegalAccessException {
        List<Long> listIds = EstateSituationHelper.getIdSubjects(request);
        List<Subject> presumableSubjects = EstateSituationHelper.getListSubjects(listIds, request.getSubject());
        List<Subject> unsuitableSubjects = SubjectHelper.deleteUnsuitable(presumableSubjects,
                request.getSituationEstateLocations().get(0).getFormalityList());
        presumableSubjects.removeAll(unsuitableSubjects);
        presumableSubjects.removeIf(x -> x.getFullName().replaceAll("\\s+", " ")
                .equals(request.getSubject().getFullName().replaceAll("\\s+", " ")));
        presumableSubjects.add(request.getSubject());

        String subContro = "";
        String subPresumable = "";
        if (!ValidationHelper.isNullOrEmpty(request.getSubjectToGenerateAlienatedTemplate())) {
            subPresumable = request.getSubjectToGenerateAlienatedTemplate().getNameBirthDateUpperCase();
        } else {
            subContro = getSectionC().stream().filter(c -> "Contro".equals(c.getSectionCType())).map(SectionC::getSubject)
                    .flatMap(List::stream).filter(s -> s.getRequestList().stream().anyMatch(r -> r.getId().equals(request.getId())))
                    .map(Subject::getNameBirthDateUpperCase).collect(Collectors.joining(", "));
            subPresumable = presumableSubjects.stream()
                    .filter(s-> !ValidationHelper.isNullOrEmpty(s) && !ValidationHelper.isNullOrEmpty(s.getNameBirthDateUpperCase()))
                    .map(Subject::getNameBirthDateUpperCase).collect(Collectors.joining(", "));
        }

        SectionA sectionA = ValidationHelper.isNullOrEmpty(getSectionA()) ? new SectionA() : getSectionA();
        String subFavore = getSectionC().stream().filter(c -> "A favore".equals(c.getSectionCType())).map(SectionC::getSubject)
                .flatMap(List::stream).map(Subject::getNameBirthDateUpperCase).collect(Collectors.joining(", "));

        if (!ValidationHelper.isNullOrEmpty(subContro) && subContro.equals(subPresumable)) {
            subPresumable = "";
        } else if (!ValidationHelper.isNullOrEmpty(subContro) && subPresumable.contains(subContro)) {
            subContro = "";
        }

        String deriveCode = "";
        String publicOffice = "";

        if (!ValidationHelper.isNullOrEmpty(sectionA.getDerivedFromCode())) {
            int code = Integer.parseInt(sectionA.getDerivedFromCode());
            if (code >= 8000 && code <= 9999) {
                deriveCode = String.format(ResourcesHelper.getString("alienatedTableDeriveCode"),
                        sectionA.getOtherParticularRegister() == null ? "" : sectionA.getOtherParticularRegister(),
                        DateTimeHelper.toStringDateWithDots(sectionA.getOtherData()));
            }
        }
        if (!ValidationHelper.isNullOrEmpty(sectionA.getPublicOfficialNotary())) {
            publicOffice = "atto per notaio " + WordUtils.capitalizeFully(sectionA.getPublicOfficialNotary());
        } else {
            publicOffice = "decreto emesso da tribunale";
        }
        return String.format(ResourcesHelper.getString("alienatedTableSubject"),
                publicOffice, sectionA.getSeatCamelCase(), DateTimeHelper.toStringDateWithDots(sectionA.getTitleDate()),
                sectionA.getNumberDirectory(), deriveCode, subPresumable, subContro, sectionA.generateDeliveredFromCodeStr(), subFavore);
    }

    public String getSubjectDeceadesTable(Request request) throws PersistenceBeanException, IllegalAccessException {
        List<Long> listIds = EstateSituationHelper.getIdSubjects(request);
        List<Subject> presumableSubjects = EstateSituationHelper.getListSubjects(listIds, request.getSubject());
        List<Subject> unsuitableSubjects = SubjectHelper.deleteUnsuitable(presumableSubjects,
                request.getSituationEstateLocations().get(0).getFormalityList());
        presumableSubjects.removeAll(unsuitableSubjects);
        SectionA sectionA = ValidationHelper.isNullOrEmpty(getSectionA()) ? new SectionA() : getSectionA();
        Stream<Subject> streamFirst = getSectionC().stream().filter(c -> "A favore".equals(c.getSectionCType())).map(SectionC::getSubject).flatMap(List::stream);
        String subFavore =
                Stream.concat(presumableSubjects.stream(), streamFirst).map(Subject::getNameBirthDateUpperCaseBold).collect(Collectors.joining(", "));
        String nameAndSurname = "";
        String birthCity = "";
        String birthDate = "";
        String seat = "";
        if (!ValidationHelper.isNullOrEmpty(request.getSubject())) {
            nameAndSurname = request.getSubject().getFullName();
            birthDate = DateTimeHelper.toStringDateWithDots(request.getSubject().getBirthDate());
            if (request.getSubject().getBirthCity() != null) {
                birthCity = request.getSubject().getBirthCity().getCamelCityDescription();
            }
        }
        if (!ValidationHelper.isNullOrEmpty(sectionA.getSeat()) && !ValidationHelper.isNullOrEmpty(sectionA.getPublicOfficial())) {
            seat = " di " + sectionA.getSeat();
        }
        return String.format(ResourcesHelper.getString("deceadesTableSubject"),
                nameAndSurname, birthCity, birthDate,
                DateTimeHelper.toStringDateWithDots(sectionA.getDeathDate()), sectionA.getNumberDirectory(),
                DateTimeHelper.toStringDateWithDots(sectionA.getTitleDate()), sectionA.getPublicOfficialCamelCase(),
                seat, subFavore);
    }

    public List<Property> loadPropertiesByRelationship(List<Subject> presumableSubjects) throws PersistenceBeanException, IllegalAccessException {
        List<Property> propertyList = DaoManager.load(Property.class, new CriteriaAlias[]{
                new CriteriaAlias("relationships", "r", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("r.formality.id", getId()),
                Restrictions.in("r.subject", presumableSubjects)
        });

        for (Property property : propertyList) {
            for (CadastralData cadastralData : property.getCadastralData()) {
                if (!ValidationHelper.isNullOrEmpty(property.getCadastralData())) {
                    property.setCadastralData(property.getCadastralData().stream().distinct()
                            .collect(Collectors.toList()));
                }
            }
        }

        return propertyList;

    }

    public String getConservatoryStr() {
        if (!ValidationHelper.isNullOrEmpty(getReclamePropertyService())) {
            return getReclamePropertyService().toString();
        } else if (!ValidationHelper.isNullOrEmpty(getProvincialOffice())) {
            return getProvincialOffice().toString();
        } else return "";
    }

    public String getDescriptionStr() {
        if (ValidationHelper.isNullOrEmpty(getSectionA()) || ValidationHelper.isNullOrEmpty(getSectionA())) {
            return "";
        } else if (!ValidationHelper.isNullOrEmpty(getSectionA().getConventionSpecies())) {
            return String.format("%s-%s", getSectionA().getConventionSpecies(),
                    getSectionA().getConventionDescription());
        } else if (!ValidationHelper.isNullOrEmpty(getSectionA().getMortgageSpecies())) {
            return String.format("%s-%s", getSectionA().getMortgageSpecies(),
                    getSectionA().getDerivedFrom());
        } else return "";
    }

    public String getFormalityStr() {
        return String.format("%s/%s del %s", getGeneralRegister() == null ? "" : getGeneralRegister(),
                getParticularRegister() == null ? "" : getParticularRegister(),
                getPresentationDate() == null ? "" : DateTimeHelper.toStringDateWithDots(getPresentationDate()));
    }

    public TypeActEnum getTypeEnum() {
        if(!ValidationHelper.isNullOrEmpty(getType())) {
            if (getType().equalsIgnoreCase("trascrizione")) {
                return TypeActEnum.TYPE_T;
            } else if (getType().equalsIgnoreCase("iscrizione")) {
                return TypeActEnum.TYPE_I;
            } else {
                return TypeActEnum.TYPE_A;
            }    
        }else
            return TypeActEnum.TYPE_T;
    }

    public String getAllFields() throws TypeFormalityNotConfigureException {
        SectionA sectionA;
        if (!ValidationHelper.isNullOrEmpty(getSectionA()) && !ValidationHelper.isNullOrEmpty(getSectionA())) {
            sectionA = getSectionA();
        } else {
            sectionA = new SectionA();
        }
        TypeActEnum typeActEnum = getTypeEnum();
        Integer code = ValidationHelper.isNullOrEmpty(sectionA.getDerivedFromCode()) ?
                0 : Integer.parseInt(sectionA.getDerivedFromCode());
        boolean textInVisuraAppend = false;
        if (code / 1000 != 0) {
            if (code / 1000 == 9 || code / 1000 == 8) {
                textInVisuraAppend = true;
            }
            code = code % 1000;
        }
        TypeFormality typeFormality = null;
        try {
            List<TypeFormality> typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                    Restrictions.eq("type", typeActEnum),
                    Restrictions.eq("code", code.toString())
            });
            if (!ValidationHelper.isNullOrEmpty(typeFormalities)) {
                typeFormality = typeFormalities.get(0);
            }
        } catch (Exception e) {
            LogHelper.log(Logger.getLogger(Formality.class), e);
        }
        if (typeFormality == null) {
            throw new TypeFormalityNotConfigureException("TypeFormality not found", typeActEnum, code.toString());
        }
        String delIfNeed = "";
        if (!ValidationHelper.isNullOrEmpty(typeFormality.getInitText())) {
            if ("Notificata dall'Ufficiale Giudiziario".equalsIgnoreCase(typeFormality.getInitText().trim())) {
                delIfNeed = " del";
            }
        }

        String inDataIfNeed = "del";
        if (!ValidationHelper.isNullOrEmpty(typeFormality.getInitText())) {
            if ("Atto amministrativo redatto da".equalsIgnoreCase(typeFormality.getInitText().trim())) {
                inDataIfNeed = " in data";
            }
        }

        String str1 = ResourcesHelper.getString("estateSituationTableFormality1");
        String str2 = ResourcesHelper.getString("estateSituationTableFormality2");
        String str2Special = ResourcesHelper.getString("estateSituationTableFormality2Special");
        String str3 = ResourcesHelper.getString("estateSituationTableFormality3");
        String str2Beginning;
        if (!ValidationHelper.isNullOrEmpty(sectionA.getPublicOfficialNotary())) {
            str2Beginning = String.format("%s%s %s", typeFormality.getInitText(), delIfNeed,
                    WordUtils.capitalizeFully(sectionA.getPublicOfficialNotary()));
        } else if (!ValidationHelper.isNullOrEmpty(sectionA.getPublicOfficial())) {
            if (!ValidationHelper.isNullOrEmpty(sectionA.getDerivedFrom())
                    && sectionA.getDerivedFrom().contains("PIGNORAMENTO")) {
                str2Beginning = String.format(str2Special, WordUtils.capitalizeFully(sectionA.getPublicOfficial()));
            } else {
                str2Beginning = String.format("%s%s %s", typeFormality.getInitText(), delIfNeed,
                        WordUtils.capitalizeFully(sectionA.getPublicOfficial()));
            }
        } else {
            str2Beginning = typeFormality.getInitText();
        }
        String part3 = "";
        if (!ValidationHelper.isNullOrEmpty(sectionA.getMortgageSpecies())) {
            String finalText = typeFormality.getFinalText();

            String duration = "";

            if (("capitale mutuato estinguibile in".equalsIgnoreCase(typeFormality.getFinalText())
                    || typeFormality.getFinalText().contains("estinguibile in"))) {

                if (ValidationHelper.isNullOrEmpty(sectionA.getDuration()))
                    finalText = "quale capitale";
                else
                    duration = " " + sectionA.getDuration();
            }

            part3 = String.format(str3, sectionA.getTotal(),
                    (ValidationHelper.isNullOrEmpty(sectionA.getCapital()) || sectionA.getTotal().equals(sectionA.getCapital())) ? "" :
                        (" di cui " + sectionA.getCapital()), 
                        (ValidationHelper.isNullOrEmpty(sectionA.getCapital()) ? "" : finalText), duration);
        }
        String textInVisura = "";
        if (!ValidationHelper.isNullOrEmpty(getSectionA())
                && !ValidationHelper.isNullOrEmpty(getSectionA().getDerivedFromCode())) {
            
            if(getSectionA().getDerivedFromCode().trim().endsWith("00")) {
                StringBuilder result = new StringBuilder();
                switch (getTypeEnum()) {
                case TYPE_I:
                    String mortgageSpecies = getSectionA().getMortgageSpecies();
                    if(StringUtils.isNotBlank(mortgageSpecies) && Character.isDigit(mortgageSpecies.charAt(0))) {
                        String startDigits = mortgageSpecies.split("\\s+")[0];
                        mortgageSpecies = mortgageSpecies.replaceFirst(startDigits, "").trim();
                    }
                    String derivedFrom =  getSectionA().getDerivedFrom();
                    if(StringUtils.isNotBlank(derivedFrom) && Character.isDigit(derivedFrom.charAt(0))) {
                        String startDigits = derivedFrom.split("\\s+")[0];
                        derivedFrom = derivedFrom.replaceFirst(startDigits, "").trim();
                    }
                    
                    if(StringUtils.isNotBlank(mortgageSpecies)) {
                        result.append(mortgageSpecies);    
                    }
                    if(StringUtils.isNotBlank(mortgageSpecies) && StringUtils.isNotBlank(derivedFrom)) {
                        result.append("-");
                    }
                    if(StringUtils.isNotBlank(derivedFrom)) {
                        result.append(derivedFrom);    
                    }
                    textInVisura = result.toString();
                    break;
                case TYPE_T:
                    
                    String conventionSpecies = getSectionA().getConventionSpecies();
                    if(StringUtils.isNotBlank(conventionSpecies) && Character.isDigit(conventionSpecies.charAt(0))) {
                        String startDigits = conventionSpecies.split("\\s+")[0];
                        conventionSpecies = conventionSpecies.replaceFirst(startDigits, "").trim();
                    }
                    
                    String conventionDescription =  getSectionA().getConventionDescription();
                    if(StringUtils.isNotBlank(conventionDescription) && Character.isDigit(conventionDescription.charAt(0))) {
                        String startDigits = conventionDescription.split("\\s+")[0];
                        conventionDescription = conventionDescription.replaceFirst(startDigits, "").trim();
                    }
                    
                    if(StringUtils.isNotBlank(conventionSpecies)) {
                        result.append(conventionSpecies);    
                    }
                    if(StringUtils.isNotBlank(conventionSpecies) && StringUtils.isNotBlank(conventionDescription)) {
                        result.append("-");
                    }
                    if(StringUtils.isNotBlank(conventionDescription)) {
                        result.append(conventionDescription);    
                    }
                    textInVisura = result.toString();

                    break;
                case TYPE_A:
                    String annotationType  = getSectionA().getAnnotationType();
                    if(StringUtils.isNotBlank(annotationType) && Character.isDigit(annotationType.charAt(0))) {
                        String startDigits = annotationType.split("\\s+")[0];
                        conventionSpecies = annotationType.replaceFirst(startDigits, "").trim();
                    }
                    
                    String annotationDescription  =  getSectionA().getAnnotationDescription();
                    if(StringUtils.isNotBlank(annotationDescription) && Character.isDigit(annotationDescription.charAt(0))) {
                        String startDigits = annotationDescription.split("\\s+")[0];
                        annotationDescription = annotationDescription.replaceFirst(startDigits, "").trim();
                    }
                    if(StringUtils.isNotBlank(annotationType)) {
                        result.append(annotationType);    
                    }
                    if(StringUtils.isNotBlank(annotationType) && StringUtils.isNotBlank(annotationDescription)) {
                        result.append("-");
                    }
                    if(StringUtils.isNotBlank(annotationDescription)) {
                        result.append(annotationDescription);    
                    }
                    textInVisura =  result.toString();
                    break;
                }
            }
        }
        if(textInVisura == null || textInVisura.trim().isEmpty())
            textInVisura = typeFormality.getTextInVisura();

        return String.format(str1, getGeneralRegister(), getParticularRegister(),
                DateTimeHelper.toStringDateWithDots(getPresentationDate()),
                textInVisura + (textInVisuraAppend ? " - RETTIFICA" : "")) +
                String.format(str2, str2Beginning, sectionA.getCapitalizeSeat(), inDataIfNeed,
                        DateTimeHelper.toStringDateWithDots(sectionA.getTitleDate()), sectionA.getNumberDirectory(),
                        getSubjectStr("A favore"), getSubjectStr("Contro"),
                        getSubjectDebitoreStr("Debitore non datore di ipoteca")) +
                getSectionANote(sectionA) + getSectionCSubjects() + part3 +
                (!getComment().equalsIgnoreCase(ResourcesHelper.getString("formalityCommentDefaultValue")) ?
                        ("<div style=\"font-style: italic; text-align: justify;\" >" + getComment() + "</div>") : "");
    }

    private String getSectionANote(SectionA sectionA) {
        if (!ValidationHelper.isNullOrEmpty(sectionA) && !ValidationHelper.isNullOrEmpty(sectionA.getDerivedFromCode()) &&
                (sectionA.getDerivedFromCode().equals("8726") || sectionA.getDerivedFromCode().equals("9726"))) {
            return String.format(ResourcesHelper.getString("estateSituationTableFormality2Note"),
                    DateTimeHelper.toStringDateWithDots(sectionA.getOtherData()), sectionA.getOtherParticularRegister());
        }
        return "";
    }

    private String getSubjectStr(String type) {
        String result = getSectionC().stream().filter(c -> c.getSectionCType().equals(type)).map(SectionC::getSubject)
                .flatMap(List::stream).map(Subject::toFormalityTableString).collect(Collectors.joining(", "));
        if (!ValidationHelper.isNullOrEmpty(result)) {
            String end = type.equals("A favore") ? "" : ".";
            return result.trim() + end;
        }
        return result;
    }

    private String getSubjectDebitoreStr(String type) {
        String result = getSectionC().stream().filter(c -> c.getSectionCType().equals(type)).map(SectionC::getSubject)
                .flatMap(List::stream).map(Subject::toFormalityTableDebitore).collect(Collectors.joining(", "));
        if (!ValidationHelper.isNullOrEmpty(result)) {
            return result.trim() + ".";
        }
        return result;
    }

    private String getSectionCSubjects() {
        if (!ValidationHelper.isNullOrEmpty(getSectionC())) {
            List<String> result = getSectionC().stream().filter(c -> "Debitori non datori di ipoteca".equals(c.getSectionCType()))
                    .map(SectionC::getSubject).flatMap(List::stream).distinct().map(Subject::getSectionCStr)
                    .collect(Collectors.toList());
            if (!ValidationHelper.isNullOrEmpty(result)) {
                if (result.size() == 1) {
                    return "<div style=\"text-align: justify;\">Debitore non datore di ipoteca: "
                            + result.stream().collect(Collectors.joining(", ")) + "</div>";
                } else {
                    return "<div style=\"text-align: justify;\">Debitori non datori di ipoteca: "
                            + result.stream().collect(Collectors.joining(", ")) + "</div>";
                }
            }
        }
        return "";
    }

    public String getDocumentTitle() {
        return getDocument() == null ? "" : getDocument().getTitle();
    }

    public String getDocumentPath() {
        return getDocument() == null ? ""
                : getDocument().getPath().replace("\\", "\\\\");
    }

    public String getStateStr(){
        if (FormalityStateType.MANUALE.getId().equals(this.getState())) {
            return "M";
        } else if (FormalityStateType.TITOLO.getId().equals(this.getState())) {
            return "T";
        } else if (!ValidationHelper.isNullOrEmpty(this.getSectionA())
                && !ValidationHelper.isNullOrEmpty(this.getSectionC())) {
            if (!ValidationHelper.isNullOrEmpty(this.getSectionB())) {
                return "S";
            }
            // return "O";
        }
        return "O";
    }

    public String getForAgainst() {
        if (!ValidationHelper.isNullOrEmpty(getSectionC())) {
            boolean findFor = false;
            boolean findAgainst = false;
            boolean findDebitori = false;
            Set<String> types = getSectionC().stream()
                    .map(SectionC::getSectionCType).collect(Collectors.toSet());

            if (types.contains("A favore")) {
                findFor = true;
            }

            if (types.contains("Contro")) {
                findAgainst = true;
            }

            if (types.contains("Debitori non datori di ipoteca")) {
                findDebitori = true;
            }

            if (findFor && findAgainst && findDebitori) {
                return "F/C/D";
            } else if (findFor && findDebitori) {
                return "F/D";
            } else if (findFor && findAgainst) {
                return "F/C";
            } else if (findAgainst && findDebitori) {
                return "C/D";
            } else if (findFor) {
                return "F";
            } else if (findAgainst) {
                return "C";
            } else if (findDebitori) {
                return "D";
            }
        }

        return "";
    }

    public Boolean getSectionTypeIsFavor() {
        return "F".equals(getForAgainst()) || "F/C".equals(getForAgainst()) || "F/D".equals(getForAgainst());
    }

    public Boolean getSectionTypeIsContro() {
        return "C".equals(getForAgainst()) || "F/C".equals(getForAgainst()) || "C/D".equals(getForAgainst());
    }

    public Boolean getSectionTypeIsDebitori() {
        return "D".equals(getForAgainst()) || "C/D".equals(getForAgainst()) || "F/D".equals(getForAgainst());
    }

    public Boolean getSectionBothType() {
        return "F/C".equals(getForAgainst());
    }

    public Boolean getSectionAllType() {
        return "F/C/D".equals(getForAgainst());
    }

    public String getSpeciesStr() {
        return ValidationHelper.isNullOrEmpty(getSectionA().getSpecies()) ? ""
                : getSectionA().getSpecies();
    }

    public String getInspectionDateStr() {
        return getInspectionDate() == null ? ""
                : DateTimeHelper.toString(getInspectionDate());
    }

    public String getProvincialOfficeName() {
        return getProvincialOffice() == null ? ""
                : getProvincialOffice().getName();
    }

    public LandChargesRegistry getProvincialOffice() {
        return provincialOffice;
    }

    public void setProvincialOffice(LandChargesRegistry provincialOffice) {
        this.provincialOffice = provincialOffice;
    }

    public LandChargesRegistry getReclamePropertyService() {
        return reclamePropertyService;
    }

    public void setReclamePropertyService(
            LandChargesRegistry reclamePropertyService) {
        this.reclamePropertyService = reclamePropertyService;
    }

    public String getGeneralRegister() {
        return generalRegister;
    }

    public void setGeneralRegister(String generalRegister) {
        this.generalRegister = generalRegister;
    }

    public String getParticularRegister() {
        return particularRegister;
    }

    public void setParticularRegister(String particularRegister) {
        this.particularRegister = particularRegister;
    }

    public Date getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(Date inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public String getNumberPresentation() {
        return numberPresentation;
    }

    public void setNumberPresentation(String numberPresentation) {
        this.numberPresentation = numberPresentation;
    }

    public Date getPresentationDateOrNewDateIfNull() {
        return presentationDate == null ? new Date(): presentationDate;
    }

    public Date getPresentationDate() {
        return presentationDate;
    }

    public void setPresentationDate(Date presentationDate) {
        this.presentationDate = presentationDate;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public SectionA getSectionA() {
        return sectionA;
    }

    public String getMortgageSpeciesDerivedFromAllFields() {
        if (!ValidationHelper.isNullOrEmpty(getSectionA())) {
            SectionA sa = getSectionA();
            return String.format("%s %s", sa.getMortgageSpecies() == null ? "" : sa.getMortgageSpecies(),
                    sa.getDerivedFrom() == null ? "" : sa.getDerivedFrom());
        }
        return "";
    }

    public String getMortgageSpeciesDerivedFrom() {
        if (!ValidationHelper.isNullOrEmpty(getSectionA())) {
            SectionA sa = getSectionA();
            String mortgageSpecies = sa.getMortgageSpecies() == null ? ""
                    : sa.getMortgageSpecies();
            String derivedFrom = sa.getDerivedFrom() == null ? ""
                    : sa.getDerivedFrom();

            return String.format("%s - %s", mortgageSpecies, derivedFrom);
        }

        return "";
    }

    public String getSectionAPublicOfficialNotary() {
        return getSectionA().getPublicOfficialNotary();
    }

    public String getSectionASeat() {
        return getSectionA().getSeat();
    }

    public String getSectionATitleDate() {
        if (!ValidationHelper.isNullOrEmpty(getSectionA())
                && !ValidationHelper.isNullOrEmpty(getSectionA().getTitleDate())) {
            return DateTimeHelper.toString(getSectionA().getTitleDate());
        } else {
            return "";
        }
    }

    public String getSectionANumberDirectory() {
        return getSectionA().getNumberDirectory();
    }

    public String getSectionATotal() {
        return getSectionA().getTotal();
    }

    public String getSectionACapital() {
        return getSectionA().getCapital();
    }

    public String getSectionADurationYear() {
        return getSectionA().getDurationYear() == null ? ""
                : getSectionA().getDurationYear().toString();
    }

    public void setSectionA(SectionA sectionA) {
        this.sectionA = sectionA;
    }

    public List<SectionB> getSectionB() {
        return sectionB;
    }

    public void setSectionB(List<SectionB> sectionB) {
        this.sectionB = sectionB;
    }

    public Set<Map.Entry<Integer, List<Property>>> getSectionBMap() throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(sectionBMap)) {
            List<String> unitsStrings = DaoManager.loadField(SectionB.class, "bargainingUnit", String.class, new Criterion[]{
                    Restrictions.eq("formality.id", getId())
            });
            List<Integer> bargainingUnits = unitsStrings.stream().map(Integer::valueOf).sorted().collect(Collectors.toList());
            if (!ValidationHelper.isNullOrEmpty(bargainingUnits)) {
                Map<Integer, List<Property>> result = new HashMap<>();
                for (Integer unit : bargainingUnits) {
                    if (!result.containsKey(unit)) {
                        List<Property> properties = DaoManager.load(Property.class, new CriteriaAlias[]{
                                new CriteriaAlias("sectionB", "sb", JoinType.INNER_JOIN)
                        }, new Criterion[]{
                                Restrictions.eq("sb.bargainingUnit", unit.toString()),
                                Restrictions.eq("sb.formality.id", getId())
                        });
                        for (Property property : properties) {
                            property.fillSelectLists();
                            for (CadastralData data : property.getCadastralData()) {
                                if (!ValidationHelper.isNullOrEmpty(property.getCadastralData())) {
                                    property.setCadastralData(property.getCadastralData().stream().distinct()
                                            .collect(Collectors.toList()));
                                }
                            }

                            List<Property> oldProperties = DaoManager.load(Property.class, new CriteriaAlias[]{
                                    new CriteriaAlias("oldProperty", "old", JoinType.INNER_JOIN)
                            }, new Criterion[]{
                                    Restrictions.isNotNull("old.oldProperty"),
                                    Restrictions.eq("old.formality.id", this.getId()),
                                    Restrictions.eq("old.property.id", property.getId())
                            });
                            if (!ValidationHelper.isNullOrEmpty(oldProperties)) {
                                property.setOldPropertiesToView(oldProperties);
                            }
                        }
                        if (!ValidationHelper.isNullOrEmpty(properties)) {
                            IntStream.range(0, properties.size()).forEach(i -> properties.get(i).setNumberInFormalityGroup(i + 1));
                            result.put(unit, properties);
                        }
                    }
                }
                sectionBMap = result.entrySet();
            }
        }
        return sectionBMap;
    }

    public String getSecrionCFavorSubjects() {
        return getSectionC().stream()
                .filter(sc -> "A favore".equalsIgnoreCase(sc.getSectionCType()))
                .map(SectionC::getAllSubjects)
                .collect(Collectors.joining(", "));
    }

    public String getSecrionCAgainstSubjects() {
        return getSectionC().stream()
                .filter(sc -> "Contro".equalsIgnoreCase(sc.getSectionCType()))
                .map(SectionC::getAllSubjects)
                .collect(Collectors.joining(", "));
    }

    public boolean isExistsByPrejudicialAndСode(boolean isPrejudicial) {
        boolean result = false;

        List<TypeFormality> typeFormalities = null;
        try {
            if(!ValidationHelper.isNullOrEmpty(this.getSectionA())) {
                typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                        isPrejudicial ?
                                Restrictions.eq("prejudicial", true) :
                                Restrictions.or(
                                        Restrictions.eq("prejudicial", false),
                                        Restrictions.isNull("prejudicial")),
                        Restrictions.eq("code", this.getSectionA().getCodeWithoutFirstZero()),
                        Restrictions.eq("type", this.getTypeEnum())});
            }
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }

        if (!ValidationHelper.isNullOrEmpty(typeFormalities)) {
            result = true;
        }

        return result;
    }

    public Set<SectionC> getSectionC() {
        return sectionC;
    }

    public void setSectionC(Set<SectionC> sectionC) {
        this.sectionC = sectionC;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public List<EstateSituation> getEstateSituationList() {
        return estateSituationList;
    }

    public void setEstateSituationList(List<EstateSituation> estateSituationList) {
        this.estateSituationList = estateSituationList;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public List<SectionD> getSectionD() {
        return sectionD;
    }

    public void setSectionD(List<SectionD> sectionD) {
        this.sectionD = sectionD;
    }

    public Subject getCurrentSubject() {
        return currentSubject;
    }

    public void setCurrentSubject(Subject currentSubject) {
        this.currentSubject = currentSubject;
    }

    public String getComment() {
        if (comment == null) {
            comment = ResourcesHelper.getString("formalityCommentDefaultValue");
        }
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Request> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<Request> requestList) {
        this.requestList = requestList;
    }

    public Long getState() {
        return state;
    }

    public void setState(Long state) {
        this.state = state;
    }

    public List<Request> getRequestForcedList() {
        return requestForcedList;
    }

    public void setRequestForcedList(List<Request> requestForcedList) {
        this.requestForcedList = requestForcedList;
    }

    public String getSectionAText() {
        return sectionAText;
    }

    public void setSectionAText(String sectionAText) {
        this.sectionAText = sectionAText;
    }

    public List<Subject> getSubjectExternalList() {
        return subjectExternalList;
    }

    public void setSubjectExternalList(List<Subject> subjectExternalList) {
        this.subjectExternalList = subjectExternalList;
    }

    public List<Request> getDistraintRequests() {
        return distraintRequests;
    }

    public void setDistraintRequests(List<Request> distraintRequests) {
        this.distraintRequests = distraintRequests;
    }

    public List<EstateSituationFormalityProperty> getEstateSituationFormalityProperties() {
        return estateSituationFormalityProperties;
    }

    public void setEstateSituationFormalityProperties(List<EstateSituationFormalityProperty> estateSituationFormalityProperties) {
        this.estateSituationFormalityProperties = estateSituationFormalityProperties;
    }

    public String getDistraintComment() {
        return distraintComment;
    }

    public void setDistraintComment(String distraintComment) {
        this.distraintComment = distraintComment;
    }

    public String getTextCertification() {
		return textCertification;
	}

	public void setTextCertification(String textCertification) {
		this.textCertification = textCertification;
	}

	public boolean isShouldReportRelationships() {
        return shouldReportRelationships;
    }

    public void setShouldReportRelationships(boolean shouldReportRelationships) {
        this.shouldReportRelationships = shouldReportRelationships;
    }
    
    public Formality cloneFormality() {
    	Formality formality = new Formality();
    	formality.setComment(getComment());
    	formality.setCurrentSubject(getCurrentSubject());
    	formality.setCustomId(true);
    	formality.setDistraintComment(getDistraintComment());
        Hibernate.isInitialized(getDistraintRequests());
    	if(!ValidationHelper.isNullOrEmpty(getDistraintRequests())){
            formality.setDistraintRequests(new ArrayList<>(getDistraintRequests()));
        }
    	formality.setDocument(getDocument());
        Hibernate.isInitialized(getEstateSituationFormalityProperties());
        if(!ValidationHelper.isNullOrEmpty(getEstateSituationFormalityProperties())){
            formality.setEstateSituationFormalityProperties(new ArrayList<>(getEstateSituationFormalityProperties()));
        }
        Hibernate.isInitialized(getEstateSituationList());
        if(!ValidationHelper.isNullOrEmpty(getEstateSituationList())){
            formality.setEstateSituationList(new ArrayList<>(getEstateSituationList()));
        }
    	formality.setGeneralRegister(getGeneralRegister());
    	formality.setInspectionDate(getInspectionDate());
    	formality.setNumberPresentation(getNumberPresentation());
    	formality.setParticularRegister(getParticularRegister());
    	formality.setPresentationDate(getPresentationDate());
    	formality.setProvincialOffice(getProvincialOffice());
    	formality.setReclamePropertyService(getReclamePropertyService());
        Hibernate.isInitialized(getRequestForcedList());
        if(!ValidationHelper.isNullOrEmpty(getRequestForcedList())){
            formality.setRequestForcedList(new ArrayList<>(getRequestForcedList()));
        }
        Hibernate.isInitialized(getRequestList());
        if(!ValidationHelper.isNullOrEmpty(getRequestList())){
            formality.setRequestList(new ArrayList<>(getRequestList()));
        }
        if(!ValidationHelper.isNullOrEmpty(getSectionA())){
            SectionA sectionA = new SectionA();
            sectionA.setTitleDescription(getSectionA().getTitleDescription());
            sectionA.setTitleDate(getSectionA().getTitleDate());
            sectionA.setPublicOfficialNotary(getSectionAPublicOfficialNotary());
            sectionA.setPublicOfficial(getSectionA().getPublicOfficial());
            sectionA.setNumberDirectory(getSectionANumberDirectory());
            sectionA.setFiscalCode(getSectionA().getFiscalCode());
            sectionA.setSeat(getSectionASeat());
            sectionA.setMortgageSpeciesOrPrivilege(getSectionA().getMortgageSpeciesOrPrivilege());
            sectionA.setDerivedFrom(getSectionA().getDerivedFrom());
            sectionA.setDerivedFromCode(getSectionA().getDerivedFromCode());
            sectionA.setCapital(getSectionACapital());
            sectionA.setAnnualInterestRate(getSectionA().getAnnualInterestRate());
            sectionA.setInterests(getSectionA().getInterests());
            sectionA.setExpense(getSectionA().getExpense());
            sectionA.setTotal(getSectionATotal());
            sectionA.setVaryingAmounts(getSectionA().getVaryingAmounts());
            sectionA.setForeignCurrency(getSectionA().getForeignCurrency());
            sectionA.setEnteredAmountAutomaticallyIncrease(getSectionA().getEnteredAmountAutomaticallyIncrease());
            sectionA.setPresenceOfConditionSubsequent(getSectionA().getPresenceOfConditionSubsequent());
            sectionA.setDurationMonth(getSectionA().getDurationMonth());
            sectionA.setDurationYear(getSectionA().getDurationYear());
            sectionA.setDuration(getSectionA().getDuration());
            sectionA.setDeadline(getSectionA().getDeadline());
            sectionA.setDeathDate(getSectionA().getDeathDate());
            sectionA.setNumberNegotiatingUnits(getSectionA().getNumberNegotiatingUnits());
            sectionA.setNumberSubjectsInFavor(getSectionA().getNumberSubjectsInFavor());
            sectionA.setNumberPersonsOrEntities(getSectionA().getNumberPersonsOrEntities());
            sectionA.setMortgageSpecies(getSectionA().getMortgageSpecies());
            sectionA.setConventionSpecies(getSectionA().getConventionSpecies());
            sectionA.setConventionDescription(getSectionA().getConventionDescription());
            sectionA.setOtherData(getSectionA().getOtherData());
            sectionA.setOtherParticularRegister(getSectionA().getOtherParticularRegister());
            sectionA.setOtherTypeFormality(getSectionA().getOtherTypeFormality());
            sectionA.setAnnotationType(getSectionA().getAnnotationType());
            sectionA.setAnnotationDescription(getSectionA().getAnnotationDescription());
            sectionA.setAnnotationProperties(getSectionA().getAnnotationProperties());
            sectionA.setApplicant(getSectionA().getApplicant());
            sectionA.setFiscalCodeAppliant(getSectionA().getFiscalCodeAppliant());
            sectionA.setAddressAppliant(getSectionA().getAddressAppliant());
            sectionA.setLandChargesRegistry(getSectionA().getLandChargesRegistry());
            formality.setSectionA(sectionA);
        }
    	formality.setSectionAText(getSectionAText());
    	formality.setShouldReportRelationships(isShouldReportRelationships());
    	formality.setSubject(getSubject());
        Hibernate.isInitialized(getSubjectExternalList());
        if(!ValidationHelper.isNullOrEmpty(getSubjectExternalList())){
            formality.setSubjectExternalList(new ArrayList<>(getSubjectExternalList()));
        }
    	formality.setTextCertification(getTextCertification());
    	formality.setType(getType());
        return formality;
    }

    public TypeFormality checkRenewalTypeFormality() {
        TypeFormality typeFormality = null;
        TypeActEnum typeActEnum = getTypeEnum();
        Integer code = ValidationHelper.isNullOrEmpty(sectionA.getDerivedFromCode()) ?
                0 : Integer.parseInt(sectionA.getDerivedFromCode());
        if (code / 1000 != 0) {
            if (code / 1000 == 9 || code / 1000 == 8) {
            }
            code = code % 1000;
        }
        try {
            List<TypeFormality> typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                    Restrictions.eq("type", typeActEnum),
                    Restrictions.eq("code", code.toString()),
                    Restrictions.isNotNull("renewal"),
                    Restrictions.eq("renewal", Boolean.TRUE)
            });
            if(!ValidationHelper.isNullOrEmpty(typeFormalities)){
                typeFormality = typeFormalities.get(0);
            }
        } catch (Exception e) {
            LogHelper.log(log,e);
        }

        return typeFormality;
    }

    public TypeFormality checkSalesDicTypeFormality() {
        TypeFormality typeFormality = null;
        TypeActEnum typeActEnum = getTypeEnum();
        Integer code = ValidationHelper.isNullOrEmpty(sectionA.getDerivedFromCode()) ?
                0 : Integer.parseInt(sectionA.getDerivedFromCode());
        if (code / 1000 != 0) {
            if (code / 1000 == 9 || code / 1000 == 8) {
            }
            code = code % 1000;
        }
        try {
            List<TypeFormality> typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                    Restrictions.eq("type", typeActEnum),
                    Restrictions.eq("code", code.toString()),
                    Restrictions.isNotNull("salesDevelopmentOMI"),
                    Restrictions.eq("salesDevelopmentOMI", Boolean.TRUE)
            });
            if(!ValidationHelper.isNullOrEmpty(typeFormalities)){
                typeFormality = typeFormalities.get(0);
            }
        } catch (Exception e) {
            LogHelper.log(log,e);
        }

        return typeFormality;
    }

    public String getDicTypeFormalityText() {
        TypeFormality typeFormality = null;
        String textInVisura = "";
        TypeActEnum typeActEnum = getTypeEnum();
        Integer code = ValidationHelper.isNullOrEmpty(sectionA.getDerivedFromCode()) ?
                0 : Integer.parseInt(sectionA.getDerivedFromCode());
        if (code / 1000 != 0) {
            if (code / 1000 == 9 || code / 1000 == 8) {
            }
            code = code % 1000;
        }
        try {
            List<TypeFormality> typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                    Restrictions.eq("type", typeActEnum),
                    Restrictions.eq("code", code.toString()),
                    Restrictions.isNotNull("salesDevelopmentOMI"),
                    Restrictions.eq("salesDevelopmentOMI", Boolean.TRUE)
            });
            if(!ValidationHelper.isNullOrEmpty(typeFormalities)){
                typeFormality = typeFormalities.get(0);
            }
        } catch (Exception e) {
            LogHelper.log(log,e);
        }

        if(typeFormality != null){
            if (!ValidationHelper.isNullOrEmpty(getSectionA())
                    && !ValidationHelper.isNullOrEmpty(getSectionA().getDerivedFromCode())) {

                if(getSectionA().getDerivedFromCode().trim().endsWith("00")) {
                    StringBuilder result = new StringBuilder();
                    switch (getTypeEnum()) {
                        case TYPE_I:
                            String mortgageSpecies = getSectionA().getMortgageSpecies();
                            if(StringUtils.isNotBlank(mortgageSpecies) && Character.isDigit(mortgageSpecies.charAt(0))) {
                                String startDigits = mortgageSpecies.split("\\s+")[0];
                                mortgageSpecies = mortgageSpecies.replaceFirst(startDigits, "").trim();
                            }
                            String derivedFrom =  getSectionA().getDerivedFrom();
                            if(StringUtils.isNotBlank(derivedFrom) && Character.isDigit(derivedFrom.charAt(0))) {
                                String startDigits = derivedFrom.split("\\s+")[0];
                                derivedFrom = derivedFrom.replaceFirst(startDigits, "").trim();
                            }

                            if(StringUtils.isNotBlank(mortgageSpecies)) {
                                result.append(mortgageSpecies);
                            }
                            if(StringUtils.isNotBlank(mortgageSpecies) && StringUtils.isNotBlank(derivedFrom)) {
                                result.append("-");
                            }
                            if(StringUtils.isNotBlank(derivedFrom)) {
                                result.append(derivedFrom);
                            }
                            textInVisura = result.toString();
                            break;
                        case TYPE_T:

                            String conventionSpecies = getSectionA().getConventionSpecies();
                            if(StringUtils.isNotBlank(conventionSpecies) && Character.isDigit(conventionSpecies.charAt(0))) {
                                String startDigits = conventionSpecies.split("\\s+")[0];
                                conventionSpecies = conventionSpecies.replaceFirst(startDigits, "").trim();
                            }

                            String conventionDescription =  getSectionA().getConventionDescription();
                            if(StringUtils.isNotBlank(conventionDescription) && Character.isDigit(conventionDescription.charAt(0))) {
                                String startDigits = conventionDescription.split("\\s+")[0];
                                conventionDescription = conventionDescription.replaceFirst(startDigits, "").trim();
                            }

                            if(StringUtils.isNotBlank(conventionSpecies)) {
                                result.append(conventionSpecies);
                            }
                            if(StringUtils.isNotBlank(conventionSpecies) && StringUtils.isNotBlank(conventionDescription)) {
                                result.append("-");
                            }
                            if(StringUtils.isNotBlank(conventionDescription)) {
                                result.append(conventionDescription);
                            }
                            textInVisura = result.toString();

                            break;
                        case TYPE_A:
                            String annotationType  = getSectionA().getAnnotationType();

                            String annotationDescription  =  getSectionA().getAnnotationDescription();
                            if(StringUtils.isNotBlank(annotationDescription) && Character.isDigit(annotationDescription.charAt(0))) {
                                String startDigits = annotationDescription.split("\\s+")[0];
                                annotationDescription = annotationDescription.replaceFirst(startDigits, "").trim();
                            }
                            if(StringUtils.isNotBlank(annotationType)) {
                                result.append(annotationType);
                            }
                            if(StringUtils.isNotBlank(annotationType) && StringUtils.isNotBlank(annotationDescription)) {
                                result.append("-");
                            }
                            if(StringUtils.isNotBlank(annotationDescription)) {
                                result.append(annotationDescription);
                            }
                            textInVisura =  result.toString();
                            break;
                    }
                }
            }
            if(textInVisura == null || textInVisura.trim().isEmpty())
                textInVisura = typeFormality.getTextInVisura();
        }
        return textInVisura;
    }

    public Date getComparedDate() {
        if(ValidationHelper.isNullOrEmpty(checkRenewalTypeFormality())){
            if (!ValidationHelper.isNullOrEmpty(getSectionA()) &&
                    !ValidationHelper.isNullOrEmpty(getSectionA().getOtherData()))
                return getSectionA().getOtherData();
        }
        return getPresentationDateOrNewDateIfNull();
    }

    public Date getComparedDeathDate() {
        if(!ValidationHelper.isNullOrEmpty(getSectionA())  &&
                !ValidationHelper.isNullOrEmpty(getSectionA().getDeathDate()))
            return  getSectionA().getDeathDate();
        return new Date();
    }

    public TypeFormality getDicTypeFormality() {
        TypeFormality typeFormality = null;
        TypeActEnum typeActEnum = getTypeEnum();
        if(!ValidationHelper.isNullOrEmpty(sectionA)){
            Integer code = ValidationHelper.isNullOrEmpty(sectionA.getDerivedFromCode()) ?
                    0 : Integer.parseInt(sectionA.getDerivedFromCode());
            if (code / 1000 != 0) {
                if (code / 1000 == 9 || code / 1000 == 8) {
                }
                code = code % 1000;
            }
            try {
                List<TypeFormality> typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                        Restrictions.eq("type", typeActEnum),
                        Restrictions.eq("code", code.toString())
                });
                if(!ValidationHelper.isNullOrEmpty(typeFormalities)){
                    typeFormality = typeFormalities.get(0);
                }
            } catch (Exception e) {
                LogHelper.log(log,e);
            }
        }
        return typeFormality;
    }
}
