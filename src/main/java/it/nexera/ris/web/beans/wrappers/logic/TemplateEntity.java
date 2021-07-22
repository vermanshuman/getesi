package it.nexera.ris.web.beans.wrappers.logic;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

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

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.DocumentGenerationTags;
import it.nexera.ris.common.enums.RealEstateType;
import it.nexera.ris.common.enums.SectionCType;
import it.nexera.ris.common.exceptions.CannotProcessException;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ImportXMLHelper;
import it.nexera.ris.common.helpers.ListHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.TemplatePdfTableHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.tableGenerator.AlienatedTableGenerator;
import it.nexera.ris.common.helpers.tableGenerator.CertificazioneTableGenerator;
import it.nexera.ris.common.helpers.tableGenerator.DeceasedTableGenerator;
import it.nexera.ris.common.helpers.tableGenerator.NegativeTableGenerator;
import it.nexera.ris.common.helpers.tableGenerator.NoAssetsTableGenerator;
import it.nexera.ris.common.helpers.tableGenerator.RealEstateRelationshipTableGenerator;
import it.nexera.ris.common.helpers.tableGenerator.TagTableGenerator;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.CadastralData;
import it.nexera.ris.persistence.beans.entities.domain.EstateFormality;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.Relationship;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.RequestFormality;
import it.nexera.ris.persistence.beans.entities.domain.SectionB;
import it.nexera.ris.persistence.beans.entities.domain.SectionC;
import it.nexera.ris.persistence.beans.entities.domain.SectionD;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Regime;
import it.nexera.ris.settings.ApplicationSettingsHolder;

