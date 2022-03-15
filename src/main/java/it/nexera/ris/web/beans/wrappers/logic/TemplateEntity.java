package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.DocumentGenerationTags;
import it.nexera.ris.common.enums.RealEstateType;
import it.nexera.ris.common.enums.SectionCType;
import it.nexera.ris.common.exceptions.CannotProcessException;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.tableGenerator.*;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Regime;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.pages.RequestTextEditBean;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.PartedPairsByCityWrapper;
import static it.nexera.ris.common.helpers.TemplatePdfTableHelper.distinctByKey;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

public class TemplateEntity {


    class TagTableWrapper {
        int allRowNum;
        int counter;
        String estateFormalityDef;
        String cityDesc;
        int cityRowNumber;
        List<String> descriptionRows;
        List<Pair<String, String>> pairs;
    }

    public transient final static Log log = LogFactory.getLog(TemplateEntity.class);

    private Request request;

    private UserWrapper currentUser;

    public TemplateEntity(Request request, UserWrapper currentUser) {
        this.request = request;
        this.currentUser = currentUser;
    }

    public String invokeGetMethod(DocumentGenerationTags dgTag, String result) throws TypeFormalityNotConfigureException,
            CannotProcessException, PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (getRequest() == null) {
            return null;
        }
        TagTableGenerator generator;
        switch (dgTag) {
            case GENERAL_CURRENT_DATE:
                return DateTimeHelper.getCurrentDate();
            case GENERAL_CURRENT_USER:
                return currentUser.getFullname();
            case REQUEST_CONSERVATORY:
                if (getRequest().getAggregationLandChargesRegistry() != null) {
                    AggregationLandChargesRegistry aggregation = DaoManager.get(AggregationLandChargesRegistry.class,
                            getRequest().getAggregationLandChargesRegistry().getId());
                    if (aggregation.getName().equals("CASERTA - SMCV")) {
                        return aggregation.getName();
                    }
                    return aggregation.getLandChargesRegistries().stream().filter(x -> (!ValidationHelper.
                            isNullOrEmpty(x.getVisualize()) && x.getVisualize())).map(LandChargesRegistry::toString)
                            .collect(Collectors.joining(" - "));
                } else {
                    return "";
                }
            case CONSERVATION_DATE:
                return TemplatePdfTableHelper.getEstateFormalityConservationDate(getRequest());
            case SUBJECT_REGISTRY:
                return TemplatePdfTableHelper.getSubjectRegistry(getRequest());
            case INIT_TEXT_REGISTRY_OR_TABLE:
                return TemplatePdfTableHelper.getInitTextRegistryOrTable(getRequest());
            case CLIENT_NAME:
                if (!ValidationHelper.isNullOrEmpty(getRequest().getClient())) {
                    return getRequest().getClient().getNameOfTheCompany();
                } else {
                    return "";
                }
            case CLIENT_FISCAL_CODE:
                if (!ValidationHelper.isNullOrEmpty(getRequest().getClient())) {
                    return getRequest().getClient().getFiscalCode();
                } else {
                    return "";
                }
            case CLIENT_ADDRESS_NUMBER:
                if (!ValidationHelper.isNullOrEmpty(getRequest().getClient())) {
                    return getRequest().getClient().getAddressHouseNumber();
                } else {
                    return "";
                }
            case CLIENT_ADDRESS_CITY:
                if (!ValidationHelper.isNullOrEmpty(getRequest().getClient())) {
                    return getRequest().getClient().getAddressCityId().getDescription();
                } else {
                    return "";
                }
            case CLIENT_ADDRESS_PROVINCE:
                if (!ValidationHelper.isNullOrEmpty(getRequest().getClient())) {
                    return getRequest().getClient().getAddressProvinceId().getDescription();
                } else {
                    return "";
                }
            case CLIENT_PHONE:
                if (!ValidationHelper.isNullOrEmpty(getRequest().getClient())) {
                    return getRequest().getClient().getPhone();
                } else {
                    return "";
                }
            case CLIENT_CELL:
                if (!ValidationHelper.isNullOrEmpty(getRequest().getClient())) {
                    return getRequest().getClient().getCell();
                } else {
                    return "";
                }
            case CLIENT_MAIL:
                if (!ValidationHelper.isNullOrEmpty(getRequest().getClient())) {
                    return getRequest().getClient().getMailPEC();
                } else {
                    return "";
                }
            case CLIENT_ADDRESS_INFO:
                if (!ValidationHelper.isNullOrEmpty(getRequest().getClient())) {
                    return getRequest().getClient().getAddressStreet();
                } else {
                    return "";
                }
            case CLIENT_NAME_PROFESSIONAL:
                if (!ValidationHelper.isNullOrEmpty(getRequest().getClient())) {
                    return getRequest().getClient().getNameProfessional();
                } else {
                    return "";
                }
            case AGGREGATE_CONSERVATORY_NAME:
                if (!ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry())) {
                    return getRequest().getAggregationLandChargesRegistry().getName();
                } else {
                    return "";
                }
            case SERVICE_REQUEST:
                if (!ValidationHelper.isNullOrEmpty(getRequest().getService())) {
                    return getRequest().getService().getName();
                } else {
                    return "";
                }
            case EMERGENCY_REQUEST:
                return getRequest().getUrgentStr();
            case REQUESTED_DATE:
                return getRequest().getCreateDateStr();
            case MOVEMENTS_AUTHORIZED_REQUEST:
                return getRequest().getFormalityAuthorizedStr();
            case NOTES_REQUEST:
                return getRequest().getNote();
            case SUBJECT_S_FORMALITIES:
                return TemplatePdfTableHelper.makeSubjectFormalityTable(getRequest().getSubject());
            case SUBJECT_S_CADASTRAL:
                return TemplatePdfTableHelper.makeSubjectCadastralTable(getRequest().getSubject());
            case REAL_ESTATE_RELATIONSHIP_TABLE:
                generator = new RealEstateRelationshipTableGenerator(getRequest());
                return generator.compileTable();
            case ALIENATED_TABLE:
                generator = new AlienatedTableGenerator(getRequest());
                return generator.compileTable();
            case CERTIFICAZIONE_TABLE:
                generator = new CertificazioneTableGenerator(getRequest(), true, result);
                return generator.compileTable();
            case DECEASED_TABLE:
                generator = new DeceasedTableGenerator(getRequest());
                return generator.compileTable();
            case NEGATIVE_TABLE:
                generator = new NegativeTableGenerator(getRequest());
                return generator.compileTable();
            case NO_ASSETS_TABLE:
                generator = new NoAssetsTableGenerator(getRequest());
                return generator.compileTable();
            case SUBJECT_LAND_REGISTRATION:
                return TemplatePdfTableHelper.makeSubjectLandRegisterTable(getRequest().getSubject());
            case REAL_ESTATE_REPORT_NOTE:
                return TemplatePdfTableHelper.makeRealEstateReportNote();
            case RICHIEDENTE_RICHIESTA:
                if (getRequest().getCreateUserId() != null) {
                    User user = DaoManager.get(User.class, getRequest().getCreateUserId());
                    if (!ValidationHelper.isNullOrEmpty(user)) {
                        if (!ValidationHelper.isNullOrEmpty(user.getFullname())) {
                            return user.getFullname();
                        } else if (!ValidationHelper.isNullOrEmpty(user.getBusinessName())) {
                            return user.getBusinessName();
                        }
                    }
                    return "";
                } else {
                    return "";
                }

            case FILIALE_RICHIEDENTE_RICHIESTA:
                if (getRequest().getCreateUserId() != null) {
                    User user = DaoManager.get(User.class, new CriteriaAlias[]{
                            new CriteriaAlias("office", "o", JoinType.INNER_JOIN)
                    }, new Criterion[]{
                            Restrictions.eq("id", getRequest().getCreateUserId())
                    });

                    if (!ValidationHelper.isNullOrEmpty(user)
                            && !ValidationHelper.isNullOrEmpty(user.getOffice())) {
                        return user.getOffice().getDescription();
                    }
                    return "";
                } else {
                    return "";
                }

            case NUMERO_ATTI:
                List<RequestFormality> requestFormalities = DaoManager.load(RequestFormality.class, new Criterion[]{
                        Restrictions.eq("request.id", this.getRequest().getId())
                });

                List<EstateFormality> estateFormalityList = emptyIfNull(requestFormalities).stream().map(RequestFormality::getFormality)
                        .collect(Collectors.toList());
                return String.valueOf(estateFormalityList.size());

            case COSTO_RICHIESTA:
                return request.getTotalCost();