public class TemplateEntity {

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
                    User user = DaoManager.get(User.class,getRequest().getCreateUserId());
                    if (!ValidationHelper.isNullOrEmpty(user)) {
                        if(!ValidationHelper.isNullOrEmpty(user.getFullname())) {
                            return user.getFullname();    
                        }else  if(!ValidationHelper.isNullOrEmpty(user.getBusinessName())) {
                            return user.getBusinessName();
                        }
                    }
                    return "";
                } else {
                    return "";
                }

            case FILIALE_RICHIEDENTE_RICHIESTA:
                if (getRequest().getCreateUserId() != null) {
                    User user = DaoManager.get(User.class,new CriteriaAlias[]{
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
                if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                      && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) 
                      && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getNumberDirectory())) {
                    sb.append("&lt;PrimoNumeroRepertorio&gt;");
                    String numberDirectory = request.getTranscriptionActId().getSectionA().getNumberDirectory();
                    String [] tokens = numberDirectory.trim().split("\\/");
                    sb.append(tokens[0].trim());
                    sb.append("&lt;&#47;PrimoNumeroRepertorio&gt;");
                    if(tokens.length > 1) {
                        sb.append("\n");
                        sb.append("&lt;SecondoNumeroRepertorio&gt;");
                        sb.append(tokens[1].trim());
                        sb.append("&lt;&#47;SecondoNumeroRepertorio&gt;");
                    }
                }
                return sb.toString();
            case TRASCRIZIONE_CODICEFISCALEPU:
                if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getFiscalCode())) {
                    
                    return request.getTranscriptionActId().getSectionA().getFiscalCode();
                }else
                    return "";
                
            case TRASCRIZIONE_CF_RICHIEDENTE:
                if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getFiscalCodeAppliant())) {
                    
                    return request.getTranscriptionActId().getSectionA().getFiscalCodeAppliant();
                }else
                    return "";
                
            case TRASCRIZIONE_NOME_RICHIEDENTE:
                if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getApplicant())) {
                    
                    return request.getTranscriptionActId().getSectionA().getApplicant();
                }else
                    return "";
                
            case TRASCRIZIONE_INDIRIZZO_RICHIEDENTE:
                if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getAddressAppliant())) {
                    
                    return request.getTranscriptionActId().getSectionA().getAddressAppliant();
                }else
                    return "";
            case TRASCRIZIONE_DESCRIZIONE_TITOLO:
                if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getTitleDescription())) {
                    
                    return request.getTranscriptionActId().getSectionA().getTitleDescription();
                }else
                    return "";
                
            case TRASCRIZIONE_DATA_TITOLO:
                if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getTitleDate())) {
                    
                    return DateTimeHelper.toFormatedString(request.getTranscriptionActId().getSectionA().getTitleDate(), DateTimeHelper.getXmlDatePattert());
                }else
                    return "";
                
            case TRASCRIZIONE_PUBBLICO_UFFICIALE:
                StringBuffer tag = new StringBuffer();
                if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())) {
                    String codiceComune = "";
                    String denominazionePU = "";
                    String tipoPU = "";
                    if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getSeat())) {
                        
                        String seat = request.getTranscriptionActId().getSectionA().getSeat().split("\\(")[0].trim();
                        
                        List<City> cities = DaoManager.load(City.class,
                                new Criterion[]{
                                        Restrictions.eq("description", seat),
                                        Restrictions.isNotNull("province.id"),
                                        Restrictions.eq("external", Boolean.TRUE)
                                });
                        if(!ValidationHelper.isNullOrEmpty(cities)) {
                            codiceComune = cities.get(0).getCfis();
                        }
                    }
                    
                    if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getPublicOfficialNotary())) {
                        denominazionePU = request.getTranscriptionActId().getSectionA().getPublicOfficialNotary();
                        tipoPU = "1";
                    }else if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getPublicOfficial())) {
                        denominazionePU = request.getTranscriptionActId().getSectionA().getPublicOfficial();
                        tipoPU = "2";
                    }
                    if(!ValidationHelper.isNullOrEmpty(codiceComune) || 
                            !ValidationHelper.isNullOrEmpty(denominazionePU) ||
                            !ValidationHelper.isNullOrEmpty(tipoPU))
                    tag.append("&lt;PubblicoUfficiale");
                    if(!ValidationHelper.isNullOrEmpty(codiceComune)) {
                        tag.append(" CodiceComune&#61;\"");
                        tag.append(codiceComune);
                        tag.append("\"");
                    }
                    if(!ValidationHelper.isNullOrEmpty(denominazionePU)) {
                        tag.append(" DenominazionePU&#61;\"");
                        tag.append(denominazionePU);
                        tag.append("\"");
                    }
                    
                    if(!ValidationHelper.isNullOrEmpty(tipoPU)) {
                        tag.append(" TipoPU&#61;\"");
                        tag.append(tipoPU);
                        tag.append("\"");
                    }
                    tag.append("&#47;&gt;");
                }
                   
                return tag.toString();
                
            case TRASCRIZIONE_ATTO_CODICE_ATTO:
                if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getDerivedFromCode())) {
                    
                    return request.getTranscriptionActId().getSectionA().getDerivedFromCode();
                }else
                    return "";
                
            case TRASCRIZIONE_ATTO_DESCRIZIONE_ATTO:
                String trascrizioneAtto = "";
                
                if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())){
                    
                    if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getConventionDescription())) {
                        trascrizioneAtto = request.getTranscriptionActId().getSectionA().getConventionDescription();
                    }else  if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getAnnotationDescription())) {
                        trascrizioneAtto = request.getTranscriptionActId().getSectionA().getAnnotationDescription();
                    }else  if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getDerivedFrom())) {
                        trascrizioneAtto = request.getTranscriptionActId().getSectionA().getDerivedFrom();
                    }
                    trascrizioneAtto = trascrizioneAtto.trim();
                    if(StringUtils.isNotBlank(trascrizioneAtto) && Character.isDigit(trascrizioneAtto.charAt(0))) {
                        String startDigits = trascrizioneAtto.split("\\s+")[0];
                        trascrizioneAtto = trascrizioneAtto.replaceFirst(startDigits, "").trim();
                    }
                }
                return trascrizioneAtto; 
                
            case TRASCRIZIONE_SPECIE_ATTO:
                String trascrizioneSpecieAtto = "";
                if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())) {
                    
                    if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getConventionSpecies())) {
                        trascrizioneSpecieAtto = request.getTranscriptionActId().getSectionA().getConventionSpecies();
                    }else  if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getMortgageSpecies())) {
                        trascrizioneSpecieAtto = request.getTranscriptionActId().getSectionA().getMortgageSpecies();
                    }else  if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getAnnotationType())) {
                        trascrizioneSpecieAtto = request.getTranscriptionActId().getSectionA().getAnnotationType();
                    }
                    trascrizioneSpecieAtto = trascrizioneSpecieAtto.trim();
                }
                
                return trascrizioneSpecieAtto;
                
            case TRASCRIZIONE_TIPO_NOTA:
                if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                        && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getType())) {
                    if (request.getTranscriptionActId().getType().equalsIgnoreCase("trascrizione")) {
                        return "T";
                    } else if (request.getTranscriptionActId().getType().equalsIgnoreCase("iscrizione")) {
                        return "I";
                    } else if (request.getTranscriptionActId().getType().equalsIgnoreCase("annotazione") ||
                            request.getTranscriptionActId().getType().equalsIgnoreCase("annotamento")) {
                        return "A";
                    }else
                        return "";
                }else
                    return "";
            case TRASCRIZIONE_CODICECONSERVATORIA:
                String code = "";
                try {
               
                String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.CLIENT_CONSERVATIVE_CODE_FILES).getValue();
                if (!ValidationHelper.isNullOrEmpty(path)) {
                    if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                            && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getReclamePropertyService())
                            && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getReclamePropertyService().getName())) {
                        
                        Path xmlFile = Paths.get(path);
                        if(Files.exists(xmlFile)) {
                            Document doc = ImportXMLHelper.prepareDocument(xmlFile.toFile());
                            if (doc != null) {
                                NodeList rows = doc.getElementsByTagName("Row");
                                for (int r = 0; r < rows.getLength(); r++) {
                                    Node nNode = rows.item(r);
                                    org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
                                    if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem("Des"))) {
                                        String attributeValue = namedNodeMap.getNamedItem("Des").getNodeValue();
                                        if(attributeValue.trim().equalsIgnoreCase(request.getTranscriptionActId().getReclamePropertyService().getName())) {
                                            code = namedNodeMap.getNamedItem("Cod").getNodeValue();
                                        }else if(attributeValue.replaceAll("\\s+", "").trim().equalsIgnoreCase(request.getTranscriptionActId().getReclamePropertyService().getName())) {
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
               
               if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())) {
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
                   for(SectionB sectionB : distinctSections) {
                       List<Property> properties = sectionB.getProperties();
                       if(properties == null)
                           properties = new ArrayList<Property>();
                       
                       sectionbuffer.append("&lt;UnitaNegoziali ");
                       sectionbuffer.append("IdImmobile=\"");
                       for(int p=1; p <=properties.size();p++) {
                           
                           String str = String.format("%06d", unitCount++);
                           sectionbuffer.append("I" + str);
                           if(p < properties.size()) {
                               sectionbuffer.append(" ");
                           }
                       }
                       sectionbuffer.append("\" ");
                       sectionbuffer.append("IdUnitaNegoziale=\"U00000" +sectionB.getBargainingUnit() + "\"&#47;&gt;");
                       sectionbuffer.append("<br/>");
                   }
                   negotiationUnits = sectionbuffer.toString();
               }
               return negotiationUnits;
               
           case TRASCRIZIONE_DATI_ATTO:
               StringBuffer da = new StringBuffer();
               if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                       && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getType())) {
                   
                   if(request.getTranscriptionActId().getType().equalsIgnoreCase("trascrizione")) {
                       
                       if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())) {
                           if(ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherTypeFormality())) {
                               da.append("&lt;DatiTrascrizione Condizione=\"0\" ConvenzioneSoggettaVoltura=\"0\"");
                               if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getDeathDate())) {
                                   da.append(" DataMorte=\"");
                                   da.append(DateTimeHelper.toFormatedString(request.getTranscriptionActId().getSectionA().getDeathDate(), DateTimeHelper.getXmlDatePattert()));
                                   da.append("\"");
                               }
                               da.append(" PLquadroA=\"0\" PLquadroB=\"0\" PLquadroC=\"0\" RinunciaTestamentaria=\"0\" SuccessioneTestamentaria=\"0\" TerminiEfficaciaAtto=\"0\" VolturaDifferitaCatastale=\"0\"&#47;&gt;");
                           }else {
                               da.append("&lt;DatiTrascrizione Condizione=\"0\" ConvenzioneSoggettaVoltura=\"0\" PLquadroA=\"1\" PLquadroB=\"1\" PLquadroC=\"1\" RinunciaTestamentaria=\"0\" SuccessioneTestamentaria=\"0\" TerminiEfficaciaAtto=\"0\" VolturaDifferitaCatastale=\"0\"&gt;");
                               da.append("<br/>");
                               da.append("&lt;FormalitaRiferimento");
                               if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherData())) {
                                   da.append(" Data=\"");
                                   da.append(DateTimeHelper.toFormatedString(
                                           request.getTranscriptionActId().getSectionA().getOtherData(), 
                                           DateTimeHelper.getXmlDatePattert(), null));
                                   da.append("\"");
                               }

                               if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherParticularRegister())) {

                                   da.append(" RegistroParticolareUno=\"");
                                   da.append(request.getTranscriptionActId().getSectionA().getOtherParticularRegister());
                                   da.append("\"");
                               }

                               if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherTypeFormality())) {
                                   if(request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("trascrizione")) {
                                       da.append(" TipoNota=\"T\"");        
                                   }else if(request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("iscrizione")) {
                                       da.append(" TipoNota=\"I\"");   
                                   }else {
                                       da.append(" TipoNota=\"A\"");
                                   }
                                   da.append("&gt;");
                               }
                             
                               code = "";
                               try {

                                   String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.CLIENT_CONSERVATIVE_CODE_FILES).getValue();
                                   if (!ValidationHelper.isNullOrEmpty(path)) {
                                       if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                                               && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                                               && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getLandChargesRegistry())) {

                                           Path xmlFile = Paths.get(path);
                                           if(Files.exists(xmlFile)) {
                                               Document doc = ImportXMLHelper.prepareDocument(xmlFile.toFile());
                                               if (doc != null) {
                                                   NodeList rows = doc.getElementsByTagName("Row");
                                                   for (int r = 0; r < rows.getLength(); r++) {
                                                       Node nNode = rows.item(r);
                                                       org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
                                                       if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem("Des"))) {
                                                           String attributeValue = namedNodeMap.getNamedItem("Des").getNodeValue();
                                                           if(attributeValue.trim().equalsIgnoreCase(request.getTranscriptionActId().getSectionA().getLandChargesRegistry().getName())) {
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
                               if(!ValidationHelper.isNullOrEmpty(code)) {
                                   da.append("<br/>");
                                   da.append("&lt;CodiceConservatoria&gt;");
                                   da.append(code);    
                                   da.append("&lt;&#47;CodiceConservatoria&gt;");
                               }
                               da.append("&lt;&#47;FormalitaRiferimento&gt;");

                               da.append("&lt;&#47;DatiTrascrizione&gt;");
                           }
                       }
                   }else  if(request.getTranscriptionActId().getType().equalsIgnoreCase("iscrizione")) {
                       
                       da.append("&lt;DatiIscrizione ");
                       
                       if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) && 
                               !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getCapital())) {
                               da.append("Capitale=\"");
                               da.append(request.getTranscriptionActId().getSectionA().getCapital());
                               da.append("\"");
                       }
                       
                       da.append(" CondizioneRisolutiva=\"0\" ElencoMacchinari=\"0\" ImportiTassiVariabili=\"0\" ImportiValutaEstera=\"0\"");
                       
                       if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) && 
                               !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getInterests())) {
                               da.append(" ImportoInteressi=\"");
                               da.append(request.getTranscriptionActId().getSectionA().getInterests());
                               da.append("\"");
                       }
                       da.append(" PLquadroA=\"1\" PLquadroB=\"1\" PLquadroC=\"1\" SommaIscritta=\"0\"");
                       if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) && 
                               !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getExpense())) {
                               da.append(" SpeseInteressiMora=\"");
                               String expense = request.getTranscriptionActId().getSectionA().getExpense();
                               boolean isCommaorDot = false;
                               if(expense.contains(".")) {
                                   isCommaorDot = true;
                                   expense = expense.replaceAll("\\.", "");
                               }else if(expense.contains(",")) {
                                   isCommaorDot = true;
                                   expense = expense.replaceAll("\\,", "");
                               }
                               if(!isCommaorDot) {
                                   expense = expense + "00";
                               }
                               da.append(expense);
                               da.append("\"");
                       }
                       da.append(" StipulaUnicoContratto=\"0\"");
                       if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) && 
                               !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getTotal())) {
                               da.append(" Totale=\"");
                               da.append(request.getTranscriptionActId().getSectionA().getTotal());
                               da.append("\"");
                       }
                        
                       da.append("&gt;");
                       if(!ValidationHelper.isNullOrEmpty(
                               request.getTranscriptionActId().getSectionA().getOtherTypeFormality())) {
                           da.append("<br/>");
                           da.append("&lt;FormalitaAnnotata");
                           if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherData())) {
                               da.append(" Data=\"");
                               da.append(DateTimeHelper.toFormatedString(
                                       request.getTranscriptionActId().getSectionA().getOtherData(), 
                                       DateTimeHelper.getXmlDatePattert(), null));
                               da.append("\"");
                           }
                           if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherParticularRegister())) {
                               da.append(" RegistroParticolareUno=\"");
                               da.append(request.getTranscriptionActId().getSectionA().getOtherParticularRegister());
                               da.append("\"");
                           }
                           
                           if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherTypeFormality())) {
                               if(request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("trascrizione")) {
                                   da.append(" TipoNota=\"T\"");        
                               }else if(request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("iscrizione")) {
                                   da.append(" TipoNota=\"I\"");   
                               }else {
                                   da.append(" TipoNota=\"A\"");
                               }
                           }
                           da.append("&gt;");
                           code = "";
                           try {

                               String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.CLIENT_CONSERVATIVE_CODE_FILES).getValue();
                               if (!ValidationHelper.isNullOrEmpty(path)) {
                                   if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                                           && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                                           && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getLandChargesRegistry())) {

                                       Path xmlFile = Paths.get(path);
                                       if(Files.exists(xmlFile)) {
                                           Document doc = ImportXMLHelper.prepareDocument(xmlFile.toFile());
                                           if (doc != null) {
                                               NodeList rows = doc.getElementsByTagName("Row");
                                               for (int r = 0; r < rows.getLength(); r++) {
                                                   Node nNode = rows.item(r);
                                                   org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
                                                   if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem("Des"))) {
                                                       String attributeValue = namedNodeMap.getNamedItem("Des").getNodeValue();
                                                       if(attributeValue.trim().equalsIgnoreCase(request.getTranscriptionActId().getSectionA().getLandChargesRegistry().getName())) {
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
                           if(!ValidationHelper.isNullOrEmpty(code)) {
                               da.append("<br/>");
                               da.append("&lt;CodiceConservatoria&gt;");
                               da.append(code);    
                               da.append("&lt;&#47;CodiceConservatoria&gt;");
                           }
                           da.append("&lt;&#47;FormalitaAnnotata&gt;");
                       }
                       da.append("&lt;&#47;DatiIscrizione&gt;");
                       
                   }else  if(request.getTranscriptionActId().getType().equalsIgnoreCase("annotamento") ||
                           request.getTranscriptionActId().getType().equalsIgnoreCase("annotazione")) {
                       da.append("&lt;DatiAnnotazione VolturaCatastaleAttoOrig=\"0\" &gt;");
                       da.append("<br/>");
                       if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA()) &&
                               !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherTypeFormality())) {
                           da.append("&lt;FormalitaAnnotata TipoNotadaAnnotare=\"");
                           if(request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("trascrizione")) {
                               da.append("T\"");        
                           }else if(request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("iscrizione")) {
                               da.append("I\"");   
                           }else if(request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("annotamento") ||
                                   request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("annotazione")) {
                               da.append("A\"");
                           }
                           da.append("&gt;");
                       }
                       
                       if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherData())) {
                           da.append("&lt;FormalitaRiferimento Data=\"");
                           da.append(DateTimeHelper.toFormatedString(
                                   request.getTranscriptionActId().getSectionA().getOtherData(), 
                                   DateTimeHelper.getXmlDatePattert(), null));
                           da.append("\"");
                       }
                       if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherParticularRegister())) {
                           da.append(" RegistroParticolareUno=\"");
                           da.append(request.getTranscriptionActId().getSectionA().getOtherParticularRegister());
                           da.append("\"");
                       }
                       
                       if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getOtherTypeFormality())) {
                           if(request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("trascrizione")) {
                               da.append(" TipoNota=\"T\"");        
                           }else if(request.getTranscriptionActId().getSectionA().getOtherTypeFormality().equalsIgnoreCase("iscrizione")) {
                               da.append(" TipoNota=\"I\"");   
                           }else {
                               da.append(" TipoNota=\"A\"");
                           }
                           da.append("&gt;");
                       }
                     
                      
                       code = "";
                       try {

                           String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.CLIENT_CONSERVATIVE_CODE_FILES).getValue();
                           if (!ValidationHelper.isNullOrEmpty(path)) {
                               if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) 
                                       && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA())
                                       && !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionA().getLandChargesRegistry())) {

                                   Path xmlFile = Paths.get(path);
                                   if(Files.exists(xmlFile)) {
                                       Document doc = ImportXMLHelper.prepareDocument(xmlFile.toFile());
                                       if (doc != null) {
                                           NodeList rows = doc.getElementsByTagName("Row");
                                           for (int r = 0; r < rows.getLength(); r++) {
                                               Node nNode = rows.item(r);
                                               org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
                                               if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem("Des"))) {
                                                   String attributeValue = namedNodeMap.getNamedItem("Des").getNodeValue();
                                                   if(attributeValue.trim().equalsIgnoreCase(request.getTranscriptionActId().getSectionA().getLandChargesRegistry().getName())) {
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
                       if(!ValidationHelper.isNullOrEmpty(code)) {
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
               if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())){
                   List<Relationship> relationships = DaoManager.load(Relationship.class, new Criterion[]{
                           Restrictions.eq("formality", request.getTranscriptionActId())
                   });
                   
                   List<Subject> distinctSubjects = null;
                   
                   if(!ValidationHelper.isNullOrEmpty(relationships)) {
                       distinctSubjects = relationships.stream().
                               filter(r -> Objects.nonNull(
                                       r.getSubject()))
                               .filter(ListHelper.distinctByKey(r -> r.getSubject()))
                               .map(Relationship::getSubject)
                               .collect(Collectors.toList());
                       int x = 0;
                       for(Subject subject : distinctSubjects) {
                           if(da.length() > 0)
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
                          for(Relationship gresult : groupedResult) {
                              if(gindex > 0)
                                  da.append("<br/>");

                              if(!subjects.contains(gresult.getSubject())) {
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

                              if(!ValidationHelper.isNullOrEmpty(gresult.getSectionCType())) {
                                  da.append("Qualifica=\"");
                                  if(gresult.getSectionCType().equals(SectionCType.CONTRO.getName())) {
                                      da.append("CONTRO\"");    
                                  }else if(gresult.getSectionCType().equals(SectionCType.A_FAVORE.getName())) {
                                      da.append("FAVORE\"");    
                                  }
                                  da.append(" TipoQualifica=\"");
                                  if(gresult.getSectionCType().equals(SectionCType.CONTRO.getName())) {
                                      da.append("C\"");    
                                  }else if(gresult.getSectionCType().equals(SectionCType.A_FAVORE.getName())) {
                                      da.append("F\"");    
                                  }
                              }

                              String xmlCode = "";
                              try {
                                  String path = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.TABLE_CONSERVATIVE_CODE_FILES).getValue();
                                  if (!ValidationHelper.isNullOrEmpty(path)) {
                                      if(!ValidationHelper.isNullOrEmpty(gresult.getPropertyType())) {
                                          Path xmlFile = Paths.get(path);
                                          if(Files.exists(xmlFile)) {
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
                                                              if(attributeValue.trim().equalsIgnoreCase(gresult.getPropertyType())) {
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
                              if(!ValidationHelper.isNullOrEmpty(xmlCode)) {
                                  da.append(" CodiceDiritto=\"");
                                  da.append(xmlCode);
                                  da.append("\"");
                              }

                              if(!ValidationHelper.isNullOrEmpty(gresult) && 
                                      !ValidationHelper.isNullOrEmpty(gresult.getPropertyType())) {
                                  da.append(" Descrizione=\"");
                                  da.append(gresult.getPropertyType());
                                  da.append("\"");
                              }
                              if(!ValidationHelper.isNullOrEmpty(gresult) && 
                                      !ValidationHelper.isNullOrEmpty(gresult.getQuote())) {
                                  da.append(" Quota=\"");
                                  da.append(gresult.getQuote());
                                  da.append("\"");
                              }

                              if(!ValidationHelper.isNullOrEmpty(gresult) && 
                                      !ValidationHelper.isNullOrEmpty(gresult.getRegime())) {
                                  Regime regime = DaoManager.get(Regime.class,
                                          new Criterion[]{
                                                  Restrictions.eq("text", gresult.getRegime())});
                                  if(!ValidationHelper.isNullOrEmpty(regime)) {
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
                   }else {
                       List<Subject> sectionCSubjects = new ArrayList<Subject>();
                       for (SectionC sectionC : request.getTranscriptionActId().getSectionC()) {
                           sectionCSubjects.addAll(sectionC.getSubject());
                       }
                       distinctSubjects = sectionCSubjects.stream()
                               .distinct()
                               .collect(Collectors.toList());
                   
                       int x = 1;
                       for(Subject subject : distinctSubjects) {
                           if(da.length() > 0)
                               da.append("<br/>");
                           da.append("&lt;DatiAssociazione IdSoggetto=\"");
                           String xstr = String.format("%06d", x++);
                           da.append("S" + xstr + "\"");
                           da.append(" &gt;");
                           da.append("<br/>");
                           da.append("&lt;DatiTitolarita&gt;");
                           da.append("<br/>");
                           da.append("&lt;Qualifica ");
                           if(!ValidationHelper.isNullOrEmpty(subject.getSectionC())) {
                               SectionC sectionC = subject.getSectionC().get(0);
                               if(!ValidationHelper.isNullOrEmpty(sectionC.getSectionCType())) {
                                   da.append("Qualifica=\"");
                                   if(sectionC.getSectionCType().equals(SectionCType.CONTRO.getName())) {
                                       da.append("CONTRO\"");    
                                   }else if(sectionC.getSectionCType().equals(SectionCType.A_FAVORE.getName())) {
                                       da.append("FAVORE\"");    
                                   }
                                   da.append(" TipoQualifica=\"");
                                   if(sectionC.getSectionCType().equals(SectionCType.CONTRO.getName())) {
                                       da.append("C\"");    
                                   }else if(sectionC.getSectionCType().equals(SectionCType.A_FAVORE.getName())) {
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
               
               if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId()) && 
                       !ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getSectionD())){
                   List<SectionD> sectionDs = request.getTranscriptionActId().getSectionD();
                   for(int s=0; s < sectionDs.size();s++) {
                       SectionD sectionD = request.getTranscriptionActId().getSectionD().get(s);
                       
                       if(!ValidationHelper.isNullOrEmpty(sectionD.getAdditionalInformation())) {
                           final AtomicInteger atomicInteger = new AtomicInteger(0);
                           Collection<String> tokens = sectionD.getAdditionalInformation().chars()
                                                               .mapToObj(c -> String.valueOf((char)c) )
                                                               .collect(Collectors.groupingBy(c -> atomicInteger.getAndIncrement() / 70
                                                                                           ,Collectors.joining()))
                                                               .values();
                           for(String token : tokens) {
                               
                               da.append("&lt;Descrizione&gt;");
                               da.append(token);
                               da.append("&lt;&#47;Descrizione&gt;");
                               da.append("<br/>");
                           }
                       }
                           
                       if(da.length() > 0 && s > 0 && s < sectionDs.size())
                           da.append("<br/>");
                   }
               }
               return da.toString();
           case TRASCRIZIONE_DATII_MMOBILI:
               da = new StringBuffer();
               
               if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())){
                   
                   List<SectionB> sectionBList = DaoManager.load(
                           SectionB.class, new Criterion[]{Restrictions.eq("formality", request.getTranscriptionActId())});
                   
                   List<Property> properties = sectionBList.stream().
                           map(SectionB::getProperties).flatMap(List::stream)
                           .collect(Collectors.toList());
                   
                   
                   if(!ValidationHelper.isNullOrEmpty(properties)) {
                       int unitCount = 1;
                      
                       
                       for(int p=0; p <properties.size();p++) {
                           
                           da.append("&lt;DatiImmobile IdImmobile=\"");
                           Property property = properties.get(p);
                           if(!ValidationHelper.isNullOrEmpty(property.getType())){
                               String str = String.format("%06d", unitCount++);
                               da.append("I");
                               da.append(str);
                               da.append("\"&gt;");
                               da.append("<br/>");
                               da.append("&lt;ImmobileUT");
                               if(!ValidationHelper.isNullOrEmpty(property.getCity()) 
                                       && !ValidationHelper.isNullOrEmpty(property.getCity().getCfis())) {
                                   da.append(" CodiceComune=\"");
                                   da.append(property.getCity().getCfis());
                                   da.append("\"&#47;&gt;");
                               }
                               
                               if(property.getType().equals(RealEstateType.BUILDING.getId())) {
                                   da.append("<br/>");
                                   da.append("&lt;ImmobileU&gt;");                                 
                                   if(!ValidationHelper.isNullOrEmpty(property.getCadastralData())) {
                                       int counter = 1;
                                       for(CadastralData cadastralData : property.getCadastralData()) {
                                           if(property.getCadastralData().size() > 1) {
                                               da.append("<br/>");
                                               da.append("&lt;Graffati&gt;");
                                               da.append("<br/>");
                                               da.append("&lt;ProgGraffato&gt;");
                                               da.append("" + (counter++));
                                               da.append("&lt;&#47;ProgGraffato&gt;");
                                           }
                                           da.append("<br/>");
                                           da.append("&lt;IdentificativoDefinitivo");
                                           if(!ValidationHelper.isNullOrEmpty(cadastralData.getSheet())) {
                                               da.append(" Foglio=\"");
                                               da.append(cadastralData.getSheet());
                                               da.append("\"");
                                           }
                                           if(!ValidationHelper.isNullOrEmpty(cadastralData.getParticle())) {
                                               da.append(" ParticellaUno=\"");
                                               da.append(cadastralData.getParticle());
                                               da.append("\"");
                                           }
                                           if(!ValidationHelper.isNullOrEmpty(cadastralData.getSection())) {
                                               da.append(" SezUrbana=\"");
                                               da.append(cadastralData.getSection());
                                               da.append("\"");
                                           }
                                           if(!ValidationHelper.isNullOrEmpty(cadastralData.getSub())) {
                                               da.append(" SubalternoUno=\"");
                                               da.append(cadastralData.getSub());
                                               da.append("\"");
                                           }
                                           da.append("&#47;&gt;");
                                           if(property.getCadastralData().size() > 1) {
                                               da.append("<br/>");
                                               da.append("&lt;&#47;Graffati&gt;");
                                           }
                                       }
                                   }
                                   da.append("<br/>");
                                   da.append("&lt;ConsistenzaU InteresseSA=\"0\" Legge154=\"0\"&gt;");
                                   da.append("<br/>");
                                   da.append("&lt;ImmobileClassato");
                                   if(!ValidationHelper.isNullOrEmpty(property.getCategory()) &&
                                           !ValidationHelper.isNullOrEmpty(property.getCategory().getCode())) {
                                       da.append(" Categoria=\"");
                                       da.append(property.getCategory().getCode());
                                       da.append("\"");
                                   }
                                   da.append("&gt;");
                                   
                                   if(!ValidationHelper.isNullOrEmpty(property.getConsistency()) &&
                                           property.getConsistency().toLowerCase().contains("vani")) {
                                       da.append("<br/>");
                                       da.append("&lt;Vani&gt;");
                                       da.append(ListHelper.ignoreCaseReplace(property.getConsistency(), "vani", "").trim());
                                       da.append("&lt;&#47;Vani&gt;");
                                   }else  if(!ValidationHelper.isNullOrEmpty(property.getConsistency()) &&
                                           property.getConsistency().toLowerCase().contains("mq")) {
                                       da.append("<br/>");
                                       da.append("&lt;MetriQuadrati&gt;");
                                       da.append(ListHelper.ignoreCaseReplace(property.getConsistency(), "mq", "").trim());
                                       da.append("&lt;&#47;MetriQuadrati&gt;");
                                   }else  if(!ValidationHelper.isNullOrEmpty(property.getConsistency()) &&
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
                                   if(!ValidationHelper.isNullOrEmpty(property.getAddress())) {
                                       da.append(" Indirizzo=\"");
                                       da.append(property.getAddress());
                                       da.append("\"");
                                       
                                   }
                                   if(!ValidationHelper.isNullOrEmpty(property.getFloor())) {
                                       da.append(" Piano=\"");
                                       da.append(property.getFloor());
                                       da.append("\"");
                                   }
                                   da.append("&#47;&gt;");
                                   
                               }else if(property.getType().equals(RealEstateType.LAND.getId())) {
                                   da.append("<br/>");
                                   da.append("&lt;ImmobileT&gt;");
                                   if(!ValidationHelper.isNullOrEmpty(property.getCadastralData())) {
                                       int counter = 1;
                                       for(CadastralData cadastralData : property.getCadastralData()) {
                                           if(property.getCadastralData().size() > 1) {
                                               da.append("<br/>");
                                               da.append("&lt;Graffati&gt;");
                                               da.append("<br/>");
                                               da.append("&lt;ProgGraffato&gt;");
                                               da.append("" + (counter++));
                                               da.append("&lt;&#47;ProgGraffato&gt;");
                                           }
                                           da.append("<br/>");
                                           da.append("&lt;IdentificativoDefinitivo");
                                           if(!ValidationHelper.isNullOrEmpty(cadastralData.getSheet())) {
                                               da.append(" Foglio=\"");
                                               da.append(cadastralData.getSheet());
                                               da.append("\"");
                                           }
                                           if(!ValidationHelper.isNullOrEmpty(cadastralData.getParticle())) {
                                               da.append(" ParticellaUno=\"");
                                               da.append(cadastralData.getParticle());
                                               da.append("\"");
                                           }
                                           if(!ValidationHelper.isNullOrEmpty(cadastralData.getSection())) {
                                               da.append(" SezUrbana=\"");
                                               da.append(cadastralData.getSection());
                                               da.append("\"");
                                           }
                                           if(!ValidationHelper.isNullOrEmpty(cadastralData.getSub())) {
                                               da.append(" SubalternoUno=\"");
                                               da.append(cadastralData.getSub());
                                               da.append("\"");
                                           }
                                           da.append("&#47;&gt;");
                                           if(property.getCadastralData().size() > 1) {
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
                                   if(!ValidationHelper.isNullOrEmpty(property.getAres())){
                                       da.append(" Are=\"");
                                       da.append(property.getAres().intValue());
                                       da.append("\"");
                                   }
                                   if(!ValidationHelper.isNullOrEmpty(property.getCentiares())){
                                       da.append(" Centiare=\"");
                                       da.append(property.getCentiares().intValue());
                                       da.append("\"");
                                   }
                                   if(!ValidationHelper.isNullOrEmpty(property.getHectares())){
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
               if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())){
                   List<Relationship> relationships = DaoManager.load(Relationship.class, new Criterion[]{
                           Restrictions.eq("formality", request.getTranscriptionActId())
                   });
                   List<Subject> distinctSubjects = null;
                   if(!ValidationHelper.isNullOrEmpty(relationships)) {
                       distinctSubjects = relationships.stream().
                               filter(r -> Objects.nonNull(
                                       r.getSubject()))
                               .filter(ListHelper.distinctByKey(r -> r.getSubject()))
                               .map(Relationship::getSubject)
                               .collect(Collectors.toList());
                      
                   }else {
                       List<Subject> sectionCSubjects = new ArrayList<Subject>();
                       for (SectionC sectionC : request.getTranscriptionActId().getSectionC()) {
                           sectionCSubjects.addAll(sectionC.getSubject());
                       }
                       distinctSubjects = sectionCSubjects.stream()
                               .distinct()
                               .collect(Collectors.toList());
                       
                   }
                   
                   int x = 1;
                   for(Subject subject : distinctSubjects) {
                       if(da.length() > 0)
                           da.append("<br/>");
                       relationships = DaoManager.load(Relationship.class, new Criterion[]{
                               Restrictions.eq("subject", subject)
                       });
                       
                       if(subject.getTypeId().equals(1L)) {
                           da.append("&lt;DatiSoggetto IdSoggetto=\"");
                           String xstr = String.format("%06d", x++);
                           da.append("S" + xstr + "\"");
                           da.append(" &gt;");
                           da.append("<br/>");
                           da.append("&lt;SoggettoF");
                           if(!ValidationHelper.isNullOrEmpty(subject.getFiscalCode())) {
                               da.append(" CodiceFiscale=\"");
                               da.append(subject.getFiscalCode());
                               da.append("\"");
                           }
                           if(!ValidationHelper.isNullOrEmpty(subject.getSurname())) {
                               da.append(" Cognome=\"");
                               da.append(subject.getSurname());
                               da.append("\"");
                           }
                           
                           if (!ValidationHelper.isNullOrEmpty(subject.getForeignCountry()) &&
                                   subject.getForeignCountry()) {
                               if(!ValidationHelper.isNullOrEmpty(subject.getCountry()) 
                                       && !ValidationHelper.isNullOrEmpty(subject.getCountry().getDescription())) {
                                   da.append(" ComuneNascita=\"");
                                   da.append(subject.getCountry().getDescription());
                                   da.append("\"");
                               }
                           }else if(!ValidationHelper.isNullOrEmpty(subject.getBirthCityDescription())) {
                               da.append(" ComuneNascita=\"");
                               da.append(subject.getBirthCityDescription());
                               da.append("\"");
                           }
                           
                           if(!ValidationHelper.isNullOrEmpty(subject.getBirthDate())) {
                               da.append(" DataNascita=\"");
                               da.append(DateTimeHelper.toFormatedString(subject.getBirthDate(), DateTimeHelper.getXmlDatePattert()));
                               da.append("\"");
                           }
                           
                           if(!ValidationHelper.isNullOrEmpty(subject.getName())) {
                               da.append(" Nome=\"");
                               da.append(subject.getName());
                               da.append("\"");
                           }
                           
                           if (!ValidationHelper.isNullOrEmpty(subject.getForeignCountry()) &&
                                   subject.getForeignCountry()) {
                               da.append(" Provincia=\"EE\"");
                           }else if(!ValidationHelper.isNullOrEmpty(subject.getBirthProvince()) &&
                                   !ValidationHelper.isNullOrEmpty(subject.getBirthProvince().getCode())) {
                               da.append(" Provincia=\"");
                               da.append(subject.getBirthProvince().getCode());
                               da.append("\"");
                           }
                           
                           if(!ValidationHelper.isNullOrEmpty(subject.getSex())) {
                               da.append(" Sesso=\"");
                               da.append(subject.getSexType().getShortValue());
                               da.append("\"");
                           }
                           da.append("&#47;&gt;");
                           da.append("<br/>");
                           da.append("&lt;&#47;DatiSoggetto&gt;");
                       }else if(subject.getTypeId().equals(2L)) {
                           da.append("&lt;DatiSoggetto IdSoggetto=\"");
                           String xstr = String.format("%06d", x++);
                           da.append("S" + xstr + "\"");
                           da.append(" &gt;");
                           da.append("<br/>");
                           da.append("&lt;SoggettoN");
                           if(!ValidationHelper.isNullOrEmpty(subject.getNumberVAT())) {
                               da.append(" CodiceFiscale=\"");
                               da.append(subject.getNumberVAT());
                               da.append("\"");
                           }
                           if(!ValidationHelper.isNullOrEmpty(subject.getBusinessName())) {
                               da.append(" Denominazione=\"");
                               da.append(subject.getBusinessName());
                               da.append("\"");
                           }
                           
                           if (!ValidationHelper.isNullOrEmpty(subject.getForeignCountry()) &&
                                   subject.getForeignCountry()) {
                               da.append(" Provincia=\"EE\"");
                           }else if(!ValidationHelper.isNullOrEmpty(subject.getBirthProvince()) &&
                                   !ValidationHelper.isNullOrEmpty(subject.getBirthProvince().getCode())) {
                               da.append(" Provincia=\"");
                               da.append(subject.getBirthProvince().getCode());
                               da.append("\"");
                           }
                           
                           
                           if (!ValidationHelper.isNullOrEmpty(subject.getForeignCountry()) &&
                                   subject.getForeignCountry()) {
                               if(!ValidationHelper.isNullOrEmpty(subject.getCountry()) 
                                       && !ValidationHelper.isNullOrEmpty(subject.getCountry().getDescription())) {
                                   da.append(" Sede=\"");
                                   da.append(subject.getCountry().getDescription());
                                   da.append("\"");
                               }
                           }else if(!ValidationHelper.isNullOrEmpty(subject.getBirthCityDescription())) {
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
               if(!ValidationHelper.isNullOrEmpty(path)) {
                   return Paths.get(path).getFileName().toString();
               }
               else
                   return "";
        }
        throw new CannotProcessException("Cannot process such method");

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