            case TRASCRIZIONE_REPERTORIO:
                StringBuffer sb = new StringBuffer();
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getNumberDirectory())) {
                    sb.append("&lt;PrimoNumeroRepertorio&gt;");
                    String numberDirectory = request.getTranscriptionActId().getSectionA().getNumberDirectory();
                    String[] tokens = numberDirectory.trim().split("\\/");
                    sb.append(tokens[0].trim());
                    sb.append("&lt;&#47;PrimoNumeroRepertorio&gt;");
                    if (tokens.length > 1) {
                        sb.append("\n");
                        sb.append("&lt;SecondoNumeroRepertorio&gt;");
                        sb.append(tokens[1].trim());
                        sb.append("&lt;&#47;SecondoNumeroRepertorio&gt;");
                    }
                }
                return sb.toString();
            case TRASCRIZIONE_CODICEFISCALEPU:
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getFiscalCode())) {

                    return request.getTranscriptionActId().getSectionA().getFiscalCode();
                } else
                    return "";

            case TRASCRIZIONE_CF_RICHIEDENTE:
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getFiscalCodeAppliant())) {

                    return request.getTranscriptionActId().getSectionA().getFiscalCodeAppliant();
                } else
                    return "";

            case TRASCRIZIONE_NOME_RICHIEDENTE:
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getApplicant())) {

                    return request.getTranscriptionActId().getSectionA().getApplicant();
                } else
                    return "";

            case TRASCRIZIONE_INDIRIZZO_RICHIEDENTE:
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getAddressAppliant())) {

                    return request.getTranscriptionActId().getSectionA().getAddressAppliant();
                } else
                    return "";
            case TRASCRIZIONE_DESCRIZIONE_TITOLO:
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getTitleDescription())) {

                    return request.getTranscriptionActId().getSectionA().getTitleDescription();
                } else
                    return "";

            case TRASCRIZIONE_DATA_TITOLO:
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getTitleDate())) {

                    return DateTimeHelper.toFormatedString(request.getTranscriptionActId().getSectionA().getTitleDate(), DateTimeHelper.getXmlDatePattert());
                } else
                    return "";

            case TRASCRIZIONE_PUBBLICO_UFFICIALE:
                StringBuffer tag = new StringBuffer();
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())) {
                    String codiceComune = "";
                    String denominazionePU = "";
                    String tipoPU = "";
                    if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getSeat())) {

                        String seat = request.getTranscriptionActId().getSectionA().getSeat().split("\\(")[0].trim();

                        List<City> cities = DaoManager.load(City.class,
                                new Criterion[]{
                                        Restrictions.eq("description", seat),
                                        Restrictions.isNotNull("province.id"),
                                        Restrictions.eq("external", Boolean.TRUE)
                                });
                        if (!ValidationHelper.isNullOrEmpty(cities)) {
                            codiceComune = cities.get(0).getCfis();
                        }
                    }

                    if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getPublicOfficialNotary())) {
                        denominazionePU = request.getTranscriptionActId().getSectionA().getPublicOfficialNotary();
                        tipoPU = "1";
                    } else if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getPublicOfficial())) {
                        denominazionePU = request.getTranscriptionActId().getSectionA().getPublicOfficial();
                        tipoPU = "2";
                    }
                    if (!ValidationHelper.isNullOrEmpty(codiceComune) ||
                            !ValidationHelper.isNullOrEmpty(denominazionePU) ||
                            !ValidationHelper.isNullOrEmpty(tipoPU))
                        tag.append("&lt;PubblicoUfficiale");
                    if (!ValidationHelper.isNullOrEmpty(codiceComune)) {
                        tag.append(" CodiceComune&#61;\"");
                        tag.append(codiceComune);
                        tag.append("\"");
                    }
                    if (!ValidationHelper.isNullOrEmpty(denominazionePU)) {
                        tag.append(" DenominazionePU&#61;\"");
                        tag.append(denominazionePU);
                        tag.append("\"");
                    }

                    if (!ValidationHelper.isNullOrEmpty(tipoPU)) {
                        tag.append(" TipoPU&#61;\"");
                        tag.append(tipoPU);
                        tag.append("\"");
                    }
                    tag.append("&#47;&gt;");
                }

                return tag.toString();

            case TRASCRIZIONE_ATTO_CODICE_ATTO:
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getDerivedFromCode())) {

                    return request.getTranscriptionActId().getSectionA().getDerivedFromCode();
                } else
                    return "";

            case TRASCRIZIONE_ATTO_DESCRIZIONE_ATTO:
                String trascrizioneAtto = "";

                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())) {

                    if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getConventionDescription())) {
                        trascrizioneAtto = request.getTranscriptionActId().getSectionA().getConventionDescription();
                    } else if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getAnnotationDescription())) {
                        trascrizioneAtto = request.getTranscriptionActId().getSectionA().getAnnotationDescription();
                    } else if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getDerivedFrom())) {
                        trascrizioneAtto = request.getTranscriptionActId().getSectionA().getDerivedFrom();
                    }
                    trascrizioneAtto = trascrizioneAtto.trim();
                    if (StringUtils.isNotBlank(trascrizioneAtto) && Character.isDigit(trascrizioneAtto.charAt(0))) {
                        String startDigits = trascrizioneAtto.split("\\s+")[0];
                        trascrizioneAtto = trascrizioneAtto.replaceFirst(startDigits, "").trim();
                    }
                }
                return trascrizioneAtto;

            case TRASCRIZIONE_SPECIE_ATTO:
                String trascrizioneSpecieAtto = "";
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())) {

                    if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getConventionSpecies())) {
                        trascrizioneSpecieAtto = request.getTranscriptionActId().getSectionA().getConventionSpecies();
                    } else if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getMortgageSpecies())) {
                        trascrizioneSpecieAtto = request.getTranscriptionActId().getSectionA().getMortgageSpecies();
                    } else if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getAnnotationType())) {
                        trascrizioneSpecieAtto = request.getTranscriptionActId().getSectionA().getAnnotationType();
                    }
                    trascrizioneSpecieAtto = trascrizioneSpecieAtto.trim();
                }

                return trascrizioneSpecieAtto;

            case TRASCRIZIONE_TIPO_NOTA:
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getType())) {
                    if (request.getTranscriptionActId().getType().equalsIgnoreCase("trascrizione")) {
                        return "T";
                    } else if (request.getTranscriptionActId().getType().equalsIgnoreCase("iscrizione")) {
                        return "I";
                    } else if (request.getTranscriptionActId().getType().equalsIgnoreCase("annotazione") ||
                            request.getTranscriptionActId().getType().equalsIgnoreCase("annotamento")) {
                        return "A";
                    } else
                        return "";
                } else
                    return "";
            case TRASCRIZIONE_CODICECONSERVATORIA:
                String code = "";
                try {

                    String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.CLIENT_CONSERVATIVE_CODE_FILES).getValue();
                    if (!ValidationHelper.isNullOrEmpty(path)) {
                        if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                                && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getReclamePropertyService())
                                && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getReclamePropertyService().getName())) {

                            Path xmlFile = Paths.get(path);
                            if (Files.exists(xmlFile)) {
                                Document doc = ImportXMLHelper.prepareDocument(xmlFile.toFile());
                                if (doc != null) {
                                    NodeList rows = doc.getElementsByTagName("Row");
                                    for (int r = 0; r < rows.getLength(); r++) {
                                        Node nNode = rows.item(r);
                                        org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
                                        if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem("Des"))) {
                                            String attributeValue = namedNodeMap.getNamedItem("Des").getNodeValue();
                                            if (attributeValue.trim().equalsIgnoreCase(request.getTranscriptionActId().getReclamePropertyService().getName())) {
                                                code = namedNodeMap.getNamedItem("Cod").getNodeValue();
                                            } else if (attributeValue.replaceAll("\\s+", "").trim().equalsIgnoreCase(request.getTranscriptionActId().getReclamePropertyService().getName())) {
                                                code = namedNodeMap.getNamedItem("Cod").getNodeValue();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                return code;
            case TRASCRIZIONE_UNITA_NEGOZIALI:
                String negotiationUnits = "";

                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())) {
                    List<SectionB> sectionBList = DaoManager.load(SectionB.class,
                            new Criterion[]{Restrictions.eq("formality", request.getTranscriptionActId())});

                    List<SectionB> distinctSections =
                            sectionBList.stream().
                                    filter(sectionB -> Objects.nonNull(
                                            sectionB.getBargainingUnit()))
                                    .filter(ListHelper.distinctByKey(sectionB -> sectionB.getBargainingUnit()))
                                    .collect(Collectors.toList());

                    StringBuffer sectionbuffer = new StringBuffer();
                    int unitCount = 1;
                    for (SectionB sectionB : distinctSections) {
                        List<Property> properties = sectionB.getProperties();
                        if (properties == null)
                            properties = new ArrayList<Property>();

                        sectionbuffer.append("&lt;UnitaNegoziali ");
                        sectionbuffer.append("IdImmobile=\"");
                        for (int p = 1; p <= properties.size(); p++) {

                            String str = String.format("%06d", unitCount++);
                            sectionbuffer.append("I" + str);
                            if (p < properties.size()) {
                                sectionbuffer.append(" ");
                            }
                        }
                        sectionbuffer.append("\" ");
                        sectionbuffer.append("IdUnitaNegoziale=\"U00000" + sectionB.getBargainingUnit() + "\"&#47;&gt;");
                        sectionbuffer.append("<br/>");
                    }
                    negotiationUnits = sectionbuffer.toString();
                }
                return negotiationUnits;

            case TRASCRIZIONE_DATI_ATTO:
                StringBuffer da = new StringBuffer();
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getType())) {

                    if (request.getTranscriptionActId().getType().equalsIgnoreCase("trascrizione")) {

                        if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())) {
                            if (ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherTypeFormality())) {
                                da.append("&lt;DatiTrascrizione Condizione=\"0\" ConvenzioneSoggettaVoltura=\"0\"");
                                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getDeathDate())) {
                                    da.append(" DataMorte=\"");
                                    da.append(DateTimeHelper.toFormatedString(request.getTranscriptionActId().getSectionA().getDeathDate(), DateTimeHelper.getXmlDatePattert()));
                                    da.append("\"");
                                }
                                da.append(" PLquadroA=\"0\" PLquadroB=\"0\" PLquadroC=\"0\" RinunciaTestamentaria=\"0\" SuccessioneTestamentaria=\"0\" TerminiEfficaciaAtto=\"0\" VolturaDifferitaCatastale=\"0\"&#47;&gt;");
                            } else {
                                da.append("&lt;DatiTrascrizione Condizione=\"0\" ConvenzioneSoggettaVoltura=\"0\" PLquadroA=\"1\" PLquadroB=\"1\" PLquadroC=\"1\" RinunciaTestamentaria=\"0\" SuccessioneTestamentaria=\"0\" TerminiEfficaciaAtto=\"0\" VolturaDifferitaCatastale=\"0\"&gt;");
                                da.append("<br/>");
                                da.append("&lt;FormalitaRiferimento");
                                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherData())) {
                                    da.append(" Data=\"");
                                    da.append(DateTimeHelper.toFormatedString(
                                            request.getTranscriptionActId().getSectionA().getOtherData(),
                                            DateTimeHelper.getXmlDatePattert(), null));
                                    da.append("\"");
                                }

                                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherParticularRegister())) {

                                    da.append(" RegistroParticolareUno=\"");
                                    da.append(request.getTranscriptionActId().getSectionA().getOtherParticularRegister());
                                    da.append("\"");
                                }

                                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherTypeFormality())) {
                                    if (request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("trascrizione")) {
                                        da.append(" TipoNota=\"T\"");
                                    } else if (request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("iscrizione")) {
                                        da.append(" TipoNota=\"I\"");
                                    } else {
                                        da.append(" TipoNota=\"A\"");
                                    }
                                    da.append("&gt;");
                                }

                                code = "";
                                try {

                                    String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.CLIENT_CONSERVATIVE_CODE_FILES).getValue();
                                    if (!ValidationHelper.isNullOrEmpty(path)) {
                                        if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                                                && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                                                && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getLandChargesRegistry())) {

                                            Path xmlFile = Paths.get(path);
                                            if (Files.exists(xmlFile)) {
                                                Document doc = ImportXMLHelper.prepareDocument(xmlFile.toFile());
                                                if (doc != null) {
                                                    NodeList rows = doc.getElementsByTagName("Row");
                                                    for (int r = 0; r < rows.getLength(); r++) {
                                                        Node nNode = rows.item(r);
                                                        org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
                                                        if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem("Des"))) {
                                                            String attributeValue = namedNodeMap.getNamedItem("Des").getNodeValue();
                                                            if (attributeValue.trim().equalsIgnoreCase(request.getTranscriptionActId().getSectionA().getLandChargesRegistry().getName())) {
                                                                code = namedNodeMap.getNamedItem("Cod").getNodeValue();
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    LogHelper.log(log, e);
                                }
                                if (!ValidationHelper.isNullOrEmpty(code)) {
                                    da.append("<br/>");
                                    da.append("&lt;CodiceConservatoria&gt;");
                                    da.append(code);
                                    da.append("&lt;&#47;CodiceConservatoria&gt;");
                                }
                                da.append("&lt;&#47;FormalitaRiferimento&gt;");

                                da.append("&lt;&#47;DatiTrascrizione&gt;");
                            }
                        }
                    } else if (request.getTranscriptionActId().getType().equalsIgnoreCase("iscrizione")) {

                        da.append("&lt;DatiIscrizione ");

                        if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) &&
                                !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getCapital())) {
                            da.append("Capitale=\"");
                            da.append(request.getTranscriptionActId().getSectionA().getCapital());
                            da.append("\"");
                        }

                        da.append(" CondizioneRisolutiva=\"0\" ElencoMacchinari=\"0\" ImportiTassiVariabili=\"0\" ImportiValutaEstera=\"0\"");

                        if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) &&
                                !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getInterests())) {
                            da.append(" ImportoInteressi=\"");
                            da.append(request.getTranscriptionActId().getSectionA().getInterests());
                            da.append("\"");
                        }
                        da.append(" PLquadroA=\"1\" PLquadroB=\"1\" PLquadroC=\"1\" SommaIscritta=\"0\"");
                        if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) &&
                                !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getExpense())) {
                            da.append(" SpeseInteressiMora=\"");
                            String expense = request.getTranscriptionActId().getSectionA().getExpense();
                            boolean isCommaorDot = false;
                            if (expense.contains(".")) {
                                isCommaorDot = true;
                                expense = expense.replaceAll("\\.", "");
                            } else if (expense.contains(",")) {
                                isCommaorDot = true;
                                expense = expense.replaceAll("\\,", "");
                            }
                            if (!isCommaorDot) {
                                expense = expense + "00";
                            }
                            da.append(expense);
                            da.append("\"");
                        }
                        da.append(" StipulaUnicoContratto=\"0\"");
                        if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) &&
                                !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getTotal())) {
                            da.append(" Totale=\"");
                            da.append(request.getTranscriptionActId().getSectionA().getTotal());
                            da.append("\"");
                        }

                        da.append("&gt;");
                        if (!ValidationHelper.isNullOrEmpty(
                                request.getTranscriptionActId().getSectionA().getOtherTypeFormality())) {
                            da.append("<br/>");
                            da.append("&lt;FormalitaAnnotata");
                            if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherData())) {
                                da.append(" Data=\"");
                                da.append(DateTimeHelper.toFormatedString(
                                        request.getTranscriptionActId().getSectionA().getOtherData(),
                                        DateTimeHelper.getXmlDatePattert(), null));
                                da.append("\"");
                            }
                            if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherParticularRegister())) {
                                da.append(" RegistroParticolareUno=\"");
                                da.append(request.getTranscriptionActId().getSectionA().getOtherParticularRegister());
                                da.append("\"");
                            }

                            if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherTypeFormality())) {
                                if (request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("trascrizione")) {
                                    da.append(" TipoNota=\"T\"");
                                } else if (request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("iscrizione")) {
                                    da.append(" TipoNota=\"I\"");
                                } else {
                                    da.append(" TipoNota=\"A\"");
                                }
                            }
                            da.append("&gt;");
                            code = "";
                            try {

                                String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.CLIENT_CONSERVATIVE_CODE_FILES).getValue();
                                if (!ValidationHelper.isNullOrEmpty(path)) {
                                    if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                                            && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                                            && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getLandChargesRegistry())) {

                                        Path xmlFile = Paths.get(path);
                                        if (Files.exists(xmlFile)) {
                                            Document doc = ImportXMLHelper.prepareDocument(xmlFile.toFile());
                                            if (doc != null) {
                                                NodeList rows = doc.getElementsByTagName("Row");
                                                for (int r = 0; r < rows.getLength(); r++) {
                                                    Node nNode = rows.item(r);
                                                    org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
                                                    if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem("Des"))) {
                                                        String attributeValue = namedNodeMap.getNamedItem("Des").getNodeValue();
                                                        if (attributeValue.trim().equalsIgnoreCase(request.getTranscriptionActId().getSectionA().getLandChargesRegistry().getName())) {
                                                            code = namedNodeMap.getNamedItem("Cod").getNodeValue();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                LogHelper.log(log, e);
                            }
                            if (!ValidationHelper.isNullOrEmpty(code)) {
                                da.append("<br/>");
                                da.append("&lt;CodiceConservatoria&gt;");
                                da.append(code);
                                da.append("&lt;&#47;CodiceConservatoria&gt;");
                            }
                            da.append("&lt;&#47;FormalitaAnnotata&gt;");
                        }
                        da.append("&lt;&#47;DatiIscrizione&gt;");

                    } else if (request.getTranscriptionActId().getType().equalsIgnoreCase("annotamento") ||
                            request.getTranscriptionActId().getType().equalsIgnoreCase("annotazione")) {
                        da.append("&lt;DatiAnnotazione VolturaCatastaleAttoOrig=\"0\" &gt;");
                        da.append("<br/>");
                        if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) &&
                                !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherTypeFormality())) {
                            da.append("&lt;FormalitaAnnotata TipoNotadaAnnotare=\"");
                            if (request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("trascrizione")) {
                                da.append("T\"");
                            } else if (request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("iscrizione")) {
                                da.append("I\"");
                            } else if (request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("annotamento") ||
                                    request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("annotazione")) {
                                da.append("A\"");
                            }
                            da.append("&gt;");
                        }

                        if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherData())) {
                            da.append("&lt;FormalitaRiferimento Data=\"");
                            da.append(DateTimeHelper.toFormatedString(
                                    request.getTranscriptionActId().getSectionA().getOtherData(),
                                    DateTimeHelper.getXmlDatePattert(), null));
                            da.append("\"");
                        }
                        if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherParticularRegister())) {
                            da.append(" RegistroParticolareUno=\"");
                            da.append(request.getTranscriptionActId().getSectionA().getOtherParticularRegister());
                            da.append("\"");
                        }

                        if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherTypeFormality())) {
                            if (request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("trascrizione")) {
                                da.append(" TipoNota=\"T\"");
                            } else if (request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("iscrizione")) {
                                da.append(" TipoNota=\"I\"");
                            } else {
                                da.append(" TipoNota=\"A\"");
                            }
                            da.append("&gt;");
                        }


                        code = "";
                        try {

                            String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.CLIENT_CONSERVATIVE_CODE_FILES).getValue();
                            if (!ValidationHelper.isNullOrEmpty(path)) {
                                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())
                                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getLandChargesRegistry())) {

                                    Path xmlFile = Paths.get(path);
                                    if (Files.exists(xmlFile)) {
                                        Document doc = ImportXMLHelper.prepareDocument(xmlFile.toFile());
                                        if (doc != null) {
                                            NodeList rows = doc.getElementsByTagName("Row");
                                            for (int r = 0; r < rows.getLength(); r++) {
                                                Node nNode = rows.item(r);
                                                org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
                                                if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem("Des"))) {
                                                    String attributeValue = namedNodeMap.getNamedItem("Des").getNodeValue();
                                                    if (attributeValue.trim().equalsIgnoreCase(request.getTranscriptionActId().getSectionA().getLandChargesRegistry().getName())) {
                                                        code = namedNodeMap.getNamedItem("Cod").getNodeValue();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LogHelper.log(log, e);
                        }
                        if (!ValidationHelper.isNullOrEmpty(code)) {
                            da.append("<br/>");
                            da.append("&lt;CodiceConservatoria&gt;");
                            da.append(code);
                            da.append("&lt;&#47;CodiceConservatoria&gt;");
                        }
                        da.append("&lt;&#47;FormalitaRiferimento&gt;");
                        da.append("&lt;&#47;FormalitaAnnotata&gt;");
                        da.append("&lt;&#47;DatiAnnotazione&gt;");
                    }
                }
                return da.toString();


            case TRASCRIZIONE_DATI_ASSOCIAZIONE:
                da = new StringBuffer();
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())) {
                    List<Relationship> relationships = DaoManager.load(Relationship.class, new Criterion[]{
                            Restrictions.eq("formality", request.getTranscriptionActId())
                    });

                    List<Subject> distinctSubjects = null;

                    if (!ValidationHelper.isNullOrEmpty(relationships)) {
                        distinctSubjects = relationships.stream().
                                filter(r -> Objects.nonNull(
                                        r.getSubject()))
                                .filter(ListHelper.distinctByKey(r -> r.getSubject()))
                                .map(Relationship::getSubject)
                                .collect(Collectors.toList());
                        int x = 0;
                        for (Subject subject : distinctSubjects) {
                            if (da.length() > 0)
                                da.append("<br/>");
                            relationships = DaoManager.load(Relationship.class, new Criterion[]{
                                    Restrictions.eq("subject", subject),
                                    Restrictions.eq("formality", request.getTranscriptionActId())
                            });

                            List<Relationship> distinctRelationships =
                                    relationships.stream().
                                            filter(r -> Objects.nonNull(r))
                                            .filter(ListHelper.distinctByKey(r -> r.getId()))
                                            .collect(Collectors.toList());
                            List<Relationship> groupedResult = getGroupedRelationships(distinctRelationships);
                            int gindex = 0;
                            Set<Subject> subjects = new HashSet<Subject>();
                            for (Relationship gresult : groupedResult) {
                                if (gindex > 0)
                                    da.append("<br/>");

                                if (!subjects.contains(gresult.getSubject())) {
                                    x++;
                                    subjects.add(gresult.getSubject());
                                }
                                da.append("&lt;DatiAssociazione IdSoggetto=\"");
                                String xstr = String.format("%06d", x);
                                da.append("S" + xstr + "\"");
                                da.append(" IdUnitaNegoziali=\"");
                                String ystr = String.format("%06d", Integer.parseInt(gresult.getUnitaNeg().trim()));
                                da.append("U" + ystr);
                                da.append("\"");

                                da.append(" &gt;");
                                da.append("<br/>");
                                da.append("&lt;DatiTitolarita&gt;");
                                da.append("<br/>");
                                da.append("&lt;Qualifica ");

                                if (!ValidationHelper.isNullOrEmpty(gresult.getSectionCType())) {
                                    da.append("Qualifica=\"");
                                    if (gresult.getSectionCType().equals(SectionCType.CONTRO.getName())) {
                                        da.append("CONTRO\"");
                                    } else if (gresult.getSectionCType().equals(SectionCType.A_FAVORE.getName())) {
                                        da.append("FAVORE\"");
                                    }
                                    da.append(" TipoQualifica=\"");
                                    if (gresult.getSectionCType().equals(SectionCType.CONTRO.getName())) {
                                        da.append("C\"");
                                    } else if (gresult.getSectionCType().equals(SectionCType.A_FAVORE.getName())) {
                                        da.append("F\"");
                                    }
                                }

                                String xmlCode = "";
                                try {
                                    String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.TABLE_CONSERVATIVE_CODE_FILES).getValue();
                                    if (!ValidationHelper.isNullOrEmpty(path)) {
                                        if (!ValidationHelper.isNullOrEmpty(gresult.getPropertyType())) {
                                            Path xmlFile = Paths.get(path);
                                            if (Files.exists(xmlFile)) {
                                                Document doc = ImportXMLHelper.prepareDocument(xmlFile.toFile());
                                                if (doc != null) {
                                                    NodeList baseNList = doc.getElementsByTagName("CodiceDiritti");
                                                    if (baseNList.getLength() > 0) {
                                                        NodeList nList = ((Element) baseNList.item(0)).getElementsByTagName("Row");
                                                        for (int temp = 0; temp < nList.getLength(); temp++) {
                                                            Node nNode = nList.item(temp);
                                                            org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
                                                            if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem("Des"))) {
                                                                String attributeValue = namedNodeMap.getNamedItem("Des").getNodeValue();
                                                                if (attributeValue.trim().equalsIgnoreCase(gresult.getPropertyType())) {
                                                                    xmlCode = namedNodeMap.getNamedItem("Cod").getNodeValue();
                                                                }
                                                            }

                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    LogHelper.log(log, e);
                                }

                                da.append("&#47;&gt;");
                                da.append("<br/>");
                                da.append("&lt;DirittiReali ");
                                if (!ValidationHelper.isNullOrEmpty(xmlCode)) {
                                    da.append(" CodiceDiritto=\"");
                                    da.append(xmlCode);
                                    da.append("\"");
                                }

                                if (!ValidationHelper.isNullOrEmpty(gresult) &&
                                        !ValidationHelper.isNullOrEmpty(gresult.getPropertyType())) {
                                    da.append(" Descrizione=\"");
                                    da.append(gresult.getPropertyType());
                                    da.append("\"");
                                }
                                if (!ValidationHelper.isNullOrEmpty(gresult) &&
                                        !ValidationHelper.isNullOrEmpty(gresult.getQuote())) {
                                    da.append(" Quota=\"");
                                    da.append(gresult.getQuote());
                                    da.append("\"");
                                }

                                if (!ValidationHelper.isNullOrEmpty(gresult) &&
                                        !ValidationHelper.isNullOrEmpty(gresult.getRegime())) {
                                    Regime regime = DaoManager.get(Regime.class,
                                            new Criterion[]{
                                                    Restrictions.eq("text", gresult.getRegime())});
                                    if (!ValidationHelper.isNullOrEmpty(regime)) {
                                        da.append(" RegimeConiugi=\"");
                                        da.append(regime.getCode());
                                        da.append("\"");
                                    }
                                }
                                da.append("&#47;&gt;");
                                da.append("<br/>");
                                da.append("&lt;&#47;DatiTitolarita&gt;");
                                da.append("<br/>");
                                da.append("&lt;&#47;DatiAssociazione&gt;");
                                gindex++;
                            }


//
//                           if(groupedResult.size() > 0)
//                               da.append("\"");
//                           da.append(" &gt;");
//                           da.append("<br/>");
//                           da.append("&lt;DatiTitolarita&gt;");
//                           da.append("<br/>");
//                           da.append("&lt;Qualifica ");
//                           code = "";
//                           Relationship gresult = null;
//                           if(groupedResult.size() > 0) {
//                               gresult = groupedResult.get(0);
//                               if(!ValidationHelper.isNullOrEmpty(gresult.getSectionCType())) {
//                                   da.append("Qualifica=\"");
//                                   if(gresult.getSectionCType().equals(SectionCType.CONTRO.getName())) {
//                                       da.append("CONTRO\"");
//                                   }else if(gresult.getSectionCType().equals(SectionCType.A_FAVORE.getName())) {
//                                       da.append("FAVORE\"");
//                                   }
//                                   da.append(" TipoQualifica=\"");
//                                   if(gresult.getSectionCType().equals(SectionCType.CONTRO.getName())) {
//                                       da.append("C\"");
//                                   }else if(gresult.getSectionCType().equals(SectionCType.A_FAVORE.getName())) {
//                                       da.append("F\"");
//                                   }
//                               }
//
//                               try {
//                                   String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.TABLE_CONSERVATIVE_CODE_FILES).getValue();
//                                   if (!ValidationHelper.isNullOrEmpty(path)) {
//                                       if(!ValidationHelper.isNullOrEmpty(gresult.getPropertyType())) {
//                                           Path xmlFile = Paths.get(path);
//                                           if(Files.exists(xmlFile)) {
//                                               Document doc = ImportXMLHelper.prepareDocument(xmlFile.toFile());
//                                               if (doc != null) {
//                                                   NodeList baseNList = doc.getElementsByTagName("CodiceDiritti");
//                                                   if (baseNList.getLength() > 0) {
//                                                       NodeList nList = ((Element) baseNList.item(0)).getElementsByTagName("Row");
//                                                       for (int temp = 0; temp < nList.getLength(); temp++) {
//                                                           Node nNode = nList.item(temp);
//                                                           org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
//                                                           if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem("Des"))) {
//                                                               String attributeValue = namedNodeMap.getNamedItem("Des").getNodeValue();
//                                                               if(attributeValue.trim().equalsIgnoreCase(gresult.getPropertyType())) {
//                                                                   code = namedNodeMap.getNamedItem("Cod").getNodeValue();
//                                                               }
//                                                           }
//
//                                                       }
//
//                                                   }
//                                               }
//                                           }
//                                       }
//                                   }
//                               } catch (Exception e) {
//                                   LogHelper.log(log, e);
//                               }
//                           }
//                           da.append("&#47;&gt;");
//                           da.append("<br/>");
//                           da.append("&lt;DirittiReali ");
//                           if(!ValidationHelper.isNullOrEmpty(code)) {
//                               da.append(" CodiceDiritto=\"");
//                               da.append(code);
//                               da.append("\"");
//                           }
//
//                           if(!ValidationHelper.isNullOrEmpty(gresult) &&
//                                   !ValidationHelper.isNullOrEmpty(gresult.getPropertyType())) {
//                               da.append(" Descrizione=\"");
//                               da.append(gresult.getPropertyType());
//                               da.append("\"");
//                           }
//                           if(!ValidationHelper.isNullOrEmpty(gresult) &&
//                                   !ValidationHelper.isNullOrEmpty(gresult.getQuote())) {
//                               da.append(" Quota=\"");
//                               da.append(gresult.getQuote());
//                               da.append("\"");
//                           }
//
//                           if(!ValidationHelper.isNullOrEmpty(gresult) &&
//                                   !ValidationHelper.isNullOrEmpty(gresult.getRegime())) {
//                               Regime regime = DaoManager.get(Regime.class,
//                                       new Criterion[]{
//                                               Restrictions.eq("text", gresult.getRegime())});
//                               if(!ValidationHelper.isNullOrEmpty(regime)) {
//                                   da.append(" RegimeConiugi=\"");
//                                   da.append(regime.getCode());
//                                   da.append("\"");
//                               }
//                           }
//                           da.append("&#47;&gt;");
//                           da.append("<br/>");
//                           da.append("&lt;&#47;DatiTitolarita&gt;");
//                           da.append("<br/>");
//                           da.append("&lt;&#47;DatiAssociazione&gt;");
                        }
                    } else {
                        List<Subject> sectionCSubjects = new ArrayList<Subject>();
                        for (SectionC sectionC : request.getTranscriptionActId().getSectionC()) {
                            sectionCSubjects.addAll(sectionC.getSubject());
                        }
                        distinctSubjects = sectionCSubjects.stream()
                                .distinct()
                                .collect(Collectors.toList());

                        int x = 1;
                        for (Subject subject : distinctSubjects) {
                            if (da.length() > 0)
                                da.append("<br/>");
                            da.append("&lt;DatiAssociazione IdSoggetto=\"");
                            String xstr = String.format("%06d", x++);
                            da.append("S" + xstr + "\"");
                            da.append(" &gt;");
                            da.append("<br/>");
                            da.append("&lt;DatiTitolarita&gt;");
                            da.append("<br/>");
                            da.append("&lt;Qualifica ");
                            if (!ValidationHelper.isNullOrEmpty(subject.getSectionC())) {
                                SectionC sectionC = subject.getSectionC().get(0);
                                if (!ValidationHelper.isNullOrEmpty(sectionC.getSectionCType())) {
                                    da.append("Qualifica=\"");
                                    if (sectionC.getSectionCType().equals(SectionCType.CONTRO.getName())) {
                                        da.append("CONTRO\"");
                                    } else if (sectionC.getSectionCType().equals(SectionCType.A_FAVORE.getName())) {
                                        da.append("FAVORE\"");
                                    }
                                    da.append(" TipoQualifica=\"");
                                    if (sectionC.getSectionCType().equals(SectionCType.CONTRO.getName())) {
                                        da.append("C\"");
                                    } else if (sectionC.getSectionCType().equals(SectionCType.A_FAVORE.getName())) {
                                        da.append("F\"");
                                    }
                                }

                            }
                            da.append("&#47;&gt;");
                            da.append("<br/>");
                            da.append("&lt;&#47;DatiTitolarita&gt;");
                            da.append("<br/>");
                            da.append("&lt;&#47;DatiAssociazione&gt;");
                        }
                    }
                }
                return da.toString();

            case TRASCRIZIONE_QUADROD:
                da = new StringBuffer();

                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) &&
                        !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionD())) {
                    List<SectionD> sectionDs = request.getTranscriptionActId().getSectionD();
                    for (int s = 0; s < sectionDs.size(); s++) {
                        SectionD sectionD = request.getTranscriptionActId().getSectionD().get(s);

                        if (!ValidationHelper.isNullOrEmpty(sectionD.getAdditionalInformation())) {
                            final AtomicInteger atomicInteger = new AtomicInteger(0);
                            Collection<String> tokens = sectionD.getAdditionalInformation().chars()
                                    .mapToObj(c -> String.valueOf((char) c))
                                    .collect(Collectors.groupingBy(c -> atomicInteger.getAndIncrement() / 70
                                            , Collectors.joining()))
                                    .values();
                            for (String token : tokens) {

                                da.append("&lt;Descrizione&gt;");
                                da.append(token);
                                da.append("&lt;&#47;Descrizione&gt;");
                                da.append("<br/>");
                            }
                        }

                        if (da.length() > 0 && s > 0 && s < sectionDs.size())
                            da.append("<br/>");
                    }
                }
                return da.toString();
            case TRASCRIZIONE_DATII_MMOBILI:
                da = new StringBuffer();

                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())) {

                    List<SectionB> sectionBList = DaoManager.load(
                            SectionB.class, new Criterion[]{Restrictions.eq("formality", request.getTranscriptionActId())});

                    List<Property> properties = sectionBList.stream().
                            map(SectionB::getProperties).flatMap(List::stream)
                            .collect(Collectors.toList());


                    if (!ValidationHelper.isNullOrEmpty(properties)) {
                        int unitCount = 1;


                        for (int p = 0; p < properties.size(); p++) {

                            da.append("&lt;DatiImmobile IdImmobile=\"");
                            Property property = properties.get(p);
                            if (!ValidationHelper.isNullOrEmpty(property.getType())) {
                                String str = String.format("%06d", unitCount++);
                                da.append("I");
                                da.append(str);
                                da.append("\"&gt;");
                                da.append("<br/>");
                                da.append("&lt;ImmobileUT");
                                if (!ValidationHelper.isNullOrEmpty(property.getCity())
                                        && !ValidationHelper.isNullOrEmpty(property.getCity().getCfis())) {
                                    da.append(" CodiceComune=\"");
                                    da.append(property.getCity().getCfis());
                                    da.append("\"&#47;&gt;");
                                }

                                if (property.getType().equals(RealEstateType.BUILDING.getId())) {
                                    da.append("<br/>");
                                    da.append("&lt;ImmobileU&gt;");
                                    if (!ValidationHelper.isNullOrEmpty(property.getCadastralData())) {
                                        int counter = 1;
                                        for (CadastralData cadastralData :
                                                CollectionUtils.emptyIfNull(property.getCadastralData())
                                                        .stream()
                                                        .filter(distinctByKey(x -> x.getId()))
                                                        .collect(Collectors.toList())) {
                                            if (property.getCadastralData().size() > 1) {
                                                da.append("<br/>");
                                                da.append("&lt;Graffati&gt;");
                                                da.append("<br/>");
                                                da.append("&lt;ProgGraffato&gt;");
                                                da.append("" + (counter++));
                                                da.append("&lt;&#47;ProgGraffato&gt;");
                                            }
                                            da.append("<br/>");
                                            da.append("&lt;IdentificativoDefinitivo");
                                            if (!ValidationHelper.isNullOrEmpty(cadastralData.getSheet())) {
                                                da.append(" Foglio=\"");
                                                da.append(cadastralData.getSheet());
                                                da.append("\"");
                                            }
                                            if (!ValidationHelper.isNullOrEmpty(cadastralData.getParticle())) {
                                                da.append(" ParticellaUno=\"");
                                                da.append(cadastralData.getParticle());
                                                da.append("\"");
                                            }
                                            if (!ValidationHelper.isNullOrEmpty(cadastralData.getSection())) {
                                                da.append(" SezUrbana=\"");
                                                da.append(cadastralData.getSection());
                                                da.append("\"");
                                            }
                                            if (!ValidationHelper.isNullOrEmpty(cadastralData.getSub())) {
                                                da.append(" SubalternoUno=\"");
                                                da.append(cadastralData.getSub());
                                                da.append("\"");
                                            }
                                            da.append("&#47;&gt;");
                                            if (property.getCadastralData().size() > 1) {
                                                da.append("<br/>");
                                                da.append("&lt;&#47;Graffati&gt;");
                                            }
                                        }
                                    }
                                    da.append("<br/>");
                                    da.append("&lt;ConsistenzaU InteresseSA=\"0\" Legge154=\"0\"&gt;");
                                    da.append("<br/>");
                                    da.append("&lt;ImmobileClassato");
                                    if (!ValidationHelper.isNullOrEmpty(property.getCategory()) &&
                                            !ValidationHelper.isNullOrEmpty(property.getCategory().getCode())) {
                                        da.append(" Categoria=\"");
                                        da.append(property.getCategory().getCode());
                                        da.append("\"");
                                    }
                                    da.append("&gt;");

                                    if (!ValidationHelper.isNullOrEmpty(property.getConsistency()) &&
                                            property.getConsistency().toLowerCase().contains("vani")) {
                                        da.append("<br/>");
                                        da.append("&lt;Vani&gt;");
                                        da.append(ListHelper.ignoreCaseReplace(property.getConsistency(), "vani", "").trim());
                                        da.append("&lt;&#47;Vani&gt;");
                                    } else if (!ValidationHelper.isNullOrEmpty(property.getConsistency()) &&
                                            property.getConsistency().toLowerCase().contains("mq")) {
                                        da.append("<br/>");
                                        da.append("&lt;MetriQuadrati&gt;");
                                        da.append(ListHelper.ignoreCaseReplace(property.getConsistency(), "mq", "").trim());
                                        da.append("&lt;&#47;MetriQuadrati&gt;");
                                    } else if (!ValidationHelper.isNullOrEmpty(property.getConsistency()) &&
                                            property.getConsistency().toLowerCase().contains("metri quadrati")) {
                                        da.append("<br/>");
                                        da.append("&lt;MetriQuadrati&gt;");
                                        da.append(ListHelper.ignoreCaseReplace(property.getConsistency(), "metri quadrati", "").trim());
                                        da.append("&lt;&#47;MetriQuadrati&gt;");
                                    }

                                    da.append("<br/>");
                                    da.append("&lt;&#47;ImmobileClassato&gt;");
                                    da.append("<br/>");
                                    da.append("&lt;&#47;ConsistenzaU&gt;");
                                    da.append("<br/>");
                                    da.append("&lt;&#47;ImmobileU&gt;");
                                    da.append("<br/>");

                                    da.append("&lt;DatiIndirizzo");
                                    if (!ValidationHelper.isNullOrEmpty(property.getAddress())) {
                                        da.append(" Indirizzo=\"");
                                        da.append(property.getAddress());
                                        da.append("\"");

                                    }
                                    if (!ValidationHelper.isNullOrEmpty(property.getFloor())) {
                                        da.append(" Piano=\"");
                                        da.append(property.getFloor());
                                        da.append("\"");
                                    }
                                    da.append("&#47;&gt;");

                                } else if (property.getType().equals(RealEstateType.LAND.getId())) {
                                    da.append("<br/>");
                                    da.append("&lt;ImmobileT&gt;");
                                    if (!ValidationHelper.isNullOrEmpty(property.getCadastralData())) {
                                        int counter = 1;
                                        for (CadastralData cadastralData : CollectionUtils.emptyIfNull(property.getCadastralData())
                                                .stream()
                                                .filter(distinctByKey(x -> x.getId()))
                                                .collect(Collectors.toList())) {
                                            if (property.getCadastralData().size() > 1) {
                                                da.append("<br/>");
                                                da.append("&lt;Graffati&gt;");
                                                da.append("<br/>");
                                                da.append("&lt;ProgGraffato&gt;");
                                                da.append("" + (counter++));
                                                da.append("&lt;&#47;ProgGraffato&gt;");
                                            }
                                            da.append("<br/>");
                                            da.append("&lt;IdentificativoDefinitivo");
                                            if (!ValidationHelper.isNullOrEmpty(cadastralData.getSheet())) {
                                                da.append(" Foglio=\"");
                                                da.append(cadastralData.getSheet());
                                                da.append("\"");
                                            }
                                            if (!ValidationHelper.isNullOrEmpty(cadastralData.getParticle())) {
                                                da.append(" ParticellaUno=\"");
                                                da.append(cadastralData.getParticle());
                                                da.append("\"");
                                            }
                                            if (!ValidationHelper.isNullOrEmpty(cadastralData.getSection())) {
                                                da.append(" SezUrbana=\"");
                                                da.append(cadastralData.getSection());
                                                da.append("\"");
                                            }
                                            if (!ValidationHelper.isNullOrEmpty(cadastralData.getSub())) {
                                                da.append(" SubalternoUno=\"");
                                                da.append(cadastralData.getSub());
                                                da.append("\"");
                                            }
                                            da.append("&#47;&gt;");
                                            if (property.getCadastralData().size() > 1) {
                                                da.append("<br/>");
                                                da.append("&lt;&#47;Graffati&gt;");
                                            }
                                        }
                                    }

                                    da.append("<br/>");
                                    da.append("&lt;ConsistenzaT Natura=\"T\" TipoTerreno=\"5\"&gt;");
                                    da.append("<br/>");
                                    da.append("&lt;SuperficieTotale&gt;");
                                    da.append("<br/>");
                                    da.append("&lt;Superficie");
                                    if (!ValidationHelper.isNullOrEmpty(property.getAres())) {
                                        da.append(" Are=\"");
                                        da.append(property.getAres().intValue());
                                        da.append("\"");
                                    }
                                    if (!ValidationHelper.isNullOrEmpty(property.getCentiares())) {
                                        da.append(" Centiare=\"");
                                        da.append(property.getCentiares().intValue());
                                        da.append("\"");
                                    }
                                    if (!ValidationHelper.isNullOrEmpty(property.getHectares())) {
                                        da.append(" Ettari=\"");
                                        da.append(property.getHectares().intValue());
                                        da.append("\"");
                                    }
                                    da.append("&#47;&gt;");
                                    da.append("<br/>");
                                    da.append("&lt;&#47;SuperficieTotale&gt;");
                                    da.append("<br/>");
                                    da.append("&lt;&#47;ConsistenzaT&gt;");
                                    da.append("<br/>");
                                    da.append("&lt;&#47;ImmobileT&gt;");
                                }
                            }
                            da.append("<br/>");
                            da.append("&lt;&#47;DatiImmobile&gt;");
                            da.append("<br/>");
                        }

                    }
                }

                return da.toString();

            case TASCRIZIONE_DATI_SOGGETTI:
                da = new StringBuffer();
                if (!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())) {
                    List<Relationship> relationships = DaoManager.load(Relationship.class, new Criterion[]{
                            Restrictions.eq("formality", request.getTranscriptionActId())
                    });
                    List<Subject> distinctSubjects = null;
                    if (!ValidationHelper.isNullOrEmpty(relationships)) {
                        distinctSubjects = relationships.stream().
                                filter(r -> Objects.nonNull(
                                        r.getSubject()))
                                .filter(ListHelper.distinctByKey(r -> r.getSubject()))
                                .map(Relationship::getSubject)
                                .collect(Collectors.toList());

                    } else {
                        List<Subject> sectionCSubjects = new ArrayList<Subject>();
                        for (SectionC sectionC : request.getTranscriptionActId().getSectionC()) {
                            sectionCSubjects.addAll(sectionC.getSubject());
                        }
                        distinctSubjects = sectionCSubjects.stream()
                                .distinct()
                                .collect(Collectors.toList());

                    }

                    int x = 1;
                    for (Subject subject : distinctSubjects) {
                        if (da.length() > 0)
                            da.append("<br/>");
                        relationships = DaoManager.load(Relationship.class, new Criterion[]{
                                Restrictions.eq("subject", subject)
                        });

                        if (subject.getTypeId().equals(1L)) {
                            da.append("&lt;DatiSoggetto IdSoggetto=\"");
                            String xstr = String.format("%06d", x++);
                            da.append("S" + xstr + "\"");
                            da.append(" &gt;");
                            da.append("<br/>");
                            da.append("&lt;SoggettoF");
                            if (!ValidationHelper.isNullOrEmpty(subject.getFiscalCode())) {
                                da.append(" CodiceFiscale=\"");
                                da.append(subject.getFiscalCode());
                                da.append("\"");
                            }
                            if (!ValidationHelper.isNullOrEmpty(subject.getSurname())) {
                                da.append(" Cognome=\"");
                                da.append(subject.getSurname());
                                da.append("\"");
                            }

                            if (!ValidationHelper.isNullOrEmpty(subject.getForeignCountry()) &&
                                    subject.getForeignCountry()) {
                                if (!ValidationHelper.isNullOrEmpty(subject.getCountry())
                                        && !ValidationHelper.isNullOrEmpty(subject.getCountry().getDescription())) {
                                    da.append(" ComuneNascita=\"");
                                    da.append(subject.getCountry().getDescription());
                                    da.append("\"");
                                }
                            } else if (!ValidationHelper.isNullOrEmpty(subject.getBirthCityDescription())) {
                                da.append(" ComuneNascita=\"");
                                da.append(subject.getBirthCityDescription());
                                da.append("\"");
                            }

                            if (!ValidationHelper.isNullOrEmpty(subject.getBirthDate())) {
                                da.append(" DataNascita=\"");
                                da.append(DateTimeHelper.toFormatedString(subject.getBirthDate(), DateTimeHelper.getXmlDatePattert()));
                                da.append("\"");
                            }

                            if (!ValidationHelper.isNullOrEmpty(subject.getName())) {
                                da.append(" Nome=\"");
                                da.append(subject.getName());
                                da.append("\"");
                            }

                            if (!ValidationHelper.isNullOrEmpty(subject.getForeignCountry()) &&
                                    subject.getForeignCountry()) {
                                da.append(" Provincia=\"EE\"");
                            } else if (!ValidationHelper.isNullOrEmpty(subject.getBirthProvince()) &&
                                    !ValidationHelper.isNullOrEmpty(subject.getBirthProvince().getCode())) {
                                da.append(" Provincia=\"");
                                da.append(subject.getBirthProvince().getCode());
                                da.append("\"");
                            }

                            if (!ValidationHelper.isNullOrEmpty(subject.getSex())) {
                                da.append(" Sesso=\"");
                                da.append(subject.getSexType().getShortValue());
                                da.append("\"");
                            }
                            da.append("&#47;&gt;");
                            da.append("<br/>");
                            da.append("&lt;&#47;DatiSoggetto&gt;");
                        } else if (subject.getTypeId().equals(2L)) {
                            da.append("&lt;DatiSoggetto IdSoggetto=\"");
                            String xstr = String.format("%06d", x++);
                            da.append("S" + xstr + "\"");
                            da.append(" &gt;");
                            da.append("<br/>");
                            da.append("&lt;SoggettoN");
                            if (!ValidationHelper.isNullOrEmpty(subject.getNumberVAT())) {
                                da.append(" CodiceFiscale=\"");
                                da.append(subject.getNumberVAT());
                                da.append("\"");
                            }
                            if (!ValidationHelper.isNullOrEmpty(subject.getBusinessName())) {
                                da.append(" Denominazione=\"");
                                da.append(subject.getBusinessName());
                                da.append("\"");
                            }

                            if (!ValidationHelper.isNullOrEmpty(subject.getForeignCountry()) &&
                                    subject.getForeignCountry()) {
                                da.append(" Provincia=\"EE\"");
                            } else if (!ValidationHelper.isNullOrEmpty(subject.getBirthProvince()) &&
                                    !ValidationHelper.isNullOrEmpty(subject.getBirthProvince().getCode())) {
                                da.append(" Provincia=\"");
                                da.append(subject.getBirthProvince().getCode());
                                da.append("\"");
                            }


                            if (!ValidationHelper.isNullOrEmpty(subject.getForeignCountry()) &&
                                    subject.getForeignCountry()) {
                                if (!ValidationHelper.isNullOrEmpty(subject.getCountry())
                                        && !ValidationHelper.isNullOrEmpty(subject.getCountry().getDescription())) {
                                    da.append(" Sede=\"");
                                    da.append(subject.getCountry().getDescription());
                                    da.append("\"");
                                }
                            } else if (!ValidationHelper.isNullOrEmpty(subject.getBirthCityDescription())) {
                                da.append(" Sede=\"");
                                da.append(subject.getBirthCityDescription());
                                da.append("\"");
                            }
                            da.append("&#47;&gt;");
                            da.append("<br/>");
                            da.append("&lt;&#47;DatiSoggetto&gt;");
                        }
                    }
                }
                return da.toString();

            case TASCRIZIONE_DTD:
                String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.DTD).getValue();
                if (!ValidationHelper.isNullOrEmpty(path)) {
                    return Paths.get(path).getFileName().toString();
                } else
                    return "";

            case ATTACHMENT_INDICATION:
                Boolean showEstateSituationTag = Boolean.FALSE;
                StringBuffer attachmentBuffer = new StringBuffer();

                if (!ValidationHelper.isNullOrEmpty(request.getClient()) &&
                        !ValidationHelper.isNullOrEmpty(request.getClient().getLandOmi()) &&
                        request.getClient().getLandOmi()) {
                    List<EstateSituation> estateSituations = getRequest().getSituationEstateLocations();
                    if (!ValidationHelper.isNullOrEmpty(estateSituations)) {
                        List<Property> propertyList = estateSituations
                                .stream()
                                .map(EstateSituation::getPropertyList).flatMap(List::stream)
                                .collect(Collectors.toList());
                        EstateSituation salesEstateSituation = getRequest().getSituationEstateLocations().stream()
                                .filter(es -> !ValidationHelper.isNullOrEmpty(es.getSalesDevelopment()) &&
                                        es.getSalesDevelopment())
                                .findFirst()
                                .orElse(null);
                        Property landProperty = propertyList
                                .stream()
                                .filter(p -> !ValidationHelper.isNullOrEmpty(p.getType()) &&
                                        RealEstateType.LAND.getId().equals(p.getType()))
                                .findFirst()
                                .orElse(null);
                        if (!ValidationHelper.isNullOrEmpty(salesEstateSituation) ||
                                !ValidationHelper.isNullOrEmpty(landProperty)) {
                            showEstateSituationTag = Boolean.TRUE;
                        }
                        if (showEstateSituationTag) {
                            attachmentBuffer.append("Sono presenti:<br/>");
                        }
                        if (showEstateSituationTag) {
                            attachmentBuffer.append("<ul>");
                            if (!ValidationHelper.isNullOrEmpty(landProperty)) {
                                attachmentBuffer.append("<li>");
                                attachmentBuffer.append("Valori OMI terreni, vedere Allegato A");
                                attachmentBuffer.append("</li>");
                            }
                            if (!ValidationHelper.isNullOrEmpty(salesEstateSituation)) {
                                attachmentBuffer.append("<li>");
                                attachmentBuffer.append("Sviluppo atti di alienazione degli ultimi 5 anni, vedere Allegato B");
                                attachmentBuffer.append("</li>");
                            }
                            attachmentBuffer.append("</ul>");
                        }
                    }
                }

                return attachmentBuffer.toString();

            case ATTACHMENT_A:
                attachmentBuffer = new StringBuffer();
                if (!ValidationHelper.isNullOrEmpty(request.getClient()) &&
                        !ValidationHelper.isNullOrEmpty(request.getClient().getLandOmi()) &&
                        request.getClient().getLandOmi()) {

                    List<EstateSituation> landEstateSituations = getRequest().getSituationEstateLocations();
                    if (!ValidationHelper.isNullOrEmpty(landEstateSituations)) {
                        DecimalFormat df = new DecimalFormat(
                                "#,##0.00",
                                new DecimalFormatSymbols(new Locale("pt", "BR")));
                        List<Property> propertyList = landEstateSituations
                                .stream()
                                .map(EstateSituation::getPropertyList).flatMap(List::stream)
                                .collect(Collectors.toList());

                        List<Property> landProperties = propertyList
                                .stream()
                                .filter(p -> !ValidationHelper.isNullOrEmpty(p.getCity()) &&
                                        !ValidationHelper.isNullOrEmpty(p.getType()) &&
                                        RealEstateType.LAND.getId().equals(p.getType()))
                                .collect(Collectors.toList());

                        if (!ValidationHelper.isNullOrEmpty(landProperties)) {
                            attachmentBuffer.append("<style type=\"text/css\"> *{box-sizing: border-box;}.container{border-radius: 5px; padding: 20px;}.col-10{float: left; width: 10%;}.col-20{float: left; width: 20%;}.col-25{float: left; width: 25%;}.col-35{float: left; width: 35%;}.col-75{float: left; width: 75%;}.col-65{float: left; width: 65%;}/* Clear floats after the columns */ .row:after{content: \"\"; display: table; clear: both;}.b2{border-top: 1px black solid !important; border-right: 1px black solid !important; border-left: 1px black solid !important;}.p10{padding: 10px;}.p6{padding: 6px;}.txt-center{text-align: center;}.no-rb{border-right: 0px;}.row:not(.last-row) > div{border-bottom: none;}.row{display: flex;}.row > div > div{word-break: break-all;}.blank-col{min-height: 40px;}.allegatoa{width: 100%; border-collapse: collapse; font-family: Courier New, Courier, monospace !important; font-size: 12px;}.allegatoa tr{line-height: 5px;}.allegatoa, .allegatoa td, .allegatoa th{border: 1px black solid !important;}.allegatoa tr{display: flex;}.datiterrenomain{padding-left: 1em; text-indent: 0pt; margin-top: 0em; margin-bottom: 0em; line-height: 1pt !important; text-align: left;}.datiterreno{padding-left: 7em; text-indent: 0pt; margin-top: 0em; margin-bottom: 0em; line-height: 1pt !important; text-align: left;}.allegatoa.of-h tr{line-height: 8px;}/* Responsive layout - when the screen is less than 600px wide, make the two columns stack on top of each other instead of next to each other */ @media screen and (max-width: 600px){.col-25, .col-75;}</style>");
                            attachmentBuffer.append("<pd4ml-page-break /><h3>&nbsp;</h3>");
                            attachmentBuffer.append("<div style=\"text-align: center\"><h3>Allegato A</h3></div>");
                            attachmentBuffer.append("<div style=\"text-align: center;\"><h3>");
                            attachmentBuffer.append(ResourcesHelper.getString("formalityListAllegatoAHeader"));
                            attachmentBuffer.append("</h3></div>");

                            Map<City, List<Property>> cityProperties = landProperties.stream()
                                    .collect(Collectors.groupingBy(Property::getCity));

                            for (Map.Entry<City, List<Property>> entry : cityProperties.entrySet()) {

                                attachmentBuffer.append(" <div class=\"container\">");
                                attachmentBuffer.append(" <div class=\"row\">"); // Row 1
                                attachmentBuffer.append("<div class=\"col-35\" style=\"margin-top: 1px;\">");
                                attachmentBuffer.append("</div>");
                                attachmentBuffer.append("<div class=\"col-65 b2 p10\">");

                                attachmentBuffer.append("<div>Comune: <b>");
                                attachmentBuffer.append(entry.getKey().getDescription());
                                attachmentBuffer.append("</b></div>");
                                attachmentBuffer.append("<div>Provincia: <b>");
                                if (!ValidationHelper.isNullOrEmpty(entry.getKey().getProvince()))
                                    attachmentBuffer.append(entry.getKey().getProvince().getDescription());
                                attachmentBuffer.append("</b></div>");
                                attachmentBuffer.append(" <div style=\"float: right\">Aggiornamento dati valori OMI: ");
                                if (!ValidationHelper.isNullOrEmpty(entry.getKey().getLandOmis())) {
                                    attachmentBuffer.append(entry.getKey().getLandOmis().get(0).getYear());
                                }
                                attachmentBuffer.append("</div>");

                                attachmentBuffer.append("</div>");
                                attachmentBuffer.append("</div>"); // Row 1

                                attachmentBuffer.append("<table class=\"allegatoa of-h\">");  // table1

                                attachmentBuffer.append("<thead>"); // thead

                                attachmentBuffer.append("<td class=\"col-35 p10 txt-center\"><b>COLTURA</b></td>");
                                attachmentBuffer.append("<td class=\"col-25 p10 txt-center\"><b>Dati terreno</b></td>");
                                attachmentBuffer.append("<td class=\"col-20 p10 txt-center\"><b>Estensione (Mq)</b></td>");
                                attachmentBuffer.append("<td class=\"col-20 p10 txt-center\"><b>Valore OMI (€)</td>");

                                attachmentBuffer.append("</thead>"); // thead

                                attachmentBuffer.append("<tbody>"); // tbody

                                List<Property> properties = entry.getValue();
                                List<Property> inAppropriateProperties = new ArrayList<>();
                                Double extensionTotal = 0.0;
                                Double omiValueTotal = 0.0;
                                Map<Long, String> terrenoData = landPropertyBlock(properties);
                                for (Property property : properties) {
                                    Boolean inAppropriateProperty = Boolean.FALSE;
                                    String landCultureName = "";
                                    if (!ValidationHelper.isNullOrEmpty(property.getQuality())) {
                                        List<LandCadastralCulture> landCadastralCultures = DaoManager.load(LandCadastralCulture.class,
                                                new Criterion[]{Restrictions.eq("description", property.getQuality()).ignoreCase()
                                                });
                                        if (!ValidationHelper.isNullOrEmpty(landCadastralCultures)) {
                                            LandCulture landCulture = landCadastralCultures.get(0).getLandCulture();
                                            if (!ValidationHelper.isNullOrEmpty(landCulture)) {
                                                landCultureName = landCulture.getName();
                                            } else {
                                                inAppropriateProperty = Boolean.TRUE;
                                            }
                                        } else {
                                            inAppropriateProperty = Boolean.TRUE;
                                        }
                                    } else {
                                        inAppropriateProperty = Boolean.TRUE;
                                    }
                                    if (inAppropriateProperty) {
                                        inAppropriateProperties.add(property);
                                        continue;
                                    }
                                    extensionTotal += property.getLandMQ();
                                    attachmentBuffer.append("<tr>"); // Property row

                                    attachmentBuffer.append("<td class=\"col-35 p6 txt-center\">");
                                    attachmentBuffer.append(landCultureName);
                                    attachmentBuffer.append("</td>");

                                    attachmentBuffer.append("<td class=\"col-25 p10 txt-center\">");
                                    String landBlock = terrenoData.get(property.getId());
                                    String landClass = "";
                                    if (landBlock.contains("foglio")) {
                                        landClass = "datiterrenomain";
                                    } else
                                        landClass = "datiterreno";

                                    attachmentBuffer.append("<p class=\"");
                                    attachmentBuffer.append(landClass);
                                    attachmentBuffer.append("\" style=\"float:right\">");
                                    attachmentBuffer.append(landBlock);
                                    attachmentBuffer.append("</p>");
                                    attachmentBuffer.append("</td>");

                                    attachmentBuffer.append("<td class=\"col-20 p10 txt-center\">");
                                    attachmentBuffer.append(property.getEstateLandMQ());
                                    attachmentBuffer.append("</td>");

                                    attachmentBuffer.append("<td class=\"col-20 p10 txt-center\">");
                                    if (!ValidationHelper.isNullOrEmpty(property.getQuality())) {
                                        List<LandCadastralCulture> landCadastralCultures = DaoManager.load(LandCadastralCulture.class,
                                                new Criterion[]{Restrictions.eq("description", property.getQuality()).ignoreCase()
                                                });
                                        List<LandCulture> landCultures = emptyIfNull(landCadastralCultures)
                                                .stream()
                                                .filter(lcc -> !ValidationHelper.isNullOrEmpty(lcc.getLandCulture()))
                                                .map(LandCadastralCulture::getLandCulture)
                                                .collect(Collectors.toList());

                                        if (!ValidationHelper.isNullOrEmpty(landCultures)) {
                                            List<LandOmiValue> landOmiValues = DaoManager.load(LandOmiValue.class,
                                                    new Criterion[]{Restrictions.in("landCulture", landCultures)
                                                    });
                                            if (!ValidationHelper.isNullOrEmpty(landOmiValues)) {
                                                List<LandOmiValue> cityLandOmiValues = landOmiValues
                                                        .stream()
                                                        .filter(lov -> !ValidationHelper.isNullOrEmpty(lov.getLandOmi())
                                                                && !ValidationHelper.isNullOrEmpty(lov.getLandOmi().getCities())
                                                                && lov.getLandOmi().getCities().contains(entry.getKey()))
                                                        .collect(Collectors.toList());
                                                if (!ValidationHelper.isNullOrEmpty(cityLandOmiValues)
                                                        && !ValidationHelper.isNullOrEmpty(property.getLandMQ())) {
                                                    Double omiValue = (cityLandOmiValues.get(0).getValue() / 10000) * property.getLandMQ();
                                                    BigDecimal value = new BigDecimal(omiValue);
                                                    String omiValueString = df.format(value.doubleValue());
                                                    attachmentBuffer.append(omiValueString);
                                                    omiValueTotal += omiValue;
                                                }
                                            }
                                        }
                                    }
                                    attachmentBuffer.append("</td>");
                                    attachmentBuffer.append("</tr>"); // Property row
                                }


                                attachmentBuffer.append("<tr>"); // Total row

                                attachmentBuffer.append("<td class=\"col-35 p10 txt-center\">");
                                attachmentBuffer.append("<b>TOTALE</b>");
                                attachmentBuffer.append("</td>");
                                attachmentBuffer.append("<td class=\"col-25 p10 txt-center\">");
                                attachmentBuffer.append("</td>");
                                attachmentBuffer.append("<td class=\"col-20 p10 txt-center\"><b>");
                                String extensionTotalValue = extensionTotal.toString();
                                if (extensionTotalValue.endsWith(".00") || extensionTotalValue.endsWith(".0"))
                                    extensionTotalValue = extensionTotalValue.substring(0, extensionTotalValue.lastIndexOf("."));
                                if (!extensionTotalValue.contains(".") && !extensionTotalValue.contains(",")) {
                                    extensionTotalValue = GeneralFunctionsHelper.formatDoubleString(extensionTotalValue);
                                }
                                attachmentBuffer.append(extensionTotalValue);
                                attachmentBuffer.append("</b></td>");
                                attachmentBuffer.append("<td class=\"col-20 p10 txt-center\"><b>");

                                String omiValueString = df.format(omiValueTotal);
                                attachmentBuffer.append(omiValueString);
                                attachmentBuffer.append("</b></td>");

                                attachmentBuffer.append("</tr>"); // Total row

                                attachmentBuffer.append("</tbody>"); // tbody
                                attachmentBuffer.append("</table>"); // table1

                                if (!ValidationHelper.isNullOrEmpty(inAppropriateProperties)) {
                                    terrenoData = landPropertyBlock(inAppropriateProperties);
                                    attachmentBuffer.append("<br/><table style=\"margin-top: 50px;\" class=\"allegatoa\">"); // table2
                                    attachmentBuffer.append("<tbody>"); // tbody2


                                    extensionTotal = 0.0;
                                    for (Property property : inAppropriateProperties) {
                                        extensionTotal += property.getLandMQ();
                                        attachmentBuffer.append("<tr>");

                                        attachmentBuffer.append("<td class=\"col-35 p10 txt-center\">");
                                        attachmentBuffer.append("NON DISPONIBILE");
                                        attachmentBuffer.append("</td>");
                                        attachmentBuffer.append("<td class=\"col-25 p10 txt-center\">");
                                        String landBlock = terrenoData.get(property.getId());
                                        String landClass = "";
                                        if (landBlock.contains("foglio")) {
                                            landClass = "datiterrenomain";
                                        } else
                                            landClass = "datiterreno";

                                        attachmentBuffer.append("<p class=\"");
                                        attachmentBuffer.append(landClass);
                                        attachmentBuffer.append("\" style=\"float:right\">");
                                        attachmentBuffer.append(landBlock);
                                        attachmentBuffer.append("</p>");

                                        attachmentBuffer.append("</td>");
                                        attachmentBuffer.append("<td class=\"col-20 p10 txt-center\">");
                                        attachmentBuffer.append(property.getEstateLandMQ());
                                        attachmentBuffer.append("</td>");
                                        attachmentBuffer.append("<td class=\"col-20 p10 txt-center\">");
                                        attachmentBuffer.append("</td>");

                                        attachmentBuffer.append("</tr>"); // Total row
                                    }
                                    attachmentBuffer.append("</tbody>"); // tbody2
                                    attachmentBuffer.append("</table>"); // table2
                                }
                                attachmentBuffer.append("</div>");// container
                            }
                        }
                    }
                }
                return attachmentBuffer.toString();

            case ATTACHMENT_B:
                attachmentBuffer = new StringBuffer();

                List<EstateSituation> attachmentBEstateSituations = DaoManager.load(EstateSituation.class, new Criterion[]{
                        Restrictions.eq("request.id", getRequest().getId()),
                        Restrictions.eq("salesDevelopment", Boolean.TRUE)
                });
                if (!ValidationHelper.isNullOrEmpty(attachmentBEstateSituations)) {
                    attachmentBEstateSituations.sort(new RequestTextEditBean.SortByInnerEstateFormalityDate());
                    List<Formality> formalities = attachmentBEstateSituations.get(0).getFormalityList();
                    if (ValidationHelper.isNullOrEmpty(formalities)) {
                        return "";
                    }
                    formalities.sort(Comparator.comparing(Formality::getComparedDate)
                            .thenComparing(Formality::getGeneralRegister)
                            .thenComparing(Formality::getParticularRegister));
                    int counter = 1;
                    List<PartedPairsByCityWrapper> partedPairsByCityWrapperList = new ArrayList<>();
                    List<Long> listIds = EstateSituationHelper.getIdSubjects(request);
                    List<Subject> presumableSubjects = EstateSituationHelper.getListSubjects(listIds);
                    List<Subject> unsuitableSubjects = SubjectHelper.deleteUnsuitable(presumableSubjects, formalities);
                    presumableSubjects.removeAll(unsuitableSubjects);
                    presumableSubjects.add(request.getSubject());
                    for (Formality formality : formalities) {
                        Boolean showCadastralIncome = Boolean.FALSE;
                        Boolean showAgriculturalIncome = Boolean.FALSE;
                        if (!ValidationHelper.isNullOrEmpty(request.getClient()) &&
                                !ValidationHelper.isNullOrEmpty(request.getClient().getShowCadastralIncome())) {
                            showCadastralIncome = request.getClient().getShowCadastralIncome();
                        }
                        if (!ValidationHelper.isNullOrEmpty(request.getClient()) &&
                                !ValidationHelper.isNullOrEmpty(request.getClient().getShowAgriculturalIncome())) {
                            showAgriculturalIncome = request.getClient().getShowAgriculturalIncome();
                        }
                        List<Property> properties = formality.loadPropertiesByRelationship(presumableSubjects);
                        List<Pair<String, String>> tempPairs = TemplatePdfTableHelper.groupPropertiesByQuoteTypeListLikePairs(properties,
                                request.getSubject(), presumableSubjects, false, formality, showCadastralIncome, showAgriculturalIncome, Boolean.FALSE);
                        PartedPairsByCityWrapper pairsByCityWrapper = new PartedPairsByCityWrapper(formality, tempPairs);
                        pairsByCityWrapper.fillPatredList();
                        partedPairsByCityWrapperList.add(pairsByCityWrapper);
                    }

                    attachmentBuffer.append("<pd4ml-page-break><h3>&nbsp;</h3><div style=\"text-align: center\"><h3>Allegato B</h3></div>");
                    attachmentBuffer.append("<div style=\"text-align: center\"><h3>");
                    attachmentBuffer.append(ResourcesHelper.getString("formalityListSDHeader"));
                    attachmentBuffer.append("</h3></div>");
                    List<TagTableWrapper> tagTableList = new ArrayList<>();
                    for (PartedPairsByCityWrapper partedPairsByCityWrapper : partedPairsByCityWrapperList) {
                        for (List<Pair<String, String>> pairList : partedPairsByCityWrapper.getPatredList()) {
                            TagTableWrapper wrapper = new TagTableWrapper();
                            wrapper.counter = counter;
                            wrapper.cityDesc = partedPairsByCityWrapper.getFormality().getFirstPropertyAlienatedTable();
                            wrapper.descriptionRows = new ArrayList<>();
                            wrapper.pairs = new ArrayList<>();
                            String formalityBlock = "";
                            sb = new StringBuffer();
                            if (!ValidationHelper.isNullOrEmpty(partedPairsByCityWrapper.getFormality().getType())) {
                                sb.append(partedPairsByCityWrapper.getFormality().getType());
                                sb.append(" ");
                            }
                            sb.append("N.RI ");
                            if (!ValidationHelper.isNullOrEmpty(partedPairsByCityWrapper.getFormality().getGeneralRegister())) {
                                sb.append(partedPairsByCityWrapper.getFormality().getGeneralRegister());
                                sb.append("/");
                            }
                            if (!ValidationHelper.isNullOrEmpty(partedPairsByCityWrapper.getFormality().getParticularRegister())) {
                                sb.append(partedPairsByCityWrapper.getFormality().getParticularRegister());
                                sb.append(" ");
                            }
                            sb.append("del ");
                            if (!ValidationHelper.isNullOrEmpty(partedPairsByCityWrapper.getFormality().getPresentationDate())) {
                                sb.append(DateTimeHelper.toString(partedPairsByCityWrapper.getFormality().getPresentationDate()));
                                sb.append(" ");
                            }
                            if (sb.length() > 0) {
                                String prefix = sb.toString().toUpperCase();
                                sb.setLength(0);
                                sb.append("<div style=\"text-align: justify;font-weight: bold;\">");
                                sb.append(prefix);
                                String textInVisura = partedPairsByCityWrapper.getFormality().getDicTypeFormalityText();
                                if (!ValidationHelper.isNullOrEmpty(textInVisura)) {
                                    sb.append(" - " + textInVisura);
                                }
                                sb.append("</div>");
                                sb.append("</div><br/>");
                            }
                            String data = partedPairsByCityWrapper.getFormality().getSubjectAlienatedTable(getRequest());
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
                            tagTableList.add(wrapper);
                            counter++;
                        }
                    }
                    attachmentBuffer.append("<ol style=\"font-family: Courier New, Courier, monospace !important; font-size: 12px;\">");

                    int index = 1;
                    for (TagTableWrapper wrapper : tagTableList) {
                        String style = "\"margin-bottom: 40px;";
                        if (index != (tagTableList.size())) {
                            style += "border-bottom: 1px solid black;";
                        }
                        attachmentBuffer.append("<li style=" + style + "\">");
                        attachmentBuffer.append(wrapper.pairs.get(0).getSecond());
                        for (int i = 1; i < wrapper.pairs.size(); i++) {
                            Pair<String, String> cityProperty = wrapper.pairs.get(i);
                            if (!ValidationHelper.isNullOrEmpty(cityProperty.getFirst())) {
                                attachmentBuffer.append("<b>");
                                attachmentBuffer.append(cityProperty.getFirst());
                                attachmentBuffer.append("</b>");
                            }
                            attachmentBuffer.append("<br/>");
                            attachmentBuffer.append(cityProperty.getSecond());
                        }
                        if (index != (tagTableList.size())) {
                            attachmentBuffer.append("<br/>");
                            attachmentBuffer.append("<br/>");
                        }
                        attachmentBuffer.append("</li>");
                        index++;
                    }
                    attachmentBuffer.append("</ol>");
                    attachmentBuffer.append("</div>");
                }
                return attachmentBuffer.toString();
        }
        throw new CannotProcessException("Cannot process such method");

    }


    private static Map<Long, String> landPropertyBlock(List<Property> propertyList) {
        Map<Long, String> map = new HashMap<>();
        List<Pair<CadastralData, Property>> dataList = new ArrayList<>();
        for (Property property : propertyList) {
            for (CadastralData cadastralData : CollectionUtils.emptyIfNull(property.getCadastralData())
                    .stream()
                    .filter(distinctByKey(x -> x.getId()))
                    .collect(Collectors.toList())) {
                dataList.add(new Pair<>(cadastralData, property));
            }
        }
        dataList.sort(Comparator.comparing(p -> p.getFirst().getSheet()));
        for (int i = 0; i < dataList.size(); i++) {
            StringBuilder str = new StringBuilder();
            CadastralData data = dataList.get(i).getFirst();
            Property property = dataList.get(i).getSecond();
            if (i == 0 || !data.getSheet().equals(dataList.get(i - 1).getFirst().getSheet())) {
                str.append("<span>").append("&nbsp;foglio&nbsp;").append(data.getSheet());
            }
            str.append("&nbsp;p.lla&nbsp;").append(data.getParticle())
                    .append("</span>");
            map.put(property.getId(), str.toString());
        }
        return map;
    }

    public static String correctMethodInvoking(Method method, Object instance) {
        Object result = null;
        try {
            result = method.invoke(instance);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (result != null) {
            return convertResult(result);
        }
        return "";
    }

    private static String convertResult(Object result) {
        if (result != null) {
            if (result instanceof Date) {
                return DateTimeHelper.toString((Date) result);
            } else if (result instanceof Boolean) {
                return (Boolean) result ? "SI" : "NO";
            } else {
                return result.toString();
            }
        }
        return null;
    }

    public Request getRequest() {
        return request;
    }

    public UserWrapper getCurrentUser() {
        return currentUser;
    }

    private List<Relationship> getGroupedRelationships(List<Relationship> relationshipList) {
        List<Relationship> intermediateResult = new ArrayList<>();

        Function<Relationship, List<Object>> compositeKey = relationship ->
                Arrays.<Object>asList(
                        relationship.getSectionCType(),
                        relationship.getPropertyType(),
                        relationship.getQuote(),
                        relationship.getUnitaNeg(),
                        relationship.getRegime()
                );

        Map<Object, List<Relationship>> collect =
                relationshipList.stream().collect(Collectors.groupingBy(compositeKey, Collectors.toList()));
        for (Map.Entry<Object, List<Relationship>> stringListEntry : collect.entrySet()) {
            intermediateResult.add(stringListEntry.getValue().get(0));
        }
        return intermediateResult;
    }


}
